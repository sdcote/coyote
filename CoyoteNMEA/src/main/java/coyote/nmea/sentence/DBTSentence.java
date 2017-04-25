package coyote.nmea.sentence;

/**
 * Water depth below transducer, in meters, feet and fathoms.
 * 
 * <p>Example:<pre>$SDDBT,8.1,f,2.4,M,1.3,F*0B</pre>
 */
public interface DBTSentence extends DepthSentence {

  /**
   * Get depth in fathoms.
   * 
   * @return Depth value
   */
  double getFathoms();




  /**
   * Get depth in feet.
   * 
   * @return Depth value
   */
  double getFeet();




  /**
   * Set depth value, in fathoms.
   * 
   * @param depth Depth to set
   */
  void setFathoms( double depth );




  /**
   * Set depth value, in feet.
   * 
   * @param depth Depth to set
   */
  void setFeet( double depth );

}
