package coyote.nmea.sentence;

import coyote.nmea.SentenceId;
import coyote.nmea.TalkerId;


/**
 * MHUParser - Humidity & dew point.
 *
 * $--MHU,x.x,x.x,x.x,C*hh<CR><LF>
 */
class MHUSentenceImpl extends AbstractSentence implements MHUSentence {

  private static final int RELATIVE_HUMIDITY = 0;
  private static final int ABSOLUTE_HUMIDITY = 1;
  private static final int DEW_POINT = 2;
  private static final int DEW_POINT_UNIT = 3;




  /**
   * Constructor for parsing MHU sentence.
   *
   * @param nmea MHU sentence String
   */
  public MHUSentenceImpl( String nmea ) {
    super( nmea, SentenceId.MHU );
  }




  /**
   * Constructor for fresh MHU sentence.
   *
   * @param tid Talker ID to be used.
   */
  public MHUSentenceImpl( TalkerId tid ) {
    super( tid, SentenceId.MHU, 4 );
    setDewPointUnit( 'C' );
  }




  /**
   * @see coyote.nmea.sentence.MHUSentence#getRelativeHumidity()
   */
  @Override
  public double getRelativeHumidity() {
    return getDoubleValue( RELATIVE_HUMIDITY );
  }




  /**
   * @see coyote.nmea.sentence.MHUSentence#getAbsoluteHumidity()
   */
  @Override
  public double getAbsoluteHumidity() {
    return getDoubleValue( ABSOLUTE_HUMIDITY );
  }




  /**
   * @see coyote.nmea.sentence.MHUSentence#getDewPoint()
   */
  @Override
  public double getDewPoint() {
    return getDoubleValue( DEW_POINT );
  }




  /**
   * @see coyote.nmea.sentence.MHUSentence#getDewPointUnit()
   */
  @Override
  public char getDewPointUnit() {
    return getCharValue( DEW_POINT_UNIT );
  }




  /**
   * @see coyote.nmea.sentence.MHUSentence#setRelativeHumidity(double)
   */
  @Override
  public void setRelativeHumidity( double humidity ) {
    setDoubleValue( RELATIVE_HUMIDITY, humidity, 1, 1 );
  }




  /**
   * @see coyote.nmea.sentence.MHUSentence#setAbsoluteHumidity(double)
   */
  @Override
  public void setAbsoluteHumidity( double humidity ) {
    setDoubleValue( ABSOLUTE_HUMIDITY, humidity, 1, 1 );
  }




  /**
   * @see coyote.nmea.sentence.MHUSentence#setDewPoint(double)
   */
  @Override
  public void setDewPoint( double dewPoint ) {
    setDoubleValue( DEW_POINT, dewPoint, 1, 1 );
  }




  /**
   * @see coyote.nmea.sentence.MHUSentence#setDewPointUnit(char)
   */
  @Override
  public void setDewPointUnit( char unit ) {
    setCharValue( DEW_POINT_UNIT, unit );
  }

}
