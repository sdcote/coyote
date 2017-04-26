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
import java.net.SocketAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.spi.AbstractSelectableChannel;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import coyote.commons.ByteUtil;
import coyote.commons.ExceptionUtil;
import coyote.commons.NetUtil;
import coyote.commons.UriUtil;
import coyote.dataframe.DataField;
import coyote.mbus.LogAppender;
import coyote.mbus.MessageSink;
import coyote.mbus.NullLogAppender;
import coyote.mbus.message.ClosureMessage;
import coyote.mbus.message.Message;
import coyote.mbus.message.MessageAddress;


/**
 * The MessageBus class models a UDP socket server that listens on a port for 
 * Message Packets and then passes those Packets on for routing. 
 * 
 * <p>Instance of this class runs in a NetworkService.</p>
 */
public final class MessageBus implements NetworkServiceHandler, MessageMediator, MessageSink {
  /** Tag used in various class identifying locations like DataCapsule nodes */
  public final String CLASS_TAG = "MessageBus";

  /** A buffer used for read/writing datagrams */
  private ByteBuffer buffer;

  /** The datagram channel we use to send and receive UDP messages */
  private DatagramChannel datagramChannel = null;

  /** The SelectionKey used by the selector to arbitrate I/O abilities. */
  private SelectionKey key;

  /** The IP port to which we listen by default */
  public static final int DEFAULT_PORT = 7943;

  /** The default URI we use to specify where we listen for messages */
  public static final URI DEFAULT_URI = UriUtil.parse( "mbus://0.0.0.0:" + MessageBus.DEFAULT_PORT );

  /**
   * The default netmask for our broadcasts is 0.0.0.0 (no mask) meaning the
   * entire address is our network. This will result in a broadcast address of
   * 255.255.255.255
   */
  public static final String DEFAULT_NETMASK = "0.0.0.0";

  /** The outbound packet queue from which we send everything */
  private final PacketQueue packetQueue = new PacketQueue();

  /** The cache of all the Packets we have sent so far */
  PacketQueue cache = new PacketQueue();

  /**
   * The time when the service was inserted into the bus.
   */
  public volatile long inserted = 0;

  /** The last (or current) message sequence identifier we have sent */
  volatile long sequence = 0;

  /** When the next heartbeat should be sent */
  volatile private long nextHeartbeat = 0;

  /** The SocketAddress we use for broadcasting on the network bus */
  SocketAddress bcastaddr = null;

  /** The calendar object we use to calculate the UTC timestamp for each packet */
  private static final Calendar cal = Calendar.getInstance();

  /** The MessageChannel that receives all our received messages */
  public MessageChannel MessageChannel = null;

  /** The number of milliseconds we wait for ARPs to our claim of an endpoint. */
  public static final long INSERTION_INTERVAL = 3000;

  /** The URI representing the bus interface for this instance */
  private URI busUri = MessageBus.DEFAULT_URI;

  /** The number that uniquely identifies this node on the local (subnet) bus */
  public volatile long endpoint = -1;

  /** Create a random number generator for endpoint (re)generation */
  Random generator = new SecureRandom();

  /** Get the unique identifier of this object within the VM for identity */
  long token = System.identityHashCode( this );

  /** The maximum transmission unit for our datagrams (UDP_MAX - HEADER_SIZE) */
  static final int DATAGRAM_MTU = 65507;

  /** Number of milliseconds between heartbeats. (Default = 20,000ms) */
  long heartbeatInterval = 20000;

  /** The map of remote node objects */
  final HashMap<Long, RemoteNode> nodes = new HashMap<Long, RemoteNode>();

  /**
   * The message we constantly broadcast to the network informing the other nodes
   * of our current status.
   */
  Message heartbeatPacket = null;

  long numberOfTimesInvalidKeyWasObserved = 0;

  /** Optional dependency on a TCP service. */
  private volatile MessageService tcpService = null;

  private volatile boolean shutdown = false;

  private Timer heartbeatTimer;

  /** The object we use to append log messages */
  private LogAppender LOG = new NullLogAppender();

  /** Error messages are logged here */
  private LogAppender ERR = new NullLogAppender();




  /**
   * Creates a PacketBus using the network and host specified by the given URI.
   * 
   * <p>The MessageChannel's address and port information is assumed to the TCP 
   * server's Address and port and will be used for all addresses.</p>
   *
   * @param channel
   * @param uri
   */
  public MessageBus( final MessageChannel channel, final URI uri ) {
    super();

    if ( channel == null ) {
      throw new IllegalArgumentException( "MessageChannel was null" );
    }

    MessageChannel = channel;

    // All messages received via the message channel will be handled by this object
    MessageChannel.outSink = this;

    try {
      // Make sure we have a complete URI, otherwise use the defaults
      if ( uri.getPort() < 1 ) {
        if ( uri.getHost() != null ) {
          // Scheme MUST be UDP so the handler will create a datagram service
          busUri = UriUtil.parse( "udp://" + uri.getHost() + ":" + MessageBus.DEFAULT_URI.getPort() );
        }
      } else {
        if ( uri.getHost() != null ) {
          busUri = uri;
        } else {
          throw new IllegalArgumentException( "No host in URI" );
        }
      }

      setNetmask( MessageBus.DEFAULT_NETMASK );

      LOG.append( "MessageBus.ctor " + busUri + " - broadcasting on " + bcastaddr );

      buffer = ByteBuffer.allocate( MessageBus.DATAGRAM_MTU );

      if ( ( MessageChannel.address.getAddress() == null ) || MessageChannel.address.getAddress().getHostAddress().equals( "0.0.0.0" ) ) {
        // Make sure we get the IP Address by which the rest of the world knows us
        // or at least, our host's default network interface
        try {
          // This helps insure that we do not get localhost (127.0.0.1)
          final InetAddress addr = InetAddress.getByName( InetAddress.getLocalHost().getHostName() );
          LOG.append( "MessageBus.ctor: MessageChannel address was not set, using " + addr );
          MessageChannel.setAddress( new MessageAddress( addr, MessageChannel.address.getPort(), 0, MessageChannel.address.getChannelId() ) );
        } catch ( final UnknownHostException e ) {
          // Aaaaww Phooey! DNS is not working or we are not in it.
        }

      }

    } catch ( final Exception e ) {
      e.printStackTrace();

      throw new IllegalArgumentException( e.getMessage() );
    }
  }




  /**
   * @param appender The log appender to use when appending messages to the log.
   */
  public synchronized void setLogAppender( LogAppender appender ) {
    LOG = appender;
  }




  /**
   * @param appender The log appender to use when appending error messages to the log.
   */
  public synchronized void setErrorAppender( LogAppender appender ) {
    ERR = appender;
  }




  /**
   * Method setNetmask
   *
   * @param netmask
   */
  public void setNetmask( final String netmask ) {
    // calculate a broadcast address based upon the currently set network mask
    // applied to the address in the URI
    if ( ( netmask != null ) && ( netmask.trim().length() > 0 ) ) {
      bcastaddr = new InetSocketAddress( NetUtil.getBroadcastAddress( busUri.getHost(), netmask ), busUri.getPort() );
    }
  }




  /**
   * @return  the bcastaddr
   */
  public SocketAddress getBcastaddr() {
    return bcastaddr;
  }




  /**
   * This method will be called just before being added to the selector.
   */
  public void initialize() {
    // insert into the bus
    insertIntoBus();

    // Setup a timer to send heartbeats and perform housekeeping tasks
    heartbeatTimer = new Timer();
    heartbeatTimer.schedule( new HeartbeatTask(), 1000, 1000 ); // every second
  }




  /**
   * Method shutdown
   */
  public void shutdown() {
    // If we were interrupted, we need to clear the interrupted flag so the
    // datagramChannel will properly send the last packet and not throw a closed
    // exception even though the channel is actually open.
    Thread.interrupted();

    // Stop sending heartbeats
    if ( heartbeatTimer != null ) {
      heartbeatTimer.cancel();
    }

    // If we have a key we are probably connected to a socket so clean up
    if ( key != null ) {
      // withdraw from the bus, by first flushing out the packet queue then 
      // sending the withdrawal ADMIN packet
      withdrawFromBus();

      // remove this handler from the network service by clearing out selection
      // keys and closing channels
      key.cancel();
      key.attach( null );
      try {
        key.channel().close();
      } catch ( final IOException ignore ) {}
    }

    // We are no-longer performing I/O so we are offically shutdown.
    shutdown = true;
  }




  /**
   * @return  Returns true if the bus has been shutdown, false otherwise.
   */
  public boolean isShutdown() {
    return shutdown;
  }




  /**
   * Wait for the bus to shutdown.
   * 
   * @param timeout the number of milliseconds to wait for the bus.
   */
  public void join( final long timeout ) {
    final long tout = System.currentTimeMillis() + timeout;
    while ( System.currentTimeMillis() < tout ) {
      try {
        Thread.sleep( 10 );
      } catch ( final InterruptedException e ) {}

      // break when we are flagged as shutdown
      if ( shutdown ) {
        break;
      }

    } // while

  }




  /**
   * Wrap the message in a packet and queue it for sending.
   * 
   * <p>This call does NOT send the message immediately, it only queues it up for
   * transmission by the NetworkService thread when it has the opportunity to
   * perform a write operation on the network channel.</p>
   *
   * @param message the Message to send
   */
  public void send( final Message message, final short type ) {
    // if we have a message...
    if ( message != null ) {
      // ...create a Packet in which it can be transported
      final Packet packet = new Packet();

      // Set the packet type to message
      packet.type = type;

      // Create a copy of the Message so it does not get changed by another
      // component (like the original sender) while the message is sitting in
      // the send queue.
      packet.message = (Message)message.clone();

      // packet queue is internally synchronized
      packetQueue.add( packet );
      LOG.append( "Placed packet " + packet + " in queue for sending; size=" + packetQueue.size() + " packets" );

      // Make sure we are interested in the Write operation
      if ( ( key != null ) && key.isValid() ) {
        key.selector().wakeup();
        key.interestOps( key.interestOps() | SelectionKey.OP_WRITE ); // causes thread lock on Sun!
      }
    }
  }




  /**
   * Create a channel that uses the source address of this PacketBus instance.
   * 
   * <p>This is a convenience method for those components that use this object 
   * on its own.</p>
   * 
   * <p>The values the for the channels message address are based on the IP Address 
   * and Port of the main message channel passed to the constructor. The endpoint 
   * is the negiotiated endpoint identifier and the channel identifier is the 
   * passed argument.</p> 
   *
   * @param chnlId The channel identifier to use for the returned channel.
   * 
   * @return A channel that has its source address assigned to this message bus.
   */
  public MessageChannel createChannel( final int chnlId ) {
    // Create a new channel using the given handler
    final MessageChannel retval = new MessageChannel();

    // set the address to our endpoint
    retval.address = new MessageAddress( MessageChannel.address.getAddress(), MessageChannel.address.getPort(), endpoint, chnlId );

    // return the newly-created channel
    return retval;
  }




  /**
   * Return the URI on which we want to listen.
   * 
   * <p>This allows the network service know what kind of socket to create for 
   * us. Normally on the scheme of the URI is important, but sometimes the 
   * address is important as in the cases of multicast groups and broadcast 
   * addresses.</p>
   * 
   * @see coyote.mbus.network.NetworkServiceHandler#getServiceUri()
   *
   * @return A URI with a scheme that is recogizable so a socket may be created
   *         to support this handler.
   */
  public URI getServiceUri() {
    return busUri;
  }




  /**
   * Method setChannel
   *
   * @param channel
   */
  public void setChannel( final AbstractSelectableChannel channel ) {
    datagramChannel = (DatagramChannel)channel;
  }




  /**
   * Return the reference to our DatagramChannel.
   * 
   * @see coyote.mbus.network.NetworkServiceHandler#getChannel()
   */
  public AbstractSelectableChannel getChannel() {
    return datagramChannel;
  }




  /**
   * @see coyote.mbus.network.NetworkServiceHandler#setKey(java.nio.channels.SelectionKey)
   * @param  key
   */
  public void setKey( final SelectionKey key ) {
    this.key = key;
  }




  /**
   * Return our NIO selection key.
   */
  public SelectionKey getKey() {
    return key;
  }




  /**
   * Accepts should not occur in UDP implementations.
   *
   * @see coyote.mbus.network.NetworkServiceHandler#accept(java.nio.channels.SelectionKey)
   *
   * @param key
   */
  public void accept( final SelectionKey key ) {}




  /**
   * There are no connection in this UDP implementation.
   *
   * @see coyote.mbus.network.NetworkServiceHandler#connect(java.nio.channels.SelectionKey)
   *
   * @param key
   */
  public void connect( final SelectionKey key ) {}




  /**
   * This method will be called whenever data is ready to be read from the UDP
   * receive buffers.
   * 
   * <p>This method will read data from the receive buffers.</p>
   *
   * @see coyote.mbus.network.NetworkServiceHandler#read(java.nio.channels.SelectionKey)
   *
   * @param key
   */
  public void read( final SelectionKey key ) {
    byte[] data;
    Packet packet = null;
    InetSocketAddress address;

    LOG.append( "MessageBus.read()" );

    try {
      // CAREFUL! This will read multiple messages if they are in the UDP buffer
      while ( ( address = (InetSocketAddress)datagramChannel.receive( buffer ) ) != null ) {
        buffer.flip();

        LOG.append( buffer.remaining() + " bytes of data received from " + address );

        if ( buffer.remaining() > 0 ) {
          data = new byte[buffer.remaining()]; // allocate a byte array

          buffer.get( data ); // copy the data into the buffer
          buffer.clear(); // clear out the buffer

          try {
            packet = new Packet( data );
            packet.remoteAddress = address.getAddress();
            packet.remotePort = address.getPort();

            LOG.append( "Received " + packet + " from " + packet.remoteAddress + ":" + packet.remotePort );
            if ( packet.message != null ) {
              LOG.append( "Received packet contained a message:\r\n" + packet.message.toString() );
            }

            // Only process packets from other endpoints...
            if ( packet.endPoint != this.endpoint ) {
              // Get the RemoteNode representing the node from which this 
              // message came
              RemoteNode node = nodes.get( new Long( packet.endPoint ) );

              // If there is no node on record...
              if ( node == null ) {
                // Create a new RemoteNode with a reference to the bus channel
                // so it can route the packet to all the channels it manages
                node = new RemoteNode( this, this.endpoint, cache, packet.endPoint, address );

                // Set loggers
                node.setLogAppender( LOG );
                node.setErrorAppender( ERR );

                // record the nodes UDP address
                node.udpAddress = address.getAddress();
                node.udpPort = address.getPort();

                // place it in our map of node identifiers to nodes
                nodes.put( new Long( packet.endPoint ), node );
              }

              // Check for duplicate endpoints by checking the source address.
              // Some multi-homed hosts may get confused and send messages over
              // both their interfaces...Ignore duplicates!
              if ( address.getAddress().equals( node.udpAddress ) ) {
                // Have the node process the packet as it has all the details
                // regarding how to process the packet with respect to that node
                node.processPacket( packet );
              } else {
                LOG.append( "Detected duplicate endpoint " + packet.endPoint + " first observed on " + node.udpAddress + " now observed on " + address.getAddress() );
              }

            } // if remote endpoint

            if ( packet.type == Packet.ADMIN ) {
              // Handle administration packets
              processAdminPacket( packet );
            }

          } catch ( final Exception ex ) {
            LOG.append( "Message from " + address + " was not a valid message packet " + ex.getMessage() );
            LOG.append( ByteUtil.dump( data, data.length ) );
            LOG.append( ExceptionUtil.stackTrace( ex ) );
          }
        } else {
          LOG.append( "Read from datagram channel, but no bytes were there - not bad, but wierd." );
        }
      }
    } catch ( final IOException ignore ) {
      if ( !shutdown )
        ERR.append( "Exception on read(): " + ignore.getMessage() + "\r\n" + ExceptionUtil.stackTrace( ignore ) );
    }
  }




  /**
   * Write to the network any packets that appear in our packet queue.
   * 
   * <p>This will drain our packet queue.</p>
   * 
   * @see coyote.mbus.network.NetworkServiceHandler#write(java.nio.channels.SelectionKey)
   *
   * @param key
   */
  public void write( final SelectionKey key ) {
    LOG.append( "MicroBus.write()" );

    // if we are inserted into the bus (have a BusID assigned and negotiated)
    if ( inserted > 0 ) {
      LOG.append( "MicroBus.write() sending packets from queue of " + packetQueue.size() + " packets waiting to be sent" );
      // while we have any packets in our outbound queue
      while ( packetQueue.size() > 0 ) {
        // get next packet in the queue (should not block inside while block)
        final Packet packet = packetQueue.next();

        // send the packet over the network
        send( packet );

      } // while packets in the outbound queue

      LOG.append( "MicroBus.write() sent all packets in outbound queue; size is now " + packetQueue.size() );

      // / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / /
      // now check the NodeCaches for possible missing messages, and if we haven't
      // received the missing messages in XX milliseconds, then assumed a missed
      // message and process all the remaining messages in their proper order,
      // reporting a packet loss for any missing messages.
      // / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / /

    }
    LOG.append( "MicroBus.write() completed sending packets" );

    // Inform the selector that we no-longer need write operations
    if ( key.isValid() ) {
      key.interestOps( key.interestOps() & ~SelectionKey.OP_WRITE );
    }
  }




  /**
   * Process the administration packet received from the network.
   * 
   * @param packet The packet to process.
   */
  private void processAdminPacket( final Packet packet ) {
    LOG.append( "processAdminPacket: Admin packet from  " + packet.remoteAddress + " ep:" + packet.endPoint );
    if ( packet.message != null ) {
      LOG.append( "processAdminPacket: MSG:\r\n" + packet.message.toXml() );
      DataField field = packet.message.getField( OamMessage.ACTION );

      if ( field != null ) {
        // We have an action field which means this follows the OAM 
        // formatting standard
        final String action = field.getObjectValue().toString();

        // INSERT / / / / / / / / / / / / / / / / / / / / / / / / / / 
        if ( OamMessage.INSERT.equals( action ) ) {
          LOG.append( "processAdminPacket: Processing INSERT packet from " + packet.remoteAddress );
          field = packet.message.getField( OamMessage.TOKEN );
          long tokn = 0;
          if ( field != null ) {
            try {
              tokn = Long.parseLong( field.getObjectValue().toString() );
              LOG.append( "processAdminPacket: Parsed token as " + tokn + " ours=" + token );
            } catch ( final RuntimeException ignore ) {
              // can't complain since the token is not an integer and we use an
              // integer to specify our token. This must be from another node.
              // Log.warn( "Token '" + field.getStringValue() + "' was not numeric" );
              return;
            }
          }

          // check to make sure it is not a duplicate
          if ( packet.remoteAddress.equals( MessageChannel.address.getAddress() ) && ( tokn == token ) ) {
            // this came from this host, check the token
            LOG.append( "processAdminPacket: Ignoring our own INSERT" );
          } else {
            LOG.append( "processAdminPacket: Processing remote INSERT: packet.endpoint=" + packet.endPoint + " ours=" + endpoint );

            if ( packet.endPoint == this.endpoint ) {
              LOG.append( "processAdminPacket: CONFLICT! " + endpoint );
              // Send an ARP indicating we have the endpoint
              final Packet admin = new Packet();
              admin.type = Packet.ADMIN;

              // Create a Message message
              final Message message2 = OamMessage.createArpMessage( MessageChannel.address.getAddress().getHostAddress(), this.endpoint, token, MessageChannel.address.getAddress().getHostAddress(), this.endpoint );
              message2.setTarget( new MessageAddress( packet.remoteAddress, packet.remotePort, packet.endPoint, 0 ) );

              // Add the message to the packet
              admin.message = message2;

              // Send the packet
              send( admin );
            } else {
              LOG.append( "processAdminPacket: INSERTION passed." );
            }
          }
        }

        // / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / /
        // WITHDRAW / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / 
        else if ( OamMessage.WITHDRAW.equals( action ) ) {
          LOG.append( "processAdminPacket: Processing WITHDRAW packet" );
          // remove the node, mark it as expired
        }

        // / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / /
        // ARP / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / /
        else if ( OamMessage.ARP.equals( action ) ) {
          LOG.append( "processAdminPacket: Processing ARP packet" );
          field = packet.message.getField( OamMessage.TOKEN );
          long tokn = 0;
          if ( field != null ) {
            LOG.append( "processAdminPacket: Parsing token of '" + field.getObjectValue().toString() + "'" );
            try {
              tokn = Long.parseLong( field.getObjectValue().toString() );
              LOG.append( "processAdminPacket: Parsed token as " + tokn + " ours=" + token );
            } catch ( final RuntimeException ignore ) {
              // can't complain since the token is not an integer and we use an
              // integer to specify our token. This must be from another node.
              // Log.warn( "Could not parse token of '" + field.getStringValue() + "' into an integer" );
              return;
            }
          }

          // check to make sure it is not a duplicate
          if ( packet.remoteAddress.equals( MessageChannel.address.getAddress() ) && ( tokn == token ) ) {
            // this came from this host and runtime instance
            LOG.append( "processAdminPacket: Ignoring our own ALERT" );
          } else {
            field = packet.message.getField( OamMessage.SOURCE_ENDPOINT );
            // if this endpoint is in conflict
            if ( ( field != null ) && field.isNumeric() ) {
              try {
                if ( this.endpoint == ( (Long)field.getObjectValue() ).longValue() ) //field.getAsLong() )
                {
                  LOG.append( "processAdminPacket: Processing remote ARP: packet.endpoint=" + packet.endPoint + " ours=" + endpoint + " token:" + tokn + " ours=" + token );
                  if ( ( inserted + MessageBus.INSERTION_INTERVAL ) > System.currentTimeMillis() ) {
                    LOG.append( "processAdminPacket: Still in insertion phase, re-inserting into network with new endpoint" );
                    insertIntoBus();
                  } else {
                    LOG.append( "processAdminPacket: No longer in insertion phase: instd:" + inserted + " intvl:" + MessageBus.INSERTION_INTERVAL + " <= " + System.currentTimeMillis() );
                  }
                }
              } catch ( final Exception e ) {
                // Apparently the endpoint was not a long, therefore it is not 
                // from this instance as we use a long to represent our token
              }

            } // if endpoint is numeric

          } // packet came from another runtime instance

        } // if ARP message

        // / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / /

      } // field
    } // message
  }




  /**
   * Send the given packet over the network socket to the intended target,
   * placing the proper sequence identifier in the packet.
   * 
   * <p>Used to send information over the transport medium to affect a
   * communications protocol via PacketPackets. Packets are sequenced, timestamped
   * sent over the sockets and cached for retransmission.</p>
   *
   * @param packet The packet that is to be sent to the transport medium to affect
   *        the communications protocol implemented by the caller.
   */
  public void send( final Packet packet ) {
    LOG.append( "MicroBus.send(Packet) packet: " + packet );

    if ( packet != null ) {
      SocketAddress destination = null;

      // Only cache and renumber MSG and ADMIN messages
      if ( ( packet.type == Packet.MSG ) || ( packet.type == Packet.ADMIN ) ) {
        // Set the packet sequence from this node
        packet.sequence = sequence++;

        // Set the time this packet was sent adjusting for Time Zone and DST. This
        // calculation has to be performed here as this component may be running
        // when DST changes, otherwise we could make it a constant.
        packet.timestamp = MessageBus.cal.getTimeInMillis() + MessageBus.cal.get( Calendar.ZONE_OFFSET ) - MessageBus.cal.get( Calendar.DST_OFFSET );

        // Make sure we have a source address
        if ( packet.getSourceAddress() == null ) {
          if ( packet.message != null ) {
            ( packet.message ).setSource( new MessageAddress( MessageChannel.address.getAddress(), MessageChannel.address.getPort(), endpoint, -1 ) );
            LOG.append( "MicroBus.send(Frame) set source address as " + ( packet.message ).getSource().toString() );
          }
        }

        // place the packet in the cache before sending
        cache.add( packet );
        LOG.append( "MicroBus.send(Packet) Placed packet " + packet + " in resend cache; size=" + cache.size() + " packets" );
      }

      // Set the bus node that sent this packet to our bus identifier
      packet.endPoint = endpoint;

      final MessageAddress target = packet.getTargetAddress();

      // If there is a target address, send it there (unicast)
      if ( target != null ) {
        // use the target address to get the host of the destination, but not 
        // the port because the port may be different since it represents the 
        // TCP port and not the UDP port.
        destination = new InetSocketAddress( target.getAddress(), busUri.getPort() );
      } else {
        // else, broadcast the data on the bus
        destination = bcastaddr;
      }

      String line = "MicroBus.send(Packet) Sending " + packet.toString() + "  to ----------> " + destination;
      if ( packet.message != null ) {
        line = new String( line + "\r\n" + packet.message.toString() );
      }
      LOG.append( line );

      // send the packet on the network
      try {
        final int sent = datagramChannel.send( ByteBuffer.wrap( packet.getBytes() ), destination );

        // Important logging of synchronization delivery messages
        if ( Packet.NAK == packet.getType() ) {
          LOG.append( "NAK sent to " + destination.toString() + ":\n" + packet.toString() );
        } else if ( Packet.RETRANSMIT == packet.getType() ) {
          LOG.append( "Successful RETRANSMIT to " + destination.toString() + ":\n" + packet.toString() );
        } else if ( Packet.RETRANSMIT == packet.getType() ) {
          LOG.append( "EXPIRED message sent to " + destination.toString() + ":\n" + packet.toString() );
        }

        LOG.append( "MicroBus.send(Frame) Sent " + sent + " bytes to " + destination );

      } catch ( final ClosedChannelException e ) {
        // ClosedChannelException - If this channel is closed
        //        if( !MicroBus.isShutdown() )
        //        {
        //          ERR.append( "Channel closed before sending packet " + packet.sequence );
        //        }
      } catch ( final Throwable e ) {
        ERR.append( "Could not send packet " + packet.sequence + ": " + e.getClass().getName() + ": " + e.getMessage() + "\r\n" + ExceptionUtil.stackTrace( e ) );
        // AsynchronousCloseException - If another thread closes this channel while the read operation is in progress
        // ClosedByInterruptException - If another thread interrupts the current thread while the read operation is in progress, thereby closing the channel and setting the current thread's interrupt status
        // SecurityException - If a security manager has been installed and it does not permit datagrams to be sent to the given address
        // IOException - If some other I/O error occurs
      }
    }

    LOG.append( "MicroBus.send(Packet) completed sending datagram" );
  }




  /**
   * Insert ourselves into the bus.
   * 
   * <p>This method generates a pseudo-random number representing our intended
   * endpoint identifier. It then sends and ADMIN packet with an action of
   * &quot;INSERT&quot; which acts to notify all the participants on the bus
   * that this node is using a particular endpoint identifier.</p>
   */
  private void insertIntoBus() {
    // Create a bus identifier at random, Java uses signed integer
    endpoint = generator.nextInt();

    // make sure it is positive
    if ( endpoint < 0 ) {
      endpoint *= -1;
    }

    LOG.append( "Inserting with an endpoint of " + endpoint );

    // (re)set our sequence to zero
    sequence = 0;

    // (re)set our heartbeat time
    nextHeartbeat = System.currentTimeMillis() + heartbeatInterval;

    // Create a new Packet packet
    final Packet admin = new Packet();

    // Set the packet type to admin
    admin.type = Packet.ADMIN;

    // Set the packet's message to our insertion OAM message
    admin.message = OamMessage.createInsertionMessage( Long.toString( endpoint ), Long.toString( token ) );

    // send the packet over the network channel, we will change our endpoint if
    // we later receive a conflicting ARP packet
    send( admin );

    // Create a new address based upon our existing socket address and port, 
    // inserted endpoint identifier and existing channel identifier
    if ( ( tcpService != null ) && ( tcpService.getKey() != null ) ) {
      // try to use the actual values of the initialized TCP service
      MessageChannel.setAddress( new MessageAddress( tcpService.getAddress(), tcpService.getPort(), endpoint, MessageChannel.address.getChannelId() ) );
    } else {
      // use what was previously set
      MessageChannel.setAddress( new MessageAddress( MessageChannel.address.getAddress(), MessageChannel.address.getPort(), endpoint, MessageChannel.address.getChannelId() ) );
    }
    LOG.append( "NetworkService Message Address: " + MessageChannel.getAddress() );

    // We are inserted by default
    inserted = System.currentTimeMillis();
  }




  /**
   * Send an message on the bus indicating this node is no-longer participating 
   * on the bus.
   * 
   * <p>This is a courtesy notification only. If it is lost, it can not be
   * retransmitted as the sender will not be around to receive the NAK, let
   * alone re-send the packet.</p>
   */
  void withdrawFromBus() {
    // clear out our outbound queue prior to withdrawal
    flush();

    // Create a new Packet packet
    final Packet admin = new Packet();

    // Set the packet type to admin
    admin.type = Packet.ADMIN;

    // The the packet's message to our insertion OAM message
    admin.message = OamMessage.createWithdrawalMessage( Long.toString( endpoint ) );

    // send the packet over the network channel
    send( admin );
  }




  /**
   * Try to flush our outbound messages to the bus
   */
  void flush() {
    if ( packetQueue.size() > 0 ) {
      // Try to flush our outbound messages to the bus
      try {
        while ( packetQueue.size() > 0 ) {
          write( key );
        }
      } catch ( final Exception ignore ) {
        // best effort...usually during shutdown so there is nothing to do
      }
    }
  }




  /**
   * Broadcast a heartbeat packet to all nodes that are connected to the same
   * Message bus to which we are connected.
   * 
   * <p>This method writes directly to the datagram channel sending a single
   * heartbeat packet to the current broadcast address. There is no caching of
   * the packet.</p>
   */
  void sendHeartbeat() {
    // Create a new packet
    final Packet packet = new Packet();

    // set our PacketBus Identifier
    packet.endPoint = endpoint;

    // send the previous packet sequence
    packet.sequence = sequence - 1;

    // set the packet type
    packet.type = Packet.HEARTBEAT;

    // Create a heartbeat message if we haven't already done so.
    if ( heartbeatPacket == null ) {
      heartbeatPacket = OamMessage.createHeartbeatMessage( MessageChannel.address.getAddress().getHostAddress(), MessageChannel.address.getPort() );
    }

    // Attach an OAM message that represents our current status
    packet.message = heartbeatPacket;

    try {
      // send the packet on the broadcast address of the Message port
      datagramChannel.send( ByteBuffer.wrap( packet.getBytes() ), bcastaddr );
      LOG.append( "MessageBus.sendHeartbeat: sent heartbeat - last packet sent = " + packet.sequence );
    } catch ( final IOException e ) {
      ERR.append( "Could not send heartbeat: " + e.getMessage() );
    }

    // Set the time for the next heartbeat
    nextHeartbeat = System.currentTimeMillis() + heartbeatInterval;
  }




  /**
   * Message Sink call-back method.
   * 
   * <p>Whenever an message is received through this object's message channel, 
   * this method is called; placing the message in a packet and placing it in the 
   * packet queue for later sending.</p>
   * 
   * @param msg The message to process.
   */
  public void onMessage( final Message msg ) {
    if ( msg instanceof ClosureMessage ) {
      // Time to shutdown!
      shutdown();
    } else {
      send( msg, Packet.MSG );
    }
  }




  /**
   * Send the message to the Message Manager when it is received.
   * 
   * <p>This method is usually called by the RemoteNode instance when a message
   * has been successfully received in the best possible sequence.</p>
   *
   * @param message The message to pass to the Message Manager.
   */
  public void process( final Message message ) {
    // This message came from the bus, set the source channel to prevent loops
    message.sourceChannel = MessageChannel;

    // place the message on our channel, if there is a message listener, then the
    // message will be passed to the onPacket method of the listener, otherwise 
    // it will be placed in the inbound queue and the channelReceive method 
    // will be called on any and all channel listeners so they can perform some 
    // type of processing on the message.
    MessageChannel.receive( message );
  }




  /**
   * Return a list of the currently discovered nodes.
   * 
   * <p>This returns an ArrayList of RemoteNode objects currently being ued by 
   * this PacketBus instance to track messages from the various nodes in the 
   * framework. <strong>These nodes should not be changed in any way!</strong> 
   * These nodes are NOT synchronized and are constantly being updated by the 
   * messages being received by the PacketBus. While it is generally safe to 
   * access the RemoteNode instances in a read-only fashion, changing any of 
   * the members of the RemoteNode instance can cause message synchronization 
   * errors.</p>
   *
   * @return A List of RemoteNode objects representing all the discovered nodes
   *         in the network.
   */
  public List<RemoteNode> getNodeIterator() {
    synchronized( nodes ) {
      final ArrayList<RemoteNode> list = new ArrayList<RemoteNode>();

      for ( final Iterator<RemoteNode> it = nodes.values().iterator(); it.hasNext(); list.add( it.next() ) ) {
        ;
      }

      return list;
    }
  }




  /**
   * @return A string representation of the object.
   */
  public String toString() {
    final StringBuffer retval = new StringBuffer( "Bus" );
    if ( endpoint > -1 ) {
      retval.append( "(" );
      retval.append( endpoint );
      retval.append( ")" );
    } else {
      retval.append( ' ' );
    }
    retval.append( busUri );
    retval.append( "[" );
    retval.append( bcastaddr );
    retval.append( "] seq=" );
    retval.append( sequence );

    return retval.toString();
  }




  /**
   * @return  The Message that is broadcast as a heartbeat.
   */
  public Message getHeartbeatPacket() {
    return heartbeatPacket;
  }




  /**
   * @return  Returns the heartbeatInterval in milliseconds.
   */
  public long getHeartbeatInterval() {
    return heartbeatInterval;
  }




  /**
   * @param millis  The number of milliseconds between heartbeats.
   */
  public void setHeartbeatInterval( final long millis ) {
    this.heartbeatInterval = millis;
  }




  /**
   * @return the number of packets waiting to be written to the bus.
   */
  public int getOutboundQueueDepth() {
    return packetQueue.size();
  }




  public void fireGroupJoined( final String group, final MessageChannel channel ) {
    LOG.append( channel + " joined the '" + group + "' group" );
    send( OamMessage.createJoinMessage( group ), Packet.ADMIN );
  }




  public void fireGroupLeave( final String group, final MessageChannel channel ) {
    LOG.append( channel + " left the '" + group + "' group" );
    send( OamMessage.createLeaveMessage( group ), Packet.ADMIN );
  }




  /**
   * @return the iterator over the Remote Nodes discoverd so far.
   */
  public Iterator<RemoteNode> getRemoteNodeIterator() {
    return nodes.values().iterator();
  }




  /**
   * A simple check to see if the Bus is operational.
   * 
   * <p>This method will check to see if the bus is ready of I/O by seeing if
   * it has sent its insertion message and that its sockets are ready.</p>
   * 
   * @return true if the bus is inserted and the selection key is valid, false
   *         otherwise
   */
  public boolean isReady() {
    return ( ( inserted > 0 ) && ( key != null ) && key.isValid() );
  }




  /**
   * Setup a dependency on the given TCP service, not initializing until the  service is operational.
   * @param svc  The server on which to depend.
   */
  public void setTcpService( final MessageService svc ) {
    this.tcpService = svc;
  }

  /**
   * The HeartbeatTask is run by the HeartbeatTimer once each second.
   */
  class HeartbeatTask extends TimerTask {
    /**
     * Go through all the received chunks of data and attempt to create Message
     * Packets from them.
     * 
     * <p>Pass each message packet through it's respective RemoteNode instance to 
     * affect reliable delivery.</p>
     */
    public void run() {
      // now see if there is data to send,
      if ( packetQueue.size() > 0 ) {
        // if the key isn't valid for some reason, then send them from here!
        if ( key.isValid() ) {
          key.interestOps( key.interestOps() | SelectionKey.OP_WRITE );
        } else {
          numberOfTimesInvalidKeyWasObserved++;
          write( key );
        }
      }

      // If it is time to send a heartbeat packet, do so
      if ( nextHeartbeat <= System.currentTimeMillis() ) {
        // expire messages every time we send a heartbeat
        cache.expire( 60000 );// MicroBus.EXPIRATION_INTERVAL );

        if ( numberOfTimesInvalidKeyWasObserved > 0 ) {
          System.err.println( "Invalid key count=" + numberOfTimesInvalidKeyWasObserved );
          numberOfTimesInvalidKeyWasObserved = 0;
        }

        // send the actual heartbeat
        sendHeartbeat();

        // Now clean up any old nodes in memory
        synchronized( nodes ) {
          for ( final Iterator it = nodes.values().iterator(); it.hasNext(); ) {
            final RemoteNode node = (RemoteNode)it.next();

            // if the node has expired or withdrawn, remove it
            if ( node.isWithdrawn() || node.isExpired() ) {
              node.close();
              it.remove();
            } // remove withdrawn/expired nodes
          } // for each node
        } // sync
      } // if heartbeat
    } // run
  } // timertask

}
