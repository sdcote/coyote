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

import coyote.commons.network.mqtt.MqttClient;
import coyote.commons.network.mqtt.MqttClientImpl;
import coyote.commons.network.mqtt.MqttException;
import coyote.commons.network.mqtt.MqttToken;
import coyote.loader.Context;
import coyote.loader.component.AbstractManagedComponent;
import coyote.loader.log.Log;


/**
 * 
 */
public class Producer extends AbstractManagedComponent {

  /**
   * 
   */
  public Producer() {

  }




  /**
   * @see coyote.loader.thread.ThreadJob#initialize()
   */
  @Override
  public void initialize() {
    String clientId = "GetIdFromConfig";
    String brokerUri = "GetUriFromConfig";

    MqttClient client = null;

    Context context = loader.getContext();
    Object sharedClient = context.get( "MQTT" );
    if ( sharedClient == null ) {
      try {
        client = new MqttClientImpl( brokerUri, clientId );
        context.set( "MQTT", client );

        // TODO set other things according to the configuration

        MqttToken connectToken = client.connect();
        connectToken.waitForCompletion( 10000 );
        if ( !client.isConnected() ) {
          Log.error( "Timed-out waiting for client to connect" );
          context.set( "MQTT", null );
          shutdown();
        } else {
          // let any components know that they should re-check the context.
          loader.getContext().notifyAll();
        }

      } catch ( MqttException e ) {
        Log.error( "Problems waiting for MQTT client to connect: " + e.getMessage() );
        context.set( "MQTT", null );
        shutdown();
      }

    } else {
      if ( sharedClient instanceof MqttClient ) {
        client = (MqttClient)sharedClient;
      } else {
        Log.error( "Shared client in context is not a MqttClient" );
        shutdown();
      }
    }

    if ( !client.isConnected() ) {
      long timeout = System.currentTimeMillis() + 10000;
      while ( !client.isConnected() ) {
        try {
          Thread.sleep( 100 );
        } catch ( InterruptedException ignore ) {}
        if ( timeout <= System.currentTimeMillis() )
          ;
        break;
      }
      if ( !client.isConnected() ) {
        Log.error( "Timed-out waiting for client to connect" );
        shutdown();
      }

    }

  }




  /**
   * @see coyote.loader.thread.ThreadJob#doWork()
   */
  @Override
  public void doWork() {

  }




  /**
   * @see coyote.loader.thread.ThreadJob#terminate()
   */
  @Override
  public void terminate() {

  }

}
