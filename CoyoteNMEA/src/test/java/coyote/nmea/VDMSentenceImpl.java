package coyote.nmea;

import coyote.nmea.TalkerId;
import coyote.nmea.sentence.AbstractSentence;


/**
 * Dummy sentence implementation simulating AIVDM sentence with alternative 
 * begin character, for testing the inheritance of AbstractSentence and custom 
 * implementation registering in SentenceParser.
 */
public class VDMSentenceImpl extends AbstractSentence {

  public VDMSentenceImpl( final String s ) {
    // just like any other parser, begin char comes in String param
    super( s, "VDM" );
  }




  public VDMSentenceImpl( final TalkerId tid ) {
    // alternative begin char is set here for empty sentences
    super( '!', tid, "VDM", 3 );
  }




  public String getValueA() {
    return getStringValue( 0 );
  }




  public String getValueB() {
    return getStringValue( 1 );
  }




  public String getValueC() {
    return getStringValue( 2 );
  }
}
