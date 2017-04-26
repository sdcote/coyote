package coyote.commons.network.mqtt;

import java.net.URI;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import coyote.commons.network.mqtt.MqttConnectOptions;
import coyote.commons.network.mqtt.MqttException;
import coyote.commons.network.mqtt.Topic;
import coyote.commons.network.mqtt.client.TestClientFactory;
import coyote.commons.network.mqtt.properties.TestProperties;
import coyote.commons.network.mqtt.utilities.MqttV3Receiver;
import coyote.commons.network.mqtt.utilities.Utility;
import coyote.commons.network.mqtt.utilities.MqttV3Receiver.ReceivedMessage;
import coyote.loader.log.Log;


public class LiveTakeOverTest {

  private static URI serverURI;
  private static TestClientFactory clientFactory;

  static enum FirstClientState {
    INITIAL, READY, RUNNING, FINISHED, ERROR
  }

  private static String ClientId = "TakeOverClient";
  private static String FirstSubTopicString = "FirstClient/Topic";




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
   * Test that a client actively doing work can be taken over
   * @throws Exception 
   */
  @Test
  public void testLiveTakeOver() throws Exception {

    MqttBlockingClient mqttClient = null;
    try {
      FirstClient firstClient = new FirstClient();
      Thread firstClientThread = new Thread( firstClient );
      Log.info( "Starting the firstClient thread" );
      firstClientThread.start();
      Log.info( "firstClientThread Started" );

      firstClient.waitForState( FirstClientState.READY );

      Log.debug( "telling the 1st client to go and let it publish for 2 seconds" );
      //Tell the first client to go and let it publish for a couple of seconds
      firstClient.setState( FirstClientState.RUNNING );
      Thread.sleep( 2000 );

      Log.debug( "Client has been run for 2 seconds, now taking over connection" );

      //Now lets take over the connection  
      // Create a second MQTT client connection with the same clientid. The 
      // server should spot this and kick the first client connection off. 
      // To do this from the same box the 2nd client needs to use either
      // a different form of persistent store or a different locaiton for 
      // the store to the first client. 
      // MqttClientPersistence persist = new MemoryPersistence();
      mqttClient = clientFactory.createMqttClient( serverURI, ClientId, null );

      MqttV3Receiver mqttV3Receiver = new MqttV3Receiver( mqttClient, System.out );
      mqttClient.setCallback( mqttV3Receiver );
      MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
      mqttConnectOptions.setCleanSession( false );
      mqttConnectOptions.setWill( "WillTopic", "payload".getBytes(), 2, true );
      Log.info( "Connecting...(serverURI:" + serverURI + ", ClientId:" + ClientId );
      mqttClient.connect( mqttConnectOptions );

      //We should have taken over the first Client's subscription...we may have some
      //of his publishes arrive.
      // NOTE: as a different persistence is used for the second client any inflight 
      // publications from the client will not be recovered / restarted. This will 
      // leave debris on the server.
      Log.debug( "We should have taken over the first Client's subscription...we may have some of his publishes arrive." );
      //Ignore his publishes that arrive...
      ReceivedMessage oldMsg;
      do {
        oldMsg = mqttV3Receiver.receiveNext( 1000 );
      }
      while ( oldMsg != null );

      Log.debug( "Now check we have grabbed his subscription by publishing.." );
      //Now check we have grabbed his subscription by publishing..
      byte[] payload = ( "Message payload from second client " + getClass().getName() ).getBytes();
      Topic mqttTopic = mqttClient.getTopic( FirstSubTopicString );
      Log.info( "Publishing to..." + FirstSubTopicString );
      mqttTopic.publish( payload, 1, false );
      Log.info( "Publish sent, checking for receipt..." );

      boolean ok = mqttV3Receiver.validateReceipt( FirstSubTopicString, 1, payload );
      if ( !ok ) {
        throw new Exception( "Receive failed" );
      }
    } catch ( Exception exception ) {
      throw exception;
    }
    finally {
      try {
        if ( mqttClient != null ) {
          mqttClient.disconnect();
          Log.info( "Disconnecting..." );
          mqttClient.close();
          Log.info( "Close..." );
        }
      } catch ( Exception exception ) {
        throw exception;
      }
    }

  }

  class FirstClient implements Runnable {

    private FirstClientState state = FirstClientState.INITIAL;
    public Object stateLock = new Object();
    MqttBlockingClient mqttClient = null;
    MqttV3Receiver mqttV3Receiver = null;




    void waitForState( FirstClientState desiredState ) throws InterruptedException {
      synchronized( stateLock ) {
        while ( ( state != desiredState ) && ( state != FirstClientState.ERROR ) ) {
          try {
            stateLock.wait();
          } catch ( InterruptedException exception ) {
            throw exception;
          }
        }

        if ( state == FirstClientState.ERROR ) {
          Assert.fail( "Firstclient entered an ERROR state" );
        }
      }
    }




    void setState( FirstClientState newState ) {
      synchronized( stateLock ) {
        state = newState;
        stateLock.notifyAll();
      }
    }




    void connectAndSub() {
      try {
        mqttClient = clientFactory.createMqttClient( serverURI, ClientId );
        mqttV3Receiver = new MqttV3Receiver( mqttClient, System.out );
        mqttV3Receiver.setReportConnectionLoss( false );
        mqttClient.setCallback( mqttV3Receiver );
        MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
        mqttConnectOptions.setCleanSession( false );
        mqttConnectOptions.setWill( "WillTopic", "payload".getBytes(), 2, true );
        Log.info( "Connecting...(serverURI:" + serverURI + ", ClientId:" + ClientId );
        mqttClient.connect( mqttConnectOptions );
        Log.info( "Subscribing to..." + FirstSubTopicString );
        mqttClient.subscribe( FirstSubTopicString, 2 );
      } catch ( Exception exception ) {
        Log.error( "caugh exception:" + exception );
        setState( FirstClientState.ERROR );
        Assert.fail( "Failed ConnectAndSub exception=" + exception );
      }
    }




    void repeatedlyPub() {

      int i = 0;
      while ( mqttClient.isConnected() ) {
        try {
          if ( i > 999999 ) {
            i = 0;
          }
          byte[] payload = ( "Message payload " + getClass().getName() + ".publish" + ( i++ ) ).getBytes();
          Topic mqttTopic = mqttClient.getTopic( FirstSubTopicString );
          Log.info( "Publishing to..." + FirstSubTopicString );
          mqttTopic.publish( payload, 1, false );

        } catch ( Exception exception ) {
          Log.debug( "Caught exception:" + exception );
          // Don't fail - we are going to get an exception as we disconnected during takeOver
          // Its likely the publish rate is too high i.e. inflight window is full
        }
      }
    }




    public void run() {

      connectAndSub();
      try {
        setState( FirstClientState.READY );
        waitForState( FirstClientState.RUNNING );
        repeatedlyPub();
        Log.info( "FirstClient exiting..." );

        mqttClient.close();

      } catch ( InterruptedException exception ) {
        setState( FirstClientState.ERROR );
        Log.error( "caught exception:", exception );
      } catch ( MqttException exception ) {
        setState( FirstClientState.ERROR );
        Log.error( "caught exception:", exception );
      }
    }
  }
}
