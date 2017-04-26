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

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.URI;
import java.net.UnknownHostException;
import java.nio.channels.ClosedSelectorException;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.util.ArrayList;

import coyote.commons.UriUtil;
import coyote.commons.network.IpInterface;
import coyote.mbus.LogAppender;
import coyote.mbus.NullLogAppender;


/**
 * A facility to operate as a single-threaded, non-blocking network server.
 *  
 * <p>This class handles the selector, and listens for activity in a single, 
 * non-blocking thread. When I/O activity occurs, this class figures out who is 
 * interested in what has just happened and hands off operation to that 
 * object.</p>
 */
public class NetworkService implements Runnable {
  /** Object we use to synchronize our operations at the method level. */
  protected Object mutex = new Object();

  /** Reference to our current thread of execution */
  protected Thread current_thread = null;

  /** The object we use to synchronize our running flag operations */
  protected Object activeLock = new Object();

  /**
   * Flag indicating if we have entered and are currently active within the 
   * main run loop
   */
  protected volatile boolean active = false;

  /** Indicates we have been asked to stop processing and shutdown */
  protected volatile boolean shutdown;

  /** 
   * Flag indicating that this object should re-initialize and start running 
   * again after shutdown.
   */
  protected volatile boolean restart = false;

  /** The selector used to sense when IO is ready to occur. */
  private Selector selector;

  /** The amount of time to wait during a selection (ms) */
  public int SELECT_WAIT_TIME = 10;

  /**
   * The size of the buffer used to read incoming datagrams must be large enough
   * to contain several datagram packets.
   */
  public static int DATAGRAM_RECEIVE_BUFFER_SIZE = 131072;

  /**
   * The size of the buffer used to send outgoing datagrams this is also the
   * largest message size that can be sent via UDP.
   */
  public static int DATAGRAM_SEND_BUFFER_SIZE = 65536;

  private final ArrayList<NetworkServiceHandler> handlers = new ArrayList<NetworkServiceHandler>();

  /** Small components designed to run very often to keep resources in check */
  private final ArrayList<Runnable> housekeepers = new ArrayList<Runnable>();
  long nextHousekeeping;
  private static final long housekeepingInterval = 3000;

  /** Flag indicating the node is completely terminated. */
  protected volatile boolean terminated = false;
  private static final Object[] EMPTY_OBJECT_ARRAY = new Object[0];

  /** The object we use to append log messages */
  private LogAppender LOG = new NullLogAppender();

  /** Error messages are logged here */
  private LogAppender ERR = new NullLogAppender();




  /**
   * Constructor.
   */
  public NetworkService() {}




  /**
   * Set the log appender for this component.
   * 
   * @param appender the appender to use for log messages.
   */
  public void setLogAppender( LogAppender appender ) {
    LOG = appender;
  }




  /**
   * Set the error log appender for this component.
   * 
   * @param appender the appender to use for error messages.
   */
  public void setErrorAppender( LogAppender appender ) {
    ERR = appender;
  }




  /**
   * Simply stage the handler expecting it to be added when the service is 
   * started.
   * 
   * @param handler The handler to add.
   */
  public void stage( final NetworkServiceHandler handler ) {
    if ( handler != null ) {
      handlers.add( handler );
    }
  }




  /**
   * Add and initialize the handler.
   * 
   * @param handler The handler to add.
   * 
   * @throws NetworkServiceException If the handler could not be initialized.
   */
  public void add( final NetworkServiceHandler handler ) throws NetworkServiceException {
    addHandler( handler );
  }




  /**
   * Method initialize
   */
  private void initialize() {
    LOG.append( "NetworkService initializing" );

    // attempt to create selector
    try {
      selector = Selector.open();
    } catch ( final IOException e ) {
      ERR.append( "ERROR creating selector: " + e.getClass().getName() + " - " + e.getMessage() + System.getProperty( "line.separator" ) );
    }

    // TODO Verify if this still makes sense
    // reAdd();

    nextHousekeeping = System.currentTimeMillis() + NetworkService.housekeepingInterval;
    terminated = false;
  }




  /**
   * This is in the process of being factored out of the design.
   * 
   * <p>It isn't clear why (re)initializations require a close and an open.</p>
   *
   * <p>Closing then opening a channel will sometimes result is ChannelClosed 
   * exceptions being thrown.</p>
   */
  private void reAdd() {
    // get a list of handlers
    final NetworkServiceHandler[] handler = new NetworkServiceHandler[handlers.size()];
    for ( int x = 0; x < handlers.size(); handler[x] = (NetworkServiceHandler)handlers.get( x++ ) ) {
      ;
    }

    // clear all the handlers since this could be a restart
    for ( int x = 0; x < handler.length; x++ ) {
      try {
        removeHandler( handler[x] );
      } catch ( final NetworkServiceException e ) {
        ERR.append( "Problems removing existing handler during initialization: " + e.getClass().getName() + " - " + e.getMessage() + System.getProperty( "line.separator" ) );
      }
    }

    // Add all the handlers we just removed
    for ( int x = 0; x < handler.length; x++ ) {
      try {
        // re-add the handlers effectively re-starting them
        addHandler( handler[x] );
      } catch ( final NetworkServiceException e ) {
        ERR.append( "Problems adding existing handler during initialization: " + e.getClass().getName() + " - " + e.getMessage() + System.getProperty( "line.separator" ) );
      }
    }

  }




  /**
   * Method terminate
   */
  public void terminate() {
    try {
      final Object[] keys = selector.keys().toArray();

      for ( int i = 0; i < keys.length; i++ ) {
        final SelectionKey key = (SelectionKey)keys[i];

        try {
          // ask the handler to shutdown first
          ( (NetworkServiceHandler)key.attachment() ).shutdown();

          // Close the channel it handles
          key.channel().close();

          // Cancel and de-register the key
          key.cancel();
        } catch ( final Throwable ignore ) {
          // exceptions are OK here, we are closing everything down
        }
      }

      try {
        selector.close();
      } catch ( final IOException ignore ) {
        // exceptions are OK here, we are closing everything down
      }
    } catch ( final ClosedSelectorException e ) {
      // exceptions are OK here, we are closing everything down
    }

    terminated = true;
  }




  /**
   * This method performs all the work.
   */
  public void run() {
    current_thread = Thread.currentThread();
    do {
      // Setup everything we need to run
      initialize();

      // set the active flag true only after we have initialized, so others can
      // use waitForActive to wait for us to initialize
      setActiveFlag( true );

      while ( !isShutdown() ) {
        try {
          // loop while waiting for activity
          if ( ( selector != null ) && selector.isOpen() && ( selector.select( SELECT_WAIT_TIME ) >= 0 ) ) {
            Object[] keys = null;

            keys = selector.selectedKeys().toArray();

            for ( int i = 0; i < keys.length; i++ ) {

              final SelectionKey key = (SelectionKey)keys[i];
              selector.selectedKeys().remove( key );

              // get the selection key handler from the key attachment
              final NetworkServiceHandler handler = (NetworkServiceHandler)key.attachment();

              if ( handler != null ) {
                LOG.append( "Activity on " + handler.toString() );
                // inform the handler to accept the connection
                if ( key.isValid() && key.isAcceptable() ) {
                  handler.accept( key );
                }

                // Have the key handler connect to the accepted connection
                if ( key.isValid() && key.isConnectable() ) {
                  handler.connect( key );
                }

                // Have the handler read from the key
                if ( key.isValid() && key.isReadable() ) {
                  handler.read( key );
                }

                // have the key write to the connection
                if ( key.isValid() && key.isWritable() ) {
                  handler.write( key );
                }
              } else {
                // No key attachment, then cancel the key
                key.cancel();
              }
            } // for each key in the selection set

            // / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / /
            // see if it is time to perform housekeeping

            // Housekeeping check and run if it is time (usually every few 
            // seconds _not_ every iteration)
            if ( System.currentTimeMillis() >= nextHousekeeping ) {
              synchronized( housekeepers ) {
                for ( int x = 0; x < housekeepers.size(); x++ ) {
                  final Runnable housekeeper = (Runnable)housekeepers.get( x );
                  try {
                    housekeeper.run();
                  } catch ( final RuntimeException e ) {
                    ERR.append( "ERROR during a housekeeping run: " + e.getClass().getName() + " - " + e.getMessage() + System.getProperty( "line.separator" ) );
                  }
                }
              }
              nextHousekeeping = System.currentTimeMillis() + NetworkService.housekeepingInterval;
            }

          } // if operations are available

        } catch ( final Throwable e ) {
          // We can usually ignore exceptions during shutdown, otherwise...
          if ( !shutdown ) {
            ERR.append( "ERROR: " + e.getClass().getName() + " - " + e.getMessage() + System.getProperty( "line.separator" ) );
          }
        } // catch

        // This iteration is complete

      } // while !shutdown

      // Clean up after ourselves
      terminate();
    }
    while ( restart );

    // We are no longer running active
    setActiveFlag( false );
  }




  /**
   * @return  whether or not the current thread is set to shutdown.
   */
  public boolean isShutdown() {
    synchronized( mutex ) {
      return shutdown;
    }
  }




  /**
   * Request this object to restart.
   *
   * <p>For the UNIX types, this is just like the HUP signal and causes, the
   * thread of execution to perform a normal shutdown, and re-initialize.</p>
   */
  public void restart() {
    synchronized( mutex ) {
      restart = true;
      shutdown = true;

      if ( current_thread != null ) {
        current_thread.interrupt();

        // Make sure suspended threads are resumed so they can shutdown
        mutex.notifyAll();
      }
    }
  }




  /**
   * Wait for the network service to go active.
   *
   * <p>The main run loop will set the active flag to true when it enters the
   * main run loop AFTER the initialize() method has been called. The active
   * flag will not be set to false until the run loop has exited as the last
   * operation before leaving the run() method.</p>
   *
   * @param timeout The number of milliseconds to wait for the main run loop to
   *        be entered.
   */
  public void waitForActive( final long timeout ) {
    if ( !isActive() ) {
      // determine the timeout sentinel value
      final long tout = System.currentTimeMillis() + timeout;

      // While we have not reached the sentinel time
      while ( tout > System.currentTimeMillis() ) {
        // wait on the active lock object
        synchronized( activeLock ) {
          try {
            activeLock.wait( 10 );
          } catch ( final Throwable t ) {
            // exceptions are OK here, we are just giving other threads a 
            // chance to process by waiting
          }
        }

        // if we are now active...
        if ( isActive() ) {
          // ... break out of the time-out while loop
          break;
        }

      } // while time-out not reached

    } // if not active

  }




  public void waitForTerminated( final long timeout ) {
    if ( !terminated ) {
      // determine the timeout sentinel value
      final long tout = System.currentTimeMillis() + timeout;

      // While we have not reached the sentinel time
      while ( tout > System.currentTimeMillis() ) {
        try {
          activeLock.wait( 10 );
        } catch ( final Throwable t ) {
          // exceptions are OK here, we are just giving other threads a 
          // chance to process by waiting
        }

        if ( terminated ) {
          break;
        }

      } // while time-out not reached

    } // if not terminated

  }




  /**
   * Return whether or not the thread has entered and is currently within the main run loop.
   * @return  True if the ThreadJob has ben initialized and is cycling in the run  loop, False otherwise.
   */
  public boolean isActive() {
    synchronized( activeLock ) {
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
    synchronized( activeLock ) {
      active = flag;

      if ( active ) {
        activeLock.notifyAll();
      }
    }
  }




  /**
   * Method addHandler
   *
   * @param handler
   *
   * @throws NetworkServiceException
   */
  private void addHandler( final NetworkServiceHandler handler ) throws NetworkServiceException {
    if ( handler != null ) {
      final URI uri = handler.getServiceUri();

      if ( uri == null ) {
        throw new NetworkServiceException( "cannot bind: Handler returned a null service URI" );
      }

      LOG.append( "NetworkService.addHandler: adding a handler with a service URI of " + uri );

      int port = uri.getPort();

      // If we got a negative number, it means that a port was not specified
      if ( port < 0 ) {
        // Try to use one of the well-known ports as specified by the scheme
        port = UriUtil.getPort( uri.getScheme() );
      }

      if ( port < 0 ) {
        throw new NetworkServiceException( "Could not determine which port to use" );
      }

      // defined outside the try-catch so we can access these values in the 
      // catch block
      InetSocketAddress isa = null;

      final String host = uri.getHost();

      try {
        final InetAddress adr = InetAddress.getByName( host );

        if ( adr.isAnyLocalAddress() ) {
          isa = new InetSocketAddress( port );
        } else {

          // Create a socket address for the specified port
          isa = new InetSocketAddress( adr, port );

        } // if host represents a wildcard address

      } catch ( final UnknownHostException e2 ) {}

      // Check the URI for a valid TCP or UDP scheme
      if ( UriUtil.isUdp( uri ) ) {
        LOG.append( "NetworkService.addHandler: provisioning a UDP service for " + isa );

        // TODO Need to check to see if there is already something at this URI

        DatagramChannel channel = null;

        // create a datagram channel that supports the URI
        try {
          // Open a datagram channel
          channel = DatagramChannel.open();

          // This channel should not block since we are going to use a Selector
          channel.configureBlocking( false );

          // Allow the socket to send Broadcasts
          channel.socket().setBroadcast( true );

          // suggest a send buffer size to the IP stack
          channel.socket().setSendBufferSize( NetworkService.DATAGRAM_SEND_BUFFER_SIZE );

          // Suggest to the underlying UDP stack that we want a receive buffer
          // of a certian size reserved to make sure we get all our data
          channel.socket().setReceiveBufferSize( NetworkService.DATAGRAM_RECEIVE_BUFFER_SIZE );

          // indicates that the rules used in validating addresses supplied in
          // a bind call should allow reuse of local addresses. This means
          // multiple objects can send and receive data on the same port of the
          // same address
          channel.socket().setReuseAddress( true );

          LOG.append( "Binding UDP socket to " + isa );

          channel.socket().bind( isa );

          LOG.append( "UDP channel is bound to " + channel.socket().getLocalAddress() );

          // Type of service IPTOS_RELIABILITY(0x04) and IPTOS_THROUGHPUT(0x08)
          final int tos = 4 | 8;
          try {
            channel.socket().setTrafficClass( tos );
            LOG.append( "UDP ToS: " + channel.socket().getTrafficClass() );
          } catch ( final SocketException e ) {
            LOG.append( "UDP traffic class of " + tos + " could not be set, will be ignored" );
          }

          // Give it the channel we created for it
          handler.setChannel( channel );

          LOG.append( "NetworkService.addHandler: registering UDP channel with selector" );

          SelectionKey key = null;

          key = channel.register( selector, SelectionKey.OP_READ );

          LOG.append( "NetworkService.addHandler: UDP key validity check..." );
          while ( !key.isValid() ) {
            // problems with the key for some reason
            ERR.append( "NetworkService.addHandler selector returned an invalid key!" );
            key.cancel();
            key = channel.register( selector, SelectionKey.OP_READ );
          }

          LOG.append( "NetworkService.addHandler: UDP key is valid" );

          // Attach the given handler to the selection key
          key.attach( handler );

          // Give the handler the selection key so it can inform the selector
          // an interest in write operations
          handler.setKey( key );

          // All handlers are initialized when they have been added but after 
          // they have been assigned their keys so they can send stuff
          handler.initialize();

          LOG.append( "NetworkService.addHandler: UDP channel added and initialized" );

        } catch ( final Exception e ) {
          boolean sorFlag = false;
          try {
            sorFlag = channel.socket().getReuseAddress();
          } catch ( final SocketException e1 ) {
            ERR.append( "ERROR Can't get SocketOptions: " + e1.getClass().getName() + " - " + e1.getMessage() + System.getProperty( "line.separator" ) );
          }

          final StringBuffer buffer = new StringBuffer( "Error binding datagram server: " );
          buffer.append( e.getMessage() );
          buffer.append( " - address:'" );
          buffer.append( isa.toString() );
          buffer.append( "' SO_REUSEADDR=" );
          buffer.append( sorFlag );
          throw new NetworkServiceException( buffer.toString(), e );
        }

        // Add the UDP handler to the list of handlers 
      } else {
        LOG.append( "NetworkService.addHandler: provisioning a TCP service for " + isa );

        // create a ServerSocketChannel to support the URI
        try {

          if ( handler.getChannel() != null ) {
            LOG.append( "NetworkService.addHandler: using existing Server Socket Channel" );
          } else {
            LOG.append( "NetworkService.addHandler: Opening a new Server Socket Channel" );
            // Create a server socket
            final ServerSocketChannel channel = ServerSocketChannel.open();

            LOG.append( "NetworkService.addHandler: Server Socket Channel opened." );

            // This channel should not block since we are going to use a Selector
            channel.configureBlocking( false );

            // create a socket address for the requested URI
            //final InetSocketAddress isa = new InetSocketAddress( UriUtil.getHostAddress( uri ), port );

            // indicates that the rules used in validating addresses supplied in
            // a bind call should allow reuse of local addresses. This means
            // multiple objects can send and receive data on the same port of the
            // same address
            channel.socket().setReuseAddress( true );

            LOG.append( "NetworkService.addHandler: binding TCP socket to " + isa );

            // Bind the socket to the given address
            channel.socket().bind( isa );

            // Give it the channel we created for it
            handler.setChannel( channel );

            LOG.append( "NetworkService.addHandler: handler " + handler + " has channel set to " + handler.getChannel() );

          }

          LOG.append( "NetworkService.addHandler: registering TCP channel with selector" );

          SelectionKey key = null;

          synchronized( selector ) // TODO currently under review
          {
            LOG.append( "NetworkService.addHandler: handler.channel=" + handler.getChannel() );
            key = handler.getChannel().register( selector, SelectionKey.OP_ACCEPT );

            while ( !key.isValid() ) {
              // problems with the key for some reason
              ERR.append( "NetworkService.addHandler selector returned an invalid key! re-registering" );
              key.cancel();
              key = handler.getChannel().register( selector, SelectionKey.OP_ACCEPT );
            }
            LOG.append( "NetworkService.addHandler: received a valid selector key for TCP handler" );
          }

          // Attach the given handler to the selection key
          key.attach( handler );

          // Give the handler the selection key so it can inform the selector
          // an interest in write operations
          handler.setKey( key );

          LOG.append( "NetworkService.addHandler: initializing handler" );

          // All handlers are initialized when they have been added
          handler.initialize();

          LOG.append( "NetworkService.addHandler: TCP channel added and initialized" );

        } catch ( final Exception e ) {
          ERR.append( "ERROR binding socket server to port " + port + ": " + e.getClass().getName() + " - " + e.getMessage() + System.getProperty( "line.separator" ) );
        }

      } // uri = tcp or udp

    } // if handler !null

  } // addHandler()




  public void removeHandler( final NetworkServiceHandler handler ) throws NetworkServiceException {
    if ( handler != null ) {
      try {
        if ( !handlers.remove( handler ) ) {
          LOG.append( "Shutting down untracked handler " + handler.getClass().getCanonicalName() );
        }

        // This should close everything
        handler.shutdown();

        // These are just to make sure
        if ( handler.getChannel() != null ) {
          handler.getChannel().close();
        }

        if ( handler.getKey() != null ) {
          handler.getKey().cancel();
        }
      } catch ( final Exception e ) {
        ERR.append( "ERROR while removing a handler: " + e.getClass().getName() + " - " + e.getMessage() + System.getProperty( "line.separator" ) );
      }
    }
  }




  public int getKeyCount() {
    return selector.keys().size();
  }




  /**
   * Request this object to shutdown.
   */
  public void shutdown() {
    synchronized( mutex ) {
      shutdown = true;
      restart = false;

      if ( current_thread != null ) {
        current_thread.interrupt();

        // Make sure suspended threads are resumed so they can shutdown
        mutex.notifyAll();
      }
    }
  }




  /**
   * Used when the VM is coming down to ensure everything is closed in one 
   * thread call
   */
  public void destroy() {
    synchronized( mutex ) {
      shutdown = true;
      restart = false;

      if ( current_thread != null ) {
        // current_thread.interrupt(); // may cause datagramchannel to close

        // Make sure suspended threads are resumed so they can shutdown
        mutex.notifyAll();
      }
    }
    terminate();
  }




  /**
   * Add a runnable task that will be called every 6 seconds.
   * 
   * <p>This task is called in the IO thread and should <strong>NOT</strong> 
   * take too much time to process as all network IO will be suspended until 
   * the task is complete.</p>
   * 
   * @param housekeeper The runnable class to use as the housekeeper.
   */
  public void addHousekeeper( final Runnable housekeeper ) {
    if ( housekeeper != null ) {
      synchronized( housekeepers ) {
        if ( !housekeepers.contains( housekeeper ) ) {
          housekeepers.add( housekeeper );
        }
      }
    }
  }




  /**
   * Return a TCP server socket on the given address and port, incrementing the
   * port until a server socket can be opened.
   * 
   * TODO Check to make sure Security Manager is not denying us Listen access
   *
   * @param address
   * @param port
   *
   * @return TODO Complete Documentation
   */
  public static ServerSocketChannel getNextServerSocket( InetAddress address, final int port ) {
    int i = port;
    ServerSocketChannel channel = null;

    try {
      channel = ServerSocketChannel.open();

      // This channel should not block since we are going to use a Selector
      channel.configureBlocking( false );

      // indicates that the rules used in validating addresses supplied in
      // a bind call should allow reuse of local addresses. This means
      // multiple objects can send and receive data on the same port of the
      // same address
      channel.socket().setReuseAddress( true );

    } catch ( final IOException e1 ) {
      e1.printStackTrace();
      return null;
    }

    // If no address was given, then try to determine our local address so we
    // can use our main address instead of 127.0.0.1 which may be chosen by the
    // VM if it is not specified in the ServerSocket constructor
    if ( address == null ) {
      address = IpInterface.getPrimary().getAddress().toInetAddress();
    }

    while ( NetworkService.validatePort( i ) != 0 ) {
      try {
        if ( address == null ) {
          channel.socket().bind( new InetSocketAddress( i ) );
        } else {
          channel.socket().bind( new InetSocketAddress( address, i ) );
        }
        return channel;

      } catch ( final Exception e ) {
        // happens normally when the socket is unavailable, just try the next
        i++;
      }
    }

    return null;
  }




  /**
   *
   * @param port
   *
   * @return TODO Complete Documentation
   */
  public static int validatePort( final int port ) {
    if ( ( port < 0 ) || ( port > 0xFFFF ) ) {
      return 0;
    } else {
      return port;
    }
  }

}
