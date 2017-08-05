/*
 * Copyright (c) 2016 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 *
 * Contributors:
 *   Stephan D. Cote 
 *      - Initial concept and implementation
 */
package coyote.dx.web.auth;

import coyote.dataframe.DataFrame;
import coyote.dx.web.Resource;
import coyote.loader.cfg.ConfigurationException;


/**
 * A do nothing implementation of the Authenticator to reduce pointer 
 * exceptions.
 */
public class NullAuthenticator implements Authenticator {

  /**
   * @see coyote.dx.web.auth.Authenticator#isAuthenticatingPreemptively()
   */
  @Override
  public boolean isAuthenticatingPreemptively() {
    return false;
  }




  /**
   * @see coyote.dx.web.auth.Authenticator#setPreemptiveAuthentication(boolean)
   */
  @Override
  public void setPreemptiveAuthentication( boolean preemptive ) {
    // no-op method
  }




  /**
   * @see coyote.dx.web.auth.Authenticator#getPassword()
   */
  @Override
  public String getPassword() {
    return null;
  }




  /**
   * @see coyote.dx.web.auth.Authenticator#getUsername()
   */
  @Override
  public String getUsername() {
    return null;
  }




  /**
   * @see coyote.dx.web.auth.Authenticator#setUsername(java.lang.String)
   */
  @Override
  public void setUsername( String username ) {
    // no-op method
  }




  /**
   * @see coyote.dx.web.auth.Authenticator#setPassword(java.lang.String)
   */
  @Override
  public void setPassword( String password ) {
    // no-op method
  }




  /**
   * @see coyote.dx.web.auth.Authenticator#init(coyote.dx.web.Resource)
   */
  @Override
  public void init( Resource resource ) {
    // no-op method
  }




  /**
   * @see coyote.dx.web.auth.Authenticator#setConfiguration(coyote.dataframe.DataFrame)
   */
  @Override
  public void setConfiguration( DataFrame cfg ) throws ConfigurationException {
    // no-op method
  }

}
