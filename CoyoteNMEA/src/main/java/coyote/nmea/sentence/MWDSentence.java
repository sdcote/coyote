package coyote.nmea.sentence;

import coyote.nmea.Sentence;


/**
 * Wind speed and true/magnetic direction, speed given in meters per second and
 * knots.
 */
public interface MWDSentence extends Sentence {

  /**
   * @return Wind direction, degrees True, to the nearest 0,1 degree. NaN if
   *         not available.
   */
  double getMagneticWindDirection();




  /**
   * @return Wind direction, degrees True, to the nearest 0,1 degree. NaN if
   *         not available.
   */
  double getTrueWindDirection();




  /**
   * @return Wind speed, meters per second, to the nearest 0,1 m/s. NaN if not
   *         available.
   */
  double getWindSpeed();




  /**
   * @return Wind speed, in knots, to the nearest 0,1 m/s. NaN if not
   *         available.
   */
  double getWindSpeedKnots();




  /**
   * Sets the magnetic wind direction.
   * 
   * @param direction Wind direction in degrees [0..360]
   */
  void setMagneticWindDirection( double direction );




  /**
   * Sets the true wind direction.
   * 
   * @param direction Wind direction in degrees [0..360].
   */
  void setTrueWindDirection( double direction );




  /**
   * Sets the wind speed in meters per second.
   * 
   * @param speed Wind speed to set.
   */
  void setWindSpeed( double speed );




  /**
   * Sets the wind speed in knots.
   * 
   * @param speed Wind speed to set.
   */
  void setWindSpeedKnots( double speed );

}
