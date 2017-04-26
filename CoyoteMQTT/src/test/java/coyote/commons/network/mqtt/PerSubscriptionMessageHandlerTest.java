package coyote.commons.network.mqtt;

import java.net.URI;
import java.util.ArrayList;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import coyote.commons.network.mqtt.MessageListener;
import coyote.commons.network.mqtt.MqttClient;
import coyote.commons.network.mqtt.MqttConnectOptions;
import coyote.commons.network.mqtt.MqttMessage;
import coyote.commons.network.mqtt.MqttToken;
import coyote.commons.network.mqtt.client.TestClientFactory;
import coyote.commons.network.mqtt.properties.TestProperties;
import coyote.commons.network.mqtt.utilities.Utility;
import coyote.loader.log.Log;


public class PerSubscriptionMessageHandlerTest {

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

  class listener implements MessageListener {

    ArrayList<MqttMessage> messages;




    public listener() {
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




    public void messageArrived( String topic, MqttMessage message ) throws Exception {

      Log.info( "message arrived: '" + new String( message.getPayload() ) + "' " + this.hashCode() + " " + ( message.isDuplicate() ? "duplicate" : "" ) );

      if ( !message.isDuplicate() ) {
        synchronized( messages ) {
          messages.add( message );
          messages.notifyAll();
        }
      }
    }
  }




  /**
   * Basic test with 1 subscription for the synchronous client
   * 
   * @throws Exception
   */
  @Test
  public void testSyncSubs1() throws Exception {
    listener mylistener = new listener();
    MqttBlockingClient mqttClient = clientFactory.createMqttClient( serverURI, "F00" );
    String mytopic = "PerSubscriptionTest/topic";

    mqttClient.connect();
    Log.info( "Connecting...(serverURI:" + serverURI + ", ClientId:F00" );

    mqttClient.subscribe( mytopic, 2, mylistener );

    MqttMessage message = new MqttMessage();
    message.setPayload( "testSyncSubs1".getBytes() );
    mqttClient.publish( mytopic, message );

    Log.info( "Checking msg" );
    MqttMessage msg = mylistener.getNextMessage();
    Assert.assertNotNull( msg );
    Assert.assertEquals( "testSyncSubs1", msg.toString() );

    mqttClient.disconnect();

    mqttClient.close();

  }




  @Test
  public void testAsyncSubs1() throws Exception {

    listener mylistener = new listener();
    MqttClient mqttClient = clientFactory.createMqttAsyncClient( serverURI, "B4R" );
    String mytopic = "PerSubscriptionTest/topic";

    MqttToken token = mqttClient.connect( null, null );
    Log.info( "Connecting...(serverURI:" + serverURI + ", ClientId:B4R" );
    token.waitForCompletion();

    token = mqttClient.subscribe( mytopic, 2, mylistener );
    token.waitForCompletion();

    MqttMessage message = new MqttMessage();
    message.setPayload( "testAsyncSubs1".getBytes() );
    token = mqttClient.publish( mytopic, message );
    token.waitForCompletion();

    Log.info( "Checking msg" );
    MqttMessage msg = mylistener.getNextMessage();
    Assert.assertNotNull( msg );
    Assert.assertEquals( "testAsyncSubs1", msg.toString() );

    token = mqttClient.disconnect();
    token.waitForCompletion();

    mqttClient.close();

  }




  /*
   *  Check handlers still exist after reconnection non-cleansession
   */

  @Test
  public void testSyncCleanSessionFalse() throws Exception {

    listener mylistener = new listener();
    MqttBlockingClient mqttClient = clientFactory.createMqttClient( serverURI, "F001" );
    String mytopic = "PerSubscriptionTest/topic";

    MqttConnectOptions opts = new MqttConnectOptions();
    opts.setCleanSession( false );

    mqttClient.connect( opts );
    Log.info( "Connecting...(serverURI:" + serverURI + ", ClientId:F001" );

    mqttClient.subscribe( mytopic, 2, mylistener );

    MqttMessage message = new MqttMessage();
    message.setPayload( "testSyncCleanSessionFalse".getBytes() );
    mqttClient.publish( mytopic, message );

    Log.info( "Checking msg" );
    MqttMessage msg = mylistener.getNextMessage();
    Assert.assertNotNull( msg );
    Assert.assertEquals( "testSyncCleanSessionFalse", msg.toString() );

    mqttClient.disconnect();

    /* subscription handler should still exist on reconnect */

    mqttClient.connect( opts );
    Log.info( "Connecting...(serverURI:" + serverURI + ", ClientId:F001" );

    message = new MqttMessage();
    message.setPayload( "testSyncCleanSessionFalse1".getBytes() );
    mqttClient.publish( mytopic, message );

    Log.info( "Checking msg" );
    msg = mylistener.getNextMessage();
    Assert.assertNotNull( msg );
    Assert.assertEquals( "testSyncCleanSessionFalse1", msg.toString() );

    mqttClient.disconnect();

    /* clean up by connecting cleansession */
    mqttClient.connect();
    mqttClient.disconnect();

    mqttClient.close();
  }




  @Test
  public void testAsyncCleanSessionFalse() throws Exception {

    listener mylistener = new listener();
    MqttClient mqttClient = clientFactory.createMqttAsyncClient( serverURI, "F002" );
    String mytopic = "PerSubscriptionTest/topic";

    MqttConnectOptions opts = new MqttConnectOptions();
    opts.setCleanSession( false );

    MqttToken token = mqttClient.connect( opts, null, null );
    Log.info( "Connecting...(serverURI:" + serverURI + ", ClientId:F002" );
    token.waitForCompletion();

    token = mqttClient.subscribe( mytopic, 2, mylistener );
    token.waitForCompletion();

    MqttMessage message = new MqttMessage();
    message.setPayload( "testAsyncCleanSessionFalse".getBytes() );
    token = mqttClient.publish( mytopic, message );
    token.waitForCompletion();

    Log.info( "Checking msg" );
    MqttMessage msg = mylistener.getNextMessage();
    Assert.assertNotNull( msg );
    Assert.assertEquals( "testAsyncCleanSessionFalse", msg.toString() );

    token = mqttClient.disconnect();
    token.waitForCompletion();

    /* subscription handler should still exist on reconnect */

    token = mqttClient.connect( opts, null, null );
    Log.info( "Connecting...(serverURI:" + serverURI + ", ClientId:F002" );
    token.waitForCompletion();

    message = new MqttMessage();
    message.setPayload( "testAsyncCleanSessionFalse1".getBytes() );
    token = mqttClient.publish( mytopic, message );
    token.waitForCompletion();

    Log.info( "Checking msg" );
    msg = mylistener.getNextMessage();
    Assert.assertNotNull( msg );
    Assert.assertEquals( "testAsyncCleanSessionFalse1", msg.toString() );

    token = mqttClient.disconnect();
    token.waitForCompletion();

    /* clean up by connecting cleansession */
    token = mqttClient.connect();
    token.waitForCompletion();
    token = mqttClient.disconnect();
    token.waitForCompletion();

    mqttClient.close();
  }

}
