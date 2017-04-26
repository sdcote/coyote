package coyote.commons.network.mqtt.protocol;

import java.io.IOException;

import coyote.commons.network.mqtt.MqttException;


/**
 * The DISCONNECT message is sent from the client to the server to indicate 
 * that it is about to close its TCP/IP connection.
 * 
 * <p>This allows for a clean disconnection, rather than just dropping the 
 * line. If the client had connected with the clean session flag set, then all 
 * previously maintained information about the client will be discarded. A 
 * server should not rely on the client to close the TCP/IP connection after 
 * receiving a DISCONNECT.An on-the-wire representation of an MQTT DISCONNECT 
 * message.</p>
 */
public class DisconnectMessage extends AbstractMessage {
  public static final String KEY = "Disc";




  public DisconnectMessage() {
    super( AbstractMessage.DISCONNECT );
  }




  public DisconnectMessage( final byte info, final byte[] variableHeader ) throws IOException {
    super( AbstractMessage.DISCONNECT );
  }




  @Override
  public String getKey() {
    return KEY;
  }




  @Override
  protected byte getMessageInfo() {
    return (byte)0;
  }




  @Override
  protected byte[] getVariableHeader() throws MqttException {
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
