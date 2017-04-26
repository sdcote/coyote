package coyote.commons.network.mqtt.cache;

import coyote.commons.network.mqtt.MqttTokenImpl;
import coyote.commons.network.mqtt.protocol.AbstractMessage;


/**
 * A CachedMessage contains an MqttWire Message and token and allows both 
 * message and token to be cached when the client is in a resting state.
 */
public class CachedMessage {

  private final AbstractMessage message;
  private final MqttTokenImpl token;




  public CachedMessage( final AbstractMessage msg, final MqttTokenImpl tkn ) {
    message = msg;
    token = tkn;
  }




  public AbstractMessage getMessage() {
    return message;
  }




  public MqttTokenImpl getToken() {
    return token;
  }
}
