package coyote.commons.network.mqtt.protocol;

/**
 * Represents a Multi-Byte Integer (MBI), as defined by the MQTT V3
 * specification.
 */
public class MultiByteInteger {
  private final long value;
  private final int length;




  public MultiByteInteger( final long value ) {
    this( value, -1 );
  }




  public MultiByteInteger( final long value, final int length ) {
    this.value = value;
    this.length = length;
  }




  /**
   * Returns the number of bytes read when decoding this MBI.
   */
  public int getEncodedLength() {
    return length;
  }




  /**
   * Returns the value of this MBI.
   */
  public long getValue() {
    return value;
  }
}
