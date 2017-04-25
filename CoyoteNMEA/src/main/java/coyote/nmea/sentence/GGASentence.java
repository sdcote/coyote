package coyote.nmea.sentence;

import coyote.nmea.DataNotAvailableException;
import coyote.nmea.GpsFixQuality;
import coyote.nmea.ParseException;
import coyote.nmea.PositionSentence;
import coyote.nmea.Units;


/**
 * Global Positioning System fix data. 
 * 
 * <p>Current position, time and other fix related data for a GPS receiver.
 * Example:<pre>
 * $GPGGA,143503.000,4002.1083,N,08309.5833,W,2,08,1.18,260.0,M,-33.2,M,0000,0000*57</pre>
 * <ol><li>Time (143503.000)</li>
 * <li>Latitude (4002.1083)</li>
 * <li>Latitude Direction (N)</li>
 * <li>Longitude (08309.5833)</li>
 * <li>Longitude Direction (W)</li>
 * <li>Fix Quality - 0=Invalid, 1=GPS fix, 2=DGPS fix (2)</li>
 * <li>Number of Sattelites (08)</li>
 * <li>Horizontal Dilution of Precision (HDOP) - Relative accuracy of horizontal position (1.18)</li>
 * <li>Altitude (260.0)</li>
 * <li>Altitude UoM - M=Meters (M)</li>
 * <li>Height of geoid above WGS84 ellipsoid (-33.2)</li>
 * <li>Height of geoid above WGS84 ellipsoid UoM (M)</li>
 * <li>Time since last DGPS update (0000)</li>
 * <li>DGPS reference station id (0000)</li>
 * <li>Checksum (*57)</li></ol>
 */
public interface GGASentence extends PositionSentence, TimeSentence {

  /** Altitude presented in meters. */
  char ALT_UNIT_METERS = 'M';

  /** Altitude presented in feet. */
  char ALT_UNIT_FEET = 'f';




  /**
   * Get antenna altitude above mean sea level.
   * 
   * @return Altitude value
   * 
   * @throws DataNotAvailableException If the data is not available.
   * @throws ParseException If the field contains unexpected or illegal value.
   */
  double getAltitude();




  /**
   * Gets the altitude units, meters or feet.
   * 
   * @return Units enum
   * 
   * @throws DataNotAvailableException If the data is not available.
   * @throws ParseException If the field contains unexpected or illegal value.
   */
  Units getAltitudeUnits();




  /**
   * Gets the age of differential GPS data (DGPS).
   * 
   * @return Seconds since last valid RTCM transmission
   * 
   * @throws DataNotAvailableException If the data is not available.
   * @throws ParseException If the field contains unexpected or illegal value.
   */
  double getDgpsAge();




  /**
   * Gets the ID of DGPS station.
   * 
   * @return Station ID (0000-1024)
   * 
   * @throws DataNotAvailableException If the data is not available.
   * @throws ParseException If the field contains unexpected or illegal value.
   */
  String getDgpsStationId();




  /**
   * Get the GPS fix quality.
   * 
   * @return GpsFixQuality enum
   * 
   * @throws DataNotAvailableException If the data is not available.
   * @throws ParseException If the field contains unexpected or illegal value.
   */
  GpsFixQuality getFixQuality();




  /**
   * Get height/separation of geoid above WGS84 ellipsoid, i.e. difference
   * between WGS-84 earth ellipsoid and mean sea level. 
   * 
   * <p>Negative values are below WGS-84 ellipsoid.
   * 
   * @return Height value
   * 
   * @throws DataNotAvailableException If the data is not available.
   * @throws ParseException If the field contains unexpected or illegal value.
   */
  double getGeoidalHeight();




  /**
   * Get units of height above geoid.
   * 
   * @return Units of geoidal height value
   * 
   * @throws DataNotAvailableException If the data is not available.
   * @throws ParseException If the field contains unexpected or illegal value.
   */
  Units getGeoidalHeightUnits();




  /**
   * Get the horizontal dilution of precision (HDOP), i.e. the relative
   * accuracy of horizontal position.
   * 
   * @return Horizontal dilution
   * 
   * @throws DataNotAvailableException If the data is not available.
   * @throws ParseException If the field contains unexpected or illegal value.
   */
  double getHorizontalDOP();




  /**
   * Get the number of active satellites in use.
   * 
   * @return Number of satellites
   * 
   * @throws DataNotAvailableException If the data is not available.
   * @throws ParseException If the field contains unexpected or illegal value.
   */
  int getSatelliteCount();




  /**
   * Set the number of active satellites in use.
   * 
   * @param count Number of satellites
   */
  void setSatelliteCount( int count );




  /**
   * Set the antenna altitude.
   * 
   * @param alt Altitude to set
   */
  void setAltitude( double alt );




  /**
   * Sets the unit of altitude.
   * 
   * @param unit Units to set
   */
  void setAltitudeUnits( Units unit );




  /**
   * Sets the age of differential GPS data (DGPS).
   * 
   * @param age Seconds since last valid RTCM transmission to set.
   */
  void setDgpsAge( double age );




  /**
   * Sets the ID of DGPS station.
   * 
   * @param id Station ID to set
   */
  void setDgpsStationId( String id );




  /**
   * Sets the GPS fix quality.
   * 
   * @param quality Fix quality to set
   */
  void setFixQuality( GpsFixQuality quality );




  /**
   * Set height/separation of geoid above WGS84 ellipsoid, i.e. difference
   * between WGS-84 earth ellipsoid and mean sea level. 
   * 
   * <p>Negative values are below WGS-84 ellipsoid.
   * 
   * @param height Height value to set
   */
  void setGeoidalHeight( double height );




  /**
   * Get unit of height above geoid.
   * 
   * @param unit Unit to set
   */
  void setGeoidalHeightUnits( Units unit );




  /**
   * Set the horizontal dilution of precision (HDOP), i.e. the relative
   * accuracy of horizontal position.
   * 
   * @param hdop Horizontal dilution
   */
  void setHorizontalDOP( double hdop );

}
