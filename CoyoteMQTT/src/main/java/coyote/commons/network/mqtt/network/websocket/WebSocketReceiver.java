package coyote.commons.network.mqtt.network.websocket;

import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

import coyote.commons.network.mqtt.MQTT;
import coyote.loader.log.Log;
import coyote.loader.log.LogMsg;


public class WebSocketReceiver implements Runnable {
  private boolean running = false;
  private boolean stopping = false;
  private final Object lifecycle = new Object();
  private final InputStream input;
  private Thread receiverThread = null;
  private volatile boolean receiving;
  private final PipedOutputStream pipedOutputStream;




  public WebSocketReceiver( final InputStream input, final PipedInputStream pipedInputStream ) throws IOException {
    this.input = input;
    pipedOutputStream = new PipedOutputStream();
    pipedInputStream.connect( pipedOutputStream );
  }




  private void closeOutputStream() {
    try {
      pipedOutputStream.close();
    } catch ( final IOException e ) {}
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




  @Override
  public void run() {

    while ( running && ( input != null ) ) {
      try {
        Log.append( MQTT.EVENT, LogMsg.createMsg( MQTT.MSG, "receiver.websocket_run" ) );
        receiving = input.available() > 0;
        final WebSocketFrame incomingFrame = new WebSocketFrame( input );
        if ( !incomingFrame.isCloseFlag() ) {
          for ( int i = 0; i < incomingFrame.getPayload().length; i++ ) {
            pipedOutputStream.write( incomingFrame.getPayload()[i] );
          }

          pipedOutputStream.flush();
        } else {
          if ( !stopping ) {
            throw new IOException( "Server sent a WebSocket Frame with the Stop OpCode" );
          }
        }

        receiving = false;

      } catch ( final IOException ex ) {
        // Exception occurred whilst reading the stream.
        stop();
      }
    }
  }




  /**
   * Starts up the WebSocketReceiver's thread
   */
  public void start( final String threadName ) {
    Log.append( MQTT.EVENT, LogMsg.createMsg( MQTT.MSG, "receiver.websocket_start" ) );
    synchronized( lifecycle ) {
      if ( !running ) {
        running = true;
        receiverThread = new Thread( this, threadName );
        receiverThread.start();
      }
    }
  }




  /**
   * Stops this WebSocketReceiver's thread.
   * This call will block.
   */
  public void stop() {
    stopping = true;
    synchronized( lifecycle ) {
      Log.append( MQTT.EVENT, LogMsg.createMsg( MQTT.MSG, "receiver.websocket_stop", Thread.currentThread().getName() ) );
      if ( running ) {
        running = false;
        receiving = false;
        closeOutputStream();
        if ( !Thread.currentThread().equals( receiverThread ) ) {
          try {
            // Wait for the thread to finish
            receiverThread.join();
          } catch ( final InterruptedException ex ) {
            // Interrupted Exception
          }
        }
      }
    }
    receiverThread = null;
    Log.append( MQTT.EVENT, LogMsg.createMsg( MQTT.MSG, "receiver.websocket_stopped" ) );
  }

}
