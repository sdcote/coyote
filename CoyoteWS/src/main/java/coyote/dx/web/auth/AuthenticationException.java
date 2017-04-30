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

/**
 * Exception thrown when the authenticator fails authentication.
 */
public class AuthenticationException extends Exception {

  /**
   * 
   */
  private static final long serialVersionUID = -5299884919732516777L;




  /**
   * 
   */
  public AuthenticationException() {
    super();
  }




  /**
   * @param message
   */
  public AuthenticationException( final String message ) {
    super( message );
  }




  /**
   * @param message
   * @param newNested
   */
  public AuthenticationException( final String message, final Throwable newNested ) {
    super( message, newNested );
  }




  /**
   * @param newNested
   */
  public AuthenticationException( final Throwable newNested ) {
    super( newNested );
  }

}
