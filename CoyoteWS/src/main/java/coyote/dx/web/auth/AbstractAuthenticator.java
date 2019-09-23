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

import coyote.commons.CipherUtil;
import coyote.commons.template.SymbolTable;
import coyote.commons.template.Template;
import coyote.dataframe.DataField;
import coyote.dataframe.DataFrame;
import coyote.dataframe.DataFrameException;
import coyote.dx.CWS;
import coyote.dx.ConfigTag;
import coyote.dx.web.Resource;
import coyote.loader.Loader;
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
   * @see coyote.dx.web.auth.Authenticator#isAuthenticatingPreemptively()
   */
  @Override
  public boolean isAuthenticatingPreemptively() {
    return authenticatePreemptively;
  }




  /**
   * @see coyote.dx.web.auth.Authenticator#setConfiguration(coyote.dataframe.DataFrame)
   */
  @Override
  public void setConfiguration( final DataFrame cfg ) throws ConfigurationException {
    configuration = cfg;

    if ( configuration != null ) {
      for ( DataField field : configuration.getFields() ) {
        if ( ConfigTag.USERNAME.equalsIgnoreCase( field.getName() ) ) {
          setUsername( Template.resolve( field.getStringValue(),new SymbolTable()) );
        } else if ( ConfigTag.PASSWORD.equalsIgnoreCase( field.getName() ) ) {
          setPassword( Template.resolve(field.getStringValue(),new SymbolTable()) );
        } else if ( (Loader.ENCRYPT_PREFIX+ConfigTag.USERNAME).equalsIgnoreCase( field.getName() ) ) {
          setUsername( CipherUtil.decryptString( field.getStringValue() ) );
        } else if ( (Loader.ENCRYPT_PREFIX+ConfigTag.PASSWORD).equalsIgnoreCase( field.getName() ) ) {
          setPassword( CipherUtil.decryptString( field.getStringValue() ) );
        } else if ( CWS.PREEMTIVE_AUTH.equalsIgnoreCase( field.getName() ) ) {
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
   * @see coyote.dx.web.auth.Authenticator#setPassword(java.lang.String)
   */
  @Override
  public void setPassword( final String password ) {
    this.password = password;
  }




  /**
   * @see coyote.dx.web.auth.Authenticator#setPreemptiveAuthentication(boolean)
   */
  @Override
  public void setPreemptiveAuthentication( final boolean preemptive ) {
    authenticatePreemptively = preemptive;
  }




  public void setResource( final Resource resrc ) {}




  /**
   * @see coyote.dx.web.auth.Authenticator#setUsername(java.lang.String)
   */
  @Override
  public void setUsername( final String username ) {
    this.username = username;
  }

}
