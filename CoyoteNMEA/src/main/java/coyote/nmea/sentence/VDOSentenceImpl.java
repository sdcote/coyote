package coyote.nmea.sentence;

import coyote.nmea.SentenceId;
import coyote.nmea.TalkerId;


/**
 * AIS VDO sentence parser, contains only the NMEA layer. The actual payload
 * message is parsed by AIS message parsers.
 */
class VDOSentenceImpl extends AbstractAISSentence {

  /**
   * Creates a new instance of VDOParser.
   * 
   * @param nmea NMEA sentence String.
   */
  public VDOSentenceImpl( String nmea ) {
    super( nmea, SentenceId.VDO );
  }




  /**
   * Creates a new empty VDOParser.
   * 
   * @param talker TalkerId to set
   */
  public VDOSentenceImpl( TalkerId talker ) {
    super( talker, SentenceId.VDO );
  }
}
