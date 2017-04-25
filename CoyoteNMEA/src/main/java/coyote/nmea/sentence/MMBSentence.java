package coyote.nmea.sentence;

import coyote.nmea.Sentence;


/**
 * Barometer - Barometric pressure in bars and inches of mercury.
 *
 * <p><em>Notice: not recommended as of Oct 2008, should use <code>XDR</code>
 * instead.</em>
 *
 * <p>Example:<pre>$IIMMB,29.9870,I,1.0154,B*75</pre>
 */
public interface MMBSentence extends Sentence {

  /**
   * Returns the barometric pressure in inches of mercury.
   *
   * @return Barometric pressure, inHg.
   */
  double getInchesOfMercury();




  /**
   * Returns the barometric pressure in bars.
   *
   * @return Barometric pressure, bars.
   */
  double getBars();




  /**
   * Sets the barometric pressure in inches of mercury.
   *
   * @param inhg Barometric pressure, inHg.
   */
  void setInchesOfMercury( double inhg );




  /**
   * Sets the barometric pressure in bars.
   *
   * @param bars Barometric pressure, bars.
   */
  void setBars( double bars );

}
