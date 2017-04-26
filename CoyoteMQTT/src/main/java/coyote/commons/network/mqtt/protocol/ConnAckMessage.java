package coyote.commons.network.mqtt.protocol;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

import coyote.commons.network.mqtt.MqttException;


/**
 * The CONNACK message is the message sent by the server in response to a 
 * CONNECT request from a client.
 */
public class ConnAckMessage extends AbstractAckMessage {
  public static final String KEY = "Con";

  private final int returnCode;
  private final boolean sessionPresent;




  public ConnAckMessage( final byte info, final byte[] variableHeader ) throws IOException {
    super( AbstractMessage.CONNACK );
    final ByteArrayInputStream bais = new ByteArrayInputStream( variableHeader );
    final DataInputStream dis = new DataInputStream( bais );
    sessionPresent = ( dis.readUnsignedByte() & 0x01 ) == 0x01;
    returnCode = dis.readUnsignedByte();
    dis.close();
  }




  @Override
  public String getKey() {
    return KEY;
  }




  public int getReturnCode() {
    return returnCode;
  }




  public boolean getSessionPresent() {
    return sessionPresent;
  }




  @Override
  protected byte[] getVariableHeader() throws MqttException {
    // Not needed, as the client never encodes a CONNACK
    return new byte[0];
  }




  /**
   * Returns whether or not this message needs to include a message ID.
   */
  @Override
  public boolean isMessageIdRequired() {
    return false;
  }




  @Override
  public String toString() {
    return super.toString() + " session present:" + sessionPresent + " return code: " + returnCode;
  }
}
