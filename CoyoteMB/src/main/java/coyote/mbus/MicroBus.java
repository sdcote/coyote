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
package coyote.mbus;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.channels.ServerSocketChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import coyote.commons.ExceptionUtil;
import coyote.commons.UriUtil;
import coyote.commons.network.IpAddress;
import coyote.commons.network.IpAddressException;
import coyote.commons.network.IpInterface;
import coyote.commons.network.IpNetwork;
import coyote.mbus.message.Message;
import coyote.mbus.message.MessageAddress;
import coyote.mbus.network.MessageBus;
import coyote.mbus.network.MessageChannel;
import coyote.mbus.network.MessageService;
import coyote.mbus.network.NetworkService;
import coyote.mbus.network.NetworkServiceException;
import coyote.mbus.network.OamMessage;
import coyote.mbus.network.RemoteNode;


/**
 * The this class models a set of connectivity services for the exchange of 
 * messages across the network.
 */
public class MicroBus implements MessageSink {
  /** String used in various locations to identify this class. */
  public static final String CLASS_TAG = "MicroBus";

  /** The component that runs in its own thread to handle network IO. */
  private final NetworkService SERVICE = new NetworkService();

  /** The UDP bus we use to pass messages asynchronously. */
  private MessageBus messageBus = null;

  /** The TCP service we use to support bridges to other subnets. */
  private MessageService messageService = null;

  /** The channel on which our asynchronous messages are passed. */
  private MessageChannel busChannel;

  /** The channel on which our asynchronous messages are passed. */
  private MessageChannel publicChannel;

  /** 
   * Name of the property that is used to determine the port on which this 
   * this NetworkService listens for messages. This is considered the default bus.
   */
  public static final String PORT_TAG = "mbus.port";
  public static final String ADDR_TAG = "mbus.addr";
  public static final String MASK_TAG = "mbus.mask";

  /** The port on which we communicate */
  private int bindPort = 7943;

  /**
   * The IP address to which we are to bind
   */
  private IpAddress bindAddress = null;

  /**
   * The network mask to use for broadcast address calculations
   */
  private IpAddress netMask = null;

  /** The next sequential channel identifier to use (0=endpoint, 1+=clients) */
  private volatile int nextChannelID = 0;

  /**
   * The list of all the MessageChannels we are to service when messages are 
   * received.
   */
  protected List<MessageChannel> channelList = new ArrayList<MessageChannel>();

  private ChannelWatcher channelWatcher;

  /** */
  private volatile boolean shutdown = false;

  /** */
  public long EXPIRATION_INTERVAL = 60000;

  /** */
  public MessageSink NULL_SINK = null;

  /** The object we use to append log messages */
  private LogAppender LOG = new DefaultLogAppender( System.out );

  /** Error messages are logged here */
  private LogAppender ERR = new DefaultLogAppender( System.err );

  private volatile boolean busIsOpen = false;




  /**
   * This is where we initialize ourselves.
   */
  public MicroBus() {
    final MicroBus mbus = this;

    // Share our lo appenders with the network service
    SERVICE.setLogAppender( LOG );
    SERVICE.setErrorAppender( ERR );

    // Add a shutdown hook into the JVM to help us shut everything down nicely
    try {
      Runtime.getRuntime().addShutdownHook( new Thread( "mBusShutdown" ) {
        public void run() {
          synchronized( MicroBus.CLASS_TAG ) {
            mbus.shutdown();
          }
        }
      } );
    } catch ( final Throwable ignoree ) {}

    // Create a reusable null message sink;
    NULL_SINK = new NullBus();

    // instruct the network service to block for 1 second on selection checks
    SERVICE.SELECT_WAIT_TIME = 1000;

    // We always run the service in its own thread because we will always want
    // housekeeping to run even if we are a private bus
    final Thread netThread = new Thread( SERVICE );
    netThread.setName( "mBus" );

    // Run the network service thread as a daemon-level thread so the VM will
    // exit if this is the only thread running
    netThread.setDaemon( true );

    // create a channel that will be used to send all data to the network bus
    busChannel = new MessageChannel( this );

    // create a closed bus by default
    busChannel.outSink = NULL_SINK;

    // Start the service running, although there are no handlers at this point
    netThread.start();

    // Create a public channel that anyone can use to send() messages through
    publicChannel = this.createChannel( new NullSink() );

  }




  /**
   * Permanently terminate the service for all components in the runtime.
   * 
   * <p>Once shut-down the service can not be restarted.</p>
   */
  public void shutdown() {

    final StringBuffer line = new StringBuffer( "This is NOT an error, simply a diagnostic message showing what called this method\n" );
    line.append( "Thread \"" + Thread.currentThread().getName() + "\" is in the current call stack:\n" );
    final StackTraceElement[] stack = new Exception().fillInStackTrace().getStackTrace();

    for ( int x = 0; x < stack.length; x++ ) {
      final StackTraceElement elem = stack[x];
      line.append( stack.length - x );
      line.append( " - " );
      line.append( ExceptionUtil.getLocalJavaName( elem.getClassName() ) );
      line.append( "." );
      line.append( elem.getMethodName() );
      line.append( "():" );

      if ( elem.getLineNumber() < 0 ) {
        line.append( "Native Method" );
      } else {
        line.append( elem.getLineNumber() );
      }

      if ( x + 1 < stack.length ) {
        line.append( "\n" );
      }
    }

    LOG.append( line.toString() );

    synchronized( SERVICE ) {
      if ( !shutdown ) {
        // place all the channels in a closed state, this places closure objects
        // in the inbound queues which signals the inbound sinks to shutdown.
        for ( int i = 0; i < channelList.size(); ( ( (MessageChannel)channelList.get( i++ ) ) ).close() );

        // wait for the bus to receive its closure object via its message channel
        if ( messageBus != null ) {
          messageBus.shutdown();
          messageBus.join( 5000 );
        }

        shutdown = true;
        try {
          // The service can not be restarted once it's shut down 
          SERVICE.destroy();

          messageBus = null;
          busChannel.destroy();
        } catch ( final Throwable ignore ) {}

      } // if !shutdown

    } //sync

  }




  /**
   * Add the given channel to the list of channels managed by this fixture.
   *  
   * <p>Channels that are added to the fixture will be included in all routing 
   * operations.</p>
   *
   * @param channel The channel to manage.
   */
  void addChannel( final MessageChannel channel ) {
    // Add the given channel to the list of managed channels
    synchronized( channelList ) {
      channel.setChannelId( nextChannelID++ );
      channelList.add( channel );
      channel.addListener( channelWatcher );
    }
  }




  /**
   * Create a channel that can send and receive data with other channels using 
   * the given sink as the direct call-back to process all incoming data.
   * 
   * <p>The result is a channel that will have its inbound data passed directly 
   * to the sink and processed in the senders thread of execution. This means 
   * that the thread sending the data through the channel will also invoke the 
   * call-back method of the sink and execute the logic there.</p>
   * 
   * <p>Care should be taken to not implement too much logic in the call-back
   * method as it <strong>will</strong> slow processing. Consider decoupling 
   * the sink from the returned channel and process the incoming data in a 
   * separate thread if possible.</p>
   * 
   * @param inbound The MessageSink which will handle incoming messages.
   * 
   * @return A channel on which the client component can exchange data.
   */
  public MessageChannel createChannel( final MessageSink inbound ) {

    MessageChannel retval;

    // if there is a message bus component handling network traffic, create the 
    // channel within it otherwise, just create an 'unattached' channel.
    if ( messageBus != null ) {
      retval = messageBus.createChannel( nextChannelID++ );
    } else {
      retval = new MessageChannel();
    }

    // wire the given inbound sink to the given argument
    retval.inSink = inbound;

    // wire the outbound messages sink to this fixture so data can be routed
    retval.outSink = this;

    // Add the new channel to the list of managed channels. It is not added 
    // with the addChannel method because the channel identifier is set here.
    synchronized( channelList ) {
      channelList.add( retval );
    }

    // register our channel watcher as a listener to the returned channel
    retval.addListener( channelWatcher );

    return retval;
  }




  /**
   * Create a channel that will be included in routing operations but has no
   * direct inbound sink.
   * 
   * <p>A NullSink is used to ignore inbound messages. The caller is expected 
   * to set an inbound sink to process messages when it is ready to process 
   * incoming messages via <code>MessageChannel.setInSink(MessageSink)</code>.
   * This results in a handy way to start processing messages later on as 
   * opposed to begin processing messages immediately upon creation of the 
   * channel.</p>
   *
   * @return Return a managed channel with a 'null' inbound sink.
   */
  public MessageChannel createChannel() {
    return createChannel( new NullSink() );
  }




  /**
   * Send the message across the bus using the specified group.
   * 
   * <p>If the group is not null and not empty it will be placed in the message
   * overriding any existing group specified therein.</p>
   * 
   * @param msg The value object to send
   * @param group The name of the message group on the message should be sent 
   * 
   * @throws IllegalArgumentException if no message group is set in the message
   */
  public void send( Message msg, String group ) {
    // if a group was specified, use place it in the message
    if ( group != null && group.trim().length() > 0 )
      msg.setGroup( group.trim() );

    // Mak sure there is a group specified in the message before sending it
    if ( msg.getGroup() == null || msg.getGroup().trim().length() == 0 )
      throw new IllegalArgumentException( "No group specified for message" );

    // Send the message on the public channel
    publicChannel.send( msg );
  }




  /**
   * Send the message across the bus.
   * 
   * <p>It is assumed the message contains the group in which it should be 
   * sent.</p>
   * 
   * @param msg The value object to send
   * 
   * @throws IllegalArgumentException if no message group is set in the message
   */
  public void send( Message msg ) {
    // send the message as it is with no group override
    this.send( msg, null );
  }




  /**
   * <strong>Experimental</strong> Create a channel where the given sink will 
   * be working with other sinks in a coordinated group where only one of the 
   * sinks will receive the message for processing.
   * 
   * <p>A queue channel is a special channel that tracks its sinks and issues 
   * heartbeats with other queue channels to negotiate a manager to assign 
   * messages to identified sinks.</p>
   * 
   * @param inbound The MessageSink which will handle incoming messages.
   * 
   * @return a message queue
   */
  public MessageQueueChannel createQueueChannel( final MessageSink inbound ) {
    return new MessageQueueChannel( createChannel( null ) );
  }




  /**
   * This fixture will receive messages from all the MessageChannels it creates
   * so when MessageChannel.send(Message) is called, this method is called to
   * route the message.
   * 
   * <p>All message delivery is accomplished in the calling thread. This means 
   * the call will not return until the <tt>onMessage(Message)</tt> method of
   * all the appropriate listeners have been called and returned. Any delay in 
   * processing will be incurred by the calling thread.</p> 
   * 
   * @see coyote.mbus.MessageSink#onMessage(coyote.mbus.message.Message)
   */
  public void onMessage( final Message message ) {
    if ( shutdown ) {
      throw new IllegalStateException( "Can not handle message, this is in a shutdown state" );
    }

    // send it over the bus channel over the network or through loopback
    if ( message.sourceChannel != busChannel ) {
      busChannel.send( message );
    }

    // Get the target address to see if it is a directed message
    final MessageAddress target = ( message ).getTarget();

    if ( target != null ) {
      LOG.append( "Received targeted message: " + target.toString() );

      // This is a directed message ignore it unless it is for this endpoint
      // if the endpoint is negative
      if ( ( messageBus != null ) && ( target.getEndPoint() == messageBus.endpoint ) ) {
        LOG.append( "Message targeted for this endpoint" );

        if ( target.getChannelId() > -1 ) {
          LOG.append( "Message targeted for channel " + target.getChannelId() );

          for ( int i = 0; i < channelList.size(); i++ ) {
            final MessageChannel channel = (MessageChannel)channelList.get( i );
            if ( target.getChannelId() == channel.getChannelId() ) {
              channel.receive( message );
              return;
            }
          }
          ERR.append( "Could not deliver directed message to " + target + " - channel " + target.getChannelId() + " does not exist" );
        } else {
          LOG.append( "Message targeted for all channels in this endpoint" );
          for ( int i = 0; i < channelList.size(); i++ ) {
            final MessageChannel channel = (MessageChannel)channelList.get( i );
            if ( channel.matchGroup( message.getGroup() ) && ( channel != ( message ).sourceChannel ) ) {
              channel.receive( (Message)message.clone() );
            }

          } // for each channel

        } // channelId > -1

      } // not our endpoint

    } // Target !null
    else {
      LOG.append( "Message has no target passing to all (" + channelList.size() + ") channels in the list" );
      // This is a group message, so route it to all group members. It does not 
      // need to be synchronized, because the list is not being changed, only 
      // accessed.        
      for ( int i = 0; i < channelList.size(); i++ ) {
        final MessageChannel channel = (MessageChannel)channelList.get( i );

        // send the message only to those channels interested in the group and 
        // those that are not the origination of the message
        if ( channel.matchGroup( message.getGroup() ) && ( channel != message.sourceChannel ) ) {
          // Create a copy of the Message and place it in the channel
          channel.receive( (Message)message.clone() );
        }

      } // for each channel

    } // if targeted or group

  }




  /**
   * @return the number of messages still in the outbound message queue
   */
  public int getOutboundQueueDepth() {
    if ( messageBus != null ) {
      return messageBus.getOutboundQueueDepth();
    }
    return -1;
  }




  /**
   * Access a mutable list of Remote Nodes.
   * 
   * @return the current list of remote nodes. 
   */
  List<RemoteNode> getRemoteNodes() {
    final ArrayList<RemoteNode> retval = new ArrayList<RemoteNode>();
    if ( messageBus != null ) {
      for ( final Iterator<RemoteNode> it = messageBus.getRemoteNodeIterator(); it.hasNext(); retval.add( it.next() ) );
    }
    return retval;
  }




  /**
   * @return Return a list of messages that describe the currently known remote
   *         nodes
   */
  public List<Message> getRemoteNodeMessages() {
    final ArrayList<Message> retval = new ArrayList<Message>();
    if ( messageBus != null ) {
      for ( final Iterator<RemoteNode> it = messageBus.getRemoteNodeIterator(); it.hasNext(); ) {
        final Message nodeMessage = OamMessage.createNodePacket( it.next() );
        if ( nodeMessage != null ) {
          retval.add( nodeMessage );
        }
      }
    }
    return retval;
  }




  /**
   * @return  True if the this framework is not operational for any reason;   false indicates messages are being exchanged.
   */
  public boolean isShutdown() {
    return shutdown;
  }

  //

  //

  //

  //

  //

  /**
   * The NullBus class models a private bus that silently consumes messages. It 
   * is used in place of a MessageBus object to keep the code clean. This is an 
   * implementation of the Null Object design pattern, a.k.a. Active Nothing 
   * and Stub pattern.
   */
  class NullBus implements MessageSink {
    /** Default constructor */
    NullBus() {}




    /**
     * Silently consume the message.
     * 
     * @see coyote.mbus.MessageSink#onMessage(coyote.mbus.message.Message)
     */
    public void onMessage( final Message message ) {
      // silently ignore the message
      LOG.append( "NullBus (not so silently) consuming message" );
    }

  }




  /**
   * @return the NetworkService running the network services.
   */
  NetworkService getLocalNode() {
    return SERVICE;
  }




  /**
   * Wait for the Bus to insert and become ready for operation.
   * 
   * <p>This will make sure the service running and the bus component is read 
   * and valid for I/O operations.</p>
   *  
   * <p>It is usually a good idea to call this at least once in an applications 
   * initialization as it will ensure the bus is operational before being used.
   * Its use is entirely optional.</p>
   * 
   * @param millis How long to wait for the bus to become operational.
   */
  public void waitForBus( final long millis ) {
    final long end = System.currentTimeMillis() + millis;

    // The service thread should always be running in the background
    SERVICE.waitForActive( millis );

    final long remain = end - System.currentTimeMillis();
    if ( remain > 0 ) {
      // determine the timeout sentinel value
      final long tout = System.currentTimeMillis() + remain;

      // While we have not reached the sentinel time
      while ( tout >= System.currentTimeMillis() ) {
        // If we have a Bus and it is ready, break
        if ( ( messageBus != null ) && messageBus.isReady() ) {
          break;
        }

        // wait around for a while
        synchronized( this ) {
          try {
            this.wait( 10 );
          } catch ( final Throwable ignore ) {}
        }
      } // while !timed-out
    } // if there is time left after waiting for the SERVICE to init
  }




  public boolean isReady() {
    if ( messageBus != null ) {
      // return the readiness of the bus
      return messageBus.isReady();
    }

    // No Bus, we are not ready
    return false;
  }




  /**
   * Enable or disable the generation of diagnostic console messages.
   * 
   * @param flag True enables diagnostic messages, false disables messages to 
   *        the console
   */
  public synchronized void enableLogging( final boolean flag ) {
    LOG.setEnabled( flag );
    ERR.setEnabled( flag );
  }




  /**
   * Add a network specification to this nodes Access Control List (ACL) with 
   * the given allowance.
   * 
   * <p>This controls what TCP connections will be accepted by this node by 
   * allowing or denying remote IP addresses based upon the network rules in 
   * this list. Each rule entry represents a network specification with either 
   * an <tt>allow</tt> value of true for allowing the connection or false 
   * effectively denying the connection.</p>
   * 
   * <p>The size of the network can be small (i.e. /32), specifying just one 
   * host, or it can be large (i.e. /8) specifying many hosts. This gives the 
   * operator reasonable flexibility in controlling TCP access to the node.</p>
   * 
   * <p>Order is extremely important when defining an ACL. Each ACL entry is
   * matched against the address of the incoming connection in same order the 
   * rule was added to the list. If the rule matches the entry then the ACL 
   * will return the <tt>allows</tt> value. If no rule matches the address, 
   * then the default rule will be returned. This means if an IP address is 
   * allowed by rule #5, but is disallowed by rule #22, the IP address will be 
   * allowed because rule #5 allowed it and checking stopped before rule #22 
   * was reached.</p>
   * 
   * <p>The default rule is to deny all TCP connections and with an empty ACL,
   * no TCP connections will be accepted from any host.</p>
   * 
   * @param network The network specification to add.
   * @param allowed The flag indicating whether or not TCP connections from the 
   *        specified network will be accepted.
   */
  public void addAclEntry( final IpNetwork network, final boolean allowed ) {
    messageService.addAclEntry( network, allowed );
  }




  public IpAddress getTcpAddress() {
    waitForBus( 1000 );
    try {
      return new IpAddress( messageService.getAddress() );
    } catch ( final IpAddressException e ) {
      return IpInterface.getPrimary().getAddress();
    }
  }




  public int getTcpPort() {
    if ( messageBus != null ) {
      waitForBus( 1000 );
      return messageService.getPort();
    } else {
      return -1;
    }
  }




  public long getEndpoint() {
    if ( messageBus != null ) {
      waitForBus( 1000 );
      return messageBus.endpoint;
    } else {
      return -1;
    }
  }




  public void finalize() throws Throwable {
    shutdown = true;
    try {
      SERVICE.destroy();
      messageBus = null;
      busChannel.destroy();
      busChannel = null;
      messageService = null;
    } catch ( final Throwable ignore ) {}

  }




  /**
   * Opens the message passing service to the network.
   * 
   * <p>This will cause this node to generate a new endpoint identifier.</p>
   */
  public synchronized void open() {

    // If this is not open yet
    if ( !busIsOpen ) {
      busIsOpen = true;

      LOG.append( "Opening bus..." );

      // if the bus has not been opened previously
      if ( busChannel.outSink instanceof NullBus ) {
        // create network facilities...
        if ( ( bindAddress == null ) || ( netMask == null ) ) {
          // Get the primary interface to which we should bind our services
          final IpInterface ipInterface = IpInterface.getPrimary();
          LOG.append( "BindAddr:" + bindAddress + " NetMask:" + netMask + " - using a primary interface of " + ipInterface );

          if ( ipInterface == null ) {
            ERR.append( "Could not get a default interface on this host, will attempt to find the address of this host via DNS" );
            try {
              bindAddress = new IpAddress( InetAddress.getLocalHost() );
            } catch ( final IpAddressException e ) {
              // should not happen but complain just in case it does
              ERR.append( "Could not find this hosts address via DNS: " + e.getMessage() + " - Trying the loopback address" );
            } catch ( final UnknownHostException e2 ) {
              try {
                bindAddress = new IpAddress( "127.0.0.1" );
              } catch ( final IpAddressException e ) {
                ERR.append( "Could not resolve the loopback address to this host; networking appears to be disabled - " + e.getMessage() );
              }
            }

          } else {
            LOG.append( "BindAddr:" + bindAddress + " NetMask:" + netMask + " - using a primary interface of " + ipInterface );

            bindAddress = ipInterface.getAddress();
          }

          if ( netMask == null ) {
            if ( bindAddress != null ) {
              final IpInterface ipi = IpInterface.getInterface( bindAddress );
              if ( ipi != null ) {
                netMask = ipi.getNetmask();
              } else {
                ERR.append( "Could not retrieve the network interface for the address of " + bindAddress + " using a default mask" );
              }
            }

            // If all else fails, use the default
            if ( netMask == null ) {
              try {
                netMask = new IpAddress( MessageBus.DEFAULT_NETMASK );
              } catch ( final IpAddressException e ) {
                // should not happen
                e.printStackTrace();
              }

            } // mask is still null

          } // mask = null

        } // if addr or mask is null

        // Create a Service handler
        final ServerSocketChannel ssc = NetworkService.getNextServerSocket( bindAddress.toInetAddress(), bindPort );

        if ( ssc != null ) {
          LOG.append( "Using TCP service of " + ssc.socket().getLocalSocketAddress() );
          messageService = new MessageService( ssc );
          LOG.append( "messageService=" + messageService.getServiceUri() );
        } else {
          ERR.append( "Problems obtaining a server socket channel - " + bindAddress + " on port " + bindPort );
        }

        LOG.append( "Adding TCP to service" );

        if ( SERVICE.isActive() ) {
          // add the TCP handler to the already active thread.
          try {
            SERVICE.add( messageService );
          } catch ( final NetworkServiceException e ) {
            ERR.append( "Problems adding TCP service to currently active node: " + e.getMessage() );
          }
        } else {
          LOG.append( "Staging TCP in service for later startup" );

          // Stage the TCP service so it is ready to initialize
          SERVICE.stage( messageService );
        }

        LOG.append( "initializing bus handler" );

        // Set the Bus Channel address to reflect the started TCP service so the
        // heartbeat messages will reflect this nodes TCP address
        busChannel.setAddress( new MessageAddress( ssc.socket().getInetAddress(), ssc.socket().getLocalPort(), 0L, nextChannelID++ ) ); // should be channel 0 (zero)

        // Create a Bus listening to the given URI that places all incoming 
        // messages on the given channel. 
        // MUST use the scheme of UDP to ensure a datagram socket will be used
        messageBus = new MessageBus( busChannel, UriUtil.parse( "udp://" + bindAddress.toString() + ":" + bindPort ) );

        // Make sure that the message bus uses the same log appenders we do.
        messageBus.setLogAppender( LOG );
        messageBus.setErrorAppender( ERR );

        // setup a dependency on the this service
        messageBus.setTcpService( messageService );

        // If we have a good netmask, set it here so our messages will be routed
        // to the entire subnet and not blocked at the first router interface
        if ( netMask != null ) {
          messageBus.setNetmask( netMask.toString() );
        }
        LOG.append( "Bus broadcast address is " + messageBus.getBcastaddr() );

        LOG.append( "Including bus handler " + messageBus + " in node" );

        if ( SERVICE.isActive() ) {
          LOG.append( "Adding bus handler to service" );
          // add the UDP handler to the already active thread.
          try {
            SERVICE.add( messageBus );
          } catch ( final NetworkServiceException e ) {
            ERR.append( "Problems adding UDP service to currently active node: " + e.getMessage() );
          }
        } else {
          LOG.append( "Staging bus handler in service for later startup" );

          // Stage the bus in the network service thread
          SERVICE.stage( messageBus );
        }

      } // if NullBus

      LOG.append( "Initial open call completed" );

    } // if opencount == 0
    else {
      ERR.append( "mBus already open" );
    }

  }




  /**
   * Closes the node off from the network.
   * 
   * <p>Messages are still passed within the runtime, but messages remain 
   * local.</p>
   */
  public synchronized void close() {

    // If we have been closed as many times as we have been opened...
    if ( busIsOpen ) {
      LOG.append( "Terminating services" );

      // Stop and remove the TCP service
      if ( messageService != null ) {
        messageService.shutdown();
        try {
          SERVICE.removeHandler( messageService );
        } catch ( final NetworkServiceException e ) {
          LOG.append( "Problems closing TCP service: " + e.getMessage() );
        }

        messageService = null;
      }

      if ( busChannel != null ) {
        busChannel.outSink = NULL_SINK;
      }

    }
  }




  public void openBridge( final IpAddress addr, final int port ) {
    // TODO open a bridge to the given address and port
  }




  public void closeBridge( final IpAddress addr, final int port ) {
    // TODO close the bridge to the given address and port
  }




  public void closeBridges( final IpAddress addr ) {
    // TODO close all bridges to the given address
  }




  /**
   * Set the this node to bind to a particular port.
   * 
   * @param port The IP port to use for communications.
   * 
   * @throws IllegalStateException if the node has already been opened.
   * @throws IllegalArgumentException if the argument is out of range (0-65534)
   */
  public void setPort( final int port ) {
    if ( busIsOpen ) {
      throw new IllegalStateException( "this is already open, cannot set port" );
    }

    if ( ( port < 0 ) || ( port > 0xFFFF ) ) {
      throw new IllegalArgumentException( "Port argument is out of range (0-65534)" );
    }

    bindPort = port;
  }




  /**
   * @param addr  the bindAddress to set
   */
  public void setBindAddress( final IpAddress addr ) {
    bindAddress = addr;
  }




  /**
   * @param addr  the netMask to set
   */
  public void setNetMask( final IpAddress addr ) {
    netMask = addr;
  }

  //

  //

  // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
  // Inner Classes
  // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

  // 

  /**
   * The ChannelWatcher class performs basic functions when channels do things.
   */
  class ChannelWatcher implements MessageChannelListener {
    /**
     * @see coyote.mbus.MessageChannelListener#channelConnect(coyote.mbus.network.MessageChannel)
     */
    public void channelConnect( final MessageChannel channel ) {}




    /**
     * @see coyote.mbus.MessageChannelListener#channelDisconnect(coyote.mbus.network.MessageChannel)
     */
    public void channelDisconnect( final MessageChannel channel ) {}




    /**
     * @see coyote.mbus.MessageChannelListener#channelReceive(coyote.mbus.network.MessageChannel)
     */
    public void channelReceive( final MessageChannel channel ) {}




    /**
     * @see coyote.mbus.MessageChannelListener#channelSend(coyote.mbus.network.MessageChannel)
     */
    public void channelSend( final MessageChannel channel ) {}




    public void channelStop( final String group, final MessageChannel channel ) {}




    /**
     * @see coyote.mbus.MessageChannelListener#channelJoined(java.lang.String, coyote.mbus.network.MessageChannel)
     */
    public void channelJoined( final String group, final MessageChannel channel ) {}




    /**
     * @see coyote.mbus.MessageChannelListener#channelLeft(java.lang.String, coyote.mbus.network.MessageChannel)
     */
    public void channelLeft( final String group, final MessageChannel channel ) {}

  } // class ChannelWatcher

}
