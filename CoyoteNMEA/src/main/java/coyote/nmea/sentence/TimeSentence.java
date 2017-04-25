package coyote.nmea.sentence;

import coyote.nmea.NMEATime;
import coyote.nmea.Sentence;


/**
 * Interface for sentences that provide UTC time. Notice that some sentences
 * contain only UTC time, while others may provide also date.
 */
public interface TimeSentence extends Sentence {

  /**
   * Get the time of day.
   * 
   * @return Time
   */
  NMEATime getTime();




  /**
   * Set the time of day.
   * 
   * @param time Time to set
   */
  void setTime( NMEATime time );
}
