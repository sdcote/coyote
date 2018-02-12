/*
 * Copyright (c) 2016 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 *
 * Contributors:
 *   Stephan D. Cote 
 *      - Initial concept and implementation
 */
package coyote.dx.writer;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import coyote.commons.DataFrameUtil;
import coyote.commons.ExceptionUtil;
import coyote.commons.StringUtil;
import coyote.commons.template.Template;
import coyote.dataframe.DataField;
import coyote.dataframe.DataFrame;
import coyote.dx.AbstractConfigurableComponent;
import coyote.dx.CDX;
import coyote.dx.CWS;
import coyote.dx.ConfigTag;
import coyote.dx.ConfigurableComponent;
import coyote.dx.FrameWriter;
import coyote.dx.context.TransformContext;
import coyote.dx.eval.Evaluator;
import coyote.dx.web.InvocationException;
import coyote.dx.web.Parameters;
import coyote.dx.web.Proxy;
import coyote.dx.web.Resource;
import coyote.dx.web.Response;
import coyote.dx.web.auth.AuthenticationException;
import coyote.dx.web.auth.Authenticator;
import coyote.dx.web.auth.NullAuthenticator;
import coyote.loader.cfg.Config;
import coyote.loader.cfg.ConfigurationException;
import coyote.loader.log.Log;
import coyote.loader.log.LogMsg;


/**
 * This component simply PUTs or POSTs the data frame to a particular service 
 * endpoint.
 * 
 * <p>All responses can be written to a file (or other transport) through the 
 * ResposeWriter. The results of making web service calls then become the 
 * input for other jobs. Any transform writer can be specified as it will be 
 * called in the exact same manner as it would be within a transform as a top-
 * level writer.
 */
public class WebServiceWriter extends AbstractConfigurableComponent implements FrameWriter, ConfigurableComponent {
  /** Constant to assist in determining the full class name of writers */
  private static final String WRITER_PKG = AbstractFrameWriter.class.getPackage().getName();

  private Evaluator evaluator = new Evaluator();
  private String expression = null;
  private String servicePath = null;
  private int rowCounter = 0;
  private DataFrame lastRequest = null;
  private Response lastResponse = null;
  private Resource resource = null;
  private Authenticator authenticator = new NullAuthenticator();
  private Proxy proxy = null;
  private Parameters parameters = null;
  protected List<FrameWriter> writers = new ArrayList<FrameWriter>();




  @Override
  public void setConfiguration(Config cfg) throws ConfigurationException {
    super.setConfiguration(cfg);

    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
    // Look for a conditional statement the writer may use to control if it is 
    // to write the record or not
    expression = getConfiguration().getString(ConfigTag.CONDITION);
    if (StringUtil.isNotBlank(expression)) {
      expression = expression.trim();

      try {
        evaluator.evaluateBoolean(expression);
      } catch (final IllegalArgumentException e) {
        context.setError("Invalid boolean expression in writer: " + e.getMessage());
      }
    }

    // look for a path
    servicePath = getConfiguration().getString(ConfigTag.PATH);
  }




  /**
   * @see coyote.dx.Component#open(coyote.dx.context.TransformContext)
   */
  @Override
  public void open(TransformContext context) {
    setContext(context);

    // get the resource from the context, and if it does not exist, create one 
    // for subsequent use.
    if (resource == null) {

      // If we don't have an existing instance, create our own
      if (resource == null) {
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
              DataFrame cfg = (DataFrame)field.getObjectValue();
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
                proxy = CWS.configProxy((DataFrame)field.getObjectValue());
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
                parameters = CWS.configParameters((DataFrame)field.getObjectValue(), getContext());
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

          // Now look for Request Decorators 
          for (DataField field : getConfiguration().getFields()) {
            if (field.getName() != null && field.getName().equalsIgnoreCase(CWS.DECORATOR)) {
              if (field.isFrame()) {
                DataFrame cfgFrame = (DataFrame)field.getObjectValue();
                for (DataField cfgfield : cfgFrame.getFields()) {
                  if (cfgfield.isFrame()) {
                    if (StringUtil.isNotBlank(cfgfield.getName())) {
                      CWS.configDecorator(cfgfield.getName(), (DataFrame)cfgfield.getObjectValue(), resource, getContext());
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

          for (DataField field : getConfiguration().getFields()) {
            if (StringUtil.equalsIgnoreCase(CWS.RESPONSE_WRITER, field.getName())) {
              if (field.isFrame()) {
                DataFrame cfg = (DataFrame)field.getObjectValue();
                if (cfg != null) {
                  String className = DataFrameUtil.findString(ConfigTag.CLASS, cfg);
                  if (className != null && StringUtil.countOccurrencesOf(className, ".") < 1) {
                    className = WRITER_PKG + "." + className;
                    cfg.put(ConfigTag.CLASS, className);
                  }
                  Object object = CDX.createComponent(cfg);
                  if (object != null) {
                    if (object instanceof FrameWriter) {
                      writers.add((FrameWriter)object);
                      Log.debug(LogMsg.createMsg(CWS.MSG, "Writer.created_writer", object.getClass().getName()));
                    } else {
                      Log.error(LogMsg.createMsg(CWS.MSG, "Writer.specified_class_is_not_a_writer", object.getClass().getName()));
                    }
                  } else {
                    Log.error(LogMsg.createMsg(CWS.MSG, "Writer.could_not_create_instance_of_specified_writer", className));
                  }
                }
              } else {
                Log.error("Invalid writer configuration section");
              }
            }

            if (field.getName() != null && field.getName().equalsIgnoreCase(CWS.DECORATOR)) {
              if (field.isFrame()) {
                DataFrame cfgFrame = (DataFrame)field.getObjectValue();
                for (DataField cfgfield : cfgFrame.getFields()) {
                  if (cfgfield.isFrame()) {
                    if (StringUtil.isNotBlank(cfgfield.getName())) {
                      CWS.configDecorator(cfgfield.getName(), (DataFrame)cfgfield.getObjectValue(), resource, getContext());
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

          for (FrameWriter writer : writers) {
            writer.open(getContext());
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
      } else {
        Log.debug("Using resource set in the transform context");
      }
    } else {
      Log.debug("Using existing resource");
    }

    Log.debug(LogMsg.createMsg(CWS.MSG, "Writer.init_complete", resource));
  }




  /**
   * @see coyote.dx.FrameWriter#write(coyote.dataframe.DataFrame)
   */
  @Override
  public void write(DataFrame frame) {
    // If there is a conditional expression
    if (expression != null) {

      try {
        // if the condition evaluates to true
        if (evaluator.evaluateBoolean(expression)) {
          Log.debug("Condition is true...writing frame to resource");
          writeFrame(frame);
        } else {
          if (Log.isLogging(Log.DEBUG_EVENTS)) {
            Log.debug("Expression evaluated to false - frame not written");
          }
        }
      } catch (final IllegalArgumentException e) {
        Log.warn(LogMsg.createMsg(CWS.MSG, "Writer.boolean_evaluation_error", e.getMessage()));
      }
    } else {
      Log.debug("Unconditionally writing frame");
      writeFrame(frame);
    }
  }




  /**
   * This is where we actually write a frame to the web service endpoint
   * 
   * @param frame the frame to write
   * 
   * @return the number of bytes written
   */
  private int writeFrame(DataFrame frame) {
    int bytesWritten = 0;
    lastRequest = frame;

    Log.debug(frame.toString());
    // Set up an object to hold our request parameters these will over-ride 
    // the default protocol parameters
    //Parameters params = new Parameters();

    // Place the request payload in the request parameters
    parameters.setPayload(frame);

    // Treat the resource URI as a template, substituting variables in the URI 
    // (e.g. ReST identifiers in the path) for data in the transaction context
    // which may heve been changed by listeners or other components to ensure 
    // the data was written to the correct ReSTful resource URI.
    // This sets the path portion of the resource using a template
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
      // invoke the operation and receive a future object representing our results
      lastResponse = resource.request(parameters);

      // wait for results (invocation may be asynchronous)
      while (!lastResponse.isComplete()) {
        if (lastResponse.isTimedOut()) {
          // nothing happened
          System.err.println("Operation timed-out");
          rowCounter--; // FIXME: hack!
          break;
        } else if (lastResponse.isInError()) {
          // we received one or more errors
          System.err.println("Operation failed");
          rowCounter--; // FIXME: hack!
          break;
        } else {
          // wait for the results to arrive
          Thread.sleep(100);
        }
      }

      // TODO: What do we do with the response of other than a 200?

      DataFrame auditFrame = new DataFrame();
      auditFrame.add("RequestUrl", resource.getFullURI().toString());
      auditFrame.add("RequestBody", frame);
      auditFrame.add("Start", new Date(lastResponse.getOperationStart()));
      auditFrame.add("ElapsedTime", lastResponse.getOperationElapsed());
      auditFrame.add("WriteTime", lastResponse.getOperationTime());
      auditFrame.add("TransactionTime", lastResponse.getTransactionTime());
      auditFrame.add("WebResponseTime", lastResponse.getRequestTime());
      auditFrame.add("ParsingTime", lastResponse.getParsingTime());
      auditFrame.add("Result", lastResponse.getResult());

      // Write the response frame to all the configured sub-writers
      if (writers.size() > 0) {
        for (FrameWriter writer : writers) {
          try {
            // Write the target (new) frame
            writer.write(auditFrame);
          } catch (Exception e) {
            Log.error(LogMsg.createMsg(CDX.MSG, "Engine.write_error", e.getClass().getSimpleName(), e.getMessage(), ExceptionUtil.stackTrace(e)));
            e.printStackTrace();
          }
        }
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
   * @see java.io.Closeable#close()
   */
  @Override
  public void close() throws IOException {
    // close our stuff first
    Log.debug(LogMsg.createMsg(CWS.MSG, "Writer.records_processed", rowCounter, (context != null) ? context.getRow() : 0));

    if (resource != null) {
      try {
        resource.close();
      } catch (Exception e) {
        Log.warn(LogMsg.createMsg(CWS.MSG, "Writer.close_error", e.getLocalizedMessage()));
      }
    }

    for (FrameWriter writer : writers) {
      try {
        writer.close();
      } catch (Exception e) {
        Log.warn(LogMsg.createMsg(CDX.MSG, "Engine.problems_closing_writer", writer.getClass().getName(), e.getClass().getSimpleName(), e.getMessage()));
      }
    }

  }




  /**
   * @return the last response generated from the last write operation
   */
  public Response getLastResponse() {
    return lastResponse;
  }




  /**
   * @return the last Request payload
   */
  public DataFrame getLastRequest() {
    return lastRequest;
  }

}
