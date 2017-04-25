package coyote.nmea.sentence;

import coyote.nmea.Sentence;


/**
 * Water temperature in degrees Celsius.
 * 
 * <p>Example:<pre>$YXMTW,17.75,C*5D</pre>
 */
public interface MTWSentence extends Sentence {

  /**
   * Get the water temperature.
   * 
   * @return Temperature in degrees Celsius.
   */
  double getTemperature();




  /**
   * Set the water temperature.
   * 
   * @param temp Water temperature in degrees Celsius.
   */
  void setTemperature( double temp );
}
