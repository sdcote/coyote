package coyote.commons.network.mqtt;

import coyote.commons.network.mqtt.ClientListener;
import coyote.commons.network.mqtt.MessageListener;
import coyote.commons.network.mqtt.MqttConnectOptions;
import coyote.commons.network.mqtt.MqttDeliveryToken;
import coyote.commons.network.mqtt.MqttException;
import coyote.commons.network.mqtt.MqttMessage;
import coyote.commons.network.mqtt.MqttSecurityException;
import coyote.commons.network.mqtt.MqttToken;
import coyote.commons.network.mqtt.MqttTokenImpl;
import coyote.commons.network.mqtt.Topic;
import coyote.commons.network.mqtt.cache.CacheException;


/**
 * Enables an application to communicate with an MQTT server.
 */
public interface MqttBlockingClient {
  /**
   * Close the client.
   * 
   * <p>Releases all resource associated with the client. After the client has
   * been closed it cannot be reused. For instance attempts to connect will 
   * fail.</p>
   * 
   * @throws MqttException if the client is not disconnected.
   */
  public void close() throws MqttException;




  /**
   * Connects to an MQTT server using the default options.
   * 
   * <p>The default options are specified in {@link MqttConnectOptions} 
   * class.</p>
   *
   * @throws MqttSecurityException when the server rejects the connect for 
   *         security reasons
   * @throws MqttException  for non security related problems
   * 
   * @see #connect(MqttConnectOptions)
   */
  public void connect() throws MqttSecurityException, MqttException;




  /**
   * Connects to an MQTT server using the specified options.
   * 
   * <p>This is a blocking method that returns once connect completes</p>
   *
   * @param options a set of connection parameters that override the defaults.
   * 
   * @throws MqttSecurityException when the server rejects the connect for 
   *         security reasons
   * @throws MqttException  for non security related problems including 
   *         communication errors
   */
  public void connect( MqttConnectOptions options ) throws MqttSecurityException, MqttException;




  /**
   * Connects to an MQTT server using the specified options.
   * 
   * <p>This is a blocking method that returns once the connection process 
   * completes.</p>
   *
   * @param options a set of connection parameters that override the defaults.
   * 
   * @return the MqttToken used for the call. Can be used to obtain the session 
   *         present flag
   * @throws MqttSecurityException when the server rejects the connect for 
   *         security reasons
   * @throws MqttException  for non security related problems including 
   *         communication errors
   */
  public MqttToken connectWithResult( MqttConnectOptions options ) throws MqttSecurityException, MqttException;




  /**
   * Disconnects from the server.
   * 
   * <p>An attempt is made to quiesce the client allowing outstanding work to 
   * complete before disconnecting. It will wait for a maximum of 30 seconds 
   * for work to quiesce before disconnecting.</p>
   * 
   *  <p><strong>NOTE:</strong>This method must not be called from inside 
   *  {@link ClientListener} methods.</p>
   *
   * @see #disconnect(long)
   */
  public void disconnect() throws MqttException;




  /**
   * Disconnects from the server.
   * 
   * <p>The client will wait for all {@link ClientListener} methods to complete. 
   * It will then wait for up to the quiesce timeout to allow for work which 
   * has already been initiated to complete.</p>
   *
   * <p>This is a blocking method that returns once disconnection completes.</p>
   *
   * @param quiesceTimeout the amount of time in milliseconds to allow for 
   *        existing work to finish before disconnecting. A value of zero or 
   *        less means the client will not quiesce.
   *        
   * @throws MqttException if a problem is encountered while disconnecting
   */
  public void disconnect( long quiesceTimeout ) throws MqttException;




  /**
   * Disconnects from the server forcibly to reset all the states. 
   * 
   * @throws MqttException if any unexpected error
   */
  public void disconnectForcibly() throws MqttException;




  /**
   * Disconnects from the server forcibly to reset all the states.
   * 
   * @param disconnectTimeout the amount of time in milliseconds to allow send 
   *        disconnect packet to server.
   *        
   * @throws MqttException if any unexpected error
   */
  public void disconnectForcibly( long disconnectTimeout ) throws MqttException;




  /**
   * Disconnects from the server forcibly to reset all the states.
   * 
   * @param quiesceTimeout the amount of time in milliseconds to allow for 
   *        existing work to finish before disconnecting. A value of zero or 
   *        less means the client will not quiesce.
   * @param disconnectTimeout the amount of time in milliseconds to allow send 
   *        disconnect packet to server.
   *        
   * @throws MqttException if any unexpected error
   */
  public void disconnectForcibly( long quiesceTimeout, long disconnectTimeout ) throws MqttException;




  /**
   * Returns the client ID used by this client.
   * 
   * @return the client ID used by this client.
   */
  public String getClientId();




  /**
   * Returns the delivery tokens for any outstanding publish operations.
   * 
   * <p>If a client has been restarted and there are messages that were in the
   * process of being delivered when the client stopped this method will
   * return a token for each message enabling the delivery to be tracked
   * Alternately the {@link ClientListener#deliveryComplete(MqttDeliveryToken)}
   * callback can be used to track the delivery of outstanding messages.</p>
   * 
   * <p>If a client connects with cleanSession true then there will be no 
   * delivery tokens as the cleanSession option deletes all earlier state. For 
   * state to be remembered the client must connect with cleanSession set to 
   * false</p>
   * 
   * @return zero or more delivery tokens
   */
  public MqttDeliveryToken[] getPendingDeliveryTokens();




  /**
   * Returns the URI address of the server used by this client.
   * 
   * <p>The format is the same as specified on the constructor.</p>
   *
   * @return the server's address, as a URI String.
   */
  public String getServerURI();




  /**
   * Get a topic object which can be used to publish messages.
   * 
   * @param topic the topic to use, for example "/oam/device/profile".
   * 
   * @return an MqttTopic object, which can be used to publish messages.
   * 
   * @throws IllegalArgumentException if the topic contains a '+' or '#' 
   *         wildcard character.
   */
  public Topic getTopic( String topic );




  /**
   * Determines if this client is currently connected to the server.
   *
   * @return {@code true} if connected, {@code false} otherwise.
   */
  public boolean isConnected();




  /**
   * Indicate that the application has completed processing the message with id 
   * messageId causing the MQTT acknowledgment to be sent to the server.
   * 
   * @param messageId the MQTT message id to be acknowledged
   * @param qos the MQTT QoS of the message to be acknowledged
   * 
   * @throws MqttException
   */
  public void messageArrivedComplete( int messageId, int qos ) throws MqttException;




  /**
   * Publishes a message to a topic on the server returning once it is 
   * delivered.
   * 
   * <p>This is a convenience method, which will create a new {@link 
   * MqttMessage} object with a byte array payload and the specified QoS, and 
   * then publish it.  All other values in the message will be set to the 
   * defaults.</p>
   *
   * @param topic  to deliver the message to, for example "/oam/device/ping".
   * @param payload the byte array to use as the payload
   * @param qos the Quality of Service to deliver the message at. Valid values 
   *        are 0, 1 or 2.
   * @param retained whether or not this message should be retained by the 
   *        server.
   *
   * @throws CacheException when a problem with storing the message
   * @throws IllegalArgumentException if value of QoS is not 0, 1 or 2.
   * @throws MqttException for other errors encountered while publishing the 
   *         message. For instance client not connected.
   *
   * @see #publish(String, MqttMessage)
   * @see MqttMessage#setQos(int)
   * @see MqttMessage#setRetained(boolean)
   */
  public void publish( String topic, byte[] payload, int qos, boolean retained ) throws MqttException, CacheException;




  /**
   * Publishes a message to a topic on the server.
   * 
   * <p>Delivers a message to the server at the requested quality of service 
   * and returns control once the message has been delivered. In the event the 
   * connection fails or the client stops, any messages that are in the process 
   * of being delivered will be delivered once a connection is re-established 
   * to the server on condition that:<ul>
   * <li>The connection is re-established with the same clientID</li>
   * <li>The original connection was made with (@link 
   *     MqttConnectOptions#setCleanSession(boolean)} set to false</li>
   * <li>The connection is re-established with (@link 
   *     MqttConnectOptions#setCleanSession(boolean)} set to false</li></ul></p>
   * 
   * <p>In the event that the connection breaks or the client stops it is still 
   * possible to determine when the delivery of the message completes. Prior to 
   * re-establishing the connection to the server:<ul>
   * <li>Register a {@link #setCallback(ClientListener)} callback on the client 
   * and the delivery complete callback will be notified once a delivery of a 
   * message completes</li>
   * <li>or call {@link #getPendingDeliveryTokens()} which will return a token 
   * for each message that is in-flight. The token can be used to wait for 
   * delivery to complete.</li></ul></p>
   *
   * <p>This is a blocking method that returns once publish completes</p>	 *
   *
   * @param topic  to deliver the message to, for example "/oam/device/ping".
   * @param message to delivery to the server
   * 
   * @throws CacheException when a problem with storing the message
   * @throws IllegalArgumentException if value of QoS is not 0, 1 or 2.
   * @throws MqttException for other errors encountered while publishing the 
   *         message. For instance client not connected.
   */
  public void publish( String topic, MqttMessage message ) throws MqttException, CacheException;




  /**
   * Sets the callback listener to use for events that happen asynchronously.
   * 
   * <p>There are a number of events that listener will be notified about, 
   * including the following<ul>
   * <li>A new message has arrived and is ready to be processed</li>
   * <li>The connection to the server has been lost</li>
   * <li>Delivery of a message to the server has completed.</li></ul></p>
   * 
   * <p>Other events that track the progress of an individual operation such as 
   * connect and subscribe can be tracked using the {@link MqttTokenImpl} 
   * passed to the operation<p>
   * 
   * @see ClientListener
   * 
   * @param callback the class to callback when for events related to the client
   */
  public void setCallback( ClientListener callback );




  /**
   * Control how acknowledgments are sent.
   * 
   * <p>If manualAcks is set to true, then on completion of the messageArrived 
   * callback,  the MQTT acknowledgments are not sent. You must then call 
   * messageArrivedComplete to send those acknowledgments. This allows finer 
   * control over when the ACKs are sent. The default behavior, when manualAcks 
   * is false, is to send the MQTT acknowledgments automatically at the 
   * successful completion of the messageArrived callback method.</p>
   * 
   * @param manualAcks
   */
  public void setManualAcks( boolean manualAcks );




  /**
   * Subscribe to a topic, which may include wildcards using a QoS of 1.
   *
   * @see #subscribe(String[], int[])
   *
   * @param topicFilter the topic to subscribe to, which can include wildcards.
   * 
   * @throws MqttException if there was an error registering the subscription.
   */
  public void subscribe( String topicFilter ) throws MqttException, MqttSecurityException;




  /**
   * Subscribe to a topic, which may include wildcards using a QoS of 1.
   *
   * @see #subscribe(String[], int[])
   *
   * @param topicFilter the topic to subscribe to, which can include wildcards.
   * @param messageListener a callback to handle incoming messages
   * 
   * @throws MqttException if there was an error registering the subscription.
   */
  public void subscribe( String topicFilter, MessageListener messageListener ) throws MqttException, MqttSecurityException;




  /**
   * Subscribe to a topic, which may include wildcards.
   *
   * @param topicFilter the topic to subscribe to, which can include wildcards.
   * @param qos the maximum quality of service at which to subscribe. Messages 
   *        published at a lower quality of service will be received at the 
   *        published QoS. Messages published at a higher quality of service 
   *        will be received using the QoS specified on the subscribe.
   *
   * @throws MqttException if there was an error registering the subscription.
   * 
   * @see #subscribe(String[], int[])
   */
  public void subscribe( String topicFilter, int qos ) throws MqttException;




  /**
   * Subscribe to a topic, which may include wildcards.
   *
   * @param topicFilter the topic to subscribe to, which can include wildcards.
   * @param qos the maximum quality of service at which to subscribe. Messages 
   *        published at a lower quality of service will be received at the 
   *        published QoS. Messages published at a higher quality of service 
   *        will be received using the QoS specified on the subscribe.
   * @param messageListener a callback to handle incoming messages
   * 
   * @throws MqttException if there was an error registering the subscription.
   *
   * @see #subscribe(String[], int[])
   */
  public void subscribe( String topicFilter, int qos, MessageListener messageListener ) throws MqttException;




  /**
   * Subscribes to a one or more topics, which may include wildcards using a 
   * QoS of 1.
   *
   * @see #subscribe(String[], int[])
   *
   * @param topicFilters the topic to subscribe to, which can include wildcards.
   * 
   * @throws MqttException if there was an error registering the subscription.
   */
  public void subscribe( String[] topicFilters ) throws MqttException;




  /**
   * Subscribes to a one or more topics, which may include wildcards using a QoS of 1.
   *
   * @param topicFilters the topic to subscribe to, which can include wildcards.
   * @param messageListeners one or more call-backs to handle incoming messages
   * 
   * @throws MqttException if there was an error registering the subscription.
   *
   * @see #subscribe(String[], int[])
   */
  public void subscribe( String[] topicFilters, MessageListener[] messageListeners ) throws MqttException;




  /**
   * Subscribes to multiple topics, each of which may include wildcards.
   * 
   * <p>This is a blocking method that returns once subscribe completes</p>
   *
   * @param topicFilters one or more topics to subscribe to, which can include 
   *        wildcards.
   * @param qos the maximum quality of service at which each topic subscribes
   * 
   * @throws MqttException if there was an error registering the subscription.
   * 
   * @throws IllegalArgumentException if the two supplied arrays are not the 
   *         same size.
   */
  public void subscribe( String[] topicFilters, int[] qos ) throws MqttException;




  /**
   * Subscribes to multiple topics, each of which may include wildcards.
   * 
   * <p>This is a blocking method that returns once subscribe completes</p>
   *
   * @param topicFilters one or more topics to subscribe to, which can include 
   *        wildcards.
   * @param qos the maximum quality of service at which each topic subscribes
   * @param messageListeners one or more call-backs to handle incoming messages
   * 
   * @throws MqttException if there was an error registering the subscription.
   * @throws IllegalArgumentException if the two supplied arrays are not the 
   *         same size.
   */
  public void subscribe( String[] topicFilters, int[] qos, MessageListener[] messageListeners ) throws MqttException;




  /**
   * Requests the server unsubscribe the client from a topic.
   *
   * @param topicFilter the topic from which to unsubscribe.
   * 
   * @throws MqttException if there was an error unregistering the subscription.
   */
  public void unsubscribe( String topicFilter ) throws MqttException;




  /**
   * Requests the server unsubscribe the client from one or more topics.
   * 
   * <p>This is a blocking method that returns once unsubscribe completes</p>
   *
   * @param topicFilters one or more topics from which to unsubscribe from.
   * @throws MqttException if there was an error unregistering the subscription.
   */
  public void unsubscribe( String[] topicFilters ) throws MqttException;



  /**
   * User triggered attempt to reconnect
   * 
   * @throws MqttException
   */
  void reconnect() throws MqttException;

}
