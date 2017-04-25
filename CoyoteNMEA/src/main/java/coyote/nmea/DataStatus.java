package coyote.nmea;

/**
 * DataStatus defines the validity of data being broadcasted by an NMEA device.
 */
public enum DataStatus {

  /** Valid data available. May also indicate boolean value <code>true</code>. */
  ACTIVE( 'A'),

  /**
   * No valid data available. May also indicate boolean value
   * <code>false</code>.
   */
  VOID( 'V');

  private final char character;




  DataStatus( char ch ) {
    character = ch;
  }




  /**
   * Returns the character used in NMEA sentences to indicate the status.
   * 
   * @return Char indicator for DataStatus
   */
  public char toChar() {
    return character;
  }




  /**
   * Returns the DataStatus enum for status char used in sentences.
   * 
   * @param ch Status char
   * @return DataStatus
   */
  public static DataStatus valueOf( char ch ) {
    for ( DataStatus ds : values() ) {
      if ( ds.toChar() == ch ) {
        return ds;
      }
    }
    return valueOf( String.valueOf( ch ) );
  }
}
