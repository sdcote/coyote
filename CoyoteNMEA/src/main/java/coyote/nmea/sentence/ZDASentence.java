package coyote.nmea.sentence;

import java.util.Date;

import coyote.nmea.DataNotAvailableException;
import coyote.nmea.NMEATime;
import coyote.nmea.ParseException;


/**
 * UTC time and date with local time zone offset.
 * <p>Example:<pre>$GPZDA,032915,07,08,2004,00,00*4D</pre>
 */
public interface ZDASentence extends TimeSentence, DateSentence {

  /**
   * Get offset to local time zone in hours, from 0 to +/- 13 hours.
   *
   * @return Time zone offset
   * 
   * @throws DataNotAvailableException If the data is not available.
   * @throws ParseException If the field contains unexpected or illegal value.
   */
  int getLocalZoneHours();




  /**
   * Get offset to local time zone in minutes, from 0 to +/- 59.
   *
   * @return Time zone offset
   * 
   * @throws DataNotAvailableException If the data is not available.
   * @throws ParseException If the field contains unexpected or illegal value.
   */
  int getLocalZoneMinutes();




  /**
   * Set offset to local time zone in hours.
   *
   * @param hours Offset, from 0 to +/- 13 hours.
   */
  void setLocalZoneHours( int hours );




  /**
   * Set offset to local time zone in minutes.
   *
   * @param minutes Offset, from 0 to +/- 59 minutes.
   */
  void setLocalZoneMinutes( int minutes );




  /**
   * Set time and local time zone hours and minutes.
   *
   * @param t Time to be inserted in sentence.
   */
  void setTimeAndLocalZone( NMEATime t );




  /**
   * Get date and time as {@link java.util.Date}.
   *
   * @return {@link java.util.Date}
   * 
   * @throws DataNotAvailableException If any of the date/time values is not available.
   * @throws ParseException If the any of the date/time fields contains invalid value.
   */
  Date toDate();

}