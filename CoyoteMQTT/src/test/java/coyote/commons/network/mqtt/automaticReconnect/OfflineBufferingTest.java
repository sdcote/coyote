package coyote.commons.network.mqtt.automaticReconnect;

import java.net.URI;
import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import coyote.commons.network.mqtt.DisconnectedBufferOptions;
import coyote.commons.network.mqtt.MQTT;
import coyote.commons.network.mqtt.MqttClientImpl;
import coyote.commons.network.mqtt.MqttConnectOptions;
import coyote.commons.network.mqtt.MqttDeliveryToken;
import coyote.commons.network.mqtt.MqttException;
import coyote.commons.network.mqtt.MqttMessage;
import coyote.commons.network.mqtt.MqttToken;
import coyote.commons.network.mqtt.cache.MemoryCache;
import coyote.commons.network.mqtt.properties.TestProperties;
import coyote.commons.network.mqtt.protocol.PublishMessage;
import coyote.commons.network.mqtt.utilities.ConnectionManipulationProxyServer;
import coyote.commons.network.mqtt.utilities.MqttV3Receiver;
import coyote.commons.network.mqtt.utilities.TestMemoryCache;
import coyote.commons.network.mqtt.utilities.Utility;
import coyote.loader.log.Log;


public class OfflineBufferingTest {

  private static final MemoryCache DATA_STORE = new MemoryCache();

  private static URI serverURI;
  private static String serverURIString;
  private String testTopic = "OBTOPIC";
  static ConnectionManipulationProxyServer proxy;




  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    try {
      serverURI = TestProperties.getServerURI();
      serverURIString = "tcp://" + serverURI.getHost() + ":" + serverURI.getPort();
      // Use 0 for the first time.
      proxy = new ConnectionManipulationProxyServer( serverURI.getHost(), serverURI.getPort(), 0 );
      proxy.startProxy();
      while ( !proxy.isPortSet() ) {
        Thread.sleep( 0 );
      }
      Log.info( "Proxy Started, port set to: " + proxy.getLocalPort() );
    } catch ( Exception exception ) {
      Log.error( "caught exception:", exception );
      throw exception;
    }

  }




  /**
   * Tests that A message can be buffered whilst the client is in a
   * disconnected state and is then delivered once the client has reconnected
   * automatically.
   */
  @Test
  public void testSingleMessageBufferAndDeliver() throws Exception {
    // Tokens
    MqttToken connectToken;
    MqttDeliveryToken pubToken;

    String methodName = "BooFar";
    // Client Options
    MqttConnectOptions options = new MqttConnectOptions();
    options.setCleanSession( true );
    options.setAutomaticReconnect( true );
    MqttClientImpl client = new MqttClientImpl( "tcp://localhost:" + proxy.getLocalPort(), methodName, DATA_STORE );
    DisconnectedBufferOptions disconnectedOpts = new DisconnectedBufferOptions();
    disconnectedOpts.setBufferEnabled( true );
    client.setBufferOpts( disconnectedOpts );

    // Enable Proxy & Connect to server
    proxy.enableProxy();
    connectToken = client.connect( options );
    connectToken.waitForCompletion();
    boolean isConnected = client.isConnected();
    Log.info( "First Connection isConnected: " + isConnected );
    Assert.assertTrue( isConnected );

    // Disable Proxy and cause disconnect
    proxy.disableProxy();
    isConnected = client.isConnected();
    Log.info( "Proxy Disconnect isConnected: " + isConnected );
    Assert.assertFalse( isConnected );

    // Publish Message
    pubToken = client.publish( testTopic + methodName, new MqttMessage( methodName.getBytes() ) );
    Log.info( "Publish attempted: isComplete:" + pubToken.isComplete() );
    Assert.assertFalse( pubToken.isComplete() );
    // Enable Proxy
    proxy.enableProxy();
    pubToken.waitForCompletion();

    // Check that we are connected
    // give it some time to reconnect
    long currentTime = System.currentTimeMillis();
    int timeout = 4000;
    while ( client.isConnected() == false ) {
      long now = System.currentTimeMillis();
      if ( ( currentTime + timeout ) < now ) {
        Log.warn( "Timeout Exceeded" );
        break;
      }
      Thread.sleep( 500 );
    }
    isConnected = client.isConnected();
    Log.info( "Proxy Re-Enabled isConnected: " + isConnected );
    Assert.assertTrue( isConnected );

    // Check that Message has been delivered
    Log.info( "Message Delivered: " + pubToken.isComplete() );
    Assert.assertTrue( pubToken.isComplete() );
    MqttToken disconnectToken = client.disconnect();
    disconnectToken.waitForCompletion();
    client.close();
    client = null;
    proxy.disableProxy();
  }




  /**
   * Tests that multiple messages can be buffered whilst the client is in a
   * disconnected state and that they are all then delivered once the client
   * has connected automatically.
   */
  @Test
  public void testManyMessageBufferAndDeliver() throws Exception {
    String methodName = Utility.getMethodName();

    // Tokens
    MqttToken connectToken;

    // Client Options
    MqttConnectOptions options = new MqttConnectOptions();
    options.setCleanSession( false );
    options.setAutomaticReconnect( true );
    MqttClientImpl client = new MqttClientImpl( "tcp://localhost:" + proxy.getLocalPort(), methodName, DATA_STORE );
    DisconnectedBufferOptions disconnectedOpts = new DisconnectedBufferOptions();
    disconnectedOpts.setBufferEnabled( true );
    client.setBufferOpts( disconnectedOpts );

    // Create subscription client that won't be affected by proxy
    MqttClientImpl subClient = new MqttClientImpl( serverURIString, methodName + "sub-client" );
    MqttV3Receiver mqttV3Receiver = new MqttV3Receiver( subClient, System.out );
    subClient.setCallback( mqttV3Receiver );
    MqttToken subConnectToken = subClient.connect();
    subConnectToken.waitForCompletion();
    // Subscribe to topic
    subClient.subscribe( testTopic + methodName, 0 );

    // Enable Proxy & Connect to server
    proxy.enableProxy();
    connectToken = client.connect( options );
    connectToken.waitForCompletion();
    boolean isConnected = client.isConnected();
    Log.info( "First Connection isConnected: " + isConnected );
    Assert.assertTrue( isConnected );

    // Disable Proxy and cause disconnect
    proxy.disableProxy();
    isConnected = client.isConnected();
    Log.info( "Proxy Disconnect isConnected: " + isConnected );
    Assert.assertFalse( isConnected );

    // Publish 100 messages
    for ( int x = 0; x < 100; x++ ) {
      client.publish( testTopic + methodName, new MqttMessage( Integer.toString( x ).getBytes() ) );
    }
    // Enable Proxy
    proxy.enableProxy();

    // Check that we are connected
    // give it some time to reconnect
    long currentTime = System.currentTimeMillis();
    int timeout = 8000;
    while ( client.isConnected() == false ) {

      long now = System.currentTimeMillis();
      if ( ( currentTime + timeout ) < now ) {
        Log.warn( "Timeout Exceeded" );
        break;
      }
      Thread.sleep( 500 );
    }
    // send debug information to the logs
    MQTT.logClientDebug( client );

    isConnected = client.isConnected();
    Log.info( "Proxy Re-Enabled isConnected: " + isConnected );
    Assert.assertTrue( isConnected );

    // Check that all messages have been delivered
    for ( int x = 0; x < 100; x++ ) {
      boolean recieved = mqttV3Receiver.validateReceipt( testTopic + methodName, 0, Integer.toString( x ).getBytes() );
      Assert.assertTrue( recieved );
    }
    Log.info( "All messages sent and Recieved correctly." );
    MqttToken disconnectToken = client.disconnect();
    disconnectToken.waitForCompletion();
    client.close();
    client = null;

    MqttToken subClientDisconnectToken = subClient.disconnect();
    subClientDisconnectToken.waitForCompletion();
    subClient.close();
    subClient = null;

    proxy.disableProxy();
  }




  /**
   * Tests that the buffer correctly handles messages being buffered when the
   * buffer is full and deleteOldestBufferedMessage is set to true.
   */
  @Test
  public void testDeleteOldestBufferedMessages() throws Exception {
    String methodName = Utility.getMethodName();

    // Tokens
    MqttToken connectToken;

    // Client Options
    MqttConnectOptions options = new MqttConnectOptions();
    options.setCleanSession( false );
    options.setAutomaticReconnect( true );
    MqttClientImpl client = new MqttClientImpl( "tcp://localhost:" + proxy.getLocalPort(), methodName, DATA_STORE );
    DisconnectedBufferOptions disconnectedOpts = new DisconnectedBufferOptions();
    disconnectedOpts.setBufferEnabled( true );
    // Set buffer to 100 to save time
    disconnectedOpts.setBufferSize( 100 );
    disconnectedOpts.setDeleteOldestMessages( true );
    client.setBufferOpts( disconnectedOpts );

    // Enable Proxy & Connect to server
    proxy.enableProxy();
    connectToken = client.connect( options );
    connectToken.waitForCompletion();
    boolean isConnected = client.isConnected();
    Log.info( "First Connection isConnected: " + isConnected );
    Assert.assertTrue( isConnected );

    // Disable Proxy and cause disconnect
    proxy.disableProxy();
    isConnected = client.isConnected();
    Log.info( "Proxy Disconnect isConnected: " + isConnected );
    Assert.assertFalse( isConnected );

    // Publish 100 messages
    for ( int x = 0; x < 100; x++ ) {
      client.publish( testTopic + methodName, new MqttMessage( Integer.toString( x ).getBytes() ) );
    }

    // Publish one message too many
    Log.info( "About to publish one message too many" );
    client.publish( testTopic + methodName, new MqttMessage( Integer.toString( 101 ).getBytes() ) );
    // Make sure that the message now at index 0 in the buffer is '1'
    // instead of '0'
    MqttMessage messageAt0 = client.getBufferedMessage( 0 );
    String msg = new String( messageAt0.getPayload() );
    Assert.assertEquals( "1", msg );
    client.close();
    client = null;
    proxy.disableProxy();
  }




  /**
   * Tests that A message cannot be buffered when the buffer is full and
   * deleteOldestBufferedMessage is set to false.
   */
  @Test
  public void testNoDeleteOldestBufferedMessages() throws Exception {
    String methodName = Utility.getMethodName();

    // Tokens
    MqttToken connectToken;

    // Client Options
    MqttConnectOptions options = new MqttConnectOptions();
    options.setCleanSession( false );
    options.setAutomaticReconnect( true );
    MqttClientImpl client = new MqttClientImpl( "tcp://localhost:" + proxy.getLocalPort(), methodName, DATA_STORE );
    DisconnectedBufferOptions disconnectedOpts = new DisconnectedBufferOptions();
    disconnectedOpts.setBufferEnabled( true );
    // Set buffer to 100 to save time
    disconnectedOpts.setBufferSize( 100 );
    client.setBufferOpts( disconnectedOpts );

    // Enable Proxy & Connect to server
    proxy.enableProxy();
    connectToken = client.connect( options );
    connectToken.waitForCompletion();
    boolean isConnected = client.isConnected();
    Log.info( "First Connection isConnected: " + isConnected );
    Assert.assertTrue( isConnected );

    // Disable Proxy and cause disconnect
    proxy.disableProxy();
    isConnected = client.isConnected();
    Log.info( "Proxy Disconnect isConnected: " + isConnected );
    Assert.assertFalse( isConnected );

    // Publish 100 messages
    for ( int x = 0; x < 100; x++ ) {
      client.publish( testTopic + methodName, new MqttMessage( Integer.toString( x ).getBytes() ) );
    }
    Log.info( "About to publish one message too many" );

    try {
      client.publish( testTopic + methodName, new MqttMessage( Integer.toString( 101 ).getBytes() ) );
      client.close();
      client = null;
      Assert.fail( "An MqttException Should have been thrown." );
    } catch ( MqttException ex ) {
      client.close();
      client = null;
      proxy.disableProxy();
    }
    finally {
      proxy.disableProxy();
    }

  }




  /**
   * Tests that if enabled, buffered messages are persisted to the persistence
   * layer
   */
  @Test
  public void testPersistBufferedMessages() throws Exception {
    String methodName = Utility.getMethodName();

    // Tokens
    MqttToken connectToken;
    MqttDeliveryToken pubToken;

    // Client Options
    MqttConnectOptions options = new MqttConnectOptions();
    options.setCleanSession( true );
    options.setAutomaticReconnect( true );
    final MemoryCache persistence = new MemoryCache();
    MqttClientImpl client = new MqttClientImpl( "tcp://localhost:" + proxy.getLocalPort(), methodName, persistence );
    DisconnectedBufferOptions disconnectedOpts = new DisconnectedBufferOptions();
    disconnectedOpts.setBufferEnabled( true );
    client.setBufferOpts( disconnectedOpts );

    // Enable Proxy & Connect to server
    proxy.enableProxy();
    connectToken = client.connect( options );
    connectToken.waitForCompletion();
    boolean isConnected = client.isConnected();
    Log.info( "First Connection isConnected: " + isConnected );
    Assert.assertTrue( isConnected );

    // Disable Proxy and cause disconnect
    proxy.disableProxy();
    isConnected = client.isConnected();
    Log.info( "Proxy Disconnect isConnected: " + isConnected );
    Assert.assertFalse( isConnected );

    // Make Sure persistence is empty before publish
    @SuppressWarnings("unchecked")
    List<String> keys = Collections.list( persistence.keys() );
    Assert.assertEquals( 0, keys.size() );

    // Publish Message
    pubToken = client.publish( testTopic + methodName, new MqttMessage( "test".getBytes() ) );
    Log.info( "Publish attempted: isComplete:" + pubToken.isComplete() );
    Assert.assertFalse( pubToken.isComplete() );
    // Check that message is now in persistence layer

    @SuppressWarnings("unchecked")
    List<String> keysNew = Collections.list( persistence.keys() );
    Log.info( "There are now: " + keysNew.size() + " keys in persistence" );
    Assert.assertEquals( 1, keysNew.size() );

    client.close();
    client = null;
    proxy.disableProxy();
  }




  /**
   * Tests that persisted buffered messages are published correctly when the
   * client connects for the first time and are un persisted.
   */
  @Test
  public void testUnPersistBufferedMessagesOnNewClient() throws Exception {
    String methodName = Utility.getMethodName();

    // Mock up an Mqtt Message to be stored in Persistence
    MqttMessage mqttMessage = new MqttMessage( methodName.getBytes() );
    mqttMessage.setQos( 0 );
    PublishMessage pubMessage = new PublishMessage( testTopic + methodName, mqttMessage );
    final TestMemoryCache persistence = new TestMemoryCache();
    persistence.open( null, null );
    persistence.put( "sb-0", (PublishMessage)pubMessage );
    @SuppressWarnings("unchecked")
    List<String> persistedKeys = Collections.list( persistence.keys() );
    Log.info( "There are now: " + persistedKeys.size() + " keys in persistence" );
    Assert.assertEquals( 1, persistedKeys.size() );

    // Create Subscription client to watch for the message being published
    // as soon as the main client connects
    Log.info( "Creating subscription client" );
    MqttClientImpl subClient = new MqttClientImpl( serverURIString, methodName + "sub-client" );
    MqttV3Receiver mqttV3Receiver = new MqttV3Receiver( subClient, System.out );
    subClient.setCallback( mqttV3Receiver );
    MqttToken subConnectToken = subClient.connect();
    subConnectToken.waitForCompletion();
    Assert.assertTrue( subClient.isConnected() );
    MqttToken subToken = subClient.subscribe( testTopic + methodName, 0 );
    subToken.waitForCompletion();

    // Create Real client
    Log.info( "Creating new client that uses existing persistence layer" );
    MqttConnectOptions optionsNew = new MqttConnectOptions();
    optionsNew.setCleanSession( false );
    MqttClientImpl newClient = new MqttClientImpl( serverURIString, methodName + "new-client11", persistence );

    // Connect Client with existing persistence layer
    MqttToken newClientConnectToken = newClient.connect( optionsNew );
    newClientConnectToken.waitForCompletion();
    Assert.assertTrue( newClient.isConnected() );

    // Check that message is published / delivered
    boolean recieved = mqttV3Receiver.validateReceipt( testTopic + methodName, 0, methodName.getBytes() );
    Assert.assertTrue( recieved );
    Log.info( "Message was successfully delivered after connect" );

    @SuppressWarnings("unchecked")
    List<String> postConnectKeys = Collections.list( persistence.keys() );
    Log.info( "There are now: " + postConnectKeys.size() + " keys in persistence" );
    Assert.assertEquals( 0, postConnectKeys.size() );

    MqttToken newClientDisconnectToken = newClient.disconnect();
    newClientDisconnectToken.waitForCompletion();
    newClient.close();
    newClient = null;

    MqttToken subClientDisconnectToken = subClient.disconnect();
    subClientDisconnectToken.waitForCompletion();
    subClient.close();
    subClient = null;

  }

}
