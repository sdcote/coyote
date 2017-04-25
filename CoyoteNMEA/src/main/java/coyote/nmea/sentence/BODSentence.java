package coyote.nmea.sentence;

import coyote.dataframe.marshal.ParseException;
import coyote.nmea.DataNotAvailableException;
import coyote.nmea.Sentence;


/**
 * True and magnetic bearing from origin to destination in degrees. 
 * 
 * <p>This sentence is transmitted by a GPS in the GOTO mode (with or without 
 * active route).
 * <p>Example:<pre>
 * $GPBOD,234.9,T,228.8,M,RUSKI,*1D</pre>
 */
public interface BODSentence extends Sentence {

  /**
   * Get the ID of destination waypoint. This field should be always available
   * in GOTO mode.
   * 
   * @return waypoint id
   * 
   * @throws DataNotAvailableException If the data is not available.
   * @throws ParseException If the field contains unexpected or illegal value.
   */
  String getDestinationWaypointId();




  /**
   * Get the magnetic bearing from origin to destination.
   * <p><strong>Note:</strong> The bearing is calculated from the origin when 
   * GOTO is activated and it is <b>not</b> updated dynamically.
   * 
   * @return magnetic bearing value
   * 
   * @throws DataNotAvailableException If the data is not available.
   * @throws ParseException If the field contains unexpected or illegal value.
   */
  double getMagneticBearing();




  /**
   * Get the ID of origin waypoint. 
   * 
   * <p>This field is available only when route is active.
   * 
   * @return waypoint id
   * 
   * @throws DataNotAvailableException If the data is not available.
   * @throws ParseException If the field contains unexpected or illegal value.
   */
  String getOriginWaypointId();




  /**
   * Get the true bearing from origin to destination.
   * 
   * <p><strong>Notice:</strong> Typically the bearing is calculated when GOTO 
   * mode is activated and it is <b>not</b> updated dynamically.
   * 
   * @return True bearing
   * 
   * @throws DataNotAvailableException If the data is not available.
   * @throws ParseException If the field contains unexpected or illegal value.
   */
  double getTrueBearing();




  /**
   * Sets the destination waypoint ID.
   * 
   * @param id ID to set
   */
  void setDestinationWaypointId( String id );




  /**
   * Sets the true bearing from origin to destination, in degrees.
   * 
   * @param bearing Bearing value
   * 
   * @throws IllegalArgumentException If bearing value out range 0..360 degrees
   */
  void setMagneticBearing( double bearing );




  /**
   * Sets the ID of origin waypoint.
   * 
   * @param id ID to set.
   */
  void setOriginWaypointId( String id );




  /**
   * Sets the true bearing from origin to destination, in degrees.
   * 
   * @param bearing Bearing value
   * @throws IllegalArgumentException If bearing value out range 0..360 degrees
   */
  void setTrueBearing( double bearing );

}
