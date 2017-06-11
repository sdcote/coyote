package coyote.commons.network.http;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PipedInputStream;

import org.junit.Test;


public class HttpChunkedResponseTest extends HttpServerTest {

  private static class ChunkedInputStream extends PipedInputStream {

    int chunk = 0;

    String[] chunks;




    private ChunkedInputStream( final String[] chunks ) {
      this.chunks = chunks;
    }




    @Override
    public synchronized int read( final byte[] buffer, final int off, final int len ) throws IOException {
      for ( int i = 0; i < chunks[chunk].length(); ++i ) {
        buffer[i] = (byte)chunks[chunk].charAt( i );
      }
      return chunks[chunk++].length();
    }
  }




  @Test
  public void thatChunkedContentIsChunked() throws Exception {
    final PipedInputStream pipedInputStream = new ChunkedInputStream( new String[] { "some", "thing which is longer than sixteen characters", "whee!", "" } );
    final String[] expected = { "HTTP/1.1 200 OK", "Content-Type: what/ever", "Date: .*", "Connection: keep-alive", "Transfer-Encoding: chunked", "", "4", "some", "2d", "thing which is longer than sixteen characters", "5", "whee!", "0", "" };
    testServer.response = Response.createChunkedResponse( Status.OK, "what/ever", pipedInputStream );
    testServer.response.setChunkedTransfer( true );

    final ByteArrayOutputStream byteArrayOutputStream = invokeServer( "GET / HTTP/1.1" );

    assertResponse( byteArrayOutputStream, expected );
  }
}
