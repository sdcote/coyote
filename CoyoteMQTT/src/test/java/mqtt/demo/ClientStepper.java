package mqtt.demo;

import coyote.commons.network.mqtt.MqttClient;
import coyote.commons.network.mqtt.MqttClientImpl;
import coyote.commons.network.mqtt.MqttConnectOptions;
import coyote.commons.network.mqtt.MqttException;
import coyote.commons.network.mqtt.MqttMessage;
import coyote.commons.network.mqtt.MqttToken;
import coyote.dataframe.DataFrame;
import coyote.loader.log.ConsoleAppender;
import coyote.loader.log.Log;


/**
 * 
 */
public class ClientStepper {

  public static void main( String[] args ) {

    // Add a logger that will send log messages to the console 
    Log.addLogger( Log.DEFAULT_LOGGER_NAME, new ConsoleAppender( Log.INFO_EVENTS | Log.WARN_EVENTS | Log.ERROR_EVENTS | Log.FATAL_EVENTS ) );
    //    Log.addLogger( Log.DEFAULT_LOGGER_NAME, new ConsoleAppender( Log.ALL_EVENTS ) );

    String brokerUri = "tcp://iot.eclipse.org:1883";
    String clientId = "CoyoteAsyncTest";

    MqttClient client = null;
    try {
      client = new MqttClientImpl( brokerUri, clientId );
    } catch ( MqttException e ) {
      e.printStackTrace();
    }

    if ( client != null ) {
      MqttConnectOptions options = new MqttConnectOptions();
      options.setAutomaticReconnect( true );
      options.setCleanSession( false );

      try {
        MqttToken token = client.connect( options );

        token.waitForCompletion( 5000 );

        if ( client.isConnected() ) {
          Log.info( "Success!" );

          DataFrame data = new DataFrame();
          data.put( "MSG", "Hello Paho!" );

          MqttMessage message = new MqttMessage();
          message.setPayload( data.getBytes() );
          token = client.publish( "test/hello", message );

          Log.info( "Published a message - token:" + token );
          while ( !token.isComplete() ) {
            Log.info( "waiting for completion of publish..." );
            try {
              Thread.sleep( 500 );
            } catch ( InterruptedException ignore ) {}
          }
          Log.info( "Publish acknowledged by broker!" );

        } else {
          Log.error( "Connection failed" );
        }

      } catch ( MqttException e ) {
        e.printStackTrace();
      }

      try {
        Log.info( "Disconnecting..." );
        client.disconnect();
      } catch ( MqttException e ) {
        e.printStackTrace();
      }
      
      Log.info( "Done" );

    }

  }
}
