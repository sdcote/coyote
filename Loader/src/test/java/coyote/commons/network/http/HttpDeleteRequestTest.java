package coyote.commons.network.http;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import org.junit.Test;

import coyote.commons.network.MimeType;


public class HttpDeleteRequestTest extends HttpServerTest {

  @Test
  public void testDeleteRequestThatDoesntSendBackResponseBody_EmptyString() throws Exception {
    testServer.response = Response.createFixedLengthResponse( Status.NO_CONTENT, MimeType.HTML.getType(), "" );
    final ByteArrayOutputStream outputStream = invokeServer( "DELETE " + HttpServerTest.URI + " HTTP/1.1" );
    final String[] expected = { "HTTP/1.1 204 No Content", "Content-Type: text/html", "Date: .*", "Connection: keep-alive", "Content-Length: 0", "" };
    assertResponse( outputStream, expected );
  }




  @Test
  public void testDeleteRequestThatDoesntSendBackResponseBody_NullInputStream() throws Exception {
    testServer.response = Response.createChunkedResponse( Status.NO_CONTENT, MimeType.HTML.getType(), (InputStream)null );
    final ByteArrayOutputStream outputStream = invokeServer( "DELETE " + HttpServerTest.URI + " HTTP/1.1" );
    final String[] expected = { "HTTP/1.1 204 No Content", "Content-Type: text/html", "Date: .*", "Connection: keep-alive", "Content-Length: 0", "" };
    assertResponse( outputStream, expected );
  }




  @Test
  public void testDeleteRequestThatDoesntSendBackResponseBody_NullString() throws Exception {
    testServer.response = Response.createFixedLengthResponse( Status.NO_CONTENT, MimeType.HTML.getType(), (String)null );
    final ByteArrayOutputStream outputStream = invokeServer( "DELETE " + HttpServerTest.URI + " HTTP/1.1" );
    final String[] expected = { "HTTP/1.1 204 No Content", "Content-Type: text/html", "Date: .*", "Connection: keep-alive", "Content-Length: 0", "" };
    assertResponse( outputStream, expected );
  }




  @Test
  public void testDeleteRequestThatSendsBackResponseBody_Accepted() throws Exception {
    testServer.response = Response.createFixedLengthResponse( Status.ACCEPTED, "application/xml", "<body />" );
    final ByteArrayOutputStream outputStream = invokeServer( "DELETE " + HttpServerTest.URI + " HTTP/1.1" );
    final String[] expected = { "HTTP/1.1 202 Accepted", "Content-Type: application/xml", "Date: .*", "Connection: keep-alive", "Content-Length: 8", "", "<body />" };
    assertResponse( outputStream, expected );
  }




  @Test
  public void testDeleteRequestThatSendsBackResponseBody_Success() throws Exception {
    testServer.response = Response.createFixedLengthResponse( Status.OK, "application/xml", "<body />" );
    final ByteArrayOutputStream outputStream = invokeServer( "DELETE " + HttpServerTest.URI + " HTTP/1.1" );
    final String[] expected = { "HTTP/1.1 200 OK", "Content-Type: application/xml", "Date: .*", "Connection: keep-alive", "Content-Length: 8", "", "<body />" };
    assertResponse( outputStream, expected );
  }

}
