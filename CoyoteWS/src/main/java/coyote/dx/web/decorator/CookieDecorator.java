/*
 * Copyright (c) 2017 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 *
 * Contributors:
 *   Stephan D. Cote 
 *      - Initial concept and implementation
 */
package coyote.dx.web.decorator;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpMessage;

import coyote.commons.StringUtil;


/**
 * Add a set of cookies to the request.
 * 
 * <p>Expects to be called thusly:<pre>
 * CookieDecorator decorator = new CookieDecorator();
 * decorator.add( "ROUTING_TOKEN", "eyJraWQiOiJ5zcmk" );
 * resource.addRequestDecorator( decorator );
 * </pre>
 */
public class CookieDecorator extends AbstractDecorator implements RequestDecorator {
  private List<Cookie> cookies = new ArrayList<Cookie>();
  protected static final String COOKIE = "Cookie";
  private String headerData = "";

  private class Cookie {
    private String key;
    private String value;




    private Cookie( String key, String value ) {
      this.key = key;
      this.value = value;
    }




    private String getKey() {
      return key;
    }




    private String getValue() {
      return value;
    }

  }




  private void calculateHeaderData() {
    StringBuffer b = new StringBuffer();

    int x = 1;
    for ( Cookie cookie : cookies ) {
      b.append( cookie.getKey() );
      b.append( "=" );
      b.append( cookie.getValue() );
      if ( x++ < cookies.size() ) {
        b.append( "; " );
      }
    }
    headerData = b.toString();
  }




  /**
   * @see coyote.dx.web.decorator.RequestDecorator#process(org.apache.http.HttpMessage)
   */
  @Override
  public void process( HttpMessage request ) {
    if ( StringUtil.isBlank( headerData ) ) {
      calculateHeaderData();
    }

    // Set the header if data exists
    if ( StringUtil.isNotBlank( headerData ) ) {
      request.setHeader( COOKIE, headerData );
    }
  }




  /**
   * Add the given name value pair as a cookie.
   * 
   * @param name the name of the cookie
   * @param value the value of the cookie
   */
  public void add( String name, String value ) {
    if ( StringUtil.isNotBlank( name ) ) {
      cookies.add( new Cookie( name, ( value == null ) ? "" : value ) );
    }
  }

  
}
