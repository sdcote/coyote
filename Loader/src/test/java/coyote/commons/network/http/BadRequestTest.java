package coyote.commons.network.http;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.junit.Test;


public class BadRequestTest extends HttpServerTest {

  @Test
  public void testEmptyRequest() throws IOException {
    final ByteArrayOutputStream outputStream = invokeServer( "\n\n" );
    final String[] expected = new String[] { "HTTP/1.1 400 Bad Request" };
    assertResponse( outputStream, expected );
  }




  @Test
  public void testInvalidMethod() throws IOException {
    final ByteArrayOutputStream outputStream = invokeServer( "GETT http://example.com" );
    final String[] expected = new String[] { "HTTP/1.1 400 Bad Request" };
    assertResponse( outputStream, expected );
  }




  @Test
  public void testMissingURI() throws IOException {
    final ByteArrayOutputStream outputStream = invokeServer( "GET" );
    final String[] expected = new String[] { "HTTP/1.1 400 Bad Request" };
    assertResponse( outputStream, expected );
  }

}
