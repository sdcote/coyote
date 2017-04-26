package coyote.commons.network.mqtt;

/**
 * Enables an application to be notified when asynchronous events related to 
 * the client occur.
 */
public interface ClientListener {

  /**
   * This method is called when the connection to the server is lost.
   *
   * @param cause the reason behind the loss of connection.
   */
  public void connectionLost( Throwable cause );




  /**
   * Called when delivery for a message has been completed, and all 
   * acknowledgments have been received. 
   * 
   * <p>For QoS 0 messages it is called once the message has been handed to the 
   * network for delivery. For QoS 1 it is called when PUBACK is received and
   * for QoS 2 when PUBCOMP is received. The token will be the same token as 
   * that returned when the message was published.</p>
   *
   * @param token the delivery token associated with the message.
   */
  public void deliveryComplete( MqttDeliveryToken token );




  /**
   * This method is called when a message arrives from the server.
   *
   * <p>This method is invoked synchronously by the MQTT client. An 
   * acknowledgment is not sent back to the server until this method returns 
   * cleanly.</p>
   * 
   * <p>If an implementation of this method throws an {@code Exception}, then 
   * the client will be shut down.  When the client is next re-connected, any 
   * QoS 1 or 2 messages will be re-delivered by the server.</p>
   * 
   * <p>Any additional messages which arrive while an implementation of this 
   * method is running, will build up in memory, and will then back up on the 
   * network.</p>
   * 
   * <p>If an application needs to persist data, then it should ensure the data 
   * is persisted prior to returning from this method, as after returning from 
   * this method, the message is considered to have been delivered, and will 
   * not be reproducible.</p>
   * 
   * <p>It is possible to send a new message within an implementation of this 
   * callback (for example, a response to this message), but the implementation 
   * must not disconnect the client, as it will be impossible to send an 
   * acknowledgment for the message being processed, and a deadlock will 
   * occur.</p>
   *
   * @param topic name of the topic on the message was published to
   * @param message the actual message.
   * 
   * @throws Exception if a terminal error has occurred, and the client should 
   *         be shut down.
   */
  public void messageArrived( String topic, MqttMessage message ) throws Exception;




  /**
   * Called when the connection to the server is completed successfully.
   * 
   * @param reconnect If true, the connection was the result of automatic 
   *        reconnect.
   * @param serverURI The server URI that the connection was made to.
   */
  public void connectComplete( boolean reconnect, String serverURI );

}
