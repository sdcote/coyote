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
package coyote.dx.web;

import coyote.commons.network.http.IHTTPSession;
import coyote.dx.context.TransactionContext;


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
 */
public class ResponseFuture {

  private TransactionContext context = null;
  private IHTTPSession session;
  private volatile boolean complete = false;




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
   * @return the transaction context used by the transform engine to process 
   *         this request.
   */
  public TransactionContext getTransactionContext() {
    return context;
  }




  /**
   * This sets the transaction context in the future to the responder thread
   * can determine how to generate the response based on what is stored in 
   * this context.
   * 
   * @param context The context the transformation engine is using while 
   *        processing this transaction data. 
   */
  public void setTransactionContext( TransactionContext context ) {
    this.context = context;
  }

}
