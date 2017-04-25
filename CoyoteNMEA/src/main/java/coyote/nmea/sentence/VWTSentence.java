package coyote.nmea.sentence;

import coyote.nmea.DataNotAvailableException;
import coyote.nmea.Direction;
import coyote.nmea.ParseException;
import coyote.nmea.Sentence;


/**
 * True Wind Speed and Angle
 * 
 * <p>True wind angle in relation to the vessel's heading and true wind speed 
 * referenced to the water. Speed in Knots, Meters Per Second and Kilometers 
 * Per Hour. Example:<pre>$--VWT,x.x,a,x.x,N,x.x,M,x.x,K*hh</pre>
 */
public interface VWTSentence extends Sentence {

  /** Units indicator for meters per second */
  char MPS = 'M';
  /** Units indicator for kilometers per hour */
  char KMPH = 'K';
  /** Units indicator for knots (nautical miles per hour) */
  char KNOT = 'N';




  /**
   * Get the Wind angle magnitude in degrees
   *
   * @return Wind angle
   * 
   * @throws DataNotAvailableException If the data is not available.
   * @throws ParseException If the field contains unexpected or illegal value.
   */
  double getWindAngle();




  /**
   * Get the Wind angle Left/Right of bow
   *
   * @since NMEA 2.3
   * 
   * @return {@link Direction} enum
   * 
   * @throws DataNotAvailableException If the data is not available.
   * @throws ParseException If the field contains unexpected or illegal value.
   */
  Direction getDirectionLeftRight();




  /**
   * Get relative wind speed, in kilometers per hour.
   * 
   * @return Speed in km/h
   * 
   * @throws DataNotAvailableException If the data is not available.
   * @throws ParseException If the field contains unexpected or illegal value.
   */
  double getSpeedKmh();




  /**
   * Get relative wind speed in knots.
   * 
   * @return Speed in knots (nautical miles per hour)
   * 
   * @throws DataNotAvailableException If the data is not available.
   * @throws ParseException If the field contains unexpected or illegal value.
   */
  double getSpeedKnots();




  /**
   * Set the Wind angle magnitude
   * 
   * @param mWindAngle Wind angle magnitude in degrees.
   */
  void setWindAngle( double mWindAngle );




  /**
   * Set the Wind angle Left/Right of bow
   * 
   * @param direction Direction to set
   * 
   * @since NMEA 2.3
   */
  void setDirectionLeftRight( Direction direction );




  /**
   * Set the relative wind speed in kmh
   * 
   * @param kmh Speed in kilometers per hour (km/h).
   */
  void setSpeedKmh( double kmh );




  /**
   * Set the relative wind speed in knots.
   * 
   * @param knots Speed in knots (nautical miles per hour)
   */
  void setSpeedKnots( double knots );

}