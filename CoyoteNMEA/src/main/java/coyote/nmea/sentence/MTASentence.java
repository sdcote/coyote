package coyote.nmea.sentence;

import coyote.nmea.Sentence;


/**
 * Air temperature in degrees Celsius.
 * 
 * <p>Example:<pre>$IIMTA,16.7,C*05</pre>
 */
public interface MTASentence extends Sentence {

  /**
   * Returns the air temperature.
   * 
   * @return Temperature in degrees Celsius.
   */
  double getTemperature();




  /**
   * Sets the air temperature.
   * 
   * @param temp Temperature in degrees Celsius.
   */
  void setTemperature( double temp );

}
