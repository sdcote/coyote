package coyote.nmea.sentence;

import coyote.nmea.DataNotAvailableException;
import coyote.nmea.Direction;
import coyote.nmea.ParseException;
import coyote.nmea.Sentence;


/**
 * Relative Wind Speed and Angle.
 * 
 * <p>Wind direction magnitude in degrees Wind direction Left/Right of bow 
 * Speed in Knots, Meters Per Second and Kilometers Per Hour.
 */
public interface VWRSentence extends Sentence {

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
   * Get the Wind direction Left/Right of bow
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
   * @return Speed in knots
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
   * Set the Wind direction Left/Right of bow
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