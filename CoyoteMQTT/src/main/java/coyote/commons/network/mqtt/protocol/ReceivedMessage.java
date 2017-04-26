package coyote.commons.network.mqtt.protocol;

import coyote.commons.network.mqtt.MqttMessage;


/**
 * Generic message received from the server.
 */
public class ReceivedMessage extends MqttMessage {

  public int getMessageId() {
    return super.getId();
  }




  public void setMessageId( final int msgId ) {
    super.setId( msgId );
  }

}
