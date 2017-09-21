/*
 * Copyright (c) 2017 Stephan D. Cote' - All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the MIT License which accompanies this distribution, and is
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.dx.reader;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

import coyote.commons.StringUtil;
import coyote.commons.network.MimeType;
import coyote.commons.network.http.Body;
import coyote.commons.network.http.HTTP;
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
  private static final Object OK = "OK";
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
    return handleRequest("DELETE", session, queue, timeout);
  }




  /**
   * @see coyote.dx.http.responder.AbstractBatchResponder#get(coyote.commons.network.http.responder.Resource, java.util.Map, coyote.commons.network.http.IHTTPSession)
   */
  @Override
  public Response get(final Resource resource, final Map<String, String> urlParams, final IHTTPSession session) {
    @SuppressWarnings("unchecked")
    final ConcurrentLinkedQueue<HttpFuture> queue = resource.initParameter(0, ConcurrentLinkedQueue.class);
    final int timeout = resource.initParameter(1, Integer.class);
    return handleRequest("GET", session, queue, timeout);
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
  private Response handleRequest(final String method, final IHTTPSession session, final ConcurrentLinkedQueue<HttpFuture> queue, final int timeout) {
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
      final HttpFuture future = new HttpFuture();
      future.setMethod(method);
      future.setAcceptType(getAcceptType(session));
      future.setContentType(getContentType(session));

      // parse the body of the message into a dataframe
      try {
        final Body body = session.parseBody();
        Log.debug("Body contains " + body.size() + " entities");

        if (body.size() > 0) {
          for (final String key : body.keySet()) {
            Log.debug("Parsing body entity '" + key + "'");
            final Object obj = body.get(key);
            Log.debug("body entity '" + key + "' is a " + obj.getClass().getName());

            String data = null;
            if (obj instanceof String) {
              data = (String)obj;
            } else if (obj instanceof ByteBuffer) {
              ByteBuffer buffer = (ByteBuffer)obj;
              data = new String(buffer.array());
            } else {
              Log.error("I don't know how to parse a " + obj.getClass().getName() + " body object.");
            }

            if (data != null) {
              List<DataFrame> frames = null;
              if (session.getRequestHeaders().containsKey(MimeType.XML)) {
                frames = XMLMarshaler.marshal(data);
              } else {
                frames = JSONMarshaler.marshal(data);
              }
              if (frames != null) {
                final DataFrame frame = frames.get(0);
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
          Log.notice("Form parameters are not yet supported...open a ticket");
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




  /**
   * @see coyote.dx.http.responder.AbstractBatchResponder#other(java.lang.String, coyote.commons.network.http.responder.Resource, java.util.Map, coyote.commons.network.http.IHTTPSession)
   */
  @Override
  public Response other(final String method, final Resource resource, final Map<String, String> urlParams, final IHTTPSession session) {
    @SuppressWarnings("unchecked")
    final ConcurrentLinkedQueue<HttpFuture> queue = resource.initParameter(0, ConcurrentLinkedQueue.class);
    final int timeout = resource.initParameter(1, Integer.class);
    return handleRequest(method, session, queue, timeout);
  }




  /**
   * @see coyote.dx.http.responder.AbstractBatchResponder#post(coyote.commons.network.http.responder.Resource, java.util.Map, coyote.commons.network.http.IHTTPSession)
   */
  @Override
  public Response post(final Resource resource, final Map<String, String> urlParams, final IHTTPSession session) {
    @SuppressWarnings("unchecked")
    final ConcurrentLinkedQueue<HttpFuture> queue = resource.initParameter(0, ConcurrentLinkedQueue.class);
    final int timeout = resource.initParameter(1, Integer.class);
    return handleRequest("POST", session, queue, timeout);
  }




  /**
   * @see coyote.dx.http.responder.AbstractBatchResponder#put(coyote.commons.network.http.responder.Resource, java.util.Map, coyote.commons.network.http.IHTTPSession)
   */
  @Override
  public Response put(final Resource resource, final Map<String, String> urlParams, final IHTTPSession session) {
    @SuppressWarnings("unchecked")
    final ConcurrentLinkedQueue<HttpFuture> queue = resource.initParameter(0, ConcurrentLinkedQueue.class);
    final int timeout = resource.initParameter(1, Integer.class);
    return handleRequest("PUT", session, queue, timeout);
  }

}