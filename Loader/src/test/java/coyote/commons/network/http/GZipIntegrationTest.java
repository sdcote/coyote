package coyote.commons.network.http;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DecompressingHttpClient;
import org.apache.http.util.EntityUtils;
import org.junit.Ignore;
import org.junit.Test;

import coyote.commons.NetUtil;


public class GZipIntegrationTest extends IntegrationTestBase<GZipIntegrationTest.TestServer> {
  
  public static final int PORT = NetUtil.getNextAvailablePort( 7428 );
  
  public static class TestServer extends HTTPD {

    public Response response;




    public TestServer() {
      super( PORT );
    }




    @Override
    public Response serve( final IHTTPSession session ) {
      return response;
    }




    @Override
    protected boolean useGzipWhenAccepted( final Response r ) {
      return true;
    }
  }









  @Test
  public void chunkedContentIsEncodedProperly() throws IOException {
    final InputStream data = new ByteArrayInputStream( "This is a test".getBytes( "UTF-8" ) );
    testServer.response = Response.createChunkedResponse( Status.OK, "text/plain", data );
    final HttpGet request = new HttpGet( "http://localhost:"+PORT+"/" );
    request.addHeader( HTTP.HDR_ACCEPT_ENCODING, "gzip" );
    final HttpResponse response = new DecompressingHttpClient( httpclient ).execute( request );
    assertEquals( "This is a test", EntityUtils.toString( response.getEntity() ) );
  }




  @Ignore
  public void contentEncodingShouldBeAddedToChunkedResponses() throws IOException {
    final InputStream data = new ByteArrayInputStream( "This is a test".getBytes( "UTF-8" ) );
    testServer.response = Response.createChunkedResponse( Status.OK, "text/plain", data );
    final HttpGet request = new HttpGet( "http://localhost:"+PORT+"/" );
    request.addHeader( HTTP.HDR_ACCEPT_ENCODING, "gzip" );
    final HttpResponse response = httpclient.execute( request );
    
    Header[] headers = response.getAllHeaders();
    for(int x=0;x<headers.length;x++){
      Header hdr = headers[x];
      System.out.println( hdr.getName()+" -- "+ hdr.getValue());
    }
    /*
     * Content-Type -- text/plain
     * Date -- Sun, 19 Mar 2017 23:08:39 GMT
     * Connection -- keep-alive
     * Transfer-Encoding -- chunked
     */
    
    final Header contentEncoding = response.getFirstHeader( HTTP.HDR_CONTENT_ENCODING );
    assertNotNull( "Content-Encoding should be set", contentEncoding );
    assertEquals( "gzip", contentEncoding.getValue() );
  }




  @Ignore
  public void contentEncodingShouldBeAddedToFixedLengthResponses() throws IOException {
    testServer.response = Response.createFixedLengthResponse( "This is a test" );
    final HttpGet request = new HttpGet( "http://localhost:"+PORT+"/" );
    request.addHeader( HTTP.HDR_ACCEPT_ENCODING, "gzip" );
    final HttpResponse response = httpclient.execute( request );
    final Header contentEncoding = response.getFirstHeader( HTTP.HDR_CONTENT_ENCODING );
    assertNotNull( "Content-Encoding should be set", contentEncoding );
    assertEquals( "gzip", contentEncoding.getValue() );
  }




  @Test
  public void contentLengthShouldBeRemovedFromZippedResponses() throws IOException {
    testServer.response = Response.createFixedLengthResponse( "This is a test" );
    final HttpGet request = new HttpGet( "http://localhost:"+PORT+"/" );
    request.addHeader( HTTP.HDR_ACCEPT_ENCODING, "gzip" );
    final HttpResponse response = httpclient.execute( request );
    final Header contentLength = response.getFirstHeader( HTTP.HDR_CONTENT_LENGTH);
    assertNull( "Content-Length should not be set when gzipping response", contentLength );
  }




  @Test
  public void contentShouldNotBeGzippedIfContentLengthIsAddedManually() throws IOException {
    testServer.response = Response.createFixedLengthResponse( "This is a test" );
    testServer.response.addHeader( "Content-Length", "" + ( "This is a test".getBytes( "UTF-8" ).length ) );
    final HttpGet request = new HttpGet( "http://localhost:"+PORT+"/" );
    request.addHeader( HTTP.HDR_ACCEPT_ENCODING, "gzip" );
    final HttpResponse response = httpclient.execute( request );
    final Header contentEncoding = response.getFirstHeader( HTTP.HDR_CONTENT_ENCODING );
    assertNull( "Content-Encoding should not be set when manually setting content-length", contentEncoding );
    assertEquals( "This is a test", EntityUtils.toString( response.getEntity() ) );

  }




  @Override
  public TestServer createTestServer() {
    return new TestServer();
  }




  @Test
  public void fixedLengthContentIsEncodedProperly() throws IOException {
    testServer.response = Response.createFixedLengthResponse( "This is a test" );
    final HttpGet request = new HttpGet( "http://localhost:"+PORT+"/" );
    request.addHeader( HTTP.HDR_ACCEPT_ENCODING, "gzip" );
    final HttpResponse response = new DecompressingHttpClient( httpclient ).execute( request );
    assertEquals( "This is a test", EntityUtils.toString( response.getEntity() ) );
  }




  @Test
  public void noGzipWithoutAcceptEncoding() throws IOException {
    testServer.response = Response.createFixedLengthResponse( "This is a test" );
    final HttpGet request = new HttpGet( "http://localhost:"+PORT+"/" );
    final HttpResponse response = httpclient.execute( request );
    final Header contentEncoding = response.getFirstHeader( HTTP.HDR_CONTENT_ENCODING );
    assertThat( contentEncoding, is( nullValue() ) );
    assertEquals( "This is a test", EntityUtils.toString( response.getEntity() ) );
  }




  @Ignore
  public void shouldFindCorrectAcceptEncodingAmongMany() throws IOException {
    testServer.response = Response.createFixedLengthResponse( "This is a test" );
    final HttpGet request = new HttpGet( "http://localhost:"+PORT+"/" );
    request.addHeader( HTTP.HDR_ACCEPT_ENCODING, "deflate,gzip" );
    final HttpResponse response = httpclient.execute( request );
    final Header contentEncoding = response.getFirstHeader( HTTP.HDR_CONTENT_ENCODING );
    assertNotNull( "Content-Encoding should be set", contentEncoding );
    assertEquals( "gzip", contentEncoding.getValue() );
  }

}
