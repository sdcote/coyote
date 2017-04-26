package coyote.commons.network.mqtt.protocol;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

import coyote.commons.network.mqtt.MqttException;


/**
 * A PUBREL message is the response either from a publisher to a PUBREC message 
 * from the server, or from the server to a PUBREC message from a subscriber. 
 * 
 * <p>It is the third message in the QoS 2 protocol flow.</p>
 */
public class PubRelMessage extends CachableMessage {

  /**
   * Creates a pubrel based on a pubrel set of bytes read from the network
   * @param info
   * @param data
   * @throws IOException
   */
  public PubRelMessage( final byte info, final byte[] data ) throws IOException {
    super( AbstractMessage.PUBREL );
    final ByteArrayInputStream bais = new ByteArrayInputStream( data );
    final DataInputStream dis = new DataInputStream( bais );
    msgId = dis.readUnsignedShort();
    dis.close();
  }




  /**
   * Create a a pubrel message based on a pubrec
   * @param pubRec
   */
  public PubRelMessage( final PubRecMessage pubRec ) {
    super( AbstractMessage.PUBREL );
    setMessageId( pubRec.getMessageId() );
  }




  @Override
  protected byte getMessageInfo() {
    return (byte)( 2 | ( duplicate ? 8 : 0 ) );
  }




  @Override
  protected byte[] getVariableHeader() throws MqttException {
    return encodeMessageId();
  }




  @Override
  public String toString() {
    return super.toString() + " msgId " + msgId;
  }

}
