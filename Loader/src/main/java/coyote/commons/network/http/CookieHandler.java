/*
 * Copyright (c) 2003 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 */

package coyote.commons.network.http;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Provides basic support for cookies.
 */
public class CookieHandler implements Iterable<String> {

  private final HashMap<String, String> cookies = new HashMap<String, String>();

  private final ArrayList<Cookie> queue = new ArrayList<Cookie>();




  public CookieHandler( final Map<String, String> httpHeaders ) {
    final String raw = httpHeaders.get( "cookie" );
    if ( raw != null ) {
      final String[] tokens = raw.split( ";" );
      for ( final String token : tokens ) {
        final String[] data = token.trim().split( "=" );
        if ( data.length == 2 ) {
          cookies.put( data[0], data[1] );
        }
      }
    }
  }




  /**
   * Set a cookie with an expiration date from a month ago, effectively
   * deleting it on the client side.
   * 
   * @param name The cookie name.
   */
  public void delete( final String name ) {
    set( name, "-delete-", -30 );
  }




  @Override
  public Iterator<String> iterator() {
    return cookies.keySet().iterator();
  }




  /**
   * Read a cookie from the HTTP Headers.
   * 
   * @param name The cookie's name.
   * 
   * @return The cookie's value if it exists, null otherwise.
   */
  public String read( final String name ) {
    return cookies.get( name );
  }




  public void set( final Cookie cookie ) {
    queue.add( cookie );
  }




  /**
   * Sets a cookie.
   * 
   * @param name The cookie's name.
   * @param value The cookie's value.
   * @param expires How many days until the cookie expires.
   */
  public void set( final String name, final String value, final int expires ) {
    queue.add( new Cookie( name, value, Cookie.getHTTPTime( expires ) ) );
  }




  /**
   * Internally used by the webserver to add all queued cookies into the
   * Response's HTTP Headers.
   * 
   * @param response The Response object to which headers the queued cookies 
   *        will be added.
   */
  public void unloadQueue( final Response response ) {
    for ( final Cookie cookie : queue ) {
      response.addHeader( "Set-Cookie", cookie.getHTTPHeader() );
    }
  }
  
}