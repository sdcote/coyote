package coyote.commons.network.mqtt.protocol;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import coyote.commons.network.mqtt.MqttException;


/**
 * An UNSUBSCRIBE message is sent by the client to the server to unsubscribe 
 * from named topics.
 */
public class MqttUnsubscribe extends AbstractMessage {

  private final String[] names;
  private int count;




  /**
   * Constructor for an on the wire MQTT un-subscribe message
   * 
   * @param info
   * @param data
   * @throws IOException
   */
  public MqttUnsubscribe( final byte info, final byte[] data ) throws IOException {
    super( AbstractMessage.UNSUBSCRIBE );
    final ByteArrayInputStream bais = new ByteArrayInputStream( data );
    final DataInputStream dis = new DataInputStream( bais );
    msgId = dis.readUnsignedShort();

    count = 0;
    names = new String[10];
    boolean end = false;
    while ( !end ) {
      try {
        names[count] = decodeUTF8( dis );
      } catch ( final Exception e ) {
        end = true;
      }
    }
    dis.close();
  }




  /**
   * Constructs an MqttUnsubscribe
   */
  public MqttUnsubscribe( final String[] names ) {
    super( AbstractMessage.UNSUBSCRIBE );
    this.names = names;
  }




  @Override
  protected byte getMessageInfo() {
    return (byte)( 2 | ( duplicate ? 8 : 0 ) );
  }




  @Override
  public byte[] getPayload() throws MqttException {
    try {
      final ByteArrayOutputStream baos = new ByteArrayOutputStream();
      final DataOutputStream dos = new DataOutputStream( baos );
      for ( final String name : names ) {
        encodeUTF8( dos, name );
      }
      dos.flush();
      return baos.toByteArray();
    } catch ( final IOException ex ) {
      throw new MqttException( ex );
    }
  }




  @Override
  protected byte[] getVariableHeader() throws MqttException {
    try {
      final ByteArrayOutputStream baos = new ByteArrayOutputStream();
      final DataOutputStream dos = new DataOutputStream( baos );
      dos.writeShort( msgId );
      dos.flush();
      return baos.toByteArray();
    } catch ( final IOException ex ) {
      throw new MqttException( ex );
    }
  }




  @Override
  public boolean isRetryable() {
    return true;
  }




  /**
   * @return string representation of this un-subscribe packet
   */
  @Override
  public String toString() {
    final StringBuffer sb = new StringBuffer();
    sb.append( super.toString() );
    sb.append( " names:[" );
    for ( int i = 0; i < count; i++ ) {
      if ( i > 0 ) {
        sb.append( ", " );
      }
      sb.append( "\"" + names[i] + "\"" );
    }
    sb.append( "]" );
    return sb.toString();
  }
}
