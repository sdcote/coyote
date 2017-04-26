package coyote.commons.network.mqtt.protocol;

import java.io.IOException;
import java.io.InputStream;


/**
 * An input stream that counts the bytes read from it.
 */
public class CountingInputStream extends InputStream {
  private final InputStream in;
  private int counter;




  /**
   * Constructs a new <code>CountingInputStream</code> wrapping the supplied
   * input stream.
   */
  public CountingInputStream( final InputStream in ) {
    this.in = in;
    counter = 0;
  }




  /**
   * Returns the number of bytes read since the last reset.
   */
  public int getCounter() {
    return counter;
  }




  @Override
  public int read() throws IOException {
    final int i = in.read();
    if ( i != -1 ) {
      counter++;
    }
    return i;
  }




  /**
   * Resets the counter to zero.
   */
  public void resetCounter() {
    counter = 0;
  }
}
