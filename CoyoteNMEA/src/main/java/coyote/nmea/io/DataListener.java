package coyote.nmea.io;

/**
 * Listener for all data that is not recognized as NMEA 0183.
 * 
 * <p>DataListeners are set in a {@link SentenceReader} to process anything 
 * read in which is not a sentence.
 */
public interface DataListener {

  /**
   * Invoked by {@link SentenceReader} when non-NMEA data has been read from
   * the device/data source.
   * 
   * <p>This could be anything from noise on the line to unsupported sentence 
   * types. It is most often used for debugging. 
   * 
   * @param data Data String read from the device.
   */
  void onRead( String data );

}
