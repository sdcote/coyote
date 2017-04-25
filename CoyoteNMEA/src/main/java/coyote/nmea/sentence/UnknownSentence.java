package coyote.nmea.sentence;

import coyote.nmea.Sentence;
import coyote.nmea.io.ExceptionListener;


/**
 * Wrapper around the abstract sentence for unknown sentence types.
 * 
 * <p>This is used to capture the valid sentences this API does not know and 
 * allows for {@link ExceptionListener} to retrieve the data for later 
 * analysis.
 */
public class UnknownSentence extends AbstractSentence implements Sentence {

  /**
   * Creates a generic sentence.
   *
   * @param nmea sentence string to parse.
   * 
   * @throws IllegalArgumentException If the given sentence is invalid.
   */
  public UnknownSentence( String nmea ) {
    super( nmea );
  }

}
