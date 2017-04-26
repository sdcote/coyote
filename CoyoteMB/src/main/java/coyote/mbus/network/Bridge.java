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
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.AbstractSelectableChannel;

import coyote.commons.network.IpAddress;
import coyote.commons.network.IpAddressException;
import coyote.commons.network.IpNetwork;


/**
 * The Bridge class models a component that manages a TCP connection to another 
 * node. 
 * 
 * <p>Object of this class maintain all state for the session.</p> 
 * 
 * <p>EVERYTHING BELOW IS BEING REFACTORED<br/> 
 * =========================================================================== 
 * <p>The source address of each incoming packet is checked to see if it 
 * matches any nodes on the bus subnet. If there is a match the packet is 
 * discarded.</p>
 * 
 * <p>Bridges listen to "join" and "leave" announcements and only bridge 
 * packets that match the current active membership list on either end of the 
 * connection. This reduces the number of packets that cross the bridges.</p>
 * 
 * <p>Before a bridge is created, the MicroBus attempts to see if there are any
 * other bridges to the subnet by sending a "ROUTE" packet to the bus with the
 * address of the destination node. Any node on the bus with a bridge to the 
 * subnet containing the destination address will respond with a "BRIDGE" 
 * packet indicating a bridge exists to the destination. If a BRIDGE packet is
 * not received in a timely fashion (TBD) the requesting node will open a 
 * bridge to the destination address and issue a BRIDGE packet announcing the 
 * new connection to others.</p>
 * 
 * <p>When a bridge is established to the destination, the remote systems send 
 * their respective subnet mask to the other end, allowing each end of the 
 * bridge to announce the availability of the bridge to their local bus 
 * neighbors. The BRIDGE announcement can then be checked by others to sense 
 * routing loops.</p>
 * 
 * <p>All JOINs and LEAVEs are propagated across bridges allowing multi-hop 
 * bridging to occur based upon group name while throttling bridge traffic.</p> 
 * 
 * TODO NIOSESSIONSERVICE is the model 
 * TODO Negiotiate session-level QoS - like IAM/EAM, priority, encryption, authentication, etc. 
 * TODO Use the ClosureMessage object to signal disconnect.
 */
class Bridge implements NetworkServiceHandler {
  /** The socket channel we use to send and receive TCP data */
  // not needed as we can get this from the selection key private final SocketChannel socketChannel = null;
  /** The size of the "send" and "receive" buffers for sockets (32768 bytes) */
  public static int SOCKET_BUFFER_SIZE = 32768;

  /** A buffer used for reading data */
  private final ByteBuffer readBuffer = ByteBuffer.allocateDirect( Bridge.SOCKET_BUFFER_SIZE );

  /** Key used by the NIO server to track this bridge. */
  SelectionKey nioKey = null;

  /** The epoch time we connected */
  long connectedTime = 0;

  /** The remaining number of octets required to complete the current packet */
  int remainingOctets = 0;

  /** The buffer containing the current Packet Header */
  private final byte[] headerBuffer = new byte[Packet.HEADER_SIZE];

  /**
   * The buffer we use for holding frames while reading. It is started small and
   * increased in size as needed.
   */
  private ByteBuffer packetBuffer = ByteBuffer.allocate( 50 );

  /** Our current read state. */
  short readState = 0;

  /** The NEW read state indicating we have not yet started reading data. */
  static final short NEW = 0;

  /** The READY read state indicating we are ready to read in data. */
  static final short READY = 1;

  /** The READING read state indicating we are reading a large packet. */
  static final short READING = 2;

  /** Address to which this handler binds to accept connections */
  private IpAddress listenAddress = null;

  /** Poet to which this handler binds to accept connections */
  private int listenPort = 5555;

  /** The channel assigned to this handler when operating as a server */
  AbstractSelectableChannel listenerChannel = null;

  /** Access Control List for TCP connections. */
  private final IpAcl ACL = new IpAcl();

  /** The number of times we tried to read the cookie */
  short cookieAttempts = 0;

  /** Field COOKIE_READ_LIMIT */
  static final short COOKIE_READ_LIMIT = 3;

  /** If we don't receive a cookie in 3 seconds, then time the connection out */
  static final long COOKIE_TIMEOUT = 3000;

  /**
   * Our magic cookie: Data Link Escape (DLE), Start Of Header (SOH) and End of
   * Transmit Block (ETB) octet sequence indicating the start of a session.
   */
  public static final byte[] MAGIC = { 16, 1, 23 };

  /**
   * Our magic cookie: Data Link Escape (DLE), Synchronous Idle  (SYN) and End 
   * of Transmit Block (ETB) octet sequence indicating the start of a bridge.
   */
  public static final byte[] BRIDGE_MAGIC = { 16, 22, 23 };

  /** The message channel we use to communicate inbound and outbound messages */
  MessageChannel messageChannel = null;




  /**
   * Constructor used by the driver to create a TCP listener which accepts 
   * connections and creates new TCP sessions.
   * 
   * @param addr the address to which we bind
   * @param port the port to which we bind
   */
  public Bridge( final IpAddress addr, final int port ) {

    if ( addr != null ) {
      listenAddress = addr;
    } else {
      try {
        listenAddress = new IpAddress( "0.0.0.0" );
      } catch ( final IpAddressException e ) {
        // should not happen
      }
    }

    if ( ( port > 0 ) && ( port < 65535 ) ) {
      listenPort = port;
    }

    // By default we disallow everything
    // ACL.setDefaultAllow( false );
    ACL.setDefaultAllow( true ); // only during testing
  }




  /**
   * Constructor used by the network service when accepting client connections.
   * 
   * @param key
   */
  public Bridge( final SelectionKey key ) {
    nioKey = key;
  }




  public void initialize() {
    //Log.debug( "Bridge is being initialized" );
  }




  public void terminate() {
    //Log.debug( "Bridge is being terminated" );
  }




  public void close() {
    //Log.debug( "Bridge is being closed" );
  }




  /* (non-Javadoc)
   * @see net.bralyn.network.NetworkServiceHandler#accept(java.nio.channels.SelectionKey)
   */
  public void accept( final SelectionKey key ) {
    //Log.debug( "Bridge is accepting a connection" );

    try {
      final SocketChannel socketChannel = ( (ServerSocketChannel)key.channel() ).accept();
      //Log.info( "Connection from " + socketChannel.socket().getRemoteSocketAddress() );

      // Use ACL to determine if the connection should be accepted
      if ( ACL.allows( socketChannel.socket().getInetAddress() ) ) {
        //Log.info( "Passed ACL check" );
        socketChannel.socket().setSendBufferSize( Bridge.SOCKET_BUFFER_SIZE );
        socketChannel.socket().setReceiveBufferSize( Bridge.SOCKET_BUFFER_SIZE );
        socketChannel.configureBlocking( false );

        if ( socketChannel != null ) {
          // Create a new TCP listener to service this connection/session
          final Bridge session = new Bridge( key );

          // Set the time it was connected
          session.connectedTime = System.currentTimeMillis();

          // Assign the socket channel to the handler
          session.setChannel( socketChannel );

          // Initialize the handler
          session.initialize();

          final Selector selector = key.selector();
          synchronized( selector ) {
            final SelectionKey clientKey = socketChannel.register( selector, SelectionKey.OP_READ );
            //Log.info( "Accepted connection from " + socketChannel.socket().getRemoteSocketAddress() );
            clientKey.attach( session );
          }

        }
      } else {
        //Log.error( "Failed ACL check, closing connection." );
        try {
          socketChannel.close();
        } catch ( final Exception e ) {}
        return;
      }

    } catch ( final IOException e ) {
      System.out.println( "ERROR (accepting connection): " + e );
    }

  }




  /* (non-Javadoc)
   * @see net.bralyn.network.NetworkServiceHandler#connect(java.nio.channels.SelectionKey)
   */
  public void connect( final SelectionKey key ) {
    //Log.debug( "Bridge is connecting for some odd reason" );
  }




  /* (non-Javadoc)
   * @see net.bralyn.network.NetworkServiceHandler#getChannel()
   */
  public AbstractSelectableChannel getChannel() {
    return listenerChannel;
  }




  /* (non-Javadoc)
   * @see net.bralyn.network.NetworkServiceHandler#getKey()
   */
  public SelectionKey getKey() {
    // TODO Auto-generated method stub
    return null;
  }




  /* (non-Javadoc)
   * @see net.bralyn.network.NetworkServiceHandler#getServiceUri()
   */
  public URI getServiceUri() {
    try {
      return new URI( "tcp://" + listenAddress.toString() + ":" + listenPort );
    } catch ( final URISyntaxException e ) {
      e.printStackTrace();
      return null;
    }
  }




  /* (non-Javadoc)
   * @see net.bralyn.network.NetworkServiceHandler#setChannel(java.nio.channels.spi.AbstractSelectableChannel)
   */
  public void setChannel( final AbstractSelectableChannel channel ) {
    //Log.info( "Assigned a channel of " + channel );
    listenerChannel = channel;
  }




  /* (non-Javadoc)
   * @see net.bralyn.network.NetworkServiceHandler#setKey(java.nio.channels.SelectionKey)
   */
  public void setKey( final SelectionKey key ) {
    // TODO Auto-generated method stub

  }




  /**
   * Close the socket channel and deregister the selection key.
   * 
   * @see coyote.mbus.network.NetworkServiceHandler#shutdown()
   */
  public void shutdown() {
    // Get the channel from the key
    final ServerSocketChannel socketChannel = (ServerSocketChannel)nioKey.channel();

    //    if( Log.isLogging( Log.DEBUG_EVENTS ) )
    //    {
    //      Log.debug( "Closing connection on " + socketChannel.socket().getLocalSocketAddress() );
    //    }

    try {
      // close the connection socket which will also close the channel
      socketChannel.socket().close();
    } catch ( final IOException e ) {
      try {
        // try to close the channel if the socket close failed
        socketChannel.close();
      } catch ( final IOException e1 ) {
        // Log our socket closing woes
        //Log.error( "Could not close socket (" + e.getMessage() + ") and could not close channel (" + e1.getMessage() + ")" );
      }
    }

    // deregister the key so select will not be bothered by this connection
    nioKey.cancel();
  }




  /* (non-Javadoc)
   * @see net.bralyn.network.NetworkServiceHandler#write(java.nio.channels.SelectionKey)
   */
  public void write( final SelectionKey key ) {
    // TODO Auto-generated method stub

  }




  /**
   * Read data from the channel specified via the given SelectionKey.
   * 
   * <p>This method reads packets by first starting in the READY state. While it 
   * is READY, the process builds the header buffer with the data it reads in 
   * until all 13 octets have been filled. Once the header buffer is filled, 
   * the loop enters the READING state.</p>
   * 
   * <p>While READING, the loop starts filling the packet buffer with the bytes 
   * up to the length of the payload of the packet, reusing the framebuffer 
   * until it is noticed that the buffer is too small to hold the entire 
   * packet.</p>
   * 
   * <p>The only way to detect that the remote host has closed the connection 
   * is to attempt to read or write from the connection. If the remote host 
   * properly closed the connection, read() will return -1. If the connection 
   * was not terminated normally, read() and write() will throw an 
   * exception.</p>
   * 
   * <p>When using a selector to process events from a non-blocking socket, the 
   * selector will try to return an OP_READ or OP_WRITE event if the remote 
   * host has closed the socket.</p>
   *
   * @param key
   */
  public void read( final SelectionKey key ) {

    final SocketChannel socketChannel = (SocketChannel)key.channel();

    //    MicroBus.log( "Reading data from " + socketChannel.socket().getRemoteSocketAddress() );

    int readCount = 0;

    try {
      // Read data from the channel into our buffer
      readCount = socketChannel.read( readBuffer );

      // check for closed connection
      if ( readCount < 0 ) {
        //        MicroBus.log( "Peer '" + socketChannel.socket().getRemoteSocketAddress() + "' closed the connection" );

        // close the connection
        socketChannel.socket().close();

        // deregister the key
        key.cancel();

        shutdown(); // EOF - shutdown the handler
        return; // early exit
      }

      //      MicroBus.log( "Received " + readCount + " bytes from " + socketChannel.socket().getRemoteSocketAddress() );

      // flip the buffer to process what we have received so far
      readBuffer.flip();
    } catch ( final IOException e ) {
      //      MicroBus.log( "IO problems with peer '" + socketChannel.socket().getRemoteSocketAddress() + "' = reason: " + e.getMessage() );

      // close the connection
      try {
        socketChannel.socket().close();
      } catch ( final IOException e1 ) {
        // probably what caused the problem anyways
      }

      // deregister the key
      key.cancel();

      shutdown(); // EOF - shutdown the handler
      return; // early exit
    }

    try {
      // We have read in all we can, now process what is in the buffer so far
      while ( readBuffer.remaining() > 0 ) {
        //        MicroBus.log( "Read buffer contains " + readBuffer.remaining() + " bytes to process" );

        // If we are in a ready state, we are expecting a packet header
        if ( readState == Bridge.READY ) {
          //          MicroBus.log( "READY: Reading header" );

          // if there is enough data in the buffer to read in the entire header
          if ( readBuffer.remaining() >= Packet.HEADER_SIZE ) {
            // ... read in the header
            readBuffer.get( headerBuffer );

            //            MicroBus.log( "Received header:\r\n" + ByteUtil.dump( headerBuffer ) );

            // extract the length of the payload portion of the packet
            remainingOctets = Packet.getPacketLength( headerBuffer );

            //            MicroBus.log( "Expecting a payload of " + remainingOctets + " bytes" );

            if ( remainingOctets > 0 ) {
              // determine the length of the entire packet
              final int packetLength = Packet.HEADER_SIZE + remainingOctets;

              if ( packetBuffer.capacity() < packetLength ) {
                // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
                // perform some checks here...if the requested packet length is
                // too large, it might be best to open a file and dump the 
                // thing to disk using a MappedBuffer or just log a "Packet Too 
                // Large" error, drop the rest of the payload and just process 
                // the next header.
                // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

                //                MicroBus.log( "creating new, larger packet buffer to support packet size of " + packetLength );

                packetBuffer = ByteBuffer.allocate( packetLength );
              }

              // clear out the packet buffer
              packetBuffer.clear();

              // copy the header into the packet buffer
              packetBuffer.put( headerBuffer );

              // change our state
              readState = Bridge.READING;
            } else {
              // No payload, probably a heartbeat packet to keep ISDN or dialup
              // connections alive.
              processPacket( new Packet( headerBuffer ) );
            }
          } else {
            // not enough data to read header, break out of while loop and wait
            // for another read to get more data
            break;
          }
        } else if ( readState > Bridge.READY ) {
          //          MicroBus.log( "READING: payload into packet buffer " + packetBuffer );

          //          MicroBus.log( "expecting " + remainingOctets + " read buffer contains " + readBuffer.remaining() );
          // if the expected amount is <= what is remaining in the buffer
          if ( remainingOctets <= readBuffer.remaining() ) {
            // / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / /
            // -    -    -    -    -    -    -    -    -    -    -    -    -
            // <gripe>not sure I like allocating a byte array for each chunk...
            // It seems data transfer would be MUCH faster to somehow copy to 
            // the packetBuffer from the buffer with NIO similar to the 
            // arrayCopy function and not allocate more memory from the heap 
            // only to have it GC'd a moment later.</gripe>
            // TODO re-work the chunking so it is efficient
            final byte[] chunk = new byte[remainingOctets];

            // ...read in only the needed amount
            readBuffer.get( chunk );

            // ...place it in the packet buffer
            packetBuffer.put( chunk );

            // reduce the remaining octet count by the amount we just chunked
            remainingOctets -= chunk.length;
            //            MicroBus.log( "processed a chunk of " + chunk.length + " bytes - remaining = " + remainingOctets + " bytes" );
            // -    -    -    -    -    -    -    -    -    -    -    -    -
            // / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / /

            // See if we have all our data to complete the packet for processing
            if ( remainingOctets == 0 ) {
              // We have a complete packet buffer according to the header values
              // Pass the packet buffer to the Packet for parsing and pass
              // the Packet to our RemoteService instance for processing.
              processPacket( new Packet( packetBuffer.array() ) );

              // ...remove the old data from the packet buffer
              packetBuffer.clear();

              // ...go into the READY state so we will read the next header
              readState = Bridge.READY;
            }

            // done
            break;
          }
        }
        // If we are not READY to read the next packet header or READING the 
        // body of a packet header we previously received, then we must be 
        // looking for our magic cookie to get started.
        else {
          // attempting to find our magic cookie
          if ( readBuffer.remaining() > 2 ) {
            // while there is enough data to read in an entire cookie
            while ( readBuffer.remaining() > 2 ) {
              cookieAttempts++;

              if ( cookieAttempts > Bridge.COOKIE_READ_LIMIT ) {
                shutdown();

                return;
              }

              byte octet = readBuffer.get();

              if ( octet == Bridge.MAGIC[0] ) {
                octet = readBuffer.get();

                if ( octet == Bridge.MAGIC[1] ) {
                  octet = readBuffer.get();

                  if ( octet == Bridge.MAGIC[2] ) {
                    readState = Bridge.READY;

                    readBuffer.compact();
                    //                    MicroBus.log( "MAGIC received from " + socketChannel.socket().getRemoteSocketAddress() );

                    // Now that we have received our magic, we can add our
                    // channel to the event manager for servicing
                    //eventManager.addChannel( messageChannel );
                    return;
                  } // ETB
                } // SOH
              } // DLE

            } // while

          } // remaining>2
          else {
            // not enough characters in the buffer to read the magic cookie
            break;
          }

        } // if state

      } // while remaining
    } catch ( final Exception e ) {
      //      MicroBus.err( "Exception processing frames from " + socketChannel.socket().getRemoteSocketAddress() + "\n" + PacketException.stackTrace( e ) );
      //      MicroBus.err( "PacketBuffer dump:\r\n" + ByteUtil.dump( packetBuffer.array() ) );
      //shutdown();
    }

    // compact the buffer so subsequent reads will append data
    readBuffer.compact();
  }




  private void processPacket( final Packet packet ) {

  }




  /**
   * Add a network specification to the ACL with the given allowance.
   * 
   * <p>Network specification of 127.0.0.1/32 represents the entire loopback
   * address.</p>
   * 
   * <p>Passing the string of &quot;DEFAULT&quot; will assign the default 
   * access to the given allowed argument. If false, all addresses that do not 
   * match an entry in the ACL will be denied, while a value of true will 
   * result in allowing a connection from a network not otherwise specified in 
   * the ACL.</p>
   *
   * @param network the network specification to add (e.g. "192.168/16", "10/8")
   * @param allowed whether or not connections from the specified network will
   *        be accepted.
   * 
   * @throws IpAddressException if the specified network is not valid.
   */
  public void addAclEntry( final String network, final boolean allowed ) throws IpAddressException {
    if ( "DEFAULT".equalsIgnoreCase( network ) ) {
      ACL.setDefaultAllow( allowed );
    } else {
      ACL.add( new IpNetwork( network ), allowed );
    }
  }




  /**
   * Add a network specification to the ACL with the given allowance.
   * 
   * @param network the network specification to add.
   * @param allowed whether or not TCP connections from the specified network 
   *        will be accepted.
   */
  public void addAclEntry( final IpNetwork network, final boolean allowed ) {
    ACL.add( network, allowed );
  }




  /**
   * Add one or more new rules to the Access Control List.
   *
   * <p>The argument String should adhere to the following format:
   * <pre>
   *     network:allowed;network:allowed;network:allowed...
   * </pre>
   * Where "network" is a CIDR representation of a network against which a
   * match is to be made, and "allowed" is either 'ALLOW' or 'DENY'. There is a
   * special expression of 'DEFAULT' which represents the default rule (what
   * should  happen if no expression is matched when performing a check).</p>
   *
   * <p>Examples include:
   * <pre>
   *     192.168/16:ALLOW;DEFAULT:DENY
   * </pre>
   *
   * Where everything coming from the 192.168.0.0/255.255.0.0 network is
   * allowed and everything else is denied.</p>
   *
   * @param rules A semicolon delimited list of rules
   */
  public void addAclEntry( final String rules ) throws IpAddressException {
    ACL.parse( rules );
  }

}
