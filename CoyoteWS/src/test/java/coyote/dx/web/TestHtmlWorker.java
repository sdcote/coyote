/*
 * Copyright (c) 2017 Stephan D. Cote' - All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the MIT License which accompanies this distribution, and is
 * available at http://creativecommons.org/licenses/MIT/
 *
 * Contributors:
 *   Stephan D. Cote
 *      - Initial concept and initial implementation
 */
package coyote.dx.web;

import static org.junit.Assert.assertEquals;
//import static org.junit.Assert.*;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import coyote.commons.NetUtil;
import coyote.commons.network.http.HTTPD;


/**
 *
 */
public class TestHtmlWorker {

  private static HTTPD server = null;
  private static int port = 54321;




  /**
   * @throws java.lang.Exception
   */
  @BeforeClass
  public static void setUpBeforeClass() throws Exception {

    port = NetUtil.getNextAvailablePort( port );
    server = new TestingServer( port );

    try {
      server.start( HTTPD.SOCKET_READ_TIMEOUT, true );
    } catch ( final IOException ioe ) {
      System.err.println( "Couldn't start server:\n" + ioe );
      server.stop();
      server = null;
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
    assertNotNull( server );
    assertTrue( port == server.getPort() );
  }




  /**
   * This is a control test to make sure the HTTP server is running and the 
   * test file is accessible
   */
  @Test
  public void baseServerTest() throws ClientProtocolException, IOException {
    CloseableHttpResponse response = null;
    try {
      HttpGet httpGet = new HttpGet( "http://localhost:" + port + "/data/test.html" );
      httpGet.addHeader( "if-none-match", "*" );
      CloseableHttpClient httpClient = HttpClients.createDefault();
      response = httpClient.execute( httpGet );
      assertEquals( 200, response.getStatusLine().getStatusCode() );
    }
    finally {
      if ( response != null ) {
        response.close();
      }
    }
  }




  @Test
  public void setHtmlGet() throws IOException {
    Resource resource = new Resource( "http://localhost:" + port + "/data/test.html" );
    Response response = resource.request();
    assertNotNull( response );

    while ( !response.isComplete() ) {
      Thread.yield();
    }

    Document doc = response.getDocument();
    assertNotNull( doc );
    Elements elements = doc.getAllElements();
    System.out.println( doc.toString() );
    System.out.println( "Retrieved document contains "+elements.size()+" elements" );
    assertTrue( elements.size() >= 40 );
  }

}
