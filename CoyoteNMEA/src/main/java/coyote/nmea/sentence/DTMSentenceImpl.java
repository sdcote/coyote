package coyote.nmea.sentence;

import coyote.nmea.SentenceId;
import coyote.nmea.TalkerId;


/**
 * DTM sentence implementation.
 */
class DTMSentenceImpl extends AbstractSentence implements DTMSentence {

  private static final int DATUM_CODE = 0;
  private static final int DATUM_SUBCODE = 1;
  private static final int LATITUDE_OFFSET = 2;
  private static final int LAT_OFFSET_HEMISPHERE = 3;
  private static final int LONGITUDE_OFFSET = 4;
  private static final int LON_OFFSET_HEMISPHERE = 5;
  private static final int ALTITUDE_OFFSET = 6;
  private static final int DATUM_NAME = 7;




  /**
   * Creates a new instance of a DTM sentence.
   */
  public DTMSentenceImpl( String nmea ) {
    super( nmea, SentenceId.DTM );
  }




  /**
   * Creates a new instance of a DTM sentence with no data.
   */
  public DTMSentenceImpl( TalkerId talker ) {
    super( talker, SentenceId.DTM, 8 );
  }




  /**
   * @see coyote.nmea.sentence.DTMSentence#getAltitudeOffset()
   */
  @Override
  public double getAltitudeOffset() {
    return getDoubleValue( ALTITUDE_OFFSET );
  }




  /**
   * @see coyote.nmea.sentence.DTMSentence#getDatumCode()
   */
  @Override
  public String getDatumCode() {
    return getStringValue( DATUM_CODE );
  }




  /**
   * @see coyote.nmea.sentence.DTMSentence#getDatumSubCode()
   */
  @Override
  public String getDatumSubCode() {
    return getStringValue( DATUM_SUBCODE );
  }




  /**
   * @see coyote.nmea.sentence.DTMSentence#getLatitudeOffset()
   */
  @Override
  public double getLatitudeOffset() {
    return getDoubleValue( LATITUDE_OFFSET );
  }




  /**
   * @see coyote.nmea.sentence.DTMSentence#getLongitudeOffset()
   */
  @Override
  public double getLongitudeOffset() {
    return getDoubleValue( LONGITUDE_OFFSET );
  }




  /**
   * @see coyote.nmea.sentence.DTMSentence#getName()
   */
  @Override
  public String getName() {
    return getStringValue( DATUM_NAME );
  }




  /**
   * @see coyote.nmea.sentence.DTMSentence#setDatumCode(java.lang.String)
   */
  @Override
  public void setDatumCode( String code ) {
    setStringValue( DATUM_CODE, code );
  }




  /**
   * @see coyote.nmea.sentence.DTMSentence#setDatumSubCode(java.lang.String)
   */
  @Override
  public void setDatumSubCode( String code ) {
    setStringValue( DATUM_SUBCODE, code );
  }




  /**
   * @see coyote.nmea.sentence.DTMSentence#setLatitudeOffset(double)
   */
  @Override
  public void setLatitudeOffset( double offset ) {
    setDoubleValue( LATITUDE_OFFSET, offset, 1, 4 );
    if ( offset < 0 ) {
      setCharValue( LAT_OFFSET_HEMISPHERE, 'S' );
    } else {
      setCharValue( LAT_OFFSET_HEMISPHERE, 'N' );
    }
  }




  /**
   * @see coyote.nmea.sentence.DTMSentence#setLongitudeOffset(double)
   */
  @Override
  public void setLongitudeOffset( double offset ) {
    setDoubleValue( LONGITUDE_OFFSET, offset, 1, 4 );
    if ( offset < 0 ) {
      setCharValue( LON_OFFSET_HEMISPHERE, 'W' );
    } else {
      setCharValue( LON_OFFSET_HEMISPHERE, 'E' );
    }
  }




  /**
   * @see coyote.nmea.sentence.DTMSentence#setName(java.lang.String)
   */
  @Override
  public void setName( String name ) {
    setStringValue( DATUM_NAME, name );
  }

}
