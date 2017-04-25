package coyote.nmea;




/**
 * <p>FAA operating modes reported by APB, BWC, BWR, GLL, RMA, RMB, RMC, VTG,
 * WCV and XTE sentences since NMEA 2.3. Also, the mode field in GGA was
 * extended to contain these statuses.
 * 
 * <p>Notice that FAA mode dominates the {@link DataStatus} fields. Status 
 * field will be set to {@link DataStatus#ACTIVE} for modes {@link #AUTOMATIC} 
 * and {@link #DGPS}, and {@link DataStatus#VOID} for all other modes.
 * 
 * @see GpsFixQuality
 * @see GpsFixStatus
 * @see DataStatus
 */
public enum FaaMode {

  /** Operating in autonomous mode (automatic 2D/3D). */
  AUTOMATIC( 'A'),

  /** Operating in manual mode (forced 2D or 3D). */
  MANUAL( 'M'),

  /** Operating in differential mode (DGPS). */
  DGPS( 'D'),

  /** Operating in estimating mode (dead-reckoning). */
  ESTIMATED( 'E'),

  /** Simulated data (running in simulator/demo mode) */
  SIMULATED( 'S'),

  /** No valid GPS data available. */
  NONE( 'N');

  private final char mode;




  FaaMode( char modeCh ) {
    mode = modeCh;
  }




  /**
   * Returns the corresponding char indicator of GPS mode.
   * 
   * @return Mode char used in sentences.
   */
  public char toChar() {
    return mode;
  }




  /**
   * Returns the FaaMode enum corresponding the actual char indicator used in
   * the sentences.
   * 
   * @param ch Char mode indicator
   * 
   * @return FaaMode enum
   */
  public static FaaMode valueOf( char ch ) {
    for ( FaaMode gm : values() ) {
      if ( gm.toChar() == ch ) {
        return gm;
      }
    }
    return valueOf( String.valueOf( ch ) );
  }
}
