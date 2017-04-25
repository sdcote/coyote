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
package coyote.batch.web;

import javax.net.ssl.SSLEngineResult.Status;

import coyote.commons.network.MimeType;
import coyote.commons.network.http.IHTTPSession;


/**
 * Future object for web server requests.
 * 
 * <p>When a request thread receives a request, it generates an instance of 
 * this class in a queue. The thread then begins blocking on this instance, 
 * waiting for a response.
 * 
 * <p>The WebServerReader reads this future instance from the queue, generates 
 * a working frame, places this instance in the transaction context and 
 * returns from the read method. The transaction is processed in the pipeline
 * the same as other transactions.
 * 
 * <p>Later in the pipeline, a WebServerWriter uses the data in the 
 * transaction context to create a response and places it in the future object 
 * in the transaction context o which the request thread is presumably still 
 * waiting. The writer then returns from its read method.
 * 
 * <p>Once the writer updates the future object with a response, the request 
 * thread then generates the appropriate HTTP response and the thread closes 
 * the connection and terminates. 
 */
public class ResponseFuture {

  private IHTTPSession session;
  private volatile boolean complete = false;
  private Status responseStatus = Status.OK;
  private MimeType responseType = MimeType.JSON;
  private String responseText = "";




  public ResponseFuture( IHTTPSession session ) {
    this.session = session;
  }




  /**
   * @return the HTTP request session
   */
  public IHTTPSession getSession() {
    return session;
  }




  /**
   * @param session the session to set
   */
  public void setSession( IHTTPSession session ) {
    this.session = session;
  }




  /**
   * @return true if the response is ready to send to the requester, false the request is still processing
   */
  public synchronized boolean isComplete() {
    return complete;
  }




  /**
   * @param flag the complete to set
   */
  public synchronized void setComplete( boolean flag ) {
    this.complete = flag;
  }




  /**
   * @return the response status
   */
  public synchronized Status getResponseStatus() {
    return responseStatus;
  }




  /**
   * @param status the response status to set
   */
  public synchronized void setResponseStatus( Status status ) {
    this.responseStatus = status;
  }




  /**
   * @return the response MIME type
   */
  public synchronized MimeType getResponseType() {
    return responseType;
  }




  /**
   * @param mimeType the response Type to set
   */
  public synchronized void setResponseType( MimeType mimeType ) {
    this.responseType = mimeType;
  }




  /**
   * @return the response Text
   */
  public synchronized String getResponseText() {
    return responseText;
  }




  /**
   * @param text the responseText to set
   */
  public synchronized void setResponseText( String text ) {
    this.responseText = text;
  }

}
