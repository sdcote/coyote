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

import java.net.InetAddress;
import java.net.URI;
import java.util.ArrayList;
import java.util.Calendar;

import coyote.dataframe.DataField;
import coyote.mbus.message.Message;
import coyote.mbus.message.MessageAddress;


/**
 * RemoteService is a class used by MessageSessions for tracking the state of
 * remote sessions directly connected to the network.
 * 
 * <p>All of the Message protocol is contained in this class. The sequencing and
 * acknowledgments of frames are also handled here.</p>
 * 
 * <p>The concept is this class filters all message frames received through the
 * <tt>processPacket()</tt> method acting on protocol-specific frames and
 * allowing all non-protocol frames (user-level events) to be passed on the the
 * set MessageTransports receive method. All frames that are to be sent with
 * regards to protocol processing are sent through the set MessageTransports send
 * method.</p>
 */
class RemoteService {
  private static final long RMTSVC = 66;//Log.getCode( "RMTSVC" );

  /** Field udpAddress */
  InetAddress udpAddress = null;

  /** Field udpPort */
  int udpPort = 0;

  /** The URI on which the remote node listens for TCP connections */
  URI serviceUri = null;

  /** Field channels */
  ArrayList channels = new ArrayList();

  /** The MessageBus Identifier for this node */
  long endPoint = 0;

  /** Last frame sequence identifier received */
  long lastPacket = -1;

  /** The MessageTransport we are to use to route messages */
  MessageMediator eventTransport = null;

  /** The calendar object we use to calculate the UTC timestamp for each frame */
  private static final Calendar cal = Calendar.getInstance();

  /** The last (or current) EVENT sequence identifier we have sent */
  long sequence = 0;

  /** The cache of all the Packets we have sent so far */
  PacketQueue cache = new PacketQueue();

  /** The unique identifier we use to identify this session endpoint. (-1 implies unassigned) */
  private final long endpoint = -1;

  private static final int MAJOR = 0;
  private static final int MINOR = 1;
  private static final int PATCH = 0;
  private static final int BUILD = 0;




  /**
   *
   *  @param svc
   */
  public RemoteService( final MessageMediator svc ) {
    if ( svc == null ) {
      throw new IllegalArgumentException( "MessageTransport argument was null" );
    }

    eventTransport = svc;
  }




  /**
   * Package the message for sending according to the protocol.
   *
   * @param msg The message to send.
   * 
   * @return The bytes representing the official identifier of the sent message.
   */
  public byte[] send( final Message msg ) {
    byte[] retval = null;

    // ...create a Packet in which it can be transported
    final Packet packet = new Packet();

    // Set the frame type to message
    packet.type = Packet.MSG;

    // Generate a unique identifier for this message
    retval = msg.getId();

    // Create a copy of the Message so it does not get changed by another
    // component (like the original sender) while the message is sitting in
    // a buffer waiting to be sent.
    packet.message = (Message)msg.clone();

    // Set the endpoint that sent this frame to our endpoint identifier
    packet.endPoint = endpoint;

    // Set the frame sequence from this node
    packet.sequence = sequence++;

    // Set the time this frame was sent adjusting for Time Zone and DST. This
    // calculation has to be performed here as this component may be running
    // when DST changes, otherwise we could make it a constant.
    packet.timestamp = cal.getTimeInMillis() + cal.get( Calendar.ZONE_OFFSET ) - cal.get( Calendar.DST_OFFSET );

    // place the frame in the packet queue so we can send it back if it's NAKed
    cache.add( packet );

    // send the frame over the transport
    eventTransport.send( packet );

    // TODO expire events every time through the loop?
    final long last = cache.expire( 60000 ); // long last = cache.expire( MessageNode.cacheMaxAge );
    //Log.append( RMTSVC, "send: expired cached frames - last frame in cache = " + last );

    return retval;
  }




  /**
   * Indicates if this RemoteService has a NAK registered for a frame.
   *
   * @return True if this node has any outstanding NAKs for frames, False if the
   *         remote node is up-to-date.
   */
  boolean hasNak() {
    return false;
  }




  /**
   * Indicates if this RemoteService has a NAK registered for the given frame
   * sequence.
   *
   * @param seq The frame to check.
   * @return True if the given frame sequence ID represents a missing frame,
   *         false if there are no outstanding NAKs for this frame identifier.
   */
  boolean hasNak( final long seq ) {
    return false;
  }




  /**
   * Process the given frame.
   *
   * @param frame The frame that this node should reprocess.
   */
  void processPacket( final Packet frame ) {
    try {
      if ( frame != null ) {
        //Log.append( RMTSVC, "processPacket: ----[ Processing " + frame.getTypeName() + "(" + frame.type + ") sequence = " + frame.sequence + " ]--------------------" );

        // / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / /
        // Perform a sequence check
        if ( frame.type == Packet.HEARTBEAT ) {
          if ( lastPacket != frame.sequence ) {
            if ( lastPacket > -1 ) {
              if ( lastPacket < frame.sequence ) {
                //Log.error( "processPacket: 1 Heartbeat reports frame loss - expected " + lastPacket + " received " + frame.sequence + " difference of " + ( frame.sequence - lastPacket ) + " frames." );
                System.err.println( "ERROR: missing " + ( frame.sequence - lastPacket ) + " frames!" );
              } else {
                //Log.error( "processPacket: 1 Heartbeat reports frame decremented counter - expected " + lastPacket + " received " + frame.sequence + " difference of " + ( lastPacket - frame.sequence ) + " frames." );

                // It is possible that the packets are being sent out of order
              }
            }
          }

          // Whatever - for now
          lastPacket = frame.sequence;
        } else if ( frame.type == Packet.MSG ) {
          if ( lastPacket + 1 != frame.sequence ) {
            //Log.error( "processPacket: 1 Packet OOS from " + toString() + " - expected " + ( lastPacket + 1 ) + " received " + frame.sequence );

            // send a nak, place the frame in the node for later retrieval

            // Whatever - for now
            lastPacket = frame.sequence;
          } else {
            // Update the sequence identifiers
            lastPacket = frame.sequence;

            //Log.append( RMTSVC, "processPacket: 1 Sequence check on " + frame.getTypeName() + " successful node.lastPacket=" + lastPacket );
          }

          // Whatever - for now
          lastPacket = frame.sequence;
        }

        // This is the heart of the protocol each of the frame types are
        // processed here

        if ( frame.type == Packet.ACK ) {
          //Log.append( RMTSVC, "processPacket: 2 processing ACK" );
        } else if ( frame.type == Packet.NAK ) {
          //Log.append( RMTSVC, "processPacket: 2 processing NAK" );
        } else if ( frame.type == Packet.ADMIN ) {
          //Log.append( RMTSVC, "processPacket: 2 processing ADMIN" );
          processAdminPacket( frame );
        } else if ( frame.type == Packet.HEARTBEAT ) {
          //Log.append( RMTSVC, "processPacket: 2 processing HEARTBEAT" );
        } else if ( frame.type == Packet.MSG ) {
          //Log.append( RMTSVC, "processPacket: 2 processing EVENT" );
          if ( frame.message != null ) {
            eventTransport.process( frame.message );
          }
        } else {
          //Log.append( RMTSVC, "processPacket: 2 processing UNKNOWN" );
        }

        //Log.append( RMTSVC, "processPacket: ----[ Finished Processing Received Packet ]------------------------" );
      }
    } catch ( final RuntimeException e ) {
      e.printStackTrace();
    }
  }




  /**
   * Process the given administration frame with respect to the given remote
   * node.
   *
   * @param frame The Packet to process.
   */
  private void processAdminPacket( final Packet frame ) {
    //Log.append( RMTSVC, "processAdminPacket: ADMIN frame from " + toString() );

    final Message message = frame.message;

    if ( message != null ) {
      final DataField field = message.getField( "ACTION" );

      if ( field != null ) {
        final String action = field.getObjectValue().toString();
        //Log.append( RMTSVC, "processAdminPacket: Action field = '" + action + "'" );

        if ( action.equalsIgnoreCase( "INSERT" ) ) {
          final DataField idField = message.getField( "ENDPOINT" );
          //Log.append( RMTSVC, "processAdminPacket: EndPoint field = '" + idField + "'" );

          if ( idField != null ) {
            try {
              final int id = Integer.parseInt( idField.getObjectValue().toString() );

              if ( id == endPoint ) {
                //Log.append( RMTSVC, "processAdminPacket: MessageBus IDs match - sending ALERT" );

                final Packet admin = new Packet();
                admin.type = Packet.ADMIN;

                // Create a Message message
                final Message event2 = new Message();
                event2.add( "ACTION", "ALERT" );
                event2.add( "MSG", "Duplicate EndPoint Identifier" );
                event2.add( "ENDPOINT", Long.toString( endPoint ) );
                event2.setTarget( new MessageAddress( udpAddress, udpPort, id, 0 ) );

                // Add the message to the frame
                admin.message = event2;

                // Send the frame over the transport
                eventTransport.send( admin );
              }
            } catch ( final Exception e ) {}
          } else {
            //Log.append( RMTSVC, "processAdminPacket: ------------ NO ID FIELD ----------" );
          }
        } else if ( action.equalsIgnoreCase( "ALERT" ) ) {
          //Log.append( RMTSVC, "processAdminPacket: processing alert" );

          final DataField idField = message.getField( "ENDPOINT" );
          //Log.append( RMTSVC, "processAdminPacket: endPoint field: " + idField );
          if ( idField != null ) {
            final DataField alertField = message.getField( "MSG" );
            //Log.append( RMTSVC, "processAdminPacket: endPoint field: " + alertField );

            if ( ( alertField != null ) && alertField.getObjectValue().toString().equalsIgnoreCase( "Duplicate MessageBus Identifier" ) ) {
              //Log.append( RMTSVC, "processAdminPacket: Duplicate MessageBus ID alert" );

              try {
                final int id = Integer.parseInt( idField.getObjectValue().toString() );

                if ( id == endPoint ) {
                  //Log.append( RMTSVC, "processAdminPacket: Received a bus identifier collision alert on our bus identifier from " + toString() );
                }
              } catch ( final Exception e ) {
                //Log.error( "processAdminPacket: exception checking Re-selecting a bus identifier " + e.getMessage() );
              }
            }
          }
        } else if ( action.equalsIgnoreCase( "WITHDRAW" ) ) {
          //Log.append( RMTSVC, "processAdminPacket: processing withdrawal of node " + toString() );
        } else {
          //Log.append( RMTSVC, "processAdminPacket: received action of '" + action + "' from " + toString() );
        }
      }
    } else {
      // Log.append( RMTSVC, "processAdminPacket: NO EVENT ENCLOSED" );
    }

    //Log.append( RMTSVC, "processAdminPacket: processing complete" );
  }




  public String toString() {
    return new String( "RemoteService:" + endPoint + " Packet:" + lastPacket + " Addr:" + udpAddress.getHostAddress() + ":" + udpPort + " Service:" + serviceUri );
  }
}