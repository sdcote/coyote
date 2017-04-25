package coyote.nmea.sentence;

import coyote.nmea.DataNotAvailableException;
import coyote.nmea.DataStatus;
import coyote.nmea.ParseException;
import coyote.nmea.PositionSentence;


/**
 * Current geographic position and time.
 * <p>Example: <pre>
 * $GPGLL,6011.552,N,02501.941,E,120045,A*26</pre>
 */
public interface GLLSentence extends PositionSentence, TimeSentence {

  /**
   * Get the data quality status, valid or invalid.
   * 
   * @return {@link DataStatus#ACTIVE} or {@link DataStatus#VOID}
   * 
   * @throws DataNotAvailableException If the data is not available.
   * @throws ParseException If the field contains unexpected or illegal values.
   */
  DataStatus getStatus();




  /**
   * Set the data quality status, valid or invalid.
   * 
   * @param status DataStatus to set
   * 
   * @throws DataNotAvailableException If the data is not available.
   * @throws ParseException If the field contains unexpected or illegal values.
   */
  void setStatus( DataStatus status );

}
