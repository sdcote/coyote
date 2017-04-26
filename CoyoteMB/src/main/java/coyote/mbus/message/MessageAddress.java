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
package coyote.mbus.message;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;

import coyote.commons.ByteUtil;


/**
 * MessageAddress models an exact endpoint in the message network.
 * 
 * <p>MessageAddresses signify the exact source or destination within the mBus
 * network by specifying the IP address, port and endpoint identifier of the
 * message service node and the inbound message queue of a particular 
 * MessageChannel.
 * 
 * <p>MessageAddresses are encoded by placing the channel identifier in the 
 * first 2 bytes of the address as an unsigned integer. This is followed by a 
 * 4-byte, unsigned integer signifying the unique numeric identifier of the 
 * endpoint on the packet network. The next field is the IP port of the 
 * endpoint represented by another 2-byte unsigned integer. The remaining bytes 
 * are the InetAddress of the EndPoint which is either 4 or 16 bytes 
 * representing an IPv4 and an IPv6 address respectively.
 * 
 * <p>Valid addresses can encode into 6, 8, 12 or 24-byte arrays. This allows 
 * for the simple determination of a local, peer or remote delivery simply by 
 * using the size of the address. If the target address is only 6 bytes, the 
 * packet stays within the endpoint.
 * 
 * <p>A channel identifier represents a unique channel of packets managed by an
 * EndPoint. Message channels are local to and endpoint and are the primary 
 * means through which client to the service send and receive packets.
 * 
 * <p>An endpoint represents a unique packet node in the packet framework. Data 
 * are transferred between endpoints for routing to the local channels within 
 * that endpoint.
 * 
 * <p><pre>
 * +-[U16]--+-[U32]--+-[U16]--+-[U32]--+
 * |CHANNEL |ENDPOINT|IP_PORT |IP_ADDR |
 * |offset00|offset02|offset06|offset08|
 * +--------+--------+--------+--------+
 * </pre>
 */
public class MessageAddress {
  /**
   * Return the IP address of the given data
   *
   * @param packetAddr
   * 
   * @return the IP address of the given data
   */
  public static InetAddress getAddress( final byte[] packetAddr ) {
    InetAddress retval = null;

    if ( packetAddr != null ) {
      try {
        if ( packetAddr.length == 12 ) {
          retval = InetAddress.getByAddress( ByteUtil.subArray( packetAddr, 8, 4 ) );
        } else if ( packetAddr.length == 24 ) {
          retval = InetAddress.getByAddress( ByteUtil.subArray( packetAddr, 8, 16 ) );
        }
      } catch ( final UnknownHostException e ) {}
    }

    return retval;
  }




  /**
   * Return the Channel identifier of the given MessageAddress
   *
   * @param packetAddr
   */
  public static int getChannel( final byte[] packetAddr ) {
    if ( ( packetAddr != null ) && ( packetAddr.length > 2 ) ) {
      return ByteUtil.retrieveShort( packetAddr, 0 );
    }

    return -1;
  }




  /**
   * Return the endpoint identifier portion of the encoded address data.
   *
   * @param packetAddr the byte array representing the encoded address.
   *
   * @return the 4-byte unsigned integer value representing the endpoint.
   */
  public static long getEndPoint( final byte[] packetAddr ) {
    if ( ( packetAddr != null ) && ( packetAddr.length > 4 ) ) {
      return ByteUtil.retrieveUnsignedInt( packetAddr, 2 );
    }

    return -1;
  }




  /**
   * Return the IP port of the given MessageAddress
   *
   * @param packetAddr
   *
   * @return the IP port
   */
  public static int getPort( final byte[] packetAddr ) {
    if ( ( packetAddr != null ) && ( packetAddr.length > 10 ) ) {
      return ByteUtil.retrieveUnsignedShort( packetAddr, 6 );
    }

    return -1;
  }

  /** The address remains in an encoded byte array for fast and easy matching. */
  byte[] data = null;




  /**
   * Construct a MessageAddress with the given data.
   *
   * @param bytes
   */
  public MessageAddress( final byte[] bytes ) {
    if ( bytes != null ) {
      // parse the address
      if ( ( bytes.length == 0 ) || ( bytes.length == 2 ) || ( bytes.length == 6 ) || ( bytes.length == 8 ) || ( bytes.length == 12 ) || ( bytes.length == 24 ) ) {
        data = new byte[bytes.length];

        System.arraycopy( bytes, 0, data, 0, bytes.length );
      } else {
        throw new IllegalArgumentException( "Data size of " + bytes.length + " does not match that of a valid address" );
      }
    }
  }




  /**
   * Constructor
   *
   * @param addr The IP address (v4 or v6)
   * @param port The IP port
   * @param id The EndPoint identifier Actually an unsigned integer
   * @param channel The channel identifier
   */
  public MessageAddress( final InetAddress addr, final int port, final long id, final int channel ) {
    if ( addr != null ) {
      if ( addr instanceof Inet4Address ) {
        data = new byte[12];

        ByteUtil.overlayUnsignedShort( channel, data, 0 );
        ByteUtil.overlayUnsignedInt( id, data, 2 );
        ByteUtil.overlayUnsignedShort( port, data, 6 );
        System.arraycopy( addr.getAddress(), 0, data, 8, 4 );
      } else {
        data = new byte[24];

        ByteUtil.overlayUnsignedShort( channel, data, 0 );
        ByteUtil.overlayUnsignedInt( id, data, 2 );
        ByteUtil.overlayUnsignedShort( port, data, 6 );
        System.arraycopy( addr.getAddress(), 0, data, 8, 16 );
      }
    } else if ( port > 0 ) {
      data = new byte[8];

      ByteUtil.overlayUnsignedShort( channel, data, 0 );
      ByteUtil.overlayUnsignedInt( id, data, 2 );
      ByteUtil.overlayUnsignedShort( port, data, 6 );
    } else if ( id > -1 ) {
      data = new byte[6];

      ByteUtil.overlayUnsignedShort( channel, data, 0 );
      ByteUtil.overlayUnsignedInt( id, data, 2 );
    } else if ( channel > -1 ) {
      data = new byte[2];

      ByteUtil.overlayUnsignedShort( channel, data, 0 );
    } else {
      data = new byte[0];
    }
  }




  /**
   * Constructor
   *
   * @param id The EndPoint identifier Actually an unsigned integer
   * @param channel The channel identifier
   */
  public MessageAddress( final long id, final int channel ) {
    if ( ( id > -1 ) && ( channel > -1 ) ) {
      data = new byte[6];

      ByteUtil.overlayUnsignedShort( channel, data, 0 );
      ByteUtil.overlayUnsignedInt( id, data, 2 );
    } else {
      data = new byte[0];
    }
  }




  /**
   * Return if this MessageAddress is equal to some other object.
   * 
   * @return True if the argument is an MessageAddress representing the same 
   *         point in the infrastructure. 
   */
  public boolean equals( final Object address ) {
    if ( address != null ) {
      if ( address instanceof MessageAddress ) {
        final MessageAddress addr = (MessageAddress)address;
        if ( addr.data.length == data.length ) {
          for ( int x = 0; x < data.length; x++ ) {
            if ( addr.data[x] != data[x] ) {
              return false;
            }
          }
          return true;
        }
      }

    }
    return false;
  }




  /**
   * Return the IP address of this MessageAddress
   *
   * @return the IP address of this MessageAddress
   */
  public InetAddress getAddress() {
    return MessageAddress.getAddress( data );
  }




  /**
   * Retrieve the address as a byte array.
   */
  public byte[] getBytes() {
    return data;
  }




  /**
   * Return the Channel identifier of this MessageAddress
   */
  public int getChannelId() {
    return MessageAddress.getChannel( data );
  }




  /**
   * Return the endpoint identifier portion of this address.
   *
   * @return the 4-byte unsigned integer value representing the endpoint.
   */
  public long getEndPoint() {
    return MessageAddress.getEndPoint( data );
  }




  /**
   * Return the IP port of this MessageAddress
   */
  public int getPort() {
    return MessageAddress.getPort( data );
  }




  /**
   * Human readable representation of this address
   */
  public String toString() {
    return new String( "IP:" + getAddress() + ":" + getPort() + " EP:" + getEndPoint() + " Chl:" + getChannelId() );
  }

}
