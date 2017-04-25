package coyote.nmea;

/**
 * Defines the supported units of measure.
 */
public enum Units {

  /** Temperature in degrees Celsius (centigrade) */
  CELSIUS( 'C'),

  /** Depth in fathoms */
  FATHOMS( 'F'),

  /** Length in feet */
  FEET( 'f'),

  /** Speed in kilometers per hour */
  KMH( 'K'),

  /** Speed in knots */
  KNOT( 'N'),

  /** Length in meter */
  METER( 'M');

  private char ch;




  private Units( char c ) {
    ch = c;
  }




  /**
   * Returns the corresponding char constant.
   * 
   * @return Char indicator of enum
   */
  public char toChar() {
    return ch;
  }




  /**
   * Get the enum corresponding to specified char.
   * 
   * @param ch Char indicator for unit
   * @return Units enum
   */
  public static Units valueOf( char ch ) {
    for ( Units u : values() ) {
      if ( u.toChar() == ch ) {
        return u;
      }
    }
    return valueOf( String.valueOf( ch ) );
  }
}
