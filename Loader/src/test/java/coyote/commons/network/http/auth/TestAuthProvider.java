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
package coyote.commons.network.http.auth;

import coyote.commons.network.http.IHTTPSession;


/**
 * 
 */
public class TestAuthProvider implements AuthProvider {

  private volatile boolean allowConnections = true;
  private volatile boolean allowAuthentications = true;
  private volatile boolean allowAuthorizations = true;




  /**
   * @see coyote.commons.network.http.auth.AuthProvider#isSecureConnection(coyote.commons.network.http.IHTTPSession)
   */
  @Override
  public boolean isSecureConnection( IHTTPSession session ) {

    return allowConnections;
  }




  /**
   * @see coyote.commons.network.http.auth.AuthProvider#isAuthenticated(coyote.commons.network.http.IHTTPSession)
   */
  @Override
  public boolean isAuthenticated( IHTTPSession session ) {

    return allowAuthentications;
  }




  /**
   * @see coyote.commons.network.http.auth.AuthProvider#isAuthorized(coyote.commons.network.http.IHTTPSession, java.lang.String)
   */
  @Override
  public boolean isAuthorized( IHTTPSession session, String groups ) {

    return allowAuthorizations;
  }




  public void rejectAllConnections() {
    allowConnections = false;
  }




  public void allowAllConnections() {
    allowConnections = true;
  }




  public void rejectAllAuthentications() {
    allowAuthentications = false;
  }




  public void allowAllAuthentications() {
    allowAuthentications = true;
  }




  public void rejectAllAuthorizations() {
    allowAuthorizations = false;
  }




  public void allowAllAuthorizations() {
    allowAuthorizations = true;
  }

}
