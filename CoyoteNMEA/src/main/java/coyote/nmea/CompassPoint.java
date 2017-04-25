/*
 * Copyright (c) 2016 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.nmea;

/**
 * Defines the supported compass and relative directions.
 */
public enum CompassPoint {

  /** North */
  NORTH( 'N'),
  /** South */
  SOUTH( 'S'),
  /** East */
  EAST( 'E'),
  /** West */
  WEST( 'W');

  /**
   * Get the enum corresponding to specified char.
   * 
   * @param c Char indicator for Direction
   * @return Direction
   */
  public static CompassPoint valueOf( final char c ) {
    for ( final CompassPoint d : values() ) {
      if ( d.toChar() == c ) {
        return d;
      }
    }
    return valueOf( String.valueOf( c ) );
  }

  private char ch;




  private CompassPoint( final char c ) {
    ch = c;
  }




  /**
   * Returns the corresponding char constant.
   * 
   * @return Char indicator for Direction
   */
  public char toChar() {
    return ch;
  }
}
