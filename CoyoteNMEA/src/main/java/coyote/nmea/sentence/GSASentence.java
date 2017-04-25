package coyote.nmea.sentence;

import coyote.nmea.DataNotAvailableException;
import coyote.nmea.FaaMode;
import coyote.nmea.GpsFixStatus;
import coyote.nmea.ParseException;
import coyote.nmea.Sentence;


/**
 * Precision of GPS fix and list of active satellites. 
 * 
 * <p>Dilution of precision (DOP) is an indication of the effect of satellite 
 * geometry on the accuracy of the fix. It is a unit-less number where smaller 
 * is better.
 * 
 * <p>Example:<pre>
 * $GPGSA,A,3,02,,,07,,09,24,26,,,,,1.6,1.6,1.0*3D</pre>
 */
public interface GSASentence extends Sentence {

  /**
   * Get the GPS fix mode; 2D, 3D or no fix.
   * 
   * @return GpsFixStatus enum
   * 
   * @throws DataNotAvailableException If the data is not available.
   * @throws ParseException If the field contains unexpected or illegal value.
   */
  GpsFixStatus getFixStatus();




  /**
   * Get the horizontal dilution Of precision (HDOP).
   * 
   * @return double (smaller is better)
   * 
   * @throws DataNotAvailableException If the data is not available.
   * @throws ParseException If the field contains unexpected or illegal value.
   */
  double getHorizontalDOP();




  /**
   * Get the FAA operation mode of GPS.
   * 
   * @return FaaMode enum
   * 
   * @throws DataNotAvailableException If the data is not available.
   * @throws ParseException If the field contains unexpected or illegal value.
   */
  FaaMode getMode();




  /**
   * Get the dilution of precision (PDOP) for position.
   * 
   * @return double
   * 
   * @throws DataNotAvailableException If the data is not available.
   * @throws ParseException If the field contains unexpected or illegal value.
   */
  double getPositionDOP();




  /**
   * Get list of satellites used for acquiring the GPS fix.
   * 
   * @return String array containing satellite IDs.
   * 
   * @throws DataNotAvailableException If the data is not available.
   * @throws ParseException If the field contains unexpected or illegal value.
   */
  String[] getSatelliteIds();




  /**
   * Get the vertical dilution of precision (VDOP).
   * 
   * @return double (smaller is better)
   * 
   * @throws DataNotAvailableException If the data is not available.
   * @throws ParseException If the field contains unexpected or illegal value.
   */
  double getVerticalDOP();




  /**
   * Set the GPS fix mode; 2D, 3D or no fix.
   * 
   * @param status Status to set
   */
  void setFixStatus( GpsFixStatus status );




  /**
   * Set the horizontal dilution of precision (HDOP).
   * 
   * @param hdop Precision value to set
   */
  void setHorizontalDOP( double hdop );




  /**
   * Set the FAA operation mode of GPS.
   * 
   * @param mode Mode to set
   */
  void setMode( FaaMode mode );




  /**
   * Set the dilution of precision for position.
   * 
   * @param pdop Precision value to set
   */
  void setPositionDOP( double pdop );




  /**
   * Set list of satellites used for acquiring the GPS fix.
   * 
   * @param ids List of satellite IDs, maximum length of array is 12.
   */
  void setSatelliteIds( String[] ids );




  /**
   * Set the vertical dilution of precision (VDOP).
   * 
   * @param vdop Precision value to set
   */
  void setVerticalDOP( double vdop );

}
