package coyote.nmea.sentence;

import coyote.nmea.SentenceId;
import coyote.nmea.TalkerId;
import coyote.nmea.Units;


/**
 * MTA sentence implementation.
 */
class MTASentenceImpl extends AbstractSentence implements MTASentence {

  private static final int TEMPERATURE = 0;
  private static final int UNIT_INDICATOR = 1;




  /**
   * Parse the given data into an instance of an MTA sentence.
   */
  public MTASentenceImpl( String nmea ) {
    super( nmea, SentenceId.MTA );
  }




  /**
   * Create an empty MTA sentence implementation.
   */
  public MTASentenceImpl( TalkerId talker ) {
    super( talker, SentenceId.MTA, 2 );
    setCharValue( UNIT_INDICATOR, Units.CELSIUS.toChar() );
  }




  /**
   * @see coyote.nmea.sentence.MTASentence#getTemperature()
   */
  @Override
  public double getTemperature() {
    return getDoubleValue( TEMPERATURE );
  }




  /**
   * @see coyote.nmea.sentence.MTASentence#setTemperature(double)
   */
  @Override
  public void setTemperature( double temp ) {
    setDoubleValue( TEMPERATURE, temp, 1, 2 );
  }

}