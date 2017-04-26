package coyote.commons.network.mqtt;

import coyote.commons.network.mqtt.cache.CacheException;


/**
 * Enables an application to communicate with an MQTT server using non-blocking 
 * methods.
 */
public interface MqttClient {

  /**
   * Close the client.
   * 
   * <p>Releases all resource associated with the client. After the client has
   * been closed it cannot be reused. For instance attempts to connect will 
   * fail.</p>
   * 
   * @throws MqttException  if the client is not disconnected.
   */
  public void close() throws MqttException;




  /**
   * Connects to an MQTT server using the default options.
   * 
   * @return token used to track and wait for the connect to complete. The 
   *         token will be passed to the callback methods if a callback is set.
   * 
   * @throws MqttSecurityException  for security related problems
   * @throws MqttException  for non security related problems
   * 
   * @see #connect(MqttConnectOptions, Object, AsyncActionListener)
   */
  public MqttToken connect() throws MqttException, MqttSecurityException;




  /**
   * Connects to an MQTT server using the provided connect options.
   * 
   * <p>The connection will be established using the options specified in the 
   * {@link MqttConnectOptions} parameter.</p>
   *
   * @param options a set of connection parameters that override the defaults.
   * 
   * @return token used to track and wait for the connect to complete. The 
   *         token will be passed to any callback that has been set.
   * 
   * @throws MqttSecurityException  for security related problems
   * @throws MqttException  for non security related problems
   * 
   * @see #connect(MqttConnectOptions, Object, AsyncActionListener)
   */
  public MqttToken connect( MqttConnectOptions options ) throws MqttException, MqttSecurityException;




  /**
   * Connects to an MQTT server using the specified options.
   * 
   * <p>The server to connect to is specified on the constructor. It is 
   * recommended to call {@link #setCallback(ClientListener)} prior to 
   * connecting in order that messages destined for the client can be accepted
   * as soon as the client is connected.</p>
   * 
   * <p>The method returns control before the connect completes. Completion can
   * be tracked by:<ul>
   * <li>Waiting on the returned token {@link MqttToken#waitForCompletion()},
   * or</li>
   * <li>Passing in a callback {@link AsyncActionListener}</li></ul>
   *
   * @param options a set of connection parameters that override the defaults.
   * @param userContext optional object for used to pass context to the 
   *        callback. Use null if not required.
   * @param callback optional listener that will be notified when the connect 
   *        completes. Use null if not required.
   * 
   * @return token used to track and wait for the connect to complete. The 
   *         token will be passed to any callback that has been set.
   *
   * @throws MqttSecurityException  for security related problems
   * @throws MqttException for non security related problems including 
   *         communication errors
   */
  public MqttToken connect( MqttConnectOptions options, Object userContext, AsyncActionListener callback ) throws MqttException, MqttSecurityException;




  /**
   * Connects to an MQTT server using the default options.
   * 
   * <p>The default options are specified in {@link MqttConnectOptions} 
   * class.</p>
   *
   * @param userContext optional object used to pass context to the callback. 
   *        Use null if not required.
   * @param callback optional listener that will be notified when the connect 
   *        completes. Use null if not required.
   * 
   * @throws MqttSecurityException  for security related problems
   * @throws MqttException  for non security related problems
   * 
   * @return token used to track and wait for the connect to complete. The 
   *         token will be passed to any callback that has been set.
   *
   * @see #connect(MqttConnectOptions, Object, AsyncActionListener)
   */
  public MqttToken connect( Object userContext, AsyncActionListener callback ) throws MqttException, MqttSecurityException;




  /**
   * Disconnects from the server.
   * 
   * <p>An attempt is made to quiesce the client allowing outstanding work to 
   * complete before disconnecting. It will wait for a maximum of 30 seconds 
   * for work to quiesce before disconnecting. This method must not be called 
   * from inside {@link ClientListener} methods.</p>
   *
   * @return token used to track and wait for disconnect to complete. The token
   *         will be passed to any callback that has been set.
   * 
   * @throws MqttException for problems encountered while disconnecting
   * 
   * @see #disconnect(long, Object, AsyncActionListener)
   */
  public MqttToken disconnect() throws MqttException;




  /**
   * Disconnects from the server.
   * 
   * <p>An attempt is made to quiesce the client allowing outstanding work to 
   * complete before disconnecting. It will wait for a maximum of the specified 
   * quiesce time  for work to complete before disconnecting. This method must 
   * not be called from inside {@link ClientListener} methods.</p>
   * 
   * @param quiesceTimeout the amount of time in milliseconds to allow for 
   *        existing work to finish before disconnecting. A value of zero or 
   *        less means the client will not quiesce.
   * 
   * @return token used to track and wait for disconnect to complete. The token
   *         will be passed to the callback methods if a callback is set.
   * 
   * @throws MqttException for problems encountered while disconnecting
   * 
   * @see #disconnect(long, Object, AsyncActionListener)
   */
  public MqttToken disconnect( long quiesceTimeout ) throws MqttException;




  /**
   * Disconnects from the server.
   * 
   * <p>The client will wait for {@link ClientListener} methods to complete. It 
   * will then wait for up to the quiesce timeout to allow for work which has 
   * already been initiated to complete. For instance when a QoS 2 message has 
   * started flowing to the server but the QoS 2 flow has not completed.It 
   * prevents new messages being accepted and does not send any messages that 
   * have been accepted but not yet started delivery across the network to the 
   * server. When work has completed or after the quiesce timeout, the client 
   * will disconnect from the server. If the cleanSession flag was set to false 
   * and is set to false the next time a connection is made QoS 1 and 2 
   * messages that were not previously delivered will be delivered.</p>
   * 
   * <p>This method must not be called from inside {@link ClientListener} 
   * methods.</p>
   * 
   * <p>The method returns control before the disconnect completes. Completion 
   * can be tracked by:<ul>
   * <li>Waiting on the returned token {@link MqttToken#waitForCompletion()} 
   * or</li>
   * <li>Passing in a callback {@link AsyncActionListener}</li>
   * </ul>
   *
   * @param quiesceTimeout the amount of time in milliseconds to allow for 
   *        existing work to finish before disconnecting. A value of zero or 
   *        less means the client will not quiesce.
   * @param userContext optional object used to pass context to the callback. 
   *        Use null if not required.
   * @param callback optional listener that will be notified when the 
   *        disconnect completes. Use null if not required.
   * 
   * @return token used to track and wait for the connect to complete. The 
   *         token will be passed to any callback that has been set.
   *
   * @throws MqttException for problems encountered while disconnecting
   */
  public MqttToken disconnect( long quiesceTimeout, Object userContext, AsyncActionListener callback ) throws MqttException;




  /**
   * Disconnects from the server.
   * 
   * <p>An attempt is made to quiesce the client allowing outstanding work to 
   * complete before disconnecting. It will wait for a maximum of 30 seconds 
   * for work to quiesce before disconnecting. This method must not be called 
   * from inside {@link ClientListener} methods.</p>
   *
   * @param userContext optional object used to pass context to the callback. 
   *        Use null if not required.
   * @param callback optional listener that will be notified when the 
   *        disconnect completes. Use null if not required.
   * 
   * @return token used to track and wait for the disconnect to complete. The 
   *         token will be passed to any callback that has been set.
   * @throws MqttException for problems encountered while disconnecting
   * 
   * @see #disconnect(long, Object, AsyncActionListener)
   */
  public MqttToken disconnect( Object userContext, AsyncActionListener callback ) throws MqttException;




  /**
   * Disconnects from the server forcibly to reset all the states. 
   * 
   * <p>Could be useful when disconnect attempt failed.</p>
   * 
   * <p>Because the client is able to establish the TCP/IP connection to a 
   * failed MQTT server and it will certainly fail to send the disconnect 
   * packet. It will wait for a maximum of 30 seconds for work to quiesce 
   * before disconnecting and wait for a maximum of 10 seconds for sending the 
   * disconnect packet to server.</p>
   * 
   * @throws MqttException if any unexpected error
   */
  public void disconnectForcibly() throws MqttException;




  /**
   * Disconnects from the server forcibly to reset all the states. 
   * 
   * <p>Could be useful when disconnect attempt failed.</p>
   * 
   * <p> Because the client is able to establish the TCP/IP connection to a 
   * failed MQTT server and it will certainly fail to send the disconnect 
   * packet. It will wait for a maximum of 30 seconds for work to quiesce 
   * before disconnecting.</p>
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
   * <p>Could be useful when disconnect attempt failed.</p>
   * 
   * <p>Because the client is able to establish the TCP/IP connection to a 
   * failed MQTT server and it will certainly fail to send the disconnect 
   * packet.<p>
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
   * <p>All clients connected to the same server or server farm must have a 
   * unique ID.</p>
   *
   * @return the client ID used by this client.
   */
  public String getClientId();




  /**
   * Returns the delivery tokens for any outstanding publish operations.
   * 
   * <p>If a client has been restarted and there are messages that were in the
   * process of being delivered when the client stopped this method returns a 
   * token for each in-flight message enabling the delivery to be tracked
   * Alternately the {@link ClientListener#deliveryComplete(MqttDeliveryToken)}
   * callback can be used to track the delivery of outstanding messages.</p>
   * 
   * <p>If a client connects with cleanSession true then there will be no 
   * delivery tokens as the cleanSession option deletes all earlier state. For 
   * state to be remembered the client must connect with cleanSession set to 
   * false.</p>
   * 
   * @return zero or more delivery tokens
   */
  public MqttDeliveryToken[] getPendingDeliveryTokens();




  /**
   * Returns the address of the server used by this client.
   * 
   * <p>The format of the returned String is the same as that used on the 
   * constructor.</p>
   *
   * @return the server's address, as a URI String.
   */
  public String getServerURI();




  /**
   * Determines if this client is currently connected to the server.
   *
   * @return {@code true} if connected, {@code false} otherwise.
   */
  public boolean isConnected();




  /**
   * Indicate that the application has completed processing the message with id 
   * messageId.
   * 
   * <p>This will cause the MQTT acknowledgment to be sent to the server.</p>
   * 
   * @param messageId the MQTT message id to be acknowledged
   * @param qos the MQTT QoS of the message to be acknowledged
   * 
   * @throws MqttException
   */
  public void messageArrivedComplete( int messageId, int qos ) throws MqttException;




  /**
   * Publishes a message to a topic on the server.
   * 
   * <p>A convenience method, which will create a new {@link MqttMessage} 
   * object with a byte array payload and the specified QoS, and then publish 
   * it.</p>
   *
   * @param topic to deliver the message to, for example "finance/stock/ibm".
   * @param payload the byte array to use as the payload
   * @param qos the Quality of Service to deliver the message at. Valid values 
   *        are 0, 1 or 2.
   * @param retained whether or not this message should be retained by the 
   *        server.
   * 
   * @return token used to track and wait for the publish to complete. The 
   *         token will be passed to any callback that has been set.
   * 
   * @throws CacheException when a problem occurs storing the message
   * @throws IllegalArgumentException if value of QoS is not 0, 1 or 2.
   * @throws MqttException for other errors encountered while publishing the 
   *         message. For instance if too many messages are being processed.
   * 
   * @see #publish(String, MqttMessage, Object, AsyncActionListener)
   * @see MqttMessage#setQos(int)
   * @see MqttMessage#setRetained(boolean)
   */
  public MqttDeliveryToken publish( String topic, byte[] payload, int qos, boolean retained ) throws MqttException, CacheException;




  /**
   * Publishes a message to a topic on the server.
   * 
   * <p>A convenience method, which will create a new {@link MqttMessage} 
   * object with a byte array payload and the specified QoS, and then publish 
   * it.</p>
   *
   * @param topic  to deliver the message to, for example "finance/stock/ibm".
   * @param payload the byte array to use as the payload
   * @param qos the Quality of Service to deliver the message at. Valid values 
   *        are 0, 1 or 2.
   * @param retained whether or not this message should be retained by the 
   *        server.
   * @param userContext optional object used to pass context to the callback. 
   *        Use null if not required.
   * @param callback optional listener that will be notified when message 
   *        delivery has completed to the requested quality of service
   * 
   * @return token used to track and wait for the publish to complete. The 
   *         token will be passed to any callback that has been set.
   * 
   * @throws CacheException when a problem occurs storing the message
   * @throws IllegalArgumentException if value of QoS is not 0, 1 or 2.
   * @throws MqttException for other errors encountered while publishing the 
   *         message. For instance client not connected.
   * 
   * @see #publish(String, MqttMessage, Object, AsyncActionListener)
   * @see MqttMessage#setQos(int)
   * @see MqttMessage#setRetained(boolean)
   */
  public MqttDeliveryToken publish( String topic, byte[] payload, int qos, boolean retained, Object userContext, AsyncActionListener callback ) throws MqttException, CacheException;




  /**
   * Publishes a message to a topic on the server.
   * 
   * <p>Takes an {@link MqttMessage} message and delivers it to the server at 
   * the requested quality of service.</p>
   *
   * @param topic  to deliver the message to, for example "finance/stock/ibm".
   * @param message to deliver to the server
   * 
   * @return token used to track and wait for the publish to complete. The 
   *         token will be passed to any callback that has been set.
   * 
   * @throws CacheException when a problem occurs storing the message
   * @throws IllegalArgumentException if value of QoS is not 0, 1 or 2.
   * @throws MqttException for other errors encountered while publishing the 
   *         message.For instance client not connected.
   * 
   * @see #publish(String, MqttMessage, Object, AsyncActionListener)
   */
  public MqttDeliveryToken publish( String topic, MqttMessage message ) throws MqttException, CacheException;




  /**
   * Publishes a message to a topic on the server.
   * 
   * <p>Once this method has returned cleanly, the message has been accepted 
   * for publication by the client and will be delivered on a background 
   * thread. In the event the connection fails or the client stops. Messages 
   * will be delivered to the requested quality of service once the connection 
   * is re-established to the server on condition that:<ul>
   * <li>The connection is re-established with the same clientID</li>
   * <li>The original connection was made with (@link 
   * ConnectOptions#setCleanSession(boolean)} set to false</li>
   * <li>The connection is re-established with (@link 
   * ConnectOptions#setCleanSession(boolean)} set to false</li>
   * <li>Depending when the failure occurs QoS 0 messages may not be 
   * delivered.</li></ul>
   *
   * <p>When building an application, the design of the topic tree should take 
   * into account the following principles of topic name syntax and semantics:
   * <ul>
   * <li>A topic must be at least one character long.</li>
   * <li>Topic names are case sensitive. For example, <em>ACCOUNTS</em> and 
   * <em>Accounts</em> are two different topics.</li>
   * <li>Topic names can include the space character. For example, <em>Accounts
   * payable</em> is a valid topic.</li>
   * <li>A leading "/" creates a distinct topic. For example, <em>/finance</em> 
   * is different from <em>finance</em>. <em>/finance</em> matches "+/+" and 
   * "/+", but not "+".</li>
   * <li>Do not include the null character (Unicode {@code \x0000}) in any 
   * topic.</li></ul>
   *
   * <p>The following principles apply to the construction and content of a 
   * topic tree:<ul>
   * <li>The length is limited to 64k but within that there are no limits to 
   * the number of levels in a topic tree.</li>
   * <li>There can be any number of root nodes; that is, there can be any 
   * number  of topic trees.</li></ul>
   * 
   * <p>The method returns control before the publish completes. Completion can
   * be tracked by:<ul>
   * <li>Setting an {@link MqttClient#setCallback(ClientListener)} where the
   * {@link ClientListener#deliveryComplete(MqttDeliveryToken)}
   * method will be called.</li>
   * <li>Waiting on the returned token {@link MqttTokenImpl#waitForCompletion()} 
   * or</li>
   * <li>Passing in a callback {@link AsyncActionListener} to this method</li>
   * </ul>
   *
   * @param topic  to deliver the message to, for example "finance/stock/ibm".
   * @param message to deliver to the server
   * @param userContext optional object used to pass context to the callback. 
   *        Use null if not required.
   * @param callback optional listener that will be notified when message 
   *        delivery has completed to the requested quality of service
   * 
   * @return token used to track and wait for the publish to complete. The 
   *         token will be passed to callback methods if set.
   * 
   * @throws CacheException when a problem occurs storing the message
   * @throws IllegalArgumentException if value of QoS is not 0, 1 or 2.
   * @throws MqttException for other errors encountered while publishing the 
   *         message. For instance client not connected.
   * 
   * @see MqttMessage
   */
  public MqttDeliveryToken publish( String topic, MqttMessage message, Object userContext, AsyncActionListener callback ) throws MqttException, CacheException;




  /**
   * User triggered attempt to reconnect
   * 
   * @throws MqttException
   */
  void reconnect() throws MqttException;




  /**
   * Sets a callback listener to use for events that happen asynchronously.
   * 
   * <p>There are a number of events that the listener will be notified about.
   * These include:<ul>
   * <li>A new message has arrived and is ready to be processed</li>
   * <li>The connection to the server has been lost</li>
   * <li>Delivery of a message to the server has completed</li>
   * </ul>
   * 
   * <p>Other events that track the progress of an individual operation such as 
   * connect and subscribe can be tracked using the {@link MqttTokenImpl} 
   * returned from each non-blocking method or using setting a {@link 
   * AsyncActionListener} on the non-blocking method.<p>
   * 
   * @param callback which will be invoked for certain asynchronous events
   * 
   * @see ClientListener
   */
  public void setCallback( ClientListener callback );




  /**
   * If manualAcks is set to true, then on completion of the messageArrived 
   * callback the MQTT acknowledgments are not sent. 
   * 
   * <p>You must call messageArrivedComplete to send those acknowledgments. 
   * This allows finer control over when the ACKs are sent. The default 
   * behavior, when manualAcks is false, is to send the MQTT acknowledgments 
   * automatically at the successful completion of the messageArrived callback 
   * method.</p>
   * 
   * @param manualAcks
   */
  public void setManualAcks( boolean manualAcks );




  /**
   * Subscribe to a topic, which may include wildcards.
   *
   * @see #subscribe(String[], int[], Object, AsyncActionListener)
   *
   * @param topicFilter the topic to subscribe to, which can include wildcards.
   * @param qos the maximum quality of service at which to subscribe. Messages
   *        published at a lower quality of service will be received at the 
   *        published QoS. Messages published at a higher quality of service 
   *        will be received using the QoS specified on the subscribe.
   * 
   * @return token used to track and wait for the subscribe to complete. The 
   *         token will be passed to callback methods if set.
   * 
   * @throws MqttException if there was an error registering the subscription.
   */
  public MqttToken subscribe( String topicFilter, int qos ) throws MqttException;




  /**
   * Subscribe to a topic, which may include wildcards.
   *
   * @see #subscribe(String[], int[], Object, AsyncActionListener)
   *
   * @param topicFilter the topic to subscribe to, which can include wildcards.
   * @param qos the maximum quality of service at which to subscribe. Messages
   *        published at a lower quality of service will be received at the 
   *        published QoS. Messages published at a higher quality of service 
   *        will be received using the QoS specified on the subscribe.
   * @param messageListener a callback to handle incoming messages
   * 
   * @return token used to track and wait for the subscribe to complete. The 
   *         token will be passed to callback methods if set.
   * 
   * @throws MqttException if there was an error registering the subscription.
   */
  public MqttToken subscribe( String topicFilter, int qos, MessageListener messageListener ) throws MqttException;




  /**
   * Subscribe to a topic, which may include wildcards.
   *
   * @see #subscribe(String[], int[], Object, AsyncActionListener)
   *
   * @param topicFilter the topic to subscribe to, which can include wildcards.
   * @param qos the maximum quality of service at which to subscribe. Messages
   *        published at a lower quality of service will be received at the 
   *        published QoS. Messages published at a higher quality of service 
   *        will be received using the QoS specified on the subscribe.
   * @param userContext optional object used to pass context to the callback. 
   *        Use null if not required.
   * @param callback optional listener that will be notified when subscribe has 
   *        completed
   * 
   * @return token used to track and wait for the subscribe to complete. The 
   *         token will be passed to callback methods if set.
   * 
   * @throws MqttException if there was an error registering the subscription.
   */
  public MqttToken subscribe( String topicFilter, int qos, Object userContext, AsyncActionListener callback ) throws MqttException;




  /**
   * Subscribe to a topic, which may include wildcards.
   *
   * @see #subscribe(String[], int[], Object, AsyncActionListener)
   *
   * @param topicFilter the topic to subscribe to, which can include wildcards.
   * @param qos the maximum quality of service at which to subscribe. Messages
   *        published at a lower quality of service will be received at the 
   *        published QoS. Messages published at a higher quality of service 
   *        will be received using the QoS specified on the subscribe.
   * @param userContext optional object used to pass context to the callback. 
   *        Use null if not required.
   * @param callback optional listener that will be notified when subscribe has 
   *        completed
   * @param messageListener a callback to handle incoming messages
   * 
   * @return token used to track and wait for the subscribe to complete. The 
   *         token will be passed to callback methods if set.
   * 
   * @throws MqttException if there was an error registering the subscription.
   */
  public MqttToken subscribe( String topicFilter, int qos, Object userContext, AsyncActionListener callback, MessageListener messageListener ) throws MqttException;




  /**
   * Subscribe to multiple topics, each of which may include wildcards.
   *
   * <p>Provides an optimized way to subscribe to multiple topics compared to
   * subscribing to each one individually.</p>
   *
   * @see #subscribe(String[], int[], Object, AsyncActionListener)
   *
   * @param topicFilters one or more topics to subscribe to, which can include 
   *        wildcards
   * @param qos the maximum quality of service at which to subscribe. Messages
   *        published at a lower quality of service will be received at the 
   *        published QoS. Messages published at a higher quality of service 
   *        will be received using the QoS specified on the subscribe.
   * 
   * @return token used to track and wait for the subscribe to complete. The 
   *         token will be passed to callback methods if set.
   *
   * @throws MqttException if there was an error registering the subscription.
   */
  public MqttToken subscribe( String[] topicFilters, int[] qos ) throws MqttException;




  /**
   * Subscribe to multiple topics, each of which may include wildcards.
   *
   * <p>Provides an optimized way to subscribe to multiple topics compared to
   * subscribing to each one individually.</p>
   *
   * @see #subscribe(String[], int[], Object, AsyncActionListener)
   *
   * @param topicFilters one or more topics to subscribe to, which can include 
   *        wildcards
   * @param qos the maximum quality of service at which to subscribe. Messages
   *        published at a lower quality of service will be received at the 
   *        published QoS. Messages published at a higher quality of service 
   *        will be received using the QoS specified on the subscribe.
   * @param messageListeners one or more callbacks to handle incoming messages
   * 
   * @return token used to track and wait for the subscribe to complete. The 
   *         token will be passed to callback methods if set.
   * 
   * @throws MqttException if there was an error registering the subscription.
   */
  public MqttToken subscribe( String[] topicFilters, int[] qos, MessageListener[] messageListeners ) throws MqttException;




  /**
   * Subscribes to multiple topics, each of which may include wildcards.
   * 
   * <p>Provides an optimized way to subscribe to multiple topics compared to 
   * subscribing to each one individually.</p>
   * 
   * <p>The {@link #setCallback(ClientListener)} method should be called before 
   * this method, otherwise any received messages will be discarded.</p>
   * 
   * <p>If (@link ConnectOptions#setCleanSession(boolean)} was set to true when 
   * when connecting to the server then the subscription remains in place until 
   * either:<ul>
   * <li>The client disconnects</li>
   * <li>An unsubscribe method is called to un-subscribe the topic</li>
   * </ul>
   * 
   * <p>If (@link ConnectOptions#setCleanSession(boolean)} was set to false 
   * when connecting to the server then the subscription remains in place until 
   * either:<ul>
   * <li>An unsubscribe method is called to unsubscribe the topic</li>
   * <li>The next time the client connects with cleanSession set to true</li>
   * </ul>With cleanSession set to false the MQTT server will store messages on 
   * behalf of the client when the client is not connected. The next time the 
   * client connects with the <strong>same client ID</strong> the server will 
   * deliver the stored messages to the client.
   *
   * <p>The method returns control before the subscribe completes. Completion 
   * can be tracked by:<ul>
   * <li>Waiting on the supplied token {@link MqttTokenImpl#waitForCompletion()} 
   * or</li>
   * <li>Passing in a callback {@link AsyncActionListener} to this method</li>
   * </ul>
   *
   * @param topicFilters one or more topics to subscribe to, which can include 
   *        wildcards
   * @param qos the maximum quality of service to subscribe each topic at. 
   *        Messages published at a lower quality of service will be received 
   *        at the published QoS. Messages published at a higher quality of 
   *        service will be received using the QoS specified on the subscribe.
   * @param userContext optional object used to pass context to the callback. 
   *        Use null if not required.
   * @param callback optional listener that will be notified when subscribe has 
   *        completed
   * 
   * @return token used to track and wait for the subscribe to complete. The 
   *         token will be passed to callback methods if set.
   * 
   * @throws MqttException if there was an error registering the subscription.
   * @throws IllegalArgumentException if the two supplied arrays are not the 
   *         same size.
   */
  public MqttToken subscribe( String[] topicFilters, int[] qos, Object userContext, AsyncActionListener callback ) throws MqttException;




  /**
   * Subscribe to multiple topics, each of which may include wildcards.
   *
   * <p>Provides an optimized way to subscribe to multiple topics compared to
   * subscribing to each one individually.</p>
   *
   * @param topicFilters one or more topics to subscribe to, which can include 
   *        wildcards
   * @param qos the maximum quality of service at which to subscribe. Messages
   *        published at a lower quality of service will be received at the 
   *        published QoS. Messages published at a higher quality of service 
   *        will be received using the QoS specified on the subscribe.
   * @param userContext optional object used to pass context to the callback. 
   *        Use null if not required.
   * @param callback optional listener that will be notified when subscribe
   *        has completed
   * @param messageListeners one or more call-backs to handle incoming messages
   * 
   * @return token used to track and wait for the subscribe to complete. The 
   *         token will be passed to callback methods if set.
   * 
   * @throws MqttException if there was an error registering the subscription.
   *
   * @see #subscribe(String[], int[], Object, AsyncActionListener)
   */
  public MqttToken subscribe( String[] topicFilters, int[] qos, Object userContext, AsyncActionListener callback, MessageListener[] messageListeners ) throws MqttException;




  /**
   * Requests the server unsubscribe the client from a topic.
   *
   * @param topicFilter the topic to unsubscribe from. It must match a 
   *        topicFilter specified on an earlier subscribe.
   * 
   * @return token used to track and wait for the unsubscribe to complete. The 
   *         token will be passed to callback methods if set.
   * 
   * @throws MqttException if there was an error unregistering the subscription.
   * 
   * @see #unsubscribe(String[], Object, AsyncActionListener)
   */
  public MqttToken unsubscribe( String topicFilter ) throws MqttException;




  /**
   * Requests the server unsubscribe the client from a topics.
   *
   * @param topicFilter the topic to unsubscribe from. It must match a 
   *        topicFilter specified on an earlier subscribe.
   * @param userContext optional object used to pass context to the callback. 
   *        Use null if not required.
   * @param callback optional listener that will be notified when unsubscribe
   *        has completed
   * 
   * @return token used to track and wait for the unsubscribe to complete. The 
   *         token will be passed to callback methods if set.
   * 
   * @throws MqttException if there was an error unregistering the subscription.
   *
   * @see #unsubscribe(String[], Object, AsyncActionListener)
   */
  public MqttToken unsubscribe( String topicFilter, Object userContext, AsyncActionListener callback ) throws MqttException;




  /**
   * Requests the server unsubscribe the client from one or more topics.
   *
   * @see #unsubscribe(String[], Object, AsyncActionListener)
   *
   * @param topicFilters one or more topics to unsubscribe from. Each 
   *        topicFilter must match one specified on an earlier subscribe.
   * 
   * @return token used to track and wait for the unsubscribe to complete. The 
   *         token will be passed to callback methods if set.
   * 
   * @throws MqttException if there was an error unregistering the subscription.
   */
  public MqttToken unsubscribe( String[] topicFilters ) throws MqttException;




  /**
   * Requests the server unsubscribe the client from one or more topics.
   * 
   * <p>Unsubscribing is the opposite of subscribing. When the server receives 
   * the unsubscribe request it looks to see if it can find a matching 
   * subscription for the client and then removes it. After this point the 
   * server will send no more messages to the client for this subscription.</p>
   * 
   * <p>The topic(s) specified on the unsubscribe must match the topic(s) 
   * specified in the original subscribe request for the unsubscribe to 
   * succeed.</p>
   * 
   * <p>The method returns control before the unsubscribe completes. Completion 
   * can be tracked by:<ul>
   * <li>Waiting on the returned token {@link 
   * MqttTokenImpl#waitForCompletion()} or</li>
   * <li>Passing in a callback {@link AsyncActionListener} to this method</li>
   * </ul>
   *
   * @param topicFilters one or more topics to unsubscribe from. Each 
   *        topicFilter must match one specified on an earlier subscribe.
   * @param userContext optional object used to pass context to the callback. 
   *        Use null if not required.
   * @param callback optional listener that will be notified when unsubscribe
   *        has completed
   * 
   * @return token used to track and wait for the unsubscribe to complete. The 
   *         token will be passed to callback methods if set.
   * 
   * @throws MqttException if there was an error unregistering the subscription.
   */
  public MqttToken unsubscribe( String[] topicFilters, Object userContext, AsyncActionListener callback ) throws MqttException;




  /**
   * Return the connection instance used by this client to exchange data with 
   * the broker.
   *  
   * @return the connection used by this client.
   */
  public Connection getconnection();

}
