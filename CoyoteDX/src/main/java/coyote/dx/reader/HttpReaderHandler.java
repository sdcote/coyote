/*
 * Copyright (c) 2017 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.dx.reader;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

import coyote.commons.network.MimeType;
import coyote.commons.network.http.Body;
import coyote.commons.network.http.IHTTPSession;
import coyote.commons.network.http.Method;
import coyote.commons.network.http.Response;
import coyote.commons.network.http.Status;
import coyote.commons.network.http.responder.Resource;
import coyote.commons.network.http.responder.Responder;
import coyote.dataframe.DataFrame;
import coyote.dataframe.marshal.JSONMarshaler;
import coyote.dataframe.marshal.XMLMarshaler;
import coyote.dx.http.HttpFuture;
import coyote.dx.http.responder.AbstractBatchResponder;
import coyote.loader.log.Log;


/**
 * 
 */
public class HttpReaderHandler extends AbstractBatchResponder implements Responder {

  private static final String STATUS = "Status";
  private static final Object OK = "OK";
  private static final String MESSAGE = "Message";
  private static final Object ERROR = "Error";




  /**
   * @see coyote.dx.http.responder.AbstractBatchResponder#delete(coyote.commons.network.http.responder.Resource, java.util.Map, coyote.commons.network.http.IHTTPSession)
   */
  @Override
  public Response delete(Resource resource, Map<String, String> urlParams, IHTTPSession session) {
    @SuppressWarnings("unchecked")
    ConcurrentLinkedQueue<HttpFuture> queue = resource.initParameter(0, ConcurrentLinkedQueue.class);
    int timeout = resource.initParameter(1, Integer.class);
    return handleRequest("DELETE", session, queue, timeout);
  }




  /**
   * @see coyote.dx.http.responder.AbstractBatchResponder#get(coyote.commons.network.http.responder.Resource, java.util.Map, coyote.commons.network.http.IHTTPSession)
   */
  @Override
  public Response get(Resource resource, Map<String, String> urlParams, IHTTPSession session) {
    @SuppressWarnings("unchecked")
    ConcurrentLinkedQueue<HttpFuture> queue = resource.initParameter(0, ConcurrentLinkedQueue.class);
    int timeout = resource.initParameter(1, Integer.class);
    return handleRequest("GET", session, queue, timeout);
  }




  /**
   * @see coyote.dx.http.responder.AbstractBatchResponder#other(java.lang.String, coyote.commons.network.http.responder.Resource, java.util.Map, coyote.commons.network.http.IHTTPSession)
   */
  @Override
  public Response other(String method, Resource resource, Map<String, String> urlParams, IHTTPSession session) {
    @SuppressWarnings("unchecked")
    ConcurrentLinkedQueue<HttpFuture> queue = resource.initParameter(0, ConcurrentLinkedQueue.class);
    int timeout = resource.initParameter(1, Integer.class);
    return handleRequest(method, session, queue, timeout);
  }




  /**
   * @see coyote.dx.http.responder.AbstractBatchResponder#post(coyote.commons.network.http.responder.Resource, java.util.Map, coyote.commons.network.http.IHTTPSession)
   */
  @Override
  public Response post(Resource resource, Map<String, String> urlParams, IHTTPSession session) {
    @SuppressWarnings("unchecked")
    ConcurrentLinkedQueue<HttpFuture> queue = resource.initParameter(0, ConcurrentLinkedQueue.class);
    int timeout = resource.initParameter(1, Integer.class);
    return handleRequest("POST", session, queue, timeout);
  }




  /**
   * @see coyote.dx.http.responder.AbstractBatchResponder#put(coyote.commons.network.http.responder.Resource, java.util.Map, coyote.commons.network.http.IHTTPSession)
   */
  @Override
  public Response put(Resource resource, Map<String, String> urlParams, IHTTPSession session) {
    @SuppressWarnings("unchecked")
    ConcurrentLinkedQueue<HttpFuture> queue = resource.initParameter(0, ConcurrentLinkedQueue.class);
    int timeout = resource.initParameter(1, Integer.class);
    return handleRequest("PUT", session, queue, timeout);
  }




  /**
   * This creates a future object and places it in the queue for the reader to 
   * process.
   * 
   * <p>This method will then block for for a timeout period waiting for the 
   * results. 
   *
   * @param method HTTP method (GET, POST, PUT, etc.) called
   * @param session the session representing the HTTP request
   * @param timeout how long to wait for the completion of the future
   *  
   * @return the HTTP response with the results of processing.
   */
  private Response handleRequest(String method, IHTTPSession session, ConcurrentLinkedQueue<HttpFuture> queue, int timeout) {
    int millis = timeout;
    if (millis < 1) {
      millis = HttpReader.DEFAULT_TIMEOUT;
    }

    Response retval = null;
    Log.debug("Handling '" + method + "' request (timeout=" + timeout + "ms)");
    if (Method.GET.toString().equalsIgnoreCase(method)) {
      setResults(new DataFrame().set(STATUS, OK).set(MESSAGE, "GET requests do not send data, use PUT, POST or some other HTTP method type."));
      retval = Response.createFixedLengthResponse(Status.OK, getMimeType(), getText());
    } else {
      HttpFuture future = new HttpFuture();
      future.setMethod(method);
      // parse the body of the message into a dataframe
      try {
        Body body = session.parseBody();
        Log.debug("Body contains " + body.size() + " entities");

        if (body.size() > 0) {
          for (final String key : body.keySet()) {
            Log.debug("Parsing body entity '" + key + "'");
            Object obj = body.get(key);
            Log.debug("body entity '" + key + "' is a " + obj.getClass().getName());
            if (obj instanceof String) {
              List<DataFrame> frames = null;
              if (session.getRequestHeaders().containsKey(MimeType.XML)) {
                frames = XMLMarshaler.marshal((String)obj);
              } else {
                frames = JSONMarshaler.marshal((String)obj);
              }
              if (frames != null) {
                DataFrame frame = frames.get(0);
                if (frame != null) {
                  Log.debug("Placing data frame in future for processing");
                  future.setFrame(frame);
                } else {
                  Log.notice("No data frame found in HTTP request");
                }
              } else {
                Log.warn("No dataframe list to process");
              }
            }
          }
        } else if (session.getParms() != null && session.getParms().size() > 0) {
          //if the body contains no entities, maybe data was submitted as form-data - each vey-value pair is a field in the frame
          Log.notice("Form parameters are not yest supported...open a ticket");
        } else {
          setResults(new DataFrame().set(STATUS, ERROR).set(MESSAGE, "There was no body nor form data found to process."));
          retval = Response.createFixedLengthResponse(Status.BAD_REQUEST, getMimeType(), getText());
        }
      } catch (final Exception e) {
        e.printStackTrace();
        setResults(new DataFrame().set(STATUS, ERROR).set(MESSAGE, "Problems parsing body: " + e.getMessage()));
        retval = Response.createFixedLengthResponse(Status.BAD_REQUEST, getMimeType(), getText());
      }

      if (future.getFrame() != null) {
        // add the future to the queue for the reader to send through the engine
        queue.add(future);

        // wait for the future to be processed by the reader/engine for a 
        // specific number of milliseconds
        retval = future.getResponse(millis);

        if (retval == null) {
          setResults(new DataFrame().set(STATUS, ERROR).set(MESSAGE, "Transform did not return a result within the time-out period"));
          retval = Response.createFixedLengthResponse(Status.UNAVAILABLE, getMimeType(), getText());
        }
      } else {
        Log.debug("no data to process");
      }
    }
    return retval;
  }

}