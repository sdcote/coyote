package coyote.commons.network.mqtt.protocol;

import java.io.IOException;
import java.io.InputStream;


public class MultiByteArrayInputStream extends InputStream {

  private final byte[] bytesA;
  private final int offsetA;
  private final int lengthA;
  private final byte[] bytesB;
  private final int offsetB;
  private final int lengthB;

  private int pos = 0;




  public MultiByteArrayInputStream( final byte[] bytesA, final int offsetA, final int lengthA, final byte[] bytesB, final int offsetB, final int lengthB ) {
    this.bytesA = bytesA;
    this.bytesB = bytesB;
    this.offsetA = offsetA;
    this.offsetB = offsetB;
    this.lengthA = lengthA;
    this.lengthB = lengthB;
  }




  @Override
  public int read() throws IOException {
    int result = -1;
    if ( pos < lengthA ) {
      result = bytesA[offsetA + pos];
    } else if ( pos < ( lengthA + lengthB ) ) {
      result = bytesB[( offsetB + pos ) - lengthA];
    } else {
      return -1;
    }
    if ( result < 0 ) {
      result += 256;
    }
    pos++;
    return result;
  }

}
