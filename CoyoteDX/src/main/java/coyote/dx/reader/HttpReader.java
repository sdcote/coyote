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

import coyote.commons.StringUtil;
import coyote.commons.network.MimeType;
import coyote.commons.network.http.HTTPD;
import coyote.commons.network.http.Response;
import coyote.commons.network.http.Status;
import coyote.commons.network.http.auth.GenericAuthProvider;
import coyote.commons.network.http.responder.HTTPDRouter;
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
 * <p>This will start a new thread acting as a listener and a thread for each 
 * request that comes in. Each request thread simply converts the retrieved 
 * data into a DataFrame and places it in the Queue for the reader to return 
 * when requested.
 * 
 * <p>This reader never returns EOF. This means the job will run indefinitely 
 * until the JRE is shut down.
 */
public class HttpReader extends AbstractFrameReader implements FrameReader {
  private static final String DEFAULT_ENDPOINT = "/api";
  private static final String ENDPOINT_TAG = "endpoint";
  private static final String HTTPFUTURE = "HTTPFuture";
  private static final String HTTPLISTENER = "HTTPListener";
  private static final int DEFAULT_PORT = 80;

  private ConcurrentLinkedQueue<HttpFuture> queue = new ConcurrentLinkedQueue<HttpFuture>();

  private HttpListener listener = null;




  /**
   * @see coyote.dx.AbstractConfigurableComponent#setConfiguration(coyote.loader.cfg.Config)
   */
  @Override
  public void setConfiguration(Config cfg) throws ConfigurationException {
    super.setConfiguration(cfg);

    if (getConfiguration().containsIgnoreCase(ConfigTag.PORT)) {
      try {
        getConfiguration().getInt(ConfigTag.PORT);
      } catch (Exception ignore) {
        throw new ConfigurationException(this.getClass().getName() + " configuration contains an invalid port specification of '" + getConfiguration().getString(ConfigTag.PORT) + "'");
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
      lstnr = (HttpListener)context.get(HTTPLISTENER);
    } catch (Throwable ball) {
      // apparently there is no existing server 
    }

    if (lstnr == null) {
      try {
        listener = new HttpListener(getPort(), getConfiguration());
        listener.start();
        context.set(HTTPLISTENER, listener);
        Log.debug("Listening on port " + listener.getPort());
      } catch (IOException e) {
        getContext().setError("Could not start HTTP reader: " + e.getMessage());
        e.printStackTrace();
        return;
      }
    } else {
      listener = lstnr;
    }

    listener.addRoute(getEndpoint(), HttpReaderHandler.class, queue);
    Log.debug("Servicing endpoing '" + getEndpoint() + "'");

    context.addListener(new ResponseGenerator());
  }




  /**
   * @return
   */
  private int getPort() {
    int retval = DEFAULT_PORT;
    if (getConfiguration().containsIgnoreCase(ConfigTag.PORT)) {
      try {
        retval = getConfiguration().getInt(ConfigTag.PORT);
      } catch (Exception ignore) {
        Log.error("Configuration contains an invalid port specification of '" + getConfiguration().getString(ConfigTag.PORT) + "'");
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
      retval = getConfiguration().getString(ENDPOINT_TAG);
    }
    return retval;
  }




  @Override
  public DataFrame read(TransactionContext context) {
    HttpFuture future = queue.poll();
    DataFrame retval = null;

    if (future == null) {
      // wait a short time before returning a null frame to keep the job from running hot
      try {
        Thread.sleep(1000);
      } catch (InterruptedException ignore) {}
    }

    if (future != null) {
      retval = future.getDataFrame();
      context.set(HTTPFUTURE, future);
    }
    return retval;
  }




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

  private class ResponseGenerator extends AbstractListener implements ContextListener {

    /**
     * @see coyote.dx.listener.AbstractListener#onEnd(coyote.dx.context.OperationalContext)
     */
    @Override
    public void onEnd(OperationalContext context) {
      if (context instanceof TransactionContext) {
        Log.debug("Completing HTTP request");
        HttpFuture future = (HttpFuture)context.get(HTTPFUTURE);
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
