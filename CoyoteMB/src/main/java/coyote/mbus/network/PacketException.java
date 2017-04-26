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
 * The PacketException class models packet processing exception events
 */
public class PacketException extends Exception {
  public static final long serialVersionUID = 1L;




  /**
   * 
   */
  public PacketException() {
    super();
  }




  /**
   * @param message
   */
  public PacketException( final String message ) {
    super( message );
  }




  /**
   * @param message
   * @param newNested
   */
  public PacketException( final String message, final Throwable newNested ) {
    super( message, newNested );
  }




  /**
   * @param newNested
   */
  public PacketException( final Throwable newNested ) {
    super( newNested );
  }

}
