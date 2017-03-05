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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import coyote.commons.network.http.CookieHandler;
import coyote.commons.network.http.IHTTPSession;
import coyote.commons.network.http.Method;
import coyote.commons.network.http.ResponseException;


/**
 * 
 */
public class MockSession implements IHTTPSession {
  private static final List<String> EMPTY_LIST = new ArrayList<String>( 0 );;
  private Map<String, String> requestHeaders;
  private Map<String, String> responseHeaders;
  private String username = null;
  private List<String> usergroups = EMPTY_LIST;




  public MockSession() {
    requestHeaders = new HashMap<String, String>();
    responseHeaders = new HashMap<String, String>();
  }




  /**
   * @see coyote.commons.network.http.IHTTPSession#execute()
   */
  @Override
  public void execute() throws IOException {}




  /**
   * @see coyote.commons.network.http.IHTTPSession#getCookies()
   */
  @Override
  public CookieHandler getCookies() {
    return null;
  }




  /**
   * @see coyote.commons.network.http.IHTTPSession#getRequestHeaders()
   */
  @Override
  public final Map<String, String> getRequestHeaders() {
    return requestHeaders;
  }




  /**
   * @see coyote.commons.network.http.IHTTPSession#getResponseHeaders()
   */
  @Override
  public Map<String, String> getResponseHeaders() {
    return responseHeaders;
  }




  /**
   * @see coyote.commons.network.http.IHTTPSession#getInputStream()
   */
  @Override
  public InputStream getInputStream() {
    return null;
  }




  /**
   * @see coyote.commons.network.http.IHTTPSession#getMethod()
   */
  @Override
  public Method getMethod() {
    return null;
  }




  /**
   * @see coyote.commons.network.http.IHTTPSession#getParms()
   */
  @Override
  public Map<String, String> getParms() {
    return null;
  }




  /**
   * @see coyote.commons.network.http.IHTTPSession#getQueryParameterString()
   */
  @Override
  public String getQueryParameterString() {
    return null;
  }




  /**
   * @see coyote.commons.network.http.IHTTPSession#getRemoteHostName()
   */
  @Override
  public String getRemoteHostName() {
    return null;
  }




  /**
   * @see coyote.commons.network.http.IHTTPSession#getRemoteIpAddress()
   */
  @Override
  public String getRemoteIpAddress() {
    return null;
  }




  /**
   * @see coyote.commons.network.http.IHTTPSession#getRemoteIpPort()
   */
  @Override
  public int getRemoteIpPort() {
    return 0;
  }




  /**
   * @see coyote.commons.network.http.IHTTPSession#getUri()
   */
  @Override
  public String getUri() {
    return null;
  }




  /**
   * @see coyote.commons.network.http.IHTTPSession#parseBody(java.util.Map)
   */
  @Override
  public void parseBody( Map<String, String> files ) throws IOException, ResponseException {}




  /**
   * @see coyote.commons.network.http.IHTTPSession#setUserName(java.lang.String)
   */
  @Override
  public void setUserName( String user ) {
    username = user;
  }




  /**
   * @see coyote.commons.network.http.IHTTPSession#getUserName()
   */
  @Override
  public String getUserName() {
    return username;
  }




  /**
   * @see coyote.commons.network.http.IHTTPSession#setUserGroups(java.util.List)
   */
  @Override
  public void setUserGroups( List<String> groups ) {
    if ( groups != null ) {
      usergroups = groups;
    } else {
      usergroups = EMPTY_LIST;
    }
  }




  /**
   * @see coyote.commons.network.http.IHTTPSession#getUserGroups()
   */
  @Override
  public List<String> getUserGroups() {
    return usergroups;
  }




  /**
   * @param name header name
   * @param value header value
   */
  public void addRequestHeader( String name, String value ) {
    requestHeaders.put( name, value );
  }

}
