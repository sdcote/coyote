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
   * @return the method
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

}