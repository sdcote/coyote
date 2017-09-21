/*
 * Copyright (c) 2017 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.dx.reader;

import java.io.IOException;
import java.util.concurrent.ConcurrentLinkedQueue;

import coyote.commons.NetUtil;
import coyote.commons.StringUtil;
import coyote.commons.network.MimeType;
import coyote.commons.network.http.HTTPD;
import coyote.commons.network.http.Response;
import coyote.commons.network.http.Status;
import coyote.commons.network.http.auth.GenericAuthProvider;
import coyote.commons.network.http.responder.HTTPDRouter;
import coyote.commons.template.Template;
import coyote.dataframe.DataField;
import coyote.dataframe.DataFrame;
import coyote.dataframe.DataFrameException;
import coyote.dx.ConfigTag;
import coyote.dx.FrameReader;
import coyote.dx.context.ContextListener;
import coyote.dx.context.OperationalContext;
import coyote.dx.context.TransactionContext;
import coyote.dx.context.TransformContext;
import coyote.dx.http.HttpFuture;
import coyote.dx.http.HttpManager;
import coyote.dx.listener.AbstractListener;
import coyote.loader.cfg.Config;
import coyote.loader.cfg.ConfigurationException;
import coyote.loader.log.Log;


/**
 * This starts listening for HTTP requests on a particular port and converts 
 * those requests into DataFrames.
 * 
 * <p>HttpReader is an event-based reader, never returning EOF it listens for 
 * HTTP requests and generates data frames from the request data. The primary 
 * use case is to create a ReST endpoint to which clients can send and 
 * retrieve data, presumably through a data store or messaging system. 
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
 * <p>If the endpoint contains an optional parameter, this reader will also 
 * attach to the root URL. In the above example, this reader will also respond 
 * to requests for "/coyote" as well as "/coyote/:id".  
 */
public class HttpReader extends AbstractFrameReader implements FrameReader {
  private static final String DEFAULT_ENDPOINT = "/api";
  private static final String ENDPOINT_TAG = "endpoint";
  private static final String HTTP_FUTURE = "HttpFuture";
  private static final String HTTP_METHOD = "HttpMethod";
  private static final String HTTP_LISTENER = "HttpListener";
  private static final String HTTP_ACCEPT_TYPE = "HttpAcceptType";
  private static final String HTTP_CONTENT_TYPE = "HttpContentType";
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

    if (getConfiguration().containsIgnoreCase(ConfigTag.PORT)) {
      if (!Template.appearsToBeATemplate(getString(ConfigTag.PORT))) {
        try {
          getConfiguration().getInt(ConfigTag.PORT);
        } catch (Exception ignore) {
          throw new ConfigurationException(this.getClass().getName() + " configuration contains an invalid port specification of '" + getConfiguration().getString(ConfigTag.PORT) + "'");
        }
      }
    }

    if (getConfiguration().containsIgnoreCase(ConfigTag.TIMEOUT)) {
      if (!Template.appearsToBeATemplate(getString(ConfigTag.TIMEOUT))) {
        try {
          getConfiguration().getInt(ConfigTag.TIMEOUT);
        } catch (Exception ignore) {
          throw new ConfigurationException(this.getClass().getName() + " configuration contains an invalid timeout specification of '" + getConfiguration().getString(ConfigTag.TIMEOUT) + "'");
        }
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
  public void open(TransformContext context) {
    super.open(context);

    HttpListener lstnr = null;
    try {
      lstnr = (HttpListener)context.get(HTTP_LISTENER);
    } catch (Throwable ball) {
      // apparently there is no existing server 
    }

    // try to use any listener cached in the Loader context. This allows for 
    // one HTTP listener to be shared amongst many jobs as might be the case
    // in a Service with multiple Jobs
    try {
      lstnr = (HttpListener)context.getEngine().getContext().get(HTTP_LISTENER);
    } catch (Throwable ball) {
      // apparently there is no existing server 
    }

    if (lstnr == null) {
      try {
        listener = new HttpListener(getPort(), getConfiguration());
        listener.start();
        context.set(HTTP_LISTENER, listener); // transform context
        context.getEngine().getContext().set(HTTP_LISTENER, listener); // loader context
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

    int timeout = getTimeout();
    listener.addRoute(getEndpoint(), HttpReaderHandler.class, queue, timeout);
    Log.debug("Servicing endpoint '" + getEndpoint() + "'");

    // because there may be optional parameters, also listen to the root
    if (getEndpoint().contains(":")) {
      String root = getEndpoint().substring(0, getEndpoint().indexOf(':'));
      if (StringUtil.isNotBlank(root)) {
        if (root.endsWith("/")) {
          root = root.substring(0, root.length() - 1);
        }
        listener.addRoute(root, HttpReaderHandler.class, queue, timeout);
        Log.debug("Also servicing root endpoint '" + root + "'");
      }
    }

    context.addListener(new ResponseGenerator());
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
      context.set(HTTP_FUTURE, future);
      context.set(HTTP_METHOD, future.getMethod().toUpperCase());
      context.set(HTTP_ACCEPT_TYPE, future.getAcceptType());
      context.set(HTTP_CONTENT_TYPE, future.getContentType());
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
        DataFrame authConfig = null;
        for (DataField field : cfg.getFields()) {
          if (StringUtil.equalsIgnoreCase(GenericAuthProvider.AUTH_SECTION, field.getName()) && field.isFrame()) {
            setAuthProvider(new GenericAuthProvider(new Config((DataFrame)field.getObjectValue())));
          }
        }
        configIpACL(cfg.getSection(ConfigTag.IPACL));
        configDosTables(cfg.getSection(ConfigTag.FREQUENCY));
      }
      addDefaultRoutes();
    }

  }

  /**
   * Set the response in the request future when the transaction context is 
   * complete.
   */
  private class ResponseGenerator extends AbstractListener implements ContextListener {

    /**
     * @see coyote.dx.listener.AbstractListener#onEnd(coyote.dx.context.OperationalContext)
     */
    @Override
    public void onEnd(OperationalContext context) {
      if (context instanceof TransactionContext) {
        Log.debug("Completing HTTP request");
        HttpFuture future = (HttpFuture)context.get(HTTP_FUTURE);
        if (future != null) {
          if (context.isInError()) {
            future.setResponse(Response.createFixedLengthResponse(Status.BAD_REQUEST, MimeType.JSON.getType(), "{\"Status\":\"Error\",\"Message\",\"" + context.getErrorMessage() + "\"}"));
          } else {
            future.setResponse(Response.createFixedLengthResponse(Status.OK, MimeType.JSON.getType(), "{\"Status\":\"OK\"}"));
          }
        }
      }
    }

  }

}
