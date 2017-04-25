package coyote.nmea;

import java.util.EventObject;


/**
 * Sentence events occur when a valid NMEA 0183 sentence has been read.
 */
public class SentenceEvent extends EventObject {

  private static final long serialVersionUID = -197788137078342419L;
  private final long timestamp = System.currentTimeMillis();
  private final Sentence sentence;




  /**
   * Creates a new SentenceEvent object.
   * 
   * @param src Object that fired the event
   * @param s Sentence that triggered the event
   * 
   * @throws IllegalArgumentException if given sentence is null
   */
  public SentenceEvent( Object src, Sentence s ) {
    super( src );
    if ( s == null ) {
      throw new IllegalArgumentException( "Sentence cannot be null" );
    }
    this.sentence = s;
  }




  /**
   * @return the Sentence object that triggered the event.
   */
  public Sentence getSentence() {
    return sentence;
  }




  /**
   * @return system time when this event was created
   */
  public long getTimeStamp() {
    return timestamp;
  }

}
