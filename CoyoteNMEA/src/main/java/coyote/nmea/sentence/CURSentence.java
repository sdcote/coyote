package coyote.nmea.sentence;

import coyote.nmea.DataNotAvailableException;
import coyote.nmea.ParseException;
import coyote.nmea.Sentence;


/**
 * Multi-layer current data sentence
 */
public interface CURSentence extends Sentence {

  /**
   * Get the Current Speed in knots
   *
   * @return current speed
   * 
   * @throws DataNotAvailableException If the data is not available.
   * @throws ParseException If the field contains unexpected or illegal value.
   */
  double getCurrentSpeed();




  /**
   * Get the current direction.
   *
   * @return current direction
   * 
   * @throws DataNotAvailableException If the data is not available.
   * @throws ParseException If the field contains unexpected or illegal value.
   */
  double getCurrentDirection();




  /**
   * Get the current direction reference.
   *
   * @return current direction reference T/R
   * 
   * @throws DataNotAvailableException If the data is not available.
   * @throws ParseException If the field contains unexpected or illegal value.
   */
  String getCurrentDirectionReference();




  /**
   * Get the current heading reference.
   *
   * @return current heading reference T/M (True/Magnetic)
   * 
   * @throws DataNotAvailableException If the data is not available.
   * @throws ParseException If the field contains unexpected or illegal value.
   */
  String getCurrentHeadingReference();

}
