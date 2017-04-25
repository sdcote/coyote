package coyote.nmea.sentence;

import coyote.nmea.SentenceId;
import coyote.nmea.TalkerId;


/**
 * HDM sentence implementation.
 */
class HDMSentenceImpl extends AbstractSentence implements HDMSentence {

  private static final int HEADING = 0;
  private static final int MAGN_INDICATOR = 1;




  /**
   * Creates a new HDM sentence.
   * 
   * @param nmea HDM sentence String
   */
  public HDMSentenceImpl( String nmea ) {
    super( nmea, SentenceId.HDM );
  }




  /**
   * Creates a new empty HDM sentence.
   * 
   * @param talker Talker id to set
   */
  public HDMSentenceImpl( TalkerId talker ) {
    super( talker, SentenceId.HDM, 2 );
    setCharValue( MAGN_INDICATOR, 'M' );
  }




  /**
   * @see coyote.nmea.sentence.HeadingSentence#getHeading()
   */
  @Override
  public double getHeading() {
    return getDoubleValue( HEADING );
  }




  /**
   * @see coyote.nmea.sentence.HeadingSentence#isTrue()
   */
  @Override
  public boolean isTrue() {
    return false;
  }




  /**
   * @see coyote.nmea.sentence.HeadingSentence#setHeading(double)
   */
  @Override
  public void setHeading( double hdm ) {
    setDegreesValue( HEADING, hdm );
  }

}
