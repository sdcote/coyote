package coyote.commons.network.mqtt.protocol;

import coyote.commons.network.mqtt.MqttException;


/**
 * A PINGRESP message is the response sent by a server to a PINGREQ message and 
 * means "yes I am alive". 
 */
public class PingRespMessage extends AbstractAckMessage {
  public static final String KEY = "Ping";




  public PingRespMessage( final byte info, final byte[] variableHeader ) {
    super( AbstractMessage.PINGRESP );
  }




  @Override
  public String getKey() {
    return KEY;
  }




  @Override
  protected byte[] getVariableHeader() throws MqttException {
    // Not needed, as the client never encodes a PINGRESP
    return new byte[0];
  }




  /**
   * Returns whether or not this message needs to include a message ID.
   */
  @Override
  public boolean isMessageIdRequired() {
    return false;
  }
}
