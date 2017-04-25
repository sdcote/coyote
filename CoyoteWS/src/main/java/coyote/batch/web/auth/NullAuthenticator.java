/*
 * Copyright (c) 2016 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 *
 * Contributors:
 *   Stephan D. Cote 
 *      - Initial concept and initial implementation
 */
package coyote.batch.web.auth;

import coyote.batch.web.Resource;
import coyote.dataframe.DataFrame;
import coyote.loader.cfg.ConfigurationException;


/**
 * A do nothing implementation of the Authenticator to reduce pointer 
 * exceptions.
 */
public class NullAuthenticator implements Authenticator {

  /**
   * @see coyote.batch.web.auth.Authenticator#isAuthenticatingPreemptively()
   */
  @Override
  public boolean isAuthenticatingPreemptively() {
    return false;
  }




  /**
   * @see coyote.batch.web.auth.Authenticator#setPreemptiveAuthentication(boolean)
   */
  @Override
  public void setPreemptiveAuthentication( boolean preemptive ) {}




  /**
   * @see coyote.batch.web.auth.Authenticator#getPassword()
   */
  @Override
  public String getPassword() {
    return null;
  }




  /**
   * @see coyote.batch.web.auth.Authenticator#getUsername()
   */
  @Override
  public String getUsername() {
    return null;
  }




  /**
   * @see coyote.batch.web.auth.Authenticator#setUsername(java.lang.String)
   */
  @Override
  public void setUsername( String username ) {}




  /**
   * @see coyote.batch.web.auth.Authenticator#setPassword(java.lang.String)
   */
  @Override
  public void setPassword( String password ) {}




  /**
   * @see coyote.batch.web.auth.Authenticator#init(coyote.batch.web.Resource)
   */
  @Override
  public void init( Resource resource ) {}




  /**
   * @see coyote.batch.web.auth.Authenticator#setConfiguration(coyote.dataframe.DataFrame)
   */
  @Override
  public void setConfiguration( DataFrame cfg ) throws ConfigurationException {}

}
