package coyote.commons.network.mqtt;

import java.util.Properties;

import javax.net.SocketFactory;


/**
 * Holds the set of options that control how the client connects to a server.
 */
public class MqttConnectOptions {
  /**
   * The default keep alive interval in seconds if one is not specified
   */
  public static final int KEEP_ALIVE_INTERVAL_DEFAULT = 60;
  /**
   * The default connection timeout in seconds if one is not specified
   */
  public static final int CONNECTION_TIMEOUT_DEFAULT = 30;
  /**
     * The default max inflight if one is not specified
     */
  public static final int MAX_INFLIGHT_DEFAULT = 10;
  /**
   * The default clean session setting if one is not specified
   */
  public static final boolean CLEAN_SESSION_DEFAULT = true;

  private int keepAliveInterval = KEEP_ALIVE_INTERVAL_DEFAULT;
  private int maxInflight = MAX_INFLIGHT_DEFAULT;
  private String willDestination = null;
  private MqttMessage willMessage = null;
  private String userName;
  private char[] password;
  private SocketFactory socketFactory;
  private Properties sslClientProps = null;
  private boolean cleanSession = CLEAN_SESSION_DEFAULT;
  private int connectionTimeout = CONNECTION_TIMEOUT_DEFAULT;
  private String[] serverURIs = null;
  private int MqttVersion = MQTT.VERSION_DEFAULT;

  private boolean automaticReconnect = false;




  /**
   * Constructs a new <code>MqttConnectOptions</code> object using the
   * default values.
   *
   * The defaults are:
   * <ul>
   * <li>The keepalive interval is 60 seconds</li>
   * <li>Clean Session is true</li>
   * <li>The message delivery retry interval is 15 seconds</li>
   * <li>The connection timeout period is 30 seconds</li>
   * <li>No Will message is set</li>
   * <li>A standard SocketFactory is used</li>
   * </ul>
   * More information about these values can be found in the setter methods.
   */
  public MqttConnectOptions() {}




  /**
   * Returns the connection timeout value.
   * @see #setConnectionTimeout(int)
   * @return the connection timeout value.
   */
  public int getConnectionTimeout() {
    return connectionTimeout;
  }




  public Properties getDebug() {
    final String strNull = "null";
    final Properties p = new Properties();
    p.put( "MqttVersion", new Integer( getMqttVersion() ) );
    p.put( "CleanSession", Boolean.valueOf( isCleanSession() ) );
    p.put( "ConTimeout", new Integer( getConnectionTimeout() ) );
    p.put( "KeepAliveInterval", new Integer( getKeepAliveInterval() ) );
    p.put( "UserName", ( getUserName() == null ) ? strNull : getUserName() );
    p.put( "WillDestination", ( getWillDestination() == null ) ? strNull : getWillDestination() );
    if ( getSocketFactory() == null ) {
      p.put( "SocketFactory", strNull );
    } else {
      p.put( "SocketFactory", getSocketFactory() );
    }
    if ( getSSLProperties() == null ) {
      p.put( "SSLProperties", strNull );
    } else {
      p.put( "SSLProperties", getSSLProperties() );
    }
    return p;
  }




  /**
   * Returns the "keep alive" interval.
   * @see #setKeepAliveInterval(int)
   * @return the keep alive interval.
   */
  public int getKeepAliveInterval() {
    return keepAliveInterval;
  }




  /**
     * Returns the "max inflight".
     * The max inflight limits to how many messages we can send without receiving acknowledgments. 
     * @see #setMaxInflight(int)
     * @return the max inflight
     */
  public int getMaxInflight() {
    return maxInflight;
  }




  /**
   * Returns the MQTT version.
   * @see #setMqttVersion(int)
   * @return the MQTT version.
   */
  public int getMqttVersion() {
    return MqttVersion;
  }




  /**
   * Returns the password to use for the connection.
   * @return the password to use for the connection.
   */
  public char[] getPassword() {
    return password;
  }




  /**
   * Return a list of serverURIs the client may connect to
   * @return the serverURIs or null if not set
   */
  public String[] getServerURIs() {
    return serverURIs;
  }




  /**
   * Returns the socket factory that will be used when connecting, or
   * <code>null</code> if one has not been set.
   */
  public SocketFactory getSocketFactory() {
    return socketFactory;
  }




  /**
   * Returns the SSL properties for the connection.
   * @return the properties for the SSL connection
   */
  public Properties getSSLProperties() {
    return sslClientProps;
  }




  /**
   * Returns the user name to use for the connection.
   * @return the user name to use for the connection.
   */
  public String getUserName() {
    return userName;
  }




  /**
   * Returns the topic to be used for last will and testament (LWT).
   * @return the MqttTopic to use, or <code>null</code> if LWT is not set.
   * @see #setWill(Topic, byte[], int, boolean)
   */
  public String getWillDestination() {
    return willDestination;
  }




  /**
   * Returns the message to be sent as last will and testament (LWT).
   * The returned object is "read only".  Calling any "setter" methods on
   * the returned object will result in an
   * <code>IllegalStateException</code> being thrown.
   * @return the message to use, or <code>null</code> if LWT is not set.
   */
  public MqttMessage getWillMessage() {
    return willMessage;
  }




  /**
   * Returns whether the client will automatically attempt to reconnect to the
   * server if the connection is lost
   * @return the automatic reconnection flag.
   */
  public boolean isAutomaticReconnect() {
    return automaticReconnect;
  }




  /**
   * Returns whether the client and server should remember state for the client across reconnects.
   * @return the clean session flag
   */
  public boolean isCleanSession() {
    return cleanSession;
  }




  /**
   * Sets whether the client will automatically attempt to reconnect to the
   * server if the connection is lost.
   * <ul>
   * <li>If set to false, the client will not attempt to automatically
   *  reconnect to the server in the event that the connection is lost.</li>
   *  <li>If set to true, in the event that the connection is lost, the client
   *  will attempt to reconnect to the server. It will initially wait 1 second before
   *  it attempts to reconnect, for every failed reconnect attempt, the delay will double
   *  until it is at 2 minutes at which point the delay will stay at 2 minutes.</li>
   * </ul>
   * @param automaticReconnect
   */
  public void setAutomaticReconnect( final boolean automaticReconnect ) {
    this.automaticReconnect = automaticReconnect;
  }




  /**
   * Sets whether the client and server should remember state across restarts 
   * and reconnects.
   * <ul>
   * <li>If set to false both the client and server will maintain state across
   * restarts of the client, the server and the connection. As state is 
   * maintained:
   * <ul>
   * <li>Message delivery will be reliable meeting the specified QOS even if 
   * the client, server or connection are restarted.
   * <li> The server will treat a subscription as durable.
   * </ul>
   * <li>If set to true the client and server will not maintain state across
   * restarts of the client, the server or the connection. This means:
   * <ul>
   * <li>Message delivery to the specified QOS cannot be maintained if the
   * client, server or connection are restarted
   * <li>The server will treat a subscription as non-durable
   * </ul>
   * </ul>
   */
  public void setCleanSession( final boolean cleanSession ) {
    this.cleanSession = cleanSession;
  }




  /**
   * Sets the connection timeout value.
   * 
   * <p>This value, measured in seconds, defines the maximum time interval the 
   * client will wait for the network connection to the MQTT server to be 
   * established. The default timeout is 30 seconds. A value of 0 disables 
   * timeout processing meaning the client will wait until the network 
   * connection is made successfully or fails.</p>
   * 
   * @param connectionTimeout the timeout value, measured in seconds. It must 
   *        be &gt; 0;
   */
  public void setConnectionTimeout( final int connectionTimeout ) {
    if ( connectionTimeout < 0 ) {
      throw new IllegalArgumentException();
    }
    this.connectionTimeout = connectionTimeout;
  }




  /**
   * Sets the "keep alive" interval.
   * 
   * <p>This value, measured in seconds, defines the maximum time interval 
   * between messages sent or received. It enables the client to detect if the 
   * server is no longer available, without having to wait for the TCP/IP 
   * timeout. The client will ensure that at least one message travels across 
   * the network within each keep alive period. In the absence of a data-
   * related message during the time period, the client sends a very small 
   * "ping" message, which the server will acknowledge. A value of 0 disables 
   * keepalive processing in the client.</p>
   * 
   * <p>The default value is 60 seconds</p>
   *
   * @param keepAliveInterval the interval, measured in seconds, must be &gt;= 0.
   */
  public void setKeepAliveInterval( final int keepAliveInterval ) throws IllegalArgumentException {
    if ( keepAliveInterval < 0 ) {
      throw new IllegalArgumentException();
    }
    this.keepAliveInterval = keepAliveInterval;
  }




  /**
   * Sets the "max inflight". 
   * please increase this value in a high traffic environment.
   * <p>The default value is 10</p>
   * @param maxInflight
   */
  public void setMaxInflight( final int maxInflight ) {
    if ( maxInflight < 0 ) {
      throw new IllegalArgumentException();
    }
    this.maxInflight = maxInflight;
  }




  /**
   * Sets the MQTT version.
   * The default action is to connect with version 3.1.1, 
   * and to fall back to 3.1 if that fails.
   * Version 3.1.1 or 3.1 can be selected specifically, with no fall back,
   * by using the MQTT_VERSION_3_1_1 or MQTT_VERSION_3_1 options respectively.
   *
   * @param MqttVersion the version of the MQTT protocol.
   */
  public void setMqttVersion( final int MqttVersion ) throws IllegalArgumentException {
    if ( ( MqttVersion != MQTT.VERSION_DEFAULT ) && ( MqttVersion != MQTT.VERSION_3_1 ) && ( MqttVersion != MQTT.VERSION_3_1_1 ) ) {
      throw new IllegalArgumentException();
    }
    this.MqttVersion = MqttVersion;
  }




  /**
   * Sets the password to use for the connection.
   */
  public void setPassword( final char[] password ) {
    this.password = password;
  }




  /**
   * Set a list of one or more serverURIs the client may connect to.
   * <p>
   * Each <code>serverURI</code> specifies the address of a server that the client may
   * connect to. Two types of
   * connection are supported <code>tcp://</code> for a TCP connection and
   * <code>ssl://</code> for a TCP connection secured by SSL/TLS.
   * For example:
   * <ul>
   * 	<li><code>tcp://localhost:1883</code></li>
   * 	<li><code>ssl://localhost:8883</code></li>
   * </ul>
   * If the port is not specified, it will
   * default to 1883 for <code>tcp://</code>" URIs, and 8883 for <code>ssl://</code> URIs.
   * <p>
   * If serverURIs is set then it overrides the serverURI parameter passed in on the
   * constructor of the MQTT client.
   * <p>
   * When an attempt to connect is initiated the client will start with the first
   * serverURI in the list and work through
   * the list until a connection is established with a server. If a connection cannot be made to
   * any of the servers then the connect attempt fails.
   * <p>
   * Specifying a list of servers that a client may connect to has several uses:
   * <ol>
   * <li>High Availability and reliable message delivery
   * <p>Some MQTT servers support a high availability feature where two or more
   * "equal" MQTT servers share state. An MQTT client can connect to any of the "equal"
   * servers and be assured that messages are reliably delivered and durable subscriptions
   * are maintained no matter which server the client connects to.
   * <p>The cleansession flag must be set to false if durable subscriptions and/or reliable
   * message delivery is required.
   * <li>Hunt List
   * <p>A set of servers may be specified that are not "equal" (as in the high availability
   * option). As no state is shared across the servers reliable message delivery and
   * durable subscriptions are not valid. The cleansession flag must be set to true if the
   * hunt list mode is used
   * </ol>
   * 
   * @param array of serverURIs
   */
  public void setServerURIs( final String[] array ) {
    for ( final String element : array ) {
      MQTT.validateURI( element );
    }
    serverURIs = array;
  }




  /**
   * Sets the <code>SocketFactory</code> to use.  This allows an application
   * to apply its own policies around the creation of network sockets.  If
   * using an SSL connection, an <code>SSLSocketFactory</code> can be used
   * to supply application-specific security settings.
   * @param socketFactory the factory to use.
   */
  public void setSocketFactory( final SocketFactory socketFactory ) {
    this.socketFactory = socketFactory;
  }




  /**
   * Sets the SSL properties for the connection.  
   * 
   * <p>Note that these properties are only valid if an implementation of the 
   * Java Secure Socket Extensions (JSSE) is available. These properties are
   * <em>not</em> used if a SocketFactory has been set using
   * {@link #setSocketFactory(SocketFactory)}.
   * The following properties can be used:<dl>
   * <dt>com.ibm.ssl.protocol</dt>
   * <dd>One of: SSL, SSLv3, TLS, TLSv1, SSL_TLS.</dd>
   * <dt>com.ibm.ssl.contextProvider
   * <dd>Underlying JSSE provider.  For example "IBMJSSE2" or "SunJSSE"</dd>
   *
   * <dt>com.ibm.ssl.keyStore</dt>
   * <dd>The name of the file that contains the KeyStore object that you
   * want the KeyManager to use. For example /mydir/etc/key.p12</dd>
   *
   * <dt>com.ibm.ssl.keyStorePassword</dt>
   * <dd>The password for the KeyStore object that you want the KeyManager to 
   *   use. The password can either be in plain-text,
   *   or may be obfuscated using the static method: 
   *   <code>com.ibm.micro.security.Password.obfuscate(char[] password)</code>.
   *   This obfuscates the password using a simple and insecure XOR and Base64
   *   encoding mechanism. Note that this is only a simple scrambler to 
   *   obfuscate clear-text passwords.</dd>
   * <dt>com.ibm.ssl.keyStoreType</dt>
   * <dd>Type of key store, for example "PKCS12", "JKS", or "JCEKS".</dd>
   * <dt>com.ibm.ssl.keyStoreProvider</dt>
   * <dd>Key store provider, for example "IBMJCE" or "IBMJCEFIPS".</dd>
   * <dt>com.ibm.ssl.trustStore</dt> 
   * <dd>The name of the file that contains the KeyStore object that you want 
   *   the TrustManager to use.</dd>
   * <dt>com.ibm.ssl.trustStorePassword</dt>
   * <dd>The password for the TrustStore object that you want the TrustManager 
   *   to use. The password can either be in plain-text, or may be obfuscated 
   *   using the static method:
   *   <code>com.ibm.micro.security.Password.obfuscate(char[] password)</code>.
   *   This obfuscates the password using a simple and insecure XOR and Base64 
   *   encoding mechanism. Note that this is only a simple scrambler to 
   *   obfuscate clear-text passwords.</dd>
   * <dt>com.ibm.ssl.trustStoreType</dt>
   * <dd>The type of KeyStore object that you want the default TrustManager to 
   * use. Same possible values as "keyStoreType".</dd>
   * <dt>com.ibm.ssl.trustStoreProvider</dt>
   * <dd>Trust store provider, for example "IBMJCE" or "IBMJCEFIPS".</dd>
   * <dt>com.ibm.ssl.enabledCipherSuites</dt>
   * <dd>A list of which ciphers are enabled. Values are dependent on the 
   * provider, for example: SSL_RSA_WITH_AES_128_CBC_SHA;
   * SSL_RSA_WITH_3DES_EDE_CBC_SHA.</dd>
   * <dt>com.ibm.ssl.keyManager</dt>
   * <dd>Sets the algorithm that will be used to instantiate a 
   * KeyManagerFactory object instead of using the default algorithm available 
   * in the platform. Example values:"IbmX509" or "IBMJ9X509".</dd>
   * <dt>com.ibm.ssl.trustManager</dt>
   * <dd>Sets the algorithm that will be used to instantiate a 
   *   TrustManagerFactory object instead of using the default algorithm 
   *   available in the platform. Example values:"PKIX" or "IBMJ9X509".</dd>
   * </dl>
   */
  public void setSSLProperties( final Properties props ) {
    sslClientProps = props;
  }




  /**
   * Sets the user name to use for the connection.
   * @throws IllegalArgumentException if the user name is blank or only
   * contains whitespace characters.
   */
  public void setUserName( final String userName ) {
    if ( ( userName != null ) && ( userName.trim().equals( "" ) ) ) {
      throw new IllegalArgumentException();
    }
    this.userName = userName;
  }




  /**
   * Sets the "Last Will and Testament" (LWT) for the connection.
   * In the event that this client unexpectedly loses its connection to the
   * server, the server will publish a message to itself using the supplied
   * details.
   *
   * @param topic the topic to publish to.
   * @param payload the byte payload for the message.
   * @param qos the quality of service to publish the message at (0, 1 or 2).
   * @param retained whether or not the message should be retained.
   */
  public void setWill( final Topic topic, final byte[] payload, final int qos, final boolean retained ) {
    final String topicS = topic.getName();
    validateWill( topicS, payload );
    this.setWill( topicS, new MqttMessage( payload ), qos, retained );
  }




  /**
   * Sets the "Last Will and Testament" (LWT) for the connection.
   * In the event that this client unexpectedly loses its connection to the
   * server, the server will publish a message to itself using the supplied
   * details.
   *
   * @param topic the topic to publish to.
   * @param payload the byte payload for the message.
   * @param qos the quality of service to publish the message at (0, 1 or 2).
   * @param retained whether or not the message should be retained.
   */
  public void setWill( final String topic, final byte[] payload, final int qos, final boolean retained ) {
    validateWill( topic, payload );
    this.setWill( topic, new MqttMessage( payload ), qos, retained );
  }




  /**
   * Sets up the will information, based on the supplied parameters.
   */
  protected void setWill( final String topic, final MqttMessage msg, final int qos, final boolean retained ) {
    willDestination = topic;
    willMessage = msg;
    willMessage.setQos( qos );
    willMessage.setRetained( retained );
    // Prevent any more changes to the will message
    willMessage.setMutable( false );
  }




  @Override
  public String toString() {
    return MQTT.logProperties( getDebug(), "Connection options" );
  }




  /**
   * Validates the will fields.
   */
  private void validateWill( final String dest, final Object payload ) {
    if ( ( dest == null ) || ( payload == null ) ) {
      throw new IllegalArgumentException();
    }

    Topic.validate( dest, Topic.NO_WILDCARDS_ALLOWED );
  }
}
