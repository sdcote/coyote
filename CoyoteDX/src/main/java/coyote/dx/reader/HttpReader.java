/*
 * Copyright (c) 2017 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.dx.reader;

import coyote.commons.NetUtil;
import coyote.commons.StringUtil;
import coyote.commons.network.IpAcl;
import coyote.commons.network.MimeType;
import coyote.commons.network.http.HTTP;
import coyote.commons.network.http.HTTPD;
import coyote.commons.network.http.Response;
import coyote.commons.network.http.Status;
import coyote.commons.network.http.auth.GenericAuthProvider;
import coyote.commons.network.http.responder.HTTPDRouter;
import coyote.commons.template.Template;
import coyote.dataframe.DataField;
import coyote.dataframe.DataFrame;
import coyote.dataframe.DataFrameException;
import coyote.dataframe.marshal.JSONMarshaler;
import coyote.dataframe.marshal.XMLMarshaler;
import coyote.dx.ConfigTag;
import coyote.dx.FrameReader;
import coyote.dx.FrameValidator;
import coyote.dx.FrameWriter;
import coyote.dx.context.ContextListener;
import coyote.dx.context.OperationalContext;
import coyote.dx.context.TransactionContext;
import coyote.dx.context.TransformContext;
import coyote.dx.http.HttpFuture;
import coyote.dx.http.HttpManager;
import coyote.dx.listener.AbstractListener;
import coyote.loader.Context;
import coyote.loader.cfg.Config;
import coyote.loader.cfg.ConfigurationException;
import coyote.loader.log.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;


/**
 * This starts listening for HTTP requests on a particular port and converts 
 * those requests into DataFrames.
 * 
 * <p>HttpReader is an event-based reader, never returning EOF, it listens for 
 * HTTP requests (i.e. HttpFuture) and generates data frames from the request 
 * data contained therein. The {@code read(TransactionContext)} method then
 * binds the future to the {@code TransactionContext} and returns that data 
 * frame to be processed by the rest of the components.
 * 
 * <p>This reader registers a context listener ({@code ResponseGenerator}} 
 * when it initializes with the (@code TransformContext} which listens for the 
 * transaction context to end at which time a response is generated and placed 
 * in the future, completing it.
 * 
 * The primary use case is to create a ReST endpoint to which clients can send 
 * and retrieve data, presumably through a data store or messaging system. 
 * Multiple endpoints can be supported with conditions set on transforms and 
 * writers to control processing paths allowing basic CRUD operations for 
 * service endpoints. Custom Transforms can be written to perform more complex 
 * processing.
 * 
 * <p>This will start a new thread acting as a listener and a thread for each 
 * request that comes in. Each request thread simply converts the retrieved 
 * data into a DataFrame and places it in the Queue for the reader to return 
 * when requested.
 * 
 * <p>This reader never returns EOF. This means the job will run indefinitely 
 * until the JRE is shut down.
 * 
 * <p>This reader can be configured thusly:<pre>
 * "Reader" : { 
 *   "class" : "HttpReader",
 *   "port" : 80, 
 *   "timeout" : 5000, 
 *   "endpoint" : "/coyote/:id" 
 * }</pre>
 * 
 * <p>The {@code port} and {@code timeout} arguments are optional and will 
 * default to port 80 and 10000 (10 seconds) if not specified.
 * 
 * <p>The {@code endpoint} argument can be a single endpoint or a comma 
 * separated list of many different endpoints. For example:<pre>
 * "endpoint" : "/api/order/:id, /api/account/:id, /api/user/:id"</pre>
 * 
 * <p>If the endpoint contains an optional parameter, this reader will also 
 * attach to the root URL. In the above example, this reader will also respond 
 * to requests for "/coyote" as well as "/coyote/:id".  
 * 
 * <p>You can have as many parameters as you want: "/api/:object:/:action".
 * 
 * <p>A minimal configuration contains only the class and endpoint:<pre>
 * "Reader": { "class": "HttpReader", "endpoint": "/api/object" }</pre>
 *  */
public class HttpReader extends AbstractFrameReader implements FrameReader {
  private static final String DEFAULT_ENDPOINT = "/api";
  private static final String ENDPOINT_TAG = "endpoint";
  private static final String HTTP_FUTURE = "HttpFuture";
  private static final String HTTP_METHOD = "HttpMethod";
  private static final String HTTP_LISTENER = "HttpListener";
  private static final String HTTP_ACCEPT_TYPE = "HttpAcceptType";
  private static final String HTTP_CONTENT_TYPE = "HttpContentType";
  private static final String HTTP_RESOURCE = "HttpResource";
  private static final String HTTP_REQUEST_URI = "HttpRequestURI";
  public static final String STATUS = "Status";
  public static final String ERROR = "Error";
  public static final String MESSAGE = "Message";
  private static final int DEFAULT_PORT = 80;
  protected static final int DEFAULT_TIMEOUT = 10000;
  private ConcurrentLinkedQueue<HttpFuture> queue = new ConcurrentLinkedQueue<HttpFuture>();
  private HttpListener listener = null;


  /**
   * @see coyote.dx.AbstractConfigurableComponent#setConfiguration(coyote.loader.cfg.Config)
   */
  @Override
  public void setConfiguration(Config cfg) throws ConfigurationException {
    super.setConfiguration(cfg);

    if (getConfiguration().containsIgnoreCase(ConfigTag.PORT) && !Template.appearsToBeATemplate(getString(ConfigTag.PORT))) {
      try {
        getConfiguration().getInt(ConfigTag.PORT);
      } catch (Exception ignore) {
        throw new ConfigurationException(this.getClass().getName() + " configuration contains an invalid port specification of '" + getConfiguration().getString(ConfigTag.PORT) + "'");
      }
    }

    if (getConfiguration().containsIgnoreCase(ConfigTag.TIMEOUT) && !Template.appearsToBeATemplate(getString(ConfigTag.TIMEOUT))) {
      try {
        getConfiguration().getInt(ConfigTag.TIMEOUT);
      } catch (Exception ignore) {
        throw new ConfigurationException(this.getClass().getName() + " configuration contains an invalid timeout specification of '" + getConfiguration().getString(ConfigTag.TIMEOUT) + "'");
      }
    }
  }




  /**
   * @see coyote.dx.reader.AbstractFrameReader#close()
   */
  @Override
  public void close() throws IOException {
    listener.stop();
    super.close();
  }




  /**
   * @see coyote.dx.reader.AbstractFrameReader#open(coyote.dx.context.TransformContext)
   */
  @Override
  public void open(TransformContext transformContext) {
    super.open(transformContext);

    String listenerKey = HTTP_LISTENER + ":" + getPort();
    HttpListener lstnr = null;

    try {
      lstnr = (HttpListener)transformContext.get(listenerKey);
    } catch (Throwable ball) {
      // apparently there is no existing server 
    }

    Context loaderContext = transformContext.getEngine().getLoader().getContext();

    synchronized (loaderContext) {
      // try to use any listener cached in the Loader context. This allows for 
      // one HTTP listener to be shared amongst many jobs as might be the case
      // in a Service with multiple Jobs
      try {
        lstnr = (HttpListener)loaderContext.get(listenerKey);
      } catch (Throwable ball) {
        // apparently there is no existing server 
      }

      if (lstnr == null) {
        try {
          listener = new HttpListener(getPort(), getConfiguration());
          transformContext.set(listenerKey, listener); // transform context
          loaderContext.set(listenerKey, listener);
          listener.start();
          Log.debug("Listening on port " + listener.getPort());
        } catch (IOException e) {
          getContext().setError("Could not start HTTP reader: " + e.getMessage());
          e.printStackTrace();
          return;
        }
      } else {
        listener = lstnr;
        Log.debug("Reusing HTTP Listener on port " + listener.getPort());
      }
    }

    int timeout = getTimeout();

    String endpoint = getEndpoint();
    String[] values = endpoint.split("[,\\s]+");

    if (values.length > 0) {
      for (int x = 0; x < values.length; x++) {
        if (StringUtil.isNotBlank(values[x])) {
          synchronized (listener) {
            listener.addRoute(values[x], HttpReaderHandler.class, queue, timeout);
          }
          Log.debug("Servicing endpoint '" + values[x] + "'");

          // because there may be optional parameters, also listen to the root
          if (values[x].contains(":")) {
            String root = values[x].substring(0, values[x].indexOf(':'));
            if (StringUtil.isNotBlank(root)) {
              if (root.endsWith("/")) {
                root = root.substring(0, root.length() - 1);
              }
              listener.addRoute(root, HttpReaderHandler.class, queue, timeout);
              Log.debug("Also servicing root endpoint '" + root + "'");
            }
          } // contains params
        } // not blank
      } // for each endpoint

      // Because we are adding this listener in the Reader.open() phase, all the 
      // other listeners should have already been added and initialized. This 
      // means this listener should run after all the other listeners defined in 
      // the configuration file.
      transformContext.addListener(new ResponseGenerator());

    } else {
      getContext().setError("No Endpoints for HTTP listener");
    }

  }




  /**
   * Get a port to which this listener should bind.
   * 
   * <p>If no port is configured, the default port of 80 is used. If there are 
   * issues parsing the value, an error is logged and the default of 80 is 
   * returned.
   *   
   * @return the port to which this listener should bind.
   */
  private int getPort() {
    int retval = DEFAULT_PORT;
    if (getConfiguration().containsIgnoreCase(ConfigTag.PORT)) {
      // get the value, resolving it as a template in the process 
      String value = getString(ConfigTag.PORT);
      if (Template.appearsToBeATemplate(value)) {
        Log.error("Could not fully resolve configuration element '" + ConfigTag.PORT + "' (" + value + "), using default value of " + retval);
      } else {
        try {
          retval = Integer.parseInt(value);
          if (NetUtil.validatePort(retval) == 0) {
            retval = DEFAULT_PORT;
            Log.error("Configuration contains an out of range '" + ConfigTag.PORT + "' value of '" + value + "' (" + getConfiguration().getAsString(ConfigTag.PORT) + "), using default value of " + retval);
          }
        } catch (Exception ignore) {
          Log.error("Configuration contains an invalid '" + ConfigTag.PORT + "' value of '" + value + "' (" + getConfiguration().getAsString(ConfigTag.PORT) + "), using default value of " + retval);
        }
      }
    }
    return retval;
  }




  /**
   * @return the number of milliseconds this reader should wait for the engine 
   *         to process the request before returning a response.
   */
  private int getTimeout() {
    int retval = DEFAULT_TIMEOUT;
    if (getConfiguration().containsIgnoreCase(ConfigTag.TIMEOUT)) {
      String value = getString(ConfigTag.TIMEOUT);
      if (Template.appearsToBeATemplate(value)) {
        Log.error("Could not fully resolve configuration element '" + ConfigTag.TIMEOUT + "' (" + value + "), using default value of " + retval);
      } else {
        try {
          retval = Integer.parseInt(value);
        } catch (Exception ignore) {
          Log.error("Configuration contains an invalid '" + ConfigTag.TIMEOUT + "' value of '" + value + "' (" + getConfiguration().getAsString(ConfigTag.TIMEOUT) + "), using value of " + retval);
        }
      }
    }
    return retval;
  }




  /**
   * @return the endpoint this listener is to use. Defaults to "/api"
   */
  private String getEndpoint() {
    String retval = DEFAULT_ENDPOINT;
    if (getConfiguration().containsIgnoreCase(ENDPOINT_TAG)) {
      retval = getString(ENDPOINT_TAG);
    } else {
      Log.error("Configuration did not contain '" + ConfigTag.TIMEOUT + "', using default value of " + retval);
    }
    return retval;
  }




  /**
   * Retrieve the next future from our queue and return the data frame it contains.
   * 
   * <p>If there is no future, this method pauses for a short time to keep the 
   * engine thread from constantly cycling when there is nothing to do.
   * 
   * @see coyote.dx.FrameReader#read(coyote.dx.context.TransactionContext)
   */
  @Override
  public DataFrame read(TransactionContext context) {
    HttpFuture future = queue.poll();
    DataFrame retval = null;

    if (future == null) {
      // wait a short time before returning a null frame to keep the job from running hot
      try {
        Thread.sleep(250);
      } catch (InterruptedException ignore) {}
    }

    if (future != null) {
      retval = future.getDataFrame();

      // Set request arguments in the transaction context
      context.set(HTTP_FUTURE, future);
      context.set(HTTP_METHOD, future.getMethod().toUpperCase());
      context.set(HTTP_ACCEPT_TYPE, future.getAcceptType());
      context.set(HTTP_CONTENT_TYPE, future.getContentType());
      context.set(HTTP_RESOURCE, future.getResource());
      context.set(HTTP_REQUEST_URI, future.getRequestUri());
      if (Log.isLogging(Log.DEBUG_EVENTS)) {
        Log.debug("Processing request for '" + future.getRequestUri() + "'");
      }
    }
    return retval;
  }




  /**
   * Always return false to keep the reader activly reading from the HTTP 
   * server thread.
   * 
   * @see coyote.dx.FrameReader#eof()
   */
  @Override
  public boolean eof() {
    return false;
  }




  /**
   * Get a properly formatted error message base on the given MIME type.
   * 
   * @param errorMessage the test of the error message
   * @param type the MIME type
   * 
   * @return a string formatted according the the given MIME type indicating 
   *         a message.
   */
  public static String getErrorText(String errorMessage, MimeType type) {
    String results;
    DataFrame errorFrame = new DataFrame().set(STATUS, ERROR).set(MESSAGE, errorMessage);
    if (type.equals(MimeType.XML)) {
      results = XMLMarshaler.marshal(errorFrame);
    } else {
      results = JSONMarshaler.marshal(errorFrame);
    }
    return results;
  }

  /**
   * Our HTTP listener
   */
  private class HttpListener extends HTTPDRouter implements HttpManager {

    /**
     * Create the server instance with all the defaults.
     */
    public HttpListener(int port, Config cfg) throws IOException {
      super(port);

      boolean secureServer;
      try {
        secureServer = cfg.getAsBoolean("SecureServer");
      } catch (DataFrameException e1) {
        secureServer = false;
      }

      if (port == 443 || secureServer) {
        try {
          makeSecure(HTTPD.makeSSLSocketFactory("/keystore.jks", "password".toCharArray()), null);
        } catch (IOException e) {
          Log.error("Could not make the server secure: " + e.getMessage());
        }
      }

      if (cfg != null) {
        for (DataField field : cfg.getFields()) {
          if (StringUtil.equalsIgnoreCase(GenericAuthProvider.AUTH_SECTION, field.getName()) && field.isFrame()) {
            setAuthProvider(new GenericAuthProvider(new Config((DataFrame)field.getObjectValue())));
          }
        }

        // For ease of configuration, if there is no ACL, assume default=Allow
        Config aclSection = cfg.getSection(ConfigTag.IPACL);
        if (aclSection != null)
          configIpACL(aclSection);
        else
          setDefaultAllow(true);

        configDosTables(cfg.getSection(ConfigTag.FREQUENCY));
      }
      addDefaultRoutes();
    }

  }

  /**
   * Set the response in the request future when the transaction context is 
   * complete.
   * 
   * <p>The response is expected to be bound in the transaction context. If 
   * there is no result in the context, the target frame is used. If both are 
   * missing, an empty string is returned. 
   */
  private class ResponseGenerator extends AbstractListener implements ContextListener {

    List<String> validationErrors = new ArrayList<String>();




    /**
     * @see coyote.dx.listener.AbstractListener#onEnd(coyote.dx.context.OperationalContext)
     */
    @Override
    public void onEnd(OperationalContext context) {
      if (context instanceof TransactionContext) {
        Log.debug("Completing HTTP request");

        HttpFuture future = (HttpFuture)context.get(HTTP_FUTURE);

        if (future != null) {
          DataFrame result = ((TransactionContext)context).getProcessingResult();
          if (result == null) {
            if (future.isProcessed()) {
              future.setResponse(Response.createFixedLengthResponse(Status.NO_CONTENT, MimeType.TEXT.getType(), null));
            } else {
              future.setResponse(Response.createFixedLengthResponse(Status.NOT_FOUND, MimeType.TEXT.getType(), null));
            }
          } else {
            MimeType type = future.determineResponseType();

            if (context.isInError()) {
              String errorText = HttpReader.getErrorText(context.getErrorMessage(), type);
              future.setResponse(Response.createFixedLengthResponse(Status.BAD_REQUEST, type.getType(), errorText));
            } else {
              String results;

              // format results
              if (result instanceof DataFrame) {
                if (type.equals(MimeType.XML)) {
                  results = XMLMarshaler.marshal((DataFrame)result);
                } else {
                  results = JSONMarshaler.marshal((DataFrame)result);
                }
              } else {
                results = result.toString(); // JSON
              }

              // package the results
              if (StringUtil.isBlank(results)) {
                future.setResponse(Response.createFixedLengthResponse(Status.NO_CONTENT, type.getType(), results));
              } else {
                future.setResponse(Response.createFixedLengthResponse(Status.OK, type.getType(), results));
              }
            }

          }
        }

      }
    }




    /**
     * @see coyote.dx.listener.AbstractListener#onValidationFailed(coyote.dx.context.OperationalContext, coyote.dx.FrameValidator, java.lang.String)
     */
    @Override
    public void onValidationFailed(OperationalContext context, FrameValidator validator, String msg) {
      StringBuffer b = new StringBuffer();
      b.append("Field '");
      b.append(validator.getFieldName());
      b.append("' did not pass '");
      b.append(validator.getClass().getSimpleName());
      b.append("' check: ");
      b.append(validator.getDescription());
      validationErrors.add(b.toString());
    }




    /**
     * @see coyote.dx.listener.AbstractListener#onFrameValidationFailed(coyote.dx.context.TransactionContext)
     */
    @Override
    public void onFrameValidationFailed(TransactionContext context) {
      DataFrame errorFrame = getOrCreateErrorFrame(context);

      // add each validation error to the validation error field in the error frame
      DataFrame errors = new DataFrame();
      for (int x = 0; x < validationErrors.size(); x++) {
        errors.add(validationErrors.get(x));
      }
      errorFrame.put("ValidationError", errors);

      // clear out the collected errors
      validationErrors.clear();
    }




    private DataFrame getOrCreateErrorFrame(TransactionContext context) {
      DataFrame retval = null;
      HttpFuture future = (HttpFuture)context.get(HTTP_FUTURE);
      if (future != null) {
        retval = future.getErrorFrame();
        if (retval == null) {
          retval = new DataFrame();
          future.setErrorFrame(retval);
        }
      } else {
        retval = new DataFrame();
      }
      return retval;
    }




    /**
     * @see coyote.dx.listener.AbstractListener#onWrite(coyote.dx.context.TransactionContext, coyote.dx.FrameWriter)
     */
    @Override
    public void onWrite(TransactionContext context, FrameWriter writer) {
      HttpFuture future = (HttpFuture)context.get(HTTP_FUTURE);
      if (future != null) {
        String method = future.getMethod();
        method = (StringUtil.isNotBlank(method)) ? method.toUpperCase() : "";
        switch (method) {
          case HTTP.METHOD_POST: {
            future.setProcessed(true);
            break;
          }
          case HTTP.METHOD_PUT: {
            future.setProcessed(true);
            break;
          }
          default: {
            break;
          }
        }
      }
    }

  }

}
