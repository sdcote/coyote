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
import coyote.batch.web.decorator.BasicAuth;


/**
 * Perform RFC 2617 basic authentication by simply placing an Authorization 
 * decorator in the resource.
 */
public class BasicAuthentication extends AbstractAuthenticator implements Authenticator {

  public BasicAuthentication() {}




  public BasicAuthentication( final String usr, final String pass ) {
    setUsername( usr );
    setPassword( pass );
  }




  /**
   * As soon as we are added to the resource, this method is called and if we 
   * are to authenticate preemptively, we insert a request decorator in the 
   * resource which performs presents authentication data to all points in the 
   * channel.
   *  
   * @see coyote.batch.web.auth.Authenticator#init(coyote.batch.web.Resource)
   */
  @Override
  public void init( Resource resource ) {
    setResource( resource );
    if ( isAuthenticatingPreemptively() ) {
      resource.addRequestDecorator( new BasicAuth( getUsername(), getPassword() ) );
    }
  }

}
