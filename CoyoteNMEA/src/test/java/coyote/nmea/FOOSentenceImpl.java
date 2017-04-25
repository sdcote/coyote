package coyote.nmea;

import coyote.nmea.TalkerId;
import coyote.nmea.sentence.AbstractSentence;


/**
 * Dummy sentence implementation, for testing the inheritance of
 * SentenceParser.
 */
public class FOOSentenceImpl extends AbstractSentence implements FOOSentence {

  public FOOSentenceImpl( final String s ) {
    super( s, "FOO" );
  }




  public FOOSentenceImpl( final TalkerId tid ) {
    super( tid, "FOO", 3 );
  }




  @Override
  public String getValueA() {
    return getStringValue( 0 );
  }




  @Override
  public String getValueB() {
    return getStringValue( 1 );
  }




  @Override
  public String getValueC() {
    return getStringValue( 2 );
  }
}
