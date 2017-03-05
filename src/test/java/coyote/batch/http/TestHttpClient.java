/*
 * Copyright (c) 2017 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 *
 * Contributors:
 *   Stephan D. Cote 
 *      - Initial concept and initial implementation
 */
package coyote.batch.http;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

import coyote.commons.ByteUtil;
import coyote.commons.StringUtil;


/**
 * Various client methods to make HTTP requests.
 */
public class TestHttpClient {
  private static final String GET = "GET";
  private static final String POST = "POST";

  protected static final String BASIC = "Basic";




  /**
   * Send a GET request to the given URL.
   * 
   * @param url the URL to the resource to GET.
   * 
   * @return the results of the exchange.
   */
  public static TestResponse sendGet( String url ) {
    return sendRequest( url, GET, null, null );
  }




  /**
   * Send a GET request to the given URL.
   * 
   * @param url the URL to the resource to GET.
   * @parem headers a map of headers to attach to the request
   * 
   * @return the results of the exchange.
   */
  public static TestResponse sendGet( String url, Map<String, String> headers ) {
    return sendRequest( url, GET, headers, null );
  }




  /**
   * Send a POST request to the given URL.
   * 
   * @param url the URL to the resource to POST.
   * 
   * @return the results of the exchange.
   */
  public static TestResponse sendPost( String url ) {
    return sendRequest( url, POST, null, null );
  }




  /**
   * Send a POST request to the given URL.
   * 
   * @param url the URL to the resource to POST.
   * @param body the body of the request message.
   * 
   * @return the results of the exchange.
   */
  public static TestResponse sendPost( String url, String body ) {
    return sendRequest( url, POST, null, body );
  }




  /**
   * Send a POST request to the given URL.
   * 
   * @param url the URL to the resource to POST.
   * @parem headers a map of headers to attach to the request.
   * @param body the body of the request message.
   * 
   * @return the results of the exchange.
   */
  public static TestResponse sendPost( String url, Map<String, String> headers, String body ) {
    return sendRequest( url, POST, headers, body );
  }




  /**
   * Send a request to the given URL using the given HTTP method and headers..
   * 
   * @param url the URL to the resource to GET.
   * @param method the HTTP method to request.
   * @parem headers a map of headers to attach to the request
   * 
   * @return the results of the exchange.
   */
  public static TestResponse sendRequest( String url, String method, Map<String, String> headers, String body ) {
    TestResponse testResponse = new TestResponse( url );

    // TODO: Support Headers

    // TODO: Support body

    try {
      URL obj = new URL( url );
      HttpURLConnection con = (HttpURLConnection)obj.openConnection();
      con.setRequestMethod( method );

      int responseCode = con.getResponseCode();
      testResponse.setStatus( responseCode );
      if ( responseCode < 300 ) {
        BufferedReader in = new BufferedReader( new InputStreamReader( con.getInputStream() ) );
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ( ( inputLine = in.readLine() ) != null ) {
          response.append( inputLine );
        }
        in.close();

        testResponse.setData( response.toString() );
        testResponse.setComplete( true );
      }
    } catch ( Exception e ) {
      testResponse.setException( e );
    }

    return testResponse;
  }




  /**
   * Create an Authorization header for a username and password
   */
  public static String calculateHeaderData( String username, String password ) {
    if ( StringUtil.isNotBlank( username ) || StringUtil.isNotBlank( password ) ) {
      StringBuffer b = new StringBuffer();

      if ( StringUtil.isNotBlank( username ) ) {
        b.append( username );
      }
      b.append( ":" );

      if ( StringUtil.isNotBlank( password ) ) {
        b.append( password );
      }

      return BASIC + " " + ByteUtil.toBase64( StringUtil.getBytes( b.toString() ) );
    }
    return null;
  }

}
