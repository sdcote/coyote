package coyote.commons.network.http;

import static org.junit.Assert.assertEquals;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.Map;

import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.junit.Test;

import coyote.commons.network.MimeType;


public class PutStreamIntegrationTest extends IntegrationTestBase<PutStreamIntegrationTest.TestServer> {

  public static class TestServer extends HTTPD {

    public TestServer() {
      super( 8192 );
    }




    @Override
    public Response serve( final IHTTPSession session ) {
      final Method method = session.getMethod();
      final Map<String, String> headers = session.getRequestHeaders();
      final int contentLength = Integer.parseInt( headers.get( "content-length" ) );

      byte[] body;
      try {
        final DataInputStream dataInputStream = new DataInputStream( session.getInputStream() );
        body = new byte[contentLength];
        dataInputStream.readFully( body, 0, contentLength );
      } catch ( final IOException e ) {
        return Response.createFixedLengthResponse( Status.INTERNAL_ERROR, MimeType.TEXT.getType(), e.getMessage() );
      }

      final String response = String.valueOf( method ) + ':' + new String( body );
      return Response.createFixedLengthResponse( response );
    }

  }




  @Override
  public TestServer createTestServer() {
    return new TestServer();
  }




  @Test
  public void testSimplePutRequest() throws Exception {
    final String expected = "This HttpPut request has a content-length of 48.";

    final HttpPut httpput = new HttpPut( "http://localhost:8192/" );
    httpput.setEntity( new ByteArrayEntity( expected.getBytes() ) );
    final ResponseHandler<String> responseHandler = new BasicResponseHandler();
    final String responseBody = httpclient.execute( httpput, responseHandler );

    assertEquals( "PUT:" + expected, responseBody );
  }
}
