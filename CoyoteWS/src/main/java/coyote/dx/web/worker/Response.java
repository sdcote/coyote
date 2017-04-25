/*
 * Copyright (c) 2015 Stephan D. Cote' - All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the MIT License which accompanies this distribution, and is
 * available at http://creativecommons.org/licenses/MIT/
 *
 * Contributors:
 *   Stephan D. Cote
 *      - Initial concept and initial implementation
 */
package coyote.dx.web.worker;

import org.apache.http.HttpRequest;

import coyote.dataframe.DataFrame;


/**
 * Used to carry data relating to the response of the requests we make
 *
 * This is deprecated. Use coyote.dx.web.Response instead.
 */
@Deprecated
public class Response {
  private HttpRequest request = null;
  private int httpStatusCode = 0;
  private String httpStatusPhrase = null;
  private DataFrame resultFrame = null;
  private DataFrame errorFrame = null;
  private String status = null;

  /** multi-purpose attribute normally used with 300 series errors containing a link to the redirected location */
  private String link = null;

  private long requestStart = 0;
  private long requestEnd = 0;
  private long parseStart = 0;
  private long parseEnd = 0;
  private long txnStart = 0;
  private long txnEnd = 0;
  private byte[] data;




  /**
   * @param request
   */
  public Response( final HttpRequest request ) {
    this.request = request;
  }




  /**
   * @return the data
   */
  public byte[] getData() {
    return data;
  }




  /**
   * @return the dataframe containing the error results if present
   */
  public DataFrame getErrorFrame() {
    return errorFrame;
  }




  /**
   * @return the HTTP status code (the 200 part of "200 OK")
   */
  public int getHttpStatusCode() {
    return httpStatusCode;
  }




  /**
   * @return the HTTP status phrase  (the "OK" part of "200 OK")
   */
  public String getHttpStatusPhrase() {
    return httpStatusPhrase;
  }




  /**
   * Some responses (e.g. 301, 302) contain a link to the location of where the
   * request should go for the requested resource.
   *
   * <p>Not all responses will contain a link. The most common scenario is when
   * the status code is in the 300 series.</p>
   *
   * @return the link set in this response
   */
  public String getLink() {
    return link;
  }




  /**
  * @return the time in milliseconds when the response data parsing ended
  */
  public long getParseEnd() {
    return parseEnd;
  }




  /**
   * @return the time in milliseconds when the response data parsing began
   */
  public long getParseStart() {
    return parseStart;
  }




  /**
   * @return number of milliseconds between the start and end of parsing.
   */
  public long getParsingElapsed() {
    return parseEnd - parseStart;
  }




  /**
   * @return the request
   */
  public HttpRequest getRequest() {
    return request;
  }




  /**
   * @return number of milliseconds between the start and end of the HTTP request.
   */
  public long getRequestElapsed() {
    return requestEnd - requestStart;
  }




  /**
   * @return the time in milliseconds when the request was received
   */
  public long getRequestEnd() {
    return requestEnd;
  }




  /**
   * @return the time in milliseconds when the request was sent
   */
  public long getRequestStart() {
    return requestStart;
  }




  /**
   * @return the data frame representing the result body
   */
  public DataFrame getResultFrame() {
    return resultFrame;
  }




  /**
   * @return the status
   */
  public String getStatus() {
    return status;
  }




  /**
   * @return number of milliseconds between the start and end of the transaction.
   */
  public long getTransactionElapsed() {
    return txnEnd - txnStart;
  }




  /**
   * @return the time in milliseconds when the transaction ended
   */
  public long getTransactionEnd() {
    return txnEnd;
  }




  /**
   * @return the time in milliseconds  when the transaction started.
   */
  public long getTransactionStart() {
    return txnStart;
  }




  /**
   * Client Error- Indicate cases in which the client seems to have erred.
   * @return true if the status code is in the 4XX range
   */
  public boolean isClientError() {
    return ( ( httpStatusCode >= 400 ) && ( httpStatusCode < 500 ) );
  }




  /**
   * Error - Either client or server error.
   * @return true if the status code is greater than or equal to 400.
   */
  public boolean isError() {
    return ( httpStatusCode >= 400 );
  }




  /**
   * Informational - Request received, continuing process.
   * @return true if the status code is in the 1XX range
   */
  public boolean isInformational() {
    return ( httpStatusCode < 200 );
  }




  /**
   * Redirection - The client must take additional action to complete the
   * request.
   * @return true if the status code is in the 3XX range
   */
  public boolean isRedirect() {
    return ( ( httpStatusCode >= 300 ) && ( httpStatusCode < 400 ) );
  }




  /**
   * Server Error - The server failed to fulfill an apparently valid request.
   * @return true if the status code is in the 5XX range
   */
  public boolean isServerError() {
    return ( httpStatusCode >= 500 );
  }




  /**
   * Success - The action requested by the client was received, understood,
   * accepted and processed successfully.
   * @return true if the status code is in the 2XX range
   */
  public boolean isSuccessful() {
    return ( ( httpStatusCode >= 200 ) && ( httpStatusCode < 300 ) );
  }




  /**
   * Set the time (in milliseconds) when the data parsing ended to the current time.
   */
  public void parseEnd() {
    parseEnd = System.currentTimeMillis();
  }




  /**
   * Set the time (in milliseconds) when the data parsing began to the current time.
   */
  public void parseStart() {
    parseStart = System.currentTimeMillis();
  }




  /**
   * Set the time (in milliseconds) when the request was received to the current time.
   */
  public void requestEnd() {
    requestEnd = System.currentTimeMillis();
  }




  /**
   * Set the time (in milliseconds) when the request was sent to the current time.
   */
  public void requestStart() {
    requestStart = System.currentTimeMillis();
  }




  /**
   * @param data the data to set
   */
  public void setData( final byte[] data ) {
    this.data = data;
  }




  /**
   * @param frame
   */
  public void setErrorFrame( final DataFrame frame ) {
    errorFrame = frame;
  }




  /**
   * @param link the link to set
   */
  public void setLink( final String link ) {
    this.link = link;
  }




  /**
   * Add the frame to the result list.
   *
   * @param frame
   */
  public void setResultFrame( final DataFrame frame ) {
    resultFrame = frame;
  }




  /**
   * @param message
   */
  public void setStatus( final String message ) {
    status = message;
  }




  /**
   * @param status
   */
  public void setStatusCode( final int status ) {
    httpStatusCode = status;
  }




  /**
   * @param phrase
   */
  public void setStatusPhrase( final String phrase ) {
    httpStatusPhrase = phrase;
  }




  /**
   * Set the time (in milliseconds) when the transaction ended to the current time.
   */
  public void transactionEnd() {
    txnEnd = System.currentTimeMillis();
  }




  /**
   * Set the time (in milliseconds) when the transaction started.
   */
  public void transactionStart() {
    txnStart = System.currentTimeMillis();
  }

}
