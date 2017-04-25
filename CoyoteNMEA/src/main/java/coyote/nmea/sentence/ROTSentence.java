package coyote.nmea.sentence;

import coyote.nmea.DataStatus;
import coyote.nmea.Sentence;


/**
 * Vessel's rate of turn given in degrees per minute.
 * 
 * <p>Negative values indicate bow turning to port. Example:
 * <pre>$GPROT,35.6,A*4E</pre>
 */
public interface ROTSentence extends Sentence {

  /**
   * Returns the vessel's rate of turn.
   * 
   * @return Rate of Turn value (degrees per minute)
   */
  double getRateOfTurn();




  /**
   * Sets the vessel's rate of turn value.
   * 
   * @param rot Rate of Turn value to set (degrees per minute)
   */
  void setRateOfTurn( double rot );




  /**
   * Returns the data status (valid/invalid).
   * 
   * @return True means data is valid
   */
  DataStatus getStatus();




  /**
   * Sets the data status.
   * 
   * @param status DataStatus to set.
   */
  void setStatus( DataStatus status );

}
