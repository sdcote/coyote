package coyote.commons.network.mqtt;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.UUID;
import java.util.logging.Logger;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import coyote.commons.network.mqtt.client.TestClientFactory;
import coyote.commons.network.mqtt.properties.TestProperties;
import coyote.commons.network.mqtt.utilities.Utility;
import coyote.loader.log.Log;


/**
 * Tests providing a basic general coverage for the MQTT WebSocket Functionality
 */

public class WebSocketTest {

  private static URI serverURI;
  private static TestClientFactory clientFactory;




  /**
   * @throws Exception
   */
  @BeforeClass
  public static void setUpBeforeClass() throws Exception {

    try {
      String methodName = Utility.getMethodName();

      serverURI = TestProperties.getWebSocketServerURI();
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
   * @throws Exception
   */
  @Ignore
  public void testWebSocketConnect() throws Exception {
    String methodName = Utility.getMethodName();

    MqttBlockingClient client = null;
    try {
      String clientId = methodName;
      client = clientFactory.createMqttClient( serverURI, clientId );

      Log.info( "Connecting...(serverURI:" + serverURI + ", ClientId:" + clientId );
      client.connect();

      String clientId2 = client.getClientId();
      Log.info( "clientId = " + clientId2 );

      boolean isConnected = client.isConnected();
      Log.info( "isConnected = " + isConnected );

      String id = client.getServerURI();
      Log.info( "ServerURI = " + id );

      Log.info( "Disconnecting..." );
      client.disconnect();

      Log.info( "Re-Connecting..." );
      client.connect();

      Log.info( "Disconnecting..." );
      client.disconnect();
    } catch ( MqttException exception ) {
      Log.error( "caught exception:", exception );
      Assert.fail( "Unexpected exception: " + exception );
    }
    finally {
      if ( client != null ) {
        Log.info( "Close..." );
        client.close();
      }
    }
  }




  /**
   * @throws Exception
   */
  @Ignore
  public void testWebSocketPubSub() throws Exception {
    String methodName = Utility.getMethodName();

    MqttBlockingClient client = null;
    try {
      String topicStr = UUID.randomUUID() + "/topic_02";
      String clientId = methodName;
      client = clientFactory.createMqttClient( serverURI, clientId );

      Log.info( "Assigning callback..." );
      MessageListener listener = new MessageListener();
      client.setCallback( listener );

      Log.info( "Connecting...(serverURI:" + serverURI + ", ClientId:" + clientId );
      client.connect();

      Log.info( "Subscribing to..." + topicStr );
      client.subscribe( topicStr );

      Log.info( "Publishing to..." + topicStr );
      Topic topic = client.getTopic( topicStr );
      MqttMessage message = new MqttMessage( "foo".getBytes() );
      topic.publish( message );

      Log.info( "Checking msg" );
      MqttMessage msg = listener.getNextMessage();
      Assert.assertNotNull( msg );
      Assert.assertEquals( "foo", msg.toString() );

      Log.info( "getTopic name" );
      String topicName = topic.getName();
      Log.info( "topicName = " + topicName );
      Assert.assertEquals( topicName, topicStr );

      Log.info( "Disconnecting..." );
      client.disconnect();
    }
    finally {
      if ( client != null ) {
        Log.info( "Close..." );
        client.close();
      }
    }
  }




  /**
   * Tests Websocker support for packets over 16KB
   * Prompted by Bug: 482432
   * https://bugs.eclipse.org/bugs/show_bug.cgi?id=482432
   * This test connects to a broker via WebSockets, subscribes
   * to a topic, publishes a large payload to it and checks
   * that it recieves the same payload.
  * @throws Exception
   */
  @Test
  public void largePayloadTest() throws Exception {
    // Generate large byte array;
    byte[] largeByteArray = new byte[32000];
    new Random().nextBytes( largeByteArray );
    String methodName = Utility.getMethodName();

    MqttBlockingClient client = null;
    try {
      String topicStr = UUID.randomUUID() + "/topic_largeFile_01";
      String clientId = methodName;
      client = clientFactory.createMqttClient( serverURI, clientId );

      Log.info( "Assigning callback..." );
      MessageListener listener = new MessageListener();
      client.setCallback( listener );

      Log.info( "Connecting... serverURI:" + serverURI + ", ClientId:" + clientId );
      client.connect();

      Log.info( "Subscribing to..." + topicStr );
      client.subscribe( topicStr );

      Log.info( "Publishing to..." + topicStr );
      Topic topic = client.getTopic( topicStr );
      MqttMessage message = new MqttMessage( largeByteArray );
      topic.publish( message );

      Log.info( "Checking msg" );
      MqttMessage msg = listener.getNextMessage();
      Assert.assertNotNull( msg );
      Assert.assertTrue( Arrays.equals( largeByteArray, msg.getPayload() ) );
      Log.info( "Disconnecting..." );
      client.disconnect();
      Log.info( "Disconnected..." );
    } catch ( Exception e ) {
      e.printStackTrace();
    }
    finally {
      if ( client != null ) {
        Log.info( "Close..." );
        client.close();
      }
    }

  }

  // -------------------------------------------------------------
  // Helper methods/classes
  // -------------------------------------------------------------

  static final Class<MessageListener> cclass2 = MessageListener.class;
  static final String classSimpleName2 = cclass2.getSimpleName();
  static final String classCanonicalName2 = cclass2.getCanonicalName();
  static final Logger logger2 = Logger.getLogger( classCanonicalName2 );

  /**
   *
   */
  class MessageListener implements ClientListener {

    ArrayList<MqttMessage> messages;




    public MessageListener() {
      messages = new ArrayList<MqttMessage>();
    }




    public MqttMessage getNextMessage() {
      synchronized( messages ) {
        if ( messages.size() == 0 ) {
          try {
            // Wait a bit longer than usual because of the largePayloadTest
            messages.wait( 10000 );
          } catch ( InterruptedException e ) {
            // empty
          }
        }

        if ( messages.size() == 0 ) {
          return null;
        }
        return messages.remove( 0 );
      }
    }




    public void connectionLost( Throwable cause ) {
      logger2.info( "connection lost: " + cause.getMessage() );
    }




    /**
     * @param token
     */
    public void deliveryComplete( MqttDeliveryToken token ) {
      logger2.info( "delivery complete" );
    }




    /**
     * @param topic
     * @param message
     * @throws Exception
     */
    public void messageArrived( String topic, MqttMessage message ) throws Exception {
      logger2.info( "message arrived: " + new String( message.getPayload() ) + "'" );

      synchronized( messages ) {
        messages.add( message );
        messages.notifyAll();
      }
    }




    @Override
    public void connectComplete( boolean reconnect, String serverURI ) {
      logger2.info( "Connected" );
    }
  }
}
