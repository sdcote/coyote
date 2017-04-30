/*
 * Copyright (c) 2015 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 *
 * Contributors:
 *   Stephan D. Cote 
 *      - Initial concept and implementation
 */
package coyote.dx.web;

/**
 * Simply a wrapper for HTTP proxy data.
 */
public class Proxy {

  /** System property which specifies the user name for the proxy server */
  protected static final String PROXY_USER = "http.proxyUser";

  /** System property which specifies the user password for the proxy server */
  protected static final String PROXY_PASS = "http.proxyPassword";

  /** System property which specifies the proxy server host name */
  protected static final String PROXY_HOST = "http.proxyHost";

  /** System property which specifies the port on which proxy server listens */
  protected static final String PROXY_PORT = "http.proxyPort";

  /** System property which specifies the NTLM domain for proxy user auth */
  protected static final String PROXY_DOMAIN = "http.proxyDomain";

  // Proxy data
  private String proxyHost = null;
  private int proxyPort = 0;
  private String proxyDomain = null;
  private String proxyUsername = null;
  private String proxyPassword = null;




  public Proxy() {
    proxyHost = System.getProperty( PROXY_HOST );
    proxyUsername = System.getProperty( PROXY_USER );
    proxyPassword = System.getProperty( PROXY_PASS );
    proxyDomain = System.getProperty( PROXY_DOMAIN );

    try {
      proxyPort = Integer.parseInt( System.getProperty( PROXY_PORT ) );
    } catch ( NumberFormatException e ) {
      proxyPort = 0;
    }

  }




  /**
   * @return the name of the proxy host
   */
  public String getHost() {
    return proxyHost;
  }




  /**
   * @param hostname the name of the proxy host to set
   */
  public void setHost( String hostname ) {
    proxyHost = hostname;
  }




  /**
   * @return the proxy port
   */
  public int getPort() {
    return proxyPort;
  }




  /**
   * @param port the proxy port to set
   */
  public void setPort( int port ) {
    proxyPort = port;
  }




  /**
   * @return the domain of the username (used primarily in NTLM proxies)
   */
  public String getDomain() {
    return proxyDomain;
  }




  /**
   * @param domain the domain of the username (used primarily in NTLM proxies)
   */
  public void setDomain( String domain ) {
    this.proxyDomain = domain;
  }




  /**
   * @return the proxy username
   */
  public String getUsername() {
    return proxyUsername;
  }




  /**
   * @param username the proxy username to set
   */
  public void setUsername( String username ) {
    proxyUsername = username;
  }




  /**
   * @return the proxy password
   */
  public String getPassword() {
    return proxyPassword;
  }




  /**
   * @param passwd the proxy password to set
   */
  public void setPassword( String passwd ) {
    this.proxyPassword = passwd;
  }

}
