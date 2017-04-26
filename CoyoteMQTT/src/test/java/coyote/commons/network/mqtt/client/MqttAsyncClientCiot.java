package coyote.commons.network.mqtt.client;

import coyote.commons.network.mqtt.MqttClientImpl;
import coyote.commons.network.mqtt.MqttException;
import coyote.commons.network.mqtt.cache.ClientCache;


/**
 *
 */
public class MqttAsyncClientCiot extends MqttClientImpl {

  /**
   * @param serverURI
   * @param clientId
   * @throws MqttException
   */
  public MqttAsyncClientCiot( String serverURI, String clientId ) throws MqttException {
    super( serverURI, clientId );
  }




  /**
   * @param serverURI
   * @param clientId
   * @param persistence
   * @throws MqttException 
   */
  public MqttAsyncClientCiot( String serverURI, String clientId, ClientCache persistence ) throws MqttException {
    super( serverURI, clientId, persistence );
  }




  /**
   * @throws Exception 
   */
  public void startTrace() throws Exception {
    // not implemented
  }




  /**
   * @throws Exception 
   */
  public void stopTrace() throws Exception {
    // not implemented
  }




  /**
   * @return trace buffer
   * @throws Exception 
   */
  public String getTraceLog() throws Exception {
    return null;
  }
}
