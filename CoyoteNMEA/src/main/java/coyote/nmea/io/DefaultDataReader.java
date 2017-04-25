package coyote.nmea.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;


/**
 * The default data reader implementation using InputStream as data source.
 */
public class DefaultDataReader extends AbstractDataReader implements DataReader {
  private final BufferedReader input;




  /**
   * Creates a new instance of DefaultDataReader.
   * 
   * @param source InputStream to be used as data source.
   * @param parent SentenceReader dispatching events for this reader.
   */
  public DefaultDataReader( InputStream source, SentenceReader parent ) {
    super( parent );
    InputStreamReader isr = new InputStreamReader( source );
    this.input = new BufferedReader( isr );
  }




  /**
   * @see coyote.nmea.io.DataReader#read()
   */
  @Override
  public String read() throws IOException {
    return input.readLine();
  }




  /**
   * @see coyote.nmea.io.DataReader#isReady()
   */
  @Override
  public boolean isReady() throws IOException {
    return input.ready();
  }

}
