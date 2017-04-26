package coyote.commons.network.mqtt.protocol;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import coyote.commons.network.mqtt.MqttException;
import coyote.commons.network.mqtt.MqttMessage;


/**
 * The SUBSCRIBE message allows a client to register an interest in one or more 
 * topic names with the server.
 * 
 * <p>Messages published to these topics are delivered from the server to the 
 * client as PUBLISH messages. The SUBSCRIBE message also specifies the QoS 
 * level at which the subscriber wants to receive published messages.</p>
 */
public class SubscribeMessage extends AbstractMessage {
  private final String[] names;
  private final int[] qos;
  private int count;




  /**
   * Constructor for an on the wire MQTT subscribe message
   * 
   * @param info
   * @param data
   */
  public SubscribeMessage( final byte info, final byte[] data ) throws IOException {
    super( AbstractMessage.SUBSCRIBE );
    final ByteArrayInputStream bais = new ByteArrayInputStream( data );
    final DataInputStream dis = new DataInputStream( bais );
    msgId = dis.readUnsignedShort();

    count = 0;
    names = new String[10];
    qos = new int[10];
    boolean end = false;
    while ( !end ) {
      try {
        names[count] = decodeUTF8( dis );
        qos[count++] = dis.readByte();
      } catch ( final Exception e ) {
        end = true;
      }
    }
    dis.close();
  }




  /**
   * Constructor for an on the wire MQTT subscribe message
   * @param names - one or more topics to subscribe to 
   * @param qos - the max QoS that each each topic will be subscribed at 
   */
  public SubscribeMessage( final String[] names, final int[] qos ) {
    super( AbstractMessage.SUBSCRIBE );
    this.names = names;
    this.qos = qos;

    if ( names.length != qos.length ) {
      throw new IllegalArgumentException();
    }

    for ( final int qo : qos ) {
      MqttMessage.validateQos( qo );
    }
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
      for ( int i = 0; i < names.length; i++ ) {
        encodeUTF8( dos, names[i] );
        dos.writeByte( qos[i] );
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
   * @return string representation of this subscribe packet
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
      sb.append( "\"" ).append( names[i] ).append( "\"" );
    }
    sb.append( "] qos:[" );
    for ( int i = 0; i < count; i++ ) {
      if ( i > 0 ) {
        sb.append( ", " );
      }
      sb.append( qos[i] );
    }
    sb.append( "]" );

    return sb.toString();
  }
}
