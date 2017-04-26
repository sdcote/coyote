package coyote.commons.network.mqtt;

import java.util.Hashtable;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;

import javax.net.SocketFactory;
import javax.net.ssl.SSLSocketFactory;

import coyote.commons.network.mqtt.cache.CacheException;
import coyote.commons.network.mqtt.cache.ClientCache;
import coyote.commons.network.mqtt.cache.FileCache;
import coyote.commons.network.mqtt.cache.MemoryCache;
import coyote.commons.network.mqtt.network.LocalTransport;
import coyote.commons.network.mqtt.network.SSLSocketFactoryFactory;
import coyote.commons.network.mqtt.network.SSLTransport;
import coyote.commons.network.mqtt.network.TCPTransport;
import coyote.commons.network.mqtt.network.Transport;
import coyote.commons.network.mqtt.network.WebSocketTransport;
import coyote.commons.network.mqtt.network.websocket.WebSocketSecureTransport;
import coyote.commons.network.mqtt.protocol.DisconnectMessage;
import coyote.commons.network.mqtt.protocol.MqttUnsubscribe;
import coyote.commons.network.mqtt.protocol.PublishMessage;
import coyote.commons.network.mqtt.protocol.SubscribeMessage;
import coyote.loader.log.Log;
import coyote.loader.log.LogMsg;


/**
 * Lightweight client for talking to an MQTT server using non-blocking methods
 * that allow an operation to run in the background.
 *
 * <p>This class implements the non-blocking {@link MqttClient} client 
 * interface allowing applications to initiate MQTT actions and then carry on
 * working while the MQTT action completes on a background thread. This 
 * implementation is compatible with all Java SE runtimes from 1.4.2 and 
 * up.</p>
 * 
 * <p>An application can connect to an MQTT server using:<ul>
 * <li>A plain TCP socket</li>
 * <li>A secure SSL/TLS socket</li></ul>
 * 
 * <p>To enable messages to be delivered even across network and client 
 * restarts messages need to be safely stored until the message has been 
 * delivered at the requested quality of service. A modular caching mechanism 
 * is provided to store the messages.</p>
 * 
 * <p>By default {@link FileCache} is used to store messages to a file. If 
 * persistence is set to null then messages are stored in memory and hence can 
 * be lost if the client, Java runtime or device shuts down.</p>
 * 
 * <p>If connecting with {@link MqttConnectOptions#setCleanSession(boolean)} set to 
 * true it is safe to use memory caching as all state is cleared when a client 
 * disconnects. If connecting with cleanSession set to false in order to 
 * provide reliable message delivery then a persistent message store such as 
 * the default one should be used.</p>
 * 
 * <p>The message store interface is modular. Different stores can be used by 
 * implementing the {@link ClientCache} interface and passing it to the clients 
 * constructor.</p>
 *
 * @see MqttClient
 */
public class MqttClientImpl implements MqttClient {
  private static final String CLIENT_ID_PREFIX = "ciot";
  private static final long QUIESCE_TIMEOUT = 30000; // ms
  private static final long DISCONNECT_TIMEOUT = 10000; // ms
  private static final char MIN_HIGH_SURROGATE = '\uD800';
  private static final char MAX_HIGH_SURROGATE = '\uDBFF';
  private final String clientId;
  private final String serverURI;
  protected Connection connection;
  private final Hashtable topics;
  private ClientCache cache;
  private ClientListener listener;
  private MqttConnectOptions connOpts;
  private Object userContext;
  private Timer reconnectTimer; // Automatic reconnect timer
  private static int reconnectDelay = 1000; // Reconnect delay, starts at 1 second
  private boolean reconnecting = false;




  /**
   * @param ch
   * 
   * @return returns 'true' if the character is a high-surrogate code unit
   */
  protected static boolean Character_isHighSurrogate( final char ch ) {
    return ( ch >= MIN_HIGH_SURROGATE ) && ( ch <= MAX_HIGH_SURROGATE );
  }




  /**
   * Returns a randomly generated client identifier based on the the fixed 
   * prefix (ciot) and the system time.
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
    //length of nanoTime = 15, so total length = 19  < 65535(defined in spec) 
    return CLIENT_ID_PREFIX + System.nanoTime();
  }




  /**
   * Create an MqttClientImpl that is used to communicate with an MQTT server.
   * 
   * <p>The address of a server can be specified on the constructor. 
   * Alternatively a list containing one or more servers can be specified using 
   * the {@link MqttConnectOptions#setServerURIs(String[]) setServerURIs} method on 
   * MqttConnectOptions.</p>
   *
   * <p>The {@code serverURI} parameter is typically used with the the {@code 
   * clientId} parameter to form a key. The key is used to store and reference 
   * messages while they are being delivered. Hence the serverURI specified on 
   * the constructor must still be specified even if a list of servers is 
   * specified on an MqttConnectOptions object. The serverURI on the 
   * constructor must remain the same across restarts of the client for 
   * delivery of messages to be maintained from a given client to a given 
   * server or set of servers.</p>
   *
   * <p>The address of the server to connect to is specified as a URI. Two 
   * types of connection are supported {@code tcp://} for a TCP connection and
   * {@code ssl://} for a TCP connection secured by SSL/TLS.  For example:<ul>
   * 	<li>{@code tcp://localhost:1883}</li>
   * 	<li>{@code ssl://localhost:8883}</li>
   * </ul>
   * If the port is not specified, it will default to 1883 for {@code tcp://}" 
   * URIs, and 8883 for {@code ssl://} URIs.
   *
   * <p>A client identifier {@code clientId} must be specified and be less that 
   * 65535 characters. It must be unique across all clients connecting to the 
   * same server. The clientId is used by the server to store data related to 
   * the client, hence it is important that the clientId remain the same when 
   * connecting to a server if durable subscriptions or reliable messaging are 
   * required.</p>
   * 
   * <p>A convenience method is provided to generate a random client id that
   * should satisfy this criteria - {@link #generateClientId()}. As the client 
   * identifier is used by the server to identify a client when it reconnects, 
   * the client must use the same identifier between connections if durable 
   * subscriptions or reliable delivery of messages is required.</p>
   * 
   * <p>In Java SE, SSL can be configured in one of several ways, which the
   * client will use in the following order:<ul>
   * <li><strong>Supplying an {@code SSLSocketFactory}</strong> - applications 
   * can use {@link MqttConnectOptions#setSocketFactory(SocketFactory)} to supply
   * a factory with the appropriate SSL settings.</li>
   * <li><strong>SSL Properties</strong> - applications can supply SSL settings 
   * as a simple Java Properties using {@link 
   * MqttConnectOptions#setSSLProperties(Properties)}.</li>
   * <li><strong>Use JVM settings</strong> - There are a number of standard 
   * Java system properties that can be used to configure key and trust 
   * stores.</li></ul>
   *
   * <p>In Java ME, the platform settings are used for SSL connections.</p>
   *
   * <p>An instance of the default persistence mechanism {@link FileCache}
   * is used by the client. To specify a different persistence mechanism or to 
   * turn off persistence, use the {@link #MqttClientImpl(String, String, 
   * ClientCache)} constructor.</p>
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
  public MqttClientImpl( final String serverURI, final String clientId ) throws MqttException {
    this( serverURI, clientId, new FileCache() );
  }




  public MqttClientImpl( final String serverURI, final String clientId, final ClientCache persistence ) throws MqttException {
    this( serverURI, clientId, persistence, new TimerPingSender() );
  }




  /**
  * Create an MqttClientImpl that is used to communicate with an MQTT server.
   * 
   * <p>The address of a server can be specified on the constructor. 
   * Alternatively a list containing one or more servers can be specified using 
   * the {@link MqttConnectOptions#setServerURIs(String[]) setServerURIs} method on 
   * MqttConnectOptions.</p>
   *
   * <p>The {@code serverURI} parameter is typically used with the the {@code 
   * clientId} parameter to form a key. The key is used to store and reference 
   * messages while they are being delivered. Hence the serverURI specified on 
   * the constructor must still be specified even if a list of servers is 
   * specified on an MqttConnectOptions object. The serverURI on the 
   * constructor must remain the same across restarts of the client for 
   * delivery of messages to be maintained from a given client to a given 
   * server or set of servers.</p>
   *
   * <p>The address of the server to connect to is specified as a URI. Two 
   * types of connection are supported {@code tcp://} for a TCP connection and
   * {@code ssl://} for a TCP connection secured by SSL/TLS.  For example:<ul>
   *  <li>{@code tcp://localhost:1883}</li>
   *  <li>{@code ssl://localhost:8883}</li>
   * </ul>
   * If the port is not specified, it will default to 1883 for {@code tcp://}" 
   * URIs, and 8883 for {@code ssl://} URIs.
   *
   * <p>A client identifier {@code clientId} must be specified and be less that 
   * 65535 characters. It must be unique across all clients connecting to the 
   * same server. The clientId is used by the server to store data related to 
   * the client, hence it is important that the clientId remain the same when 
   * connecting to a server if durable subscriptions or reliable messaging are 
   * required.</p>
   * 
   * <p>A convenience method is provided to generate a random client id that
   * should satisfy this criteria - {@link #generateClientId()}. As the client 
   * identifier is used by the server to identify a client when it reconnects, 
   * the client must use the same identifier between connections if durable 
   * subscriptions or reliable delivery of messages is required.</p>
   * 
   * <p>In Java SE, SSL can be configured in one of several ways, which the
   * client will use in the following order:<ul>
   * <li><strong>Supplying an {@code SSLSocketFactory}</strong> - applications 
   * can use {@link MqttConnectOptions#setSocketFactory(SocketFactory)} to supply
   * a factory with the appropriate SSL settings.</li>
   * <li><strong>SSL Properties</strong> - applications can supply SSL settings 
   * as a simple Java Properties using {@link 
   * MqttConnectOptions#setSSLProperties(Properties)}.</li>
   * <li><strong>Use JVM settings</strong> - There are a number of standard 
   * Java system properties that can be used to configure key and trust 
   * stores.</li></ul>
   *
   * <p>In Java ME, the platform settings are used for SSL connections.</p>
   *
   * <p>An instance of the default persistence mechanism {@link FileCache}
   * is used by the client. To specify a different persistence mechanism or to 
   * turn off persistence, use the {@link #MqttClientImpl(String, String, 
   * ClientCache)} constructor.</p>
   * 
   * @param serverURI the address of the server to connect to, specified as a 
   *        URI. Can be overridden using {@link 
   *        MqttConnectOptions#setServerURIs(String[])}
   * @param clientId a client identifier that is unique on the server being 
   *        connected to
   * @param cache the caching class to use to store in-flight message. If null 
   *        then the default persistence mechanism is used
   * 
   * @throws IllegalArgumentException if the URI does not start with "tcp://", 
   *         "ssl://" or "local://"
   * @throws IllegalArgumentException if the clientId is null or is greater 
   *         than 65535 characters in length
   * @throws MqttException if any other problem was encountered
   */
  public MqttClientImpl( final String serverURI, final String clientId, final ClientCache cache, final PingSender pingSender ) throws MqttException {

    if ( clientId == null ) { //Support empty client Id, 3.1.1 standard
      throw new IllegalArgumentException( "Null clientId" );
    }
    // Count characters, surrogate pairs count as one character.
    int clientIdLength = 0;
    for ( int i = 0; i < ( clientId.length() - 1 ); i++ ) {
      if ( Character_isHighSurrogate( clientId.charAt( i ) ) ) {
        i++;
      }
      clientIdLength++;
    }
    if ( clientIdLength > 65535 ) {
      throw new IllegalArgumentException( "ClientId longer than 65535 characters" );
    }

    MQTT.validateURI( serverURI );

    this.serverURI = serverURI;
    this.clientId = clientId;

    this.cache = cache;
    if ( this.cache == null ) {
      this.cache = new MemoryCache();
    }

    Log.append( MQTT.EVENT, LogMsg.createMsg( MQTT.MSG, "client.init", clientId, serverURI, cache ) );

    this.cache.open( clientId, serverURI );
    connection = new Connection( this, this.cache, pingSender );
    this.cache.close();
    topics = new Hashtable();
  }




  /**
   * Attempts to reconnect the client to the server.
   * 
   * <p>If successful, it will make sure that there are no further reconnects 
   * scheduled. However if the connect fails, the delay will double up to 128 
   * seconds and will re-schedule the reconnect for after the delay.</p>
   * 
   * <p>Any thrown exceptions are logged but not acted upon as it is assumed 
   * that they are being thrown due to the server being off-line and so 
   * reconnect attempts will continue.</p>
   */
  private void attemptReconnect() {
    Log.append( MQTT.EVENT, LogMsg.createMsg( MQTT.MSG, "client.attempt_reconnect", clientId ) );
    try {
      connect( connOpts, userContext, new AsyncActionListener() {

        @Override
        public void onFailure( final MqttToken asyncActionToken, final Throwable exception ) {
          Log.append( MQTT.EVENT, LogMsg.createMsg( MQTT.MSG, "client.attempt_reconnect_failure", asyncActionToken.getClient().getClientId() ) );
          if ( reconnectDelay < 128000 ) {
            reconnectDelay = reconnectDelay * 2;
          }
          rescheduleReconnectCycle( reconnectDelay );
        }




        @Override
        public void onSuccess( final MqttToken asyncActionToken ) {
          Log.append( MQTT.EVENT, LogMsg.createMsg( MQTT.MSG, "client.attempt_reconnect_success", asyncActionToken.getClient().getClientId() ) );
          connection.setRestingState( false );
          stopReconnectCycle();
        }
      } );
    } catch ( final MqttSecurityException ex ) {
      Log.append( MQTT.EVENT, LogMsg.createMsg( MQTT.MSG, "client.attempt_reconnect_security_exception", ex ) );
    } catch ( final MqttException ex ) {
      Log.append( MQTT.EVENT, LogMsg.createMsg( MQTT.MSG, "client.attempt_reconnect_exception", ex ) );
    }
  }




  /**
   * Check and send a ping if needed.
   * 
   * <p>By default, client sends PingReq to server to keep the connection to 
   * server. Some platforms, such as Android, cannot use this mechanism to the
   * developer needs to handle the ping request manually with this method.</p>
   * 
   * @throws MqttException for other errors encountered while publishing the 
   *         message.
   */
  public MqttToken checkPing( final Object userContext, final AsyncActionListener callback ) throws MqttException {
    MqttTokenImpl token;
    token = connection.checkForActivity();
    return token;
  }




  /**
   * @see coyote.commons.network.mqtt.MqttClient#close()
   */
  @Override
  public void close() throws MqttException {
    connection.close();
  }




  /**
   * @see coyote.commons.network.mqtt.MqttClient#connect()
   */
  @Override
  public MqttToken connect() throws MqttException, MqttSecurityException {
    return this.connect( null, null );
  }




  /**
   * @see coyote.commons.network.mqtt.MqttClient#connect(coyote.commons.network.mqtt.MqttConnectOptions)
   */
  @Override
  public MqttToken connect( final MqttConnectOptions options ) throws MqttException, MqttSecurityException {
    return this.connect( options, null, null );
  }




  @Override
  public MqttToken connect( final MqttConnectOptions options, final Object userContext, final AsyncActionListener callback ) throws MqttException, MqttSecurityException {
    if ( connection.isConnected() ) {
      throw MQTT.createMqttException( MqttException.CLIENT_CONNECTED );
    }
    if ( connection.isConnecting() ) {
      throw new MqttException( MqttException.CONNECT_IN_PROGRESS );
    }
    if ( connection.isDisconnecting() ) {
      throw new MqttException( MqttException.CLIENT_DISCONNECTING );
    }
    if ( connection.isClosed() ) {
      throw new MqttException( MqttException.CLIENT_CLOSED );
    }

    connOpts = options;
    this.userContext = userContext;
    final boolean automaticReconnect = options.isAutomaticReconnect();

    Log.append( MQTT.EVENT, LogMsg.createMsg( MQTT.MSG, "client.cleaning_session", options.isCleanSession(), options.getConnectionTimeout(), options.getKeepAliveInterval(), options.getUserName(), ( ( null == options.getPassword() ) ? "[null]" : "[notnull]" ), ( ( null == options.getWillMessage() ) ? "[null]" : "[notnull]" ), userContext, callback ) );
    connection.setTransports( createTransports( serverURI, options ) );

    // when the connection is lost, this will restart the connection cycle
    connection.setReconnectCallback( new ClientListener() {

      @Override
      public void connectComplete( final boolean reconnect, final String serverURI ) {}




      @Override
      public void connectionLost( final Throwable cause ) {
        if ( automaticReconnect ) {
          // Automatic reconnect is set so make sure the connection is in the 
          // resting state
          connection.setRestingState( true );
          reconnecting = true;
          startReconnectCycle();
        }
      }




      @Override
      public void deliveryComplete( final MqttDeliveryToken token ) {}




      @Override
      public void messageArrived( final String topic, final MqttMessage message ) throws Exception {}
    } );

    // Insert our own callback to iterate through the URIs till the connect succeeds
    final MqttTokenImpl userToken = new MqttTokenImpl();
    final ConnectActionListener connectActionListener = new ConnectActionListener( this, cache, connection, options, userToken, userContext, callback, reconnecting );
    userToken.setActionCallback( connectActionListener );
    userToken.setUserContext( this );

    // reset the network transport to the primary (first in the list)
    connection.setTransportIndex( 0 );
    connectActionListener.connect();

    return userToken;
  }




  @Override
  public MqttToken connect( final Object userContext, final AsyncActionListener callback ) throws MqttException, MqttSecurityException {
    return this.connect( new MqttConnectOptions(), userContext, callback );
  }




  /**
   * Factory method to create the correct network transport, based on the 
   * supplied broker URI.
   *
   * @param broker the URI for the server.
   * @param options connection options
   * 
   * @return a network transport appropriate to the specified address.
   */
  private Transport createTransport( final String broker, final MqttConnectOptions options ) throws MqttException, MqttSecurityException {
    Log.append( MQTT.EVENT, LogMsg.createMsg( MQTT.MSG, "client.determining_network_transport", broker ) );

    Transport retval;
    String shortAddress;
    String host;
    int port;
    SocketFactory factory = options.getSocketFactory();

    final int serverURIType = MQTT.validateURI( broker );

    switch ( serverURIType ) {
      case MQTT.TCP_URI:
        shortAddress = broker.substring( 6 );
        host = getHostName( shortAddress );
        port = getPort( shortAddress, 1883 );
        if ( factory == null ) {
          factory = SocketFactory.getDefault();
        } else if ( factory instanceof SSLSocketFactory ) {
          throw MQTT.createMqttException( MqttException.SOCKET_FACTORY_MISMATCH );
        }
        retval = new TCPTransport( factory, host, port, clientId );
        ( (TCPTransport)retval ).setConnectTimeout( options.getConnectionTimeout() );
        break;
      case MQTT.SSL_URI:
        shortAddress = broker.substring( 6 );
        host = getHostName( shortAddress );
        port = getPort( shortAddress, 8883 );
        SSLSocketFactoryFactory factoryFactory = null;
        if ( factory == null ) {
          factoryFactory = new SSLSocketFactoryFactory();
          final Properties sslClientProps = options.getSSLProperties();
          if ( null != sslClientProps ) {
            factoryFactory.initialize( sslClientProps, null );
          }
          factory = factoryFactory.createSocketFactory( null );
        } else if ( ( factory instanceof SSLSocketFactory ) == false ) {
          throw MQTT.createMqttException( MqttException.SOCKET_FACTORY_MISMATCH );
        }

        // Create the SSL network transport...
        retval = new SSLTransport( (SSLSocketFactory)factory, host, port, clientId );
        ( (SSLTransport)retval ).setSSLhandshakeTimeout( options.getConnectionTimeout() );
        // Ciphers suites need to be set, if they are available
        if ( factoryFactory != null ) {
          final String[] enabledCiphers = factoryFactory.getEnabledCipherSuites( null );
          if ( enabledCiphers != null ) {
            ( (SSLTransport)retval ).setEnabledCiphers( enabledCiphers );
          }
        }
        break;
      case MQTT.WS_URI:
        shortAddress = broker.substring( 5 );
        host = getHostName( shortAddress );
        port = getPort( shortAddress, 80 );
        if ( factory == null ) {
          factory = SocketFactory.getDefault();
        } else if ( factory instanceof SSLSocketFactory ) {
          throw MQTT.createMqttException( MqttException.SOCKET_FACTORY_MISMATCH );
        }
        // create a websocket transport
        retval = new WebSocketTransport( factory, broker, host, port, clientId );
        ( (WebSocketTransport)retval ).setConnectTimeout( options.getConnectionTimeout() );
        break;
      case MQTT.WSS_URI:
        shortAddress = broker.substring( 6 );
        host = getHostName( shortAddress );
        port = getPort( shortAddress, 443 );
        SSLSocketFactoryFactory wSSFactoryFactory = null;
        if ( factory == null ) {
          wSSFactoryFactory = new SSLSocketFactoryFactory();
          final Properties sslClientProps = options.getSSLProperties();
          if ( null != sslClientProps ) {
            wSSFactoryFactory.initialize( sslClientProps, null );
          }
          factory = wSSFactoryFactory.createSocketFactory( null );

        } else if ( ( factory instanceof SSLSocketFactory ) == false ) {
          throw MQTT.createMqttException( MqttException.SOCKET_FACTORY_MISMATCH );
        }

        // Create the secure websocket transport...	
        retval = new WebSocketSecureTransport( (SSLSocketFactory)factory, broker, host, port, clientId );
        ( (WebSocketSecureTransport)retval ).setSSLhandshakeTimeout( options.getConnectionTimeout() );
        // Ciphers suites need to be set, if they are available
        if ( wSSFactoryFactory != null ) {
          final String[] enabledCiphers = wSSFactoryFactory.getEnabledCipherSuites( null );
          if ( enabledCiphers != null ) {
            ( (SSLTransport)retval ).setEnabledCiphers( enabledCiphers );
          }
        }
        break;
      case MQTT.LOCAL_URI:
        // create the local transport for the IBM microbroker in this JRE
        retval = new LocalTransport( broker.substring( 8 ) );
        break;
      default:
        // This shouldn't happen, as long as validateURI() has been called.
        retval = null;
    }

    Log.append( MQTT.EVENT, LogMsg.createMsg( MQTT.MSG, "client.created_network_transport", retval ) );

    return retval;
  }




  /**
   * Factory method to create an array of network transports, one for each of the 
   * supplied URIs.
   * 
   * <p>This creates at least one transport for the given broker URI and one 
   * transport for each of the broker URIs found in the connection options. 
   * This allows the connection to fail-over to other brokers if the primary 
   * becomes unavailable.</p>
   *
   * @param brokerUri the URI for the server.
   * @param options source for additional server URIs
   * 
   * @return an array of network transport appropriate to the specified address and .
   */
  protected Transport[] createTransports( final String brokerUri, final MqttConnectOptions options ) throws MqttException, MqttSecurityException {
    Log.append( MQTT.EVENT, LogMsg.createMsg( MQTT.MSG, "client.creating_network_transports", brokerUri ) );

    Transport[] retval = null;
    final String[] serverURIs = options.getServerURIs();
    String[] array = null;
    if ( serverURIs == null ) {
      array = new String[] { brokerUri };
    } else if ( serverURIs.length == 0 ) {
      array = new String[] { brokerUri };
    } else {
      array = serverURIs;
    }

    retval = new Transport[array.length];
    for ( int i = 0; i < array.length; i++ ) {
      retval[i] = createTransport( array[i], options );
    }

    Log.append( MQTT.EVENT, LogMsg.createMsg( MQTT.MSG, "client.network_transports_created", retval.length ) );
    return retval;
  }




  public void deleteBufferedMessage( final int bufferIndex ) {
    connection.deleteBufferedMessage( bufferIndex );
  }




  @Override
  public MqttToken disconnect() throws MqttException {
    return this.disconnect( null, null );
  }




  @Override
  public MqttToken disconnect( final long quiesceTimeout ) throws MqttException {
    return this.disconnect( quiesceTimeout, null, null );
  }




  @Override
  public MqttToken disconnect( final long quiesceTimeout, final Object userContext, final AsyncActionListener callback ) throws MqttException {
    Log.append( MQTT.EVENT, LogMsg.createMsg( MQTT.MSG, "client.disconnect", quiesceTimeout, userContext, callback ) );

    final MqttTokenImpl token = new MqttTokenImpl();
    token.setActionCallback( callback );
    token.setUserContext( userContext );

    final DisconnectMessage disconnect = new DisconnectMessage();
    try {
      connection.disconnect( disconnect, quiesceTimeout, token );
    } catch ( final MqttException ex ) {

      Log.append( MQTT.EVENT, LogMsg.createMsg( MQTT.MSG, "client.disconnect_call_failed", ex ) );
      throw ex;
    }

    Log.append( MQTT.EVENT, LogMsg.createMsg( MQTT.MSG, "client.disconnect_call_complete", token ) );

    return token;
  }




  @Override
  public MqttToken disconnect( final Object userContext, final AsyncActionListener callback ) throws MqttException {
    return this.disconnect( QUIESCE_TIMEOUT, userContext, callback );
  }




  @Override
  public void disconnectForcibly() throws MqttException {
    disconnectForcibly( QUIESCE_TIMEOUT, DISCONNECT_TIMEOUT );
  }




  @Override
  public void disconnectForcibly( final long disconnectTimeout ) throws MqttException {
    disconnectForcibly( QUIESCE_TIMEOUT, disconnectTimeout );
  }




  @Override
  public void disconnectForcibly( final long quiesceTimeout, final long disconnectTimeout ) throws MqttException {
    connection.disconnectForcibly( quiesceTimeout, disconnectTimeout );
  }




  public MqttMessage getBufferedMessage( final int bufferIndex ) {
    return connection.getBufferedMessage( bufferIndex );
  }




  public int getBufferedMessageCount() {
    return connection.getBufferedMessageCount();
  }




  @Override
  public String getClientId() {
    return clientId;
  }




  /**
   * Returns the currently connected Server URI
   * 
   * <p>Where getServerURI only returns the URI that was provided in the 
   * constructor, getCurrentServerURI returns the URI of the server to which 
   * the client is currently connected. This would be different in scenarios
   * where multiple server URIs have been provided to the 
   * MqttConnectOptions.</p>
   * 
   * @return the currently connected server URI
   */
  public String getCurrentServerURI() {
    return connection.getTransports()[connection.getTransportIndex()].getServerURI();
  }




  private String getHostName( final String uri ) {
    int portIndex = uri.indexOf( ':' );
    if ( portIndex == -1 ) {
      portIndex = uri.indexOf( '/' );
    }
    if ( portIndex == -1 ) {
      portIndex = uri.length();
    }
    return uri.substring( 0, portIndex );
  }




  @Override
  public MqttDeliveryToken[] getPendingDeliveryTokens() {
    return connection.getPendingDeliveryTokens();
  }




  private int getPort( final String uri, final int defaultPort ) {
    int port;
    final int portIndex = uri.lastIndexOf( ':' );
    if ( portIndex == -1 ) {
      port = defaultPort;
    } else {
      int slashIndex = uri.indexOf( '/' );
      if ( slashIndex == -1 ) {
        slashIndex = uri.length();
      }
      port = Integer.parseInt( uri.substring( portIndex + 1, slashIndex ) );
    }
    return port;
  }




  @Override
  public String getServerURI() {
    return serverURI;
  }




  /**
   * Get a topic object which can be used to publish messages.
   * 
   * @param topic the topic to use.
   * 
   * @return an MqttTopic object, onto which messages can be published.
   * 
   * @throws IllegalArgumentException if the topic contains a '+' or '#' 
   *         wildcard character.
   */
  protected Topic getTopic( final String topic ) {
    Topic.validate( topic, Topic.NO_WILDCARDS_ALLOWED );

    Topic result = (Topic)topics.get( topic );
    if ( result == null ) {
      result = new Topic( topic, connection );
      topics.put( topic, result );
    }
    return result;
  }




  @Override
  public boolean isConnected() {
    return connection.isConnected();
  }




  @Override
  public void messageArrivedComplete( final int messageId, final int qos ) throws MqttException {
    connection.messageArrivedComplete( messageId, qos );
  }




  @Override
  public MqttDeliveryToken publish( final String topic, final byte[] payload, final int qos, final boolean retained ) throws MqttException, CacheException {
    return this.publish( topic, payload, qos, retained, null, null );
  }




  @Override
  public MqttDeliveryToken publish( final String topic, final byte[] payload, final int qos, final boolean retained, final Object userContext, final AsyncActionListener callback ) throws MqttException, CacheException {
    final MqttMessage message = new MqttMessage( payload );
    message.setQos( qos );
    message.setRetained( retained );
    return this.publish( topic, message, userContext, callback );
  }




  @Override
  public MqttDeliveryToken publish( final String topic, final MqttMessage message ) throws MqttException, CacheException {
    return this.publish( topic, message, null, null );
  }




  @Override
  public MqttDeliveryToken publish( final String topic, final MqttMessage message, final Object userContext, final AsyncActionListener callback ) throws MqttException, CacheException {
    Log.append( MQTT.EVENT, LogMsg.createMsg( MQTT.MSG, "client.publish", topic, userContext, callback ) );

    //Checks if a topic is valid when publishing a message.
    Topic.validate( topic, Topic.NO_WILDCARDS_ALLOWED );

    final MqttDeliveryTokenImpl token = new MqttDeliveryTokenImpl();
    token.setActionCallback( callback );
    token.setUserContext( userContext );
    token.setMessage( message );
    token.setTopics( new String[] { topic } );

    final PublishMessage pubMsg = new PublishMessage( topic, message );
    connection.sendNoWait( pubMsg, token );

    Log.append( MQTT.EVENT, LogMsg.createMsg( MQTT.MSG, "client.publish_complete", token ) );

    return token;
  }




  @Override
  public void reconnect() throws MqttException {
    Log.append( MQTT.EVENT, LogMsg.createMsg( MQTT.MSG, "client.reconnect", clientId ) );
    // Some checks to make sure that we're not attempting to reconnect an already connected client
    if ( connection.isConnected() ) {
      throw MQTT.createMqttException( MqttException.CLIENT_CONNECTED );
    }
    if ( connection.isConnecting() ) {
      throw new MqttException( MqttException.CONNECT_IN_PROGRESS );
    }
    if ( connection.isDisconnecting() ) {
      throw new MqttException( MqttException.CLIENT_DISCONNECTING );
    }
    if ( connection.isClosed() ) {
      throw new MqttException( MqttException.CLIENT_CLOSED );
    }
    // stop any reconnection process currently in progress
    stopReconnectCycle();

    // start a new reconnection cycle
    attemptReconnect();
  }




  private void rescheduleReconnectCycle( final int delay ) {
    Log.append( MQTT.EVENT, LogMsg.createMsg( MQTT.MSG, "client.reconnect_reschedule", clientId, reconnectDelay ) );
    reconnectTimer.schedule( new ReconnectTask(), reconnectDelay );

  }




  /**
   * Sets the DisconnectedBufferOptions for this client
   * 
   * @param bufferOpts
   */
  public void setBufferOpts( final DisconnectedBufferOptions bufferOpts ) {
    connection.setDisconnectedMessageBuffer( new DisconnectedMessageBuffer( bufferOpts ) );
  }




  @Override
  public void setCallback( final ClientListener callback ) {
    listener = callback;
    connection.setCallback( callback );
  }




  @Override
  public void setManualAcks( final boolean manualAcks ) {
    connection.setManualAcks( manualAcks );
  }




  private void startReconnectCycle() {
    Log.append( MQTT.EVENT, LogMsg.createMsg( MQTT.MSG, "client.start_reconnect", clientId, reconnectDelay ) );
    reconnectTimer = new Timer( "MQTT Reconnect: " + clientId );
    reconnectTimer.schedule( new ReconnectTask(), reconnectDelay );
  }




  private void stopReconnectCycle() {
    Log.append( MQTT.EVENT, LogMsg.createMsg( MQTT.MSG, "client.stop_reconnect", clientId ) );
    reconnectTimer.cancel();
    reconnectDelay = 1000; // Reset Delay Timer

  }




  @Override
  public MqttToken subscribe( final String topicFilter, final int qos ) throws MqttException {
    return this.subscribe( new String[] { topicFilter }, new int[] { qos }, null, null );
  }




  @Override
  public MqttToken subscribe( final String topicFilter, final int qos, final MessageListener messageListener ) throws MqttException {
    return this.subscribe( new String[] { topicFilter }, new int[] { qos }, null, null, new MessageListener[] { messageListener } );
  }




  @Override
  public MqttToken subscribe( final String topicFilter, final int qos, final Object userContext, final AsyncActionListener callback ) throws MqttException {
    return this.subscribe( new String[] { topicFilter }, new int[] { qos }, userContext, callback );
  }




  @Override
  public MqttToken subscribe( final String topicFilter, final int qos, final Object userContext, final AsyncActionListener callback, final MessageListener messageListener ) throws MqttException {

    return this.subscribe( new String[] { topicFilter }, new int[] { qos }, userContext, callback, new MessageListener[] { messageListener } );
  }




  @Override
  public MqttToken subscribe( final String[] topicFilters, final int[] qos ) throws MqttException {
    return this.subscribe( topicFilters, qos, null, null );
  }




  @Override
  public MqttToken subscribe( final String[] topicFilters, final int[] qos, final MessageListener[] messageListeners ) throws MqttException {
    return this.subscribe( topicFilters, qos, null, null, messageListeners );
  }




  @Override
  public MqttToken subscribe( final String[] topicFilters, final int[] qos, final Object userContext, final AsyncActionListener callback ) throws MqttException {

    if ( topicFilters.length != qos.length ) {
      throw new IllegalArgumentException();
    }

    // remove any message handlers for individual topics 
    for ( final String topicFilter : topicFilters ) {
      connection.removeMessageListener( topicFilter );
    }

    String subs = "";
    for ( int i = 0; i < topicFilters.length; i++ ) {
      if ( i > 0 ) {
        subs += ", ";
      }
      subs += "topic=" + topicFilters[i] + " qos=" + qos[i];

      //Check if the topic filter is valid before subscribing
      Topic.validate( topicFilters[i], Topic.ALLOW_WILDCARDS );
    }
    Log.append( MQTT.EVENT, LogMsg.createMsg( MQTT.MSG, "client.subscribe", subs, userContext, callback ) );

    final MqttTokenImpl token = new MqttTokenImpl();
    token.setActionCallback( callback );
    token.setUserContext( userContext );
    token.setTopics( topicFilters );

    final SubscribeMessage register = new SubscribeMessage( topicFilters, qos );

    connection.sendNoWait( register, token );
    Log.append( MQTT.EVENT, LogMsg.createMsg( MQTT.MSG, "client.subscribe_completed", token ) );

    return token;
  }




  @Override
  public MqttToken subscribe( final String[] topicFilters, final int[] qos, final Object userContext, final AsyncActionListener callback, final MessageListener[] messageListeners ) throws MqttException {

    if ( ( messageListeners.length != qos.length ) || ( qos.length != topicFilters.length ) ) {
      throw new IllegalArgumentException();
    }

    final MqttToken token = this.subscribe( topicFilters, qos, userContext, callback );

    // add message handlers to the list for this client
    for ( int i = 0; i < topicFilters.length; ++i ) {
      connection.setMessageListener( topicFilters[i], messageListeners[i] );
    }

    return token;
  }




  @Override
  public MqttToken unsubscribe( final String topicFilter ) throws MqttException {
    return unsubscribe( new String[] { topicFilter }, null, null );
  }




  @Override
  public MqttToken unsubscribe( final String topicFilter, final Object userContext, final AsyncActionListener callback ) throws MqttException {
    return unsubscribe( new String[] { topicFilter }, userContext, callback );
  }




  @Override
  public MqttToken unsubscribe( final String[] topicFilters ) throws MqttException {
    return unsubscribe( topicFilters, null, null );
  }




  @Override
  public MqttToken unsubscribe( final String[] topicFilters, final Object userContext, final AsyncActionListener callback ) throws MqttException {
    String subs = "";
    for ( int i = 0; i < topicFilters.length; i++ ) {
      if ( i > 0 ) {
        subs += ", ";
      }
      subs += topicFilters[i];

      // Check if the topic filter is valid before unsubscribing although we 
      // already checked when subscribing, but invalid topic filter is 
      // meaningless for unsubscribing, just prohibit it to reduce unnecessary 
      // control packet sent to the broker.
      Topic.validate( topicFilters[i], Topic.ALLOW_WILDCARDS );
    }

    Log.append( MQTT.EVENT, LogMsg.createMsg( MQTT.MSG, "client.unsubscribe", subs, userContext, callback ) );

    // remove message handlers from the list for this client
    for ( final String topicFilter : topicFilters ) {
      connection.removeMessageListener( topicFilter );
    }

    final MqttTokenImpl token = new MqttTokenImpl();
    token.setActionCallback( callback );
    token.setUserContext( userContext );
    token.setTopics( topicFilters );

    final MqttUnsubscribe unregister = new MqttUnsubscribe( topicFilters );

    connection.sendNoWait( unregister, token );
    Log.append( MQTT.EVENT, LogMsg.createMsg( MQTT.MSG, "client.unsubscribe_complete", token ) );

    return token;
  }


  /**
   * @see coyote.commons.network.mqtt.MqttClient#getconnection()
   */
  @Override
  public Connection getconnection() {
    return connection;
  }

  //

  //

  //

  /**
   * 
   */
  private class ReconnectTask extends TimerTask {

    @Override
    public void run() {
      Log.append( MQTT.EVENT, LogMsg.createMsg( MQTT.MSG, "client.running_reconnect" ) );
      attemptReconnect();
    }
  }

}
