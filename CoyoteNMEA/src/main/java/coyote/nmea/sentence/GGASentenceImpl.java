package coyote.nmea.sentence;

import coyote.nmea.GpsFixQuality;
import coyote.nmea.NMEATime;
import coyote.nmea.ParseException;
import coyote.nmea.Position;
import coyote.nmea.SentenceId;
import coyote.nmea.TalkerId;
import coyote.nmea.Units;


/**
 * GGA sentence implementation.
 */
class GGASentenceImpl extends AbstractPositionSentence implements GGASentence {

  private static final int UTC_TIME = 0;
  private static final int LATITUDE = 1;
  private static final int LAT_HEMISPHERE = 2;
  private static final int LONGITUDE = 3;
  private static final int LON_HEMISPHERE = 4;
  private static final int FIX_QUALITY = 5;
  private static final int SATELLITES_IN_USE = 6;
  private static final int HORIZONTAL_DILUTION = 7;
  private static final int ALTITUDE = 8;
  private static final int ALTITUDE_UNITS = 9;
  private static final int GEOIDAL_HEIGHT = 10;
  private static final int HEIGHT_UNITS = 11;
  private static final int DGPS_AGE = 12;
  private static final int DGPS_STATION_ID = 13;




  /**
   * Creates a new instance of GGA sentence.
   * 
   * @param nmea GGA sentence String.
   * 
   * @throws IllegalArgumentException If the specified sentence is invalid or
   *         not a GGA sentence.
   */
  public GGASentenceImpl( String nmea ) {
    super( nmea, SentenceId.GGA );
  }




  /**
   * Creates an empty GSA sentence.
   * 
   * @param talker TalkerId to set
   */
  public GGASentenceImpl( TalkerId talker ) {
    super( talker, SentenceId.GGA, 14 );
  }




  /**
   * @see coyote.nmea.sentence.GGASentence#getAltitude()
   */
  @Override
  public double getAltitude() {
    return getDoubleValue( ALTITUDE );
  }




  /**
   * @see coyote.nmea.sentence.GGASentence#getAltitudeUnits()
   */
  @Override
  public Units getAltitudeUnits() {
    char ch = getCharValue( ALTITUDE_UNITS );
    if ( ch != ALT_UNIT_METERS && ch != ALT_UNIT_FEET ) {
      String msg = "Invalid altitude unit indicator: %s";
      throw new ParseException( String.format( msg, ch ) );
    }
    return Units.valueOf( ch );
  }




  /**
   * @see coyote.nmea.sentence.GGASentence#getDgpsAge()
   */
  @Override
  public double getDgpsAge() {
    return getDoubleValue( DGPS_AGE );
  }




  /**
   * @see coyote.nmea.sentence.GGASentence#getDgpsStationId()
   */
  @Override
  public String getDgpsStationId() {
    return getStringValue( DGPS_STATION_ID );
  }




  /**
   * @see coyote.nmea.sentence.GGASentence#getFixQuality()
   */
  @Override
  public GpsFixQuality getFixQuality() {
    return GpsFixQuality.valueOf( getIntValue( FIX_QUALITY ) );
  }




  /**
   * @see coyote.nmea.sentence.GGASentence#getGeoidalHeight()
   */
  @Override
  public double getGeoidalHeight() {
    return getDoubleValue( GEOIDAL_HEIGHT );
  }




  /**
   * @see coyote.nmea.sentence.GGASentence#getGeoidalHeightUnits()
   */
  @Override
  public Units getGeoidalHeightUnits() {
    return Units.valueOf( getCharValue( HEIGHT_UNITS ) );
  }




  /**
   * @see coyote.nmea.sentence.GGASentence#getHorizontalDOP()
   */
  @Override
  public double getHorizontalDOP() {
    return getDoubleValue( HORIZONTAL_DILUTION );
  }




  /**
   * @see coyote.nmea.PositionSentence#getPosition()
   */
  @Override
  public Position getPosition() {

    Position pos = parsePosition( LATITUDE, LAT_HEMISPHERE, LONGITUDE, LON_HEMISPHERE );

    if ( hasValue( ALTITUDE ) && hasValue( ALTITUDE_UNITS ) ) {
      double alt = getAltitude();
      if ( getAltitudeUnits().equals( Units.FEET ) ) {
        alt = ( alt / 0.3048 );
      }
      pos.setAltitude( alt );
    }

    return pos;
  }




  /**
   * @see coyote.nmea.sentence.GGASentence#getSatelliteCount()
   */
  @Override
  public int getSatelliteCount() {
    return getIntValue( SATELLITES_IN_USE );
  }




  /**
   * @see coyote.nmea.sentence.TimeSentence#getTime()
   */
  @Override
  public NMEATime getTime() {
    String str = getStringValue( UTC_TIME );
    return new NMEATime( str );
  }




  /**
   * @see coyote.nmea.sentence.GGASentence#setAltitude(double)
   */
  @Override
  public void setAltitude( double alt ) {
    setDoubleValue( ALTITUDE, alt, 1, 1 );
  }




  /**
   * @see coyote.nmea.sentence.GGASentence#setAltitudeUnits(coyote.nmea.Units)
   */
  @Override
  public void setAltitudeUnits( Units unit ) {
    setCharValue( ALTITUDE_UNITS, unit.toChar() );
  }




  /**
   * @see coyote.nmea.sentence.GGASentence#setDgpsAge(double)
   */
  @Override
  public void setDgpsAge( double age ) {
    setDoubleValue( DGPS_AGE, age, 1, 1 );
  }




  /**
   * @see coyote.nmea.sentence.GGASentence#setDgpsStationId(java.lang.String)
   */
  @Override
  public void setDgpsStationId( String id ) {
    setStringValue( DGPS_STATION_ID, id );
  }




  /**
   * @see coyote.nmea.sentence.GGASentence#setFixQuality(coyote.nmea.GpsFixQuality)
   */
  @Override
  public void setFixQuality( GpsFixQuality quality ) {
    setIntValue( FIX_QUALITY, quality.toInt() );
  }




  /**
   * @see coyote.nmea.sentence.GGASentence#setGeoidalHeight(double)
   */
  @Override
  public void setGeoidalHeight( double height ) {
    setDoubleValue( GEOIDAL_HEIGHT, height, 1, 1 );
  }




  /**
   * @see coyote.nmea.sentence.GGASentence#setGeoidalHeightUnits(coyote.nmea.Units)
   */
  @Override
  public void setGeoidalHeightUnits( Units unit ) {
    setCharValue( HEIGHT_UNITS, unit.toChar() );
  }




  /**
   * @see coyote.nmea.sentence.GGASentence#setHorizontalDOP(double)
   */
  @Override
  public void setHorizontalDOP( double hdop ) {
    setDoubleValue( HORIZONTAL_DILUTION, hdop, 1, 1 );
  }




  /**
   * @see coyote.nmea.PositionSentence#setPosition(coyote.nmea.Position)
   */
  @Override
  public void setPosition( Position pos ) {
    setPositionValues( pos, LATITUDE, LAT_HEMISPHERE, LONGITUDE, LON_HEMISPHERE );

    setAltitude( pos.getAltitude() );
    setAltitudeUnits( Units.METER );
  }




  /**
   * @see coyote.nmea.sentence.TimeSentence#setTime(coyote.nmea.NMEATime)
   */
  @Override
  public void setTime( NMEATime t ) {
    setStringValue( UTC_TIME, t.toString() );
  }




  /**
   * @see coyote.nmea.sentence.GGASentence#setSatelliteCount(int)
   */
  @Override
  public void setSatelliteCount( int count ) {
    setIntValue( SATELLITES_IN_USE, count );
  }

}
