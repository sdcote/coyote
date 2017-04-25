package coyote.nmea;

import coyote.nmea.io.SentenceReader;


/**
 * Base interface for listening to SentenceEvents.
 * 
 * <p>When a {@link SentenceReader} reads in a valid sentence, it passes the 
 * sentence to all registered listeners by calling the {@link 
 * #onRead(SentenceEvent)} callback method. The {@link SentenceEvent} contains 
 * reference to the sentence, reader and time stamp of the event.    
 */
public interface SentenceListener {

  /**
   * Called by {@link SentenceReader} when a single NMEA 0183 sentence has been 
   * read and parsed from the data stream. By default, only supported sentences 
   * defined in {@link SentenceId} are dispatched.
   * 
   * @param event SentenceEvent containing the data.
   */
  void onRead( SentenceEvent event );

}
