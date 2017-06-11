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
package coyote.commons.network.http.auth;

//import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import coyote.commons.NetUtil;
import coyote.commons.network.http.HTTPD;
import coyote.commons.network.http.TestHttpClient;
import coyote.commons.network.http.TestResponse;
import coyote.commons.network.http.TestRouter;
import coyote.commons.network.http.responder.HTTPDRouter;


/**
 * 
 */
public class AuthProviderTest {

  private static HTTPDRouter server = null;
  private static int port = 62611;
  private static final TestAuthProvider AUTH_PROVIDER = new TestAuthProvider();




  /**
   * @throws java.lang.Exception
   */
  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    port = NetUtil.getNextAvailablePort( port );
    server = new TestRouter( port );

    // set a test auth provider in the base server
    server.setAuthProvider( AUTH_PROVIDER );

    // add a protected uri resource 
    server.addRoute( "/", Integer.MAX_VALUE, ProtectedResponder.class );

    // try to start the server, waiting only 2 seconds before giving up
    try {
      server.start( HTTPD.SOCKET_READ_TIMEOUT, true );
      long start = System.currentTimeMillis();
      Thread.sleep( 100L );
      while ( !server.wasStarted() ) {
        Thread.sleep( 100L );
        if ( System.currentTimeMillis() - start > 2000 ) {
          server.stop();
          fail( "could not start server" );
        }
      }
    } catch ( IOException ioe ) {
      fail( "could not start server" );
    }
  }




  /**
   * @throws java.lang.Exception
   */
  @AfterClass
  public static void tearDownAfterClass() throws Exception {
    server.stop();
  }




  @Test
  public void test() {
    AUTH_PROVIDER.allowAllConnections();
    AUTH_PROVIDER.allowAllAuthentications();
    AUTH_PROVIDER.allowAllAuthorizations();
    TestResponse response = TestHttpClient.sendGet( "http://localhost:" + port );
    assertTrue( response.isComplete() );
    assertEquals( response.getStatus(), 200 );
    assertTrue( server.isAlive() );

    // Make sure the server drops the connection if SSL is not enabled.
    // No status should be returned so the response should be incomplete.
    AUTH_PROVIDER.rejectAllConnections();
    response = TestHttpClient.sendPost( "http://localhost:" + port );
    // not a very good test, refactor
    assertTrue( response.isComplete() );
    assertTrue( server.isAlive() );

  }

}
