package coyote.nmea.sentence;

/**
 * Vessel heading with magnetic deviation and variation.
 * 
 * <p>Example:<pre>$HCHDG,205.2,,,2.7,W</pre>
 */
public interface HDGSentence extends HeadingSentence {

  /**
   * Get magnetic deviation.
   * 
   * @return Deviation, in degrees.
   */
  double getDeviation();




  /**
   * Get magnetic variation. Returns negative values for easterly variation
   * and positive for westerly.
   * 
   * @return Variation, in degrees.
   */
  double getVariation();




  /**
   * Set magnetic deviation. Provide negative values to set easterly deviation
   * and positive to set westerly. Sets also the correct direction indicator
   * according to value (East/West).
   * 
   * @param deviation Deviation, in degrees.
   * 
   * @throws IllegalArgumentException If value is out of range [-180..180].
   */
  void setDeviation( double deviation );




  /**
   * Set magnetic variation. Provide negative values to set easterly variation
   * and positive to set westerly. Sets also the correct direction indicator
   * according to value (East/West).
   * 
   * @param variation Variation, in degrees.
   * 
   * @throws IllegalArgumentException If value is out of range [-180..180].
   */
  void setVariation( double variation );
}
