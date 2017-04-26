package coyote.commons.network.mqtt.protocol;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

import coyote.commons.network.mqtt.MqttException;


/**
 * A PUBREC message is the response to a PUBLISH message with QoS level 2. 
 * 
 * <p>It is the second message of the QoS level 2 protocol flow. A PUBREC 
 * message is sent by the server in response to a PUBLISH message from a 
 * publishing client, or by a subscriber in response to a PUBLISH message from 
 * the server.</p>
 */
public class PubRecMessage extends AbstractAckMessage {

  public PubRecMessage( final byte info, final byte[] data ) throws IOException {
    super( AbstractMessage.PUBREC );
    final ByteArrayInputStream bais = new ByteArrayInputStream( data );
    final DataInputStream dis = new DataInputStream( bais );
    msgId = dis.readUnsignedShort();
    dis.close();
  }




  public PubRecMessage( final PublishMessage publish ) {
    super( AbstractMessage.PUBREC );
    msgId = publish.getMessageId();
  }




  @Override
  protected byte[] getVariableHeader() throws MqttException {
    return encodeMessageId();
  }
}
