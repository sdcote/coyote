package coyote.commons.network.mqtt;

import java.net.URI;
import java.util.ArrayList;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import coyote.commons.network.mqtt.ClientListener;
import coyote.commons.network.mqtt.MqttConnectOptions;
import coyote.commons.network.mqtt.MqttDeliveryToken;
import coyote.commons.network.mqtt.MqttException;
import coyote.commons.network.mqtt.MqttMessage;
import coyote.commons.network.mqtt.Topic;
import coyote.commons.network.mqtt.client.TestClientFactory;
import coyote.commons.network.mqtt.properties.TestProperties;
import coyote.loader.log.Log;


/**
 * Tests providing a basic general coverage for the MQTT client API
 */
public class BasicTest {

  private static URI serverURI;
  private static TestClientFactory clientFactory;




  /**
   * @throws Exception 
   */
  @BeforeClass
  public static void setUpBeforeClass() throws Exception {

    try {
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
  @Test
  public void testConnect() throws Exception {

    MqttBlockingClient client = null;
    try {
      String clientId = "connect";
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
  @Test
  public void testHAConnect() throws Exception {

    // Some old clients do not support the new HA interface on the connect call
    if ( clientFactory.isHighAvalabilitySupported() == false ) {
      return;
    }

    MqttBlockingClient client = null;
    try {
      try {
        String clientId = "HAconnect";

        // If a client does not support the URI list in the connect options, then this test should fail.
        // We ensure this happens by using a junk URI when creating the client. 
        URI junk = new URI( "tcp://junk:123" );
        client = clientFactory.createMqttClient( junk, clientId );

        // The first URI has a good protocol, but has a garbage hostname. 
        // This ensures that a connect is attempted to the the second URI in the list 
        String[] urls = new String[] { "tcp://junk", serverURI.toString() };

        MqttConnectOptions options = new MqttConnectOptions();
        options.setServerURIs( urls );

        Log.info( "Connecting..." );
        client.connect( options );

        Log.info( "Disconnecting..." );
        client.disconnect();
      } catch ( Exception e ) {
        // logger.info(e.getClass().getName() + ": " + e.getMessage());
        e.printStackTrace();
        throw e;
      }
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
  @Test
  @Ignore
  public void testPubSub() throws Exception {

    MqttBlockingClient client = null;
    try {
      String topicStr = "topic_02";
      String clientId = "PubSub";
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
   * @throws Exception 
   */
  @Test
  public void testMsgProperties() throws Exception {

    Log.info( "Checking defaults for empty message" );
    MqttMessage msg = new MqttMessage();
    Assert.assertTrue( msg.getQos() == 1 );
    Assert.assertTrue( msg.isDuplicate() == false );
    Assert.assertTrue( msg.isRetained() == false );
    Assert.assertNotNull( msg.getPayload() );
    Assert.assertTrue( msg.getPayload().length == 0 );
    Assert.assertEquals( msg.toString(), "" );

    Log.info( "Checking defaults for message with payload" );
    msg = new MqttMessage( "foo".getBytes() );
    Assert.assertTrue( msg.getQos() == 1 );
    Assert.assertTrue( msg.isDuplicate() == false );
    Assert.assertTrue( msg.isRetained() == false );
    Assert.assertTrue( msg.getPayload().length == 3 );
    Assert.assertEquals( msg.toString(), "foo" );

    Log.info( "Checking QoS" );
    msg.setQos( 0 );
    Assert.assertTrue( msg.getQos() == 0 );
    msg.setQos( 1 );
    Assert.assertTrue( msg.getQos() == 1 );
    msg.setQos( 2 );
    Assert.assertTrue( msg.getQos() == 2 );

    boolean thrown = false;
    try {
      msg.setQos( -1 );
    } catch ( IllegalArgumentException iae ) {
      thrown = true;
    }
    Assert.assertTrue( thrown );
    thrown = false;
    try {
      msg.setQos( 3 );
    } catch ( IllegalArgumentException iae ) {
      thrown = true;
    }
    Assert.assertTrue( thrown );
    thrown = false;

    Log.info( "Checking payload" );
    msg.setPayload( "foobar".getBytes() );
    Assert.assertTrue( msg.getPayload().length == 6 );
    Assert.assertEquals( msg.toString(), "foobar" );

    msg.clearPayload();
    Assert.assertNotNull( msg.getPayload() );
    Assert.assertTrue( msg.getPayload().length == 0 );
    Assert.assertEquals( msg.toString(), "" );

    Log.info( "Checking retained" );
    msg.setRetained( true );
    Assert.assertTrue( msg.isRetained() == true );
    msg.setRetained( false );
    Assert.assertTrue( msg.isRetained() == false );
  }




  /**
   * @throws Exception 
   */
  @Test
  public void testConnOptDefaults() throws Exception {

    Log.info( "Checking MqttConnectOptions defaults" );
    MqttConnectOptions connOpts = new MqttConnectOptions();
    Assert.assertEquals( new Integer( connOpts.getKeepAliveInterval() ), new Integer( 60 ) );
    Assert.assertNull( connOpts.getPassword() );
    Assert.assertNull( connOpts.getUserName() );
    Assert.assertNull( connOpts.getSocketFactory() );
    Assert.assertTrue( connOpts.isCleanSession() );
    Assert.assertNull( connOpts.getWillDestination() );
    Assert.assertNull( connOpts.getWillMessage() );
    Assert.assertNull( connOpts.getSSLProperties() );
  }

  // -------------------------------------------------------------
  // Helper methods/classes
  // -------------------------------------------------------------

  static final Class<MessageListener> cclass2 = MessageListener.class;
  static final String classSimpleName2 = cclass2.getSimpleName();
  static final String classCanonicalName2 = cclass2.getCanonicalName();

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
            messages.wait( 1000 );
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
      Log.info( "connection lost: " + cause.getMessage() );
    }




    /**
     * @param token  
     */
    public void deliveryComplete( MqttDeliveryToken token ) {
      Log.info( "delivery complete" );
    }




    /**
     * @param topic  
     * @param message 
     * @throws Exception 
     */
    public void messageArrived( String topic, MqttMessage message ) throws Exception {
      Log.info( "message arrived: " + new String( message.getPayload() ) + "'" );

      synchronized( messages ) {
        messages.add( message );
        messages.notifyAll();
      }
    }




    @Override
    public void connectComplete( boolean reconnect, String serverURI ) {
      Log.info( "Connected" );
    }
    
  }
}
