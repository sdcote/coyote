package mqtt.demo;

import java.sql.Timestamp;
import java.util.concurrent.CountDownLatch;

import coyote.commons.network.mqtt.ClientListener;
import coyote.commons.network.mqtt.MqttClient;
import coyote.commons.network.mqtt.MqttClientImpl;
import coyote.commons.network.mqtt.MqttConnectOptions;
import coyote.commons.network.mqtt.MqttDeliveryToken;
import coyote.commons.network.mqtt.MqttDeliveryTokenImpl;
import coyote.commons.network.mqtt.MqttException;
import coyote.commons.network.mqtt.MqttMessage;


/**
 * A Mqtt topic subscriber
 */
public class TopicSubscriber {

  public void run( String... args ) {
    System.out.println( "TopicSubscriber initializing..." );

    try {
      // Create an Mqtt client
      MqttClient mqttClient = new MqttClientImpl( "tcp://" + args[0], "HelloWorldSub" );
      MqttConnectOptions connOpts = new MqttConnectOptions();
      connOpts.setCleanSession( true );

      // Connect the client
      System.out.println( "Connecting to broker: tcp://" + args[0] );
      mqttClient.connect( connOpts );
      System.out.println( "Connected" );

      // Latch used for synchronizing b/w threads
      final CountDownLatch latch = new CountDownLatch( 1 );

      // Topic filter the client will subscribe to
      final String subTopic = "T/GettingStarted/pubsub";

      // Callback - Anonymous inner-class for receiving messages
      mqttClient.setCallback( new ClientListener() {

        @Override
        public void messageArrived( String topic, MqttMessage message ) throws Exception {
          // Called when a message arrives from the server that matches any 
          // subscription made by the client
          String time = new Timestamp( System.currentTimeMillis() ).toString();
          System.out.println( "\nReceived a Message!" + "\n\tTime:    " + time + "\n\tTopic:   " + topic + "\n\tMessage: " + new String( message.getPayload() ) + "\n\tQoS:     " + message.getQos() + "\n" );
          latch.countDown(); // unblock main thread
        }




        @Override
        public void connectionLost( Throwable cause ) {
          System.out.println( "Connection to broker lost!" + cause.getMessage() );
          latch.countDown();
        }







        @Override
        public void deliveryComplete( MqttDeliveryToken token ) {
        }




        @Override
        public void connectComplete( boolean reconnect, String serverURI ) {
          // TODO Auto-generated method stub
          
        }

      } );

      // Subscribe client to the topic filter and a QoS level of 0
      System.out.println( "Subscribing client to topic: " + subTopic );
      mqttClient.subscribe( subTopic, 0 );

      // Wait for the message to be received
      try {
        latch.await(); // block here until message received, and latch will flip
      } catch ( InterruptedException e ) {
        System.out.println( "I was awoken while waiting" );
      }

      // Disconnect the client
      mqttClient.disconnect();
      System.out.println( "Exiting" );

      System.exit( 0 );
    } catch ( MqttException me ) {
      System.out.println( "reason " + me.getReasonCode() );
      System.out.println( "msg " + me.getMessage() );
      System.out.println( "loc " + me.getLocalizedMessage() );
      System.out.println( "cause " + me.getCause() );
      System.out.println( "excep " + me );
      me.printStackTrace();
    }
  }




  public static void main( String[] args ) {
    // Check command line arguments
    if ( args.length < 1 ) {
      System.out.println( "Usage: TopicSubscriber <msg_backbone_ip:port>" );
      System.exit( -1 );
    }

    TopicSubscriber app = new TopicSubscriber();
    app.run( args );
  }
}
