package coyote.commons.network.mqtt;

import java.util.Properties;

import javax.net.SocketFactory;

import coyote.commons.network.mqtt.ClientListener;
import coyote.commons.network.mqtt.MessageListener;
import coyote.commons.network.mqtt.MqttClientImpl;
import coyote.commons.network.mqtt.MqttConnectOptions;
import coyote.commons.network.mqtt.MqttDeliveryToken;
import coyote.commons.network.mqtt.MqttException;
import coyote.commons.network.mqtt.MqttMessage;
import coyote.commons.network.mqtt.MqttSecurityException;
import coyote.commons.network.mqtt.MqttToken;
import coyote.commons.network.mqtt.Topic;
import coyote.commons.network.mqtt.cache.CacheException;
import coyote.commons.network.mqtt.cache.ClientCache;
import coyote.commons.network.mqtt.cache.FileCache;


/**
 * Lightweight client for talking to an MQTT server using methods that block
 * until an operation completes.
 *
 * <p>This class implements the blocking {@link MqttBlockingClient} client interface 
 * where all actions block until they have completed (or timed out).</p>
 * 
 * <p>An application can connect to an MQTT server using:
 * <ul>
 * <li>A plain TCP socket
 * <li>An secure SSL/TLS socket
 * </ul>
 * </p>
 * <p>To enable messages to be delivered even across network and client restarts
 * messages need to be safely stored until the message has been delivered at the requested
 * quality of service. A modular caching mechanism is provided to store the messages.
 * </p>
 * <p>By default {@link FileCache} is used to store messages to a file.
 * If caching is set to null then messages are stored in memory and hence can  be lost
 * if the client, Java runtime or device shuts down.
 * </p>
 * <p>If connecting with {@link MqttConnectOptions#setCleanSession(boolean)} set to true it
 * is safe to use memory caching as all state it cleared when a client disconnects. If
 * connecting with cleanSession set to false, to provide reliable message delivery
 * then a persistent message store should be used such as the default one. </p>
 * <p>The message store interface is modular. Different stores can be used by implementing
 * the {@link ClientCache} interface and passing it to the clients constructor.
 * </p>
 *
 * @see MqttBlockingClient
 */
public class MqttBlockingClientImpl implements MqttBlockingClient {

  // used as a delegate for some implementations
  protected MqttClientImpl asyncClient = null;

  protected long timeToWait = -1; // How long each method should wait for action to complete




  /**
   * Returns a randomly generated client identifier based on the current users 
   * login name and the system time.
   * 
   * <p>When cleanSession is set to false, an application must ensure it uses 
   * the same client identifier when it reconnects to the server to resume 
   * state and maintain assured message delivery.</p>
   * 
   * @return a generated client identifier
   * 
   * @see MqttConnectOptions#setCleanSession(boolean)
   */
  public static String generateClientId() {
    return MqttClientImpl.generateClientId();
  }




  /**
   * Create an MqttClient that can be used to communicate with an MQTT server.
   * <p>
   * The address of a server can be specified on the constructor. Alternatively
   * a list containing one or more servers can be specified using the
   * {@link MqttConnectOptions#setServerURIs(String[]) setServerURIs} method
   * on MqttConnectOptions.
   *
   * <p>The <code>serverURI</code> parameter is typically used with the
   * the <code>clientId</code> parameter to form a key. The key
   * is used to store and reference messages while they are being delivered.
   * Hence the serverURI specified on the constructor must still be specified even if a list
   * of servers is specified on an MqttConnectOptions object.
   * The serverURI on the constructor must remain the same across
   * restarts of the client for delivery of messages to be maintained from a given
   * client to a given server or set of servers.
   *
   * <p>The address of the server to connect to is specified as a URI. Two types of
   * connection are supported <code>tcp://</code> for a TCP connection and
   * <code>ssl://</code> for a TCP connection secured by SSL/TLS.
   * For example:
   * <ul>
   * 	<li><code>tcp://localhost:1883</code></li>
   * 	<li><code>ssl://localhost:8883</code></li>
   * </ul>
   * If the port is not specified, it will
   * default to 1883 for <code>tcp://</code>" URIs, and 8883 for <code>ssl://</code> URIs.
   * </p>
   *
   * <p>
   * A client identifier <code>clientId</code> must be specified and be less that 65535 characters.
   * It must be unique across all clients connecting to the same
   * server. The clientId is used by the server to store data related to the client,
   * hence it is important that the clientId remain the same when connecting to a server
   * if durable subscriptions or reliable messaging are required.
   * <p>A convenience method is provided to generate a random client id that
   * should satisfy this criteria - {@link #generateClientId()}. As the client identifier
   * is used by the server to identify a client when it reconnects, the client must use the
   * same identifier between connections if durable subscriptions or reliable
   * delivery of messages is required.
   * </p>
   * <p>
   * In Java SE, SSL can be configured in one of several ways, which the
   * client will use in the following order:
   * </p>
   * <ul>
   * 	<li><strong>Supplying an <code>SSLSocketFactory</code></strong> - applications can
   * use {@link MqttConnectOptions#setSocketFactory(SocketFactory)} to supply
   * a factory with the appropriate SSL settings.</li>
   * 	<li><strong>SSL Properties</strong> - applications can supply SSL settings as a
   * simple Java Properties using {@link MqttConnectOptions#setSSLProperties(Properties)}.</li>
   * 	<li><strong>Use JVM settings</strong> - There are a number of standard
   * Java system properties that can be used to configure key and trust stores.</li>
   * </ul>
   *
   * <p>In Java ME, the platform settings are used for SSL connections.</p>
   *
   * <p>An instance of the default caching mechanism {@link FileCache}
   * is used by the client. To specify a different caching mechanism or to turn
   * off caching, use the {@link MqttBlockingClient} constructor.</p>
   *
   * @param serverURI the address of the server to connect to, specified as a 
   *        URI. Can be overridden using {@link 
   *        MqttConnectOptions#setServerURIs(String[])}
   * @param clientId a client identifier that is unique on the server being 
   *        connected to
   * 
   * @throws IllegalArgumentException if the URI does not start with "tcp://", 
   *         "ssl://" or "local://".
   * @throws IllegalArgumentException if the clientId is null or is greater 
   *         than 65535 characters in length
   * @throws MqttException if any other problem was encountered
   */
  public MqttBlockingClientImpl( final String serverURI, final String clientId ) throws MqttException {
    this( serverURI, clientId, new FileCache() );
  }




  /**
   * Create an MqttClient that can be used to communicate with an MQTT server.
   * <p>
   * The address of a server can be specified on the constructor. Alternatively
   * a list containing one or more servers can be specified using the
   * {@link MqttConnectOptions#setServerURIs(String[]) setServerURIs} method
   * on MqttConnectOptions.
   *
   * <p>The <code>serverURI</code> parameter is typically used with the
   * the <code>clientId</code> parameter to form a key. The key
   * is used to store and reference messages while they are being delivered.
   * Hence the serverURI specified on the constructor must still be specified even if a list
   * of servers is specified on an MqttConnectOptions object.
   * The serverURI on the constructor must remain the same across
   * restarts of the client for delivery of messages to be maintained from a given
   * client to a given server or set of servers.
   *
   * <p>The address of the server to connect to is specified as a URI. Two types of
   * connection are supported <code>tcp://</code> for a TCP connection and
   * <code>ssl://</code> for a TCP connection secured by SSL/TLS.
   * For example:
   * <ul>
   * 	<li><code>tcp://localhost:1883</code></li>
   * 	<li><code>ssl://localhost:8883</code></li>
   * </ul>
   * If the port is not specified, it will
   * default to 1883 for <code>tcp://</code>" URIs, and 8883 for <code>ssl://</code> URIs.
   * </p>
   *
   * <p>
   * A client identifier <code>clientId</code> must be specified and be less that 65535 characters.
   * It must be unique across all clients connecting to the same
   * server. The clientId is used by the server to store data related to the client,
   * hence it is important that the clientId remain the same when connecting to a server
   * if durable subscriptions or reliable messaging are required.
   * <p>A convenience method is provided to generate a random client id that
   * should satisfy this criteria - {@link #generateClientId()}. As the client identifier
   * is used by the server to identify a client when it reconnects, the client must use the
   * same identifier between connections if durable subscriptions or reliable
   * delivery of messages is required.
   * </p>
   * <p>
   * In Java SE, SSL can be configured in one of several ways, which the
   * client will use in the following order:
   * </p>
   * <ul>
   * 	<li><strong>Supplying an <code>SSLSocketFactory</code></strong> - applications can
   * use {@link MqttConnectOptions#setSocketFactory(SocketFactory)} to supply
   * a factory with the appropriate SSL settings.</li>
   * 	<li><strong>SSL Properties</strong> - applications can supply SSL settings as a
   * simple Java Properties using {@link MqttConnectOptions#setSSLProperties(Properties)}.</li>
   * 	<li><strong>Use JVM settings</strong> - There are a number of standard
   * Java system properties that can be used to configure key and trust stores.</li>
   * </ul>
   *
   * <p>In Java ME, the platform settings are used for SSL connections.</p>
   * <p>
   * A caching mechanism is used to enable reliable messaging.
   * For messages sent at qualities of service (QoS) 1 or 2 to be reliably delivered,
   * messages must be stored (on both the client and server) until the delivery of the message
   * is complete. If messages are not safely stored when being delivered then
   * a failure in the client or server can result in lost messages. A modular
   * caching mechanism is supported via the {@link ClientCache}
   * interface. An implementer of this interface that safely stores messages
   * must be specified in order for delivery of messages to be reliable. In
   * addition {@link MqttConnectOptions#setCleanSession(boolean)} must be set
   * to false. In the event that only QoS 0 messages are sent or received or
   * cleanSession is set to true then a safe store is not needed.
   * </p>
   * <p>An implementation of file-based caching is provided in
   * class {@link FileCache} which will work in all Java SE based
   * systems. If no caching is needed, the caching parameter
   * can be explicitly set to <code>null</code>.</p>
   *
   * @param serverURI the address of the server to connect to, specified as a URI. Can be overridden using
   * {@link MqttConnectOptions#setServerURIs(String[])}
   * @param clientId a client identifier that is unique on the server being connected to
   * @param cache the caching object to use to store in-flight message. If null then the
   * default caching mechanism is used
   * @throws IllegalArgumentException if the URI does not start with
   * "tcp://", "ssl://" or "local://"
   * @throws IllegalArgumentException if the clientId is null or is greater than 65535 characters in length
   * @throws MqttException if any other problem was encountered
   */
  public MqttBlockingClientImpl( final String serverURI, final String clientId, final ClientCache cache ) throws MqttException {
    asyncClient = new MqttClientImpl( serverURI, clientId, cache );
  }




  @Override
  public void close() throws MqttException {
    asyncClient.close();
  }




  @Override
  public void connect() throws MqttSecurityException, MqttException {
    this.connect( new MqttConnectOptions() );
  }




  @Override
  public void connect( final MqttConnectOptions options ) throws MqttSecurityException, MqttException {
    asyncClient.connect( options, null, null ).waitForCompletion( getTimeToWait() );
  }




  @Override
  public MqttToken connectWithResult( final MqttConnectOptions options ) throws MqttSecurityException, MqttException {
    final MqttToken tok = asyncClient.connect( options, null, null );
    tok.waitForCompletion( getTimeToWait() );
    return tok;
  }




  @Override
  public void disconnect() throws MqttException {
    asyncClient.disconnect().waitForCompletion();
  }




  @Override
  public void disconnect( final long quiesceTimeout ) throws MqttException {
    asyncClient.disconnect( quiesceTimeout, null, null ).waitForCompletion();
  }




  @Override
  public void disconnectForcibly() throws MqttException {
    asyncClient.disconnectForcibly();
  }




  @Override
  public void disconnectForcibly( final long disconnectTimeout ) throws MqttException {
    asyncClient.disconnectForcibly( disconnectTimeout );
  }




  @Override
  public void disconnectForcibly( final long quiesceTimeout, final long disconnectTimeout ) throws MqttException {
    asyncClient.disconnectForcibly( quiesceTimeout, disconnectTimeout );
  }




  @Override
  public String getClientId() {
    return asyncClient.getClientId();
  }




  /**
   * @return the currently connected server URI
   */
  public String getCurrentServerURI() {
    return asyncClient.getCurrentServerURI();
  }




  @Override
  public MqttDeliveryToken[] getPendingDeliveryTokens() {
    return asyncClient.getPendingDeliveryTokens();
  }




  @Override
  public String getServerURI() {
    return asyncClient.getServerURI();
  }




  /**
   * Return the maximum time to wait for an action to complete.
   * 
   * @see MqttBlockingClientImpl#setTimeToWait(long)
   */
  public long getTimeToWait() {
    return timeToWait;
  }




  @Override
  public Topic getTopic( final String topic ) {
    return asyncClient.getTopic( topic );
  }




  @Override
  public boolean isConnected() {
    return asyncClient.isConnected();
  }




  @Override
  public void messageArrivedComplete( final int messageId, final int qos ) throws MqttException {
    asyncClient.messageArrivedComplete( messageId, qos );
  }




  @Override
  public void publish( final String topic, final byte[] payload, final int qos, final boolean retained ) throws MqttException, CacheException {
    final MqttMessage message = new MqttMessage( payload );
    message.setQos( qos );
    message.setRetained( retained );
    this.publish( topic, message );
  }




  @Override
  public void publish( final String topic, final MqttMessage message ) throws MqttException, CacheException {
    asyncClient.publish( topic, message, null, null ).waitForCompletion( getTimeToWait() );
  }




  @Override
  public void reconnect() throws MqttException {
    asyncClient.reconnect();
  }




  @Override
  public void setCallback( final ClientListener callback ) {
    asyncClient.setCallback( callback );
  }




  @Override
  public void setManualAcks( final boolean manualAcks ) {
    asyncClient.setManualAcks( manualAcks );
  }




  /**
   * Set the maximum time to wait for an action to complete.
   * 
   * <p>Set the maximum time to wait for an action to complete before returning 
   * control to the invoking application. Control is returned when:<ul>
   * <li>the action completes</li>
   * <li>or when the timeout if exceeded</li>
   * <li>or when the client is disconnect/shutdown</li><ul>
   * The default value is -1 which means the action will not timeout. In the 
   * event of a timeout the action carries on running in the background until 
   * it completes. The timeout is used on methods that block while the action 
   * is in progress.</p>
   * 
   * @param millis before the action times out. A value or 0 or -1 will wait 
   *        until the action finishes and not timeout.
   */
  public void setTimeToWait( final long millis ) throws IllegalArgumentException {
    if ( millis < -1 ) {
      throw new IllegalArgumentException();
    }
    timeToWait = millis;
  }




  @Override
  public void subscribe( final String topicFilter ) throws MqttException {
    this.subscribe( new String[] { topicFilter }, new int[] { 1 } );
  }




  @Override
  public void subscribe( final String topicFilter, final MessageListener messageListener ) throws MqttException {
    this.subscribe( new String[] { topicFilter }, new int[] { 1 }, new MessageListener[] { messageListener } );
  }




  @Override
  public void subscribe( final String topicFilter, final int qos ) throws MqttException {
    this.subscribe( new String[] { topicFilter }, new int[] { qos } );
  }




  @Override
  public void subscribe( final String topicFilter, final int qos, final MessageListener messageListener ) throws MqttException {
    this.subscribe( new String[] { topicFilter }, new int[] { qos }, new MessageListener[] { messageListener } );
  }




  @Override
  public void subscribe( final String[] topicFilters ) throws MqttException {
    final int[] qos = new int[topicFilters.length];
    for ( int i = 0; i < qos.length; i++ ) {
      qos[i] = 1;
    }
    this.subscribe( topicFilters, qos );
  }




  @Override
  public void subscribe( final String[] topicFilters, final MessageListener[] messageListeners ) throws MqttException {
    final int[] qos = new int[topicFilters.length];
    for ( int i = 0; i < qos.length; i++ ) {
      qos[i] = 1;
    }
    this.subscribe( topicFilters, qos, messageListeners );
  }




  @Override
  public void subscribe( final String[] topicFilters, final int[] qos ) throws MqttException {
    final MqttToken tok = asyncClient.subscribe( topicFilters, qos, null, null );
    tok.waitForCompletion( getTimeToWait() );
    final int[] grantedQos = tok.getGrantedQos();
    for ( int i = 0; i < grantedQos.length; ++i ) {
      qos[i] = grantedQos[i];
    }
    if ( ( grantedQos.length == 1 ) && ( qos[0] == 0x80 ) ) {
      throw new MqttException( MqttException.SUBSCRIBE_FAILED );
    }
  }




  @Override
  public void subscribe( final String[] topicFilters, final int[] qos, final MessageListener[] messageListeners ) throws MqttException {
    this.subscribe( topicFilters, qos );

    // add message handlers to the list for this client
    for ( int i = 0; i < topicFilters.length; ++i ) {
      asyncClient.connection.setMessageListener( topicFilters[i], messageListeners[i] );
    }
  }




  @Override
  public void unsubscribe( final String topicFilter ) throws MqttException {
    unsubscribe( new String[] { topicFilter } );
  }




  @Override
  public void unsubscribe( final String[] topicFilters ) throws MqttException {
    // message handlers removed in the async client unsubscribe below
    asyncClient.unsubscribe( topicFilters, null, null ).waitForCompletion( getTimeToWait() );
  }

}
