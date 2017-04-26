/*
 * Copyright (c) 2006 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 *
 * Contributors:
 *   Stephan D. Cote 
 *      - Initial concept and implementation
 */
package coyote.mbus.network;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.URI;
import java.util.HashMap;
import java.util.List;

import coyote.mbus.MessageSink;
import coyote.mbus.message.Message;
import coyote.mbus.message.MessageAddress;


/**
 * MessageSession runs in its own thread and performs all network IO for a 
 * MessageSession. 
 * 
 * <p>Objects of this class maintain a connection to another MessageSession or 
 * similar service in order to affect event operations. It is this class that 
 * provides dedicated network communications to reliably pass events between 
 * systems or a client and an event mediation system.</p>
 * 
 * <p>Since TCP is used as a transport, it is assumed that all events will 
 * arrive in their proper order and there is no need to cache transmitted 
 * messages for possible NAKs from the receiver.</p>
 * 
 * <p>TCP connections will be re-established if the "restart" flag is set to 
 * true which will cause the run() method in the ThreadJob class to loop back 
 * up to the initialize method. The initialize method will notice the socket 
 * exists and will assume this is a subsequent initialization and attempt a 
 * reconnection.</p>
 * 
 * <p>As far as dispatching of events go, all MessageSessions run in their own 
 * thread and take events off the socket channel as fast as possible. When an 
 * event is received, it is placed in the MessageChannel.</p>
 */
public class MessageSession implements MessageSink, Runnable, MessageMediator {
  /** The socket through which we communicate. */
  Socket socket = null;

  /** The URI to the server. */
  URI serverUri = null;

  /** The time this session has connected to the server. */
  long connectedTime;

  //

  //

  // 

  RemoteService remoteService = null;

  /** Reference to our current thread of execution */
  protected Thread current_thread = null;

  /** Indicates we have been asked to stop processing and shutdown */
  protected volatile boolean shutdown;

  /** Indicates that we should shutdown, reinitialize and start running again */
  protected volatile boolean restart = false;

  /**
   * Flag indicating if we have entered and are currently active within the main
   * run loop
   */
  protected volatile boolean active = false;

  /** Indicates we have been asked to temporally stop processing */
  protected volatile boolean suspended;

  /** The how long we pause when idling due to inactivity */
  protected volatile long idle_wait_time = 10;

  /** The specialization of the IChannel we use for all our communications */
  //private SocketChannel socketChannel = null;
  /** The URI to the remote service we are to connect */
  private URI serviceUri = null;

  /** The input stream we use to get all our data */
  private DataInputStream dis = null;

  /** The output stream we use to send all our data */
  private DataOutputStream dos = null;

  /** The event channel we use to communicate inbound and outbound events */
  private MessageChannel messageChannel = null;

  /** The client identifier assigned to the session by the server */
  private String clientId = null;

  /** 
   * A map of group to which this session has joined and their policy capsules.
   */
  private final HashMap groups = new HashMap();

  /**
   * The channel we use to talk to the other event session so it can join on
   * our behalf
   */
  static final String EVENT_SESSION_GROUP = "_EVENT_SESSION_";

  /**
   * The name of the field that contains the action a private event is to
   * signal. This field is private to event session events.
   */
  static final String ACTION_FIELD = "ACTN";

  /**
   * The action indicating a 'join' operation. This field is private to
   * event session events.
   */
  static final String JOIN_ACTION = "JOIN";

  /**
   * The action indicating an 'leave' operation. This field is private to
   * event session events.
   */
  static final String LEAVE_ACTION = "LEAVE";

  /** Field STOP_ACTION */
  static final String STOP_ACTION = "STOP";

  /**
   * The name of the field that contains the group argument of a 'join' action. 
   * This field is private to event session events.
   */
  static final String GROUP_FIELD = "GRP";

  /**
   * The name of the field that contains the policy of a 'join' action.
   * This field is private to event session events.
   */
  private static final String POLICY_FIELD = "PLCY";

  /**
   * The action indicating the client identifier is being set. This field is
   * private to event session events.
   */
  static final String SETID_ACTION = "SETID";

  /**
   * The name of the field that contains the client identifier of a 'setID'
   * action. This field is private to event session events.
   */
  static final String CLIENTID_FIELD = "CID";

  /** A group of Session listeners to be notified when this session changes */
  List sessionListeners = null;

  private final Object opLock = new Object();
  private volatile boolean operational = false;




  /**
   * Create an MessageSession with the given Message Sink as the inbound sink.
   *
   * @param sink The Message Sink that will handle all our inbound events.
   */
  public MessageSession( final MessageSink sink ) {
    // setup the component that coordinates the exchange of events giving it
    // this object as it reference to an event transport. This means the
    // RemoteService object will pass us all its events through our process()
    // method. And since we only have one other endpoint (the MessageSink),
    // we then just pass the events we receive to it via its onMessage()
    // method. It then takes the event through all its processing via our
    // thread of execution.
    remoteService = new RemoteService( this );

    if ( sink == null ) {
      messageChannel = new MessageChannel( this );
    } else {
      // If the listener is an Message Manager...
      //if( sink instanceof MessageManager )
      //{
      //  // save the reference to the manager for later cleanup on termination
      //  eventManager = (MessageManager)sink;
      //
      //  // Have the event manager assign us a channel to make sure that when
      //  // the manager routes() events, this sessions channel is included
      //  messageChannel = eventManager.createChannel( sink );
      //}
      //else
      {
        messageChannel = new MessageChannel( sink );
      }
    } // is inSink = null

    // setup the event channel (ThreadJob.initialize)
    //messageChannel.initialize();

    //MicroBus.log( "Constructor: using channel " + messageChannel );
  }




  /**
   * Run the session in a background user thread.
   *
   * @return TODO finish documentation.
   */
  public Thread daemonize() {
    current_thread = new Thread( this );

    // only user threads keep the JVM running
    current_thread.setDaemon( true );

    current_thread.setName( "MsgSsn:" + serviceUri.getHost() + ":" + serviceUri.getPort() );

    // start it
    current_thread.start();

    // give the thread a chance to start
    Thread.yield();

    return current_thread;
  }




  public void onMessage( final Message msg ) {

  }




  /**
   * Do something with an event.
   * 
   * <p>This is called because this object is referenced as an MessageSink in an
   * MessageChannel somewhere. This method will normally be called because a
   * listener was not passed to us, meaning there is no-where for the event to
   * go. That is why we just ignore the event, because without a listener, we
   * have no idea how to process the event.</p>
   *
   * @param message The message event to process(ignore).
   */
  public void process( final Message message ) {
    // MicroBus.log( "process(Message) " + socketChannel.getLocalURI() + "->" + socketChannel.getRemoteURI() + "" );

    // since we have only one channel, we can route this ourselves
    if ( message != null ) {
      // MicroBus.log("process(Message): event: " + event.toXml() );

      // Set the reference to the event channel from where this event came
      //message.setMessageChannel( messageChannel );

      // check to see if this is a private message
      if ( MessageSession.EVENT_SESSION_GROUP.equals( message.getGroup() ) ) {
        // MicroBus.log("process(Message): Private message" );
        processPrivatePacket( message );
      } else {
        // MicroBus.log("process(Message): Public message" );

        if ( messageChannel != null ) {
          // MicroBus.log("process(Message): to event channel " + messageChannel );
          messageChannel.receive( message );
        } // channel !null

      } // private event

    } // event !null

  }




  /**
   * Connect to the remote service and start running in the background.
   *
   * @param uri The remote service URI
   *
   * @throws IllegalArgumentException
   */
  public void connect( final URI uri ) throws IllegalArgumentException {

    if ( uri == null ) {
      throw new IllegalArgumentException( "URI is null" );
    }

    if ( uri.getHost() == null ) {
      throw new IllegalArgumentException( "URI specifies null host" );
    }

    if ( uri.getHost().trim().length() < 1 ) {
      throw new IllegalArgumentException( "URI specifies illegal host of '" + uri.getHost() + "'" );
    }

    if ( uri.getPort() < 1 ) {
      throw new IllegalArgumentException( "URI specifies illegal port of '" + uri.getPort() + "'" );
    }

    serviceUri = uri;

    try {
      // Kick off connection establishment
      socket = new Socket( serviceUri.getHost(), serviceUri.getPort() );
      connectedTime = System.currentTimeMillis();
      System.out.println( "---------------------CONNECTED----------------------" );
      // Start the session running in the background
      daemonize();
    } catch ( final Throwable t ) {
      try {
        // try to clean things up on our end
        socket.close();
      } catch ( final Throwable ignore ) {}

      throw new IllegalArgumentException( t.getMessage() );
    }
  }




  /**
   * Stop the thread from running and prevent a re-connection from occuring.
   * 
   * <p>MessageSession listeners are not notified at this point. Disconnection
   * notification occurs in a single location: the terminate() method.</p>
   */
  public void disconnect() {
    // don't try to restart/reconnect
    restart = false;

    // stop the thread from running
    shutdown();
  }




  /**
   * Tell the remote systems that we want it to send us all events with the
   * given group name.
   * 
   * <p>We have to create an event that will be intercepted by the MessageSession 
   * at the other end. We do that by creating an event on a private channel 
   * that the other event service on the remote end will catch and process.</p>
   * 
   * <p><strong>NOTE:</strong> This action is limited to the session context 
   * and does NOT affect the channel membership. When sessions are used to wrap 
   * a channel, the operations are focused on synchronizing the remote event
   * channel with the current channel. The remote event channel that is 
   * connected to the event source needs to register the membership in that
   * context so events will be passed to that channel and, in turn, sent to the
   * local channel through the session connection.</p>
   *
   * @param group Name of the group to which we wish to join
   * 
   * @throws IllegalArgumentException if already subscribed to the channel
   */
  public void join( final String group ) throws IllegalArgumentException {
    if ( groups.containsKey( group ) ) {
      throw new IllegalArgumentException( "Already a member of group" );
    }

    final Message message = new Message();
    message.setGroup( MessageSession.EVENT_SESSION_GROUP );
    message.add( MessageSession.ACTION_FIELD, MessageSession.JOIN_ACTION );
    message.add( MessageSession.GROUP_FIELD, group );
    send( message );
    //fireJoin( group );

    // record the subscription in case we have to reconnect
    groups.put( group, null );
  }




  /**
   * Tell the remote systems that we want it to send us all events with the
   * given group name.
   * 
   * <p>We have to create an event that will be intercepted by the MessageSession 
   * at the other end. We do that by creating an event on a private group that 
   * the other event service on the remote end will catch and process.</p>
   *
   * @param group Name of the group to which we wish to join
   * @param capsule Capsule containing a membership or subscription attributes
   * 
   * @throws IllegalArgumentException if already subscribed to the group
   */
  public void join( final String group, final Message capsule ) throws IllegalArgumentException {
    if ( groups.containsKey( group ) ) {
      throw new IllegalArgumentException( "Already a member of group" );
    }

    final Message event = new Message();
    event.setGroup( MessageSession.EVENT_SESSION_GROUP );
    event.add( MessageSession.ACTION_FIELD, MessageSession.JOIN_ACTION );
    event.add( MessageSession.GROUP_FIELD, group );
    event.add( MessageSession.POLICY_FIELD, capsule.toXml() );
    //fireJoin( group );

    // record the subscription in case we have to reconnect
    groups.put( group, capsule );
  }




  /**
   * Tell the remote systems that we no-longer want it to send us events with
   * the given group name.
   *
   * @param group Name of the group from which we wish to unsubscribe
   */
  public void leave( final String group ) {
    if ( groups.containsKey( group ) ) {
      final Message event = new Message();
      event.setGroup( MessageSession.EVENT_SESSION_GROUP );
      event.add( MessageSession.ACTION_FIELD, MessageSession.LEAVE_ACTION );
      event.add( MessageSession.GROUP_FIELD, group );
      send( event );

      groups.remove( group );
    }
  }




  /**
   * <p>
   * Tell the remote systems that currently we are unable to process the events
   * with the given group name. This could be due to some internal problems like
   * loss of database connectivity at the application end. This does not mean
   * that we are not interested in receving events. But this only means that at
   * this point of time the application cannot recieve but the application needs
   * the messages once the problem is sorted out.
   * </p>
   * <p>
   * This tells the remote system to block the messages on JMS server but not to
   * unsubscribe. JMS stores the messages in its data store until the messages
   * are expired. This method is recommended for use only in case of
   * durable/guaranteed delivery modes.
   * </p>
   *
   * @param group Name of the group from which we wish to unsubscribe
   */
  private void stopReceiving( final String group ) {
    final Message event = new Message();
    event.setGroup( MessageSession.EVENT_SESSION_GROUP );
    event.add( MessageSession.ACTION_FIELD, MessageSession.STOP_ACTION );
    event.add( MessageSession.GROUP_FIELD, group );
    send( event );
  }




  /**
   * Method initialize
   */
  private void initialize() {
    //MicroBus.log( "MessageSession dispatcher initializing" );

    //MicroBus.log( "Processing connection to " + socket.getInetAddress() + ":" + socket.getPort() );

    /*
    // If we have a reference to a socket channel, we must have been previously
    // connected and somehow lost our connection.
    if( socket != null )
    {
      // Reconnection logic - while no socket as a client
      while( socket.isClosed() )
      {
        MicroBus.log( "Attempting to reconnect to " + serviceUri );
    
        try
        {
          socketChannel = SocketChannel.open();
          socketChannel.configureBlocking( false );
          socketChannel.connect( new InetSocketAddress( serviceUri.getHost(), serviceUri.getPort() ) );
    
          // Create a new RemoteService object to track session state
          //remoteService = new RemoteService( this );
        }
        catch( final Exception e )
        {
          MicroBus.log( "Could not reconnect to " + serviceUri + " - " + e.getMessage() );
    
          // pause for a short time
          park( 6000 );
        }
      } // while socket !open
    */
    // We must be open at this point, get our streams
    try {
      dis = new DataInputStream( socket.getInputStream() );
      dos = new DataOutputStream( socket.getOutputStream() );
    } catch ( final Exception ex ) {
      //MicroBus.log( "Could not open streams to '" + socket + "' - " + ex.getMessage() );
      shutdown();

      return;
    }

    setOperational( true );
    //    }
    //    else
    //    {
    //      MicroBus.err( "Reference to socket channel was lost (null)" );
    //      shutdown();
    //
    //      return;
    //    }

    //MicroBus.log( "Connecting...sending magic" );

    // ...make sure we attempt to reconnect...
    restart = true;

    // ...send the DLE/SOH/ETB combination to start the session
    sendStart( dos );

    //MicroBus.log( "Firing connection call-back" );
    // Inform the client that a connection has taken place. It can then send
    // a login event, but it MUST return quickly so we can enter our doWork
    // loop and process the results of the re-connection
    //fireConnect();

    //    if( shutdown )
    //    {
    //      MicroBus.err( "Session initialization failed." );
    //    }
    //    else
    {
      // Now is a good time to resubmit any groups, except that some
      // groups may no-longer be valid (inboxes) and security at the
      // user level may prohibit it. So, let the application resubscribe.

      //MicroBus.log( "Session initialization complete" );
    }
  }




  /**
   * Determine if we are connected to an event service.
   * 
   * <p>This is a test to see if the session is running and the socke is open. 
   * It does not determine if any protocol negotiation has taken place or what 
   * the status of the connection is.</p>
   * 
   * <p><strong>NOTE: </strong> This is not thread-safe and will cause threading
   * problems. A thread-safe alternative is the isOperational call which does
   * nearly equivilent checks.</p>
   *
   * @return True if the session is running on an open socket, false if the
   *         session is not running or the socket is not open.
   */
  public boolean isConnected() {
    return true;// ( isActive() && socketChannel.isOpen() );
  }




  /**
   * Thread-safe call to get the operational state of the session. <p>When the session is operational, it is currently connected and reading events from and placing events on its transport. This is different from  the Active state, which implies ontly that the initialize method has been called at least once.</p>
   * @return  True if the session is initialized and connected, false if the  session is not connected, or in the state of retrying a lost  connection.
   */
  public boolean isOperational() {
    synchronized( opLock ) {
      return operational;
    }
  }




  /**
   * Thread-safe setting of the operational flag.
   * @param flag  True to set the event session in the operational state, false  otherwise.
   */
  private void setOperational( final boolean flag ) {
    synchronized( opLock ) {
      operational = flag;
    }
  }




  /**
   * Wait for the session to become operational.
   * 
   * <p>This is different from isActive() as active only implies the initialize
   * method has completed. Operational means the event session is actually in
   * the process of reading events from the session and not in a (re)connection
   * or disconnection phase.</p>
   *
   * @param timeout number of milliseconds to wait for the session to become
   *        operational.
   */
  public void waitForOperational( final long timeout ) {
    if ( !isOperational() ) {
      // determine the timeout sentinel value
      final long tout = System.currentTimeMillis() + timeout;

      // While we have not reached the sentinel time
      while ( tout > System.currentTimeMillis() ) {
        // wait on the operational lock object
        synchronized( opLock ) {
          try {
            opLock.wait( 10 );
          } catch ( final Throwable t ) {}
        }

        // if we are now active...
        if ( isOperational() ) {
          // ... break out of the time-out while loop
          break;
        }
      } // while time-out not reached
    } // if not operational
  }




  /**
   * @return  whether or not the current thread is set to shutdown.
   */
  public boolean isShutdown() {
    synchronized( opLock ) {
      return shutdown;
    }
  }




  /**
   * Return whether or not the thread has entered and is currently within the main run loop.
   * @return  True if the ThreadJob has been initialized and is cycling in the run  loop, False otherwise.
   */
  public boolean isActive() {
    synchronized( opLock ) {
      return active;
    }
  }




  /**
   * Called by the run() method when it enters and exits the main run loop.
   *
   * <p>When the thread enters the main run loop and is going to begin cycling
   * this method is called with true. It then notifies all threads that are
   * waiting for this object to be active via the waitForActive() method.</p>
   *
   * <p>This method is called again with false just prior to exiting the run()
   * method to set the active flag to false. No notifications are maid as the
   * join() method will notify all interested parties when this thread
   * exits.</p>
   *
   * @param flag The boolean value to set to the active flag.
   */
  protected void setActiveFlag( final boolean flag ) {
    synchronized( opLock ) {
      active = flag;

      if ( active ) {
        opLock.notifyAll();
      }
    }
  }




  /**
   * Suspend this object's thread of execution.
   */
  public void suspend() {
    synchronized( opLock ) {
      suspended = true;
    }
  }




  /**
   * @return  whether or not the current thread is in a suspended state.
   */
  public boolean isSuspended() {
    synchronized( opLock ) {
      return suspended;
    }
  }




  /**
   * This resumes this object's thread of execution.
   */
  public void resume() {
    synchronized( opLock ) {
      suspended = false;

      opLock.notifyAll();
    }
  }




  /**
   * This "parks" the execution of this thread.
   *
   * <p>When this object's <code>suspend()</code> method is called, the code
   * will proceed to a place where it can stop processing. It will then call
   * this method to wait until the <code>resume()</code> method is called or 
   * the given number of milliseconds expire.</p>
   *
   * @param timeout The number of milliseconds to wait.
   */
  protected void park( final long timeout ) {
    synchronized( opLock ) {
      if ( isShutdown() ) {
        // Cannot suspend if shutdown is pending
        if ( isSuspended() ) {
          resume();
        }
      } else {
        if ( current_thread != null ) {
          try {
            opLock.wait( timeout );
          } catch ( final InterruptedException x ) {
            current_thread.interrupt(); // re-throw
          }
        } else {}
      }
    }
  }




  public void run() {
    current_thread = Thread.currentThread();

    do {
      // Setup everything we need to run
      initialize();

      // set the active flag true only after we have initialized, so others can
      // use waitForActive to wait for us to initialize
      setActiveFlag( true );

      while ( !isShutdown() ) {
        // Do work
        doWork();

        // Yield to other threads
        Thread.yield();

        park( idle_wait_time );

        // Check to see if we should suspend execution
        if ( isSuspended() ) {
          // park for an indefinite period of time (resume will interrupt park)
          park( 0 );
        }

      } // if !shutdown

      // Clean up after ourselves, although we may restart
      terminate();
    }
    while ( restart );

    // We are no longer running active
    setActiveFlag( false );

  }




  /**
   * Continually reads from the input stream, creating frames and passing them
   * to the RemoteSession object for processing. 
   * 
   * <p>Then Packets are taken off the outbound queue of our MessageChannel and 
   * written to the transport via the RemoteService node that caches and 
   * sequences each frame.</p>
   */
  public void doWork() {
    // check what our peer has for us
    if ( dis != null ) {
      synchronized( dis ) {
        try {
          // If the connection is open
          if ( !socket.isClosed() ) {
            Packet frame = null;

            // While there is data in the socket buffer
            if ( dis.available() > 0 ) {
              // try to create a request message from the input stream
              try {
                // create the request
                frame = new Packet( dis );

                // process the received frame
                System.out.println( frame.toString() );
              } catch ( final Exception hme ) {
                //MicroBus.err( hme.toString() );
              }
            } else {
              if ( socket.isClosed() ) {
                //MicroBus.log( "Peer from '" + socket + "' closed the connection" );

                // just exit the loop and let's restart
              }
            }
          } else {
            //MicroBus.log( "Lost connection to '" + serverUri + "', reconnecting" );
          }
        } catch ( final IOException ioe ) {
          // TODO get rid of this
          ioe.printStackTrace();
        } catch ( final Exception e ) {
          // TODO get rid of this
          e.printStackTrace();
        }
      }
    }

    try {
      // Now check to see of we have anything to send out to the peer
      // If we have any messages in our outbound queue//
      if ( messageChannel.outboundDepth() > 0 ) {
        // Make sure we don't go idle for another Idle Timeout interval
        //super.rev();

        Message event = null;

        synchronized( messageChannel ) {
          try {
            // try to get the next message, but only wait for 5 milliseconds
            event = messageChannel.getNextOutbound( 5 );
          } catch ( final Exception e ) {}
        }

        // if we have a message...
        if ( event != null ) {
          // Have the RemoteService object package, cache and send the event
          // returning the identifier of the event
          //remoteService.send( event );
        }

      } // if messages in the outbound queue

    } catch ( final RuntimeException e ) {
      // TODO get rid of this
      e.printStackTrace();
    }
  }




  /**
   * Send the Data Link Escape (DLE), Start Of Header (SOH) and End of Transmit
   * Block (ETB) octet sequence to start the session.
   * 
   * <p>This will inform the service that a session is about to begin</p>
   *
   * @param dout The output stream to use to send the initialization sequence
   */
  private void sendStart( final DataOutputStream dout ) {
    try {
      dout.write( 16 ); // Data Link Escape
      dout.write( 01 ); // Start Of Header
      dout.write( 23 ); // End of Transmit Block
      dout.flush(); // flush the data to the remote end

      // give the other end time to digest the sequence
      try {
        Thread.sleep( 5 );
      } catch ( final InterruptedException ignore ) {}
    } catch ( final IOException e ) {
      //MicroBus.err( "Could not send session start sequence: " + e.getMessage() );
      shutdown();
    }
  }




  /**
   * Method shutdown.
   * 
   * <p>This WILL NOT stop the thread from running unless the disconnect method 
   * has been called. It will only cause the terminate method to be called and 
   * exit the main run loop. The ThreadJob.restart flag will then be checked, 
   * and if set to true, will cause the initialize method to be called again 
   * and the main run loop will be re-entered.</p>
   */
  public void shutdown() {
    //super.shutdown();
    //MicroBus.log( "MessageSession shutdown" );
  }




  /**
   * Method terminate
   */
  public void terminate() {
    //MicroBus.log( "terminate: terminating connection" );
    setOperational( false );

    // We are shutting down, remove ourselves from the PacketManager
    //if( eventManager != null )      eventManager.removeChannel( messageChannel );

    if ( dis != null ) {
      try {
        dis.close();
      } catch ( final Exception ex ) {}
      finally {
        dis = null;
      }
    }

    if ( dos != null ) {
      try {
        dos.close();
      } catch ( final Exception ex ) {}
      finally {
        dos = null;
      }
    }

    // close socket
    if ( socket != null ) {
      try {
        socket.close();
      } catch ( final Exception ex ) {}
      finally {
        //if( restart == false )
        {
          socket = null;
        }
      }
    }

    //MicroBus.log( "MessageSession terminated" );
  }




  public void send( final Message packet ) {

  }




  /**
   * Write the given frame on the transport medium.
   * 
   * <p>NOTE: This method is purely I/O and does not populate the headers of 
   * the frame. Packet sequencing and the like should be already applied. It is
   * expected that the given Packet will be properly populated according 
   * to the rules of the protocol this frame is transporting.</p>
   *
   * @param packet The packet to send on the the network medium.
   */
  public void send( final Packet packet ) {
    if ( packet != null ) {
      // Make sure we have a source address
      if ( packet.getSourceAddress() == null ) {
        if ( packet.message != null ) {
          // set the source address of the event to our local address | port |
          // endpoint | channel
          packet.message.setSource( new MessageAddress( socket.getLocalAddress(), socket.getLocalPort(), 0, -1 ) );
        }
      }

      // send the frame
      try {
        dos.write( packet.getBytes() );
        dos.flush();
      } catch ( final IOException e ) {
        //MicroBus.err( "Could not send frame: " + e.getMessage() );
      }
    }
  }




  /**
   * Get the reference to the event channel this session uses to handle inbound and outbound events.
   * @return  The reference to this session's MessageChannel
   */
  public MessageChannel getMessageChannel() {
    return messageChannel;
  }




  /**
   * @return  The client identifier for this session.
   */
  public String getClientId() {
    return clientId;
  }




  /**
   * Method getConnectionTime
   *
   * @return TODO Complete Documentation
   */
  public long getConnectionTime() {
    return 0;//socketChannel.getConnectionUpTime();
  }




  /**
   * @return TODO Complete Documentation
   */
  public long getConnectedTime() {
    return 0;//socketChannel.getConnectedTime();
  }




  /**
   * Set the client identifier for this
   * @param  string
   */
  public void setClientId( final String string ) {
    clientId = string;

    messageChannel.setClientId( clientId );
  }




  /**
   * Process the event that was received on our private group.
   * 
   * <p>The protocol for private events is simple. Each private event must 
   * contain an action string in the action field and any number of arguments 
   * relating to that action.</p>
   *
   * @param event The event to process.
   */
  private void processPrivatePacket( final Message event ) {
    //MicroBus.log( "processPrivatePacket: event: " + event.toXml() );

    final String action = event.getField( MessageSession.ACTION_FIELD ).getObjectValue().toString();
    //MicroBus.log( "processPrivatePacket: checking: " + action );

    if ( MessageSession.JOIN_ACTION.equals( action ) ) {
      final String group = event.getField( MessageSession.GROUP_FIELD ).getObjectValue().toString();
      // DataCapsule qual = new DataCapsule( (String)event.getObject( POLICY_FIELD ) );

      if ( group != null ) {
        // if ( qual != null && qual instanceof Message)
        //if( qual != null )
        //{
        //  // DataCapsule cap = MessageUtil.eventToCapsule((Message)qual);
        //  messageChannel.addSubscriptionQualities( group, qual );
        //}

        messageChannel.join( group );
        //MicroBus.log( "received a request to join '" + group + "'" );
      }
    } else if ( MessageSession.LEAVE_ACTION.equals( action ) ) {
      final String group = event.getField( MessageSession.GROUP_FIELD ).getObjectValue().toString();
      if ( group != null ) {
        messageChannel.leave( group );
        //MicroBus.log( "received a request to leave the '" + group + "' group" );
      }
    } else if ( MessageSession.STOP_ACTION.equals( action ) ) {
      final String group = event.getField( MessageSession.GROUP_FIELD ).getObjectValue().toString();
      if ( group != null ) {
        // messageChannel.stopReceiving( group );
        //MicroBus.log( "received a request to stop subscription from '" + group + "'" );
      }
    } else if ( MessageSession.SETID_ACTION.equals( action ) ) {
      final String id = event.getField( MessageSession.CLIENTID_FIELD ).getObjectValue().toString();
      if ( id != null ) {
        clientId = id;

        messageChannel.setClientId( clientId );

        //MicroBus.log( "remote end set our client id to '" + id + "'" );
      }
    } else {
      //MicroBus.log( "processPrivatePacket: Unknown action: '" + action + "'" );
    }
  }




  /**
   * Make sure the remote end uses the same client identifier we are using.
   */
  void syncClientId() {
    //MicroBus.log( "sending client identifier of '" + clientId + "'" );

    if ( ( socket != null ) && !socket.isClosed() ) {
      final Message event = new Message();
      event.setGroup( MessageSession.EVENT_SESSION_GROUP );
      event.add( MessageSession.ACTION_FIELD, MessageSession.SETID_ACTION );
      event.add( MessageSession.CLIENTID_FIELD, clientId );
      send( event );
    }
  }




  /**
   * The remote end of the connection.
   */
  public URI getRemoteUri() {
    return null; // socketChannel.getRemoteURI();
  }
}