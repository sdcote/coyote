package coyote.nmea.sentence;

import coyote.nmea.Sentence;


/**
 * Interface for sentences that provide vessel's true or magnetic heading.
 */
public interface HeadingSentence extends Sentence {

  /**
   * Returns the vessel's current heading.
   * 
   * @return Heading in degrees.
   */
  double getHeading();




  /**
   * Tells if the heading returned and set by {@link #getHeading()} and
   * {@link #setHeading(double)} methods is true or magnetic.
   * 
   * @return {@code true} if true heading, otherwise {@code false} for magnetic 
   *         heading.
   */
  boolean isTrue();




  /**
   * Sets the heading value.
   * 
   * @param degrees Heading in degrees
   * 
   * @throws IllegalArgumentException If heading value out of range [0..360]
   */
  void setHeading( double degrees );

}
