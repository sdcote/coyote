/*
 * Copyright (c) 2016 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 *
 * Contributors:
 *   Stephan D. Cote 
 *      - Initial concept and implementation
 */
package mqtt.demo;

import java.io.File;

import coyote.azure.IotHubConfig;
import coyote.commons.network.mqtt.ClientListener;
import coyote.commons.network.mqtt.MQTT;
import coyote.commons.network.mqtt.MqttClient;
import coyote.commons.network.mqtt.MqttClientImpl;
import coyote.commons.network.mqtt.MqttConnectOptions;
import coyote.commons.network.mqtt.MqttDeliveryToken;
import coyote.commons.network.mqtt.MqttException;
import coyote.commons.network.mqtt.MqttMessage;
import coyote.commons.network.mqtt.MqttToken;
import coyote.commons.network.mqtt.cache.MemoryCache;
import coyote.dataframe.DataFrame;
import coyote.loader.log.ConsoleAppender;
import coyote.loader.log.FileAppender;
import coyote.loader.log.Log;


/**
 * 
 */
public class AzureIotHub implements ClientListener {

  /**
   * @param args
   */
  public static void main( String[] args ) {

    MqttMessage response = null;

    // Add a logger that will send log messages to the console 
    //Log.addLogger( Log.DEFAULT_LOGGER_NAME, new ConsoleAppender( Log.ALL_EVENTS ) );
    Log.addLogger( "mqtt", new FileAppender( new File( "mqtt.log" ), MQTT.EVENT| Log.TRACE_EVENTS , false ) );
    Log.addLogger( Log.DEFAULT_LOGGER_NAME, new ConsoleAppender( Log.INFO_EVENTS | Log.WARN_EVENTS | Log.ERROR_EVENTS | Log.FATAL_EVENTS ) );

    // These are the fundamental attributes we need to connect to the Azure IoT Hub
    String hubname = "CoyoteIoT";
    String deviceId = "device-fcbd127a";
    String deviceKey = "ypZ2F76vfOkYYHKsRbQOYP6SKW7/TOo4maD9GmqYMII=";

    // Place them all in a utility object to generate the data we need
    IotHubConfig hubConfig = new IotHubConfig( hubname, deviceId, deviceKey );

    System.out.println( "brokerUri: " + hubConfig.getBrokerURI() );
    System.out.println( "clientId: " + hubConfig.getClientId() );
    System.out.println( "Username: " + hubConfig.getUsername() );
    System.out.println( "Password: " + new String( hubConfig.getPassword() ) );

    MqttClient client = null;
    try {
      client = new MqttClientImpl( hubConfig.getBrokerURI(), hubConfig.getClientId(), new MemoryCache() );

      if ( client != null ) {
        // Set the callback to an instance of this class         
        client.setCallback( new AzureIotHub() );

        // Setup the connection options
        MqttConnectOptions options = new MqttConnectOptions();
        options.setAutomaticReconnect( true );
        options.setCleanSession( false );
        options.setUserName( hubConfig.getUsername() );
        options.setPassword( hubConfig.getPassword() );
        options.setConnectionTimeout( 60 );
        options.setKeepAliveInterval( 20 );
        options.setMqttVersion( 4 );

        // connect to the broker
        MqttToken token = client.connect( options );

        token.waitForCompletion( 5000 );

        if ( client.isConnected() ) {

          client.subscribe( hubConfig.getReceiveTopic(), 0 );

          DataFrame data = new DataFrame();
          data.put( "MSG", "Hello Azure!" );

          MqttMessage message = new MqttMessage();
          message.setPayload( data.getBytes() );
          token = client.publish( hubConfig.getSendTopic(), message );
          Log.info( "Published message" );
          token.waitForCompletion( 5000 );
          if ( token.isComplete() ) {
            Log.info( "Message delivery complete" );
          } else {
            Log.error( "Delivery failed" );
          }

          while ( response == null ) {
            try {
              Thread.sleep( 500 );
            } catch ( InterruptedException e ) {
              System.out.println( e.getMessage() );
            }
          }

        } else {
          Log.error( "Could not connect to Azure IoT hub, timed-out" );
        }
      }
    } catch ( MqttException e ) {
      Log.fatal( e.getMessage() + " cause: " + e.getCause() );
      MQTT.logClientDebug( client );
    }
    finally {
      if ( client != null ) {
        try {
          client.disconnect();
        } catch ( MqttException ignore ) {}
      }
    }

  }




  @Override
  public void connectionLost( Throwable cause ) {
    Log.warn( "CONNECTION LOST: " + cause );
  }




  @Override
  public void deliveryComplete( MqttDeliveryToken token ) {
    Log.info( "DELIVERY: " + token );

    System.out.println( token.getResponse() );
    try {
      MqttMessage msg = token.getMessage();
      if ( msg != null ) {
        System.out.println( msg );
      }
    } catch ( MqttException e ) {
      e.printStackTrace();
    }
  }




  @Override
  public void messageArrived( String topic, MqttMessage message ) throws Exception {

  }




  @Override
  public void connectComplete( boolean reconnect, String serverURI ) {
    Log.info( "CONNECTED" );
  }

}
