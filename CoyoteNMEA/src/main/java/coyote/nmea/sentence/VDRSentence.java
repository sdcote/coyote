package coyote.nmea.sentence;

import coyote.nmea.Sentence;


/**
 * Set and drift, true/magnetic direction and speed of current.
 */
public interface VDRSentence extends Sentence {

  /**
   * Returns the magnetic current direction.
   * 
   * @return Direction in degrees
   * 
   * @see #getTrueDirection()
   */
  double getMagneticDirection();




  /**
   * Returns the current flow speed.
   * 
   * @return Speed in knots
   */
  double getSpeed();




  /**
   * Returns the true direction of current.
   * 
   * @return Direction in degrees
   * 
   * @see #getMagneticDirection()
   */
  double getTrueDirection();




  /**
   * Sets the magnetic direction of current.
   * 
   * @param direction
   */
  void setMagneticDirection( double direction );




  /**
   * Sets the current flow speed.
   * 
   * @param speed Speed in knots
   */
  void setSpeed( double speed );




  /**
   * Sets the true direction of current.
   * 
   * @param direction Direction in degrees
   * 
   * @see #setMagneticDirection(double)
   */
  void setTrueDirection( double direction );

}
