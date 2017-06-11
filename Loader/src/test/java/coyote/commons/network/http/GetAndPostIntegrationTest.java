package coyote.commons.network.http;

import static org.junit.Assert.assertEquals;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.junit.Ignore;
import org.junit.Test;

import coyote.commons.NetUtil;
import coyote.commons.network.MimeType;


public class GetAndPostIntegrationTest extends IntegrationTestBase<GetAndPostIntegrationTest.TestServer> {
private static final int PORT = NetUtil.getNextAvailablePort( 7428 );

  public static class TestServer extends HTTPD {

    public String response;




    public TestServer() {
      super( PORT );
    }




    @Override
    public Response serve( final IHTTPSession session ) {
      final StringBuilder sb = new StringBuilder( String.valueOf( session.getMethod() ) + ':' + response );

      if ( session.getParms().size() > 1 ) {
        session.getParms().remove( "Httpd.QUERY_STRING" );
        sb.append( "-params=" ).append( session.getParms().size() );
        final List<String> p = new ArrayList<String>( session.getParms().keySet() );
        Collections.sort( p );
        for ( final String k : p ) {
          sb.append( ';' ).append( k ).append( '=' ).append( session.getParms().get( k ) );
        }
      }
      if ( "/encodingtest".equals( session.getUri() ) ) {
        return Response.createFixedLengthResponse( Status.OK, MimeType.HTML.getType(), "<html><head><title>TestÃ© Ã§a</title></head><body>TestÃ© Ã§a</body></html>" );
      } else if ( "/chin".equals( session.getUri() ) ) {
        return Response.createFixedLengthResponse( Status.OK, "application/octet-stream", sb.toString() );
      } else {
        return Response.createFixedLengthResponse( sb.toString() );
      }
    }
  }




  @Override
  public TestServer createTestServer() {
    return new TestServer();
  }




  @Test
  public void testGetRequestWithParameters() throws Exception {
    testServer.response = "testGetRequestWithParameters";

    final HttpGet httpget = new HttpGet( "http://localhost:"+PORT+"/?age=120&gender=Male" );
    final ResponseHandler<String> responseHandler = new BasicResponseHandler();
    final String responseBody = httpclient.execute( httpget, responseHandler );

    assertEquals( "GET:testGetRequestWithParameters-params=2;age=120;gender=Male", responseBody );
  }




  @Test
  public void testPostRequestWithEncodedParameters() throws Exception {
    testServer.response = "testPostRequestWithEncodedParameters";

    final HttpPost httppost = new HttpPost( "http://localhost:"+PORT+"/encodingtest" );
    final HttpResponse response = httpclient.execute( httppost );

    final HttpEntity entity = response.getEntity();
    final String responseBody = EntityUtils.toString( entity );

    assertEquals( "<html><head><title>TestÃ© Ã§a</title></head><body>TestÃ© Ã§a</body></html>", responseBody );
  }




  @Ignore
  public void testPostRequestWithFormEncodedParameters() throws Exception {
    testServer.response = "testPostRequestWithFormEncodedParameters";

    final HttpPost httppost = new HttpPost( "http://localhost:"+PORT+"/" );
    final List<NameValuePair> postParameters = new ArrayList<NameValuePair>();
    postParameters.add( new BasicNameValuePair( "age", "120" ) );
    postParameters.add( new BasicNameValuePair( "gender", "Male" ) );
    httppost.setEntity( new UrlEncodedFormEntity( postParameters ) );

    final ResponseHandler<String> responseHandler = new BasicResponseHandler();
    final String responseBody = httpclient.execute( httppost, responseHandler );

    assertEquals( "POST:testPostRequestWithFormEncodedParameters-params=2;age=120;gender=Male", responseBody );
  }




  @Ignore
  public void testPostRequestWithMultipartEncodedParameters() throws Exception {
    testServer.response = "testPostRequestWithMultipartEncodedParameters";

    final HttpPost httppost = new HttpPost( "http://localhost:"+PORT+"/" );
    final MultipartEntity reqEntity = new MultipartEntity( HttpMultipartMode.BROWSER_COMPATIBLE );
    reqEntity.addPart( "age", new StringBody( "120" ) );
    reqEntity.addPart( "gender", new StringBody( "Male" ) );
    httppost.setEntity( reqEntity );

    final ResponseHandler<String> responseHandler = new BasicResponseHandler();
    final String responseBody = httpclient.execute( httppost, responseHandler );

    assertEquals( "POST:testPostRequestWithMultipartEncodedParameters-params=2;age=120;gender=Male", responseBody );
  }




  @Ignore
  public void testPostRequestWithMultipartExtremEncodedParameters() throws Exception {
    testServer.response = "testPostRequestWithMultipartEncodedParameters";

    final HttpPost httppost = new HttpPost( "http://localhost:"+PORT+"/chin" );
    final MultipartEntity reqEntity = new MultipartEntity( HttpMultipartMode.BROWSER_COMPATIBLE, "sfsadfasdf", Charset.forName( "UTF-8" ) );
    reqEntity.addPart( "specialString", new StringBody( "æ‹–æ‹‰å›¾ç‰‡åˆ°æµ�è§ˆå™¨ï¼Œå�¯ä»¥å®žçŽ°é¢„è§ˆåŠŸèƒ½", "text/plain", Charset.forName( "UTF-8" ) ) );
    reqEntity.addPart( "gender", new StringBody( "å›¾ç‰‡å��ç§°", Charset.forName( "UTF-8" ) ) {

      @Override
      public String getFilename() {
        return "å›¾ç‰‡å��ç§°";
      }
    } );
    httppost.setEntity( reqEntity );
    final HttpResponse response = httpclient.execute( httppost );

    final HttpEntity entity = response.getEntity();
    final String responseBody = EntityUtils.toString( entity, "UTF-8" );

    assertEquals( "POST:testPostRequestWithMultipartEncodedParameters-params=2;gender=å›¾ç‰‡å��ç§°;specialString=æ‹–æ‹‰å›¾ç‰‡åˆ°æµ�è§ˆå™¨ï¼Œå�¯ä»¥å®žçŽ°é¢„è§ˆåŠŸèƒ½", responseBody );
  }




  @Test
  public void testPostWithNoParameters() throws Exception {
    testServer.response = "testPostWithNoParameters";

    final HttpPost httppost = new HttpPost( "http://localhost:"+PORT+"/" );
    final ResponseHandler<String> responseHandler = new BasicResponseHandler();
    final String responseBody = httpclient.execute( httppost, responseHandler );

    assertEquals( "POST:testPostWithNoParameters", responseBody );
  }




  @Test
  public void testSimpleGetRequest() throws Exception {
    testServer.response = "testSimpleGetRequest";

    final HttpGet httpget = new HttpGet( "http://localhost:"+PORT+"/" );
    final ResponseHandler<String> responseHandler = new BasicResponseHandler();
    final String responseBody = httpclient.execute( httpget, responseHandler );

    assertEquals( "GET:testSimpleGetRequest", responseBody );
  }
}
