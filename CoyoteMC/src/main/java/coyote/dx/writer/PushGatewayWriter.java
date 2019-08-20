package coyote.dx.writer;

import coyote.commons.DataFrameUtil;
import coyote.commons.ExceptionUtil;
import coyote.commons.StringUtil;
import coyote.commons.template.Template;
import coyote.dataframe.DataField;
import coyote.dataframe.DataFrame;
import coyote.dx.*;
import coyote.dx.context.TransformContext;
import coyote.dx.web.*;
import coyote.dx.web.auth.AuthenticationException;
import coyote.dx.web.auth.Authenticator;
import coyote.dx.web.auth.NullAuthenticator;
import coyote.loader.cfg.ConfigurationException;
import coyote.loader.log.Log;
import coyote.loader.log.LogMsg;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Date;

/**
 * This writer collects all the metric sample and generates an OpenMetric payload to sent to a Prometheus PushGateway.
 * You can see the metrics on the PushGateway by going to its "metric" endpoint: http://localhost:9091/metrics
 */
public class PushGatewayWriter extends AbstractFrameWriter implements FrameWriter {

  private DataFrame lastRequest = null;
  private Response lastResponse = null;
  private Resource resource = null;

  private Authenticator authenticator = new NullAuthenticator();
  private Proxy proxy = null;
  private Parameters parameters = null;
  private int rowCounter = 0;


  @Override
  public void open(TransformContext context) {
    super.open(context);
    String target = getTarget();
    Log.debug("Using a target of: " + target);



    String targetUrl = getString(ConfigTag.TARGET);
    Log.debug(LogMsg.createMsg(CDX.MSG, "Writer.using_target", this.getClass().getSimpleName(), targetUrl));

    if (StringUtil.isBlank(targetUrl)) {
      context.setError("The Writer configuration did not contain the '" + ConfigTag.TARGET + "' element");
      context.setState("Configuration Error");
      return;
    }

    // Get authenticator, if defined
    for (DataField field : getConfiguration().getFields()) {
      if ((field.getName() != null) && field.getName().equalsIgnoreCase(CWS.AUTHENTICATOR)) {
        if (field.isFrame()) {
          DataFrame cfg = (DataFrame) field.getObjectValue();
          try {
            authenticator = CWS.configAuthenticator(cfg);
            break;
          } catch (ConfigurationException e) {
            Log.fatal(e);
            context.setError("Could not create authenticator: " + e.getMessage());
            return;
          }
        } else {
          Log.error("Invalid authenticator configuration, expected a section not an attribute");
        }
      }
    }

    // Look for the proxy section
    for (DataField field : getConfiguration().getFields()) {
      if ((field.getName() != null) && field.getName().equalsIgnoreCase(CWS.PROXY)) {
        if (field.isFrame()) {
          try {
            proxy = CWS.configProxy((DataFrame) field.getObjectValue());
          } catch (ConfigurationException e) {
            Log.fatal(e);
            context.setError("Could not configure proxy: " + e.getMessage());
            return;
          }
          Log.debug("Found a proxy: " + proxy.toString());
          break;
        } else {
          context.setError("Invalid proxy configuration, expected a section not a scalar");
          context.setState("Configuration Error");
          return;
        }
      }
    }

    // look for a Protocol section
    for (DataField field : getConfiguration().getFields()) {
      if ((field.getName() != null) && field.getName().equalsIgnoreCase(CWS.PROTOCOL)) {
        if (field.isFrame()) {
          try {
            parameters = CWS.configParameters((DataFrame) field.getObjectValue(), getContext());
          } catch (ConfigurationException e) {
            Log.fatal(e);
            context.setError("Could not configure protocol: " + e.getMessage());
            return;
          }
          Log.debug("Found a protocol: " + parameters.toString());
          break;
        } else {
          context.setError("Invalid protocol configuration, expected a section not an attribute");
          context.setState("Configuration Error");
          return;
        }
      }
    }
    try {
      resource = new Resource(targetUrl, parameters, proxy);
      resource.setAuthenticator(authenticator);

      for (DataField field : getConfiguration().getFields()) {
        if (field.getName() != null && field.getName().equalsIgnoreCase(CWS.DECORATOR)) {
          if (field.isFrame()) {
            DataFrame cfgFrame = (DataFrame) field.getObjectValue();
            for (DataField cfgfield : cfgFrame.getFields()) {
              if (cfgfield.isFrame()) {
                if (StringUtil.isNotBlank(cfgfield.getName())) {
                  CWS.configDecorator(cfgfield.getName(), (DataFrame) cfgfield.getObjectValue(), resource, getContext());
                } else {
                  Log.error(LogMsg.createMsg(CWS.MSG, "Decorator.configuration_must_be_named"));
                }
              } else {
                Log.error(LogMsg.createMsg(CWS.MSG, "Decorator.invalid_decorator_configuration_section"));
              }
            }
          } else {
            Log.error(LogMsg.createMsg(CWS.MSG, "Decorator.invalid_decorator_configuration_section"));
          }
        }
      }
      resource.open();
    } catch (IOException e) {
      context.setError("The Writer could not connect the resource: " + e.getMessage());
      context.setState("Connection Error");
      return;
    } catch (AuthenticationException e) {
      context.setError("The Writer could not authenticate the resource: " + e.getMessage());
      context.setState("Authentication Error");
    }


    Log.debug(LogMsg.createMsg(CWS.MSG, "Writer.init_complete", resource));

  }


  /**
   * @see java.io.Closeable#close()
   */
  @Override
  public void close() throws IOException {
    // close our stuff first
    Log.debug(LogMsg.createMsg(CMC.MSG, "Writer.records_processed", rowCounter, (context != null) ? context.getRow() : 0));

    if (resource != null) {
      try {
        resource.close();
      } catch (Exception e) {
        Log.warn(LogMsg.createMsg(CMC.MSG, "Writer.close_error", e.getLocalizedMessage()));
      }
    }

  }


  /**
   * @see coyote.dx.FrameWriter#write(coyote.dataframe.DataFrame)
   */
  @Override
  public void write(DataFrame frame) {
    if (expression != null) {
      try {
        if (evaluator.evaluateBoolean(expression)) {
          if (Log.isLogging(Log.DEBUG_EVENTS)) Log.debug("Condition is true...writing frame to resource");
          writeFrame(frame);
        } else {
          if (Log.isLogging(Log.DEBUG_EVENTS)) Log.debug("Expression evaluated to false - frame not written");
        }
      } catch (final IllegalArgumentException e) {
        Log.warn(LogMsg.createMsg(CMC.MSG, "Writer.boolean_evaluation_error", e.getMessage()));
      }
    } else {
      if (Log.isLogging(Log.DEBUG_EVENTS)) Log.debug("Unconditionally writing frame");
      writeFrame(frame);
    }
  }


  /**
   * This is where we actually write a frame to the web service endpoint
   *
   * @param frame the frame to write
   * @return the number of bytes written
   */
  private int writeFrame(DataFrame frame) {
    int bytesWritten = 0;
    lastRequest = frame;

    if (Log.isLogging(Log.DEBUG_EVENTS)) Log.debug(frame.toString());

    String requestBody = convertDataFrameToOpenMetric(frame);
    parameters.setBody(requestBody);

    String servicePath = "/metrics/job/";

    String labelPath = "";// concatenate labels here

    if (StringUtil.isNotBlank(servicePath)) {
      try {
        resource.setPath(Template.resolve(servicePath, getContext().getTransaction().getSymbols()));
      } catch (URISyntaxException e) {
        super.context.setError("The Writer could not generate URI path: " + e.getMessage());
        super.context.setState("Resource Path Error");
        return bytesWritten;
      }
    }

    try {
      lastResponse = resource.request(parameters);
      while (!lastResponse.isComplete()) {
        if (lastResponse.isTimedOut()) {
          System.err.println("Operation timed-out");
          rowCounter--; // FIXME: hack!
          break;
        } else if (lastResponse.isInError()) {
          System.err.println("Operation failed");
          rowCounter--; // FIXME: hack!
          break;
        } else {
          Thread.sleep(100);
        }
      }

      // TODO: What do we do with the response of other than a 200?

      if (Log.isLogging(Log.DEBUG_EVENTS)) {
        DataFrame auditFrame = new DataFrame();
        auditFrame.add("RequestUrl", resource.getFullURI().toString());
        auditFrame.add("RequestBody", (frame != null) ? frame.toString() : "");
        auditFrame.add("Start", new Date(lastResponse.getOperationStart()));
        auditFrame.add("WriteTime", lastResponse.getOperationTime());
        auditFrame.add("WriteElapsed", lastResponse.getOperationElapsed());
        auditFrame.add("TransactionTime", lastResponse.getTransactionTime());
        auditFrame.add("TransactionElapsed", lastResponse.getTransactionElapsed());
        auditFrame.add("WebResponseTime", lastResponse.getRequestTime());
        auditFrame.add("WebResponseElapsed", lastResponse.getRequestElapsed());
        auditFrame.add("ParsingTime", lastResponse.getParsingTime());
        auditFrame.add("ParsingElapsed", lastResponse.getParsingElapsed());
        auditFrame.add("ResponseCode", lastResponse.getHttpStatusCode());
        auditFrame.add("ResponsePhrase", lastResponse.getHttpStatusPhrase());
        auditFrame.add("Result", (lastResponse.getResult() != null) ? lastResponse.getResult().toString() : "");
        Log.debug(auditFrame.toString());
      }

      rowCounter++;
    } catch (InvocationException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (InterruptedException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } finally {
      if (Log.isLogging(Log.DEBUG_EVENTS)) {
        Log.debug("Performance Metric: Write " + lastResponse.getOperationTime());
        Log.debug("Performance Metric: Transaction " + lastResponse.getTransactionTime());
        Log.debug("Performance Metric: WebResponse " + lastResponse.getRequestTime());
        Log.debug("Performance Metric: Parsing " + lastResponse.getParsingTime());
      }
    }

    return bytesWritten;
  }

  private String convertDataFrameToOpenMetric(DataFrame frame) {
    StringBuffer retval = new StringBuffer();
    retval.append("# HELP simpleMetric This is a simple metric");
    retval.append("# TYPE simpleMetric gauge");
    retval.append("simpleMetric 3");
    return retval.toString();
  }
}
