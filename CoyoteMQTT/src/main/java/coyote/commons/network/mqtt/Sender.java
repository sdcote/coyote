package coyote.commons.network.mqtt;

import java.io.IOException;
import java.io.OutputStream;

import coyote.commons.network.mqtt.protocol.AbstractAckMessage;
import coyote.commons.network.mqtt.protocol.AbstractMessage;
import coyote.commons.network.mqtt.protocol.DisconnectMessage;
import coyote.commons.network.mqtt.protocol.MessageOutputStream;
import coyote.loader.log.Log;
import coyote.loader.log.LogMsg;


/**
 * Sends MQTT packets to the server on its own thread
 */
public class Sender implements Runnable {
  private boolean running = false;
  private final Object lifecycle = new Object();
  private ClientState clientState = null;
  private final MessageOutputStream out;
  private Connection connection = null;
  private TokenStore tokenStore = null;
  private Thread sendThread = null;




  public Sender( final Connection conn, final ClientState clientState, final TokenStore tokenStore, final OutputStream out ) {
    this.out = new MessageOutputStream( clientState, out );
    connection = conn;
    this.clientState = clientState;
    this.tokenStore = tokenStore;
  }




  private void handleRunException( final AbstractMessage message, final Exception ex ) {
    Log.append( MQTT.EVENT, LogMsg.createMsg( MQTT.MSG, "sender.handling_exception", ex ) );
    MqttException mex;
    if ( !( ex instanceof MqttException ) ) {
      mex = new MqttException( MqttException.CONNECTION_LOST, ex );
    } else {
      mex = (MqttException)ex;
    }

    running = false;
    connection.shutdownConnection( null, mex );
  }




  @Override
  public void run() {
    AbstractMessage message = null;
    while ( running && ( out != null ) ) {
      try {
        message = clientState.get();
        if ( message != null ) {
          Log.append( MQTT.EVENT, LogMsg.createMsg( MQTT.MSG, "sender.sending", message.getKey(), message ) );

          if ( message instanceof AbstractAckMessage ) {
            out.write( message );
            out.flush();
          } else {
            final MqttTokenImpl token = tokenStore.getToken( message );
            // While quiescing the tokenstore can be cleared so need 
            // to check for null for the case where clear occurs
            // while trying to send a message.
            if ( token != null ) {
              synchronized( token ) {
                out.write( message );
                try {
                  out.flush();
                } catch ( final IOException ex ) {
                  // The flush has been seen to fail on disconnect of a SSL socket
                  // as disconnect is in progress this should not be treated as an error
                  if ( !( message instanceof DisconnectMessage ) ) {
                    throw ex;
                  }
                }
                clientState.notifySent( message );
              }
            }
          }
        } else { // null message
          Log.append( MQTT.EVENT, LogMsg.createMsg( MQTT.MSG, "sender.sending_null" ) );
          running = false;
        }
      } catch ( final MqttException me ) {
        handleRunException( message, me );
      } catch ( final Exception ex ) {
        handleRunException( message, ex );
      }
    } // end while

    Log.append( MQTT.EVENT, LogMsg.createMsg( MQTT.MSG, "sender.sending_complete" ) );

  }




  /**
   * Starts up the Sender thread.
   */
  public void start( final String threadName ) {
    synchronized( lifecycle ) {
      if ( !running ) {
        running = true;
        sendThread = new Thread( this, threadName );
        sendThread.start();
      }
    }
  }




  /**
   * Stops the Sender's thread.  This call will block.
   */
  public void stop() {

    synchronized( lifecycle ) {
      Log.append( MQTT.EVENT, LogMsg.createMsg( MQTT.MSG, "sender.stopping", Thread.currentThread().getName() ) );
      if ( running ) {
        running = false;
        if ( !Thread.currentThread().equals( sendThread ) ) {
          try {
            while ( sendThread.isAlive() ) {
              // first notify get routine to finish
              clientState.notifyQueueLock();
              // Wait for the thread to finish.
              sendThread.join( 100 );
            }
          } catch ( final InterruptedException ex ) {}
        }
      }
      sendThread = null;
      Log.append( MQTT.EVENT, LogMsg.createMsg( MQTT.MSG, "sender.stopped" ) );
    }
  }
}
