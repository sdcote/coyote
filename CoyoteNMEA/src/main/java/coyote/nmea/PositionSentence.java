package coyote.nmea;



/**
 * Common interface for sentences that contain geographic position.
 */
public interface PositionSentence extends Sentence {

  /**
   * Gets the geographic position.
   * 
   * @return Position
   * 
   * @throws DataNotAvailableException If any of the position related fields is empty.
   * @throws ParseException If any of the position related fields contains unexpected value.
   */
  Position getPosition();




  /**
   * Set the geographic position.
   * 
   * @param pos Position to set
   */
  void setPosition( Position pos );

}
