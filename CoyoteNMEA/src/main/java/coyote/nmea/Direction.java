package coyote.nmea;

/**
 * Defines the relative directions, e.g. "left" and "right".
 */
public enum Direction {

  /** Left */
  LEFT( 'L'),

  /** Right */
  RIGHT( 'R');

  private char ch;




  private Direction( char c ) {
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
   * @param c Char indicator for Direction
   * 
   * @return Direction
   */
  public static Direction valueOf( char c ) {
    for ( Direction d : values() ) {
      if ( d.toChar() == c ) {
        return d;
      }
    }
    return valueOf( String.valueOf( c ) );
  }

}
