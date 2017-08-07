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

import coyote.commons.network.http.HTTPD;
import coyote.commons.network.http.auth.GenericAuthProvider;
import coyote.commons.network.http.responder.HTTPDRouter;
import coyote.commons.network.http.responder.Responder;
import coyote.dataframe.DataField;
import coyote.dataframe.DataFrame;
import coyote.dataframe.DataFrameException;
import coyote.dx.ConfigTag;
import coyote.dx.FrameReader;
import coyote.dx.context.TransactionContext;
import coyote.dx.context.TransformContext;
import coyote.dx.http.HttpManager;
import coyote.dx.http.responder.AbstractBatchResponder;
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

  ConcurrentLinkedQueue<DataFrame> queue = new ConcurrentLinkedQueue<DataFrame>();

  HttpListener listener = null;
  int port = 80;
  Config cfg = null;




  /**
   * @see coyote.dx.AbstractConfigurableComponent#setConfiguration(coyote.loader.cfg.Config)
   */
  @Override
  public void setConfiguration(Config cfg) throws ConfigurationException {
    super.setConfiguration(cfg);
  }




  /**
   * @see coyote.dx.reader.AbstractFrameReader#open(coyote.dx.context.TransformContext)
   */
  @Override
  public void open(TransformContext context) {
    open(context);

    // Start the web server 
    try {
      listener = new HttpListener(port, cfg, this);
    } catch (IOException e) {
      getContext().setError("Could not start HTTP reader: " + e.getMessage());
      e.printStackTrace();
      return;
    }
  }




  @Override
  public DataFrame read(TransactionContext context) {
    DataFrame retval = queue.poll();

    if (retval == null) {
      // wait a short time before returning a null frame to keep the job from running hot
      try {
        Thread.sleep(1000);
      } catch (InterruptedException ignore) {}
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
     * 
     * @param reader the reader that services our requests
     */
    public HttpListener(int port, Config cfg, HttpReader reader) throws IOException {
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

      if (reader == null)
        throw new IllegalArgumentException("Cannot create HttpManager without a service reference");

      if (cfg != null) {
        DataFrame authConfig = null;
        for (DataField field : cfg.getFields()) {
          if (GenericAuthProvider.AUTH_SECTION.equalsIgnoreCase(field.getName()) && field.isFrame()) {
            setAuthProvider(new GenericAuthProvider(new Config((DataFrame)field.getObjectValue())));
          }
        }
        configIpACL(cfg.getSection(ConfigTag.IPACL));
        configDosTables(cfg.getSection(ConfigTag.FREQUENCY));
      }

      addDefaultRoutes();
      addRoute("/api", RequestHandler.class); // TODO: get from configuration
    }
  }

  private class RequestHandler extends AbstractBatchResponder implements Responder {

  }

}
