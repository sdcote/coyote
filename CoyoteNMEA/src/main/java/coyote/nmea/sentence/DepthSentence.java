package coyote.nmea.sentence;

import coyote.nmea.Sentence;


/**
 * Interface for sentences containing the depth of water.
 */
public interface DepthSentence extends Sentence {

  /**
   * Get depth of water, in meters.
   * 
   * @return Depth value
   */
  double getDepth();




  /**
   * Set depth of water, in meters.
   * 
   * @param depth Depth value
   */
  void setDepth( double depth );
}
