package coyote.commons.network.mqtt.protocol;

import java.io.IOException;

import coyote.commons.network.mqtt.MqttException;


/**
 * The PINGREQ message is an "are you alive?" message that is sent from a 
 * connected client to the server.
 */
public class PingReqMessage extends AbstractMessage {
  public static final String KEY = "Ping";




  public PingReqMessage() {
    super( AbstractMessage.PINGREQ );
  }




  public PingReqMessage( final byte info, final byte[] variableHeader ) throws IOException {
    super( AbstractMessage.PINGREQ );
  }




  @Override
  public String getKey() {
    return KEY;
  }




  @Override
  protected byte getMessageInfo() {
    return 0;
  }




  @Override
  protected byte[] getVariableHeader() throws MqttException {
    return new byte[0];
  }




  /**
   * Returns <code>false</code> as message IDs are not required for MQTT
   * PINGREQ messages.
   */
  @Override
  public boolean isMessageIdRequired() {
    return false;
  }
}
