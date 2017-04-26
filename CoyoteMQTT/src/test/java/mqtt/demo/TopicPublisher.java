package mqtt.demo;

import coyote.commons.network.mqtt.MqttClient;
import coyote.commons.network.mqtt.MqttClientImpl;
import coyote.commons.network.mqtt.MqttConnectOptions;
import coyote.commons.network.mqtt.MqttException;
import coyote.commons.network.mqtt.MqttMessage;


/**
 * A MQTT topic publisher 
 */
public class TopicPublisher {

  public void run( String... args ) {
    System.out.println( "TopicPublisher initializing..." );

    try {
      // Create an MQTT client
      MqttClient mqttClient = new MqttClientImpl( "tcp://" + args[0], "HelloWorldPub" );
      MqttConnectOptions connOpts = new MqttConnectOptions();
      connOpts.setCleanSession( true );

      // Connect the client
      System.out.println( "Connecting to broker: tcp://" + args[0] );
      mqttClient.connect( connOpts );
      System.out.println( "Connected" );

      // Create a MQTT message
      String content = "Hello world from MQTT!";
      MqttMessage message = new MqttMessage( content.getBytes() );
      // Set the QoS on the Messages - Here we are using QoS of 0
      message.setQos( 0 );

      System.out.println( "Publishing message: " + content );

      // Publish the message
      mqttClient.publish( "T/GettingStarted/pubsub", message );

      // Disconnect the client
      mqttClient.disconnect();

      System.out.println( "Message published. Exiting" );

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
      System.out.println( "Usage: TopicPublisher <msg_backbone_ip:port>" );
      System.exit( -1 );
    }

    TopicPublisher app = new TopicPublisher();
    app.run( args );
  }
}
