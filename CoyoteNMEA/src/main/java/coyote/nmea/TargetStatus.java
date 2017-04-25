package coyote.nmea;

/**
 * Defines the status of a target reported in a TTM sentence.
 */
public enum TargetStatus {

  /** Query */
  QUERY( 'Q'),
  /** Lost */
  LOST( 'L'),
  /** Tracking */
  TRACKING( 'T');

  private char ch;




  private TargetStatus( char ch ) {
    this.ch = ch;
  }




  /**
   * Returns the corresponding char constant.
   *
   * @return Char indicator for Status
   */
  public char toChar() {
    return ch;
  }




  /**
   * Get the enum corresponding to specified char.
   *
   * @param c Char indicator for Status
   * 
   * @return Status
   */
  public static TargetStatus valueOf( char c ) {
    for ( TargetStatus d : values() ) {
      if ( d.toChar() == c ) {
        return d;
      }
    }
    return valueOf( String.valueOf( c ) );
  }
}
