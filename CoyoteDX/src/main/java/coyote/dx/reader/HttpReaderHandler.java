/*
 * Copyright (c) 2017 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.dx.reader;

import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

import coyote.commons.network.MimeType;
import coyote.commons.network.http.Body;
import coyote.commons.network.http.IHTTPSession;
import coyote.commons.network.http.Response;
import coyote.commons.network.http.Status;
import coyote.commons.network.http.responder.Resource;
import coyote.commons.network.http.responder.Responder;
import coyote.dataframe.DataFrame;
import coyote.dx.http.HttpFuture;
import coyote.dx.http.responder.AbstractBatchResponder;


/**
 * 
 */
public class HttpReaderHandler extends AbstractBatchResponder implements Responder {

  /**
   * @see coyote.dx.http.responder.AbstractBatchResponder#delete(coyote.commons.network.http.responder.Resource, java.util.Map, coyote.commons.network.http.IHTTPSession)
   */
  @Override
  public Response delete(Resource resource, Map<String, String> urlParams, IHTTPSession session) {
    ConcurrentLinkedQueue<HttpFuture> queue = resource.initParameter(0, ConcurrentLinkedQueue.class);
    return handleRequest("DELETE", session, queue);
  }




  /**
   * @see coyote.dx.http.responder.AbstractBatchResponder#get(coyote.commons.network.http.responder.Resource, java.util.Map, coyote.commons.network.http.IHTTPSession)
   */
  @Override
  public Response get(Resource resource, Map<String, String> urlParams, IHTTPSession session) {
    ConcurrentLinkedQueue<HttpFuture> queue = resource.initParameter(0, ConcurrentLinkedQueue.class);
    return handleRequest("GET", session, queue);
  }




  /**
   * @see coyote.dx.http.responder.AbstractBatchResponder#other(java.lang.String, coyote.commons.network.http.responder.Resource, java.util.Map, coyote.commons.network.http.IHTTPSession)
   */
  @Override
  public Response other(String method, Resource resource, Map<String, String> urlParams, IHTTPSession session) {
    ConcurrentLinkedQueue<HttpFuture> queue = resource.initParameter(0, ConcurrentLinkedQueue.class);
    return handleRequest(method, session, queue);
  }




  /**
   * @see coyote.dx.http.responder.AbstractBatchResponder#post(coyote.commons.network.http.responder.Resource, java.util.Map, coyote.commons.network.http.IHTTPSession)
   */
  @Override
  public Response post(Resource resource, Map<String, String> urlParams, IHTTPSession session) {
    ConcurrentLinkedQueue<HttpFuture> queue = resource.initParameter(0, ConcurrentLinkedQueue.class);
    return handleRequest("POST", session, queue);
  }




  /**
   * @see coyote.dx.http.responder.AbstractBatchResponder#put(coyote.commons.network.http.responder.Resource, java.util.Map, coyote.commons.network.http.IHTTPSession)
   */
  @Override
  public Response put(Resource resource, Map<String, String> urlParams, IHTTPSession session) {
    ConcurrentLinkedQueue<HttpFuture> queue = resource.initParameter(0, ConcurrentLinkedQueue.class);
    return handleRequest("PUT", session, queue);
  }




  /**
   * This creates a future object and places it in the queue for the reader to process.
   *
   * @param method
   * @param session
   * @return
   */
  private Response handleRequest(String method, IHTTPSession session, ConcurrentLinkedQueue<HttpFuture> queue) {
    HttpFuture future = new HttpFuture();
    future.setMethod(method);
    Response retval = null;

    // parse the body of the message into a dataframe
    try {
      Body body = session.parseBody();
      System.out.println("Body contains " + body.size() + " entities");
      future.setFrame(new DataFrame().set("Message", "This is still in development"));
      for (final String key : body.keySet()) {
        Object obj = body.get(key);
      }
    } catch (final Exception e) {
      e.printStackTrace();
      retval = Response.createFixedLengthResponse(Status.BAD_REQUEST, MimeType.JSON.getType(), "Problems parsing body: " + e.getMessage());
    }

    // add the future to the queue for the reader to send through the engine
    queue.add(future);

    // wait for the future to be processed by the reader/engine
    retval = future.getResponse(10000);

    if (retval == null) {
      Response.createFixedLengthResponse(Status.UNAVAILABLE, MimeType.JSON.getType(), getText());
    }

    return retval;
  }

}