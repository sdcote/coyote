/*
 * Copyright (c) 2017 Stephan D. Cote' - All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the MIT License which accompanies this distribution, and is
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.dx.reader;

import coyote.commons.StringUtil;
import coyote.commons.network.MimeType;
import coyote.commons.network.http.*;
import coyote.commons.network.http.responder.Resource;
import coyote.commons.network.http.responder.Responder;
import coyote.dataframe.DataFrame;
import coyote.dataframe.marshal.JSONMarshaler;
import coyote.dataframe.marshal.MarshalException;
import coyote.dataframe.marshal.XMLMarshaler;
import coyote.dx.http.HttpFuture;
import coyote.dx.http.responder.AbstractCoyoteResponder;
import coyote.loader.log.Log;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;


/**
 * This is the class the HTTP server calls when a request is received for a 
 * mapped URL route.
 * 
 * <p>The purpose of this class is to convert a HTTP request into an 
 * HttpFuture object to be placed in the HttpReader's queue. Each time the 
 * Reader "reads", it pulls a future off the queue that this class created and
 * processes it. When the transaction ends, the HttpReader generates a 
 * response and places it in the HttpFuture and marks the future as complete.
 * 
 * <p>This class will be used by the HTTP Server Request thread, marshaling 
 * the request into a data frame and placing it in a future object. This then 
 * blocks on that future while the single threaded engine processes the 
 * futures in its queue. The Reader in the engine generates Responses based on 
 * the results of the engine processing the data frame inside the future. The 
 * result is many threads blocking while the engine thread processes each data 
 * frame in the order it was received. 
 */
public class HttpReaderHandler extends AbstractCoyoteResponder implements Responder {
  private static final int TWO_MINUTES = 120000;




  /**
   * @see coyote.dx.http.responder.AbstractCoyoteResponder#delete(coyote.commons.network.http.responder.Resource, java.util.Map, coyote.commons.network.http.HTTPSession)
   */
  @Override
  public Response delete(final Resource resource, final Map<String, String> urlParams, final HTTPSession session) {
    @SuppressWarnings("unchecked")
    final ConcurrentLinkedQueue<HttpFuture> queue = resource.initParameter(0, ConcurrentLinkedQueue.class);
    final int timeout = resource.initParameter(1, Integer.class);
    return handleRequest(HTTP.METHOD_DELETE, determineEndpoint(resource.getUri()), session, urlParams, queue, timeout);
  }




  /**
   * @see coyote.dx.http.responder.AbstractCoyoteResponder#get(coyote.commons.network.http.responder.Resource, java.util.Map, coyote.commons.network.http.HTTPSession)
   */
  @Override
  public Response get(final Resource resource, final Map<String, String> urlParams, final HTTPSession session) {
    @SuppressWarnings("unchecked")
    final ConcurrentLinkedQueue<HttpFuture> queue = resource.initParameter(0, ConcurrentLinkedQueue.class);
    final int timeout = resource.initParameter(1, Integer.class);
    return handleRequest(HTTP.METHOD_GET, determineEndpoint(resource.getUri()), session, urlParams, queue, timeout);
  }




  /**
   * @see coyote.dx.http.responder.AbstractCoyoteResponder#other(java.lang.String, coyote.commons.network.http.responder.Resource, java.util.Map, coyote.commons.network.http.HTTPSession)
   */
  @Override
  public Response other(final String method, final Resource resource, final Map<String, String> urlParams, final HTTPSession session) {
    @SuppressWarnings("unchecked")
    final ConcurrentLinkedQueue<HttpFuture> queue = resource.initParameter(0, ConcurrentLinkedQueue.class);
    final int timeout = resource.initParameter(1, Integer.class);
    return handleRequest(method.toUpperCase(), determineEndpoint(resource.getUri()), session, urlParams, queue, timeout);
  }




  /**
   * @see coyote.dx.http.responder.AbstractCoyoteResponder#post(coyote.commons.network.http.responder.Resource, java.util.Map, coyote.commons.network.http.HTTPSession)
   */
  @Override
  public Response post(final Resource resource, final Map<String, String> urlParams, final HTTPSession session) {
    @SuppressWarnings("unchecked")
    final ConcurrentLinkedQueue<HttpFuture> queue = resource.initParameter(0, ConcurrentLinkedQueue.class);
    final int timeout = resource.initParameter(1, Integer.class);
    return handleRequest(HTTP.METHOD_POST, determineEndpoint(resource.getUri()), session, urlParams, queue, timeout);
  }




  /**
   * @see coyote.dx.http.responder.AbstractCoyoteResponder#put(coyote.commons.network.http.responder.Resource, java.util.Map, coyote.commons.network.http.HTTPSession)
   */
  @Override
  public Response put(final Resource resource, final Map<String, String> urlParams, final HTTPSession session) {
    @SuppressWarnings("unchecked")
    final ConcurrentLinkedQueue<HttpFuture> queue = resource.initParameter(0, ConcurrentLinkedQueue.class);
    final int timeout = resource.initParameter(1, Integer.class);
    return handleRequest(HTTP.METHOD_PUT, determineEndpoint(resource.getUri()), session, urlParams, queue, timeout);
  }




  private String getAcceptType(final HTTPSession session) {
    return getPreferredHeaderValue(session, HTTP.HDR_ACCEPT);
  }




  private String getContentType(final HTTPSession session) {
    return getPreferredHeaderValue(session, HTTP.HDR_CONTENT_TYPE);
  }




  private String getPreferredHeaderValue(final HTTPSession session, final String headerName) {
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
   * process the data in that future.
   *
   * <p>This method will then block for for a timeout period waiting for the
   * results.
   *
   * @param method HTTP method (GET, POST, PUT, etc.) called
   * @param resource the name of the resource requested
   * @param session the session representing the HTTP request
   * @param timeout how long to wait for the completion of the future
   *
   * @return the HTTP response with the results of processing.
   */
  private Response handleRequest(final String method, final String resource, final HTTPSession session, final Map<String, String> urlParams, final ConcurrentLinkedQueue<HttpFuture> queue, final int timeout) {
    int millis = timeout;

    // prevent infinite and excessive blocking
    if (millis < 1 || millis > TWO_MINUTES) {
      millis = HttpReader.DEFAULT_TIMEOUT;
    }

    Response retval = null;
    final HttpFuture future = new HttpFuture();
    future.setMethod(method);
    future.setAcceptType(getAcceptType(session));
    future.setContentType(getContentType(session));
    future.setRequestUri(session.getUri());
    future.setResource(resource);

    // set our mimetype based on the future object
    setMimetype(future.determineResponseType());

    DataFrame dframe = null;

    // Start with the body
    try {
      dframe = populateBody(session);
      if (dframe == null) {
        setResults(new DataFrame().set(HttpReader.STATUS, HttpReader.ERROR).set(HttpReader.MESSAGE, "Could not parse request body into valid data frame"));
        retval = Response.createFixedLengthResponse(Status.BAD_REQUEST, getMimeType(), getText());
      }
    } catch (IllegalArgumentException e) {
      setResults(new DataFrame().set(HttpReader.STATUS, HttpReader.ERROR).set(HttpReader.MESSAGE, e.getMessage()));
      retval = Response.createFixedLengthResponse(Status.BAD_REQUEST, getMimeType(), getText());
    }

    // no retval (response) means no error
    if (retval == null) {
      // next, use request parameters overriding what may be in the body
      dframe = populateRequestParameters(dframe, session);

      // finally, URL parameters override the body and the request params
      dframe = populateUrlParameters(dframe, urlParams);

      future.setFrame(dframe);

      if (future.getFrame().getFieldCount() > 0) {
        queue.add(future);

        // wait for a response, but only for the timeout period
        retval = future.getResponse(millis);

        if (retval == null) {
          if( future.isTimedOut()){
            setResults(new DataFrame().set(HttpReader.STATUS, HttpReader.ERROR).set(HttpReader.MESSAGE, "Transform did not return a result within the time-out period"));
            retval = Response.createFixedLengthResponse(Status.UNAVAILABLE, getMimeType(), getText());
          } else {
            setResults(new DataFrame().set(HttpReader.STATUS, HttpReader.PROCESSED));
            retval = Response.createFixedLengthResponse(Status.NO_CONTENT, getMimeType(), getText());
          }
        }
      } else {
        setResults(new DataFrame().set(HttpReader.STATUS, HttpReader.ERROR).set(HttpReader.MESSAGE, "No data to process"));
        retval = Response.createFixedLengthResponse(Status.BAD_REQUEST, getMimeType(), getText());
      }
    }
    return retval;
  }




  private static String determineEndpoint(String entryUri) {
    String retval = entryUri;

    // if the entry URI contains a ':', drop everything after the first occurrence ,  including the ':' and the previous '/'
    if (retval.indexOf(':') > 0) {
      retval = retval.substring(0, retval.indexOf(':') - 1);
    }

    // retrieve everything from the last / on
    if (retval.lastIndexOf('/') > 0) {
      retval = retval.substring(retval.lastIndexOf('/') + 1);
    }
    return retval;
  }




  /**
   * Create a dataframe out of the body of the request in the given session.
   * 
   * @param session the session containing the request.
   * 
   * @return a data frame populated with the data in the body or an empty data 
   *         frame if no body was in the session.
   *
   * @throws IllegalArgumentException if there were problems parsing the body
   */
  private DataFrame populateBody(HTTPSession session) throws IllegalArgumentException {
    DataFrame retval = null;
    Body body = null;
    try {
      body = session.parseBody();
      if (Log.isLogging(Log.DEBUG)) Log.debug("Received body of " + body.size() + " key-value mappings");
    } catch (IOException | ResponseException e1) {
      throw new IllegalArgumentException("Problems parsing request body: " + e1.getMessage());
    }

    if (body != null && body.size() > 0) {
      try {
        retval = parseBody(body, session);
        if (Log.isLogging(Log.DEBUG)) Log.debug("Parsed into a frame:\r\n" + JSONMarshaler.toFormattedString(retval));
      } catch (final Exception e) {
        throw new IllegalArgumentException("Problems parsing body data: " + e.getMessage());
      }
    }

    if (retval == null) {
      retval = new DataFrame();
    }

    return retval;
  }




  /**
   * Add/overwrite URL parameters to the data frame.
   * 
   * <p>If any of the fields match existing parameters, the values in the data 
   * frame will be over-written.
   * 
   * @param frame the original data frame
   * @param urlParams the map of parameters
   * 
   * @return a data frame with the parameters added
   */
  private DataFrame populateUrlParameters(DataFrame frame, Map<String, String> urlParams) {
    DataFrame retval = frame;
    if (urlParams != null && urlParams.size() > 0) {
      for (Map.Entry<String, String> entry : urlParams.entrySet()) {
        retval.put(entry.getKey(), entry.getValue());
      }
    }
    return retval;
  }




  /**
   * Add/overwrite request parameters to the data frame.
   * 
   * <p>If any of the fields match existing parameters, the values in the data 
   * frame will be over-written.
   * 
   * @param frame the original data frame
   * @param session the session containing the request parameters
   * 
   * @return a data frame with the parameters added
   */
  private DataFrame populateRequestParameters(DataFrame frame, HTTPSession session) {
    DataFrame retval = frame;
    if (session.getParms() != null && session.getParms().size() > 0) {
      for (Map.Entry<String, String> entry : session.getParms().entrySet()) {
        retval.put(entry.getKey(), entry.getValue());
      }
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
  private DataFrame parseBody(Body body, HTTPSession session) throws MarshalException {
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

      if (data != null && StringUtil.isNotEmpty(data)) {
        if (Log.isLogging(Log.DEBUG)) Log.debug("Parsing " + data.length() + " characters into a frame:\r\n" + data);

        List<DataFrame> frames = null;
        String contentType = session.getRequestHeaders().get(HTTP.HDR_CONTENT_TYPE.toLowerCase());
        if (StringUtil.isNotEmpty(contentType) && contentType.contains(MimeType.XML.getType())) {
          frames = XMLMarshaler.marshal(data);
          if (frames == null || frames.size() == 0) {
            throw new MarshalException("No valid XML data found");
          }
        } else {
          frames = JSONMarshaler.marshal(data);
        }
        if (frames != null && frames.size() > 0) {
          retval = frames.get(0);
          break; // only get the first dataframe
        } else {
          Log.warn("No dataframe list to process");
        }
      }
    }
    return retval;
  }

}