/*
 * Copyright (c) 2014 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 *
 * Contributors:
 *   Stephan D. Cote 
 *      - Initial API and implementation
 */
package coyote.commons.network;

import java.net.InetAddress;
import java.util.Iterator;
import java.util.NoSuchElementException;


/**
 * Models an IP network with a netmask. 
 */
public class IpNetwork extends IpAddress {
  private static final short[] BITS = { 0, 128, 192, 224, 240, 248, 252, 254, 255 };

  private IpAddress netmask = null;
  private static final short[] HOSTONLYMASK = { 255, 255, 255, 255 };
  private IpNetworkElementIterator ipAddressIterator = null;

  /** The mask representing 255.255.255.255 - a network of only 1 host */
  public static final IpAddress HOSTMASK = new IpAddress( HOSTONLYMASK );




  // TODO: Validate the mask to disallow masks like 255.22.255.0 - the segment of 22 is invalid or that last 255 should be 0

  /**
   * Creates a new instance of IpNetwork
   *
   * @param net the network address like: 192.168.0.0
   * @param mask the mask address like: 255.255.0.0
   */
  public IpNetwork( final IpAddress net, final IpAddress mask ) {
    super( net );
    netmask = (IpAddress)mask.clone();
    ipAddressIterator = new IpNetworkElementIterator( net, netmask, this );
  }




  /**
   * Creates a new instance of IpNetwork
   *
   * @param net String representing a network address like: 192.168.0.0
   * @param mask String representing a mask address like: 255.255.0.0
   *
   * @throws IpAddressException if the network address is invalid
   */
  public IpNetwork( final String net, final String mask ) throws IpAddressException {
    super( net );

    netmask = new IpAddress( mask );
    ipAddressIterator = new IpNetworkElementIterator( this, netmask, this );
  }




  /**
   * Creates a new instance of IpNetwork using Class-less Inter-Domain Routing
   * (CIDR) notation.
   *
   * <p>A CIDR address includes the standard 32-bit IP address and also
   * information on how many bits are used for the network prefix. For example,
   * in the CIDR address 206.13.01.48/25, the "/25" indicates the first 25 bits
   * are used to identify the unique network leaving the remaining bits to
   * identify the specific host.</p>
   *
   * @param prefix CIDR notation of the network block
   *
   * @throws IpAddressException if the prefix contains invalid numbers for an address
   */
  public IpNetwork( final String prefix ) throws IpAddressException {
    // Look for the '/' delimiting the bitmask count
    final int mark = prefix.indexOf( '/' );

    if ( mark > 0 ) {
      try {
        netmask = new IpAddress( IpNetwork.getOctets( Integer.parseInt( prefix.substring( mark + 1 ) ) ) );
        final IpAddress ipAddress = new IpAddress( prefix.substring( 0, mark ) );
        octets = ipAddress.getOctets();
        ipAddressIterator = new IpNetworkElementIterator( ipAddress, netmask, this );
      } catch ( final Exception ex ) {
        throw new IpAddressException( "Invalid network block" );
      }
    } else {
      throw new IpAddressException( "Could not find bitmask count" );
    }
  }




  /**
   * Test equivalence of the given object with this network address
   */
  @Override
  public boolean equals( final Object obj ) {
    if ( ( obj != null ) && ( obj instanceof IpNetwork ) ) {
      return super.equals( (IpAddress)obj );
    }
    return false;
  }




  /**
   * @return an IpNetwork which represents only the local host.
   */
  public static IpNetwork getLocalHost() {
    return new IpNetwork( IpInterface.getPrimary().getAddress(), HOSTMASK );
  }




  /**
   * Checks to see of the given IpAddress is within this network.
   *
   * @param addr the address to check
   *
   * @return true if the address is in this subnet, false otherwise
   */
  public boolean contains( final IpAddress addr ) {
    boolean retval = false;

    if ( ( addr != null ) || ( netmask != null ) ) {
      final IpAddress ip = new IpAddress( addr );
      ip.applyNetMask( netmask );

      retval = ip.equals( this.applyNetMask( netmask ) );
    }

    return retval;

  }




  /**
   * Checks to see of the given IpAddress is within this network.
   *
   * @param addr the address to check
   *
   * @return true if the address is in this subnet, false otherwise
   */
  public boolean contains( final String addr ) {
    try {
      return contains( new IpAddress( addr ) );
    } catch ( final Exception e ) {
      return false;
    }
  }




  /**
   * Checks to see of the given IpAddress is within this network.
   *
   * @param addr the address to check
   *
   * @return true if the address is in this subnet, false otherwise
   */
  public boolean contains( final InetAddress addr ) {
    try {
      return contains( new IpAddress( addr ) );
    } catch ( final Exception e ) {
      return false;
    }
  }




  /**
   * Given the current address, get the broadcast address for the specified
   * netmask.
   *
   * @return an IpAddress representing the broadcast address for this network
   */
  public IpAddress getBroadcastAddress() {
    final IpAddress temporaryNetmask = (IpAddress)netmask.clone();
    final short[] mask = temporaryNetmask.getOctets();
    final short[] result = new short[octets.length];

    for ( int i = 0; i < mask.length; i++ ) {
      result[i] = octets[i];
      result[i] &= mask[i];

      mask[i] = (byte)( ~mask[i] & 0xffff );
      result[i] |= mask[i];

      if ( result[i] < 0 ) {
        result[i] = (short)( 256 - ( (byte)( result[i] ) * -1 ) );
      }
    }

    IpAddress retval = null;

    try {
      retval = new IpAddress( result );
    } catch ( final Exception e ) {
      // should always work since we have checked source data previously
    }

    return retval;
  }




  /**
   * This is a convenience method to check if a specified address resides in a
   * specified network.
   *
   * @param address The address to check
   * @param network The address of the network
   * @param netmask The netmask of the network address to determine its scope
   *
   * @return True if the given address exists within the network specified by the network address and the netmask, false otherwise.
   */
  public static boolean checkAddressInNetwork( final String address, final String network, final String netmask ) {
    boolean retval = false;

    if ( ( address == null ) || ( network == null ) || ( netmask == null ) ) {
      return false;
    }

    try {
      final IpAddress ip = new IpAddress( address );
      ip.applyNetMask( netmask );

      retval = ip.equals( network );
    } catch ( final IpAddressException ipe ) {}

    return retval;
  }




  /**
   * Returns the numbers representing each of the octets in this network address.
   *
   * @param num_bits_desired the number of bits required
   *
   * @return the requested number of octets from this network address.
   */
  public static short[] getOctets( final int num_bits_desired ) {
    short[] retval = new short[IpAddress.IP4_OCTETS];

    if ( num_bits_desired > 32 ) {
      retval = new short[IpAddress.IP6_OCTETS];
    }

    if ( num_bits_desired > 0 ) {
      final int extra_bits = num_bits_desired % 8;
      final int fullBytes = num_bits_desired / 8;

      // For each full octet, create a short value of 255
      for ( int i = 0; i < fullBytes; ++i ) {
        retval[i] = 255;
      }

      // If we have extra bits to place
      if ( extra_bits != 0 ) {
        retval[fullBytes] = IpNetwork.BITS[extra_bits];
      }

      // populate the rest of the short elements
      for ( int i = fullBytes + 1; i < retval.length; ++i ) {
        retval[i] = 0;
      }

    } else {
      // populate the short elements with all zeros
      for ( int i = 0; i < retval.length; ++i ) {
        retval[i] = 0;
      }
    }

    return retval;
  }




  public IpAddress netMaskInverse() {
    final IpAddress temporaryNetmask = (IpAddress)netmask.clone();
    final short[] mask = temporaryNetmask.getOctets();
    final short[] result = new short[octets.length];
    for ( int i = 0; i < mask.length; i++ ) {
      result[i] = octets[i];
      result[i] &= mask[i];

      mask[i] = (byte)( ~mask[i] & 0xffff );
      // System.out.println("mask " + i + "  " + mask[i]);
      result[i] = mask[i];

      if ( result[i] < 0 ) {
        result[i] = (short)( 256 - ( (byte)( result[i] ) * -1 ) );
      }

    }

    final IpAddress retval = new IpAddress( result );

    return retval;
  }




  public int determineNumberofOctetsToCareAbout() {
    final short[] mask = netMaskInverse().getOctets();
    int i = mask.length - 1;
    int num = 0;
    for ( i = mask.length - 1; i >= 0; i-- ) {
      // System.out.println("Octet " + i + " is " + mask[i]);
      if ( mask[i] > 0 ) {
        num++;
      }
    }
    return num;
  }




  /**
   * @return The string representation of the network in CIDR format.
   */
  @Override
  public String toString() {
    final StringBuffer buf = new StringBuffer();
    final short[] addr = getOctets();

    int last = addr.length - 1;

    // Find the last occurrence of a non-zero segment
    for ( ; addr[last] == 0; last-- );

    // Concatenate the segments up to and including the last non-zero value
    for ( int i = 0; i <= last; i++ ) {
      buf.append( addr[i] );

      if ( i != last ) {
        buf.append( '.' );
      }
    }

    // Delimit the bitmask size
    buf.append( '/' );

    // Figure out the number of bits set in the mask
    final short[] mask = netmask.getOctets();
    int bitcount = 0;

    for ( int i = 0; i < mask.length; i++ ) {
      for ( int x = 0; x < IpNetwork.BITS.length; x++ ) {
        if ( IpNetwork.BITS[x] == mask[i] ) {
          bitcount += x;

          break;
        }
      }
    }

    // append the number of bits in the mask
    buf.append( bitcount );

    // Return the CIDR block format
    return buf.toString();
  }




  /*public IpAddress[] getIpAddressesInNetwork()
   {
   IpAddress baseIpAddress = this.applyNetMask( netmask );
   IpAddress broadCastAddress = this.getBroadcastAddress();
   }*/

  public Iterator<IpAddress> iterator() {
    return ipAddressIterator;
  }

  private class IpNetworkElementIterator implements Iterator<IpAddress> {
    IpAddress ipAddress = null;
    IpAddress broadcastAddress;
    IpAddress netMask = null;
    long testIpAddress = 0;
    long finalIpAddress = 0;
    long startIpAddress = 0;
    boolean hasNextFlag = false;




    private IpNetworkElementIterator( final IpAddress address, final IpAddress mask, final IpNetwork network ) {
      //begins with the subnet IpAddress to figure out where the hosts start
      broadcastAddress = network.getBroadcastAddress();
      netMask = ( (IpAddress)mask.clone() );
      ipAddress = ( (IpAddress)address.clone() ).applyNetMask( netMask );
      startIpAddress = this.IpAddressToDouble( ipAddress );
      finalIpAddress = this.IpAddressToDouble( broadcastAddress );
    }




    @Override
    public boolean hasNext() {
      testIpAddress = startIpAddress + 1;
      if ( testIpAddress < finalIpAddress ) {
        hasNextFlag = true;
        return true;
      } else {
        return false;
      }
    }




    @Override
    public IpAddress next() throws NoSuchElementException {
      if ( hasNextFlag ) {
        ipAddress = this.doubleToIpAddress( testIpAddress, getOctets().length );
        startIpAddress = testIpAddress;
      } else {
        throw new NoSuchElementException();
      }

      return ipAddress;
    }




    @Override
    public void remove() {
      //Since this item is not really a collection but a
      //a mechanism for calculating the next element the remove 
      //does nothing. 
    }




    private final long IpAddressToDouble( final IpAddress address ) {
      final short addressOctets[] = address.getOctets();
      long value = 0;
      final int addressLength = addressOctets.length;
      final int lastElementNum = addressLength - 1;
      for ( int i = addressLength - 1; i >= 0; i-- ) {
        //System.out.println("Multiplier " + (Math.round( Math.pow( 256,lastElementNum-i))));
        value = value + ( addressOctets[i] * ( Math.round( Math.pow( 256, lastElementNum - i ) ) ) );
        //System.out.println("Byte " + i + " value: " + value);
      }

      return value;
    }




    private final IpAddress doubleToIpAddress( final long value, final int length ) {
      final short[] retval = new short[8];
      retval[0] = (short)( ( value >> 56 ) & 0x00FF );
      retval[1] = (short)( ( value >>> 48 ) & 0x00FF );
      retval[2] = (short)( ( value >>> 40 ) & 0x00FF );
      retval[3] = (short)( ( value >>> 32 ) & 0x00FF );
      retval[4] = (short)( ( value >>> 24 ) & 0x00FF );
      retval[5] = (short)( ( value >>> 16 ) & 0x00FF );
      retval[6] = (short)( ( value >>> 8 ) & 0x00FF );
      retval[7] = (short)( ( value >>> 0 ) & 0x00FF );

      final short[] ipArray = new short[length];

      System.arraycopy( retval, retval.length - length, ipArray, 0, length );
      final IpAddress address = new IpAddress( ipArray );

      return address;
    }

  }

}
