package coyote.nmea.io;

import java.io.IOException;


/**
 * It is possible to have many different types of DataReaders; this interface 
 * abstracts them all.
 * 
 * <p>This is used by the {@link SentenceReader} and {@link SentenceMonitor} to 
 * acquire data from some medium.
 * 
 * <p>It is possible that a DataReader may listen to a message bus or for web 
 * server POST messages so there may be a respective BusReader and WebReader 
 * class which handles them accordingly.
 */
public interface DataReader {

  /**
   * Read one line from the data source.
   * 
   * <p>This call is expected to block while waiting for a CR, LF or CR/LF to 
   * be read. Calling code should handle blocking gracefully. 
   * 
   * @return the line of data read or {@code null} if the end of the stream has 
   *         been reached.
   *
   * @throws IOException
   */
  public String read() throws IOException;




  /**
   * Determine if the reader has enough data in its buffer for a complete line 
   * to be read.
   * 
   * <p>This can be used to prevent the reader from blocking on call to {@link 
   * #read()}.
   * 
   * @return true if the next call to read will not block, false otherwise
   * 
   * @throws IOException
   */
  public boolean isReady() throws IOException;

}
