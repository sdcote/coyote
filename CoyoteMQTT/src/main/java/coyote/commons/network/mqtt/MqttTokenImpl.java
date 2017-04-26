package coyote.commons.network.mqtt;

import coyote.commons.network.mqtt.protocol.AbstractAckMessage;
import coyote.commons.network.mqtt.protocol.AbstractMessage;
import coyote.commons.network.mqtt.protocol.ConnAckMessage;
import coyote.commons.network.mqtt.protocol.SubackMessage;
import coyote.loader.log.Log;
import coyote.loader.log.LogMsg;


/**
 * Provides a mechanism for tracking the completion of an asynchronous action.
 *  
 * <p>A token that implements the MqttToken interface is returned from all 
 * non-blocking method with the exception of publish.</p>
 *  
 * @see MqttToken
 */
public class MqttTokenImpl implements MqttToken {

  private volatile boolean completed = false;
  private boolean pendingComplete = false;
  private boolean sent = false;
  private final Object responseLock = new Object();
  private final Object sentLock = new Object();
  protected MqttMessage message = null;
  private AbstractMessage response = null;
  private MqttException exception = null;
  private String[] topics = null;
  private String key;
  private MqttClient client = null;
  private AsyncActionListener callback = null;
  private Object userContext = null;
  private int messageID = 0;
  private boolean notified = false;




  public MqttTokenImpl() {}




  public boolean checkResult() throws MqttException {
    if ( getException() != null ) {
      throw getException();
    }
    return true;
  }




  /**
   * @see coyote.commons.network.mqtt.MqttToken#getActionCallback()
   */
  @Override
  public AsyncActionListener getActionCallback() {
    return callback;
  }




  /**
   * @see coyote.commons.network.mqtt.MqttToken#getClient()
   */
  @Override
  public MqttClient getClient() {
    return client;
  }




  /**
   * @see coyote.commons.network.mqtt.MqttToken#getException()
   */
  @Override
  public MqttException getException() {
    return exception;
  }




  /**
   * @see coyote.commons.network.mqtt.MqttToken#getGrantedQos()
   */
  @Override
  public int[] getGrantedQos() {
    int[] val = new int[0];
    if ( response instanceof SubackMessage ) {
      val = ( (SubackMessage)response ).getGrantedQos();
    }
    return val;
  }




  public String getKey() {
    return key;
  }




  public MqttMessage getMessage() {
    return message;
  }




  /**
   * @see coyote.commons.network.mqtt.MqttToken#getMessageId()
   */
  @Override
  public int getMessageId() {
    return messageID;
  }




  public AbstractMessage getResponse() {
    return response;
  }




  public boolean getSessionPresent() {
    boolean val = false;
    if ( response instanceof ConnAckMessage ) {
      val = ( (ConnAckMessage)response ).getSessionPresent();
    }
    return val;
  }




  public String[] getTopics() {
    return topics;
  }




  public Object getUserContext() {
    return userContext;
  }




  public AbstractMessage getWireMessage() {
    return response;
  }




  public boolean isComplete() {
    return completed;
  }




  protected boolean isCompletePending() {
    return pendingComplete;
  }




  protected boolean isInUse() {
    return ( ( getClient() != null ) && !isComplete() );
  }




  public boolean isNotified() {
    return notified;
  }




  /**
   * @see coyote.commons.network.mqtt.MqttToken#markComplete(coyote.commons.network.mqtt.protocol.AbstractMessage, coyote.commons.network.mqtt.MqttException)
   */
  @Override
  public void markComplete( final AbstractMessage msg, final MqttException ex ) {
    Log.append( MQTT.EVENT, LogMsg.createMsg( MQTT.MSG, "token.mark_complete", getKey(), msg, ex ) );

    synchronized( responseLock ) {
      // ACK means that everything was OK, so mark the message for garbage collection.
      if ( msg instanceof AbstractAckMessage ) {
        message = null;
      }
      pendingComplete = true;
      response = msg;
      exception = ex;
    }
  }




  /**
   * @see coyote.commons.network.mqtt.MqttToken#notifyComplete()
   */
  @Override
  public void notifyComplete() {
    Log.append( MQTT.EVENT, LogMsg.createMsg( MQTT.MSG, "token.notify_complete", getKey(), response, exception ) );

    synchronized( responseLock ) {
      // If pending complete is set then normally the token can be marked as 
      // complete and users notified. An abnormal error may have caused the 
      // client to shutdown between pending complete being set and notifying 
      // the user.  In this case - the action must be failed.
      if ( ( exception == null ) && pendingComplete ) {
        completed = true;
        pendingComplete = false;
      } else {
        pendingComplete = false;
      }

      responseLock.notifyAll();
    }
    synchronized( sentLock ) {
      sent = true;
      sentLock.notifyAll();
    }
  }




  /**
   * Notifies this token that the associated message has been sent
   * (i.e. written to the TCP/IP socket).
   */
  protected void notifySent() {
    Log.append( MQTT.EVENT, LogMsg.createMsg( MQTT.MSG, "token.notify_sent", getKey() ) );
    synchronized( responseLock ) {
      response = null;
      completed = false;
    }
    synchronized( sentLock ) {
      sent = true;
      sentLock.notifyAll();
    }
  }




  public void reset() throws MqttException {
    if ( isInUse() ) {
      // Token is already in use - cannot reset 
      throw new MqttException( MqttException.TOKEN_INUSE );
    }
    Log.append( MQTT.EVENT, LogMsg.createMsg( MQTT.MSG, "token.token_reset", getKey() ) );

    client = null;
    completed = false;
    response = null;
    sent = false;
    exception = null;
    userContext = null;
  }




  /**
   * @see coyote.commons.network.mqtt.MqttToken#setActionCallback(coyote.commons.network.mqtt.AsyncActionListener)
   */
  @Override
  public void setActionCallback( final AsyncActionListener listener ) {
    callback = listener;

  }




  /**
   * @see coyote.commons.network.mqtt.MqttToken#setClient(coyote.commons.network.mqtt.MqttClient)
   */
  @Override
  public void setClient( final MqttClient client ) {
    this.client = client;
  }




  public void setException( final MqttException exception ) {
    synchronized( responseLock ) {
      this.exception = exception;
    }
  }




  public void setKey( final String key ) {
    this.key = key;
  }




  public void setMessage( final MqttMessage msg ) {
    message = msg;
  }




  public void setMessageID( final int messageID ) {
    this.messageID = messageID;
  }




  public void setNotified( final boolean notified ) {
    this.notified = notified;
  }




  public void setTopics( final String[] topics ) {
    this.topics = topics;
  }




  /**
   * @see coyote.commons.network.mqtt.MqttToken#setUserContext(java.lang.Object)
   */
  @Override
  public void setUserContext( final Object userContext ) {
    this.userContext = userContext;
  }




  /**
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    final StringBuffer tok = new StringBuffer();
    tok.append( "key=" ).append( getKey() );
    tok.append( " ,topics=" );
    if ( getTopics() != null ) {
      for ( int i = 0; i < getTopics().length; i++ ) {
        tok.append( getTopics()[i] ).append( ", " );
      }
    }
    tok.append( " ,usercontext=" ).append( getUserContext() );
    tok.append( " ,isComplete=" ).append( isComplete() );
    tok.append( " ,isNotified=" ).append( isNotified() );
    tok.append( " ,exception=" ).append( getException() );
    tok.append( " ,actioncallback=" ).append( getActionCallback() );

    return tok.toString();
  }




  /**
   * @see coyote.commons.network.mqtt.MqttToken#waitForCompletion()
   */
  @Override
  public void waitForCompletion() throws MqttException {
    waitForCompletion( -1 );
  }




  /**
   * @see coyote.commons.network.mqtt.MqttToken#waitForCompletion(long)
   */
  @Override
  public void waitForCompletion( final long timeout ) throws MqttException {
    Log.append( MQTT.EVENT, LogMsg.createMsg( MQTT.MSG, "token.wait_for_completion", getKey(), timeout, this ) );

    final AbstractMessage resp = waitForResponse( timeout );
    if ( ( resp == null ) && !completed ) {
      Log.append( MQTT.EVENT, LogMsg.createMsg( MQTT.MSG, "token.wait_for_completion_timeout", getKey(), this ) );
      exception = new MqttException( MqttException.CLIENT_TIMEOUT );
      throw exception;
    }
    checkResult();
  }




  /**
   * Waits for the message delivery to complete, but doesn't throw an exception
   * in the case of a NACK.
   * 
   * <p>It does still throw an exception if something else goes wrong (e.g. an 
   * IOException). This is used for packets like CONNECT, which have useful 
   * information in the ACK that needs to be accessed.</p>
   */
  protected AbstractMessage waitForResponse() throws MqttException {
    return waitForResponse( -1 );
  }




  protected AbstractMessage waitForResponse( final long timeout ) throws MqttException {
    synchronized( responseLock ) {
      Log.append( MQTT.EVENT, LogMsg.createMsg( MQTT.MSG, "token.wait_for_response", getKey(), timeout, sent, completed, ( exception == null ) ? "false" : "true", response, this, exception ) );

      while ( !completed ) {
        if ( exception == null ) {
          try {
            Log.append( MQTT.EVENT, LogMsg.createMsg( MQTT.MSG, "token.wait_for_response_start", getKey(), timeout ) );

            if ( timeout <= 0 ) {
              responseLock.wait();
            } else {
              responseLock.wait( timeout );
            }
          } catch ( final InterruptedException e ) {
            exception = new MqttException( e );
          }
        }
        if ( !completed ) {
          if ( exception != null ) {
            Log.append( MQTT.EVENT, LogMsg.createMsg( MQTT.MSG, "token.wait_for_response_failed", getKey(), exception ) );
            throw exception;
          }

          if ( timeout > 0 ) {
            // time up and still not completed
            break;
          }
        }
      }
    }
    Log.append( MQTT.EVENT, LogMsg.createMsg( MQTT.MSG, "token.wait_for_response_done", getKey(), response ) );
    return response;
  }




  public void waitUntilSent() throws MqttException {
    synchronized( sentLock ) {
      synchronized( responseLock ) {
        if ( exception != null ) {
          throw exception;
        }
      }
      while ( !sent ) {
        try {
          Log.append( MQTT.EVENT, LogMsg.createMsg( MQTT.MSG, "token.wait_until_sent", getKey() ) );

          sentLock.wait();
        } catch ( final InterruptedException e ) {}
      }

      while ( !sent ) {
        if ( exception == null ) {
          throw MQTT.createMqttException( MqttException.UNEXPECTED_ERROR );
        }
        throw exception;
      }
    }
  }

}
