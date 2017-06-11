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
package coyote.commons.network.http;

/**
 * Used to indicate security related processing detected an issure with generating a response.
 * The HTTPSession should drop the connection immediately.
 */
public class SecurityResponseException extends Exception {

  private static final long serialVersionUID = -7169595886319527L;




  public SecurityResponseException( final String message ) {
    super( message );
  }




  public SecurityResponseException( final String message, final Exception e ) {
    super( message, e );
  }

}
