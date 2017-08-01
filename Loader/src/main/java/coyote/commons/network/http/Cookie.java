/*
 * Copyright (c) 2003 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 */

package coyote.commons.network.http;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;


public class Cookie {
  private static final String COOKIE_FORMAT = "%s=%s; Expires=%s";
  private static final String DOMAIN_FORMAT = "; domain=%s";
  private static final String SECURE = "; secure";
  private static final String HTTPONLY = "; HttpOnly";

  private boolean secure = false;

  private boolean httpOnly = false;
  private String domain = null;
  private final String name;
  private final String expiry;
  private final String value;




  public Cookie( final String name, final String value ) {
    this( name, value, 30 );
  }




  public Cookie( final String name, final String value, final int numDays ) {
    this.name = name;
    this.value = value;
    this.expiry = getHTTPTime( numDays );
  }




  public Cookie( final String name, final String value, final String expires ) {
    this.name = name;
    this.value = value;
    this.expiry = expires;
  }




  /**
   * @return the secure
   */
  public boolean isSecure() {
    return secure;
  }




  /**
   * @param flag true to make the cookie secure
   */
  public Cookie setSecure( boolean flag ) {
    secure = flag;
    return this;
  }




  /**
   * @return if this is an http only cookie or not.
   */
  public boolean isHttpOnly() {
    return httpOnly;
  }




  /**
   * @param flag true to make the cookie as HTTP only
   */
  public Cookie setHttpOnly( boolean flag ) {
    httpOnly = flag;
    return this;
  }




  /**
   * @return the domain of this cookie
   */
  public String getDomain() {
    return domain;
  }




  /**
   * @param domain the domain to set for this cookie
   */
  public Cookie setDomain( String domain ) {
    this.domain = domain;
    return this;
  }




  /**
   * @return the expiration of this cookie
   */
  public String getExpiry() {
    return expiry;
  }




  public String getHTTPHeader() {
    StringBuffer b = new StringBuffer();
    b.append( String.format( COOKIE_FORMAT, name, value, expiry ) );
    if ( domain != null && domain.trim().length() > 0 )
      b.append( String.format( DOMAIN_FORMAT, domain ) );
    if ( secure )
      b.append( SECURE );
    if ( httpOnly )
      b.append( HTTPONLY );
    return b.toString();
  }




  public static String getHTTPTime( final int days ) {
    final Calendar calendar = Calendar.getInstance();
    final SimpleDateFormat dateFormat = new SimpleDateFormat( "EEE, dd MMM yyyy HH:mm:ss z" );
    dateFormat.setTimeZone( TimeZone.getTimeZone( "GMT" ) );
    calendar.add( Calendar.DAY_OF_MONTH, days );
    return dateFormat.format( calendar.getTime() );
  }

}