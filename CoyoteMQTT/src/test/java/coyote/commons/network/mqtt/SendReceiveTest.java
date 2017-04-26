package coyote.commons.network.mqtt;

import java.net.URI;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import coyote.commons.network.mqtt.MqttConnectOptions;
import coyote.commons.network.mqtt.Topic;
import coyote.commons.network.mqtt.client.TestClientFactory;
import coyote.commons.network.mqtt.properties.TestProperties;
import coyote.commons.network.mqtt.utilities.MqttV3Receiver;
import coyote.commons.network.mqtt.utilities.Utility;
import coyote.loader.log.Log;


/**
 * This test expects an MQTT Server to be listening on the port 
 * given by the SERVER_URI property (which is 1883 by default)
 */
public class SendReceiveTest {

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
   * Tests that a client can be constructed and that it can connect to and disconnect from the
   * service
   * @throws Exception 
   */
  @Test
  public void testConnect() throws Exception {

    MqttBlockingClient mqttClient = null;
    try {
      mqttClient = clientFactory.createMqttClient( serverURI, "testConnect" );
      Log.info( "Connecting...(serverURI:" + serverURI + ", ClientId:testConnect" );
      mqttClient.connect();
      Log.info( "Disconnecting..." );
      mqttClient.disconnect();
      Log.info( "Connecting...(serverURI:" + serverURI + ", ClientId:testConnect" );
      mqttClient.connect();
      Log.info( "Disconnecting..." );
      mqttClient.disconnect();
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
   * @throws Exception 
   */
  @Test
  public void testRemoteConnect() throws Exception {

    MqttBlockingClient mqttClient = null;
    try {
      mqttClient = clientFactory.createMqttClient( serverURI, "testRemoteConnect" );
      Log.info( "Connecting...(serverURI:" + serverURI + ", ClientId:testRemoteConnect" );
      mqttClient.connect();
      Log.info( "Disconnecting..." );
      mqttClient.disconnect();

      MqttV3Receiver mqttV3Receiver = new MqttV3Receiver( mqttClient, System.out );
      Log.info( "Assigning callback..." );
      mqttClient.setCallback( mqttV3Receiver );
      MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
      mqttConnectOptions.setCleanSession( false );
      Log.info( "Connecting...(serverURI:" + serverURI + ", ClientId:testRemoteConnect, cleanSession: false" );
      mqttClient.connect( mqttConnectOptions );

      String[] topicNames = new String[] { "testRemoteConnect/Topic" };
      int[] topicQos = { 0 };
      Log.info( "Subscribing to..." + topicNames[0] );
      mqttClient.subscribe( topicNames, topicQos );

      byte[] payload = ( "Message payload testRemoteConnect" ).getBytes();
      Topic mqttTopic = mqttClient.getTopic( topicNames[0] );
      Log.info( "Publishing to..." + topicNames[0] );
      mqttTopic.publish( payload, 1, false );
      boolean ok = mqttV3Receiver.validateReceipt( topicNames[0], 0, payload );
      if ( !ok ) {
        Assert.fail( "Receive failed" );
      }
      Log.info( "Disconnecting..." );
      mqttClient.disconnect();
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
   * Test client pubSub using largish messages
   */
  @Test
  public void testLargeMessage() {

    MqttBlockingClient mqttClient = null;
    try {
      mqttClient = clientFactory.createMqttClient( serverURI, "testLargeMessage" );
      MqttV3Receiver mqttV3Receiver = new MqttV3Receiver( mqttClient, System.out );
      Log.info( "Assigning callback..." );
      mqttClient.setCallback( mqttV3Receiver );
      Log.info( "Connecting...(serverURI:" + serverURI + ", ClientId:testLargeMessage" );
      mqttClient.connect();

      int largeSize = 10000;
      String[] topicNames = new String[] { "testLargeMessage/Topic" };
      int[] topicQos = { 0 };
      byte[] message = new byte[largeSize];

      java.util.Arrays.fill( message, (byte)'s' );

      Log.info( "Subscribing to..." + topicNames[0] );
      mqttClient.subscribe( topicNames, topicQos );
      Log.info( "Unsubscribing from..." + topicNames[0] );
      mqttClient.unsubscribe( topicNames );
      Log.info( "Subscribing to..." + topicNames[0] );
      mqttClient.subscribe( topicNames, topicQos );
      Topic mqttTopic = mqttClient.getTopic( topicNames[0] );
      Log.info( "Publishing to..." + topicNames[0] );
      mqttTopic.publish( message, 0, false );

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
          Log.info( "Disconnecting..." );
          mqttClient.disconnect();
          Log.info( "Close..." );
          mqttClient.close();
        }
      } catch ( Exception exception ) {
        Log.error( "caught exception:", exception );
      }
    }

  }




  /**
   * Test that QOS values are preserved between MQTT publishers and subscribers.
   */
  @Test
  public void testQoSPreserved() {

    MqttBlockingClient mqttClient = null;
    try {
      mqttClient = clientFactory.createMqttClient( serverURI, "testQoSPreserved" );
      MqttV3Receiver mqttV3Receiver = new MqttV3Receiver( mqttClient, System.out );
      Log.info( "Assigning callback..." );
      mqttClient.setCallback( mqttV3Receiver );
      Log.info( "Connecting...(serverURI:" + serverURI + ", ClientId:testQoSPreserved" );
      mqttClient.connect();

      String[] topicNames = new String[] { "testQoSPreserved/Topic0", "testQoSPreserved/Topic1", "testQoSPreserved/Topic2" };
      int[] topicQos = { 0, 1, 2 };
      for ( int i = 0; i < topicNames.length; i++ ) {
        Log.info( "Subscribing to..." + topicNames[i] + " at Qos " + topicQos[i] );
      }
      mqttClient.subscribe( topicNames, topicQos );

      for ( int i = 0; i < topicNames.length; i++ ) {
        byte[] message = ( "Message payload testQoSPreserved " + topicNames[i] ).getBytes();
        Topic mqttTopic = mqttClient.getTopic( topicNames[i] );
        for ( int iQos = 0; iQos < 3; iQos++ ) {
          Log.info( "Publishing to..." + topicNames[i] + " at Qos " + iQos );
          mqttTopic.publish( message, iQos, false );
          boolean ok = mqttV3Receiver.validateReceipt( topicNames[i], Math.min( iQos, topicQos[i] ), message );
          if ( !ok ) {
            Assert.fail( "Receive failed sub Qos=" + topicQos[i] + " PublishQos=" + iQos );
          }
        }
      }
    } catch ( Exception exception ) {
      Log.error( "caught exception:", exception );
      Assert.fail( "Failed:testQoSPreserved exception=" + exception );
    }
    finally {
      try {
        if ( mqttClient != null ) {
          Log.info( "Disconnecting..." );
          mqttClient.disconnect();
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
   * @throws Exception 
   */
  @Test
  public void testMultipleClients() throws Exception {

    MqttBlockingClient[] mqttPublisher = new MqttBlockingClient[2];
    MqttBlockingClient[] mqttSubscriber = new MqttBlockingClient[10];
    try {
      String[] topicNames = new String[] { "testMultipleClients/Topic" };
      int[] topicQos = { 0 };

      Topic[] mqttTopic = new Topic[mqttPublisher.length];
      for ( int i = 0; i < mqttPublisher.length; i++ ) {
        mqttPublisher[i] = clientFactory.createMqttClient( serverURI, "MultiPub" + i );
        Log.info( "Connecting...(serverURI:" + serverURI + ", ClientId: MultiPub" + i );
        mqttPublisher[i].connect();
        mqttTopic[i] = mqttPublisher[i].getTopic( topicNames[0] );
      } // for...

      MqttV3Receiver[] mqttV3Receiver = new MqttV3Receiver[mqttSubscriber.length];
      for ( int i = 0; i < mqttSubscriber.length; i++ ) {
        mqttSubscriber[i] = clientFactory.createMqttClient( serverURI, "MultiSubscriber" + i );
        mqttV3Receiver[i] = new MqttV3Receiver( mqttSubscriber[i], System.out );
        Log.info( "Assigning callback..." );
        mqttSubscriber[i].setCallback( mqttV3Receiver[i] );
        Log.info( "Connecting...(serverURI:" + serverURI + ", ClientId: MultiSubscriber" + i );
        mqttSubscriber[i].connect();
        Log.info( "Subcribing to..." + topicNames[0] );
        mqttSubscriber[i].subscribe( topicNames, topicQos );
      } // for...

      for ( int iMessage = 0; iMessage < 10; iMessage++ ) {
        byte[] payload = ( "Message " + iMessage ).getBytes();
        for ( int i = 0; i < mqttPublisher.length; i++ ) {
          Log.info( "Publishing to..." + topicNames[0] );
          mqttTopic[i].publish( payload, 0, false );
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
      throw exception;
    }
    finally {
      try {
        for ( int i = 0; i < mqttPublisher.length; i++ ) {
          Log.info( "Disconnecting...MultiPub" + i );
          mqttPublisher[i].disconnect();
          Log.info( "Close..." );
          mqttPublisher[i].close();
        }
        for ( int i = 0; i < mqttSubscriber.length; i++ ) {
          Log.info( "Disconnecting...MultiSubscriber" + i );
          mqttSubscriber[i].disconnect();
          Log.info( "Close..." );
          mqttSubscriber[i].close();
        }

        Thread.sleep( 5000 );
      } catch ( Exception exception ) {
        Log.error( "caught exception:", exception );
      }
    }

  }




  /**
   * Test the behavior of the cleanStart flag, used to clean up before re-connecting.
   * @throws Exception 
   */
  @Test
  public void testCleanStart() throws Exception {

    MqttBlockingClient mqttClient = null;
    try {
      mqttClient = clientFactory.createMqttClient( serverURI, "testCleanStart" );
      MqttV3Receiver mqttV3Receiver = new MqttV3Receiver( mqttClient, System.out );
      Log.info( "Assigning callback..." );
      mqttClient.setCallback( mqttV3Receiver );
      MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
      // Clean start: true  - The broker cleans up all client state, including subscriptions, when the client is disconnected.
      // Clean start: false - The broker remembers all client state, including subscriptions, when the client is disconnected.
      //                      Matching publications will get queued in the broker whilst the client is disconnected.
      // For Mqtt V3 cleanSession=false, implies new subscriptions are durable.
      mqttConnectOptions.setCleanSession( false );
      Log.info( "Connecting...(serverURI:" + serverURI + ", ClientId:testCleanStart, cleanSession: false" );
      mqttClient.connect( mqttConnectOptions );

      String[] topicNames = new String[] { "testCleanStart/Topic" };
      int[] topicQos = { 0 };
      Log.info( "Subscribing to..." + topicNames[0] );
      mqttClient.subscribe( topicNames, topicQos );

      byte[] payload = ( "Message payload testCleanStart First" ).getBytes();
      Topic mqttTopic = mqttClient.getTopic( topicNames[0] );
      Log.info( "Publishing to..." + topicNames[0] );
      mqttTopic.publish( payload, 1, false );
      boolean ok = mqttV3Receiver.validateReceipt( topicNames[0], 0, payload );
      if ( !ok ) {
        Assert.fail( "Receive failed" );
      }

      // Disconnect and reconnect to make sure the subscription and all queued messages are cleared.
      Log.info( "Disconnecting..." );
      mqttClient.disconnect();
      Log.info( "Close" );
      mqttClient.close();

      // Send a message from another client, to our durable subscription.
      mqttClient = clientFactory.createMqttClient( serverURI, "testCleanStartOther" );
      mqttV3Receiver = new MqttV3Receiver( mqttClient, System.out );
      Log.info( "Assigning callback..." );
      mqttClient.setCallback( mqttV3Receiver );
      mqttConnectOptions = new MqttConnectOptions();
      mqttConnectOptions.setCleanSession( true );
      Log.info( "Connecting...(serverURI:" + serverURI + ", ClientId:testCleanStartOther, cleanSession: true" );
      mqttClient.connect( mqttConnectOptions );
      // Receive the publication so that we can be sure the first client has also received it.
      // Otherwise the first client may reconnect with its clean session before the message has arrived.
      Log.info( "Subscribing to..." + topicNames[0] );
      mqttClient.subscribe( topicNames, topicQos );
      payload = ( "Message payload testCleanStart Other client" ).getBytes();
      mqttTopic = mqttClient.getTopic( topicNames[0] );
      Log.info( "Publishing to..." + topicNames[0] );
      mqttTopic.publish( payload, 1, false );
      ok = mqttV3Receiver.validateReceipt( topicNames[0], 0, payload );
      if ( !ok ) {
        Assert.fail( "Receive failed" );
      }
      Log.info( "Disconnecting..." );
      mqttClient.disconnect();
      Log.info( "Close..." );
      mqttClient.close();

      // Reconnect and check we have no messages.
      mqttClient = clientFactory.createMqttClient( serverURI, "testCleanStart" );
      mqttV3Receiver = new MqttV3Receiver( mqttClient, System.out );
      Log.info( "Assigning callback..." );
      mqttClient.setCallback( mqttV3Receiver );
      mqttConnectOptions = new MqttConnectOptions();
      mqttConnectOptions.setCleanSession( true );
      Log.info( "Connecting...(serverURI:" + serverURI + ", ClientId:testCleanStart, cleanSession: true" );
      mqttClient.connect( mqttConnectOptions );
      MqttV3Receiver.ReceivedMessage receivedMessage = mqttV3Receiver.receiveNext( 100 );
      if ( receivedMessage != null ) {
        Assert.fail( "Receive messaqe:" + new String( receivedMessage.message.getPayload() ) );
      }

      // Also check that subscription is cancelled.
      payload = ( "Message payload testCleanStart Cancelled Subscription" ).getBytes();
      mqttTopic = mqttClient.getTopic( topicNames[0] );
      Log.info( "Publishing to..." + topicNames[0] );
      mqttTopic.publish( payload, 1, false );

      receivedMessage = mqttV3Receiver.receiveNext( 100 );
      if ( receivedMessage != null ) {
        Log.info( "Message I shouldn't have: " + new String( receivedMessage.message.getPayload() ) );
        Assert.fail( "Receive messaqe:" + new String( receivedMessage.message.getPayload() ) );
      }
    } catch ( Exception exception ) {
      Log.error( "caught exception:", exception );
      throw exception;
    }
    finally {
      try {
        Log.info( "Disconnecting..." );
        mqttClient.disconnect();
      } catch ( Exception exception ) {
        // do nothing
      }

      try {
        Log.info( "Close..." );
        mqttClient.close();
      } catch ( Exception exception ) {
        // do nothing
      }
    }
  }

}
