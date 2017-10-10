/*
 * Copyright (c) 2017 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 *
 * Contributors:
 *   Stephan D. Cote 
 *      - Initial concept and implementation
 */
package coyote.dx.reader;

import java.util.Map;

import coyote.commons.network.http.IHTTPSession;
import coyote.commons.network.http.Response;
import coyote.commons.network.http.responder.HTTPDRouter;
import coyote.commons.network.http.responder.Resource;
import coyote.commons.network.http.responder.Responder;
import coyote.dataframe.DataFrame;
import coyote.dx.ConfigurableComponent;
import coyote.dx.FrameReader;
import coyote.dx.FrameValidator;
import coyote.dx.FrameWriter;
import coyote.dx.context.ContextListener;
import coyote.dx.context.OperationalContext;
import coyote.dx.context.TransactionContext;
import coyote.dx.context.TransformContext;
import coyote.dx.web.ResponseFuture;
import coyote.dx.web.ResponseFutureQueue;
import coyote.loader.cfg.Config;
import coyote.loader.log.Log;


/**
 * This reader stands a web server up at a particular port and sends any 
 * received data as a data frame through the transformation pipeline.
 * 
 * <p>Note that the transform is a single thread and multiple requests result 
 * in reach request thread placing messages in a queue for serial processing. 
 * This instance then notifies the responder threads when the transform is 
 * complete and allows them to generate responses based on the state of the 
 * transaction context.
 * 
 * <p>This reader implements the Responder interface and registers this class 
 * with the WebServer so it can place ResponseFuture objects in a queue for 
 * processing which them block the request thread until a listener notifies 
 * that processing is complete which allows the response to be sent after the 
 * request has been processed by the transform engine. 
 */
public class WebServerReader extends AbstractFrameReader implements FrameReader, ConfigurableComponent, Responder, ContextListener {

  private static final int DEFAULT_PORT = 80;

  private static final String PORT = "Port";

  private HTTPDRouter server = null;

  private ResponseFutureQueue futureQueue = new ResponseFutureQueue(1024);




  /**
   * @see coyote.dx.reader.AbstractFrameReader#open(coyote.dx.context.TransformContext)
   */
  @Override
  public void open(final TransformContext context) {
    super.context = context;

    // TODO: find a webserver in the context, and if none is found or we are configured to stand one up ourselves, create one of our own.

    // Register this instance as a context listener so we will be able to 
    // perform processing when the transaction context completes.
    context.addListener(this);
  }




  /**
   * Called by the transform engine thread to read data for processing.
   * 
   * <p>This pulls
   * @see coyote.dx.FrameReader#read(coyote.dx.context.TransactionContext)
   */
  @Override
  public DataFrame read(TransactionContext context) {
    DataFrame retval = null;

    ResponseFuture future = null;
    while (future == null) {
      try {
        future = futureQueue.get(1000);
      } catch (InterruptedException e) {}

      if (future == null) {
        //Check to see if we should shutdown
      } else {
        future.setTransactionContext(context);
        // TODO: retrieve data from it
        // TODO: make sure it is valid - if not update the future with the error and go back to blocking
        // 
      }

    } // while - blocking read

    return retval;
  }




  /**
   * @see coyote.dx.FrameReader#eof()
   */
  @Override
  public boolean eof() {
    return false;
  }




  /**
   * @see coyote.commons.network.http.responder.Responder#delete(coyote.commons.network.http.responder.Resource, java.util.Map, coyote.commons.network.http.IHTTPSession)
   */
  @Override
  public Response delete(Resource resource, Map<String, String> urlParams, IHTTPSession session) {
    return null;
  }




  /**
   * @see coyote.commons.network.http.responder.Responder#get(coyote.commons.network.http.responder.Resource, java.util.Map, coyote.commons.network.http.IHTTPSession)
   */
  @Override
  public Response get(Resource resource, Map<String, String> urlParams, IHTTPSession session) {
    return null;
  }




  /**
   * @see coyote.commons.network.http.responder.Responder#other(java.lang.String, coyote.commons.network.http.responder.Resource, java.util.Map, coyote.commons.network.http.IHTTPSession)
   */
  @Override
  public Response other(String method, Resource resource, Map<String, String> urlParams, IHTTPSession session) {
    return null;
  }




  /**
   * @see coyote.commons.network.http.responder.Responder#post(coyote.commons.network.http.responder.Resource, java.util.Map, coyote.commons.network.http.IHTTPSession)
   */
  @Override
  public Response post(Resource resource, Map<String, String> urlParams, IHTTPSession session) {
    return null;
  }




  /**
   * Called by the web server request thread to queue up data for the reader
   * to process in the transform engine thread.
   * 
   * <p>Once the processing is complete, this thread will then generate a 
   * response based on the state of the transaction context.
   * 
   * @see coyote.commons.network.http.responder.Responder#put(coyote.commons.network.http.responder.Resource, java.util.Map, coyote.commons.network.http.IHTTPSession)
   */
  @Override
  public Response put(Resource resource, Map<String, String> urlParams, IHTTPSession session) {
    try {
      ResponseFuture future = new ResponseFuture(session);
      futureQueue.put(future);
      future.wait(120000);
      TransactionContext txnctx = future.getTransactionContext();
      // TODO: create a response based on the state of the transaction context
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    return null;
  }




  /**
   * @see coyote.dx.context.ContextListener#onEnd(coyote.dx.context.OperationalContext)
   */
  @Override
  public void onEnd(OperationalContext context) {
    if (context instanceof TransactionContext) {
      Log.debug("Notifying responder the transaction is complete");
      // TODO: Notify the WebTransaction that the processing is complete
    }
  }




  /**
   * @see coyote.dx.context.ContextListener#onStart(coyote.dx.context.OperationalContext)
   */
  @Override
  public void onStart(OperationalContext context) {}




  /**
   * @see coyote.dx.context.ContextListener#onWrite(coyote.dx.context.TransactionContext, coyote.dx.FrameWriter)
   */
  @Override
  public void onWrite(TransactionContext context, FrameWriter writer) {}




  /**
   * @see coyote.dx.context.ContextListener#onRead(coyote.dx.context.TransactionContext, coyote.dx.FrameReader)
   */
  @Override
  public void onRead(TransactionContext context, FrameReader reader) {}




  /**
   * @see coyote.dx.context.ContextListener#onError(coyote.dx.context.OperationalContext)
   */
  @Override
  public void onError(OperationalContext context) {}




  /**
   * @see coyote.dx.context.ContextListener#onValidationFailed(coyote.dx.context.OperationalContext, coyote.dx.FrameValidator, java.lang.String)
   */
  @Override
  public void onValidationFailed(OperationalContext context, FrameValidator validator, String msg) {}




  /**
   * @see coyote.dx.context.ContextListener#onFrameValidationFailed(coyote.dx.context.TransactionContext)
   */
  @Override
  public void onFrameValidationFailed(TransactionContext context) {}




  /**
   * @see coyote.dx.context.ContextListener#onMap(coyote.dx.context.TransactionContext)
   */
  @Override
  public void onMap(TransactionContext txnContext) {
    // TODO Auto-generated method stub

  }




  /**
   * @see coyote.dx.ConfigurableComponent#getConfiguration()
   */
  @Override
  public Config getConfiguration() {
    // TODO Auto-generated method stub
    return null;
  }

}
