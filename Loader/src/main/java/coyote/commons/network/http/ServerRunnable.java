/*
 * Copyright (c) 2002 Stephan D. Cote' - All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the MIT License which accompanies this distribution, and is
 * available at http://creativecommons.org/licenses/MIT/
 *
 * Contributors:
 *   Stephan D. Cote
 */
package coyote.commons.network.http;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

import javax.net.ssl.SSLServerSocket;

import coyote.loader.log.Log;


/**
 * The runnable that will be used for the main listening thread.
 *
 * <p>This class contains the security checks on initial connection for ACL and
 * Denial of Service.</p>
 */
public class ServerRunnable implements Runnable {

  private final HTTPD httpd;

  private final int timeout;

  IOException bindException;

  boolean hasBinded = false;




  ServerRunnable( final HTTPD httpd, final int timeout ) {
    this.httpd = httpd;
    this.timeout = timeout;
  }




  @Override
  public void run() {
    boolean secured;
    try {
      httpd.myServerSocket.bind( httpd.hostname != null ? new InetSocketAddress( httpd.hostname, httpd.myPort ) : new InetSocketAddress( httpd.myPort ) );
      hasBinded = true;
    } catch ( final IOException e ) {
      bindException = e;
      return;
    }
    do {
      try {
        final Socket clientSocket = httpd.myServerSocket.accept();
        if ( timeout > 0 ) {
          clientSocket.setSoTimeout( timeout );
        }

        // if the
        if ( httpd.myServerSocket instanceof SSLServerSocket ) {
          secured = true;
        } else {
          secured = false;
        }

        // First check if the address has been calling us too frequently
        // indicating a possible denial of service attack
        if ( httpd.dosTable.check( clientSocket.getInetAddress() ) ) {
          // Allow only connections from the local host or from remote hosts on
          // our ACL
          if ( clientSocket.getLocalAddress().equals( clientSocket.getInetAddress() ) || httpd.acl.allows( clientSocket.getInetAddress() ) ) {
            final InputStream inputStream = clientSocket.getInputStream();
            httpd.asyncRunner.exec( httpd.createClientHandler( clientSocket, inputStream, secured ) );
          } else {
            Log.append( HTTPD.EVENT, "Remote connection from " + clientSocket.getInetAddress() + " on port " + clientSocket.getPort() + " refused due to ACL restrictions" );
            HTTPD.safeClose( clientSocket );
          }
        } else {
          Log.append( HTTPD.EVENT, "Remote connection from " + clientSocket.getInetAddress() + " on port " + clientSocket.getPort() + " refused due to possible Denial of Service activity" );
          HTTPD.safeClose( clientSocket );
          // TODO: track the number of breaches from this client and either throttle, or blacklist the IP
          // TODO: track the number of events globally to detect a DDoS and terminate/retract/hide the server - it can be restarted later last gasp message to CO giving the new port
        }
      } catch ( final IOException e ) {
        Log.append( HTTPD.EVENT, "WARNING: Communication with the client broken", e );
      }
    }
    while ( !httpd.myServerSocket.isClosed() );
  }
}