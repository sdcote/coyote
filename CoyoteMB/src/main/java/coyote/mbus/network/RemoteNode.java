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
import java.net.URI;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.ArrayList;

import coyote.commons.ExceptionUtil;
import coyote.mbus.LogAppender;
import coyote.mbus.NullLogAppender;
import coyote.mbus.message.Message;


/**
 * RemoteNode is a class used by MicroBus nodes for tracking the state of 
 * remote MicroBus nodes discovered on or directly connected to the network.
 * 
 * <p>UDP services use RemoteNodes to handle frames received from the 
 * network.</p>
 * 
 * <p>Only the sequencing portion of the protocol is handled in this class, and 
 * therefore only the MSG and HEARBEAT frames are passed to this class.</p>
 */
public class RemoteNode {
  /** Our logging category */
  public static final String LOG_CATEGORY = "RMTNODE";

  /** Tag used in various class identifying locations like DataCapsule nodes */
  public final String CLASS_TAG = "RemoteNode";

  /** Field udpAddress */
  InetAddress udpAddress = null;

  /** Field udpPort */
  int udpPort = 0;

  /** The IP address on which the remote node is listening for packet sessions. */
  InetAddress tcpAddress = null;

  /** The IP port on which the remote node is listening for packet sessions. */
  int tcpPort = -1;

  /** The URI on which the remote node listens for TCP connections */
  URI serviceUri = null;

  /** Field channels */
  ArrayList channels = new ArrayList();

  /** The Endpoint Identifier for the remote node this object represents */
  long remoteEndPoint = 0;

  /** Last packet sequence identifier received */
  long lastPacket = -1;

  /** The MessageMediator we are to use to route packets */
  MessageMediator packetTransport = null;

  /**
   * The end point identifier of the this node, against which we compare other 
   * endpoints to determine their locality and endpoint collisions.
   */
  private long localEndPoint = 0;

  /** The cache of all the Packets the host has sent so far (for NAK processing) */
  PacketQueue packetCache = null;

  /** The last heartbeat packet received from the remote node */
  Message lastHeartbeat = null;

  /** The time when the node was last seen as evidenced by a received packet. */
  volatile long lastSeen = 0;

  /** The time when the node was last seen as evidenced by a received packet. */
  volatile long firstSeen = 0;

  /** The number of milliseconds (90 sec.) between frames when a node is considered to have just "disappeared". */
  public static long EXPIRATION_TIMEOUT = 90000;

  /** Flag indicating the node has withdrawn from the framework */
  volatile boolean withdrawn = false;

  /** 
   * Flag indicating that this node represents a peer on the local subnet as 
   * opposed to a remote node that was discovered via receipt of a unicast 
   * packet or configuration. 
   */
  boolean peer = false;

  /** The buffer of messages we use to re-order and maintain proper sequence. */
  PacketBuffer packetBuffer = new PacketBuffer();

  /** Flag indicating this node is requesting too many retransmissions. */
  private final boolean cryBaby = false;

  public static final short NAK_COUNT_LIMIT = 3;
  private volatile short nakCount = 0;
  private volatile long nakTotal = 0;
  private DatagramChannel channel = null;
  private InetSocketAddress endpointSocketAddress = null;

  /** The object we use to append log messages */
  private LogAppender LOG = new NullLogAppender();

  /** Error messages are logged here */
  private LogAppender ERR = new NullLogAppender();




  /**
   * Constructor
   *
   * @param transport The transport responsible for routing messages.
   * @param lep The local endpoint identifier (the local node)
   * @param cache The cache of packets we have previously sent from this node
   * @param rep The remote endpoint identifier (what we logically represent)
   * @param sa the Socket Address of the node for point-to-point communication
   */
  RemoteNode( final MessageMediator transport, final long lep, final PacketQueue cache, final long rep, final InetSocketAddress sa ) {
    if ( transport == null ) {
      throw new IllegalArgumentException( "MessageMediator argument was null" );
    }

    if ( cache == null ) {
      throw new IllegalArgumentException( "PacketQueue cache argument was null" );
    }

    packetTransport = transport;
    localEndPoint = lep;
    remoteEndPoint = rep;
    packetCache = cache;
    firstSeen = System.currentTimeMillis();
    endpointSocketAddress = sa;

    try {
      channel = DatagramChannel.open();
      channel.connect( endpointSocketAddress );
    } catch ( final IOException e ) {
      ERR.append( "Could not establish a datagram channel to " + endpointSocketAddress + " - " + e.getMessage() );
    }

  }




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
   * Process the given packet.
   * 
   * @param packet The packet that this node should process.
   */
  void processPacket( final Packet packet ) {
    try {
      if ( packet != null ) {
        lastSeen = System.currentTimeMillis();

        LOG.append( "Processing packet " + packet + " for remote node " + remoteEndPoint + " at address " + endpointSocketAddress );

        // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
        // The most common packet is the message which contains application level
        // data. Simply check the sequencing and pass to the transport for 
        // processing. Normally processing involves routing the message for 
        // delivery to the mediator listeners.
        if ( packet.type == Packet.MSG ) {
          LOG.append( "Received a message packet #" + packet.sequence + " - " + packet.toString() );

          // always allow frame 1 which implies a reset in sequencing
          if ( ( lastPacket + 1 != packet.sequence ) && ( packet.sequence != 1 ) ) {
            // if this node has an established last sequence value...
            if ( lastPacket > -1 ) {
              // and if that that last sequence is greater than this packet we 
              // just received...
              if ( lastPacket + 1 > packet.sequence ) {
                // possible duplicate packet; log the duplication
                LOG.append( "Frame OOS from " + toString() + " - expected " + ( lastPacket + 1 ) + " received " + packet.sequence + " - Ignoring duplicate frame" );
              } else {
                LOG.append( "Message packet reports dropped packet from " + toString() + " - expected " + ( lastPacket + 1 ) + " received " + packet.sequence + " difference of " + ( packet.sequence - ( lastPacket + 1 ) ) + " frames." );

                // looks like at least one frame is missing; Send a NAK for 
                // expected frame
                // packetTransport.nak( lastPacket + 1, frame );
                nak( lastPacket + 1 );

                // buffer the packet creating placeholders for any missing frames
                packetBuffer.buffer( packet );

                LOG.append( "Frame OOS from " + toString() + " - expected " + ( lastPacket + 1 ) + " received " + packet.sequence + " - Sent a NAK for missing frame(s) - Packet buffer now contains " + packetBuffer.size() + " entries:\n" + packetBuffer.dump() );

              }
            } else {
              // This is a new node, so use the present packet sequence as our 
              // starting point
              lastPacket = packet.sequence;

              if ( packet.message != null ) {
                packet.message.setTimestamp( lastSeen );
                packetTransport.process( packet.message );
              }

            } // if new node or not

          } else {
            // check to see if we have any frames in the buffer. This indicates 
            // we have at least one outstanding packet and we can not pass this 
            // packet to the transport until the buffer is complete with all 
            // packets in their proper sequence
            if ( packetBuffer.size() > 1 ) {
              packetBuffer.buffer( packet );
              LOG.append( "There are outstanding frames to be received, buffering frame for sequenced delivery - Packet buffer now contains " + packetBuffer.size() + " entries:\n" + packetBuffer.dump() );
            } else {
              // Happy path... Received the expected packet from the remote node 
              // in its proper sequence with no outstanding packets expected.

              // Update the sequence identifiers
              lastPacket = packet.sequence;

              // If there is a packet for processing...
              if ( packet.message != null ) {
                // ...timestamp the packet...
                packet.message.setTimestamp( lastSeen );

                // .. pass the packet to the transport for processing.
                packetTransport.process( packet.message );
              }
            }
          } // OOS check

          LOG.append( "Completed message processing" );

        } // if packet

        // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
        // Heartbeats are probably the next most frequent packet received and keep
        // nodes in sync with each other. These packets usually follow a standard 
        // OAM formatting convention where standard names and types are used 
        // consistently across all OAM messages.
        else if ( packet.type == Packet.HEARTBEAT ) {
          LOG.append( "Processing HEARTBEAT packet " + packet + " for remote node " + remoteEndPoint + " at address " + endpointSocketAddress );

          // Perform a sequence check
          if ( lastPacket != packet.sequence ) {
            if ( lastPacket > -1 ) {
              if ( lastPacket < packet.sequence ) {
                LOG.append( "Heartbeat reports dropped packet from " + toString() + " - expected " + ( lastPacket + 1 ) + " received " + packet.sequence + " difference of " + ( packet.sequence - ( lastPacket + 1 ) ) + " frames." );
                nak( lastPacket + 1 );
              } else {
                LOG.append( "Heartbeat reports packet decremented counter - expected " + ( lastPacket + 1 ) + " received " + packet.sequence + " difference of " + ( lastPacket - ( lastPacket + 1 ) ) + " packets. Ignoring heartbeat sequence and will wait for next exoected packet." );

                // It is possible that the packets are being sent out of order
                // or the remote node lost its mind. Current strategy is to
                // honor the sequence number
              }
            } else {
              // happy path
              lastPacket = packet.sequence;
            } // new node check

          } // OOS check

          // process heartbeat packet as it will contain useful state data
          if ( packet.message != null ) {
            if ( ( tcpAddress == null ) || ( tcpPort < 0 ) ) {
              // try to retrieve TCP address:port by placing the message in a
              // special OAM message which contain handy accessor methods 
              final OamMessage oam = new OamMessage( packet.message );

              if ( tcpAddress == null ) {
                final String addr = oam.getTcpAddress();

                try {
                  tcpAddress = InetAddress.getByName( addr );
                } catch ( final UnknownHostException e ) {
                  tcpAddress = udpAddress;

                  e.printStackTrace();
                }
              } // address

              if ( tcpPort < 0 ) {
                try {
                  tcpPort = Integer.parseInt( oam.getTcpPort() );
                } catch ( final NumberFormatException e ) {
                  tcpPort = 0;
                }
              } // port

            } // if TCP addr:port  data is not recorded fro this node

          } // if there is data in the heartbeat packet

          // save a reference to the last heartbeat as it may contain user data
          lastHeartbeat = packet.message;

          LOG.append( "Completed HEARTBEAT processing" );

        }

        // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
        // Retransmit frames answer our NAK frames
        else if ( packet.type == Packet.RETRANSMIT ) {
          LOG.append( "Received a RETRANSMIT packet #" + packet.sequence + " - " + packet.toString() );

          // reset our NAK count so we give the sender more time to resend data
          nakCount = 0;

          // if this is for a packet later in the sequence
          if ( packet.sequence > lastPacket ) {
            LOG.append( "Placing RETRANSMIT packet #" + packet.sequence + " in buffer" );

            // place the frame in the proper location in the buffer
            packetBuffer.buffer( packet );
            LOG.append( "Packet buffer now contains " + packetBuffer.size() + " entries:\n" + packetBuffer.dump() );
          } else {
            // else ignore the frame we have already received
            LOG.append( "Ignoring RETRANSMIT packet #" + packet.sequence + "; have already received packet" );
          }

          LOG.append( "Completed RETRANSMIT processing" );
        }

        // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
        // Expired frames answer our NAK frames
        else if ( packet.type == Packet.EXPIRED ) {
          LOG.append( "Received an EXPIRED packet to seq#" + packet.sequence + " - " + packet.toString() );

          // take frames out of the buffer, they won't be coming
          packetBuffer.expireToPacket( packet.sequence );

          ERR.append( "Message loss has occurred from " + toString() + " dropped " + ( lastPacket - packet.sequence ) + " messages" );

          // reset our expected frame
          lastPacket = packet.sequence - 1;

          LOG.append( "Set next expected packet to " + ( lastPacket + 1 ) + " - Packet buffer now contains " + packetBuffer.size() + " entries:\n" + packetBuffer.dump() );
          LOG.append( "Completed EXPIRED processing" );
        }

        // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
        // Received a NAK from this node...try to retransmit the packets needed 
        // by the NAKer.  A NAK may result in an expired packet being sent and a 
        // series RETRANSMIT packets which will get the NAKer back in sync with 
        // packets in our cache
        else if ( packet.type == Packet.NAK ) {
          // TODO implement cry baby protocol where frequent NAKers get flagged for special treatment
          LOG.append( "NAK packet received from " + endpointSocketAddress + " for packet #" + packet.sequence + " for a total of " + nakTotal + "; recently sent " + nakCount + " since last successful delivery" );

          // All NAKs must contain a target EndPoint otherwise it is impossible 
          // to know which node should process the NAK because some nodes often 
          // mistakenly broadcast their NAKs.

          if ( packet.message != null ) {
            final OamMessage oam = new OamMessage( packet.message );
            final String ep = oam.getEndPoint();

            if ( ep != null ) {
              try {
                final long epid = Long.parseLong( ep );
                if ( this.localEndPoint == epid ) {
                  LOG.append( "Retrieved a NAK for this node - processing..." );

                  // the current packet sequence with which we are dealing
                  long seq = packet.sequence;

                  // the member we will use to hold the current packet to be sent 
                  Packet retval = null;

                  // check to see if the sequence is in the cache.
                  Packet retrans = packetCache.getPacket( seq );

                  LOG.append( "Retrieved a cached packet of " + retrans );

                  // If we did not have the requested packet in our cache
                  if ( retrans != null ) {
                    // send an EXPIRED packet to all nodes to stem the tide of possible 
                    // NAK storms; give the last sequence we have in our cache
                    retval = new Packet();
                    retval.setType( Packet.EXPIRED );
                    retval.sequence = packetCache.getLastSequence();
                    ERR.append( "Packet #" + seq + " is not in our cache - Sending EXPIRED for all frames to " + retval.sequence );

                    // Send the EXPIRED packet to everyone
                    packetTransport.send( retval );

                    LOG.append( "Sent EXPIRED packet to everyone" );

                    // Set our retransmit start point from the first packet we have
                    seq = retval.sequence;

                    LOG.append( "Starting resend from packet #" + seq );

                    // get a reference to that first packet
                    retrans = packetCache.getPacket( seq );
                  }

                  LOG.append( "Starting resend with a cached packet of " + retrans );

                  // send all packets in our cache
                  while ( retrans != null ) {
                    // if it is, RETRANSMIT the packet in the new frame
                    retval = new Packet();
                    retval.setType( Packet.RETRANSMIT );
                    retval.sequence = retrans.sequence;
                    retval.message = retrans.message;

                    LOG.append( "Retransmitting packet " + retval.sequence + " to " + endpointSocketAddress );

                    // Send the RETRANSMIT packet
                    // packetTransport.send( retval );
                    try {
                      final int sent = channel.write( ByteBuffer.wrap( retval.getBytes() ) );
                      LOG.append( "Retransmitted " + sent + " bytes of packet " + retval.sequence + " to " + endpointSocketAddress );
                    } catch ( final Exception e ) {
                      ERR.append( "Problems retransmitting packet #" + retval.sequence + " to " + endpointSocketAddress + " - " + e.getMessage() + "\n" + ExceptionUtil.stackTrace( e ) );
                    }

                    // Get the next packet in the cache
                    retrans = packetCache.getPacket( ++seq );
                  }

                  LOG.append( "Resend loop complete" );

                } else {
                  LOG.append( "Received a NAK from " + endpointSocketAddress + " for another node (" + epid + ") ignoring NAK packet" );
                }
              } catch ( final NumberFormatException e ) {
                LOG.append( "Received a non-numeric endpoint identifier of " + ep + " from " + endpointSocketAddress + " - ignoring NAK" );
              } catch ( final Exception e ) {
                ERR.append( "Could not process NAK " + packet + " for remote node " + remoteEndPoint + " at address " + endpointSocketAddress + " reason:" + e.getMessage() + "\n" + ExceptionUtil.stackTrace( e ) );
              }
            } else {
              LOG.append( "Received a NAK from " + endpointSocketAddress + " with no EndPoint - impossible to know if NAK is for this node or not; ignoring" );
            }
          } else {
            LOG.append( "Received a NAK from " + endpointSocketAddress + " with no packet containing an EndPoint - impossible to know if NAK is for this node or not; ignoring" );
          }

          LOG.append( "NAK processing complete" );
        }

        // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
        else if ( packet.type == Packet.ADMIN ) {
          LOG.append( "Received an ADMIN packet to seq#" + packet.sequence + " - " + packet.toString() );

          // The transport handles all ADMIN frames, particulary looking for
          // INSERT and WITHDRAW. We could look for some specifics here as well
          if ( lastPacket + 1 != packet.sequence ) {
            if ( lastPacket > -1 ) {
              if ( lastPacket + 1 > packet.sequence ) {
                // possible duplicate frame
                LOG.append( "Duplicate packet from " + toString() + " - expected " + ( lastPacket + 1 ) + " received " + packet.sequence + " - Ignoring packet" );
              } else {
                LOG.append( "ADMIN packet reports dropped packet from " + toString() + " - expected " + ( lastPacket + 1 ) + " received " + packet.sequence + " difference of " + ( packet.sequence - ( lastPacket + 1 ) ) + " frames." );

                // Send a NAK for expected frame
                // packetTransport.nak( lastPacket + 1, frame );
                nak( lastPacket + 1 );

                // buffer the packet creating placeholders for any missing frames
                packetBuffer.buffer( packet );

                LOG.append( "ADMIN packet OOS from " + toString() + " - expected " + ( lastPacket + 1 ) + " received " + packet.sequence + " - Sent a NAK for missing packets(s) - Packet buffer now contains " + packetBuffer.size() + " entries:\n" + packetBuffer.dump() );
              }
            } else {
              // This is a new node, so use the present packet sequence
              lastPacket = packet.sequence;
            }
          } else {
            // here is where we can do some QoS administration
            // Explicit ACKs for all packets
            // Encryption
            // Persistent Caching
          }

          LOG.append( "Completed ADMIN processing" );
        }
        // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
        else if ( packet.type == Packet.ACK ) {
          // that's nice... so what do we do now?
        }

      } // Frame !null

      // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
      // Now we do some sanity checks to make sure our buffers don't get too 
      // big and that we don't spend too long in a NAK state waiting for 
      // packets that may never come.

      // If our NAKs are not being honored in a timely fashion...
      if ( nakCount >= RemoteNode.NAK_COUNT_LIMIT ) {
        // pop off all the frames we are missing up to the next packet, which 
        // makes this buffer ready or the buffer is empty
        final int lost = packetBuffer.makeReady();
        if ( lost > 0 ) {
          ERR.append( "Sent " + nakCount + "NAK pacckets without a RETRANSMIT - lost " + lost + " messages" );
          lastPacket += lost;
        }
        nakCount = 0;
      }

      // While we have ordered frames ready for processing...
      while ( packetBuffer.isReady() ) {
        final Packet nextPacket = packetBuffer.getFirstPacket();
        if ( nextPacket != null ) {
          // update our last frame sequence id
          lastPacket = nextPacket.sequence;

          // ...have the transport process the packet
          packetTransport.process( nextPacket.message );
        }
      }

      // Remove any old frames that may never come
      packetBuffer.expire( 6000 ); //MicroBus.EXPIRATION_INTERVAL );
    } catch ( final Throwable t ) {
      ERR.append( "Could not process packet " + packet + " for remote node " + remoteEndPoint + " at address " + endpointSocketAddress + " reason:" + t.getMessage() + "\n" + ExceptionUtil.stackTrace( t ) );
    }

  } // processFrame




  public void nak( final long sequence ) {
    final Packet retval = new Packet();
    retval.type = Packet.NAK;
    retval.endPoint = localEndPoint;
    retval.sequence = sequence;
    retval.message = new Message();

    // All NAK packets MUST contain the endpoint identifier of the target node
    retval.message.put( OamMessage.ENDPOINT, remoteEndPoint );

    nakCount++;
    nakTotal++;

    {
      LOG.append( "Sending NAK for seq# " + sequence + " to " + endpointSocketAddress );

      try {
        // Write the packet to the channel which only sends and receives packets 
        // to and from our remote node
        final int sent = channel.write( ByteBuffer.wrap( retval.getBytes() ) );
        LOG.append( "Sent a NAK packet of " + sent + " bytes for packet #" + sequence + " to " + endpointSocketAddress );
      } catch ( final IOException e ) {
        ERR.append( "Could not send a NAK to " + endpointSocketAddress + " - " + e.getMessage() );
      }
    }

  }




  public void close() {
    if ( channel != null ) {
      try {
        channel.close();
      } catch ( final IOException e ) {
        channel.socket().close();
      }

      channel = null;
    }
  }




  /**
   * @return Nice human-readble representation of the RemoteNode object
   */
  public String toString() {
    return new String( "Node:" + remoteEndPoint + " Frame:" + lastPacket + " Addr:" + udpAddress.getHostAddress() + ":" + udpPort + " NAKs:" + nakCount + "-" + nakTotal + " TCP:" + serviceUri );
  }




  /**
   * @return  The last heartbeat packet received from the remote node (may be  null)
   */
  public Message getLastHeartbeat() {
    return lastHeartbeat;
  }




  /**
   * @return True if the expiration time has expired since the last packet.
   */
  public boolean isExpired() {
    return ( ( System.currentTimeMillis() - lastSeen ) > ( RemoteNode.EXPIRATION_TIMEOUT * 1.25 ) );
  }




  /**
   * @return  True if we have received the withdrawal packet from the node, false otherwise.
   */
  public boolean isWithdrawn() {
    return withdrawn;
  }

}
