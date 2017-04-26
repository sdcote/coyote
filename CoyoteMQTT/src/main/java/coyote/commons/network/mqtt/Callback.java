package coyote.commons.network.mqtt;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import coyote.commons.network.mqtt.protocol.PubAckMessage;
import coyote.commons.network.mqtt.protocol.PubCompMessage;
import coyote.commons.network.mqtt.protocol.PublishMessage;
import coyote.loader.log.Log;
import coyote.loader.log.LogMsg;


/**
 * Bridge between Receiver and the external API. This class gets called by
 * Receiver, and then converts the network-centric MQTT message objects into ones
 * understood by the external API.
 */
public class Callback implements Runnable {
  private static final int INBOUND_QUEUE_SIZE = 10;
  private ClientListener mqttCallback;
  private ClientListener reconnectCallback;
  private final Hashtable callbacks;
  private final Connection connection;
  private final Vector messageQueue;
  private final Vector completeQueue;
  public boolean running = false;
  private boolean quiescing = false;
  private final Object lifecycle = new Object();
  private Thread callbackThread;
  private final Object workAvailable = new Object();
  private final Object spaceAvailable = new Object();
  private ClientState clientState;
  private boolean manualAcks = false;




  Callback( final Connection conn ) {
    connection = conn;
    messageQueue = new Vector( INBOUND_QUEUE_SIZE );
    completeQueue = new Vector( INBOUND_QUEUE_SIZE );
    callbacks = new Hashtable();
  }




  public void asyncOperationComplete( final MqttTokenImpl token ) {

    if ( running ) {
      // invoke callbacks on callback thread
      completeQueue.addElement( token );
      synchronized( workAvailable ) {
        Log.append( MQTT.EVENT, LogMsg.createMsg( MQTT.MSG, "callback.operation_complete", token.getKey() ) );
        workAvailable.notifyAll();
      }
    } else {
      // invoke async callback on invokers thread
      try {
        handleActionComplete( token );
      } catch ( final Throwable ex ) {
        // Users code could throw an Error or Exception e.g. in the case
        // of class NoClassDefFoundError
        Log.append( MQTT.EVENT, LogMsg.createMsg( MQTT.MSG, "719", ex ) );

        // Shutdown likely already in progress but no harm to confirm
        connection.shutdownConnection( null, new MqttException( ex ) );
      }

    }
  }




  /**
   * This method is called when the connection to the server is lost. 
   * 
   * <p>If there is no cause then it was a clean disconnect. The connectionLost 
   * callback will be invoked if registered and run on the thread that 
   * requested shutdown e.g. receiver or sender thread. If the request was a 
   * user initiated disconnect then the disconnect token will be notified.</p>
   * 
   * @param cause  the reason behind the loss of connection.
   */
  public void connectionLost( final MqttException cause ) {
    // If there was a problem and a client callback has been set inform
    // the connection lost listener of the problem.
    try {
      if ( ( mqttCallback != null ) && ( cause != null ) ) {
        Log.append( MQTT.EVENT, LogMsg.createMsg( MQTT.MSG, "callback.connection_lost_cause", cause ) );
        mqttCallback.connectionLost( cause );
      }

      // If we have a listener responsible for attempting reconnection, notify 
      // it so it can start the reconnection cycle
      if ( ( reconnectCallback != null ) && ( cause != null ) ) {
        reconnectCallback.connectionLost( cause );
      }

    } catch ( final java.lang.Throwable t ) {
      // Just log the fact that a throwable has caught connection lost 
      // is called during shutdown processing so no need to do anything else
      Log.append( MQTT.EVENT, LogMsg.createMsg( MQTT.MSG, "callback.connection_lost_callback_exception", t ) );
    }
  }




  protected boolean deliverMessage( final String topicName, final int messageId, final MqttMessage aMessage ) throws Exception {
    boolean delivered = false;

    final Enumeration keys = callbacks.keys();
    while ( keys.hasMoreElements() ) {
      final String topicFilter = (String)keys.nextElement();
      if ( Topic.isMatched( topicFilter, topicName ) ) {
        aMessage.setId( messageId );
        ( (MessageListener)( callbacks.get( topicFilter ) ) ).messageArrived( topicName, aMessage );
        delivered = true;
      }
    }

    /* if the message hasn't been delivered to a per subscription handler, give it to the default handler */
    if ( ( mqttCallback != null ) && !delivered ) {
      aMessage.setId( messageId );
      mqttCallback.messageArrived( topicName, aMessage );
      delivered = true;
    }

    return delivered;
  }




  /**
   * An action has completed - if a completion listener has been set on the
   * token then invoke it with the outcome of the action.
   * 
   * @param token
   */
  public void fireActionEvent( final MqttTokenImpl token ) {

    if ( token != null ) {
      final AsyncActionListener asyncCB = token.getActionCallback();
      if ( asyncCB != null ) {
        if ( token.getException() == null ) {
          Log.append( MQTT.EVENT, LogMsg.createMsg( MQTT.MSG, "callback.on_success", token.getKey() ) );
          asyncCB.onSuccess( token );
        } else {
          Log.append( MQTT.EVENT, LogMsg.createMsg( MQTT.MSG, "callback.on_failure", token.getKey() ) );
          asyncCB.onFailure( token, token.getException() );
        }
      }
    }
  }




  /**
   * Returns the thread used by this callback.
   */
  protected Thread getThread() {
    return callbackThread;
  }




  private void handleActionComplete( final MqttTokenImpl token ) throws MqttException {
    synchronized( token ) {
      Log.append( MQTT.EVENT, LogMsg.createMsg( MQTT.MSG, "callback.handle_action_completed", token.getKey() ) );
      if ( token.isComplete() ) {
        // Finish by doing any post processing such as delete 
        // from persistent store but only do so if the action
        // is complete
        clientState.notifyComplete( token );
      }

      // Unblock any waiters and if pending complete now set completed
      token.notifyComplete();

      if ( !token.isNotified() ) {
        // If a callback is registered and delivery has finished 
        // call delivery complete callback. 
        if ( ( mqttCallback != null ) && ( token instanceof MqttDeliveryToken ) && token.isComplete() ) {
          mqttCallback.deliveryComplete( (MqttDeliveryToken)token );
        }
        // Now call async action completion callbacks
        fireActionEvent( token );
      }

      // Set notified so we don't tell the user again about this action.
      if ( token.isComplete() ) {
        if ( ( token instanceof MqttDeliveryTokenImpl ) || ( token.getActionCallback() instanceof AsyncActionListener ) ) {
          token.setNotified( true );
        }
      }

    }
  }




  private void handleMessage( final PublishMessage publishMessage ) throws MqttException, Exception {
    // If quisecing process any pending messages.

    final String destName = publishMessage.getTopicName();

    Log.append( MQTT.EVENT, LogMsg.createMsg( MQTT.MSG, "callback.handle_message", publishMessage.getMessageId(), destName ) );
    deliverMessage( destName, publishMessage.getMessageId(), publishMessage.getMessage() );

    if ( !manualAcks ) {
      if ( publishMessage.getMessage().getQos() == 1 ) {
        connection.internalSend( new PubAckMessage( publishMessage ), new MqttTokenImpl() );
      } else if ( publishMessage.getMessage().getQos() == 2 ) {
        connection.deliveryComplete( publishMessage );
        final PubCompMessage pubComp = new PubCompMessage( publishMessage );
        connection.internalSend( pubComp, new MqttTokenImpl() );
      }
    }
  }




  public boolean isQuiesced() {
    if ( quiescing && ( completeQueue.size() == 0 ) && ( messageQueue.size() == 0 ) ) {
      return true;
    }
    return false;
  }




  /**
   * This method is called when a message arrives on a topic. 
   * 
   * <p>Messages are only added to the queue for inbound messages if the client 
   * is not quiescing.</p>
   * 
   * @param sendMessage the MQTT PUBLISH message.
   */
  public void messageArrived( final PublishMessage sendMessage ) {
    if ( ( mqttCallback != null ) || ( callbacks.size() > 0 ) ) {
      // If we already have enough messages queued up in memory, wait
      // until some more queue space becomes available. This helps 
      // the client protect itself from getting flooded by messages 
      // from the server.
      synchronized( spaceAvailable ) {
        while ( running && !quiescing && ( messageQueue.size() >= INBOUND_QUEUE_SIZE ) ) {
          try {
            Log.append( MQTT.EVENT, LogMsg.createMsg( MQTT.MSG, "callback.message_arrived_waitfor_space", messageQueue.size(), INBOUND_QUEUE_SIZE ) );
            spaceAvailable.wait( 500 );
          } catch ( final InterruptedException ex ) {}
        }
      }
      if ( !quiescing ) {
        messageQueue.addElement( sendMessage );
        // Notify the callback thread that there's work to do...
        synchronized( workAvailable ) {
          Log.append( MQTT.EVENT, LogMsg.createMsg( MQTT.MSG, "callback.message_arrived_notify" ) );
          workAvailable.notifyAll();
        }
      }
    }
  }




  public void messageArrivedComplete( final int messageId, final int qos ) throws MqttException {
    if ( qos == 1 ) {
      connection.internalSend( new PubAckMessage( messageId ), new MqttTokenImpl() );
    } else if ( qos == 2 ) {
      connection.deliveryComplete( messageId );
      final PubCompMessage pubComp = new PubCompMessage( messageId );
      connection.internalSend( pubComp, new MqttTokenImpl() );
    }
  }




  /**
   * Let the call back thread quiesce. 
   * 
   * <p>Prevent new inbound messages being added to the process queue and let 
   * existing work quiesce. (until the thread is told to shutdown).</p>
   */
  public void quiesce() {
    quiescing = true;
    synchronized( spaceAvailable ) {
      Log.append( MQTT.EVENT, LogMsg.createMsg( MQTT.MSG, "callback.quiesce" ) );
      // Unblock anything waiting for space...
      spaceAvailable.notifyAll();
    }
  }




  public void removeMessageListener( final String topicFilter ) {
    callbacks.remove( topicFilter ); // no exception thrown if the filter was not present
  }




  public void removeMessageListeners() {
    callbacks.clear();
  }




  @Override
  public void run() {
    while ( running ) {
      try {
        // If no work is currently available, then wait until there is some...
        try {
          synchronized( workAvailable ) {
            if ( running && messageQueue.isEmpty() && completeQueue.isEmpty() ) {
              Log.append( MQTT.EVENT, LogMsg.createMsg( MQTT.MSG, "callback.work_wait" ) );
              workAvailable.wait();
            }
          }
        } catch ( final InterruptedException e ) {}

        if ( running ) {
          // Check for deliveryComplete callbacks...
          MqttTokenImpl token = null;
          synchronized( completeQueue ) {
            if ( !completeQueue.isEmpty() ) {
              // First call the delivery arrived callback if needed
              token = (MqttTokenImpl)completeQueue.elementAt( 0 );
              completeQueue.removeElementAt( 0 );
            }
          }
          if ( null != token ) {
            handleActionComplete( token );
          }

          // Check for messageArrived callbacks...
          PublishMessage message = null;
          synchronized( messageQueue ) {
            if ( !messageQueue.isEmpty() ) {
              // Note, there is a window on connect where a publish
              // could arrive before we've
              // finished the connect logic.
              message = (PublishMessage)messageQueue.elementAt( 0 );

              messageQueue.removeElementAt( 0 );
            }
          }
          if ( null != message ) {
            handleMessage( message );
          }
        }

        if ( quiescing ) {
          clientState.checkQuiesceLock();
        }

      } catch ( final Throwable ex ) {
        // Users code could throw an Error or Exception e.g. in the case
        // of class NoClassDefFoundError
        Log.append( MQTT.EVENT, LogMsg.createMsg( MQTT.MSG, "callback.run_exception", ex ) );
        running = false;
        connection.shutdownConnection( null, new MqttException( ex ) );

      }
      finally {
        synchronized( spaceAvailable ) {
          // Notify the spaceAvailable lock, to say that there's now
          // some space on the queue...
          Log.append( MQTT.EVENT, LogMsg.createMsg( MQTT.MSG, "callback.run_notify_spaceavailable" ) );
          spaceAvailable.notifyAll();
        }
      }
    }
  }




  public void setCallback( final ClientListener mqttCallback ) {
    this.mqttCallback = mqttCallback;
  }




  public void setClientState( final ClientState clientState ) {
    this.clientState = clientState;
  }




  public void setManualAcks( final boolean manualAcks ) {
    this.manualAcks = manualAcks;
  }




  public void setMessageListener( final String topicFilter, final MessageListener messageListener ) {
    callbacks.put( topicFilter, messageListener );
  }




  /**
   * Set the listener to be notified when a connection is lost so it can begin 
   * our reconnect cycle.
   * 
   * @param callback the component responsible for reconnection when the
   *        connection is lost.
   */
  public void setReconnectCallback( final ClientListener callback ) {
    reconnectCallback = callback;
  }




  /**
   * Starts up the Callback thread.
   */
  public void start( final String threadName ) {
    synchronized( lifecycle ) {
      if ( !running ) {
        // Preparatory work before starting the background thread.
        // For safety ensure any old events are cleared.
        messageQueue.clear();
        completeQueue.clear();

        running = true;
        quiescing = false;
        callbackThread = new Thread( this, threadName );
        callbackThread.start();
      }
    }
  }




  /**
   * Stops the callback thread.
   *  
   * This call will block until stop has completed.
   */
  public void stop() {
    synchronized( lifecycle ) {
      if ( running ) {
        Log.append( MQTT.EVENT, LogMsg.createMsg( MQTT.MSG, "callback.stop", Thread.currentThread().getName() ) );
        running = false;
        if ( !Thread.currentThread().equals( callbackThread ) ) {
          try {
            synchronized( workAvailable ) {
              // to finish
              Log.append( MQTT.EVENT, LogMsg.createMsg( MQTT.MSG, "callback.stop_notify" ) );
              workAvailable.notifyAll();
            }
            // Wait for the thread to finish.
            callbackThread.join();
          } catch ( final InterruptedException ex ) {}
        }
      }
      callbackThread = null;
      Log.append( MQTT.EVENT, LogMsg.createMsg( MQTT.MSG, "callback.stop_complete" ) );
    }
  }
}