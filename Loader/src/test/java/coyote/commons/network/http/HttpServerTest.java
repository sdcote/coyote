package coyote.commons.network.http;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;


public class HttpServerTest {
private static final int PORT = 7428;
  public static class TestServer extends HTTPD {

    public Response response = Response.createFixedLengthResponse( "" );
    public String uri;
    public Method method;
    public Map<String, String> header;
    public Map<String, String> parms;
    public Body body;
    public Map<String, List<String>> decodedParamters;
    public Map<String, List<String>> decodedParamtersFromParameter;
    public String queryParameterString;




    public TestServer() {
      super( PORT );
    }




    public TestServer( final int port ) {
      super( port );
    }




    public HTTPSession createSession( final CacheManager tempFileManager, final InputStream inputStream, final OutputStream outputStream ) {
      return new HTTPSession( this, tempFileManager, inputStream, outputStream, false );
    }




    public HTTPSession createSession( final CacheManager tempFileManager, final InputStream inputStream, final OutputStream outputStream, final InetAddress inetAddress ) {
      return new HTTPSession( this, tempFileManager, inputStream, outputStream, inetAddress, myPort, false );
    }




    @Override
    public Response serve( final IHTTPSession session ) {
      uri = session.getUri();
      method = session.getMethod();
      header = session.getRequestHeaders();
      parms = session.getParms();
      try {
        body = session.parseBody( );
      } catch ( final Exception e ) {
        e.printStackTrace();
      }
      queryParameterString = session.getQueryParameterString();
      decodedParamtersFromParameter = decodeParameters( queryParameterString );
      decodedParamters = decodeParameters( session.getQueryParameterString() );
      return response;
    }
  }

  public static class TestTempFileManager extends DefaultCacheManager {

    public void _clear() {
      super.clear();
    }




    @Override
    public void clear() {
      // ignore
    }
  }

  public static final String URI = "http://www.myserver.org/pub/WWW/someFile.html";

  protected TestServer testServer;

  protected TestTempFileManager tempFileManager;




  protected void assertLinesOfText( final String[] expected, final List<String> lines ) {
    // assertEquals(expected.length, lines.size());
    for ( int i = 0; i < expected.length; i++ ) {
      final String line = lines.get( i );
      assertTrue( "Output line " + i + " doesn't match expectation.\n" + "  Output: " + line + "\n" + "Expected: " + expected[i], line.matches( expected[i] ) );
    }
  }




  protected void assertResponse( final ByteArrayOutputStream outputStream, final String[] expected ) throws IOException {
    final List<String> lines = getOutputLines( outputStream );
    assertLinesOfText( expected, lines );
  }




  protected List<String> getOutputLines( final ByteArrayOutputStream outputStream ) throws IOException {
    final BufferedReader reader = new BufferedReader( new StringReader( outputStream.toString() ) );
    return readLinesFromFile( reader );
  }




  protected ByteArrayOutputStream invokeServer( final String request ) {
    final ByteArrayInputStream inputStream = new ByteArrayInputStream( request.getBytes() );
    final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    final HTTPSession session = testServer.createSession( tempFileManager, inputStream, outputStream );
    try {
      session.execute();
    } catch ( final IOException e ) {
      fail( "" + e );
      e.printStackTrace();
    }
    return outputStream;
  }




  protected List<String> readLinesFromFile( final BufferedReader reader ) throws IOException {
    final List<String> lines = new ArrayList<String>();
    String line = "";
    while ( line != null ) {
      line = reader.readLine();
      if ( line != null ) {
        lines.add( line.trim() );
      }
    }
    return lines;
  }




  @Before
  public void setUp() throws Exception {
    testServer = new TestServer();
    tempFileManager = new TestTempFileManager();
  }




  @After
  public void tearDown() {
    tempFileManager._clear();
  }




  @Ignore
  public void testMultipartFormData() throws IOException {
    final int testPort = 4589;
    HTTPD server = null;

    try {
      server = new HTTPD( testPort ) {





        @Override
        public Response serve( final IHTTPSession session ) {
          final StringBuilder responseMsg = new StringBuilder();

          try {
            Body body = session.parseBody();
            for ( final String key : body.keySet() ) {
              responseMsg.append( key );
            }
          } catch ( final Exception e ) {
            responseMsg.append( e.getMessage() );
          }

          return Response.createFixedLengthResponse( responseMsg.toString() );
        }
      };
      server.start( HTTPD.SOCKET_READ_TIMEOUT, false );

      final HttpClient httpclient = new DefaultHttpClient();
      final HttpPost httppost = new HttpPost( "http://localhost:" + testPort );

      final String fileName = "file-upload-test.htm";
      final FileBody bin = new FileBody( new File( getClass().getClassLoader().getResource( fileName ).getFile() ) );
      final StringBody comment = new StringBody( "Filename: " + fileName );

      final MultipartEntity reqEntity = new MultipartEntity();
      reqEntity.addPart( "bin", bin );
      reqEntity.addPart( "comment", comment );
      httppost.setEntity( reqEntity );

      final HttpResponse response = httpclient.execute( httppost );
      final HttpEntity entity = response.getEntity();

      if ( entity != null ) {
        final InputStream instream = entity.getContent();
        final BufferedReader reader = new BufferedReader( new InputStreamReader( instream, "UTF-8" ) );
        final String line = reader.readLine();
        assertNotNull( line, "Invalid server reponse" );
        assertEquals( "Server failed multi-part data parse" + line, "bincomment", line );
        reader.close();
        instream.close();
      }
    }
    finally {
      if ( server != null ) {
        server.stop();
      }
    }
  }




  @Test
  public void testServerExists() {
    assertNotNull( testServer );
  }




  @Ignore
  public void testTempFileInterface() throws IOException {
    final int testPort = 4589;
    final HTTPD server = new HTTPD( testPort ) {


      @Override
      public Response serve( final IHTTPSession session ) {
        String responseMsg = "pass";

        try {
          Body body = session.parseBody( );
          for ( final String key : body.keySet() ) {
            Object obj = body.get( key );
            System.out.println( "body contains "+body.size()+" entities" );
//            if ( !( new File( body.get( key ) ) ).exists() ) {
//              responseMsg = "fail";
//            }
          }
        } catch ( final Exception e ) {
          responseMsg = e.getMessage();
        }

        return Response.createFixedLengthResponse( responseMsg.toString() );
      }
    };
    server.start( HTTPD.SOCKET_READ_TIMEOUT, false );

    final HttpClient httpclient = new DefaultHttpClient();
    final HttpPost httppost = new HttpPost( "http://localhost:" + testPort );

    final String fileName = "file-upload-test.htm";
    final FileBody bin = new FileBody( new File( getClass().getClassLoader().getResource( fileName ).getFile() ) );
    final StringBody comment = new StringBody( "Filename: " + fileName );

    final MultipartEntity reqEntity = new MultipartEntity();
    reqEntity.addPart( "bin", bin );
    reqEntity.addPart( "comment", comment );
    httppost.setEntity( reqEntity );

    final HttpResponse response = httpclient.execute( httppost );
    final HttpEntity entity = response.getEntity();

    if ( entity != null ) {
      final InputStream instream = entity.getContent();
      final BufferedReader reader = new BufferedReader( new InputStreamReader( instream, "UTF-8" ) );
      final String line = reader.readLine();
      assertNotNull( line, "Invalid server reponse" );
      assertEquals( "Server file check failed: " + line, "pass", line );
      reader.close();
      instream.close();
    } else {
      fail( "No server response" );
    }
    server.stop();
  }

}
