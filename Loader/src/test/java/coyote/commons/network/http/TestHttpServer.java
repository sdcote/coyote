package coyote.commons.network.http;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.UnsupportedEncodingException;

import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;


public class TestHttpServer extends AbstractTestHttpServer {

  private static PipedOutputStream stdIn;

  private static Thread serverStartThread;




  @BeforeClass
  public static void setUp() throws Exception {
    stdIn = new PipedOutputStream();
    System.setIn( new PipedInputStream( stdIn ) );
    serverStartThread = new Thread( new Runnable() {

      @Override
      public void run() {
        final String[] args = { "--host", "localhost", "--port", "9090", "--dir", "src/test/resources" };
        SimpleWebServer.main( args );
      }
    } );
    serverStartThread.start();
    Thread.sleep( 100 );
  }




  @AfterClass
  public static void tearDown() throws Exception {
    stdIn.write( "\n\n".getBytes() );
    serverStartThread.join( 2000 );
    Assert.assertFalse( serverStartThread.isAlive() );
  }




  @Test
  public void doArgumentTest() throws InterruptedException, UnsupportedEncodingException, IOException {
    final String testPort = "9458";
    Thread testServer = new Thread( new Runnable() {

      @Override
      public void run() {
        String[] args = { "-h", "localhost", "-p", testPort, "-d", "src/test/resources" };
        SimpleWebServer.main( args );
      }
    } );

    testServer.start();
    Thread.sleep( 200 );

    HttpGet httpget = new HttpGet( "http://localhost:" + testPort + "/" );
    CloseableHttpClient httpclient = HttpClients.createDefault();

    CloseableHttpResponse response = null;
    try {
      response = httpclient.execute( httpget );
      HttpEntity entity = response.getEntity();
      String str = new String( readContents( entity ), "UTF-8" );
      assertTrue( "The response entity didn't contain the string 'data'", str.indexOf( "data" ) >= 0 );
    }
    finally {
      if ( response != null )
        response.close();
    }
  }




  @Ignore
  public void doPlugin() throws Exception {
    //    CloseableHttpClient httpclient = HttpClients.createDefault();
    //    HttpGet httpget = new HttpGet( "http://localhost:9090/index.xml" );
    //    CloseableHttpResponse response = httpclient.execute( httpget );
    //    String string = new String( readContents( response.getEntity() ), "UTF-8" );
    //    assertEquals( "<xml/>", string );
    //    response.close();
    //
    //    httpget = new HttpGet( "http://localhost:9090/data/data/different.xml" );
    //    response = httpclient.execute( httpget );
    //    string = new String( readContents( response.getEntity() ), "UTF-8" );
    //    assertEquals( "<xml/>", string );
    //    response.close();
  }




  @Test
  public void doSomeBasicTest() throws Exception {
    CloseableHttpClient httpclient = HttpClients.createDefault();
    HttpGet httpget = new HttpGet( "http://localhost:9090/data/test.html" );
    CloseableHttpResponse response = httpclient.execute( httpget );
    HttpEntity entity = response.getEntity();
    String string = new String( readContents( entity ), "UTF-8" );
    assertEquals( "<html><head><title>test</title></head><body><h1>Hello</h1></body></html>", string );
    response.close();

    httpget = new HttpGet( "http://localhost:9090/" );
    response = httpclient.execute( httpget );
    entity = response.getEntity();
    string = new String( readContents( entity ), "UTF-8" );
    assertTrue( string.indexOf( "data" ) > 0 );
    response.close();

    httpget = new HttpGet( "http://localhost:9090/data" );
    response = httpclient.execute( httpget );
    entity = response.getEntity();
    string = new String( readContents( entity ), "UTF-8" );
    assertTrue( string.indexOf( "test.html" ) > 0 );
    response.close();

    httpget = new HttpGet( "http://localhost:9090/data/test.pdf" );
    response = httpclient.execute( httpget );
    entity = response.getEntity();

    byte[] actual = readContents( entity );
    byte[] expected = readContents( new FileInputStream( "src/test/resources/data/test.pdf" ) );
    assertArrayEquals( expected, actual );
    response.close();
  }




  @Test
  public void doTest404() throws Exception {
    CloseableHttpClient httpclient = HttpClients.createDefault();
    HttpGet httpget = new HttpGet( "http://localhost:9090/xxx/yyy.html" );
    CloseableHttpResponse response = httpclient.execute( httpget );
    assertEquals( 404, response.getStatusLine().getStatusCode() );
    response.close();
  }




  @Test
  public void testIfNoneMatchHeader() throws ClientProtocolException, IOException {
    CloseableHttpResponse response = null;
    try {
      HttpGet httpGet = new HttpGet( "http://localhost:9090/data/test.html" );
      httpGet.addHeader( "if-none-match", "*" );
      CloseableHttpClient httpClient = HttpClients.createDefault();
      response = httpClient.execute( httpGet );
      assertEquals( "The response status to a reqeuest with 'if-non-match=*' header should be NOT_MODIFIED(304), if the file exists", 304, response.getStatusLine().getStatusCode() );
    }
    finally {
      if ( response != null ) {
        response.close();
      }
    }
  }




  @Ignore
  public void testIndexFileIsShownWhenURLEndsWithDirectory() throws ClientProtocolException, IOException {
    //    CloseableHttpResponse response = null;
    //    try {
    //      CloseableHttpClient httpClient = HttpClients.createDefault();
    //      HttpGet httpGet = new HttpGet( "http://localhost:9090/data/data" );
    //      response = httpClient.execute( httpGet );
    //      HttpEntity entity = response.getEntity();
    //      String responseString = new String( readContents( entity ), "UTF-8" );
    //      assertThat( "When the URL ends with a directory, and if an index.html file is present in that directory," + " the server should respond with that file", responseString, containsString( "Simple index file" ) );
    //    }
    //    finally {
    //      if ( response != null ) {
    //        response.close();
    //      }
    //    }
  }




  @Ignore
  public void testPluginInternalRewrite() throws ClientProtocolException, IOException {
    //    CloseableHttpResponse response = null;
    //    try {
    //      CloseableHttpClient httpClient = HttpClients.createDefault();
    //      HttpGet httpGet = new HttpGet( "http://localhost:9090/rewrite/index.xml" );
    //      response = httpClient.execute( httpGet );
    //      HttpEntity entity = response.getEntity();
    //      String responseString = new String( readContents( entity ), "UTF-8" );
    //      Assert.assertThat( "If a plugin returns an InternalRewrite from the serveFile method, the rewritten request should be served", responseString, allOf( containsString( "dummy" ), containsString( "it works" ) ) );
    //    }
    //    finally {
    //      if ( response != null ) {
    //        response.close();
    //      }
    //    }
  }




  @Test
  public void testRangeHeaderAndIfNoneMatchHeader() throws ClientProtocolException, IOException {
    CloseableHttpResponse response = null;
    try {
      HttpGet httpGet = new HttpGet( "http://localhost:9090/data/test.html" );
      httpGet.addHeader( "range", "bytes=10-20" );
      httpGet.addHeader( "if-none-match", "*" );
      CloseableHttpClient httpClient = HttpClients.createDefault();
      response = httpClient.execute( httpGet );
      assertEquals( "The response status to a request with 'if-non-match=*' header and 'range' header should be NOT_MODIFIED(304)," + " if the file exists, because 'if-non-match' header should be given priority", 304, response.getStatusLine().getStatusCode() );
    }
    finally {
      if ( response != null ) {
        response.close();
      }
    }
  }




  @Ignore
  public void testRangeHeaderWithStartAndEndPosition() throws ClientProtocolException, IOException {
    //    CloseableHttpResponse response = null;
    //    try {
    //      HttpGet httpGet = new HttpGet( "http://localhost:9090/data/test.html" );
    //      httpGet.addHeader( "range", "bytes=10-40" );
    //      CloseableHttpClient httpClient = HttpClients.createDefault();
    //      response = httpClient.execute( httpGet );
    //      HttpEntity entity = response.getEntity();
    //      String responseString = new String( readContents( entity ), "UTF-8" );
    //      Assert.assertThat( "The data from the beginning of the file should have been skipped as specified in the 'range' header", responseString, not( containsString( "<head>" ) ) );
    //      Assert.assertThat( "The data from the end of the file should have been skipped as specified in the 'range' header", responseString, not( containsString( "</head>" ) ) );
    //      assertEquals( "The 'Content-Length' should be the length from the requested start position to end position", "31", response.getHeaders( "Content-Length" )[0].getValue() );
    //      assertEquals( "The 'Content-Range' header should contain the correct lengths and offsets based on the range served", "bytes 10-40/84", response.getHeaders( "Content-Range" )[0].getValue() );
    //      assertEquals( "Response status for a successful request with 'range' header should be PARTIAL_CONTENT(206)", 206, response.getStatusLine().getStatusCode() );
    //    }
    //    finally {
    //      if ( response != null ) {
    //        response.close();
    //      }
    //    }
  }




  @Ignore
  public void testRangeHeaderWithStartPositionOnly() throws ClientProtocolException, IOException {
    //    CloseableHttpResponse response = null;
    //    try {
    //      HttpGet httpGet = new HttpGet( "http://localhost:9090/data/test.html" );
    //      httpGet.addHeader( "range", "bytes=10-" );
    //      CloseableHttpClient httpClient = HttpClients.createDefault();
    //      response = httpClient.execute( httpGet );
    //      HttpEntity entity = response.getEntity();
    //      String responseString = new String( readContents( entity ), "UTF-8" );
    //      Assert.assertThat( "The data from the beginning of the file should have been skipped as specified in the 'range' header", responseString, not( containsString( "<head>" ) ) );
    //      Assert.assertThat( "The response should contain the data from the end of the file since end position was not given in the 'range' header", responseString, containsString( "</head>" ) );
    //      assertEquals( "The content length should be the length starting from the requested byte", "74", response.getHeaders( "Content-Length" )[0].getValue() );
    //      assertEquals( "The 'Content-Range' header should contain the correct lengths and offsets based on the range served", "bytes 10-83/84", response.getHeaders( "Content-Range" )[0].getValue() );
    //      assertEquals( "Response status for a successful range request should be PARTIAL_CONTENT(206)", 206, response.getStatusLine().getStatusCode() );
    //    }
    //    finally {
    //      if ( response != null ) {
    //        response.close();
    //      }
    //    }
  }




  @Ignore
  public void testRangeStartGreaterThanFileLength() throws ClientProtocolException, IOException {
    //    CloseableHttpResponse response = null;
    //    try {
    //      HttpGet httpGet = new HttpGet( "http://localhost:9090/data/test.html" );
    //      httpGet.addHeader( "range", "bytes=1000-" );
    //      CloseableHttpClient httpClient = HttpClients.createDefault();
    //      response = httpClient.execute( httpGet );
    //      assertEquals( "Response status for a request with 'range' header value which exceeds file length should be RANGE_NOT_SATISFIABLE(416)", 416, response.getStatusLine().getStatusCode() );
    //      assertEquals( "The 'Content-Range' header should contain the correct lengths and offsets based on the range served", "bytes */84", response.getHeaders( "Content-Range" )[0].getValue() );
    //    }
    //    finally {
    //      if ( response != null ) {
    //        response.close();
    //      }
    //    }
  }




  @Test
  public void testURLContainsParentDirectory() throws ClientProtocolException, IOException {
    CloseableHttpResponse response = null;
    try {
      CloseableHttpClient httpClient = HttpClients.createDefault();
      HttpGet httpGet = new HttpGet( "http://localhost:9090/../test.html" );
      response = httpClient.execute( httpGet );
      Assert.assertEquals( "The response status should be 403(Forbidden), " + "since the server won't serve requests with '../' due to security reasons", 403, response.getStatusLine().getStatusCode() );
    }
    finally {
      if ( response != null ) {
        response.close();
      }
    }
  }
}
