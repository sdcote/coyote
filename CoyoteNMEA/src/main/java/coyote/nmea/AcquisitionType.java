package coyote.nmea;

/**
 * Acquisition types.
 */
public enum AcquisitionType {

  /** Auto */
  AUTO( 'A'), 
  /** Manual */
  MANUAL( 'M'), 
  /** Reported */
  REPORTED( 'R');

  private char ch;




  private AcquisitionType( char ch ) {
    this.ch = ch;
  }




  /**
   * Returns the corresponding char constant.
   *
   * @return Char indicator for AcquisitionType
   */
  public char toChar() {
    return ch;
  }




  /**
   * Get the enum corresponding to specified char.
   *
   * @param c Char indicator for AcquisitionType
   * 
   * @return AcquisitionType
   */
  public static AcquisitionType valueOf( char c ) {
    for ( AcquisitionType d : values() ) {
      if ( d.toChar() == c ) {
        return d;
      }
    }
    return valueOf( String.valueOf( c ) );
  }
}
