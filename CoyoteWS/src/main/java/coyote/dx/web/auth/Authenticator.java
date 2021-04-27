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
 * Describes the contract a session authenticator should meet to be used in the
 * toolkit.
 * 
 * <p>Web services have many different options when authenticating a 
 * connection, These include simple HTTP Basic Authentication, OAuth and many
 * others. This toolkit supports a modular authentication mechanism which 
 * allows for the configuration of many different authentication strategies 
 * through the use of this interface.
 */
public interface Authenticator {

  /**
   * @return true if authentication should happen before failing or being 
   *         challenged, false to wait until challenged (default).
   */
  public boolean isAuthenticatingPreemptively();




  /**
   * Instructs the instance to send authentication with each request or to 
   * perform authentication before being challenged. 
   * 
   * <p>This is false by default as it is generally considered less secure to 
   * send authorization data with every request. In the case of HTTP Basic 
   * Authentication, this instructs the instance to send the username and 
   * password to every component (proxy and service instance) regardless of the 
   * auth domain or if authorization was required.
   * 
   * <p>This option is provided because some proxy / SOA gateway 
   * implementations will not send a challenge and just deny the connection. It 
   * is generally not recommended to set this to true unless it is required. It 
   * should not break anything, but one never knows. It is just another switch 
   * to flip when things are not working as expected.
   * 
   * @param preemptive true means the client will perform authentication before 
   *        being challenged, false, will cause the client to wait for 
   *        authentication challenges before performing authentication.
   */
  public void setPreemptiveAuthentication( boolean preemptive );




  /**
   * @return the password the instance will use to authenticate the user
   */
  public String getPassword();




  /**
   * @return the name of the user account the instance will use to authenticate
   */
  public String getUsername();




  /**
   * @param username the username to set in this authenticator
   */
  public void setUsername( String username );




  /**
   * @param password the password to set in this authenticator
   */
  public void setPassword( String password );




  /**
   * Initialize this Authenticator with this resource.
   * 
   * @param resource the resource for which this authenticator is to handle 
   * 
   * @throws AuthenticationException if authentication operations fail.
   */
  public void init( Resource resource ) throws AuthenticationException ;




  /**
   * Configure the authenticator with the given data frame.
   * 
   * <p>While authenticators perform the same role, each authenticator is 
   * different and will have different configuration options. This allows each 
   * authenticator to have its own configuration approach.
   * 
   * @param cfg the abstract data type containing configuration data
   * 
   * @throws ConfigurationException if the configuration was invalid
   */
  public void setConfiguration( DataFrame cfg ) throws ConfigurationException;





}
