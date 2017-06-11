package coyote.commons.network.http;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.junit.Test;

import coyote.commons.NetUtil;


public class CookieIntegrationTest extends IntegrationTestBase<CookieIntegrationTest.CookieTestServer> {
  private static final int PORT = NetUtil.getNextAvailablePort( 7428 );

  public static class CookieTestServer extends HTTPD {

    List<Cookie> cookiesReceived = new ArrayList<Cookie>();

    List<Cookie> cookiesToSend = new ArrayList<Cookie>();




    public CookieTestServer() {
      super( PORT );
    }




    @Override
    public Response serve( final IHTTPSession session ) {
      final CookieHandler cookies = session.getCookies();
      for ( final String cookieName : cookies ) {
        cookiesReceived.add( new Cookie( cookieName, cookies.read( cookieName ) ) );
      }
      for ( final Cookie c : cookiesToSend ) {
        cookies.set( c );
      }
      return Response.createFixedLengthResponse( "Cookies!" );
    }
  }




  @Override
  public CookieTestServer createTestServer() {
    return new CookieTestServer();
  }




  @Test
  public void testCookieSentBackToClient() throws Exception {
    testServer.cookiesToSend.add( new Cookie( "name", "value", 90 ) );

    final HttpGet httpget = new HttpGet( "http://localhost:"+PORT+"/" );
    final ResponseHandler<String> responseHandler = new BasicResponseHandler();
    httpclient.execute( httpget, responseHandler );

    assertEquals( 1, cookiestore.getCookies().size() );
    assertEquals( "name", cookiestore.getCookies().get( 0 ).getName() );
    assertEquals( "value", cookiestore.getCookies().get( 0 ).getValue() );
    cookiestore.clear();
  }




  @Test
  public void testNoCookies() throws Exception {
    final HttpGet httpget = new HttpGet( "http://localhost:"+PORT+"/" );
    final ResponseHandler<String> responseHandler = new BasicResponseHandler();
    httpclient.execute( httpget, responseHandler );
    assertEquals( 0, cookiestore.getCookies().size() );
    cookiestore.clear();
  }




  @Test
  public void testServerReceivesCookiesSentFromClient() throws Exception {
    final BasicClientCookie clientCookie = new BasicClientCookie( "name", "value" );
    final Calendar calendar = Calendar.getInstance();
    calendar.add( Calendar.DAY_OF_YEAR, 100 );
    clientCookie.setExpiryDate( calendar.getTime() );
    clientCookie.setDomain( "localhost" );
    cookiestore.addCookie( clientCookie );
    final HttpGet httpget = new HttpGet( "http://localhost:"+PORT+"/" );
    final ResponseHandler<String> responseHandler = new BasicResponseHandler();
    httpclient.execute( httpget, responseHandler );

    assertEquals( 1, testServer.cookiesReceived.size() );
    assertTrue( testServer.cookiesReceived.get( 0 ).getHTTPHeader().contains( "name=value" ) );
    cookiestore.clear();
  }
}
