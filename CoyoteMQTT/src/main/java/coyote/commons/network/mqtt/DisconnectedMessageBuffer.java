package coyote.commons.network.mqtt;

import java.util.ArrayList;

import coyote.commons.network.mqtt.cache.CachedMessage;
import coyote.commons.network.mqtt.protocol.AbstractMessage;
import coyote.loader.log.Log;
import coyote.loader.log.LogMsg;


public class DisconnectedMessageBuffer implements Runnable {

  private final DisconnectedBufferOptions bufferOpts;
  private final ArrayList buffer;
  private final Object bufLock = new Object(); // Used to synchronize the buffer
  private DisconnectedBufferCallback callback;




  public DisconnectedMessageBuffer( final DisconnectedBufferOptions options ) {
    bufferOpts = options;
    buffer = new ArrayList();
  }




  /**
   * Removes a message from the buffer
   * 
   * @param messageIndex
   */
  public void deleteMessage( final int messageIndex ) {
    synchronized( bufLock ) {
      buffer.remove( messageIndex );
    }
  }




  /**
   * Retrieves a message from the buffer at the given index.
   * 
   * @param messageIndex
   * 
   * @return the message at the given index
   */
  public CachedMessage getMessage( final int messageIndex ) {
    synchronized( bufLock ) {
      return ( (CachedMessage)buffer.get( messageIndex ) );
    }
  }




  /**
   * Returns the number of messages currently in the buffer
   * 
   * @return  the number of messages currently in the buffer
   */
  public int getMessageCount() {
    synchronized( bufLock ) {
      return buffer.size();
    }
  }




  /**
   * This will add a new message to the offline buffer.
   * 
   * <p>if the buffer is full and deleteOldestMessages is enabled then the 0th 
   * item in the buffer will be deleted and the new message will be added. If 
   * it is not enabled then an MqttException will be thrown.</p>
   * 
   * @param message the message to place in the buffer
   * 
   * @throws MqttException if the buffer is full and the message cannot be added
   */
  public void putMessage( final AbstractMessage message, final MqttTokenImpl token ) throws MqttException {
    final CachedMessage bufferedMessage = new CachedMessage( message, token );
    synchronized( bufLock ) {
      if ( buffer.size() < bufferOpts.getBufferSize() ) {
        buffer.add( bufferedMessage );
      } else if ( bufferOpts.isDeleteOldestMessages() == true ) {
        buffer.remove( 0 );
        buffer.add( bufferedMessage );
      } else {
        throw new MqttException( MqttException.DISCONNECTED_BUFFER_FULL );
      }
    }
  }




  /**
   * Flushes the buffer of messages into an open connection
   */
  @Override
  public void run() {
    Log.append( MQTT.EVENT, LogMsg.createMsg( MQTT.MSG, "client.restoring_all_cached_messages" ) );
    while ( getMessageCount() > 0 ) {
      try {
        final CachedMessage bufferedMessage = getMessage( 0 );
        callback.publishBufferedMessage( bufferedMessage );
        // Publish was successful, remove message from buffer.
        deleteMessage( 0 );
      } catch ( final MqttException ex ) {
        // Error occurred attempting to publish buffered message likely because the client is not connected
        Log.append( MQTT.EVENT, LogMsg.createMsg( MQTT.MSG, "client.disconnect_on_buffer_publish" ) );
        break;
      }
    }
  }




  public void setPublishCallback( final DisconnectedBufferCallback callback ) {
    this.callback = callback;
  }

}
