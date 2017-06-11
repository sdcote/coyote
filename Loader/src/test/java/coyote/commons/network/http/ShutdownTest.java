package coyote.commons.network.http;

import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.junit.Ignore;


public class ShutdownTest {

  private class TestServer extends HTTPD {

    public TestServer() {
      super( 8092 );
    }




    @Override
    public Response serve( final IHTTPSession session ) {
      return Response.createFixedLengthResponse( "Whatever" );
    }
  }




  @Ignore
  public void connectionsAreClosedWhenServerStops() throws IOException {
    final TestServer server = new TestServer();
    server.start();
    makeRequest();
    server.stop();
    try {
      makeRequest();
      fail( "Connection should be closed!" );
    } catch ( final IOException e ) {
      // Expected exception
    }
  }




  private void makeRequest() throws MalformedURLException, IOException {
    final HttpURLConnection connection = (HttpURLConnection)new URL( "http://localhost:8092/" ).openConnection();
    // Keep-alive seems to be on by default, but just in case that changes.
    connection.addRequestProperty( "Connection", "keep-alive" );
    final InputStream in = connection.getInputStream();
    while ( in.available() > 0 ) {
      in.read();
    }
    in.close();
  }

}
