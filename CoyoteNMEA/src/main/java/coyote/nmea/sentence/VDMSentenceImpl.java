package coyote.nmea.sentence;

import coyote.nmea.SentenceId;
import coyote.nmea.TalkerId;


/**
 * AIS VDM sentence parser, contains only the NMEA layer. The actual payload
 * message is parsed by AIS message parsers.
 */
class VDMSentenceImpl extends AbstractAISSentence {

  /**
   * Creates a new instance of VDMParser.
   * 
   * @param nmea NMEA sentence String.
   */
  public VDMSentenceImpl( String nmea ) {
    super( nmea, SentenceId.VDM );
  }




  /**
   * Creates a new empty VDMParser.
   * 
   * @param talker TalkerId to set
   */
  public VDMSentenceImpl( TalkerId talker ) {
    super( talker, SentenceId.VDM );
  }

}
