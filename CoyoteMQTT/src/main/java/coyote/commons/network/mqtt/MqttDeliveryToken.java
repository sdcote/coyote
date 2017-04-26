package coyote.commons.network.mqtt;

/**
 * Provides a mechanism for tracking the delivery of a message.
 * 
 * <p>A subclass of MqttToken that allows the delivery of a message to be 
 * tracked. Unlike instances of MqttToken delivery tokens can be used across 
 * connection and client restarts. This enables the delivery of a messages to 
 * be tracked after failures. There are two approaches:<ul> 
 * <li>A list of delivery tokens for in-flight messages can be obtained using 
 * {@link MqttClient#getPendingDeliveryTokens()}. The waitForCompletion method 
 * can then be used to block until the delivery is complete.</li>
 * <li>A {@link ClientListener} can be set on the client. Once a message has 
 * been delivered the {@link ClientListener#deliveryComplete(MqttDeliveryToken)} 
 * method will be called with the delivery token being passed as a 
 * parameter.</li></ul>
 * 
 * <p> An action is in progress until either:<ul>
 * <li>isComplete() returns true or </li>
 * <li>getException() is not null. If a client shuts down before delivery is 
 * complete, an exception is returned.  As long as the Java Runtime is not 
 * stopped a delivery token is valid across a connection disconnect and 
 * reconnect. In the event the client is shut down the getPendingDeliveryTokens 
 * method can be used once the client is restarted to obtain a list of delivery 
 * tokens for inflight messages.</li></ul>
 */

public interface MqttDeliveryToken extends MqttToken {
  /**
   * Returns the message associated with this token.
   * <p>Until the message has been delivered, the message being delivered will
   * be returned. Once the message has been delivered <code>null</code> will be 
   * returned.
   * @return the message associated with this token or null if already delivered.
   * @throws MqttException if there was a problem completing retrieving the message
   */
  public MqttMessage getMessage() throws MqttException;
}
