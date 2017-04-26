package coyote.commons.network.mqtt;

import java.net.URI;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import coyote.commons.network.mqtt.MqttClient;
import coyote.commons.network.mqtt.MqttConnectOptions;
import coyote.commons.network.mqtt.MqttDeliveryToken;
import coyote.commons.network.mqtt.MqttException;
import coyote.commons.network.mqtt.MqttToken;
import coyote.commons.network.mqtt.client.TestClientFactory;
import coyote.commons.network.mqtt.properties.TestProperties;
import coyote.commons.network.mqtt.utilities.MqttV3Receiver;
import coyote.commons.network.mqtt.utilities.Utility;
import coyote.loader.log.Log;


/**
 *
 */
public class SendReceiveAsyncTest {

  private static URI serverURI;
  private static TestClientFactory clientFactory;




  /**
   * @throws Exception
   */
  @BeforeClass
  public static void setUpBeforeClass() throws Exception {

    try {
      String methodName = Utility.getMethodName();

      serverURI = TestProperties.getServerURI();
      clientFactory = new TestClientFactory();
      clientFactory.open();
    } catch ( Exception exception ) {
      Log.error( "caught exception:", exception );
      throw exception;
    }
  }




  /**
   * @throws Exception
   */
  @AfterClass
  public static void tearDownAfterClass() throws Exception {
    String methodName = Utility.getMethodName();

    try {
      if ( clientFactory != null ) {
        clientFactory.close();
        clientFactory.disconnect();
      }
    } catch ( Exception exception ) {
      Log.error( "caught exception:", exception );
    }
  }




  /**
   * Tests that a client can be constructed and that it can connect to and
   * disconnect from the service
   * 
   * @throws Exception
   */
  @Test
  public void testConnect() throws Exception {

    MqttClient mqttClient = null;
    try {
      mqttClient = clientFactory.createMqttAsyncClient( serverURI, "testConnect" );
      MqttToken connectToken = null;
      MqttToken disconnectToken = null;

      connectToken = mqttClient.connect( null, null );
      Log.info( "Connecting...(serverURI:" + serverURI + ", ClientId:testConnect" );
      connectToken.waitForCompletion();

      disconnectToken = mqttClient.disconnect( null, null );
      Log.info( "Disconnecting..." );
      disconnectToken.waitForCompletion();

      connectToken = mqttClient.connect( null, null );
      Log.info( "Connecting...(serverURI:" + serverURI + ", ClientId:testConnect" );
      connectToken.waitForCompletion();

      disconnectToken = mqttClient.disconnect( null, null );
      Log.info( "Disconnecting..." );
      disconnectToken.waitForCompletion();
    } catch ( Exception exception ) {
      Log.error( "caught exception:", exception );
      Assert.fail( "Failed:testConnect exception=" + exception );
    }
    finally {
      if ( mqttClient != null ) {
        Log.info( "Close..." );
        mqttClient.close();
      }
    }

  }




  /**
   * Test connection using a remote host name for the local host.
   * 
   * @throws Exception
   */
  @Test
  public void testRemoteConnect() throws Exception {

    MqttClient mqttClient = null;
    try {
      mqttClient = clientFactory.createMqttAsyncClient( serverURI, "testRemoteConnect" );
      MqttToken connectToken = null;
      MqttToken subToken = null;
      MqttDeliveryToken pubToken = null;
      MqttToken disconnectToken = null;

      connectToken = mqttClient.connect( null, null );
      Log.info( "Connecting...(serverURI:" + serverURI + ", ClientId:testRemoteConnect" );
      connectToken.waitForCompletion();

      disconnectToken = mqttClient.disconnect( null, null );
      Log.info( "Disconnecting..." );
      disconnectToken.waitForCompletion();

      MqttV3Receiver mqttV3Receiver = new MqttV3Receiver( mqttClient, System.out );
      Log.info( "Assigning callback..." );
      mqttClient.setCallback( mqttV3Receiver );

      MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
      mqttConnectOptions.setCleanSession( false );

      connectToken = mqttClient.connect( mqttConnectOptions, null, null );
      Log.info( "Connecting...(serverURI:" + serverURI + ", ClientId:testRemoteConnect, cleanSession: false" );
      connectToken.waitForCompletion();

      String[] topicNames = new String[] { "testRemoteConnect/Topic" };
      int[] topicQos = { 0 };
      subToken = mqttClient.subscribe( topicNames, topicQos, null, null );
      Log.info( "Subscribing to..." + topicNames[0] );
      subToken.waitForCompletion();

      byte[] payload = ( "Message payload testRemoteConnect" ).getBytes();
      pubToken = mqttClient.publish( topicNames[0], payload, 1, false, null, null );
      Log.info( "Publishing to..." + topicNames[0] );
      pubToken.waitForCompletion();

      boolean ok = mqttV3Receiver.validateReceipt( topicNames[0], 0, payload );
      if ( !ok ) {
        Assert.fail( "Receive failed" );
      }

      disconnectToken = mqttClient.disconnect( null, null );
      Log.info( "Disconnecting..." );
      disconnectToken.waitForCompletion();

    } catch ( Exception exception ) {
      Log.error( "caught exception:", exception );
      Assert.fail( "Failed:testRemoteConnect exception=" + exception );
    }
    finally {
      if ( mqttClient != null ) {
        Log.info( "Close..." );
        mqttClient.close();
      }
    }

  }




  /**
   * Test client pubSub using very large messages
   */
  @Test
  public void testLargeMessage() {

    MqttClient mqttClient = null;
    try {
      mqttClient = clientFactory.createMqttAsyncClient( serverURI, "testLargeMessage" );
      MqttToken connectToken;
      MqttToken subToken;
      MqttToken unsubToken;
      MqttDeliveryToken pubToken;

      MqttV3Receiver mqttV3Receiver = new MqttV3Receiver( mqttClient, System.out );
      Log.info( "Assigning callback..." );
      mqttClient.setCallback( mqttV3Receiver );

      connectToken = mqttClient.connect( null, null );
      Log.info( "Connecting...(serverURI:" + serverURI + ", ClientId:testLargeMessage" );
      connectToken.waitForCompletion();

      int largeSize = 1000;
      String[] topicNames = new String[] { "testLargeMessage/Topic" };
      int[] topicQos = { 0 };
      byte[] message = new byte[largeSize];

      java.util.Arrays.fill( message, (byte)'s' );

      subToken = mqttClient.subscribe( topicNames, topicQos, null, null );
      Log.info( "Subscribing to..." + topicNames[0] );
      subToken.waitForCompletion();

      unsubToken = mqttClient.unsubscribe( topicNames, null, null );
      Log.info( "Unsubscribing from..." + topicNames[0] );
      unsubToken.waitForCompletion();

      subToken = mqttClient.subscribe( topicNames, topicQos, null, null );
      Log.info( "Subscribing to..." + topicNames[0] );
      subToken.waitForCompletion();

      pubToken = mqttClient.publish( topicNames[0], message, 0, false, null, null );
      Log.info( "Publishing to..." + topicNames[0] );
      pubToken.waitForCompletion();

      boolean ok = mqttV3Receiver.validateReceipt( topicNames[0], 0, message );
      if ( !ok ) {
        Assert.fail( "Receive failed" );
      }
    } catch ( Exception exception ) {
      Log.error( "caught exception:", exception );
      Assert.fail( "Failed to instantiate:testLargeMessage exception=" + exception );
    }
    finally {
      try {
        if ( mqttClient != null ) {
          MqttToken disconnectToken;
          disconnectToken = mqttClient.disconnect( null, null );
          Log.info( "Disconnecting..." );
          disconnectToken.waitForCompletion();
          Log.info( "Close..." );
          mqttClient.close();
        }
      } catch ( Exception exception ) {
        Log.error( "caught exception:", exception );
      }
    }

  }




  /**
   * Multiple publishers and subscribers.
   */
  @Test
  public void testMultipleClients() {

    int publishers = 2;
    int subscribers = 10;

    MqttClient[] mqttPublisher = new MqttClient[publishers];
    MqttClient[] mqttSubscriber = new MqttClient[subscribers];

    MqttToken connectToken;
    MqttToken subToken;
    MqttDeliveryToken pubToken;
    MqttToken disconnectToken;

    try {
      String[] topicNames = new String[] { "testMultipleClients/Topic" };
      int[] topicQos = { 0 };

      for ( int i = 0; i < mqttPublisher.length; i++ ) {
        mqttPublisher[i] = clientFactory.createMqttAsyncClient( serverURI, "MultiPub" + i );
        connectToken = mqttPublisher[i].connect( null, null );
        Log.info( "Connecting...(serverURI:" + serverURI + ", ClientId: MultiPub" + i );
        connectToken.waitForCompletion();
      } // for...

      MqttV3Receiver[] mqttV3Receiver = new MqttV3Receiver[mqttSubscriber.length];
      for ( int i = 0; i < mqttSubscriber.length; i++ ) {
        mqttSubscriber[i] = clientFactory.createMqttAsyncClient( serverURI, "MultiSubscriber" + i );
        mqttV3Receiver[i] = new MqttV3Receiver( mqttSubscriber[i], System.out );
        Log.info( "Assigning callback..." );
        mqttSubscriber[i].setCallback( mqttV3Receiver[i] );
        connectToken = mqttSubscriber[i].connect( null, null );
        Log.info( "Connecting...(serverURI:" + serverURI + ", ClientId: MultiSubscriber" + i );
        connectToken.waitForCompletion();
        subToken = mqttSubscriber[i].subscribe( topicNames, topicQos, null, null );
        Log.info( "Subcribing to..." + topicNames[0] );
        subToken.waitForCompletion();
      } // for...

      for ( int iMessage = 0; iMessage < 10; iMessage++ ) {
        byte[] payload = ( "Message " + iMessage ).getBytes();
        for ( int i = 0; i < mqttPublisher.length; i++ ) {
          pubToken = mqttPublisher[i].publish( topicNames[0], payload, 0, false, null, null );
          Log.info( "Publishing to..." + topicNames[0] );
          pubToken.waitForCompletion();
        }

        for ( int i = 0; i < mqttSubscriber.length; i++ ) {
          for ( int ii = 0; ii < mqttPublisher.length; ii++ ) {
            boolean ok = mqttV3Receiver[i].validateReceipt( topicNames[0], 0, payload );
            if ( !ok ) {
              Assert.fail( "Receive failed" );
            }
          } // for publishers...
        } // for subscribers...
      } // for messages...

    } catch ( Exception exception ) {
      Log.error( "caught exception:", exception );
      Assert.fail( "Failed to instantiate:testMultipleClients exception=" + exception );
    }
    finally {
      try {
        for ( int i = 0; i < mqttPublisher.length; i++ ) {
          disconnectToken = mqttPublisher[i].disconnect( null, null );
          Log.info( "Disconnecting...MultiPub" + i );
          disconnectToken.waitForCompletion();
          Log.info( "Close..." );
          mqttPublisher[i].close();
        }
        for ( int i = 0; i < mqttSubscriber.length; i++ ) {
          disconnectToken = mqttSubscriber[i].disconnect( null, null );
          Log.info( "Disconnecting...MultiSubscriber" + i );
          disconnectToken.waitForCompletion();
          Log.info( "Close..." );
          mqttSubscriber[i].close();
        }
      } catch ( Exception exception ) {
        Log.error( "caught exception:", exception );
      }
    }

  }




  /**
   * Test the behavior of the cleanStart flag, used to clean up before
   * re-connecting.
   */
  @Test
  public void testCleanStart() throws Exception {

    MqttClient mqttClient = null;

    MqttToken connectToken;
    MqttToken subToken;
    MqttDeliveryToken pubToken;
    MqttToken disconnectToken;

    try {
      mqttClient = clientFactory.createMqttAsyncClient( serverURI, "testCleanStart" );
      MqttV3Receiver mqttV3Receiver = new MqttV3Receiver( mqttClient, System.out );
      Log.info( "Assigning callback..." );
      mqttClient.setCallback( mqttV3Receiver );

      MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
      // Clean start: true - The broker cleans up all client state, including subscriptions, when the client is disconnected.
      // Clean start: false - The broker remembers all client state, including subscriptions, when the client is disconnected.
      //                      Matching publications will get queued in the broker whilst the client is disconnected.
      // For Mqtt V3 cleanSession=false, implies new subscriptions are durable.
      mqttConnectOptions.setCleanSession( false );
      connectToken = mqttClient.connect( mqttConnectOptions, null, null );
      Log.info( "Connecting...(serverURI:" + serverURI + ", ClientId:testCleanStart, cleanSession: false" );
      connectToken.waitForCompletion();

      String[] topicNames = new String[] { "testCleanStart/Topic" };
      int[] topicQos = { 0 };
      subToken = mqttClient.subscribe( topicNames, topicQos, null, null );
      Log.info( "Subscribing to..." + topicNames[0] );
      subToken.waitForCompletion();

      byte[] payload = ( "Message payload testCleanStart First" ).getBytes();
      pubToken = mqttClient.publish( topicNames[0], payload, 1, false, null, null );
      Log.info( "Publishing to..." + topicNames[0] );
      pubToken.waitForCompletion();
      boolean ok = mqttV3Receiver.validateReceipt( topicNames[0], 0, payload );
      if ( !ok ) {
        Assert.fail( "Receive failed" );
      }

      // Disconnect and reconnect to make sure the subscription and all queued messages are cleared.
      disconnectToken = mqttClient.disconnect( null, null );
      Log.info( "Disconnecting..." );
      disconnectToken.waitForCompletion();
      Log.info( "Close" );
      mqttClient.close();

      // Send a message from another client, to our durable subscription.
      mqttClient = clientFactory.createMqttAsyncClient( serverURI, "testCleanStartOther" );
      mqttV3Receiver = new MqttV3Receiver( mqttClient, System.out );
      Log.info( "Assigning callback..." );
      mqttClient.setCallback( mqttV3Receiver );

      mqttConnectOptions = new MqttConnectOptions();
      mqttConnectOptions.setCleanSession( true );
      connectToken = mqttClient.connect( mqttConnectOptions, null, null );
      Log.info( "Connecting...(serverURI:" + serverURI + ", ClientId:testCleanStartOther, cleanSession: true" );
      connectToken.waitForCompletion();

      // Receive the publication so that we can be sure the first client has also received it.
      // Otherwise the first client may reconnect with its clean session before the message has arrived.
      subToken = mqttClient.subscribe( topicNames, topicQos, null, null );
      Log.info( "Subscribing to..." + topicNames[0] );
      subToken.waitForCompletion();
      payload = ( "Message payload testCleanStart Other client" ).getBytes();
      pubToken = mqttClient.publish( topicNames[0], payload, 1, false, null, null );
      Log.info( "Publishing to..." + topicNames[0] );
      pubToken.waitForCompletion();
      ok = mqttV3Receiver.validateReceipt( topicNames[0], 0, payload );
      if ( !ok ) {
        Assert.fail( "Receive failed" );
      }
      disconnectToken = mqttClient.disconnect( null, null );
      Log.info( "Disconnecting..." );
      disconnectToken.waitForCompletion();
      Log.info( "Close..." );
      mqttClient.close();

      // Reconnect and check we have no messages.
      mqttClient = clientFactory.createMqttAsyncClient( serverURI, "testCleanStart" );
      mqttV3Receiver = new MqttV3Receiver( mqttClient, System.out );
      Log.info( "Assigning callback..." );
      mqttClient.setCallback( mqttV3Receiver );
      mqttConnectOptions = new MqttConnectOptions();
      mqttConnectOptions.setCleanSession( true );
      connectToken = mqttClient.connect( mqttConnectOptions, null, null );
      Log.info( "Connecting...(serverURI:" + serverURI + ", ClientId:testCleanStart, cleanSession: true" );
      connectToken.waitForCompletion();
      MqttV3Receiver.ReceivedMessage receivedMessage = mqttV3Receiver.receiveNext( 100 );
      if ( receivedMessage != null ) {
        Assert.fail( "Receive messaqe:" + new String( receivedMessage.message.getPayload() ) );
      }

      // Also check that subscription is cancelled.
      payload = ( "Message payload testCleanStart Cancelled Subscription" ).getBytes();
      pubToken = mqttClient.publish( topicNames[0], payload, 1, false, null, null );
      Log.info( "Publishing to..." + topicNames[0] );
      pubToken.waitForCompletion();

      receivedMessage = mqttV3Receiver.receiveNext( 100 );
      if ( receivedMessage != null ) {
        Log.debug( "Message I shouldn't have: " + new String( receivedMessage.message.getPayload() ) );
        Assert.fail( "Receive messaqe:" + new String( receivedMessage.message.getPayload() ) );
      }
    } catch ( Exception exception ) {
      Log.error( "caught exception:", exception );
      Assert.fail( "Failed:testCleanStart exception=" + exception );
    }
    finally {
      try {
        if ( mqttClient != null ) {
          disconnectToken = mqttClient.disconnect( null, null );
          Log.info( "Disconnecting..." );
          disconnectToken.waitForCompletion();
          Log.info( "Close..." );
          mqttClient.close();
        }
      } catch ( Exception exception ) {
        Log.error( "caught exception:", exception );
      }
    }

  }




  /**
   * Short keep-alive intervals along with very large payloads (some MBis) results in the client being disconnected by
   * the broker.
   * 
   * In order to recreate the issue increase the value of waitMilliseconds in
   *  coyote.mqtt.test.utilities.MqttV3Receiver.validateReceipt to some large value (e.g.
   * 60*60*1000). This allows the test to wait for a longer time.
   * 
   * The issue occurs because while receiving such a large payload no PING is sent by the client to the broker. This
   * can be seen adding some debug statements in:
   *  coyote.mqtt.internal.ClientState.checkForActivity.
   * 
   * Since no other activity (messages from the client to the broker) is generated, the broker disconnects the client.
   */
  @Test
  public void testVeryLargeMessageWithShortKeepAlive() {

    MqttClient mqttClient = null;
    try {
      mqttClient = clientFactory.createMqttAsyncClient( serverURI, "testVeryLargeMessageWithShortKeepAlive" );
      MqttV3Receiver mqttV3Receiver = new MqttV3Receiver( mqttClient, System.out );
      Log.info( "Assigning callback..." );
      mqttClient.setCallback( mqttV3Receiver );

      //keepAlive=30s
      MqttConnectOptions options = new MqttConnectOptions();
      options.setKeepAliveInterval( 30 );

      MqttToken connectToken = mqttClient.connect( options );
      Log.info( "Connecting...(serverURI:" + serverURI + ", ClientId:testMultipleClients" );
      connectToken.waitForCompletion();

      String topic = "testLargeMsg/Topic";
      //10MB
      int largeSize = 20 * ( 1 << 20 );
      byte[] message = new byte[largeSize];

      java.util.Arrays.fill( message, (byte)'s' );

      MqttToken subToken = mqttClient.subscribe( topic, 0 );
      Log.info( "Subscribing to..." + topic );
      subToken.waitForCompletion();

      MqttToken pubToken = mqttClient.publish( topic, message, 0, false, null, null );
      Log.info( "Publishing to..." + topic );
      pubToken.waitForCompletion();
      Log.info( "Published" );

      boolean ok = mqttV3Receiver.validateReceipt( topic, 0, message );
      if ( !ok ) {
        Assert.fail( "Receive failed" );
      }
    } catch ( Exception exception ) {
      Log.error( "caught exception:", exception );
      Assert.fail( "Failed to instantiate:testVeryLargeMessageWithShortKeepAlive exception=" + exception );
    }
    finally {
      try {
        if ( mqttClient != null ) {
          MqttToken disconnectToken = mqttClient.disconnect( null, null );
          Log.info( "Disconnecting..." );
          disconnectToken.waitForCompletion();
          mqttClient.close();
          Log.info( "Closed" );
        }
      } catch ( Exception exception ) {
        Log.error( "caught exception:", exception );
      }
    }

  }




  /**
   * Test the behavior of the connection timeout when connecting to a non MQTT server.
   * i.e. ssh port 22
   */
  @Test
  public void testConnectTimeout() throws Exception {

    MqttClient mqttClient = null;
    // Change the URI to a none MQTT server
    URI uri = new URI( "tcp://iot.eclipse.org:22" );
    MqttToken connectToken = null;
    try {
      mqttClient = clientFactory.createMqttAsyncClient( uri, "testConnectTimeout" );
      Log.info( "Connecting...(serverURI:" + uri + ", ClientId:testConnectTimeout" );
      connectToken = mqttClient.connect( new MqttConnectOptions() );
      connectToken.waitForCompletion( 5000 );
      Assert.fail( "Should throw an timeout exception." );
    } catch ( Exception exception ) {
      Log.info( "Connect action failed at expected." );
      Assert.assertTrue( exception instanceof MqttException );
      Assert.assertEquals( MqttException.CLIENT_TIMEOUT, ( (MqttException)exception ).getReasonCode() );
    }
    finally {
      if ( mqttClient != null ) {
        Log.info( "Close..." + mqttClient );
        mqttClient.disconnectForcibly( 5000, 5000 );
      }
    }

    //reuse the client instance to reconnect
    try {
      connectToken = mqttClient.connect( new MqttConnectOptions() );
      Log.info( "Connecting again...(serverURI:" + uri + ", ClientId:testConnectTimeout" );
      connectToken.waitForCompletion( 5000 );
    } catch ( Exception exception ) {
      Log.info( "Connect action failed at expected." );
      Assert.assertTrue( exception instanceof MqttException );
      Assert.assertEquals( ( MqttException.CLIENT_TIMEOUT == ( (MqttException)exception ).getReasonCode() || MqttException.CONNECT_IN_PROGRESS == ( (MqttException)exception ).getReasonCode() ), true );
    }
    finally {
      if ( mqttClient != null ) {
        Log.info( "Close..." + mqttClient );
        mqttClient.disconnectForcibly( 5000, 5000 );
        mqttClient.close();
      }
    }

    Assert.assertFalse( mqttClient.isConnected() );

  }
}
