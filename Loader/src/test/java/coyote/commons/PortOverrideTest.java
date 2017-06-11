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
package coyote.commons;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import coyote.commons.network.http.HTTPD;
import coyote.commons.network.http.TestHttpClient;
import coyote.commons.network.http.TestResponse;
import coyote.loader.cfg.Config;
import coyote.loader.log.ConsoleAppender;
import coyote.loader.log.Log;


/**
 * This tests the ability to specify and override the bind port from the 
 * command line. This is required for operation in Heroku.
 */
public class PortOverrideTest {

  private static int port = NetUtil.getNextAvailablePort( 7428 );
  private static Thread serverThread;
  private static WebServer server = null;




  /**
   * @throws java.lang.Exception
   */
  @BeforeClass
  public static void setUpBeforeClass() throws Exception {

    Log.addLogger( Log.DEFAULT_LOGGER_NAME, new ConsoleAppender( Log.TRACE_EVENTS | Log.DEBUG_EVENTS | Log.INFO_EVENTS | Log.WARN_EVENTS | Log.ERROR_EVENTS | Log.FATAL_EVENTS | HTTPD.EVENT ) );

    // create a server
    server = new WebServer();

    // mimic the command line arguments
    server.setCommandLineArguments( new String[] { "caller", "arg", "-p", Integer.toString( port ) } );

    // configure it
    server.configure( new Config() );
    server.addHandler( "/", coyote.commons.TestHandler.class, "123" );

    // run the server in a separate thread, returning that thread
    serverThread = server.execute();
    server.waitForActive( 1000 );
  }




  /**
   * @throws java.lang.Exception
   */
  @AfterClass
  public static void tearDownAfterClass() throws Exception {
    server.shutdown();
    serverThread.join( 2000 );
    Assert.assertFalse( serverThread.isAlive() );
  }




  @Test
  public void test() {
    assertTrue( server.isActive() );
    assertTrue( server.getPort() == port );
  }




  @Test
  public void testConnect() {
    final TestResponse response = TestHttpClient.sendGet( "http://localhost:" + port );
    assertTrue( response.isComplete() );
    assertEquals( response.getStatus(), 200 );
    assertEquals( "123", response.getData() );
  }

}
