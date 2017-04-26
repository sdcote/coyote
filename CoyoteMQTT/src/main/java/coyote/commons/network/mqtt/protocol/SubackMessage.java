package coyote.commons.network.mqtt.protocol;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

import coyote.commons.network.mqtt.MqttException;


/**
 * A SUBACK message is sent by the server to the client to confirm receipt of a 
 * SUBSCRIBE message.
 * 
 * <p>A SUBACK message contains a list of granted QoS levels. The order of 
 * granted QoS levels in the SUBACK message matches the order of the topic 
 * names in the corresponding SUBSCRIBE message.</p>
 */
public class SubackMessage extends AbstractAckMessage {
  private final int[] grantedQos;




  public SubackMessage( final byte info, final byte[] data ) throws IOException {
    super( AbstractMessage.SUBACK );
    final ByteArrayInputStream bais = new ByteArrayInputStream( data );
    final DataInputStream dis = new DataInputStream( bais );
    msgId = dis.readUnsignedShort();
    int index = 0;
    grantedQos = new int[data.length - 2];
    int qos = dis.read();
    while ( qos != -1 ) {
      grantedQos[index] = qos;
      index++;
      qos = dis.read();
    }
    dis.close();
  }




  public int[] getGrantedQos() {
    return grantedQos;
  }




  @Override
  protected byte[] getVariableHeader() throws MqttException {
    // Not needed, as the client never encodes a SUBACK
    return new byte[0];
  }




  @Override
  public String toString() {
    final StringBuffer sb = new StringBuffer();
    sb.append( super.toString() ).append( " granted Qos" );
    for ( final int grantedQo : grantedQos ) {
      sb.append( " " ).append( grantedQo );
    }
    return sb.toString();
  }

}
