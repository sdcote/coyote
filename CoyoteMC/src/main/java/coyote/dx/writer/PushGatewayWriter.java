package coyote.dx.writer;

import coyote.commons.DataFrameUtil;
import coyote.commons.StringUtil;
import coyote.commons.network.http.Method;
import coyote.dataframe.DataField;
import coyote.dataframe.DataFrame;
import coyote.dx.*;
import coyote.dx.context.TransformContext;
import coyote.dx.web.*;
import coyote.dx.web.auth.AuthenticationException;
import coyote.dx.web.auth.Authenticator;
import coyote.dx.web.auth.NullAuthenticator;
import coyote.loader.cfg.Config;
import coyote.loader.cfg.ConfigurationException;
import coyote.loader.log.Log;
import coyote.loader.log.LogMsg;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * This writer collects all the metric sample and generates an OpenMetric payload to sent to a Prometheus PushGateway.
 * You can see the metrics on the PushGateway by going to its "metric" endpoint: http://localhost:9091/metrics
 */
public class PushGatewayWriter extends AbstractFrameWriter implements FrameWriter {

  private static final String DEFAULT_JOB_NAME = "coyotemc";
  private DataFrame lastRequest = null;
  private Response lastResponse = null;
  private Resource resource = null;

  private Authenticator authenticator = new NullAuthenticator();
  private Proxy proxy = null;
  private Parameters parameters = null;
  private int rowCounter = 0;
  Map<String, MetricDefinition> metricMap = new HashMap<>();

  @Override
  public void open(TransformContext context) {
    super.open(context);


    // $ echo "cpu_utilization 20.25" | curl --data-binary @- http://localhost:9091/metrics/job/my_custom_metrics/instance/10.20.0.1:9000/provider/hetzner
    // target = http://localhost:9091/metrics
    // servicePath = /job/my_custom_metrics/instance/10.20.0.1:9000/provider/hetzner  this is the part we calculate based on the labels
    // see: https://blog.ruanbekker.com/blog/2019/05/17/install-pushgateway-to-expose-metrics-to-prometheus/
    //  https://prometheus.io/docs/instrumenting/exposition_formats/


    String targetUrl = getTarget();
    if (StringUtil.isBlank(targetUrl)) {
      context.setError("The Writer configuration did not contain the '" + ConfigTag.TARGET + "' element");
      context.setState("Configuration Error");
      return;
    } else {
      Log.debug(LogMsg.createMsg(CDX.MSG, "Writer.using_target", this.getClass().getSimpleName(), targetUrl));
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

    Config section = getConfiguration().getSection(ConfigTag.FIELDS);
    for (String name : section.getNames()) {
      Config cfg = section.getSection(name);
      Log.debug("Field: " + name + ":" + cfg.toString());
      MetricDefinition metricDef = new MetricDefinition(name);
      metricMap.put(metricDef.getName(), metricDef);
      metricDef.setHelpText(cfg.getString(ConfigTag.HELP));
      metricDef.setMetricType(cfg.getString(ConfigTag.TYPE));
    }

    if (metricMap.size() == 0) {
      context.setError("The " + this.getClass().getSimpleName() + " did not have any metric field definitions");
      context.setState("Configuration Error");
    }

    parameters = new Parameters().setExchangeType(ExchangeType.BASIC).setMethod(Method.POST);

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
    parameters.setBody(convertDataFrameToOpenMetric(frame));
    String servicePath = calculateServicePath(frame);

    try {
      resource.setPath(servicePath);
    } catch (URISyntaxException e) {
      super.context.setError("The Writer could not generate URI path: " + e.getMessage());
      super.context.setState("Resource Path Error");
      return bytesWritten;
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

  /**
   * @param frame the frame containing the record data
   * @return a push gateway URL path representing the values in the record
   */
  private String calculateServicePath(DataFrame frame) {
    StringBuffer retval = new StringBuffer("/metrics/job/");
    retval.append(findString(ConfigTag.JOB, frame, DEFAULT_JOB_NAME));
    for (String name : frame.getNames()) {
      if (!ConfigTag.JOB.equalsIgnoreCase(name) && !ConfigTag.NAME.equalsIgnoreCase(name) && !ConfigTag.VALUE.equalsIgnoreCase(name)) {
        retval.append('/');
        retval.append(name);
        retval.append('/');
        retval.append(frame.getAsString(name));
      }
    }
    return retval.toString();
  }

  private String convertDataFrameToOpenMetric(DataFrame frame) {
    StringBuffer retval = new StringBuffer();
    String metricName = DataFrameUtil.findString(ConfigTag.NAME, frame);
    if (metricName != null) {
      MetricDefinition metricDef = metricMap.get(metricName);
      if (metricDef != null) {
        if (metricDef.getHelpText() != null) {
          retval.append("# HELP ");
          retval.append(metricName);
          retval.append(" ");
          retval.append(metricDef.getHelpText());
          retval.append("\n");
        }
        if (metricDef.getMetricType() != null) {
          retval.append("# TYPE ");
          retval.append(metricName);
          retval.append(" ");
          retval.append(metricDef.getMetricType());
          retval.append("\n");
        }
        retval.append(metricName);
        retval.append(getMetricLabels(frame));
        retval.append(" ");
        retval.append(DataFrameUtil.findString(ConfigTag.VALUE, frame));
        retval.append("\n");
      } else {
        Log.error(this.getClass().getSimpleName() + " did not have a file definition for named metric: " + metricName);
      }
    } else {
      Log.error("Cannot convert anonymous frame into OpenMetric: " + frame.toString());
    }
    return retval.toString();
  }


  private String getMetricLabels(DataFrame frame) {
    StringBuilder sb = new StringBuilder();
    Map<String, String> labelMap = new HashMap<>();
    for (String name : frame.getNames()) {
      if (StringUtil.isNotBlank(name) && !ConfigTag.JOB.equalsIgnoreCase(name) && !ConfigTag.NAME.equalsIgnoreCase(name) && !ConfigTag.VALUE.equalsIgnoreCase(name)) {
        String value = frame.getAsString(name);
        if (StringUtil.isNotBlank(value)) {
          labelMap.put(name, value);
        }
      }
    }
    if( labelMap.size()>0){
      sb.append('{');
      int labelCount = labelMap.size();
      int count =0;
      for( String label: labelMap.keySet()){
        count++;
        sb.append(label);
        sb.append("=\"");
        sb.append(labelMap.get(label));
        sb.append('"');
        if( count<labelCount) sb.append(',');
      }
      sb.append('}');
    }
    return sb.toString();
  }


  public static String findString(String name, DataFrame frame, String defaultValue) {
    String retval = defaultValue;
    if (name != null) {
      for (DataField field : frame.getFields()) {
        if (equalsIgnoreCase(name, field.getName())) {
          retval = field.getStringValue();
          break;
        }
      }
    }
    return retval;
  }

  private static boolean equalsIgnoreCase(String source, String target) {
    boolean retval;
    if (source == null) {
      retval = target == null;
    } else {
      if (target == null) {
        retval = false;
      } else {
        retval = source.equalsIgnoreCase(target);
      }
    }
    return retval;
  }

  /**
   * Class to hold information about a named metric
   */
  private class MetricDefinition {
    private String name;
    private String helpText = null;
    private String metricType = null;

    MetricDefinition(String name) {
      this.name = name;
    }

    public String getHelpText() {
      return helpText;
    }

    public MetricDefinition setHelpText(String helpText) {
      this.helpText = helpText;
      return this;
    }

    public String getMetricType() {
      return metricType;
    }

    public MetricDefinition setMetricType(String metricType) {
      this.metricType = metricType;
      return this;
    }

    public String getName() {
      return name;
    }
  }

}
