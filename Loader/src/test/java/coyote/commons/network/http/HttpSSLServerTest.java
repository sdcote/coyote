package coyote.commons.network.http;

import java.io.File;
import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpTrace;
import org.apache.http.impl.client.DefaultHttpClient;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


public class HttpSSLServerTest extends HttpServerTest {

  @Override
  @Before
  public void setUp() throws Exception {
    System.setProperty( "javax.net.ssl.trustStore", new File( "src/test/resources/keystore.jks" ).getAbsolutePath() );
    testServer = new TestServer( 9043 );
    testServer.makeSecure( HTTPD.makeSSLSocketFactory( "/keystore.jks", "password".toCharArray() ), null );
    tempFileManager = new TestTempFileManager();
    testServer.start();
    try {
      final long start = System.currentTimeMillis();
      Thread.sleep( 100L );
      while ( !testServer.wasStarted() ) {
        Thread.sleep( 100L );
        if ( ( System.currentTimeMillis() - start ) > 2000 ) {
          Assert.fail( "could not start server" );
        }
      }
    } catch ( final InterruptedException e ) {}
  }




  @Override
  @After
  public void tearDown() {
    testServer.stop();
  }




  /**
   * using http to connect to https.
   *
   * @throws ClientProtocolException
   * @throws IOException
   */
  @Test(expected = ClientProtocolException.class)
  public void testHttpOnSSLConnection() throws ClientProtocolException, IOException {
    final DefaultHttpClient httpclient = new DefaultHttpClient();
    final HttpTrace httphead = new HttpTrace( "http://localhost:9043/index.html" );
    httpclient.execute( httphead );
  }




  @Test
  public void testSSLConnection() throws ClientProtocolException, IOException {
    final DefaultHttpClient httpclient = new DefaultHttpClient();
    final HttpTrace httphead = new HttpTrace( "https://localhost:9043/index.html" );
    final HttpResponse response = httpclient.execute( httphead );
    response.getEntity();
    Assert.assertEquals( 200, response.getStatusLine().getStatusCode() );

    Assert.assertEquals( 9043, testServer.getListeningPort() );
    Assert.assertTrue( testServer.isAlive() );
  }
}
