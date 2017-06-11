package coyote.commons.network.http;

import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;


public class HttpPutRequestTest extends HttpServerTest {

  @Test
  public void testPutRequestSendsContent() throws Exception {
    final ByteArrayOutputStream outputStream = invokeServer( "PUT " + HttpServerTest.URI + " HTTP/1.1\r\n\r\nBodyData 1\nLine 2" );

    final String[] expectedOutput = { "HTTP/1.1 200 OK", "Content-Type: text/html", "Date: .*", "Connection: keep-alive", "Content-Length: 0", "" };

    assertResponse( outputStream, expectedOutput );

    assertTrue( testServer.body.containsKey( "content" ) );
    BufferedReader reader = null;
    try {
      final String[] expectedInputToServeMethodViaFile = { "BodyData 1", "Line 2" };
      String content = testServer.body.getAsString( "content" );
      final List<String> lines =  Arrays.asList(content.split("\\n"));
      assertLinesOfText( expectedInputToServeMethodViaFile, lines );
    }
    finally {
      if ( reader != null ) {
        reader.close();
      }
    }
  }

}
