package coyote.nmea.sentence;

import coyote.nmea.DataNotAvailableException;
import coyote.nmea.NMEADate;
import coyote.nmea.ParseException;
import coyote.nmea.Sentence;


/**
 * Sentences that contains date information. Notice that some sentences may
 * contain only time without the date.
 */
public interface DateSentence extends Sentence {

  /**
   * Parses the date information from sentence fields and returns a NMEADate.
   * 
   * @return NMEA Date object
   * 
   * @throws DataNotAvailableException If the data is not available.
   * @throws ParseException If the field contains unexpected or illegal value.
   */
  NMEADate getDate();




  /**
   * Set date. Depending on the sentence type, the values may be inserted to
   * multiple fields or combined into one. Four-digit year value may also be
   * reduced into two-digit format.
   * 
   * @param date the date to set
   */
  void setDate( NMEADate date );

}
