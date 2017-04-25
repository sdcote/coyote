package coyote.nmea.sentence;

import coyote.nmea.SentenceId;
import coyote.nmea.TalkerId;


/**
 * AIS sentence base class implementation. 
 * 
 * <p>Handles only the NMEA layer for VDM and VDO sentences. The actual payload 
 * message is parsed by AIS message implementations.
 */
abstract class AbstractAISSentence extends AbstractSentence implements AISSentence {

  // NMEA message fields
  private static final int NUMBER_OF_FRAGMENTS = 0;
  private static final int FRAGMENT_NUMBER = 1;
  private static final int MESSAGE_ID = 2;
  private static final int RADIO_CHANNEL = 3;
  private static final int PAYLOAD = 4;
  private static final int FILL_BITS = 5;




  /**
   * Creates a new instance of an AIS sentence implementation.
   *
   * @param nmea NMEA sentence String.
   */
  public AbstractAISSentence( String nmea, SentenceId sid ) {
    super( nmea, sid );
  }




  /**
   * Creates a new empty instance of an AIS sentence implementation.
   *
   * @param tid TalkerId to set
   * @param sid
   */
  public AbstractAISSentence( TalkerId tid, SentenceId sid ) {
    super( '!', tid, sid, 6 );
  }




  /**
   * @see coyote.nmea.sentence.AISSentence#getNumberOfFragments()
   */
  @Override
  public int getNumberOfFragments() {
    return getIntValue( NUMBER_OF_FRAGMENTS );
  }




  /**
   * @see coyote.nmea.sentence.AISSentence#getFragmentNumber()
   */
  @Override
  public int getFragmentNumber() {
    return getIntValue( FRAGMENT_NUMBER );
  }




  /**
   * @see coyote.nmea.sentence.AISSentence#getMessageId()
   */
  @Override
  public String getMessageId() {
    return getStringValue( MESSAGE_ID );
  }




  /**
   * @see coyote.nmea.sentence.AISSentence#getRadioChannel()
   */
  @Override
  public String getRadioChannel() {
    return getStringValue( RADIO_CHANNEL );
  }




  /**
   * @see coyote.nmea.sentence.AISSentence#getPayload()
   */
  @Override
  public String getPayload() {
    return getStringValue( PAYLOAD );
  }




  /**
   * @see coyote.nmea.sentence.AISSentence#getFillBits()
   */
  @Override
  public int getFillBits() {
    return getIntValue( FILL_BITS );
  }




  /**
   * @see coyote.nmea.sentence.AISSentence#isFragmented()
   */
  @Override
  public boolean isFragmented() {
    return getNumberOfFragments() > 1;
  }




  /**
   * @see coyote.nmea.sentence.AISSentence#isFirstFragment()
   */
  @Override
  public boolean isFirstFragment() {
    return getFragmentNumber() == 1;
  }




  /**
   * @see coyote.nmea.sentence.AISSentence#isLastFragment()
   */
  @Override
  public boolean isLastFragment() {
    return getNumberOfFragments() == getFragmentNumber();
  }




  /**
   * @see coyote.nmea.sentence.AISSentence#isPartOfMessage(coyote.nmea.sentence.AISSentence)
   */
  @Override
  public boolean isPartOfMessage( AISSentence line ) {
    if ( getNumberOfFragments() == line.getNumberOfFragments() && getFragmentNumber() < line.getFragmentNumber() ) {

      if ( getFragmentNumber() + 1 == line.getFragmentNumber() ) {
        return ( getRadioChannel().equals( line.getRadioChannel() ) || getMessageId().equals( line.getMessageId() ) );
      } else {
        return ( getRadioChannel().equals( line.getRadioChannel() ) && getMessageId().equals( line.getMessageId() ) );
      }
    } else {
      return false;
    }
  }

}
