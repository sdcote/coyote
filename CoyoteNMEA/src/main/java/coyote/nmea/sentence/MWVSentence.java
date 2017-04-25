package coyote.nmea.sentence;

import coyote.nmea.DataStatus;
import coyote.nmea.Sentence;
import coyote.nmea.Units;


/**
 * Wind speed and angle. 
 * 
 * <p>Speed in km/h, m/s, or knots. Wind angle is given in degrees relative to 
 * bow or true north. Example:<pre>$IIMWV,199,R,5.9,N,A*2E</pre>
 */
public interface MWVSentence extends Sentence {

  /**
   * Get wind angle.
   * 
   * @return Wind angle in degrees.
   */
  double getAngle();




  /**
   * Returns the wind speed.
   * 
   * @return Wind speed value
   */
  double getSpeed();




  /**
   * Returns the wind speed unit.
   * 
   * @return {@link Units#METER} for meters per second, {@link Units#KMH} for
   *         kilometers per hour and {@link Units#KNOT} for knots.
   */
  Units getSpeedUnit();




  /**
   * Get data validity status.
   * 
   * @return Data status
   */
  DataStatus getStatus();




  /**
   * Tells if the angle is relative or true.
   * 
   * @return True if relative to true north, otherwise false (relative to bow)
   */
  boolean isTrue();




  /**
   * Set wind angle.
   * 
   * @param angle Wind angle in degrees.
   * @see #setTrue(boolean)
   */
  void setAngle( double angle );




  /**
   * Set the wind speed value.
   * 
   * @param speed Wind speed to set.
   */
  void setSpeed( double speed );




  /**
   * Set wind speed unit.
   * 
   * @param unit {@link Units#METER} for meters per second, {@link Units#KMH}
   *             for kilometers per hour and {@link Units#KNOT} for knots.
   *             
   * @throws IllegalArgumentException If trying to set invalid unit
   */
  void setSpeedUnit( Units unit );




  /**
   * Set data validity status.
   * 
   * @param status Data status to set.
   */
  void setStatus( DataStatus status );




  /**
   * Set angle to relative or true.
   * 
   * @param isTrue True for true angle, false for relative to bow.
   * 
   * @see #setAngle(double)
   */
  void setTrue( boolean isTrue );
}
