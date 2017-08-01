/*
 * Copyright (c) 2003 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 */

package coyote.commons.network.http;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;


public class ServerRunner {

  /**
   * logger to log to.
   */
  private static final Logger LOG = Logger.getLogger( ServerRunner.class.getName() );




  public static void executeInstance( HTTPD server ) {
    try {
      server.start( HTTPD.SOCKET_READ_TIMEOUT, false );
    } catch ( IOException ioe ) {
      System.err.println( "Couldn't start server:\n" + ioe );
      System.exit( -1 );
    }

    System.out.println( "Server running, Hit Enter to stop.\n" );

    try {
      System.in.read();
    } catch ( Throwable ignored ) {}

    server.stop();
    System.out.println( "Server stopped.\n" );
  }




  public static <T extends HTTPD> void run( Class<T> serverClass ) {
    try {
      executeInstance( serverClass.newInstance() );
    } catch ( Exception e ) {
      ServerRunner.LOG.log( Level.SEVERE, "Could not create server", e );
    }
  }
}
