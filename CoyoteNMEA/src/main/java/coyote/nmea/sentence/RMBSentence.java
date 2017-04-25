package coyote.nmea.sentence;

import coyote.dataframe.marshal.ParseException;
import coyote.nmea.DataNotAvailableException;
import coyote.nmea.DataStatus;
import coyote.nmea.Direction;
import coyote.nmea.Sentence;
import coyote.nmea.Waypoint;


/**
 * Recommended minimum navigation information. 
 * 
 * <p>This sentence is transmitted by a GPS receiver when a destination 
 * waypoint is active (GOTO mode). Example:<pre>
 * $GPRMB,A,0.00,R,,RUSKI,5536.200,N,01436.500,E,432.3,234.9,,V*58</pre>
 */
public interface RMBSentence extends Sentence {

  /**
   * Get the arrival to waypoint status. Status is {@link DataStatus#VOID}
   * (false) while not arrived at destination, otherwise
   * {@link DataStatus#ACTIVE} (true).
   * 
   * @return {@link DataStatus#ACTIVE} or {@link DataStatus#VOID}
   * 
   * @throws DataNotAvailableException If the data is not available.
   * @throws ParseException If the field contains unexpected or illegal value.
   * 
   * @see #hasArrived()
   */
  DataStatus getArrivalStatus();




  /**
   * Get true bearing to destination.
   * 
   * @return True bearing in degrees.
   * 
   * @throws DataNotAvailableException If the data is not available.
   * @throws ParseException If the field contains unexpected or illegal value.
   */
  double getBearing();




  /**
   * Get cross track error (XTE).
   * 
   * @return Cross track error, in nautical miles.
   * 
   * @throws DataNotAvailableException If the data is not available.
   * @throws ParseException If the field contains unexpected or illegal value.
   */
  double getCrossTrackError();




  /**
   * Get the destination waypoint.
   * 
   * @return Waypoint
   * 
   * @throws DataNotAvailableException If the data is not available.
   * @throws ParseException If the field contains unexpected or illegal value.
   */
  Waypoint getDestination();




  /**
   * Get the ID of origin waypoint.
   * 
   * @return Id String.
   * 
   * @throws DataNotAvailableException If the data is not available.
   * @throws ParseException If the field contains unexpected or illegal value.
   */
  String getOriginId();




  /**
   * Get range to destination waypoint.
   * 
   * @return Range to destination, in nautical miles.
   * 
   * @throws DataNotAvailableException If the data is not available.
   * @throws ParseException If the field contains unexpected or illegal value.
   */
  double getRange();




  /**
   * Get the sentence data status, valid or invalid.
   * 
   * @return {@link DataStatus#ACTIVE} or {@link DataStatus#VOID}
   * 
   * @throws DataNotAvailableException If the data is not available.
   * @throws ParseException If the field contains unexpected or illegal value.
   */
  DataStatus getStatus();




  /**
   * Get the direction to steer to correct error (left/right).
   * 
   * @return Direction.LEFT or Direction.RIGHT
   * 
   * @throws DataNotAvailableException If the data is not available.
   * @throws ParseException If the field contains unexpected or illegal value.
   */
  Direction getSteerTo();




  /**
   * Get velocity towards destination. Notice that returned value may also be
   * negative if vehicle is moving away from destination.
   * 
   * @return Velocity value, in knots.
   * 
   * @throws DataNotAvailableException If the data is not available.
   * @throws ParseException If the field contains unexpected or illegal value.
   */
  double getVelocity();




  /**
   * Tells if the destination waypoint has been reached or not.
   * 
   * @return True if has arrived to waypoint, otherwise false.
   * 
   * @throws DataNotAvailableException If arrival status is not available.
   * @throws ParseException If the field contains unexpected or illegal value.
   */
  boolean hasArrived();




  /**
   * Set the arrival to waypoint status. Set {@link DataStatus#VOID} if not
   * arrived at destination, otherwise {@link DataStatus#ACTIVE}.
   * 
   * @param status {@link DataStatus#VOID} or {@link DataStatus#ACTIVE}.
   * 
   * @throws IllegalArgumentException If status is <code>null</code>.
   */
  void setArrivalStatus( DataStatus status );




  /**
   * Set true bearing to destination, in degrees.
   * 
   * @param bearing Bearing value, will be rounded to one decimal.
   * 
   * @throws IllegalArgumentException If bearing value is out of bounds 0..360
   *         degrees.
   */
  void setBearing( double bearing );




  /**
   * Set cross track error (XTE), in nautical miles. 
   * 
   * <p>Negative values are translated to positive, set Steer-To to indicate 
   * the direction of error.
   * 
   * @param xte Cross track error value, will be rounded to one decimal.
   * 
   * @see #setSteerTo(Direction)
   */
  void setCrossTrackError( double xte );




  /**
   * Set the destination waypoint.
   * 
   * @param dest Waypoint to set
   */
  void setDestination( Waypoint dest );




  /**
   * Set the ID of origin waypoint.
   * 
   * @param id ID to set
   */
  void setOriginId( String id );




  /**
   * Set range to destination waypoint.
   * 
   * @param range Range value, in nautical miles.
   */
  void setRange( double range );




  /**
   * Set status of sentence data, valid or invalid.
   * 
   * @param status {@link DataStatus#ACTIVE} or {@link DataStatus#VOID}
   */
  void setStatus( DataStatus status );




  /**
   * Set the direction to steer to correct error (left/right).
   * 
   * @param steerTo {@link Direction#LEFT} or {@link Direction#RIGHT}
   * @throws IllegalArgumentException If specified direction is any other than
   *         defined valid for param <code>steer</code>.
   */
  void setSteerTo( Direction steerTo );




  /**
   * Set velocity towards destination. 
   * 
   * <p>Note that value may also be negative if vehicle is moving away from the 
   * destination.
   * 
   * @param velocity Velocity, in knots.
   */
  void setVelocity( double velocity );

}
