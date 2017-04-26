package coyote.commons.network.mqtt.client;

import java.net.URI;

import coyote.commons.network.mqtt.MQTT;
import coyote.commons.network.mqtt.MqttBlockingClient;
import coyote.commons.network.mqtt.MqttClient;
import coyote.commons.network.mqtt.cache.ClientCache;
import coyote.loader.log.Log;


/**
 *
 */
public class TestClientFactory {

  /**
   * @param serverURI 
   * @param clientId 
   * @return MqttClient
   * @throws Exception 
   */
  public MqttBlockingClient createMqttClient( URI serverURI, String clientId ) throws Exception {
    Log.append( MQTT.EVENT, "creating client" );
    return new MqttClientCiot( serverURI.toString(), clientId );
  }




  /**
   * @param serverURI 
   * @param clientId 
   * @param persistence 
   * @return MqttClient 
   * @throws Exception 
   */
  public MqttBlockingClient createMqttClient( URI serverURI, String clientId, ClientCache persistence ) throws Exception {
    Log.append( MQTT.EVENT, "creating client" );
    return new MqttClientCiot( serverURI.toString(), clientId, persistence );
  }




  /**
   * @param serverURI 
   * @param clientId 
   * @return client
   * @throws Exception 
   */
  public MqttClient createMqttAsyncClient( URI serverURI, String clientId ) throws Exception {
    Log.append( MQTT.EVENT, "creating client" );
    return new MqttAsyncClientCiot( serverURI.toString(), clientId );
  }




  /**
   * @param serverURI 
   * @param clientId 
   * @param persistence 
   * @return client
   * @throws Exception 
   */
  public MqttClient createMqttAsyncClient( URI serverURI, String clientId, ClientCache persistence ) throws Exception {
    Log.append( MQTT.EVENT, "creating client" );
    return new MqttAsyncClientCiot( serverURI.toString(), clientId, persistence );
  }




  /**
   * 
   */
  public void open() {
    // empty
  }




  /**
   * 
   */
  public void close() {
    // empty
  }




  /**
   * 
   */
  public void disconnect() {
    // empty
  }




  /**
   * @return flag indicating if this client supports High Availability
   */
  public boolean isHighAvalabilitySupported() {
    return true;
  }

}
