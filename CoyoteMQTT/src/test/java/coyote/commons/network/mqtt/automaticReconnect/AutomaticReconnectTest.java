package coyote.commons.network.mqtt.automaticReconnect;

import java.net.URI;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import coyote.commons.network.mqtt.MqttBlockingClientImpl;
import coyote.commons.network.mqtt.MqttConnectOptions;
import coyote.commons.network.mqtt.MqttException;
import coyote.commons.network.mqtt.cache.MemoryCache;
import coyote.commons.network.mqtt.properties.TestProperties;
import coyote.commons.network.mqtt.utilities.ConnectionManipulationProxyServer;
import coyote.loader.log.Log;


public class AutomaticReconnectTest {

  private static final MemoryCache DATA_STORE = new MemoryCache();

  private static URI serverURI;
  private String clientId = "device-client-id";
  static ConnectionManipulationProxyServer proxy;




  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    try {
      serverURI = TestProperties.getServerURI();
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




  @AfterClass
  public static void tearDownAfterClass() throws Exception {
    Log.info( "Tests finished, stopping proxy" );
    proxy.stopProxy();
  }




  /**
   * Tests that if a connection is opened and then is lost that the client automatically reconnects
   * @throws Exception
   */
  @Test
  public void testAutomaticReconnectAfterDisconnect() throws Exception {
    MqttConnectOptions options = new MqttConnectOptions();
    options.setCleanSession( true );
    options.setAutomaticReconnect( true );
    final MqttBlockingClientImpl client = new MqttBlockingClientImpl( "tcp://localhost:" + proxy.getLocalPort(), clientId, DATA_STORE );

    proxy.enableProxy();
    client.connect( options );

    boolean isConnected = client.isConnected();
    Log.info( "First Connection isConnected: " + isConnected );
    Assert.assertTrue( isConnected );

    proxy.disableProxy();
    isConnected = client.isConnected();
    Log.info( "Proxy Disconnect isConnected: " + isConnected );
    Assert.assertFalse( isConnected );

    proxy.enableProxy();
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
    client.disconnect();
    Assert.assertFalse( client.isConnected() );
  }




  /**
   * Tests that if a connection is opened and lost, that when the user calls reconnect() that the
   * client will attempt to reconnect straight away
   */
  @Test
  public void testManualReconnectAfterDisconnect() throws Exception {
    MqttConnectOptions options = new MqttConnectOptions();
    options.setCleanSession( true );
    options.setAutomaticReconnect( true );

    final MqttBlockingClientImpl client = new MqttBlockingClientImpl( "tcp://localhost:" + proxy.getLocalPort(), clientId, DATA_STORE );

    proxy.enableProxy();
    client.connect( options );

    boolean isConnected = client.isConnected();
    Log.info( "First Connection isConnected: " + isConnected );
    Assert.assertTrue( isConnected );

    proxy.disableProxy();
    isConnected = client.isConnected();
    Log.info( "Proxy Disconnect isConnected: " + isConnected );
    Assert.assertFalse( isConnected );

    proxy.enableProxy();
    client.reconnect();
    // give it some time to reconnect
    Thread.sleep( 4000 );
    isConnected = client.isConnected();
    Log.info( "Proxy Re-Enabled isConnected: " + isConnected );
    Assert.assertTrue( isConnected );
    client.disconnect();
    Assert.assertFalse( client.isConnected() );
  }




  /**
   * Tests that if the initial connection attempt fails, that the automatic reconnect code does NOT
   * engage.
   */
  @Test
  public void testNoAutomaticReconnectWithNoInitialConnect() throws Exception {
    MqttConnectOptions options = new MqttConnectOptions();
    options.setCleanSession( true );
    options.setAutomaticReconnect( true );
    options.setConnectionTimeout( 15 );
    final MqttBlockingClientImpl client = new MqttBlockingClientImpl( "tcp://localhost:" + proxy.getLocalPort(), clientId, DATA_STORE );

    // Make sure the proxy is disabled and give it a second to close everything down
    proxy.disableProxy();
    try {
      client.connect( options );
    } catch ( MqttException ex ) {
      // Exceptions are good in this case!
    }
    boolean isConnected = client.isConnected();
    Log.info( "First Connection isConnected: " + isConnected );
    Assert.assertFalse( isConnected );

    // Enable The Proxy
    proxy.enableProxy();

    // Give it some time to make sure we are still not connected
    long currentTime = System.currentTimeMillis();
    int timeout = 4000;
    while ( client.isConnected() == false ) {
      long now = System.currentTimeMillis();
      if ( ( currentTime + timeout ) < now ) {
        Assert.assertFalse( isConnected );
        break;
      }
      Thread.sleep( 500 );
    }
    isConnected = client.isConnected();
    Log.info( "Proxy Re-Enabled isConnected: " + isConnected );
    Assert.assertFalse( isConnected );

  }
}
