/*
 * Copyright (c) 2006 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 *
 * Contributors:
 *   Stephan D. Cote 
 *      - Initial concept and implementation
 */
package coyote.mbus.network;

/**
 * Class NetworkServiceException
 */
public class NetworkServiceException extends Exception {
  public static final long serialVersionUID = 134865456L;




  /**
   *
   */
  public NetworkServiceException() {
    super();
  }




  /**
   * @param message
   */
  public NetworkServiceException( final String message ) {
    super( message );
  }




  /**
   * @param message
   * @param newNested
   */
  public NetworkServiceException( final String message, final Throwable newNested ) {
    super( message, newNested );
  }




  /**
   * @param newNested
   */
  public NetworkServiceException( final Throwable newNested ) {
    super( newNested );
  }
}
