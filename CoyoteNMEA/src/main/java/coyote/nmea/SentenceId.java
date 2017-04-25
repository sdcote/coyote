package coyote.nmea;

/**
 * Defines the supported NMEA 0831 sentence types. 
 * 
 * <p>Sentence address field is a combination of talker and sentence IDs, for 
 * example GPBOD, GPGGA or GPGGL.
 */
public enum SentenceId {

  /** Autopilot sentence "B"; xte, bearings and heading toward destination */
  APB,
  /** Bearing Origin to Destination */
  BOD,
  /** Current */
  CUR,
  /** Depth of water below transducer; in meters, feet and fathoms */
  DBT,
  /** Depth of water below transducer; in meters. */
  DPT,
  /** Datum reference. */
  DTM,
  /** Global Positioning System fix data */
  GGA,
  /** Geographic position (latitude/longitude) */
  GLL,
  /** Dilution of precision (DOP) of GPS fix and active satellites */
  GSA,
  /** Detailed satellite data */
  GSV,
  /** Vessel heading in degrees with magnetic variation and deviation. */
  HDG,
  /** Vessel heading in degrees with respect to true north. */
  HDM,
  /** Vessel heading in degrees true */
  HDT,
  /** Relative and absolute humidity with dew point */
  MHU,
  /** Barometric pressure in inches of mercury and bars. */
  MMB,
  /** Air temperature in degrees centigrade (Celsius). */
  MTA,
  /** Water temperature in degrees centigrade (Celsius). */
  MTW,
  /** Wind speed and angle */
  MWV,
  /** Recommended minimum navigation information */
  RMB,
  /** Recommended minimum specific GPS/TRANSIT data */
  RMC,
  /** Rate of Turn */
  ROT,
  /** Revolutions measured from engine or shaft. */
  RPM,
  /** Rudder angle, measured in degrees */
  RSA,
  /** Route data and waypoint list */
  RTE,
  /** Tracked target */
  TTM,
  /** Dual ground/water speed and stern ground/water speed. */
  VBW,
  /** AIS - Received data from other vessels */
  VDM,
  /** AIS - Own vessel data */
  VDO,
  /** Set and drift, direction and speed of current. */
  VDR,
  /** Distance traveled through water, cumulative and since reset. */
  VLW,
  /** Track made good and ground speed */
  VTG,
  /** Water speed and heading */
  VHW,
  /** Waypoint location (latitude/longitude) */
  WPL,
  /** Relative Wind Speed and Angle */
  VWR,
  /** True Wind Speed and Angle */
  VWT,
  /** Transducer measurements (sensor data) */
  XDR,
  /** Cross-track error, measured */
  XTE,
  /** Meteorological Composite  */
  MDA,
  /** Wind speed and direction */
  MWD,
  /** UTC time and date with local time zone offset */
  ZDA,
  /** Unknown Sentence type - one of our own crafting */
  UNK;

  /**
   * Parses the sentence id from specified sentence String and returns a
   * corresponding <code>SentenceId</code> enum (assuming it exists).
   *
   * @param nmea Sentence String
   * 
   * @return the SentenceId for the given sentence string
   * 
   * @throws IllegalArgumentException If specified String is not valid sentence
   */
  public static SentenceId parse( final String nmea ) {
    final String sid = parseStr( nmea );
    return SentenceId.valueOf( sid );
  }




  /**
   * Parses the sentence id from specified sentence String and returns it as
   * String.
   *
   * @param nmea Sentence String
   * 
   * @return the sentence identifier, e.g. "GGA" or "GLL"
   * 
   * @throws IllegalArgumentException If specified String is not recognized as NMEA sentence
   */
  public static String parseStr( final String nmea ) {

    if ( !SentenceValidator.isSentence( nmea ) ) {
      throw new IllegalArgumentException( "String is not a sentence" );
    }

    String id = null;
    if ( nmea.startsWith( "$P" ) ) {
      id = nmea.substring( 2, nmea.indexOf( ',' ) );
    } else {
      id = nmea.substring( 3, nmea.indexOf( ',' ) );
    }
    return id;
  }

}
