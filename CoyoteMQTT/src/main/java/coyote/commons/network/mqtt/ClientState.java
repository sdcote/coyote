package coyote.commons.network.mqtt;

import java.io.EOFException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Vector;

import coyote.commons.network.mqtt.cache.CacheException;
import coyote.commons.network.mqtt.cache.Cacheable;
import coyote.commons.network.mqtt.cache.ClientCache;
import coyote.commons.network.mqtt.protocol.AbstractAckMessage;
import coyote.commons.network.mqtt.protocol.AbstractMessage;
import coyote.commons.network.mqtt.protocol.ConnAckMessage;
import coyote.commons.network.mqtt.protocol.ConnectMessage;
import coyote.commons.network.mqtt.protocol.PingReqMessage;
import coyote.commons.network.mqtt.protocol.PingRespMessage;
import coyote.commons.network.mqtt.protocol.PubAckMessage;
import coyote.commons.network.mqtt.protocol.PubCompMessage;
import coyote.commons.network.mqtt.protocol.PubRecMessage;
import coyote.commons.network.mqtt.protocol.PubRelMessage;
import coyote.commons.network.mqtt.protocol.PublishMessage;
import coyote.loader.log.Log;
import coyote.loader.log.LogMsg;


/**
 * The core of the client, which holds the state information for pending and
 * in-flight messages.
 * 
 * <p>Messages that have been accepted for delivery are moved between several 
 * objects while being delivered.<ol>
 * <li>When the client is not running messages are stored in a persistent 
 * store. The default is a file based cache which stores messages safely across 
 * failures and system restarts. If no persistence is specified there is a fall 
 * back to a memory based cache which will maintain the messages while the MQTT
 * client is instantiated.</li>
 *  
 * <li>When the client or specifically ClientState is instantiated the messages 
 * are read from the persistent store into:<ul>
 * <li>outboundqos2 hashtable if a QoS 2 PUBLISH or PUBREL</li>
 * <li>outboundqos1 hashtable if a QoS 1 PUBLISH</li></ul>
 * (see restoreState)</li>
 *  
 * <li>On Connect, copy messages from the outbound hashtables to the pendingMessages or 
 * pendingFlows vector in messageid order.
 * - Initial message publish goes onto the pendingmessages buffer. 
 * - PUBREL goes onto the pendingflows buffer
 * (see restoreInflightMessages)</li>
 * 
 * <li>Sender thread reads messages from the pendingflows and pendingmessages buffer
 * one at a time.  The message is removed from the pendingbuffer but remains on the 
 * outbound* hashtable.  The hashtable is the place where the full set of outstanding 
 * messages are stored in memory. (Persistence is only used at start up)</li>
 *  
 * <li>Receiver thread - receives wire messages:<ul> 
 *  <li>if QoS 1 then remove from persistence and outboundqos1</li>
 *  <li>if QoS 2 PUBREC send PUBREL. Updating the outboundqos2 entry with the 
 *  PUBREL and update persistence.</li>
 *  <li>if QoS 2 PUBCOMP remove from persistence and outboundqos2</li></ul>  
 * </ol>
 * 
 * <p>Notes:<br>
 * Due to the multi-threaded nature of the client, it is vital that any changes 
 * to this class take concurrency into account. For instance as soon as a flow 
 * / message is put on the wire it is possible for the receiving thread to 
 * receive the ACK and to be processing the response before the sending side 
 * has finished processing. For instance a connect may be sent, the CONNACK 
 * received before the connect notify send has been processed.</p> 
 */
public class ClientState {
  private static final String CACHE_SENT_PREFIX = "s-";
  private static final String CACHE_SENT_BUFFERED_PREFIX = "sb-";
  private static final String CACHE_CONFIRMED_PREFIX = "sc-";
  private static final String CACHE_RECEIVED_PREFIX = "r-";
  private static final int MIN_MSG_ID = 1; // Lowest possible MQTT message ID to use
  private static final int MAX_MSG_ID = 65535; // Highest possible MQTT message ID to use
  private int nextMsgId = MIN_MSG_ID - 1; // The next available message ID to use
  private Hashtable inUseMsgIds; // Used to store a set of in-use message IDs
  volatile private Vector pendingMessages;
  volatile private Vector pendingFlows;
  private TokenStore tokenStore;
  private Connection connection = null;
  private Callback callback = null;
  private long keepAlive;
  private boolean cleanSession;
  private ClientCache cache;
  private int maxInflight = 0;
  private int actualInFlight = 0;
  private int inFlightPubRels = 0;
  private final Object queueLock = new Object();
  private final Object quiesceLock = new Object();
  private boolean quiescing = false;
  private long lastOutboundActivity = 0;
  private long lastInboundActivity = 0;
  private long lastPing = 0;
  private AbstractMessage pingCommand;
  private final Object pingOutstandingLock = new Object();
  private int pingOutstanding = 0;
  private boolean connected = false;
  private Hashtable outboundQoS2 = null;
  private Hashtable outboundQoS1 = null;
  private Hashtable outboundQoS0 = null;
  private Hashtable inboundQoS2 = null;
  private PingSender pingSender = null;




  protected ClientState( final ClientCache cache, final TokenStore tokenStore, final Callback callback, final Connection connection, final PingSender pingSender ) throws MqttException {
    inUseMsgIds = new Hashtable();
    pendingFlows = new Vector();
    outboundQoS2 = new Hashtable();
    outboundQoS1 = new Hashtable();
    outboundQoS0 = new Hashtable();
    inboundQoS2 = new Hashtable();
    pingCommand = new PingReqMessage();
    inFlightPubRels = 0;
    actualInFlight = 0;

    this.cache = cache;
    this.callback = callback;
    this.tokenStore = tokenStore;
    this.connection = connection;
    this.pingSender = pingSender;

    restoreState();
  }




  /**
   * Check and send a ping if needed and check for ping timeout.
   * 
   * <p>Need to send a ping if nothing has been sent or received in the last 
   * keepalive interval. It is important to check for both sent and received 
   * packets in order to catch the case where an app is solely sending QoS 0 
   * messages or receiving QoS 0 messages. QoS 0 message are not good enough 
   * for checking a connection is alive as they are one way messages.</p>
   * 
   * <p>If a ping has been sent but no data has been received in the last 
   * keepalive interval then the connection is deemed to be broken.</p> 
   * 
   * @return token of ping command, null if no ping command has been sent.
   */
  public MqttTokenImpl checkForActivity( final AsyncActionListener pingCallback ) throws MqttException {
    Log.append( MQTT.EVENT, LogMsg.createMsg( MQTT.MSG, "clientstate.check_for_activity" ) );

    synchronized( quiesceLock ) {
      // No ping while quiescing
      if ( quiescing ) {
        return null;
      }
    }

    MqttTokenImpl token = null;
    long nextPingTime = getKeepAlive();

    if ( connected && ( keepAlive > 0 ) ) {
      final long time = System.currentTimeMillis();
      //Reduce schedule frequency since System.currentTimeMillis is no accurate, add a buffer
      //It is 1/10 in minimum keepalive unit.
      final int delta = 100;

      synchronized( pingOutstandingLock ) {

        // Is the broker connection lost because the broker did not reply to my ping?                                                                                                                                 
        if ( ( pingOutstanding > 0 ) && ( ( time - lastInboundActivity ) >= ( keepAlive + delta ) ) ) {
          // lastInboundActivity will be updated once receiving is done.                                                                                                                                        
          // Add a delta, since the timer and System.currentTimeMillis() is not accurate.                                                                                                                        
          // A ping is outstanding but no packet has been received in KA so connection is deemed broken                                                                                                         
          Log.error( LogMsg.createMsg( MQTT.MSG, "clientstate.check_for_activity_timed_out", keepAlive, lastOutboundActivity, lastInboundActivity, time, lastPing ) );

          // A ping has already been sent. At this point, assume that the                                                                                                                                       
          // broker has hung and the TCP layer hasn't noticed.                                                                                                                                                  
          throw MQTT.createMqttException( MqttException.CLIENT_TIMEOUT );
        }

        // Is the broker connection lost because I could not get any successful write for 2 keepAlive intervals?                                                                                                      
        if ( ( pingOutstanding == 0 ) && ( ( time - lastOutboundActivity ) >= ( 2 * keepAlive ) ) ) {

          // I am probably blocked on a write operations as I should have been able to write at least a ping message                                                                                                    
          Log.error( LogMsg.createMsg( MQTT.MSG, "clientstate.check_for_activity_no_write", keepAlive, lastOutboundActivity, lastInboundActivity, time, lastPing ) );

          // A ping has not been sent but I am not progressing on the current write operation. 
          // At this point, assume that the broker has hung and the TCP layer hasn't noticed.                                                                                                                                                  
          throw MQTT.createMqttException( MqttException.WRITE_TIMEOUT );
        }

        // 1. Is a ping required by the client to verify whether the broker is down?                                                                                                                                  
        //    Condition: ((pingOutstanding == 0 && (time - lastInboundActivity >= keepAlive + delta)))                                                                                                                
        //    In this case only one ping is sent. If not confirmed, client will assume a lost connection to the broker.                                                                                               
        // 2. Is a ping required by the broker to keep the client alive?                                                                                                                                              
        //    Condition: (time - lastOutboundActivity >= keepAlive - delta)                                                                                                                                           
        //    In this case more than one ping outstanding may be necessary.                                                                                                                                           
        //    This would be the case when receiving a large message;                                                                                                                                                  
        //    the broker needs to keep receiving a regular ping even if the ping response are queued after the long message                                                                                           
        //    If lacking to do so, the broker will consider my connection lost and cut my socket.                                                                                                                     
        if ( ( ( pingOutstanding == 0 ) && ( ( time - lastInboundActivity ) >= ( keepAlive - delta ) ) ) || ( ( time - lastOutboundActivity ) >= ( keepAlive - delta ) ) ) {

          Log.append( MQTT.EVENT, LogMsg.createMsg( MQTT.MSG, "clientstate.check_for_activity_ping_needed", keepAlive, lastOutboundActivity, lastInboundActivity ) );

          // pingOutstanding++;  // it will be set after the ping has been written on the wire                                                                                                             
          // lastPing = time;    // it will be set after the ping has been written on the wire                                                                                                             
          token = new MqttTokenImpl();
          if ( pingCallback != null ) {
            token.setActionCallback( pingCallback );
          }
          tokenStore.saveToken( token, pingCommand );
          pendingFlows.insertElementAt( pingCommand, 0 );

          nextPingTime = getKeepAlive();

          //Wake sender thread since it may be in wait state (in ClientState.get())                                                                                                                             
          notifyQueueLock();
        } else {
          Log.append( MQTT.EVENT, LogMsg.createMsg( MQTT.MSG, "clientstate.check_for_activity_ping_not_needed" ) );
          nextPingTime = Math.max( 1, getKeepAlive() - ( time - lastOutboundActivity ) );
        }
      }
      Log.append( MQTT.EVENT, LogMsg.createMsg( MQTT.MSG, "clientstate.check_for_activity_ping_scheduling", nextPingTime ) );
      pingSender.schedule( nextPingTime );
    }

    return token;
  }




  protected boolean checkQuiesceLock() {
    //		if (quiescing && actualInFlight == 0 && pendingFlows.size() == 0 && inFlightPubRels == 0 && callback.isQuiesced()) {
    final int tokC = tokenStore.count();
    if ( quiescing && ( tokC == 0 ) && ( pendingFlows.size() == 0 ) && callback.isQuiesced() ) {
      Log.append( MQTT.EVENT, LogMsg.createMsg( MQTT.MSG, "clientstate.check_quiesce_lock", quiescing, actualInFlight, pendingFlows.size(), inFlightPubRels, callback.isQuiesced(), tokC ) );
      synchronized( quiesceLock ) {
        quiesceLock.notifyAll();
      }
      return true;
    }
    return false;
  }




  protected void clearState() throws MqttException {
    Log.append( MQTT.EVENT, LogMsg.createMsg( MQTT.MSG, "clientstate.clearstate" ) );
    cache.clear();
    inUseMsgIds.clear();
    pendingMessages.clear();
    pendingFlows.clear();
    outboundQoS2.clear();
    outboundQoS1.clear();
    outboundQoS0.clear();
    inboundQoS2.clear();
    tokenStore.clear();
  }




  /**
   * Tidy up
   * - ensure that tokens are released as they are maintained over a 
   * disconnect / connect cycle. 
   */
  protected void close() {
    inUseMsgIds.clear();
    pendingMessages.clear();
    pendingFlows.clear();
    outboundQoS2.clear();
    outboundQoS1.clear();
    outboundQoS0.clear();
    inboundQoS2.clear();
    tokenStore.clear();
    inUseMsgIds = null;
    pendingMessages = null;
    pendingFlows = null;
    outboundQoS2 = null;
    outboundQoS1 = null;
    outboundQoS0 = null;
    inboundQoS2 = null;
    tokenStore = null;
    callback = null;
    connection = null;
    cache = null;
    pingCommand = null;
  }




  /**
   * Called when the client has successfully connected to the broker
   */
  public void connected() {
    Log.append( MQTT.EVENT, LogMsg.createMsg( MQTT.MSG, "clientstate.connected" ) );
    connected = true;
    pingSender.start(); //Start ping thread when client connected to server.
  }




  private void decrementInFlight() {
    synchronized( queueLock ) {
      actualInFlight--;
      Log.append( MQTT.EVENT, LogMsg.createMsg( MQTT.MSG, "clientstate.decrement_inflight", actualInFlight ) );

      if ( !checkQuiesceLock() ) {
        queueLock.notifyAll();
      }
    }
  }




  protected void deliveryComplete( final int messageId ) throws CacheException {
    Log.append( MQTT.EVENT, LogMsg.createMsg( MQTT.MSG, "clientstate.delivery_complete", messageId ) );

    cache.remove( getReceivedCacheKey( messageId ) );
    inboundQoS2.remove( new Integer( messageId ) );
  }




  protected void deliveryComplete( final PublishMessage message ) throws CacheException {
    Log.append( MQTT.EVENT, LogMsg.createMsg( MQTT.MSG, "clientstate.delivery_complete", message.getMessageId() ) );

    cache.remove( getReceivedCacheKey( message ) );
    inboundQoS2.remove( new Integer( message.getMessageId() ) );
  }




  /**
   * Called when the client has been disconnected from the broker.
   * 
   * @param reason The root cause of the disconnection, or null if it is a 
   *        clean disconnect
   */
  public void disconnected( final MqttException reason ) {
    Log.append( MQTT.EVENT, LogMsg.createMsg( MQTT.MSG, "clientstate.disconnected", reason ) );

    connected = false;

    try {
      if ( cleanSession ) {
        clearState();
      }

      pendingMessages.clear();
      pendingFlows.clear();
      synchronized( pingOutstandingLock ) {
        // Reset pingOutstanding to allow reconnects to assume no previous ping.
        pingOutstanding = 0;
      }
    } catch ( final MqttException e ) {
      // Ignore as we have disconnected at this point
    }
  }




  /**
   * This returns the next piece of work (i.e. message) for the Sender to send 
   * over the network.
   * 
   * <p>Calls to this method block until either:<ul>
   * <li>there is a message to be sent</li>
   * <li>the keepAlive interval is exceeded, which triggers a ping message to 
   * be returned</li>
   * <li>{@link #disconnected(MqttException)} is called</li></ul>
   * 
   * @return the next message to send, or null if the client is disconnected
   */
  protected AbstractMessage get() throws MqttException {
    AbstractMessage result = null;

    synchronized( queueLock ) {
      while ( result == null ) {

        // If there is no work wait until there is work.
        // If the inflight window is full and no flows are pending wait until space is freed.
        // In both cases queueLock will be notified.
        if ( ( pendingMessages.isEmpty() && pendingFlows.isEmpty() ) || ( pendingFlows.isEmpty() && ( actualInFlight >= maxInflight ) ) ) {
          try {
            Log.append( MQTT.EVENT, LogMsg.createMsg( MQTT.MSG, "clientstate.get_wait_start" ) );
            queueLock.wait();
            Log.append( MQTT.EVENT, LogMsg.createMsg( MQTT.MSG, "clientstate.get_wait_end" ) );
          } catch ( final InterruptedException e ) {}
        }

        // Handle the case where not connected. This should only be the case if: 
        // - in the process of disconnecting / shutting down
        // - in the process of connecting
        if ( !connected && ( pendingFlows.isEmpty() || !( (AbstractMessage)pendingFlows.elementAt( 0 ) instanceof ConnectMessage ) ) ) {
          Log.append( MQTT.EVENT, LogMsg.createMsg( MQTT.MSG, "clientstate.get_no_flows_or_connect" ) );

          return null;
        }

        // Check if there is a need to send a ping to keep the session alive. 
        // Note this check is done before processing messages. If not done first
        // an app that only publishes QoS 0 messages will prevent keepalive processing
        // from functioning. 

        // Now process any queued flows or messages
        if ( !pendingFlows.isEmpty() ) {
          // Process the first "flow" in the queue
          result = (AbstractMessage)pendingFlows.remove( 0 );
          if ( result instanceof PubRelMessage ) {
            inFlightPubRels++;

            Log.append( MQTT.EVENT, LogMsg.createMsg( MQTT.MSG, "clientstate.get_inflight_pubrel_count", inFlightPubRels ) );
          }

          checkQuiesceLock();
        } else if ( !pendingMessages.isEmpty() ) {

          // If the inflight window is full then messages are not 
          // processed until the inflight window has space. 
          if ( actualInFlight < maxInflight ) {
            // The in flight window is not full so process the 
            // first message in the queue
            result = (AbstractMessage)pendingMessages.elementAt( 0 );
            pendingMessages.removeElementAt( 0 );
            actualInFlight++;

            Log.append( MQTT.EVENT, LogMsg.createMsg( MQTT.MSG, "clientstate.get_actual_inflight_count", actualInFlight ) );
          } else {
            Log.append( MQTT.EVENT, LogMsg.createMsg( MQTT.MSG, "clientstate.get_actual_inflight_full" ) );
          }
        }
      }
    }
    return result;
  }




  public int getActualInFlight() {
    return actualInFlight;
  }




  protected boolean getCleanSession() {
    return cleanSession;
  }




  public Properties getDebug() {
    final Properties props = new Properties();
    props.put( "In use msgids", inUseMsgIds );
    props.put( "pendingMessages", pendingMessages );
    props.put( "pendingFlows", pendingFlows );
    props.put( "maxInflight", new Integer( maxInflight ) );
    props.put( "nextMsgID", new Integer( nextMsgId ) );
    props.put( "actualInFlight", new Integer( actualInFlight ) );
    props.put( "inFlightPubRels", new Integer( inFlightPubRels ) );
    props.put( "quiescing", Boolean.valueOf( quiescing ) );
    props.put( "pingoutstanding", new Integer( pingOutstanding ) );
    props.put( "lastOutboundActivity", new Long( lastOutboundActivity ) );
    props.put( "lastInboundActivity", new Long( lastInboundActivity ) );
    props.put( "outboundQoS2", outboundQoS2 );
    props.put( "outboundQoS1", outboundQoS1 );
    props.put( "outboundQoS0", outboundQoS0 );
    props.put( "inboundQoS2", inboundQoS2 );
    props.put( "tokens", tokenStore );
    return props;
  }




  protected long getKeepAlive() {
    return keepAlive;
  }




  public int getMaxInFlight() {
    return maxInflight;
  }




  /**
   * Get the next MQTT message ID that is not already in use, and marks
   * it as now being in use.
   * 
   * @return the next MQTT message ID to use
   */
  private synchronized int getNextMessageId() throws MqttException {
    int startingMessageId = nextMsgId;

    // Allow two complete passes of the message ID range. This gives
    // any asynchronous releases a chance to occur
    int loopCount = 0;
    do {
      nextMsgId++;
      if ( nextMsgId > MAX_MSG_ID ) {
        nextMsgId = MIN_MSG_ID;
      }
      if ( nextMsgId == startingMessageId ) {
        loopCount++;
        if ( loopCount == 2 ) {
          throw MQTT.createMqttException( MqttException.NO_MESSAGE_IDS_AVAILABLE );
        }
      }
    }
    while ( inUseMsgIds.containsKey( new Integer( nextMsgId ) ) );
    final Integer id = new Integer( nextMsgId );
    inUseMsgIds.put( id, id );
    return nextMsgId;
  }




  private String getReceivedCacheKey( final int messageId ) {
    return CACHE_RECEIVED_PREFIX + messageId;
  }




  private String getReceivedCacheKey( final AbstractMessage message ) {
    return CACHE_RECEIVED_PREFIX + message.getMessageId();
  }




  private String getSendBufferedCacheKey( final AbstractMessage message ) {
    return CACHE_SENT_BUFFERED_PREFIX + message.getMessageId();
  }




  private String getSendConfirmCacheKey( final AbstractMessage message ) {
    return CACHE_CONFIRMED_PREFIX + message.getMessageId();
  }




  private String getSendCacheKey( final AbstractMessage message ) {
    return CACHE_SENT_PREFIX + message.getMessageId();
  }




  /**
   * Inserts a new message to the list, ensuring that list is ordered from lowest to highest in terms of the message id's.
   * @param list the list to insert the message into
   * @param newMsg the message to insert into the list
   */
  private void insertInOrder( final Vector list, final AbstractMessage newMsg ) {
    final int newMsgId = newMsg.getMessageId();
    for ( int i = 0; i < list.size(); i++ ) {
      final AbstractMessage otherMsg = (AbstractMessage)list.elementAt( i );
      final int otherMsgId = otherMsg.getMessageId();
      if ( otherMsgId > newMsgId ) {
        list.insertElementAt( newMsg, i );
        return;
      }
    }
    list.addElement( newMsg );
  }




  /**
   * Called when waiters and call-backs have processed the message.
   * 
   * <p>For messages where delivery is complete the message can be removed from
   * the cache and counters adjusted accordingly.</p>
   * 
   * @param token
   * 
   * @throws MqttException
   */
  protected void notifyComplete( final MqttTokenImpl token ) throws MqttException {

    final AbstractMessage message = token.getWireMessage();

    if ( ( message != null ) && ( message instanceof AbstractAckMessage ) ) {

      Log.append( MQTT.EVENT, LogMsg.createMsg( MQTT.MSG, "clientstate.notify_complete", message.getMessageId(), token, message ) );

      final AbstractAckMessage ack = (AbstractAckMessage)message;

      if ( ack instanceof PubAckMessage ) {

        // QoS 1 - user notified now remove from the cache...
        cache.remove( getSendCacheKey( message ) );
        outboundQoS1.remove( new Integer( ack.getMessageId() ) );
        decrementInFlight();
        releaseMessageId( message.getMessageId() );
        tokenStore.removeToken( message );
        Log.append( MQTT.EVENT, LogMsg.createMsg( MQTT.MSG, "clientstate.notify_complete_removed_qos1", ack.getMessageId() ) );
      } else if ( ack instanceof PubCompMessage ) {
        // QoS 2 - user notified now remove from the cache...
        cache.remove( getSendCacheKey( message ) );
        cache.remove( getSendConfirmCacheKey( message ) );
        outboundQoS2.remove( new Integer( ack.getMessageId() ) );

        inFlightPubRels--;
        decrementInFlight();
        releaseMessageId( message.getMessageId() );
        tokenStore.removeToken( message );

        Log.append( MQTT.EVENT, LogMsg.createMsg( MQTT.MSG, "clientstate.notify_complete_removed_qos2", ack.getMessageId(), inFlightPubRels ) );
      }

      checkQuiesceLock();
    }
  }




  public void notifyQueueLock() {
    synchronized( queueLock ) {
      Log.append( MQTT.EVENT, LogMsg.createMsg( MQTT.MSG, "clientstate.notify_queue_lock_holders" ) );
      queueLock.notifyAll();
    }
  }




  /**
  * Called by the Receiver when an ack has arrived. 
  * 
  * @param ack
  * 
  * @throws MqttException
  */
  protected void notifyReceivedAck( final AbstractAckMessage ack ) throws MqttException {
    lastInboundActivity = System.currentTimeMillis();

    Log.append( MQTT.EVENT, LogMsg.createMsg( MQTT.MSG, "clientstate.received_ack", ack.getMessageId(), ack ) );

    final MqttTokenImpl token = tokenStore.getToken( ack );
    MqttException mex = null;

    if ( token == null ) {
      Log.append( MQTT.EVENT, LogMsg.createMsg( MQTT.MSG, "clientstate.received_ack_no_message", ack.getMessageId() ) );
    } else if ( ack instanceof PubRecMessage ) {
      // Complete the QoS 2 flow. Unlike all other flows, QoS is a 2 phase 
      // flow. The second phase sends a PUBREL - the operation is not complete 
      //until a PUBCOMP is received
      final PubRelMessage rel = new PubRelMessage( (PubRecMessage)ack );
      send( rel, token );
    } else if ( ( ack instanceof PubAckMessage ) || ( ack instanceof PubCompMessage ) ) {
      // QoS 1 & 2 notify users of result before removing from the cache
      notifyResult( ack, token, mex );
      // Do not remove publish / delivery token at this stage do this when the 
      // cache is removed later 
    } else if ( ack instanceof PingRespMessage ) {
      synchronized( pingOutstandingLock ) {
        pingOutstanding = Math.max( 0, pingOutstanding - 1 );
        notifyResult( ack, token, mex );
        if ( pingOutstanding == 0 ) {
          tokenStore.removeToken( ack );
        }
      }
      Log.append( MQTT.EVENT, LogMsg.createMsg( MQTT.MSG, "clientstate.received_ack_ping", pingOutstanding ) );
    } else if ( ack instanceof ConnAckMessage ) {
      final int rc = ( (ConnAckMessage)ack ).getReturnCode();
      if ( rc == 0 ) {
        synchronized( queueLock ) {
          if ( cleanSession ) {
            clearState();
            // Add the connect token back in so that users can be notified when 
            // connect completes.
            tokenStore.saveToken( token, ack );
          }
          inFlightPubRels = 0;
          actualInFlight = 0;
          restoreInflightMessages();
          connected();
        }
      } else {
        mex = MQTT.createMqttException( rc );
        Log.error( LogMsg.createMsg( MQTT.MSG, "clientstate.error_ack", mex ) );
        throw mex;
      }

      connection.connectComplete( (ConnAckMessage)ack, mex );
      notifyResult( ack, token, mex );
      tokenStore.removeToken( ack );

      // Notify the sender thread that there maybe work for it to do now
      synchronized( queueLock ) {
        queueLock.notifyAll();
      }
    } else {
      // Sub ack or unsuback
      notifyResult( ack, token, mex );
      releaseMessageId( ack.getMessageId() );
      tokenStore.removeToken( ack );
    }

    checkQuiesceLock();
  }




  public void notifyReceivedBytes( final int receivedBytesCount ) {
    if ( receivedBytesCount > 0 ) {
      lastInboundActivity = System.currentTimeMillis();
    }
    Log.append( MQTT.EVENT, LogMsg.createMsg( MQTT.MSG, "clientstate.received_bytes", receivedBytesCount ) );
  }




  /**
   * Called by the Receiver when a message has been received.
   * 
   * <p>Handles inbound messages and other flows such as PUBREL.</p> 
   * 
   * @param message
   * 
   * @throws MqttException
   */
  protected void notifyReceivedMsg( final AbstractMessage message ) throws MqttException {
    lastInboundActivity = System.currentTimeMillis();

    Log.append( MQTT.EVENT, LogMsg.createMsg( MQTT.MSG, "clientstate.notigy_received_message", message.getMessageId(), message ) );

    if ( !quiescing ) {
      if ( message instanceof PublishMessage ) {
        final PublishMessage send = (PublishMessage)message;
        switch ( send.getMessage().getQos() ) {
          case 0:
          case 1:
            if ( callback != null ) {
              callback.messageArrived( send );
            }
            break;
          case 2:
            cache.put( getReceivedCacheKey( message ), (PublishMessage)message );
            inboundQoS2.put( new Integer( send.getMessageId() ), send );
            send( new PubRecMessage( send ), null );
            break;

          default:
            //should NOT reach here
        }
      } else if ( message instanceof PubRelMessage ) {
        final PublishMessage sendMsg = (PublishMessage)inboundQoS2.get( new Integer( message.getMessageId() ) );
        if ( sendMsg != null ) {
          if ( callback != null ) {
            callback.messageArrived( sendMsg );
          }
        } else {
          // Original publish has already been delivered.
          final PubCompMessage pubComp = new PubCompMessage( message.getMessageId() );
          send( pubComp, null );
        }
      }
    }
  }




  protected void notifyResult( final AbstractMessage ack, final MqttTokenImpl token, final MqttException ex ) {
    // unblock any threads waiting on the token  
    token.markComplete( ack, ex );
    token.notifyComplete();

    // Let the user know an async operation has completed and then remove the token
    if ( ( ack != null ) && ( ack instanceof AbstractAckMessage ) && !( ack instanceof PubRecMessage ) ) {
      Log.append( MQTT.EVENT, LogMsg.createMsg( MQTT.MSG, "clientstate.notify_result", token.getKey(), ack, ex ) );
      callback.asyncOperationComplete( token );
    }
    // There are cases where there is no ack as the operation failed before 
    // an ack was received 
    if ( ack == null ) {
      Log.append( MQTT.EVENT, LogMsg.createMsg( MQTT.MSG, "clientstate.notify_result_noack", token.getKey(), ex ) );
      callback.asyncOperationComplete( token );
    }
  }




  /**
   * Called by the Sender when a message has been sent.
   * 
   * @param message
   */
  protected void notifySent( final AbstractMessage message ) {

    lastOutboundActivity = System.currentTimeMillis();
    Log.append( MQTT.EVENT, LogMsg.createMsg( MQTT.MSG, "clientstate.notify_sent", message.getKey() ) );

    final MqttTokenImpl token = tokenStore.getToken( message );
    token.notifySent();
    if ( message instanceof PingReqMessage ) {
      synchronized( pingOutstandingLock ) {
        final long time = System.currentTimeMillis();
        synchronized( pingOutstandingLock ) {
          lastPing = time;
          pingOutstanding++;
        }
        Log.append( MQTT.EVENT, LogMsg.createMsg( MQTT.MSG, "clientstate.notify_sent_ping", pingOutstanding ) );
      }
    } else if ( message instanceof PublishMessage ) {
      if ( ( (PublishMessage)message ).getMessage().getQos() == 0 ) {
        // once a QoS 0 message is sent we can clean up its records straight away as
        // we won't be hearing about it again
        token.markComplete( null, null );
        callback.asyncOperationComplete( token );
        decrementInFlight();
        releaseMessageId( message.getMessageId() );
        tokenStore.removeToken( message );
        checkQuiesceLock();
      }
    }
  }




  public void notifySentBytes( final int sentBytesCount ) {
    if ( sentBytesCount > 0 ) {
      lastOutboundActivity = System.currentTimeMillis();
    }
    Log.append( MQTT.EVENT, LogMsg.createMsg( MQTT.MSG, "clientstate.sent_bytes_count", sentBytesCount ) );
  }




  /**
   * Persists a buffered message to the cache layer
   * 
   * @param message
   */
  public void persistBufferedMessage( final AbstractMessage message ) {
    final String key = getSendBufferedCacheKey( message );

    // Because the client will have disconnected, we will want to re-open the cache
    try {
      message.setMessageId( getNextMessageId() );
      try {
        cache.put( key, (PublishMessage)message );
      } catch ( final CacheException mpe ) {
        Log.append( MQTT.EVENT, LogMsg.createMsg( MQTT.MSG, "clientstate.persist_buffered_message_fail", mpe.getMessage() ) );
        cache.open( connection.getClient().getClientId(), connection.getClient().getClientId() );
        cache.put( key, (PublishMessage)message );
      }
      Log.append( MQTT.EVENT, LogMsg.createMsg( MQTT.MSG, "clientstate.persist_buffered_message", key ) );
    } catch ( final MqttException ex ) {
      Log.warn( LogMsg.createMsg( MQTT.MSG, "clientstate.persist_buffered_message_failure", key, ex.getMessage() ) );
    }
  }




  /**
   * Quiesce the client state, preventing any new messages getting sent, and 
   * preventing the callback on any newly received messages.
   * 
   * <p>After the timeout expires, delete any pending messages except for 
   * outbound ACKs, and wait for those ACKs to complete.</p>
   */
  public void quiesce( final long timeout ) {
    if ( timeout > 0 ) {
      Log.append( MQTT.EVENT, LogMsg.createMsg( MQTT.MSG, "clientstate.quiesce_timeout", timeout ) );
      synchronized( queueLock ) {
        quiescing = true;
      }
      // We don't want to handle any new inbound messages
      callback.quiesce();
      notifyQueueLock();

      synchronized( quiesceLock ) {
        try {
          // If token count is not zero there is outbound work to process and 
          // if pending flows is not zero there is outstanding work to complete 
          // and if call back is not quiseced there it needs to complete. 
          final int tokc = tokenStore.count();
          if ( ( tokc > 0 ) || ( pendingFlows.size() > 0 ) || !callback.isQuiesced() ) {
            Log.append( MQTT.EVENT, LogMsg.createMsg( MQTT.MSG, "clientstate.quiesce", actualInFlight, pendingFlows.size(), inFlightPubRels, tokc ) );

            // wait for outstanding in flight messages to complete and
            // any pending flows to complete
            quiesceLock.wait( timeout );
          }
        } catch ( final InterruptedException ex ) {
          // Don't care, as we're shutting down anyway
        }
      }

      // Quiesce time up or inflight messages delivered. Ensure pending delivery
      // vectors are cleared ready for disconnect to be sent as the final flow.
      synchronized( queueLock ) {
        pendingMessages.clear();
        pendingFlows.clear();
        quiescing = false;
        actualInFlight = 0;
      }
      Log.append( MQTT.EVENT, LogMsg.createMsg( MQTT.MSG, "clientstate.quiesced" ) );
    }
  }




  /**
   * Releases a message ID back into the pool of available message IDs.
   * 
   * <p>If the supplied message ID is not in use, then nothing will happen.</p>
   * 
   * @param msgId A message ID that can be freed up for re-use.
   */
  private synchronized void releaseMessageId( final int msgId ) {
    inUseMsgIds.remove( new Integer( msgId ) );
  }




  /**
   * Produces a new list with the messages properly ordered according to their 
   * message id's.
   * 
   * @param list the list containing the messages from which a new reordered is
   *        to be produced. The list will not be modified.
   *        
   * @return a new reordered list
   */
  private Vector reOrder( final Vector list ) {

    // here up the new list
    final Vector newList = new Vector();

    if ( list.size() == 0 ) {
      return newList; // nothing to reorder
    }

    int previousMsgId = 0;
    int largestGap = 0;
    int largestGapMsgIdPosInList = 0;
    for ( int i = 0; i < list.size(); i++ ) {
      final int currentMsgId = ( (AbstractMessage)list.elementAt( i ) ).getMessageId();
      if ( ( currentMsgId - previousMsgId ) > largestGap ) {
        largestGap = currentMsgId - previousMsgId;
        largestGapMsgIdPosInList = i;
      }
      previousMsgId = currentMsgId;
    }
    final int lowestMsgId = ( (AbstractMessage)list.elementAt( 0 ) ).getMessageId();
    final int highestMsgId = previousMsgId; // last in the sorted list

    // we need to check that the gap after highest msg id to the lowest msg id is not beaten
    if ( ( ( MAX_MSG_ID - highestMsgId ) + lowestMsgId ) > largestGap ) {
      largestGapMsgIdPosInList = 0;
    }

    // starting message has been located, let's start from this point on
    for ( int i = largestGapMsgIdPosInList; i < list.size(); i++ ) {
      newList.addElement( list.elementAt( i ) );
    }

    // and any wrapping back to the beginning
    for ( int i = 0; i < largestGapMsgIdPosInList; i++ ) {
      newList.addElement( list.elementAt( i ) );
    }

    return newList;
  }




  /**
   * Called during shutdown to work out if there are any tokens still to be 
   * notified and waiters to be unblocked.
   * 
   * <p>Notifying and unblocking takes place after most shutdown processing has 
   * completed. The tokenstore is tidied up so it only contains outstanding 
   * delivery tokens which are valid after reconnect (if clean session is set 
   * to false)</p>
   * 
   * @param reason The root cause of the disconnection, or null if it is a 
   *        clean disconnect
   */
  public Vector resolveOldTokens( final MqttException reason ) {
    Log.append( MQTT.EVENT, LogMsg.createMsg( MQTT.MSG, "clientstate.resolve_old_tokens", reason ) );

    // If any outstanding let the user know the reason why it is still 
    // outstanding by putting the reason shutdown is occurring into the token. 
    MqttException shutReason = reason;
    if ( reason == null ) {
      shutReason = new MqttException( MqttException.CLIENT_DISCONNECTING );
    }

    // Set the token up so it is ready to be notified after disconnect 
    // processing has completed. Do not remove the token from the store if it 
    // is a delivery token, it is still valid after a reconnect. 
    final Vector retval = tokenStore.getOutstandingTokens();
    final Enumeration outTE = retval.elements();
    while ( outTE.hasMoreElements() ) {
      final MqttTokenImpl tok = (MqttTokenImpl)outTE.nextElement();
      synchronized( tok ) {
        if ( !tok.isComplete() && !tok.isCompletePending() && ( tok.getException() == null ) ) {
          tok.setException( shutReason );
        }
      }
      if ( !( tok instanceof MqttDeliveryToken ) ) {
        // If not a delivery token it is not valid on restart so remove
        tokenStore.removeToken( tok.getKey() );
      }
    }
    return retval;
  }




  private void restoreInflightMessages() {
    pendingMessages = new Vector( maxInflight );
    pendingFlows = new Vector();

    Enumeration keys = outboundQoS2.keys();
    while ( keys.hasMoreElements() ) {
      final Object key = keys.nextElement();
      final AbstractMessage msg = (AbstractMessage)outboundQoS2.get( key );
      if ( msg instanceof PublishMessage ) {
        Log.append( MQTT.EVENT, LogMsg.createMsg( MQTT.MSG, "clientstate.restore_inflight_message_publish", key ) );
        // set DUP flag only for PUBLISH, but NOT for PUBREL (spec 3.1.1)
        msg.setDuplicate( true );
        insertInOrder( pendingMessages, msg );
      } else if ( msg instanceof PubRelMessage ) {
        Log.append( MQTT.EVENT, LogMsg.createMsg( MQTT.MSG, "clientstate.restore_inflight_message_pubrel", key ) );

        insertInOrder( pendingFlows, msg );
      }
    }
    keys = outboundQoS1.keys();
    while ( keys.hasMoreElements() ) {
      final Object key = keys.nextElement();
      final PublishMessage msg = (PublishMessage)outboundQoS1.get( key );
      msg.setDuplicate( true );
      Log.append( MQTT.EVENT, LogMsg.createMsg( MQTT.MSG, "clientstate.restore_inflight_message_publish_qos1", key ) );

      insertInOrder( pendingMessages, msg );
    }
    keys = outboundQoS0.keys();
    while ( keys.hasMoreElements() ) {
      final Object key = keys.nextElement();
      final PublishMessage msg = (PublishMessage)outboundQoS0.get( key );
      Log.append( MQTT.EVENT, LogMsg.createMsg( MQTT.MSG, "clientstate.restore_inflight_message_publish_qos0", key ) );
      insertInOrder( pendingMessages, msg );

    }

    pendingFlows = reOrder( pendingFlows );
    pendingMessages = reOrder( pendingMessages );
  }




  private AbstractMessage restoreMessage( final String key, final Cacheable persistable ) throws MqttException {
    AbstractMessage message = null;

    try {
      message = AbstractMessage.createWireMessage( persistable );
    } catch ( final MqttException ex ) {
      Log.append( MQTT.EVENT, LogMsg.createMsg( MQTT.MSG, "clientstate.restored_message_failed", key, ex ) );
      if ( ex.getCause() instanceof EOFException ) {
        // Premature end-of-file means that the message is corrupted
        if ( key != null ) {
          cache.remove( key );
        }
      } else {
        throw ex;
      }
    }
    Log.append( MQTT.EVENT, LogMsg.createMsg( MQTT.MSG, "clientstate.restored_message", key, message ) );
    return message;
  }




  /**
   * Restores the state information from the cache.
   */
  protected void restoreState() throws MqttException {
    Enumeration messageKeys = cache.keys();
    Cacheable persistable;
    String key;
    int highestMsgId = nextMsgId;
    final Vector orphanedPubRels = new Vector();
    Log.append( MQTT.EVENT, LogMsg.createMsg( MQTT.MSG, "clientstate.restore_state" ) );

    while ( messageKeys.hasMoreElements() ) {
      key = (String)messageKeys.nextElement();
      persistable = cache.get( key );
      final AbstractMessage message = restoreMessage( key, persistable );
      if ( message != null ) {
        if ( key.startsWith( CACHE_RECEIVED_PREFIX ) ) {
          Log.append( MQTT.EVENT, LogMsg.createMsg( MQTT.MSG, "clientstate.restore_state_inbound_message", key, message ) );
          // The inbound messages that we have persisted will be QoS 2 
          inboundQoS2.put( new Integer( message.getMessageId() ), message );
        } else if ( key.startsWith( CACHE_SENT_PREFIX ) ) {
          final PublishMessage sendMessage = (PublishMessage)message;
          highestMsgId = Math.max( sendMessage.getMessageId(), highestMsgId );
          if ( cache.containsKey( getSendConfirmCacheKey( sendMessage ) ) ) {
            final Cacheable persistedConfirm = cache.get( getSendConfirmCacheKey( sendMessage ) );
            // QoS 2, and CONFIRM has already been sent...
            // NO DUP flag is allowed for 3.1.1 spec while it's not clear for 3.1 spec
            // So we just remove DUP
            final PubRelMessage confirmMessage = (PubRelMessage)restoreMessage( key, persistedConfirm );
            if ( confirmMessage != null ) {
              Log.append( MQTT.EVENT, LogMsg.createMsg( MQTT.MSG, "clientstate.restore_state_outbound_qos2_pubrel", key, message ) );
              outboundQoS2.put( new Integer( confirmMessage.getMessageId() ), confirmMessage );
            } else {
              Log.append( MQTT.EVENT, LogMsg.createMsg( MQTT.MSG, "clientstate.restore_state_outbound_qos2_done", key, message ) );
            }
          } else {
            // QoS 1 or 2, with no CONFIRM sent...
            // Put the SEND to the list of pending messages, ensuring message ID ordering...
            sendMessage.setDuplicate( true );
            if ( sendMessage.getMessage().getQos() == 2 ) {
              Log.append( MQTT.EVENT, LogMsg.createMsg( MQTT.MSG, "clientstate.restore_state_outbound_qos2_pub", key, message ) );
              outboundQoS2.put( new Integer( sendMessage.getMessageId() ), sendMessage );
            } else {
              Log.append( MQTT.EVENT, LogMsg.createMsg( MQTT.MSG, "clientstate.restore_state_outbound_qos1_pub", key, message ) );
              outboundQoS1.put( new Integer( sendMessage.getMessageId() ), sendMessage );
            }
          }
          final MqttDeliveryTokenImpl tok = tokenStore.restoreToken( sendMessage );
          tok.setClient( connection.getClient() );
          inUseMsgIds.put( new Integer( sendMessage.getMessageId() ), new Integer( sendMessage.getMessageId() ) );
        } else if ( key.startsWith( CACHE_SENT_BUFFERED_PREFIX ) ) {

          // Buffered outgoing messages that have not yet been sent at all
          final PublishMessage sendMessage = (PublishMessage)message;
          highestMsgId = Math.max( sendMessage.getMessageId(), highestMsgId );
          if ( sendMessage.getMessage().getQos() == 2 ) {
            Log.append( MQTT.EVENT, LogMsg.createMsg( MQTT.MSG, "clientstate.restore_state_outbound_qos2_pub", key, message ) );
            outboundQoS2.put( new Integer( sendMessage.getMessageId() ), sendMessage );
          } else if ( sendMessage.getMessage().getQos() == 1 ) {
            Log.append( MQTT.EVENT, LogMsg.createMsg( MQTT.MSG, "clientstate.restore_state_outbound_qos1_pub", key, message ) );

            outboundQoS1.put( new Integer( sendMessage.getMessageId() ), sendMessage );

          } else {
            Log.append( MQTT.EVENT, LogMsg.createMsg( MQTT.MSG, "clientstate.restore_state_outbound_qos0_pub", key, message ) );
            outboundQoS0.put( new Integer( sendMessage.getMessageId() ), sendMessage );
            // Because there is no Puback, we have to trust that this is enough to send the message
            cache.remove( key );
          }

          final MqttDeliveryTokenImpl tok = tokenStore.restoreToken( sendMessage );
          tok.setClient( connection.getClient() );
          inUseMsgIds.put( new Integer( sendMessage.getMessageId() ), new Integer( sendMessage.getMessageId() ) );

        } else if ( key.startsWith( CACHE_CONFIRMED_PREFIX ) ) {
          final PubRelMessage pubRelMessage = (PubRelMessage)message;
          if ( !cache.containsKey( getSendCacheKey( pubRelMessage ) ) ) {
            orphanedPubRels.addElement( key );
          }
        }
      }
    }

    messageKeys = orphanedPubRels.elements();
    while ( messageKeys.hasMoreElements() ) {
      key = (String)messageKeys.nextElement();
      Log.append( MQTT.EVENT, LogMsg.createMsg( MQTT.MSG, "609", key ) );

      cache.remove( key );
    }

    nextMsgId = highestMsgId;
  }




  /**
   * Submits a message for delivery.
   *  
   * <p>This method will block until there is room in the inFlightWindow for 
   * the message. The message is put into the cache before returning.</p>
   * 
   * @param message  the message to send
   * @param token the token that can be used to track delivery of the message
   * 
   * @throws MqttException
   */
  public void send( final AbstractMessage message, final MqttTokenImpl token ) throws MqttException {
    if ( message.isMessageIdRequired() && ( message.getMessageId() == 0 ) ) {
      message.setMessageId( getNextMessageId() );
    }
    if ( token != null ) {
      try {
        token.setMessageID( message.getMessageId() );
      } catch ( final Exception e ) {}
    }

    if ( message instanceof PublishMessage ) {
      synchronized( queueLock ) {
        if ( actualInFlight >= maxInflight ) {
          Log.append( MQTT.EVENT, LogMsg.createMsg( MQTT.MSG, "clientstate.send_max_inflight", actualInFlight ) );

          throw new MqttException( MqttException.MAX_INFLIGHT );
        }

        final MqttMessage innerMessage = ( (PublishMessage)message ).getMessage();
        Log.append( MQTT.EVENT, LogMsg.createMsg( MQTT.MSG, "clientstate.send_pending_publish", message.getMessageId(), innerMessage.getQos(), message ) );

        switch ( innerMessage.getQos() ) {
          case 2:
            outboundQoS2.put( new Integer( message.getMessageId() ), message );
            cache.put( getSendCacheKey( message ), (PublishMessage)message );
            break;
          case 1:
            outboundQoS1.put( new Integer( message.getMessageId() ), message );
            cache.put( getSendCacheKey( message ), (PublishMessage)message );
            break;
        }
        tokenStore.saveToken( token, message );
        pendingMessages.addElement( message );
        queueLock.notifyAll();
      }
    } else {
      Log.append( MQTT.EVENT, LogMsg.createMsg( MQTT.MSG, "clientstate.send_pending", message.getMessageId(), message ) );

      if ( message instanceof ConnectMessage ) {
        synchronized( queueLock ) {
          // Add the connect action at the head of the pending queue ensuring it jumps
          // ahead of any of other pending actions.
          tokenStore.saveToken( token, message );
          pendingFlows.insertElementAt( message, 0 );
          queueLock.notifyAll();
        }
      } else {
        if ( message instanceof PingReqMessage ) {
          pingCommand = message;
        } else if ( message instanceof PubRelMessage ) {
          outboundQoS2.put( new Integer( message.getMessageId() ), message );
          cache.put( getSendConfirmCacheKey( message ), (PubRelMessage)message );
        } else if ( message instanceof PubCompMessage ) {
          cache.remove( getReceivedCacheKey( message ) );
        }

        synchronized( queueLock ) {
          if ( !( message instanceof AbstractAckMessage ) ) {
            tokenStore.saveToken( token, message );
          }
          pendingFlows.addElement( message );
          queueLock.notifyAll();
        }
      }
    }
  }




  protected void setCleanSession( final boolean cleanSession ) {
    this.cleanSession = cleanSession;
  }




  public void setKeepAliveInterval( final long interval ) {
    keepAlive = interval;
  }




  protected void setKeepAliveSecs( final long keepAliveSecs ) {
    keepAlive = keepAliveSecs * 1000;
  }




  protected void setMaxInflight( final int maxInflight ) {
    this.maxInflight = maxInflight;
    pendingMessages = new Vector( this.maxInflight );
  }




  /**
   * This removes the MqttSend message from the outbound queue and the cache.
   * 
   * @param message the message to dequeue
   * 
   * @throws CacheException
   */
  protected void undo( final PublishMessage message ) throws CacheException {
    synchronized( queueLock ) {
      Log.append( MQTT.EVENT, LogMsg.createMsg( MQTT.MSG, "clientstate.dequeuing_message", message.getMessageId(), message.getMessage().getQos() ) );

      if ( message.getMessage().getQos() == 1 ) {
        outboundQoS1.remove( new Integer( message.getMessageId() ) );
      } else {
        outboundQoS2.remove( new Integer( message.getMessageId() ) );
      }
      pendingMessages.removeElement( message );
      cache.remove( getSendCacheKey( message ) );
      tokenStore.removeToken( message );
      checkQuiesceLock();
    }
  }




  public void unPersistBufferedMessage( final AbstractMessage message ) throws CacheException {
    Log.append( MQTT.EVENT, LogMsg.createMsg( MQTT.MSG, "clientstate.uncache_message", message.getKey() ) );
    cache.remove( getSendBufferedCacheKey( message ) );
  }

}
