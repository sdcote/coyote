package coyote.nmea.sentence;

import coyote.nmea.Sentence;


/**
 * Relative and absolute humidity with dew point.
 * 
 * <p><em>Notice: not recommended as of Oct 2008, should use <code>XDR</code>
 * instead.</em>
 */
public interface MHUSentence extends Sentence {

  /**
   * Returns the humidity relative to temperature of air.
   *
   * @return Relative humidity, percent.
   */
  double getRelativeHumidity();




  /**
   * Returns the absolute humidity value.
   *
   * @return Absolute humidity, g/m³.
   */
  double getAbsoluteHumidity();




  /**
   * Returns the dew point value.
   *
   * @return Dew point, degrees Celsius.
   * @see #getDewPointUnit()
   */
  double getDewPoint();




  /**
   * Returns the unit of dew point temperature, by default degrees Celsius.
   *
   * @return Temperature unit char, defaults to <code>'c'</code>.
   */
  char getDewPointUnit();




  /**
   * Returns the relative humidity.
   *
   * @param humidity Relative humidity, percent.
   */
  void setRelativeHumidity( double humidity );




  /**
   * Returns the absolute humidity value.
   *
   * @param humidity Absolute humidity, percent.
   */
  void setAbsoluteHumidity( double humidity );




  /**
   * Sets the dew point value.
   *
   * @param dewPoint Dew point in degrees Celsius.
   */
  void setDewPoint( double dewPoint );




  /**
   * Sets the unit of dew point temperature, by default degrees Celsius.
   *
   * @param unit Temperature unit char, defaults to <code>'c'</code>.
   */
  void setDewPointUnit( char unit );
}
