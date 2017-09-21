/*
 * Copyright (c) 2017 Stephan D. Cote' - All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the MIT License which accompanies this distribution, and is
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.dx.reader;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

import coyote.commons.StringUtil;
import coyote.commons.network.MimeType;
import coyote.commons.network.http.Body;
import coyote.commons.network.http.HTTP;
import coyote.commons.network.http.IHTTPSession;
import coyote.commons.network.http.Response;
import coyote.commons.network.http.ResponseException;
import coyote.commons.network.http.Status;
import coyote.commons.network.http.responder.Resource;
import coyote.commons.network.http.responder.Responder;
import coyote.dataframe.DataFrame;
import coyote.dataframe.marshal.JSONMarshaler;
import coyote.dataframe.marshal.MarshalException;
import coyote.dataframe.marshal.XMLMarshaler;
import coyote.dx.http.HttpFuture;
import coyote.dx.http.responder.AbstractBatchResponder;
import coyote.loader.log.Log;


/**
 * This is the class the HTTP server calls when a request is received for a 
 * mapped URL route.
 * 
 * <p>The purpose of this class is to convert a HTTP request into an 
 * HttpFuture object to be placed in the HttpReader's queue. Each time the 
 * reader "reads", it pulls a future off the queue that this class created and
 * processes it. When the transaction ends, the HttpReader generates a 
 * response and placeds it in the HttpFuture and marks it as complete.
 * 
 * <p>This handler waits for the reader to complete the HttpFuture when the 
 * transaction is complete. This class will then retrieve a response from the 
 * HttpFuture then send it back to the client. 
 */
public class HttpReaderHandler extends AbstractBatchResponder implements Responder {

  private static final String STATUS = "Status";
  private static final String MESSAGE = "Message";
  private static final Object ERROR = "Error";




  /**
   * @see coyote.dx.http.responder.AbstractBatchResponder#delete(coyote.commons.network.http.responder.Resource, java.util.Map, coyote.commons.network.http.IHTTPSession)
   */
  @Override
  public Response delete(final Resource resource, final Map<String, String> urlParams, final IHTTPSession session) {
    @SuppressWarnings("unchecked")
    final ConcurrentLinkedQueue<HttpFuture> queue = resource.initParameter(0, ConcurrentLinkedQueue.class);
    final int timeout = resource.initParameter(1, Integer.class);
    return handleRequest("DELETE", session, urlParams, queue, timeout);
  }




  /**
   * @see coyote.dx.http.responder.AbstractBatchResponder#get(coyote.commons.network.http.responder.Resource, java.util.Map, coyote.commons.network.http.IHTTPSession)
   */
  @Override
  public Response get(final Resource resource, final Map<String, String> urlParams, final IHTTPSession session) {
    @SuppressWarnings("unchecked")
    final ConcurrentLinkedQueue<HttpFuture> queue = resource.initParameter(0, ConcurrentLinkedQueue.class);
    final int timeout = resource.initParameter(1, Integer.class);
    return handleRequest("GET", session, urlParams, queue, timeout);
  }




  private String getAcceptType(final IHTTPSession session) {
    return getPreferredHeaderValue(session, HTTP.HDR_ACCEPT);
  }




  private String getContentType(final IHTTPSession session) {
    return getPreferredHeaderValue(session, HTTP.HDR_CONTENT_TYPE);
  }




  private String getPreferredHeaderValue(final IHTTPSession session, final String headerName) {
    String retval = null;
    if (session != null && StringUtil.isNotBlank(headerName)) {
      final String value = session.getRequestHeaders().get(headerName.toLowerCase());
      if (StringUtil.isNotEmpty(value)) {
        final String[] tokens = value.split(",");
        retval = tokens[0];
      }
    }
    return retval;
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
  @SuppressWarnings("resource")
  private Response handleRequest(final String method, final IHTTPSession session, final Map<String, String> urlParams, final ConcurrentLinkedQueue<HttpFuture> queue, final int timeout) {
    int millis = timeout;
    if (millis < 1) {
      millis = HttpReader.DEFAULT_TIMEOUT;
    }

    Response retval = null;
    final HttpFuture future = new HttpFuture();
    future.setMethod(method);
    future.setAcceptType(getAcceptType(session));
    future.setContentType(getContentType(session));

    DataFrame dframe = null;

    Body body = null;
    try {
      body = session.parseBody();
    } catch (IOException | ResponseException e1) {
      setResults(new DataFrame().set(STATUS, ERROR).set(MESSAGE, "Problems parsing request body: " + e1.getMessage()));
      retval = Response.createFixedLengthResponse(Status.BAD_REQUEST, getMimeType(), getText());
    }

    if (Log.isLogging(Log.DEBUG_EVENTS)) {
      Log.debug("Handling '" + method + "' request (timeout=" + timeout + "ms)");
      Log.debug("Body contains " + (body != null ? body.size() : 0) + " entities");
      Log.debug("Request Parameter count: " + (session.getParms() != null ? session.getParms().size() : 0) + " pairs");
      Log.debug("URL Parameter count: " + (urlParams != null ? urlParams.size() : 0) + " pairs");
    }

    // Start with the body
    if (body != null && body.size() > 0) {
      try {
        dframe = parseBody(body, session);
      } catch (final Exception e) {
        setResults(new DataFrame().set(STATUS, ERROR).set(MESSAGE, "Problems parsing body: " + e.getMessage()));
        retval = Response.createFixedLengthResponse(Status.BAD_REQUEST, getMimeType(), getText());
      }
    } else {
      dframe = new DataFrame();
    }

    // next, use request parameters overriding what may be in the body
    if (session.getParms() != null && session.getParms().size() > 0) {
      for (Map.Entry<String, String> entry : session.getParms().entrySet()) {
        dframe.put(entry.getKey(), entry.getValue());
      }
    }

    // finally, URL parameters override the body and the request params
    if (urlParams != null && urlParams.size() > 0) {
      for (Map.Entry<String, String> entry : urlParams.entrySet()) {
        dframe.put(entry.getKey(), entry.getValue());
      }
    }

    // set the results in the future
    future.setFrame(dframe);

    // if there is data to process add it to the queue, otherwise report a bad request
    if (future.getFrame().getFieldCount() > 0) {
      queue.add(future);
      retval = future.getResponse(millis);
      if (retval == null) {
        setResults(new DataFrame().set(STATUS, ERROR).set(MESSAGE, "Transform did not return a result within the time-out period"));
        retval = Response.createFixedLengthResponse(Status.UNAVAILABLE, getMimeType(), getText());
      }
    } else {
      setResults(new DataFrame().set(STATUS, ERROR).set(MESSAGE, "No data to process"));
      retval = Response.createFixedLengthResponse(Status.BAD_REQUEST, getMimeType(), getText());
    }

    return retval;
  }




  /**
   * Retrieve the first data frame from the body of the request.
   * 
   * @param body the request body
   * @param session the session request
   * 
   * @return the first dataframe parsed from the body
   * 
   * @throws MarshalException if the JSON or XML data could not be parsed
   */
  private DataFrame parseBody(Body body, IHTTPSession session) throws MarshalException {
    DataFrame retval = null;
    for (final String key : body.keySet()) {
      final Object obj = body.get(key);

      String data = null;
      if (obj instanceof String) {
        data = (String)obj;
      } else if (obj instanceof ByteBuffer) {
        ByteBuffer buffer = (ByteBuffer)obj;
        data = new String(buffer.array());
      } else {
        Log.error("I don't know how to parse a " + obj.getClass().getName() + " body object.");
        throw new MarshalException("Problems parsing request body. Check logs for details.");
      }

      if (data != null) {
        List<DataFrame> frames = null;
        if (session.getRequestHeaders().containsKey(MimeType.XML)) {
          frames = XMLMarshaler.marshal(data);
        } else {
          frames = JSONMarshaler.marshal(data);
        }
        if (frames != null) {
          retval = frames.get(0);
          break; // only get the first dataframe
        } else {
          Log.warn("No dataframe list to process");
        }
      }
    }
    return retval;
  }




  /**
   * @see coyote.dx.http.responder.AbstractBatchResponder#other(java.lang.String, coyote.commons.network.http.responder.Resource, java.util.Map, coyote.commons.network.http.IHTTPSession)
   */
  @Override
  public Response other(final String method, final Resource resource, final Map<String, String> urlParams, final IHTTPSession session) {
    @SuppressWarnings("unchecked")
    final ConcurrentLinkedQueue<HttpFuture> queue = resource.initParameter(0, ConcurrentLinkedQueue.class);
    final int timeout = resource.initParameter(1, Integer.class);
    return handleRequest(method, session, urlParams, queue, timeout);
  }




  /**
   * @see coyote.dx.http.responder.AbstractBatchResponder#post(coyote.commons.network.http.responder.Resource, java.util.Map, coyote.commons.network.http.IHTTPSession)
   */
  @Override
  public Response post(final Resource resource, final Map<String, String> urlParams, final IHTTPSession session) {
    @SuppressWarnings("unchecked")
    final ConcurrentLinkedQueue<HttpFuture> queue = resource.initParameter(0, ConcurrentLinkedQueue.class);
    final int timeout = resource.initParameter(1, Integer.class);
    return handleRequest("POST", session, urlParams, queue, timeout);
  }




  /**
   * @see coyote.dx.http.responder.AbstractBatchResponder#put(coyote.commons.network.http.responder.Resource, java.util.Map, coyote.commons.network.http.IHTTPSession)
   */
  @Override
  public Response put(final Resource resource, final Map<String, String> urlParams, final IHTTPSession session) {
    @SuppressWarnings("unchecked")
    final ConcurrentLinkedQueue<HttpFuture> queue = resource.initParameter(0, ConcurrentLinkedQueue.class);
    final int timeout = resource.initParameter(1, Integer.class);
    return handleRequest("PUT", session, urlParams, queue, timeout);
  }

}