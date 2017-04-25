package coyote.nmea.sentence;

/**
 * Water speed and heading in respect to true and magnetic north.
 * 
 * <p>Example:<pre>$IIVHW,,,213,M,0.00,N,,K*2F</pre>
 */
public interface VHWSentence extends HeadingSentence {

  /**
   * Returns the current magnetic heading.
   * 
   * @return Heading in degrees magnetic.
   */
  double getMagneticHeading();




  /**
   * Returns the current water speed.
   * 
   * @return Speed in km/h (kilometers per hour)
   */
  double getSpeedKmh();




  /**
   * Returns the current water speed.
   * 
   * @return Speed in knots
   */
  double getSpeedKnots();




  /**
   * Sets the magnetic heading.
   * 
   * @param hdg Heading in degrees magnetic.
   * 
   * @throws IllegalArgumentException If value is out of bounds [0..360]
   */
  void setMagneticHeading( double hdg );




  /**
   * Sets the water speed in km/h.
   * 
   * @param kmh Speed in kilometers per hour.
   */
  void setSpeedKmh( double kmh );




  /**
   * Sets the water speed in knots.
   * 
   * @param knots Speed in knots
   */
  void setSpeedKnots( double knots );

}
