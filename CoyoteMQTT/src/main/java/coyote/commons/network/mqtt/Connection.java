package coyote.commons.network.mqtt;

import java.util.Enumeration;
import java.util.Properties;
import java.util.Vector;

import coyote.commons.network.mqtt.cache.CacheException;
import coyote.commons.network.mqtt.cache.CachedMessage;
import coyote.commons.network.mqtt.cache.ClientCache;
import coyote.commons.network.mqtt.network.Transport;
import coyote.commons.network.mqtt.protocol.AbstractMessage;
import coyote.commons.network.mqtt.protocol.ConnAckMessage;
import coyote.commons.network.mqtt.protocol.ConnectMessage;
import coyote.commons.network.mqtt.protocol.DisconnectMessage;
import coyote.commons.network.mqtt.protocol.PublishMessage;
import coyote.loader.log.Log;
import coyote.loader.log.LogMsg;


/**
 * Handles client communications with the server.  Sends and receives MQTT V3
 * messages.
 */
public class Connection {

  private static final byte CONNECTED = 0;
  private static final byte CONNECTING = 1;
  private static final byte DISCONNECTING = 2;

  private static final byte DISCONNECTED = 3;
  private static final byte CLOSED = 4;
  private final MqttClient client;
  private int transportIndex;
  private Transport[] transports;
  private Receiver receiver;
  private Sender sender;
  private Callback callback;
  private ClientState clientState;
  private MqttConnectOptions conOptions;
  private ClientCache persistence;
  private PingSender pingSender;

  private TokenStore tokenStore;
  private boolean stopping = false;
  private byte conState = DISCONNECTED;
  private final Object conLock = new Object(); // Used to synchronize connection state
  private boolean closePending = false;

  private boolean resting = false;

  private DisconnectedMessageBuffer disconnectedMessageBuffer;

  // Kick off the connect processing in the background so that it does not block. For instance
  // the socket could take time to create.
  private class ConnectBG implements Runnable {
    Connection connection = null;
    Thread bkgConnectThread = null;
    MqttTokenImpl conToken;
    ConnectMessage conPacket;




    ConnectBG( final Connection conn, final MqttTokenImpl cToken, final ConnectMessage cPacket ) {
      connection = conn;
      conToken = cToken;
      conPacket = cPacket;
      bkgConnectThread = new Thread( this, "MQTT Con: " + getClient().getClientId() );
    }




    @Override
    public void run() {
      MqttException mqttEx = null;
      Log.append( MQTT.EVENT, LogMsg.createMsg( MQTT.MSG, "connection.running", client.getClientId() ) );

      try {
        // Reset an exception on existing delivery tokens.
        // This will have been set if disconnect occurred before delivery was
        // fully processed.
        final MqttDeliveryTokenImpl[] toks = tokenStore.getOutstandingDelTokens();
        for ( final MqttDeliveryTokenImpl tok : toks ) {
          tok.setException( null );
        }

        // Save the connect token in tokenStore as failure can occur before send
        tokenStore.saveToken( conToken, conPacket );

        // Connect to the server at the network level e.g. TCP socket and then
        // start the background processing threads before sending the connect
        // packet.
        final Transport transport = transports[transportIndex];

        transport.start();

        receiver = new Receiver( connection, clientState, tokenStore, transport.getInputStream() );
        receiver.start( "MQTT Rec: " + getClient().getClientId() );

        sender = new Sender( connection, clientState, tokenStore, transport.getOutputStream() );
        sender.start( "MQTT Snd: " + getClient().getClientId() );

        callback.start( "MQTT Call: " + getClient().getClientId() );
        internalSend( conPacket, conToken );
      } catch ( final MqttException ex ) {
        Log.append( MQTT.EVENT, LogMsg.createMsg( MQTT.MSG, "connection.connect_failed", client.getClientId(), ex.getMessage() ) );
        mqttEx = ex;
      } catch ( final Exception ex ) {
        Log.append( MQTT.EVENT, LogMsg.createMsg( MQTT.MSG, "connection.connect_failed", client.getClientId(), ex.getMessage() ) );
        mqttEx = MQTT.createMqttConnectionException( ex );
      }

      if ( mqttEx != null ) {
        shutdownConnection( conToken, mqttEx );
      }
    }




    void start() {
      bkgConnectThread.start();
    }
  }

  // Kick off the disconnect processing in the background so that it does not
  // block. For instance the quiesce
  private class DisconnectBG implements Runnable {
    Thread bkgDisconnectThread = null;
    DisconnectMessage disconnect;
    long quiesceTimeout;
    MqttTokenImpl token;




    DisconnectBG( final DisconnectMessage discoMsg, final long quiesceTime, final MqttTokenImpl token ) {
      disconnect = discoMsg;
      quiesceTimeout = quiesceTime;
      this.token = token;
    }




    @Override
    public void run() {
      Log.append( MQTT.EVENT, LogMsg.createMsg( MQTT.MSG, "connection.disconnect" ) );
      // Allow current inbound and outbound work to complete
      clientState.quiesce( quiesceTimeout );
      try {
        internalSend( disconnect, token );
        token.waitUntilSent();
      } catch ( final MqttException ex ) {}
      finally {
        token.markComplete( null, null );
        shutdownConnection( token, null );
      }
    }




    void start() {
      bkgDisconnectThread = new Thread( this, "MQTT Disc: " + getClient().getClientId() );
      bkgDisconnectThread.start();
    }
  }




  /**
   * Creates a new Connection object.
   */
  public Connection( final MqttClient client, final ClientCache persistence, final PingSender pingSender ) throws MqttException {
    conState = DISCONNECTED;
    this.client = client;
    this.persistence = persistence;
    this.pingSender = pingSender;
    this.pingSender.init( this );

    tokenStore = new TokenStore( getClient().getClientId() );
    callback = new Callback( this );
    clientState = new ClientState( persistence, tokenStore, callback, this, pingSender );

    callback.setClientState( clientState );
  }




  /**
   * Check and send a ping if needed and check for ping timeout.
   * 
   * <p>Need to send a ping if nothing has been sent or received in the last 
   * keep-alive interval.</p>
   */
  public MqttTokenImpl checkForActivity() {
    return this.checkForActivity( null );
  }




  /**
   * Check and send a ping if needed and check for ping timeout.
   * 
   * <p>Need to send a ping if nothing has been sent or received in the last 
   * keep-alive interval. Passes a listener to ClientState.checkForActivity
   * so that the call-backs are attached as soon as the token is created.</p>
   */
  public MqttTokenImpl checkForActivity( final AsyncActionListener pingCallback ) {
    MqttTokenImpl token = null;
    try {
      token = clientState.checkForActivity( pingCallback );
    } catch ( final MqttException e ) {
      handleRunException( e );
    } catch ( final Exception e ) {
      handleRunException( e );
    }
    return token;
  }




  /**
   * Close and tidy up.
   *
   * @throws MqttException if not disconnected
   */
  public void close() throws MqttException {
    synchronized( conLock ) {
      if ( !isClosed() ) {
        // Must be disconnected before close can take place
        if ( !isDisconnected() ) {
          Log.append( MQTT.EVENT, LogMsg.createMsg( MQTT.MSG, "connection.close_failed" ) );

          if ( isConnecting() ) {
            throw new MqttException( MqttException.CONNECT_IN_PROGRESS );
          } else if ( isConnected() ) {
            throw MQTT.createMqttException( MqttException.CLIENT_CONNECTED );
          } else if ( isDisconnecting() ) {
            closePending = true;
            return;
          }
        }

        conState = CLOSED;

        // ShutdownConnection has already cleaned most things
        clientState.close();
        clientState = null;
        callback = null;
        persistence = null;
        sender = null;
        pingSender = null;
        receiver = null;
        transports = null;
        conOptions = null;
        tokenStore = null;
      }
    }
  }




  /**
   * Sends a connect message and waits for an ACK or NACK.
   * 
   * <p>Connecting is a special case which will also start up the network 
   * connection, receive thread, and keep alive thread.</p>
   */
  public void connect( final MqttConnectOptions options, final MqttTokenImpl token ) throws MqttException {
    synchronized( conLock ) {
      if ( isDisconnected() && !closePending ) {
        Log.append( MQTT.EVENT, LogMsg.createMsg( MQTT.MSG, "client.connecting" ) );

        conState = CONNECTING;

        conOptions = options;

        final ConnectMessage connect = new ConnectMessage( client.getClientId(), conOptions.getMqttVersion(), conOptions.isCleanSession(), conOptions.getKeepAliveInterval(), conOptions.getUserName(), conOptions.getPassword(), conOptions.getWillMessage(), conOptions.getWillDestination() );

        clientState.setKeepAliveSecs( conOptions.getKeepAliveInterval() );
        clientState.setCleanSession( conOptions.isCleanSession() );
        clientState.setMaxInflight( conOptions.getMaxInflight() );

        tokenStore.open();
        final ConnectBG conbg = new ConnectBG( this, token, connect );
        conbg.start();
      } else {
        Log.append( MQTT.EVENT, LogMsg.createMsg( MQTT.MSG, "client.connect_failed", conState ) );
        if ( isClosed() || closePending ) {
          throw new MqttException( MqttException.CLIENT_CLOSED );
        } else if ( isConnecting() ) {
          throw new MqttException( MqttException.CONNECT_IN_PROGRESS );
        } else if ( isDisconnecting() ) {
          throw new MqttException( MqttException.CLIENT_DISCONNECTING );
        } else {
          throw MQTT.createMqttException( MqttException.CLIENT_CONNECTED );
        }
      }
    }
  }




  public void connectComplete( final ConnAckMessage cack, final MqttException mex ) throws MqttException {
    final int rc = cack.getReturnCode();
    synchronized( conLock ) {
      if ( rc == 0 ) {
        // We've successfully connected
        Log.append( MQTT.EVENT, LogMsg.createMsg( MQTT.MSG, "connection.connected" ) );

        conState = CONNECTED;
        return;
      }
    }

    Log.append( MQTT.EVENT, LogMsg.createMsg( MQTT.MSG, "connection.connect_failed", rc ) );
    throw mex;
  }




  public void deleteBufferedMessage( final int bufferIndex ) {
    disconnectedMessageBuffer.deleteMessage( bufferIndex );
  }




  protected void deliveryComplete( final int messageId ) throws CacheException {
    clientState.deliveryComplete( messageId );
  }




  protected void deliveryComplete( final PublishMessage msg ) throws CacheException {
    clientState.deliveryComplete( msg );
  }




  public void disconnect( final DisconnectMessage disconnect, final long quiesceTimeout, final MqttTokenImpl token ) throws MqttException {
    synchronized( conLock ) {
      if ( isClosed() ) {
        Log.append( MQTT.EVENT, LogMsg.createMsg( MQTT.MSG, "connection.disconnect_failed_in_closed_state" ) );
        throw MQTT.createMqttException( MqttException.CLIENT_CLOSED );
      } else if ( isDisconnected() ) {
        Log.append( MQTT.EVENT, LogMsg.createMsg( MQTT.MSG, "connection.disconnect_failed_already_disconnected" ) );
        throw MQTT.createMqttException( MqttException.CLIENT_ALREADY_DISCONNECTED );
      } else if ( isDisconnecting() ) {
        Log.append( MQTT.EVENT, LogMsg.createMsg( MQTT.MSG, "connection.disconnect_failed_already_disconnecting" ) );
        throw MQTT.createMqttException( MqttException.CLIENT_DISCONNECTING );
      } else if ( Thread.currentThread() == callback.getThread() ) {
        Log.append( MQTT.EVENT, LogMsg.createMsg( MQTT.MSG, "connection.disconnect_failed_in_callback_thread" ) );
        // Not allowed to call disconnect() from the callback, as it will deadlock.
        throw MQTT.createMqttException( MqttException.CLIENT_DISCONNECT_PROHIBITED );
      }

      Log.append( MQTT.EVENT, LogMsg.createMsg( MQTT.MSG, "client.disconnecting" ) );
      conState = DISCONNECTING;
      final DisconnectBG discbg = new DisconnectBG( disconnect, quiesceTimeout, token );
      discbg.start();
    }
  }




  /**
   * Disconnect the connection and reset all the states.
   */
  public void disconnectForcibly( final long quiesceTimeout, final long disconnectTimeout ) throws MqttException {
    // Allow current inbound and outbound work to complete
    clientState.quiesce( quiesceTimeout );
    final MqttTokenImpl token = new MqttTokenImpl();
    try {
      // Send disconnect packet
      internalSend( new DisconnectMessage(), token );

      // Wait util the disconnect packet sent with timeout
      token.waitForCompletion( disconnectTimeout );
    } catch ( final Exception ex ) {
      // ignore, probably means we failed to send the disconnect packet.
    }
    finally {
      token.markComplete( null, null );
      shutdownConnection( token, null );
    }
  }




  public MqttMessage getBufferedMessage( final int bufferIndex ) {
    final PublishMessage send = (PublishMessage)disconnectedMessageBuffer.getMessage( bufferIndex ).getMessage();
    return send.getMessage();
  }




  public int getBufferedMessageCount() {
    return disconnectedMessageBuffer.getMessageCount();
  }




  public MqttClient getClient() {
    return client;
  }




  public ClientState getClientState() {
    return clientState;
  }




  public MqttConnectOptions getConOptions() {
    return conOptions;
  }




  public Properties getDebug() {
    final Properties props = new Properties();
    props.put( "conState", new Integer( conState ) );
    props.put( "serverURI", getClient().getServerURI() );
    props.put( "callback", callback );
    props.put( "stoppingConnection", new Boolean( stopping ) );
    return props;
  }




  public long getKeepAlive() {
    return clientState.getKeepAlive();
  }




  public int getTransportIndex() {
    return transportIndex;
  }




  public Transport[] getTransports() {
    return transports;
  }




  public MqttDeliveryTokenImpl[] getPendingDeliveryTokens() {
    return tokenStore.getOutstandingDelTokens();
  }




  Receiver getReceiver() {
    return receiver;
  }




  protected Topic getTopic( final String topic ) {
    return new Topic( topic, this );
  }




  // Tidy up. There may be tokens outstanding as the client was
  // not disconnected/quiseced cleanly! Work out what tokens still
  // need to be notified and waiters unblocked. Store the
  // disconnect or connect token to notify after disconnect is
  // complete.
  private MqttTokenImpl handleOldTokens( final MqttTokenImpl token, final MqttException reason ) {

    MqttTokenImpl tokToNotifyLater = null;
    try {
      // First the token that was related to the disconnect / shutdown may
      // not be in the token table - temporarily add it if not
      if ( token != null ) {
        if ( tokenStore.getToken( token.getKey() ) == null ) {
          tokenStore.saveToken( token, token.getKey() );
        }
      }

      final Vector toksToNot = clientState.resolveOldTokens( reason );
      final Enumeration toksToNotE = toksToNot.elements();
      while ( toksToNotE.hasMoreElements() ) {
        final MqttTokenImpl tok = (MqttTokenImpl)toksToNotE.nextElement();

        if ( tok.getKey().equals( DisconnectMessage.KEY ) || tok.getKey().equals( ConnectMessage.KEY ) ) {
          // Its con or discon so remember and notify @ end of disc routine
          tokToNotifyLater = tok;
        } else {
          // notify waiters and callbacks of outstanding tokens
          // that a problem has occurred and disconnect is in
          // progress
          callback.asyncOperationComplete( tok );
        }
      }
    } catch ( final Exception ex ) {
      // Ignore as we are shutting down
    }
    return tokToNotifyLater;
  }




  private void handleRunException( final Exception ex ) {
    Log.append( MQTT.EVENT, LogMsg.createMsg( MQTT.MSG, "Client.run_exception", ex.getMessage() ) );
    MqttException mex;
    if ( !( ex instanceof MqttException ) ) {
      mex = new MqttException( MqttException.CONNECTION_LOST, ex );
    } else {
      mex = (MqttException)ex;
    }

    shutdownConnection( null, mex );
  }




  /**
   * Sends a message to the server. 
   * 
   * <p>Does not check if connected this validation must be done by invoking 
   * routines.</p>
   * 
   * @param message the message to send
   * @param token the token for the operation
   * 
   * @throws MqttException
   */
  void internalSend( final AbstractMessage message, final MqttTokenImpl token ) throws MqttException {
    Log.append( MQTT.EVENT, LogMsg.createMsg( MQTT.MSG, "connection.send", message.getKey(), message, token ) );

    if ( token.getClient() == null ) {
      // Associate the client with the token - also marks it as in use.
      token.setClient( getClient() );
    } else {
      // Token is already in use - cannot reuse
      Log.append( MQTT.EVENT, LogMsg.createMsg( MQTT.MSG, "Client.send_failed_token_in_use", message.getKey(), message, token ) );
      throw new MqttException( MqttException.TOKEN_INUSE );
    }

    try {
      // Persist if needed and send the message
      clientState.send( message, token );
    } catch ( final MqttException e ) {
      if ( message instanceof PublishMessage ) {
        clientState.undo( (PublishMessage)message );
      }
      throw e;
    }
  }




  public boolean isClosed() {
    synchronized( conLock ) {
      return conState == CLOSED;
    }
  }




  public boolean isConnected() {
    synchronized( conLock ) {
      return conState == CONNECTED;
    }
  }




  public boolean isConnecting() {
    synchronized( conLock ) {
      return conState == CONNECTING;
    }
  }




  public boolean isDisconnected() {
    synchronized( conLock ) {
      return conState == DISCONNECTED;
    }
  }




  public boolean isDisconnecting() {
    synchronized( conLock ) {
      return conState == DISCONNECTING;
    }
  }




  public boolean isResting() {
    synchronized( conLock ) {
      return resting;
    }
  }




  public void messageArrivedComplete( final int messageId, final int qos ) throws MqttException {
    callback.messageArrivedComplete( messageId, qos );
  }




  /**
   * When the client automatically reconnects, we want to send all messages from the
   * buffer first before allowing the user to send any messages
   */
  public void notifyReconnect() {
    if ( disconnectedMessageBuffer != null ) {
      Log.append( MQTT.EVENT, LogMsg.createMsg( MQTT.MSG, "connection.reconnect" ) );
      disconnectedMessageBuffer.setPublishCallback( new DisconnectedBufferCallback() {

        @Override
        public void publishBufferedMessage( final CachedMessage bufferedMessage ) throws MqttException {
          if ( isConnected() ) {
            while ( clientState.getActualInFlight() >= ( clientState.getMaxInFlight() - 1 ) ) {
              // We need to Yield to the other threads to allow the in flight messages to clear
              Thread.yield();
            }
            Log.append( MQTT.EVENT, LogMsg.createMsg( MQTT.MSG, "connection.sending_cached_message", bufferedMessage.getMessage().getKey() ) );
            internalSend( bufferedMessage.getMessage(), bufferedMessage.getToken() );
            // Delete from persistence if in there
            clientState.unPersistBufferedMessage( bufferedMessage.getMessage() );
          } else {
            Log.append( MQTT.EVENT, LogMsg.createMsg( MQTT.MSG, "connection.reconnect_failed" ) );
            throw MQTT.createMqttException( MqttException.CLIENT_NOT_CONNECTED );
          }
        }
      } );
      new Thread( disconnectedMessageBuffer ).start();
    }
  }




  public void removeMessageListener( final String topicFilter ) {
    callback.removeMessageListener( topicFilter );
  }




  /**
   * Sends a message to the broker if in connected state, but only waits for the message to be
   * stored, before returning.
   */
  public void sendNoWait( final AbstractMessage message, final MqttTokenImpl token ) throws MqttException {
    if ( isConnected() || ( !isConnected() && ( message instanceof ConnectMessage ) ) || ( isDisconnecting() && ( message instanceof DisconnectMessage ) ) ) {
      if ( ( disconnectedMessageBuffer != null ) && ( disconnectedMessageBuffer.getMessageCount() != 0 ) ) {
        Log.append( MQTT.EVENT, LogMsg.createMsg( MQTT.MSG, "connection.storing_send_nowait", message.getKey() ) );
        clientState.persistBufferedMessage( message );
        disconnectedMessageBuffer.putMessage( message, token );
      } else {
        internalSend( message, token );
      }
    } else if ( ( disconnectedMessageBuffer != null ) && isResting() ) {
      Log.append( MQTT.EVENT, LogMsg.createMsg( MQTT.MSG, "connection.resting_send_nowait", message.getKey() ) );
      clientState.persistBufferedMessage( message );
      disconnectedMessageBuffer.putMessage( message, token );
    } else {
      Log.append( MQTT.EVENT, LogMsg.createMsg( MQTT.MSG, "connection.failed_send_nowait", message.getKey() ) );
      throw MQTT.createMqttException( MqttException.CLIENT_NOT_CONNECTED );
    }
  }




  public void setCallback( final ClientListener mqttCallback ) {
    callback.setCallback( mqttCallback );
  }




  public void setDisconnectedMessageBuffer( final DisconnectedMessageBuffer disconnectedMessageBuffer ) {
    this.disconnectedMessageBuffer = disconnectedMessageBuffer;
  }




  public void setManualAcks( final boolean manualAcks ) {
    callback.setManualAcks( manualAcks );
  }




  public void setMessageListener( final String topicFilter, final MessageListener messageListener ) {
    callback.setMessageListener( topicFilter, messageListener );
  }




  public void setTransportIndex( final int index ) {
    transportIndex = index;
  }




  /**
   * Set the array of transports the connection is to use.
   * 
   * <p>There is one transport for each broker URI specified for the 
   * connection. Normally there is only one, but it is possible to define 
   * multiple server URIs in the connection options which allows the connection 
   * to fail-over to other brokers if the connection fails.</p> 
   * 
   * @param transports the array of network transports to use for broker 
   *        connections
   */
  public void setTransports( final Transport[] transports ) {
    this.transports = transports;
  }




  /**
   * Set the listener responsible for reconnecting when the connection is lost.
   *  
   * @param callback the object which will attempt to reconnect us when the 
   *        connection is lost.
   */
  public void setReconnectCallback( final ClientListener callback ) {
    this.callback.setReconnectCallback( callback );
  }




  /**
   * When Automatic reconnect is enabled, we want ClientComs to enter the
   * 'resting' state if disconnected. This will allow us to publish messages
   * 
   * @param resting
   */
  public void setRestingState( final boolean resting ) {
    this.resting = resting;
  }




  /**
   * Shuts down the connection to the server.
   * 
   * This may have been invoked as a result of a user calling disconnect or
   * an abnormal disconnection.  The method may be invoked multiple times
   * in parallel as each thread when it receives an error uses this method
   * to ensure that shutdown completes successfully.
   */
  public void shutdownConnection( final MqttTokenImpl token, final MqttException reason ) {
    boolean wasConnected;
    MqttTokenImpl endToken = null; //Token to notify after disconnect completes

    // This method could concurrently be invoked from many places only allow it
    // to run once.
    synchronized( conLock ) {
      if ( stopping || closePending || isClosed() ) {
        return;
      }
      stopping = true;

      Log.append( MQTT.EVENT, LogMsg.createMsg( MQTT.MSG, "client.disconnecting" ) );

      wasConnected = ( isConnected() || isDisconnecting() );
      conState = DISCONNECTING;
    }

    // Update the token with the reason for shutdown if it is not already complete.
    if ( ( token != null ) && !token.isComplete() ) {
      token.setException( reason );
    }

    // Stop the thread that is used to call the user back when actions complete
    if ( callback != null ) {
      callback.stop();
    }

    // Stop the network transport, send and receive is now not possible
    try {
      if ( transports != null ) {
        final Transport transport = transports[transportIndex];
        if ( transport != null ) {
          transport.stop();
        }
      }
    } catch ( final Exception ioe ) {
      // Ignore as we are shutting down
    }

    // Stop the thread that handles inbound work from the network
    if ( receiver != null ) {
      receiver.stop();
    }

    // Stop any new tokens being saved by app and throwing an exception if they do
    tokenStore.quiesce( new MqttException( MqttException.CLIENT_DISCONNECTING ) );

    // Notify any outstanding tokens with the exception of CON or DISCON which 
    // may be returned and will be notified at the end
    endToken = handleOldTokens( token, reason );

    try {
      // Clean session handling and tidy up
      clientState.disconnected( reason );
      if ( clientState.getCleanSession() ) {
        callback.removeMessageListeners();
      }
    } catch ( final Exception ex ) {
      // Ignore as we are shutting down
    }

    if ( sender != null ) {
      sender.stop();
    }

    if ( pingSender != null ) {
      pingSender.stop();
    }

    try {
      if ( ( disconnectedMessageBuffer == null ) && ( persistence != null ) ) {
        persistence.close();
      }

    } catch ( final Exception ex ) {
      // Ignore as we are shutting down
    }
    // All disconnect logic has been completed allowing the
    // client to be marked as disconnected.
    synchronized( conLock ) {
      Log.append( MQTT.EVENT, LogMsg.createMsg( MQTT.MSG, "client.disconnected" ) );
      conState = DISCONNECTED;
      stopping = false;
    }

    // Internal disconnect processing has completed.  If there
    // is a disconnect token or a connect in error notify
    // it now. This is done at the end to allow a new connect
    // to be processed and now throw a currently disconnecting error.
    // any outstanding tokens and unblock any waiters
    if ( ( endToken != null ) & ( callback != null ) ) {
      callback.asyncOperationComplete( endToken );
    }

    if ( wasConnected && ( callback != null ) ) {
      // Let the user know client has disconnected either normally or abnormally
      callback.connectionLost( reason );
    }

    // While disconnecting, close may have been requested - try it now
    synchronized( conLock ) {
      if ( closePending ) {
        try {
          close();
        } catch ( final Exception e ) { // ignore any errors as closing
        }
      }
    }
  }

}
