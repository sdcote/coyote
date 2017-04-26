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

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;

import coyote.commons.ByteUtil;
import coyote.mbus.message.Message;
import coyote.mbus.message.MessageAddress;


/**
 * Packet represents the wire format of a messaging packet. It contains a 
 * header that allows for reliable delivery. 
 * 
 * <p>Packets are envelopes that aid in the sequencing and delivery of message 
 * payloads between nodes. Each packet has a header of exactly 13 octets. The 
 * first octet is a packet type and contains an unsigned integer (0-255) 
 * indicating the type of packet it is. Next is a series of 3 4-octet fields 
 * representing the end point identifier, sequence and payload length 
 * respectively. Each of these values are represented as an unsigned integer in
 * network byte order.
 * 
 * <p><pre>
 *  +-[U8]---+-[U32]--+-[U32]--+-[U32]--+ 
 *  |TYPE    |ENDPOINT|SEQUENCE|LENGTH  |
 *  |offset00|offset01|offset05|offset09| 
 *  +--------+--------+--------+--------+ 
 * </pre>
 *          
 * <p>The standard packet types are as follows: <br>
 * 0 - MSG - application-level message <br> 
 * 1 - HEARTBEAT - heartbeat <br> 
 * 2 - ACK - acknowledgment <br> 
 * 3 - NAK - negative acknowledgment <br> 
 * 4 - RETRANSMIT - retransmission of a particular application-level packet<br> 
 * 5 - ADMIN - administration <br> 
 * 6 - EXPIRED - packet indicating the NAK packet has been expired from cache<br>
 * 
 *  
 * <p>Endpoint identifiers are exactly 32 bits and represents an unsigned
 * integer (long) value that uniquely identifies a node on the bus which sent 
 * the message. This allows receivers to track messages sent from nodes.
 * 
 * <p>The sequence is another unsigned integer representing the sequence of the 
 * packet since the start of nodes existence on the bus. This allows receivers 
 * to detect the possible loss of messages (sequence numbers skipped), 
 * duplicate or retransmitted messages (sequence numbers duplicated) and the 
 * ordering of messages.
 * 
 * <p>The length is the size of the payload that follows as an unsigned integer 
 * value in 32 bits.
 */
public class Packet {
  /** The number of octets in a Packet header */
  public static final int HEADER_SIZE = 13;

  /**
   * The unique identifier of the end point that is sending this packet. It is
   * this identifier and the sequence number that tracks packet delivery. This
   * has nothing to do with source or target addresses within the enclosed
   * Message.
   */
  public long endPoint = 0;

  /** The message this packet carries */
  public Message message = null;

  /**
   * The last (or current) packet sequence identifier the originating node has
   * sent. If this is a message packet, then this identifier represents this 
   * packets identifier.
   */
  public long sequence = 0;

  /**
   * The timestamp represents either the time the packet was received by the
   * PacketNode, or the time it was sent by the PacketNode(if the packet is sitting
   * in a cache).
   */
  public long timestamp = 0;

  /** Type of this message (range 0-255) default = 0-packet */
  public short type = 0;

  /** Message type indicating an application-level message */
  public static final short MSG = 0;

  /** Packet type indicating a heartbeat from a PacketNode */
  public static final short HEARTBEAT = 1;

  /**
   * Message type indicating an acknowledgment to a particular message
   * identifier
   */
  public static final short ACK = 2;

  /**
   * Message type indicating an administration packet. ADMIN packets are used by
   * the bus and its nodes during initialization and maintenance activities.
   */
  public static final short ADMIN = 5;

  /**
   * Packet type indicating a negative acknowledgment to a particular Packet
   * identifier.
   */
  public static final short NAK = 3;

  /** Packet type indicating a retransmission of a particular Packet identifier */
  public static final short RETRANSMIT = 4;

  /**
   * Packet type indicating a retransmission of a particular Packet identifier
   * could not be performed because the requested packet has expired from the
   * senders cache.
   */
  public static final short EXPIRED = 6;

  /**
   * Packet type indicating there are other packets enclosed within the current
   * packet. This allows for the streaming of multiple, small packets over 
   * mediators that support larger PDUs.
   */
  public static final short BATCH = 7;

  /** 
   * Indicates the packet contains a fragment of a larger packet. This allows 
   * large packets to be sent over mediators that support only small PDUs.
   */
  public static final short FRAGMENT = 8;

  private static final String[] typeNames = { "MSG", "HEARTBEAT", "ACK", "NAK", "RETRANSMIT", "ADMIN", "EXPIRED", "BATCH", "FRAGMENT" };

  /** The number of octets required to represent this packet */
  int octetCount = 0;

  /** The IP address that sent the network packet containing this packet. */
  public InetAddress remoteAddress = null;

  /** The IP port that sent the network packet containing this packet. */
  public int remotePort = 0;




  /**
   *
   */
  public Packet() {
    super();
  }




  /**
   * Create a PackePacket with the given sequence identifier.
   * 
   * @param seq the sequence identifier for this packet.
   */
  Packet( final long seq ) {
    super();
    this.sequence = seq;
  }




  /**
   * Construct a Packet from a byte array.
   *
   * @param data
   */
  public Packet( final byte[] data ) throws PacketException {
    try {
      // There is a 13-byte header on all packets
      if ( data.length > ( Packet.HEADER_SIZE - 1 ) ) {
        // Read in the packet type
        type = data[0];

        // read in the packet endpoint identifier
        endPoint = ByteUtil.retrieveUnsignedInt( data, 1 );

        // read in the packet sequence
        sequence = ByteUtil.retrieveUnsignedInt( data, 5 );

        // read in the length of the payload
        final int length = (int)ByteUtil.retrieveUnsignedInt( data, 9 );
        octetCount = Packet.HEADER_SIZE;

        // If there is more data to be processed
        if ( length > 0 ) {
          // TODO Check for excessive length and ignore any packets that are too long - Potential DoS vulnerability.

          // create the payload array large enough for just the packet
          final byte[] payload = new byte[length];
          System.arraycopy( data, Packet.HEADER_SIZE, payload, 0, payload.length );

          message = new Message( payload );
          octetCount += length;
        }
      } else {
        throw new PacketException( "data too small to be a packet" );
      }
    } catch ( final RuntimeException e ) {
      // Packet format error, null, array index out-of-bounds, etc...
      throw new PacketException( e.getMessage() );
    }
  }




  /**
   * Construct a Packet from a DataInputStream.
   *
   * @param dis
   *
   * @throws IOException
   */
  public Packet( final DataInputStream dis ) throws IOException, PacketException {
    final byte[] headerField = new byte[4];

    // The first byte is the Packet type
    type = (short)dis.read();

    // read the endpoint identifier from which this packet was sent
    dis.read( headerField );

    endPoint = ByteUtil.retrieveUnsignedInt( headerField, 0 );

    // read the current sequence counter
    dis.read( headerField );

    sequence = ByteUtil.retrieveUnsignedInt( headerField, 0 );

    // read the length of the Packet data
    dis.read( headerField );

    final long length = ByteUtil.retrieveUnsignedInt( headerField, 0 );

    octetCount = Packet.HEADER_SIZE;

    // / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / /
    // read the payload if there is a payload to read
    if ( length > 0 ) {
      // sanity check for array creation
      if ( length > Integer.MAX_VALUE ) {
        throw new IOException( "read overflow: cannot handle length of " + length );
      } else {
        // create the payload array
        // TODO DANGER! We could easily run out of memory here!
        final byte[] payload = new byte[(int)length];

        // create a read buffer
        byte[] chunk = new byte[512];

        // track how much has been read so far
        int soFar = 0;

        // While we have more to read
        while ( soFar < length ) {
          // Make sure we read only what we need to read
          if ( length - soFar < 512 ) {
            chunk = new byte[( (int)length - soFar )];
          }

          // read in a chunk of data
          final int read = dis.read( chunk );

          // if EOF, throw an exception
          if ( read == -1 ) {
            throw new IOException( "read underflow: unexpected EOF" );
          }

          // copy the chunk into our payload array
          try {
            System.arraycopy( chunk, 0, payload, soFar, read );
          } catch ( final ArrayIndexOutOfBoundsException e ) {
            // Inconceivable!
            throw new IOException( "Packet AIOOB: Payload length='" + length + "' - chunk=" + chunk.length + ", 0, payload=" + payload.length + ", soFar=" + soFar + ", read=" + read );
          }

          // update the number of bytes read so far
          soFar += read;
        } // while soFar < length

        // At this point we have read in only the amount given in the length
        // field so the DataInputStream should be positioned at the next
        // Packet. Now read the payload field as a packet
        try {
          message = new Message( payload );
        } catch ( final Exception e ) {
          throw new IOException( "Packet Error: " + e.getMessage() );
        }

      } // length<=MAX
    } // length>0

    octetCount += length;
  }




  /**
   * Returns an array of bytes suitable for sending over the wire.
   *
   * @return the bytes that can be used for transport over the network.
   */
  public byte[] getBytes() {
    final ByteArrayOutputStream baos = new ByteArrayOutputStream();
    final DataOutputStream dos = new DataOutputStream( baos );

    try {
      // write the packet type
      dos.write( ByteUtil.renderShortByte( type ) );

      // write the packet sequence
      dos.write( ByteUtil.renderUnsignedInt( endPoint ) );

      // write the packet sequence
      dos.write( ByteUtil.renderUnsignedInt( sequence ) );

      if ( message != null ) {
        final byte[] payload = message.getBytes();

        // Write the payload length
        dos.write( ByteUtil.renderUnsignedInt( payload.length ) );

        // write the payload
        dos.write( payload );
      } else {
        // Write a payload length of zero
        dos.write( ByteUtil.renderUnsignedInt( 0L ) );
      }
    } catch ( final IOException e ) {
      e.printStackTrace();
    } catch ( final RuntimeException e ) {
      e.printStackTrace();
    }

    octetCount = baos.size() + Packet.HEADER_SIZE;

    return baos.toByteArray();
  }




  /**
   * Looks into the embedded packet and returns the MessageAddress of the source
   * channel.
   *
   * @return The address of the MessageChannel from which this packet's packet was
   *         sent.
   */
  public MessageAddress getSourceAddress() {
    if ( message != null ) {
      return message.getSource();
    }

    return null;
  }




  /**
   * Looks into the embedded packet and returns the MessageAddress of the source
   * channel.
   *
   * @return The address of the MessageChannel for which this packet's packet is
   *         intended.
   */
  public MessageAddress getTargetAddress() {
    if ( message != null ) {
      return message.getTarget();
    }

    return null;
  }




  /**
   * Set the target address of the enclosed packet to the given address.
   * 
   * @param addr The address to set in the enclosed packet.
   */
  public void setTargetAddress( final MessageAddress addr ) {
    if ( message != null ) {
      message.setTarget( addr );
    }
  }




  /**
   * Return the type code of this packet.
   * @return  A short value in the range of 0-255.
   */
  public short getType() {
    return type;
  }




  /**
   * Return the name of the type of packet this is.
   *
   * @return A human-readable type name.
   */
  public String getTypeName() {
    return Packet.getTypeName( type );
  }




  /**
   * Set the type of Message this is.
   * @param s  short in the range of 0-255.
   */
  public void setType( final short s ) {
    type = s;
  }




  /**
   * @return a human readable string representation of the packet
   */
  public String toString() {
    if ( remoteAddress != null ) {
      return new String( getTypeName() + " seq:" + sequence + " NetworkService:" + endPoint + " src=" + remoteAddress.getHostAddress() + ":" + remotePort + " (" + octetCount + " bytes)" );
    } else {
      return new String( getTypeName() + " #" + sequence + " NetworkService " + endPoint + " (local)" );
    }

  }




  /**
   * Method getTypeName
   *
   * @param code
   *
   * @return TODO Complete Documentation
   */
  public static String getTypeName( final short code ) {
    if ( code < Packet.typeNames.length ) {
      return Packet.typeNames[code];
    }

    return null;
  }




  /**
   * Method getPacketType
   *
   * @param header
   *
   * @return TODO Complete Documentation
   */
  public static int getPacketType( final byte[] header ) {
    if ( header.length > ( Packet.HEADER_SIZE - 1 ) ) {
      return header[0];
    }

    return -1;
  }




  /**
   * Method getPacketEndpoint
   *
   * @param header
   *
   * @return TODO Complete Documentation
   */
  public static long getPacketEndpoint( final byte[] header ) {
    if ( header.length > ( Packet.HEADER_SIZE - 1 ) ) {
      return ByteUtil.retrieveUnsignedInt( header, 1 );
    }

    return -1;
  }




  /**
   * Method getPacketSequence
   *
   * @param header
   *
   * @return TODO Complete Documentation
   */
  public static long getPacketSequence( final byte[] header ) {
    if ( header.length > ( Packet.HEADER_SIZE - 1 ) ) {
      return ByteUtil.retrieveUnsignedInt( header, 5 );
    }

    return -1;
  }




  /**
   * Method getPacketLength
   *
   * @param header
   *
   * @return TODO Complete Documentation
   */
  public static int getPacketLength( final byte[] header ) {
    if ( header.length > ( Packet.HEADER_SIZE - 1 ) ) {
      return (int)ByteUtil.retrieveUnsignedInt( header, 9 );
    }

    return -1;
  }




  /**
   * Returns the number of bytes determined to be required to represent this
   * packet.
   * 
   * <p>This actually represents only the cached value as determined by 
   * previous calls to the constructors with arguments or the getBytes method.
   * If none of these methods have been called on this instance, this method 
   * will return zero and NOT attempt to calculate the size of the packet.
   * 
   * <p>The return value represents the size of the encoded packet embedded in
   * this packet (if any) and the length of the header. Subtract HEADER_SIZE 
   * from the returned value to determine the size of the encoded packet.
   * 
   * @return  The number of octets required to represent this packet.
   */
  public int getOctetCount() {
    return octetCount;
  }




  /**
   * @return  Returns the IP address from which this packet was sent.
   */
  public InetAddress getRemoteAddress() {
    return remoteAddress;
  }




  /**
   * @return  Returns the IP port from which this packet was sent.
   */
  public int getRemotePort() {
    return remotePort;
  }
}
