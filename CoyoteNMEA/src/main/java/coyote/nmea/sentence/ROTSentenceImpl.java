package coyote.nmea.sentence;

import coyote.nmea.DataStatus;
import coyote.nmea.SentenceId;
import coyote.nmea.TalkerId;


/**
 * ROT sentence implementation.
 */
class ROTSentenceImpl extends AbstractSentence implements ROTSentence {

  private static final int RATE_OF_TURN = 0;
  private static final int STATUS = 1;




  /**
   * Creates a new ROT sentence .
   * 
   * @param nmea ROT sentence String to parse.
   */
  public ROTSentenceImpl( String nmea ) {
    super( nmea, SentenceId.ROT );
  }




  /**
   * Creates a new empty ROT sentence.
   * 
   * @param talker Talker id to set
   */
  public ROTSentenceImpl( TalkerId talker ) {
    super( talker, SentenceId.ROT, 2 );
  }




  /**
   * @see coyote.nmea.sentence.ROTSentence#getRateOfTurn()
   */
  @Override
  public double getRateOfTurn() {
    return getDoubleValue( RATE_OF_TURN );
  }




  /**
   * @see coyote.nmea.sentence.ROTSentence#getStatus()
   */
  @Override
  public DataStatus getStatus() {
    return DataStatus.valueOf( getCharValue( STATUS ) );
  }




  /**
   * @see coyote.nmea.sentence.ROTSentence#setRateOfTurn(double)
   */
  @Override
  public void setRateOfTurn( double rot ) {
    setDegreesValue( RATE_OF_TURN, rot );
  }




  /**
   * @see coyote.nmea.sentence.ROTSentence#setStatus(coyote.nmea.DataStatus)
   */
  @Override
  public void setStatus( DataStatus status ) {
    setCharValue( STATUS, status.toChar() );
  }

}
