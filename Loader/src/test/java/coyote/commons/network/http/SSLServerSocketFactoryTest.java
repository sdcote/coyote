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
package coyote.commons.network.http;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import javax.net.ssl.SSLServerSocket;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpTrace;
import org.apache.http.impl.client.DefaultHttpClient;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import coyote.commons.NetUtil;


/**
 * Generating a self-signed SSL certificate:
 *         keytool -genkey -keyalg RSA -alias selfsigned -keystore keystore.jks -storepass password -validity 360 -keysize 2048 -ext SAN=DNS:localhost,IP:127.0.0.1  -validity 9999
 * This will generate a keystore file named 'keystore.jks' with a self signed certificate for a host named localhost with the IP address 127.0.0.1 . Now you can use:
 *         server.makeSecure(HTTPD.makeSSLSocketFactory("/keystore.jks", "password".toCharArray()), null);
 * before you start the server to make HTTPD serve HTTPS connections. Make sure 'keystore.jks' is in your classpath.
 */
public class SSLServerSocketFactoryTest {
  private static TestingServer testServer = null;
  private static int port = 62611;




  /**
   * @throws java.lang.Exception
   */
  @BeforeClass
  public static void setUpBeforeClass() throws Exception {

    // setup the keystore
    System.setProperty( "javax.net.ssl.trustStore", new File( "src/test/resources/keystore.jks" ).getAbsolutePath() );

    // make sure our port is available, or pick the next available
    port = NetUtil.getNextAvailablePort( port );

    // start the server on the port
    testServer = new TestingServer( port );

    // set the secure server socket factory using the keystore
    testServer.makeSecure( HTTPD.makeSSLSocketFactory( "/keystore.jks", "password".toCharArray() ), null ); // which way is better?
    //testServer.setServerSocketFactory( new SecureServerSocketFactory( HTTPD.makeSSLSocketFactory( "/keystore.jks", "password".toCharArray() ), null ) );

    // start the server
    testServer.start();

    // try to start the server, waiting only 2 seconds before giving up
    try {
      final long start = System.currentTimeMillis();
      Thread.sleep( 100L );
      while ( !testServer.wasStarted() ) {
        Thread.sleep( 100L );
        if ( ( System.currentTimeMillis() - start ) > 2000 ) {
          testServer.stop();
          fail( "could not start server" );
        }
      }
    } catch ( final InterruptedException e ) {}
  }




  /**
   * @throws java.lang.Exception
   */
  @AfterClass
  public static void tearDownAfterClass() throws Exception {
    testServer.stop();
  }




  @Test
  public void createPassesTheProtocolsToServerSocket() throws IOException {
    // first find the supported protocols
    SecureServerSocketFactory secureServerSocketFactory = new SecureServerSocketFactory( HTTPD.makeSSLSocketFactory( "/keystore.jks", "password".toCharArray() ), null );
    SSLServerSocket socket = (SSLServerSocket)secureServerSocketFactory.create();
    String[] protocols = socket.getSupportedProtocols();

    // remove one element from supported protocols
    if ( protocols.length > 0 ) {
      protocols = Arrays.copyOfRange( protocols, 0, protocols.length - 1 );
    }

    // test
    secureServerSocketFactory = new SecureServerSocketFactory( HTTPD.makeSSLSocketFactory( "/keystore.jks", "password".toCharArray() ), protocols );
    socket = (SSLServerSocket)secureServerSocketFactory.create();
    Assert.assertArrayEquals( "Enabled protocols specified in the factory were not set to the socket.", protocols, socket.getEnabledProtocols() );
  }




  @Test
  public void testConnectViaSSL() {
    final TestResponse response = TestHttpClient.sendGet( "https://localhost:" + port );
    assertTrue( response.isComplete() );
    assertEquals( response.getStatus(), 200 );
  }




  // Apache HttpClient
  public void testSSLConnection() throws ClientProtocolException, IOException {
    final DefaultHttpClient httpclient = new DefaultHttpClient();
    final HttpTrace httphead = new HttpTrace( "https://localhost:" + port + "/" );
    final HttpResponse response = httpclient.execute( httphead );
    response.getEntity();
    Assert.assertEquals( 200, response.getStatusLine().getStatusCode() );

    Assert.assertEquals( port, testServer.getListeningPort() );
    Assert.assertTrue( testServer.isAlive() );
  }

}
