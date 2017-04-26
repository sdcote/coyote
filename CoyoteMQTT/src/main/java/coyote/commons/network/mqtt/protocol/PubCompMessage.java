package coyote.commons.network.mqtt.protocol;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

import coyote.commons.network.mqtt.MqttException;


/**
 * This message is either the response from the server to a PUBREL message from 
 * a publisher, or the response from a subscriber to a PUBREL message from the 
 * server. 
 * 
 * <p>It is the fourth and last message in the QoS 2 protocol flow.</p>
 */
public class PubCompMessage extends AbstractAckMessage {
  public PubCompMessage( final byte info, final byte[] data ) throws IOException {
    super( AbstractMessage.PUBCOMP );
    final ByteArrayInputStream bais = new ByteArrayInputStream( data );
    final DataInputStream dis = new DataInputStream( bais );
    msgId = dis.readUnsignedShort();
    dis.close();
  }




  public PubCompMessage( final int msgId ) {
    super( AbstractMessage.PUBCOMP );
    this.msgId = msgId;
  }




  public PubCompMessage( final PublishMessage publish ) {
    super( AbstractMessage.PUBCOMP );
    msgId = publish.getMessageId();
  }




  @Override
  protected byte[] getVariableHeader() throws MqttException {
    return encodeMessageId();
  }
}
