package coyote.commons.network.mqtt;

import coyote.commons.network.mqtt.cache.CachedMessage;


public interface DisconnectedBufferCallback {

  public void publishBufferedMessage( CachedMessage bufferedMessage ) throws MqttException;

}
