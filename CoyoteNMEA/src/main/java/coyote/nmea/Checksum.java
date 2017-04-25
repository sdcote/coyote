package coyote.nmea;

/**
 * Provides Sentence checksum calculation and utilities.
 */
public final class Checksum {

  /**
   * Append or replace existing checksum in specified NMEA sentence.
   * 
   * @param nmea Sentence in String representation
   * @return The specified String with checksum added.
   */
  public static String add( final String nmea ) {
    final String str = nmea.substring( 0, index( nmea ) );
    final String sum = calculate( str );
    return String.format( "%s%c%s", str, Sentence.CHECKSUM_DELIMITER, sum );
  }




  /**
   * Calculates checksum for given NMEA sentence, i.e. XOR of each
   * character between '$' and '*' characters (exclusive).
   * 
   * @param nmea Sentence String with or without checksum.
   * @return Hexadecimal checksum
   */
  public static String calculate( final String nmea ) {
    return xor( nmea.substring( 1, index( nmea ) ) );
  }




  /**
   * Returns the index of checksum separator char in specified NMEA sentence.
   * If separator is not found, returns the String length.
   * 
   * @param nmea Sentence String
   * @return Index of checksum separator or String length.
   */
  public static int index( final String nmea ) {
    return nmea.indexOf( Sentence.CHECKSUM_DELIMITER ) > 0 ? nmea.indexOf( Sentence.CHECKSUM_DELIMITER ) : nmea.length();
  }




  /**
   * Calculates XOR checksum of given String. Resulting hex value is returned
   * as a String in two digit format, padded with a leading zero if necessary.
   * 
   * @param str String to calculate checksum for.
   * @return Hexadecimal checksum
   */
  public static String xor( final String str ) {
    int sum = 0;
    for ( int i = 0; i < str.length(); i++ ) {
      sum ^= (byte)str.charAt( i );
    }
    return String.format( "%02X", sum );
  }




  private Checksum() {}

}
