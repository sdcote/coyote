package coyote.commons.network.mqtt;

//import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import coyote.commons.ByteUtil;
import coyote.commons.network.mqtt.ClientListener;
import coyote.commons.network.mqtt.MQTT;
import coyote.commons.network.mqtt.MqttClient;
import coyote.commons.network.mqtt.MqttConnectOptions;
import coyote.commons.network.mqtt.MqttDeliveryToken;
import coyote.commons.network.mqtt.MqttMessage;


public class SimpleTests {

  @Before
  public void setUp() throws Exception {}




  @Test
  @Ignore
  public void testSend() {
    try {
      MqttBlockingClient client = new MqttBlockingClientImpl( "tcp://localhost:1883", "simpletest1" );
      client.connect();
      MqttMessage message = new MqttMessage();
      message.setPayload( "A single message".getBytes() );
      client.publish( "demo/test", message );
      client.disconnect();
    } catch ( Exception e ) {
      e.printStackTrace();
    }
  }




  @Test
  @Ignore
  public void testSendOptions() {
    try {
      MqttBlockingClient client = new MqttBlockingClientImpl( "tcp://localhost:1883", "simpletest2" );
      MqttConnectOptions options = new MqttConnectOptions();
      options.setWill( "demo/clienterrors", "I crashed".getBytes(), 2, true );
      client.connect( options );
      MqttMessage message = new MqttMessage();
      message.setPayload( "A single message".getBytes() );
      client.publish( "demo/test", message );
      client.disconnect();
    } catch ( Exception e ) {
      e.printStackTrace();
    }
  }




  @Test
  @Ignore
  public void testDeliveryCallback() {
    try {
      MqttBlockingClient client = new MqttBlockingClientImpl( "tcp://localhost:1883", "simpletest3" );
      client.setCallback( new CallbackHandler() );
      MqttMessage message = new MqttMessage();
      message.setPayload( "A single message".getBytes() );
      client.publish( "demo/test", message );
      client.disconnect();
    } catch ( Exception e ) {
      e.printStackTrace();
    }
  }




  @Test
  @Ignore
  public void testMQTTCalls() {
    try {
      MqttClient client = MQTT.createClient( "tcp://localhost:1883", "simpletest3" );
      client.setCallback( new CallbackHandler() );
      MqttMessage message = new MqttMessage();
      message.setPayload( "A single message".getBytes() );
      client.publish( "demo/test", message );
      client.disconnect();
    } catch ( Exception e ) {
      e.printStackTrace();
    }
  }




  @Test
  @Ignore
  public void testSubscribe() {
    try {
      MqttBlockingClient client = new MqttBlockingClientImpl( "tcp://localhost:1883", "simpletest4" );
      client.setCallback( new CallbackHandler() );

      client.subscribe( "demo/test" );

      // wait around for a message here, the Callback handler will handle the 
      // message when it comes.

      client.disconnect();
    } catch ( Exception e ) {
      e.printStackTrace();
    }
  }

  //

  //

  // 
  class CallbackHandler implements ClientListener {

    @Override
    public void connectionLost( Throwable cause ) {
      System.out.println( "Whoops! Connection lost: " + cause.getMessage() );
    }




    @Override
    public void deliveryComplete( MqttDeliveryToken token ) {
      System.out.println( "Message Delivered!" );
    }




    /**
     * @see coyote.commons.network.mqtt.ClientListener#messageArrived(java.lang.String, coyote.commons.network.mqtt.MqttMessage)
     */
    @Override
    public void messageArrived( String topic, MqttMessage message ) throws Exception {
      System.out.println( "Message for you sir!" );
      System.out.println( "From: " + topic );
      System.out.println( "ID: " + message.getId() );
      System.out.println( "QoS: " + message.getQos() );
      System.out.println( ByteUtil.dump( message.getPayload() ) );
    }




    @Override
    public void connectComplete( boolean reconnect, String serverURI ) {
      System.out.println( "Connected" );
    }

  }

}
