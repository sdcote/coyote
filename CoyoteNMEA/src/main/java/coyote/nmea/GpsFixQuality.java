package coyote.nmea;




/**
 * GpsFixQuality defines the supported fix quality types.
 * 
 * @see FaaMode
 * @see GpsFixStatus
 * @see DataStatus
 */
public enum GpsFixQuality {

  /** No GPS fix acquired. */
  INVALID( 0),

  /** Normal GPS fix, Standard Position Service (SPS). */
  NORMAL( 1),

  /** Differential GPS fix. */
  DGPS( 2),

  /** Precise Positioning Service fix. */
  PPS( 3),

  /** Real Time Kinematic */
  RTK( 4),

  /** Float RTK */
  FRTK( 5),

  /** Estimated, dead reckoning (2.3 feature) */
  ESTIMATED( 6),

  /** Manual input mode */
  MANUAL( 7),

  /** Simulation mode */
  SIMULATED( 8);

  private final int value;




  GpsFixQuality( int intValue ) {
    value = intValue;
  }




  /**
   * Returns the corresponding int indicator for fix quality.
   * 
   * @return Fix quality indicator value as indicated in sentences.
   */
  public int toInt() {
    return value;
  }




  /**
   * Get GpsFixQuality enum that corresponds the actual integer identifier
   * used in the sentences.
   * 
   * @param val Status identifier value
   * @return GpsFixQuality enum
   */
  public static GpsFixQuality valueOf( int val ) {
    for ( GpsFixQuality gfq : values() ) {
      if ( gfq.toInt() == val ) {
        return gfq;
      }
    }
    return valueOf( String.valueOf( val ) );
  }
}
