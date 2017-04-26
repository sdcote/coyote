package coyote.commons.network.mqtt;

import java.io.IOException;
import java.io.InputStream;

import coyote.commons.network.mqtt.protocol.AbstractAckMessage;
import coyote.commons.network.mqtt.protocol.AbstractMessage;
import coyote.commons.network.mqtt.protocol.MessageInputStream;
import coyote.loader.log.Log;
import coyote.loader.log.LogMsg;


/**
 * Receives MQTT packets from the server.
 */
public class Receiver implements Runnable {
  private boolean running = false;
  private final Object lifecycle = new Object();
  private ClientState clientState = null;
  private Connection connection = null;
  private final MessageInputStream in;
  private TokenStore tokenStore = null;
  private Thread recThread = null;
  private volatile boolean receiving;




  public Receiver( final Connection conn, final ClientState clientState, final TokenStore tokenStore, final InputStream in ) {
    this.in = new MessageInputStream( clientState, in );
    connection = conn;
    this.clientState = clientState;
    this.tokenStore = tokenStore;
  }




  /**
   * Returns the receiving state.
   * 
   * @return true if the receiver is receiving data, false otherwise.
   */
  public boolean isReceiving() {
    return receiving;
  }




  public boolean isRunning() {
    return running;
  }




  /**
   * Run loop to receive messages from the server.
   */
  @Override
  public void run() {
    MqttTokenImpl token = null;

    while ( running && ( in != null ) ) {
      try {
        Log.append( MQTT.EVENT, LogMsg.createMsg( MQTT.MSG, "receiver.run" ) );
        receiving = in.available() > 0;
        final AbstractMessage message = in.readMessage();
        receiving = false;

        if ( message instanceof AbstractAckMessage ) {
          token = tokenStore.getToken( message );
          if ( token != null ) {
            synchronized( token ) {
              // Ensure the notify processing is done under a lock on the token
              // This ensures that the send processing can complete  before the 
              // receive processing starts! Request and ack and ack processing 
              // can occur before request processing is complete if not!
              clientState.notifyReceivedAck( (AbstractAckMessage)message );
            }
          } else {
            // It its an ack and there is no token then something is not right.
            // An ack should always have a token assoicated with it.
            throw new MqttException( MqttException.UNEXPECTED_ERROR );
          }
        } else {
          // A new message has arrived
          clientState.notifyReceivedMsg( message );
        }
      } catch ( final MqttException ex ) {
        Log.append( MQTT.EVENT, LogMsg.createMsg( MQTT.MSG, "receiver.run_stop", ex ) );
        running = false;
        // Token maybe null but that is handled in shutdown
        connection.shutdownConnection( token, ex );
      } catch ( final IOException ioe ) {
        Log.append( MQTT.EVENT, LogMsg.createMsg( MQTT.MSG, "receiver.run_stop_io" ) );

        running = false;
        // An EOFException could be raised if the broker processes the 
        // DISCONNECT and ends the socket before we complete. As such,
        // only shutdown the connection if we're not already shutting down.
        if ( !connection.isDisconnecting() ) {
          connection.shutdownConnection( token, new MqttException( MqttException.CONNECTION_LOST, ioe ) );
        }
      }
      finally {
        receiving = false;
      }
    }

    Log.append( MQTT.EVENT, LogMsg.createMsg( MQTT.MSG, "receiver.run_return" ) );
  }




  /**
   * Starts up the Receiver's thread.
   */
  public void start( final String threadName ) {
    Log.append( MQTT.EVENT, LogMsg.createMsg( MQTT.MSG, "receiver.start" ) );
    synchronized( lifecycle ) {
      if ( !running ) {
        running = true;
        recThread = new Thread( this, threadName );
        recThread.start();
      }
    }
  }




  /**
   * Stops the Receiver's thread.  This call will block.
   */
  public void stop() {
    synchronized( lifecycle ) {
      Log.append( MQTT.EVENT, LogMsg.createMsg( MQTT.MSG, "receiver.stop", Thread.currentThread().getName() ) );
      if ( running ) {
        running = false;
        receiving = false;
        if ( !Thread.currentThread().equals( recThread ) ) {
          try {
            // Wait for the thread to finish.
            recThread.join();
          } catch ( final InterruptedException ex ) {}
        }
      }
    }
    recThread = null;
    Log.append( MQTT.EVENT, LogMsg.createMsg( MQTT.MSG, "receiver.stopped" ) );
  }
}
