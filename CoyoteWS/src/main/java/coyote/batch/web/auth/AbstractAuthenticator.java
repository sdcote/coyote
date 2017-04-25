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

import coyote.batch.BatchWS;
import coyote.batch.ConfigTag;
import coyote.batch.web.Resource;
import coyote.commons.CipherUtil;
import coyote.dataframe.DataField;
import coyote.dataframe.DataFrame;
import coyote.dataframe.DataFrameException;
import coyote.loader.cfg.ConfigurationException;


/**
 * Abstract base class for components responsible for establishing an 
 * authenticated session with the web service provider
 */
public abstract class AbstractAuthenticator implements Authenticator {

  private DataFrame configuration = null;
  private volatile boolean authenticatePreemptively = false;
  private String username = null;
  private String password = null;




  /**
   * @return the configuration
   */
  public DataFrame getConfiguration() {
    return configuration;
  }




  /**
   * @return the password the instance will use to authenticate the user
   */
  @Override
  public String getPassword() {
    return password;
  }




  /**
   * @return the name of the user account the instance will use to authenticate
   */
  @Override
  public String getUsername() {
    return username;
  }




  /**
   * @see coyote.batch.web.auth.Authenticator#isAuthenticatingPreemptively()
   */
  @Override
  public boolean isAuthenticatingPreemptively() {
    return authenticatePreemptively;
  }




  /**
   * @see coyote.batch.web.auth.Authenticator#setConfiguration(coyote.dataframe.DataFrame)
   */
  @Override
  public void setConfiguration( final DataFrame cfg ) throws ConfigurationException {
    configuration = cfg;

    if ( configuration != null ) {
      for ( DataField field : configuration.getFields() ) {
        if ( ConfigTag.USERNAME.equalsIgnoreCase( field.getName() ) ) {
          setUsername( field.getStringValue() );
        } else if ( ConfigTag.PASSWORD.equalsIgnoreCase( field.getName() ) ) {
          setPassword( field.getStringValue() );
        } else if ( ConfigTag.ENCRYPTED_USERNAME.equalsIgnoreCase( field.getName() ) ) {
          setUsername( CipherUtil.decryptString( field.getStringValue() ) );
        } else if ( ConfigTag.ENCRYPTED_PASSWORD.equalsIgnoreCase( field.getName() ) ) {
          setPassword( CipherUtil.decryptString( field.getStringValue() ) );
        } else if ( BatchWS.PREEMTIVE_AUTH.equalsIgnoreCase( field.getName() ) ) {
          try {
            setPreemptiveAuthentication( configuration.getAsBoolean( field.getName() ) );
          } catch ( DataFrameException e ) {
            setPreemptiveAuthentication( false );
          } //catch
        } // field name check
      } // for each field
    } // config!null

  }




  /**
   * @see coyote.batch.web.auth.Authenticator#setPassword(java.lang.String)
   */
  @Override
  public void setPassword( final String password ) {
    this.password = password;
  }




  /**
   * @see coyote.batch.web.auth.Authenticator#setPreemptiveAuthentication(boolean)
   */
  @Override
  public void setPreemptiveAuthentication( final boolean preemptive ) {
    authenticatePreemptively = preemptive;
  }




  public void setResource( final Resource resrc ) {}




  /**
   * @see coyote.batch.web.auth.Authenticator#setUsername(java.lang.String)
   */
  @Override
  public void setUsername( final String username ) {
    this.username = username;
  }

}
