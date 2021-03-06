/*
 * Copyright (c) 2017 Stephan D. Cote' - All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the MIT License which accompanies this distribution, and is
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.dx.http;

import coyote.commons.StringUtil;
import coyote.commons.network.MimeType;
import coyote.commons.network.http.Response;
import coyote.dataframe.DataFrame;


/**
 * Future class which allow requesting threads to wait for completion of
 * processing in other threads before sending a response.
 * 
 * <p>This holds several items other components may find useful in 
 * transforming and writing the contained data frame.
 * 
 * <p>It is expected that a Response object will be set to mark this future 
 * completed.
 */
public class HttpFuture {
  private final Object mutex = new Object();
  private volatile Response response = null;
  private DataFrame frame = null;
  private String method = null;
  private String acceptType = null;
  private String contentType = null;
  private String requestUri = null;
  private String resource = null;
  private DataFrame errorFrame = null;
  private volatile boolean processedFlag = false;
  private volatile boolean timedOut = false;




  /**
   * @return the MIME type in the Accept-Type request header
   */
  public String getAcceptType() {
    return acceptType;
  }




  /**
   * @return the MIME type in the Content-Type request header
   */
  public String getContentType() {
    return contentType;
  }




  public DataFrame getDataFrame() {
    return frame;
  }




  /**
   * @return the frame
   */
  public DataFrame getFrame() {
    return frame;
  }




  /**
   * @return the HTTP request method or null if it was not set.
   */
  public String getMethod() {
    return method;
  }




  public Response getResponse(final long timeout) {
    synchronized (mutex) {
      final long expiry = System.currentTimeMillis() + timeout;
      while (response == null && System.currentTimeMillis() < expiry) {
        try {
          mutex.wait(10);
        } catch (final InterruptedException ignore) {
          // don't care, simply time-out
        }
      }
      if(response == null) timedOut= true;
      return response;
    }

  }




  public boolean isComplete() {
    synchronized (mutex) {
      return (response != null);
    }
  }




  /**
   * Set the accept type for this request.
   *
   * @param type the MIME type in the Accept-Type request header
   */
  public void setAcceptType(final String type) {
    acceptType = type;
  }




  /**
   * Set the content type for this request.
   *
   * @param type the MIME type in the Content-Type request header
   */
  public void setContentType(final String type) {
    contentType = type;
  }




  /**
   * @param frame the frame to set
   */
  public void setFrame(final DataFrame frame) {
    this.frame = frame;
  }




  public void setMethod(final String method) {
    this.method = method;
  }




  public void setResponse(final Response result) {
    synchronized (mutex) {
      response = result;
      mutex.notifyAll();
    }
  }




  /**
   * Determine the MIME type of the response based on the contents of the the 
   * Accept-Type and Content-Type data in this future.
   * 
   * @return the approperiate MIME type for the response.
   */
  public MimeType determineResponseType() {
    return determineResponseType(this);
  }




  /**
   * Determine the MIME type of the response based on the contents of the the 
   * HttpFuture.
   * 
   * @param future the future containing the Accept-Type and Content-Type
   * 
   * @return the approperiate MIME type for the response.
   */
  private static MimeType determineResponseType(HttpFuture future) {
    MimeType retval;
    String acceptType = future.getAcceptType();
    String contentType = future.getContentType();
    if (StringUtil.isBlank(acceptType) || acceptType.contains(MimeType.ANY.getType())) {
      if (StringUtil.isNotBlank(contentType) && contentType.contains(MimeType.XML.getType())) {
        retval = MimeType.XML;
      } else {
        retval = MimeType.JSON;
      }
    } else if (acceptType.contains(MimeType.XML.getType())) {
      retval = MimeType.XML;
    } else {
      retval = MimeType.JSON;
    }
    return retval;
  }




  /**
   * @return the request URI
   */
  public String getRequestUri() {
    return requestUri;
  }




  /**
   * @param uri the URI to set
   */
  public void setRequestUri(String uri) {
    this.requestUri = uri;
  }




  /**
   * @param resource the name of the resource requested
   */
  public void setResource(String resource) {
    this.resource = resource;
  }




  /**
   * @return the resource
   */
  public String getResource() {
    return resource;
  }




  /**
   * @return the data frame containing the error information to be sent to the 
   *         requester
   */
  public DataFrame getErrorFrame() {
    return errorFrame;
  }




  /**
   * @param frame the error frame to set
   */
  public void setErrorFrame(DataFrame frame) {
    errorFrame = frame;
  }




  /**
   * Indicates the processing resulted in an error.
   * 
   * @return true if there is an error frame, false if there is no frame.
   */
  public boolean isInError() {
    return errorFrame != null;
  }




  /**
   * This is a flag indicating this future and the data it contains has been 
   * processed.
   * 
   * <p>Processing simply means a component examined the future and performed 
   * processing as configured. Even if processing ended in error, this flag 
   * should still indicate processing did occur.
   * 
   * <p>A return value of false indicate no component processed this future 
   * and that the request is not supported by this configuration. In such 
   * cases a 501 (Not Implemented) may be returned indicating this system 
   * instance does not support the request.
   * 
   * @return true of the future was processed by a component, false otherwise.
   */
  public boolean isProcessed() {
    return processedFlag;
  }




  /**
   * @param processed true indicates a component examined this request and 
   *        processed it, false indicates this future has not been processed.
   */
  public void setProcessed(boolean processed) {
    this.processedFlag = processed;
  }




  /**
   * This is a flag indicating the {@link #getResponse(long)} method was called
   * and no response was returned within the given time out period.
   *
   * @return true if {@link #getResponse(long)} returned nothing after the
   *         expiration period, fales if {@link #getResponse(long)} was never
   *         called, or called and returned a response within the wait time.
   */
  public boolean isTimedOut() {
    return timedOut;
  }

}