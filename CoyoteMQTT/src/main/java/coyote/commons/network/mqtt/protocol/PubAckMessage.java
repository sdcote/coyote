package coyote.commons.network.mqtt.protocol;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

import coyote.commons.network.mqtt.MqttException;


/**
 * A PUBACK message is the response to a PUBLISH message with QoS level 1. 
 * 
 * <p>A PUBACK message is sent by a server in response to a PUBLISH message 
 * from a publishing client, and by a subscriber in response to a PUBLISH 
 * message from the server.</p>
 */
public class PubAckMessage extends AbstractAckMessage {
  public PubAckMessage( final byte info, final byte[] data ) throws IOException {
    super( AbstractMessage.PUBACK );
    final ByteArrayInputStream bais = new ByteArrayInputStream( data );
    final DataInputStream dis = new DataInputStream( bais );
    msgId = dis.readUnsignedShort();
    dis.close();
  }




  public PubAckMessage( final int messageId ) {
    super( AbstractMessage.PUBACK );
    msgId = messageId;
  }




  public PubAckMessage( final PublishMessage publish ) {
    super( AbstractMessage.PUBACK );
    msgId = publish.getMessageId();
  }




  @Override
  protected byte[] getVariableHeader() throws MqttException {
    return encodeMessageId();
  }
}
