package coyote.nmea.io;

/**
 * A listener callback interface for listening to Exceptions in Sentence 
 * Readers/Monitors. 
 */
public interface ExceptionListener {

  /**
   * Invoked by {@link SentenceReader} when error has occurred while reading
   * the data source.
   * 
   * @param e Exception that was thrown while reading data.
   */
  public void onException( Exception e );

}
