package coyote.nmea;

import coyote.nmea.TalkerId;
import coyote.nmea.sentence.AbstractSentence;


/**
 * Invalid sentence implementation, no constructor with String param.
 */
public class BARSentenceImpl extends AbstractSentence {

  /**
   * Constructor
   */
  public BARSentenceImpl() {
    super( TalkerId.GP, "BAR", 5 );
  }

}
