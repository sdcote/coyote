package coyote.nmea;

/**
 * Defines the sides of a boat, i.e. "port" and "starboard".
 */
public enum Side {

  /** Port */
  PORT( 'P'),

  /** Right */
  STARBOARD( 'S');

  private char ch;




  private Side( char c ) {
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




  /**
   * Get the enum corresponding to specified char.
   * 
   * @param c Char indicator for Side
   * 
   * @return Side
   */
  public static Side valueOf( char c ) {
    for ( Side d : values() ) {
      if ( d.toChar() == c ) {
        return d;
      }
    }
    return valueOf( String.valueOf( c ) );
  }
}
