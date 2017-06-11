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
package coyote.commons.network.http.responder;

//import static org.junit.Assert.*;
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
import coyote.commons.network.http.responder.ResourceResponder;


/**
 * 
 */
public class ResourceResponderTest {

  private static TestRouter server = null;
  private static int port = 3232;




  /**
   * @throws java.lang.Exception
   */
  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    port = NetUtil.getNextAvailablePort( port );
    server = new TestRouter( port );
    server.addDefaultRoutes();

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
    server.addRoute( "/", Integer.MAX_VALUE, ResourceResponder.class, "content" );
    server.addRoute( "/(.)+", Integer.MAX_VALUE, ResourceResponder.class, "content", true );

    try {
      TestResponse response = TestHttpClient.sendGet( "http://localhost:" + port );
      System.out.println( response.getStatus() + ":" + response.getData() );
      assertTrue( response.getStatus() == 200 );
      assertTrue( server.isAlive() );

      response = TestHttpClient.sendGet( "http://localhost:" + port + "/" );
      System.out.println( response.getStatus() + ":" + response.getData() );
      assertTrue( response.getStatus() == 200 );
      assertTrue( server.isAlive() );

      response = TestHttpClient.sendGet( "http://localhost:" + port + "/notfound.txt" );
      System.out.println( response.getStatus() + ":" + response.getData() );
      assertTrue( response.getStatus() == 404 );
      assertTrue( server.isAlive() );

    } catch ( Exception e ) {
      fail( e.getMessage() );
    }

  }

}
