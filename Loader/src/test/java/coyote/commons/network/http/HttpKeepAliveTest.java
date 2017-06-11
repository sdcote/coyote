package coyote.commons.network.http;

import static org.junit.Assert.fail;

import java.io.ByteArrayOutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

import org.junit.Ignore;


public class HttpKeepAliveTest extends HttpServerTest {

  private Throwable error = null;




  @Ignore
  public void testManyGetRequests() throws Exception {
    final String request = "GET " + HttpServerTest.URI + " HTTP/1.1\r\n\r\n";
    final String[] expected = { "HTTP/1.1 200 OK", "Content-Type: text/html", "Date: .*", "Connection: keep-alive", "Content-Length: 0", "" };
    testManyRequests( request, expected );
  }




  @Ignore
  public void testManyPutRequests() throws Exception {
    final String data = "BodyData 1\nLine 2";
    final String request = "PUT " + HttpServerTest.URI + " HTTP/1.1\r\nContent-Length: " + data.length() + "\r\n\r\n" + data;
    final String[] expected = { "HTTP/1.1 200 OK", "Content-Type: text/html", "Date: .*", "Connection: keep-alive", "Content-Length: 0", "" };
    testManyRequests( request, expected );
  }




  /**
   * Issue the given request many times to check whether an error occurs. For
   * this test, a small stack size is used, since a stack overflow is among 
   * the possible errors.
   *
   * @param request The request to issue
   * @param expected The expected response
   */
  public void testManyRequests( final String request, final String[] expected ) throws Exception {
    final Runnable r = new Runnable() {

      @Override
      public void run() {
        try {
          final PipedOutputStream requestStream = new PipedOutputStream();
          final PipedInputStream inputStream = new PipedInputStream( requestStream );
          final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
          final DefaultCacheManager tempFileManager = new DefaultCacheManager();
          try {
            final HTTPSession session = HttpKeepAliveTest.this.testServer.createSession( tempFileManager, inputStream, outputStream );
            for ( int i = 0; i < 2048; i++ ) {
              requestStream.write( request.getBytes() );
              requestStream.flush();
              outputStream.reset();
              session.execute();
              assertResponse( outputStream, expected );
            }

            // Finally, try "Connection: Close"
            final String closeReq = request.replaceAll( HTTP.VERSION_1_1, "HTTP/1.1\r\nConnection: Close" );
            expected[3] = "Connection: close";
            requestStream.write( closeReq.getBytes() );
            outputStream.reset();
            requestStream.flush();
            // Server should now close the socket by throwing a SocketException:
            try {
              session.execute();
            } catch ( final java.net.SocketException se ) {
              junit.framework.Assert.assertEquals( se.getMessage(), "HTTPD Shutdown" );
            }
            assertResponse( outputStream, expected );

          }
          finally {
            tempFileManager.clear();
          }
        } catch ( final Throwable t ) {
          error = t;
        }
      }
    };
    final Thread t = new Thread( null, r, "Request Thread", 1 << 17 );
    t.start();
    t.join();
    if ( error != null ) {
      fail( "" + error );
      error.printStackTrace();
    }
  }
}
