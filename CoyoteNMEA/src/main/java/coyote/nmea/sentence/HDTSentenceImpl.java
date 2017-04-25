package coyote.nmea.sentence;

import coyote.nmea.SentenceId;
import coyote.nmea.TalkerId;


/**
 * HDT sentence implementation.
 */
class HDTSentenceImpl extends AbstractSentence implements HDTSentence {

  private static final int HEADING = 0;
  private static final int TRUE_INDICATOR = 1;




  /**
   * Creates a new HDT sentence.
   * 
   * @param nmea HDT sentence String to parse.
   */
  public HDTSentenceImpl( String nmea ) {
    super( nmea, SentenceId.HDT );
  }




  /**
   * Creates a new empty HDT sentence.
   * 
   * @param talker Talker id to set
   */
  public HDTSentenceImpl( TalkerId talker ) {
    super( talker, SentenceId.HDT, 2 );
    setCharValue( TRUE_INDICATOR, 'T' );
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
    return true;
  }




  /**
   * @see coyote.nmea.sentence.HeadingSentence#setHeading(double)
   */
  @Override
  public void setHeading( double hdt ) {
    setDegreesValue( HEADING, hdt );
  }

}
