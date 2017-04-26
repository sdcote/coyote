package coyote.commons.network.mqtt.protocol;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

import coyote.commons.network.mqtt.MqttException;


/**
 * The UNSUBACK message is sent by the server to the client to confirm receipt 
 * of an UNSUBSCRIBE message.
 */
public class UnsubAckMessage extends AbstractAckMessage {

  public UnsubAckMessage( final byte info, final byte[] data ) throws IOException {
    super( AbstractMessage.UNSUBACK );
    final ByteArrayInputStream bais = new ByteArrayInputStream( data );
    final DataInputStream dis = new DataInputStream( bais );
    msgId = dis.readUnsignedShort();
    dis.close();
  }




  @Override
  protected byte[] getVariableHeader() throws MqttException {
    // Not needed, as the client never sends an UNSUBACK
    return new byte[0];
  }

}
