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

import coyote.commons.network.http.IHTTPSession;
import coyote.commons.network.http.auth.AuthProvider;
import coyote.dataframe.DataFrame;


/**
 * This is the default authentication and Authorization component for the HTTP service.
 */
public class DefaultAuthProvider implements AuthProvider {
  public static final String AUTH_SECTION = "Auth";




  /**
   * @param cfg
   */
  public DefaultAuthProvider( DataFrame cfg ) {
    if ( cfg != null ) {
      System.out.println( cfg.toString() );
    }
  }




  /**
   * @see coyote.commons.network.http.auth.AuthProvider#isValidConnection(coyote.commons.network.http.IHTTPSession)
   */
  @Override
  public boolean isValidConnection( IHTTPSession session ) {
    return true;
  }




  /**
   * @see coyote.commons.network.http.auth.AuthProvider#isAuthenticated(coyote.commons.network.http.IHTTPSession)
   */
  @Override
  public boolean isAuthenticated( IHTTPSession session ) {

    // if no authentication header, we can send a request for client to send one, most browsers will then pop-up a form 
    //session.getResponseHeaders().put( HTTP.HDR_WWW_AUTHENTICATE, HTTP.BASIC+" realm=\"Batch Manager\"" );

    return true;
  }




  /**
   * @see coyote.commons.network.http.auth.AuthProvider#isAuthorized(coyote.commons.network.http.IHTTPSession, java.lang.String)
   */
  @Override
  public boolean isAuthorized( IHTTPSession session, String groups ) {
    return true;
  }

}
