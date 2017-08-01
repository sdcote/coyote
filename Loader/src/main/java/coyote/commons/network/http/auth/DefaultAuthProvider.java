/*
 * Copyright (c) 2017 Stephan D. Cote' - All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the MIT License which accompanies this distribution, and is
 * available at http://creativecommons.org/licenses/MIT/
 */

package coyote.commons.network.http.auth;

import coyote.commons.network.http.IHTTPSession;


/**
 * The default Authentication and authorization provider for the server.
 *
 * This denies access to everything.
 */
public class DefaultAuthProvider implements AuthProvider {

  /**
   * @see coyote.commons.network.http.auth.AuthProvider#isAuthenticated(coyote.commons.network.http.IHTTPSession)
   */
  @Override
  public boolean isAuthenticated(final IHTTPSession session) {
    return false;
  }




  /**
   * @see coyote.commons.network.http.auth.AuthProvider#isAuthorized(coyote.commons.network.http.IHTTPSession, java.lang.String)
   */
  @Override
  public boolean isAuthorized(final IHTTPSession session, final String groups) {
    return false;
  }




  /**
   * @see coyote.commons.network.http.auth.AuthProvider#isSecureConnection(coyote.commons.network.http.IHTTPSession)
   */
  @Override
  public boolean isSecureConnection(final IHTTPSession session) {
    return false;
  }

}
