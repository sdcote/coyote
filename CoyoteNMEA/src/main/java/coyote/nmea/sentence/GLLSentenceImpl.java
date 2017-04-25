package coyote.nmea.sentence;

import coyote.nmea.DataStatus;
import coyote.nmea.NMEATime;
import coyote.nmea.Position;
import coyote.nmea.SentenceId;
import coyote.nmea.TalkerId;


/**
 * GLL Sentence implementation.
 */
class GLLSentenceImpl extends AbstractPositionSentence implements GLLSentence {

  private static final int LATITUDE = 0;
  private static final int LAT_HEMISPHERE = 1;
  private static final int LONGITUDE = 2;
  private static final int LON_HEMISPHERE = 3;
  private static final int UTC_TIME = 4;
  private static final int DATA_STATUS = 5;




  /**
   * Creates a new instance of GLL sentence.
   *
   * @param nmea GLL sentence String.
   * 
   * @throws IllegalArgumentException If the given sentence is invalid or does
   *         not contain GLL sentence.
   */
  public GLLSentenceImpl( String nmea ) {
    super( nmea, SentenceId.GLL );
  }




  /**
   * Creates GLL implementation with empty sentence.
   *
   * @param talker TalkerId to set
   */
  public GLLSentenceImpl( TalkerId talker ) {
    super( talker, SentenceId.GLL, 6 );
  }




  /**
   * @see coyote.nmea.PositionSentence#getPosition()
   */
  @Override
  public Position getPosition() {
    return parsePosition( LATITUDE, LAT_HEMISPHERE, LONGITUDE, LON_HEMISPHERE );
  }




  /**
   * @see coyote.nmea.sentence.GLLSentence#getStatus()
   */
  @Override
  public DataStatus getStatus() {
    return DataStatus.valueOf( getCharValue( DATA_STATUS ) );
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
   * @see coyote.nmea.PositionSentence#setPosition(coyote.nmea.Position)
   */
  @Override
  public void setPosition( Position pos ) {
    setPositionValues( pos, LATITUDE, LAT_HEMISPHERE, LONGITUDE, LON_HEMISPHERE );
  }




  /**
   * @see coyote.nmea.sentence.GLLSentence#setStatus(coyote.nmea.DataStatus)
   */
  @Override
  public void setStatus( DataStatus status ) {
    setCharValue( DATA_STATUS, status.toChar() );
  }




  /**
   * @see coyote.nmea.sentence.TimeSentence#setTime(coyote.nmea.NMEATime)
   */
  @Override
  public void setTime( NMEATime t ) {
    setStringValue( UTC_TIME, t.toString() );
  }

}
