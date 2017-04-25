package coyote.nmea.sentence;

import coyote.nmea.SentenceId;
import coyote.nmea.TalkerId;
import coyote.nmea.Units;


/**
 * MTW Sentence implementation.
 */
class MTWSentenceImpl extends AbstractSentence implements MTWSentence {

  private static final int TEMPERATURE = 0;
  private static final int UNIT_INDICATOR = 1;




  /**
   * Creates new instance of MTWParser with specified sentence.
   * 
   * @param nmea MTW sentence string
   */
  public MTWSentenceImpl( String nmea ) {
    super( nmea );
  }




  /**
   * Creates new MTW parse without data.
   * 
   * @param tid TalkerId to set
   */
  public MTWSentenceImpl( TalkerId tid ) {
    super( tid, SentenceId.MTW, 2 );
    setCharValue( UNIT_INDICATOR, Units.CELSIUS.toChar() );
  }




  /**
   * @see coyote.nmea.sentence.MTWSentence#getTemperature()
   */
  @Override
  public double getTemperature() {
    return getDoubleValue( TEMPERATURE );
  }




  /**
   * @see coyote.nmea.sentence.MTWSentence#setTemperature(double)
   */
  @Override
  public void setTemperature( double temp ) {
    setDoubleValue( TEMPERATURE, temp, 1, 2 );
  }

}