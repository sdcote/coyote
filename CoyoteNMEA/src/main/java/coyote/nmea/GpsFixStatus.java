package coyote.nmea;




/**
 * GpsFixStatus defines the status of current GPS fix.
 * 
 * @see FaaMode
 * @see GpsFixQuality
 * @see DataStatus
 */
public enum GpsFixStatus {

  /** No GPS fix available */
  GPS_NA( 1),
  /** 2D GPS fix (lat/lon) */
  GPS_2D( 2),
  /** 3D GPS fix (lat/lon/alt) */
  GPS_3D( 3);

  private final int status;




  GpsFixStatus( int intVal ) {
    status = intVal;
  }




  /**
   * Returns the corresponding int value for fix status enum.
   * 
   * @return Fix status integer values as in sentences
   */
  public int toInt() {
    return status;
  }




  /**
   * Returns the GpsFixStatus enum corresponding to actual int identifier used
   * in the sentences.
   * 
   * @param val Fix status identifier int
   * 
   * @return GpsFixStatus enum
   */
  public static GpsFixStatus valueOf( int val ) {
    for ( GpsFixStatus st : values() ) {
      if ( st.toInt() == val ) {
        return st;
      }
    }
    return valueOf( String.valueOf( val ) );
  }
}
