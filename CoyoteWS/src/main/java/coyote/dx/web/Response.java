/*
 * Copyright (c) 2016 Stephan D. Cote' - All rights reserved.
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

import org.jsoup.nodes.Document;

import coyote.dataframe.DataFrame;


public class Response {
  private volatile boolean complete = false;
  private volatile DataFrame result = null;
  private volatile Document document = null;
  private volatile String body = null;

  private volatile byte[] data = null;

  // represents operation (read/write) timestamps
  private long operationStart = 0;
  private long operationEnd = 0;

  // represents web requests timestamps
  private long requestStart = 0;
  private long requestEnd = 0;

  // represents result parsing timestamps
  private long parseStart = 0;
  private long parseEnd = 0;

  // represents entire web,parsing and other overhead timestamps
  private long txnStart = 0;
  private long txnEnd = 0;

  private int httpStatusCode = 0;
  private String httpStatusPhrase = null;

  /** multi-purpose attribute normally used with 300 series errors containing a link to the redirected location */
  private String link = null;
  
  protected Object lock = new Object();




  public synchronized boolean isComplete() {
    return complete;
  }




  public boolean isTimedOut() {
    // TODO Auto-generated method stub
    return false;
  }




  public boolean isInError() {
    // TODO Auto-generated method stub
    return false;
  }




  public synchronized void setResult( DataFrame frame ) {
    result = frame;
  }




  public synchronized DataFrame getResult() {
    return result;
  }




  public synchronized void setComplete( boolean flag ) {
    complete = flag;
  }




  /**
   * @return the requestStart
   */
  public long getRequestStart() {
    return requestStart;
  }




  /**
   * @param time the requestStart to set
   */
  public void setRequestStart( long time ) {
    requestStart = time;
  }




  /**
   * Set the time (in milliseconds) when the request was sent to the current time.
   */
  public void requestStart() {
    requestStart = System.currentTimeMillis();
  }




  /**
   * @return the requestEnd
   */
  public long getRequestEnd() {
    return requestEnd;
  }




  /**
   * @param time the requestEnd to set
   */
  public void setRequestEnd( long time ) {
    requestEnd = time;
  }




  /**
   * Set the time (in milliseconds) when the request was received to the current time.
   */
  public void requestEnd() {
    requestEnd = System.currentTimeMillis();
  }




  /**
   * @return the parseStart
   */
  public long getParseStart() {
    return parseStart;
  }




  /**
   * @param time the parseStart to set
   */
  public void setParseStart( long time ) {
    parseStart = time;
  }




  /**
   * Set the time (in milliseconds) when the data parsing began to the current time.
   */
  public void parseStart() {
    parseStart = System.currentTimeMillis();
  }




  /**
   * @return the parseEnd
   */
  public long getParseEnd() {
    return parseEnd;
  }




  /**
   * @param time the parseEnd to set
   */
  public void setParseEnd( long time ) {
    parseEnd = time;
  }




  /**
   * Set the time (in milliseconds) when the data parsing ended to the current time.
   */
  public void parseEnd() {
    parseEnd = System.currentTimeMillis();
  }




  /**
   * @return the txnStart
   */
  public long getTransactionStart() {
    return txnStart;
  }




  /**
   * @param time the txnStart to set
   */
  public void setTransactionStart( long time ) {
    txnStart = time;
  }




  /**
   * Set the time (in milliseconds) when the transaction started.
   */
  public void transactionStart() {
    txnStart = System.currentTimeMillis();
  }




  /**
   * @return the txnEnd
   */
  public long getTransactionEnd() {
    return txnEnd;
  }




  /**
   * @param time the txnEnd to set
   */
  public void setTransactionEnd( long time ) {
    txnEnd = time;
  }




  /**
   * Set the time (in milliseconds) when the transaction ended to the current time.
   */
  public void transactionEnd() {
    txnEnd = System.currentTimeMillis();
  }




  /**
   * Set the time (in milliseconds) when the operation (read/write) started.
   */
  public void operationStart() {
    operationStart = System.currentTimeMillis();
  }




  /**
   * Set the time (in milliseconds) when the operation (read/write) ended to the current time.
   */
  public void operationEnd() {
    operationEnd = System.currentTimeMillis();
  }




  /**
   * @return the operationEnd
   */
  protected long getOperationEnd() {
    return operationEnd;
  }




  /**
   * @param time the operationEnd to set
   */
  protected void setOperationEnd( long time ) {
    operationEnd = time;
  }




  /**
   * Formats the given number of milliseconds into hours, minutes and seconds 
   * and if requested the remaining milliseconds.
   * 
   * @param val the interval in milliseconds
   * 
   * @return the time interval in hh:mm:ss format.
   */
  public String formatElapsedMillis( long val, boolean millis ) {
    StringBuilder buf = new StringBuilder( 20 );
    String sgn = "";

    if ( val < 0 ) {
      sgn = "-";
      val = Math.abs( val );
    }

    append( buf, sgn, 0, ( val / 3600000 ) );
    append( buf, ":", 2, ( ( val % 3600000 ) / 60000 ) );
    append( buf, ":", 2, ( ( val % 60000 ) / 1000 ) );
    if ( millis )
      append( buf, ".", 3, ( val % 1000 ) );

    return buf.toString();
  }




  /** Append a right-aligned and zero-padded numeric value to a `StringBuilder`. */
  static private void append( StringBuilder tgt, String pfx, int dgt, long val ) {
    tgt.append( pfx );
    if ( dgt > 1 ) {
      int pad = ( dgt - 1 );
      for ( long xa = val; xa > 9 && pad > 0; xa /= 10 ) {
        pad--;
      }
      for ( int xa = 0; xa < pad; xa++ ) {
        tgt.append( '0' );
      }
    }
    tgt.append( val );
  }




  /**
   * @return formatted time of how long the web request took to process.
   */
  public String getRequestTime() {
    return formatElapsedMillis( getRequestElapsed(), true );
  }




  /**
   * @return number of milliseconds the entire web request took to process.
   */
  public long getRequestElapsed() {
    return requestEnd - requestStart;
  }




  /**
     * @return formatted time of how long the request payload took to parse.
   */
  public String getParsingTime() {
    return formatElapsedMillis( parseEnd - parseStart, true );
  }




  /**
   * @return formatted time of how long the entire web exchange took to process including HTTP exchange, parsing and other processing.
   */
  public String getTransactionTime() {
    return formatElapsedMillis( getTransactionElapsed(), true );
  }




  /**
   * @return number of milliseconds the entire web exchange took to process including HTTP exchange, parsing and other processing.
   */
  public long getTransactionElapsed() {
    return txnEnd - txnStart;
  }




  /**
   * @return formatted time of how long the entire operation (read or write) took to process.
   */
  public String getOperationTime() {
    return formatElapsedMillis( getOperationElapsed(), true );
  }




  /**
   * @return number of milliseconds the entire operation (read or write) took to process.
   */
  public long getOperationElapsed() {
    return operationEnd - operationStart;
  }




  /**
   * @return the operationStart
   */
  public long getOperationStart() {
    return operationStart;
  }




  /**
   * @param millis the time in milliseconds to set
   */
  public void setOperationStart( long millis ) {
    this.operationStart = millis;
  }




  /**
   * @param code the HTTP Status Code to set
   */
  public void setHttpStatusCode( int code ) {
    httpStatusCode = code;
  }




  /**
   * @return the HTTP Status Phrase
   */
  public String getHttpStatusPhrase() {
    return httpStatusPhrase;
  }




  /**
   * @param phrase the HTTP Status Phrase to set
   */
  public void setHttpStatusPhrase( String phrase ) {
    httpStatusPhrase = phrase;
  }




  /**
   * @return the HTTP Status Code
   */
  public int getHttpStatusCode() {
    return httpStatusCode;
  }




  /**
   * @return the document returned from the resource unless a data frame was generated.
   */
  public Document getDocument() {
    return document;
  }




  /**
   * @param document the document object model to set
   */
  public void setDocument( Document document ) {
    this.document = document;
  }




  /**
   * @param data the data to set
   */
  public void setData( byte[] data ) {
    this.data = data;
  }




  /**
   * @return the data (if any) returned from the resource unless used to create a data frame or a document.
   */
  public byte[] getData() {
    return data;
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
   * @param link the link to set
   */
  public void setLink( String link ) {
    this.link = link;
  }




  public void setBody( String body ) {
    this.body = body;
  }




  /**
   * @return the body of the HTTP response as a string
   */
  public String getBody() {
    return body;
  }




  /**
   * Wait for the response to complete for the given number of milliseconds
   * @param timeout number of milliseconds to wait for the response to be completed.
   */
  public void waitForComplete( int timeout ) {
    if ( !isComplete() ) {
      final long tout = System.currentTimeMillis() + timeout;
      while ( tout > System.currentTimeMillis() ) {
        synchronized( lock ) {
          try {
            lock.wait( 10 );
          } catch ( final Throwable t ) {}
        }
        if ( isComplete() ) {
          break;
        }
      }
    }   
  }

}
