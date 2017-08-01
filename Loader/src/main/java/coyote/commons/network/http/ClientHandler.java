/*
 * Copyright (c) 2003 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 */

package coyote.commons.network.http;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

import coyote.loader.log.Log;


/**
 * The runnable that will be used for every new client connection.
 * 
 * <p>This is the class which generates the HTTP Session
 */
public class ClientHandler implements Runnable {

  /** The server which created the connection */
  private final HTTPD httpd;
  /** The socket input stream */
  private final InputStream inputStream;
  /** The socket */
  private final Socket clientSocket;
  /** Flag indicating the connection is over a secured socket server, an encrypted connection */
  private final boolean secure;




  ClientHandler( final HTTPD daemon, final InputStream input, final Socket acptSocket, final boolean secured ) {
    httpd = daemon;
    inputStream = input;
    clientSocket = acptSocket;
    secure = secured;
  }




  public void close() {
    HTTPD.safeClose( inputStream );
    HTTPD.safeClose( clientSocket );
  }




  @Override
  public void run() {
    OutputStream outputStream = null;
    try {
      outputStream = clientSocket.getOutputStream();
      final CacheManager tempFileManager = httpd.cacheManagerFactory.create();
      final HTTPSession session = new HTTPSession( httpd, tempFileManager, inputStream, outputStream, clientSocket.getInetAddress(),clientSocket.getPort(),secure );
      while ( !clientSocket.isClosed() ) {
        session.execute();
      }
    } catch ( final Exception e ) {
      // When the socket is closed by the client, we throw our own 
      // SocketException to break the "keep alive" loop above. If the exception 
      // was anything other than the expected SocketException OR a 
      // SocketTimeoutException, print the stack trace
      if ( !( ( e instanceof SocketException ) && "HTTPD Shutdown".equals( e.getMessage() ) ) && !( e instanceof SocketTimeoutException ) ) {
        Log.append( HTTPD.EVENT, "ERROR: Communication with the client broken, or an bug in the handler code", e );
      }
    }
    finally {
      HTTPD.safeClose( outputStream );
      HTTPD.safeClose( inputStream );
      HTTPD.safeClose( clientSocket );
      httpd.asyncRunner.closed( this );
    }
  }

}