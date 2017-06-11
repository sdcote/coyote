/*
 * Copyright (c) 2004 Stephan D. Cote' - All rights reserved.
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
import java.net.UnknownHostException;


/**
 * Simple Class to do some IP Address manipulation, including applying a subnet
 * mask to an address.
 *
 * <p>Sample Code
 * <PRE>
 * import coyote.commons.network.IpAddress;
 * ...
 * boolean bResult=false;
 * try {
 *   ip=new IpAddress(cUserIPAddress);
 *   ip.applyNetMask(oUser.NetMask);
 *   bResult=ip.equals(oUser.AllowedNetwork);
 * } catch (IpAddressException ipae) {
 * }</PRE>
 */
public class IpAddress {
  protected final static int IP4_OCTETS = 4;
  protected final static int IP6_OCTETS = 16;

  public static final String IP4_LOOPBACK = "127.0.0.1";
  public static final String IP6_LOOPBACK = "0:0:0:0:0:0:0:1";

  public static final IpAddress IPV4_LOOPBACK_ADDRESS = new IpAddress( new short[] { 127, 0, 0, 1 } );
  public static final IpAddress IPV6_LOOPBACK_ADDRESS = new IpAddress( new short[] { 0, 0, 0, 0, 0, 0, 0, 1 } );

  protected short[] octets = new short[IP4_OCTETS];

  private InetAddress netAddress = null;
  private String dnsName = null;




  /**
   * Construct a new blank IP Address (0.0.0.0)
   */
  public IpAddress() {}




  /**
   * Construct a new IP Address object from an address string.
   *
   * <p>A common format for an IP Address is: 192.168.117.32, but this
   * constructor will allow truncated addresses in the form of 192.168 which
   * can be used to specify a network, as the remaining octets of the address
   * will be populated with zeros.</p>
   *
   * @param addr the string representation of the "dotted-quad" address
   *
   * @throws IpAddressException if there is a problem parsing the address string
   */
  public IpAddress( String addr ) throws IpAddressException {
    if ( addr == null )
      throw new IpAddressException( "Null argument to constructor" );

    octets = getOctets( addr.trim() );
  }




  /**
   * Construct a new IP Address object from an InetAddress object.
   *
   * @param addr the InetAddress to convert
   *
   * @throws IpAddressException if the InetAddress reference is null
   */
  public IpAddress( InetAddress addr ) throws IpAddressException {
    if ( addr != null ) {
      byte[] bytes = addr.getAddress();
      octets = new short[bytes.length];
      for ( int i = 0; i < bytes.length; octets[i] = fixByte( bytes[i++] ) );
    } else {
      throw new IpAddressException( "address reference was null" );
    }
  }




  /**
   * Construct a new IpAddress object from the byte representation of the address
   * 
   * @param data the bytes to parse
   * 
   * @throws IpAddressException if data is null, empty, or an odd number of bytes
   */
  public IpAddress( byte[] data ) throws IpAddressException {
    if ( data == null )
      throw new IpAddressException( "Null argument in constructor" );

    if ( data.length == 0 )
      throw new IpAddressException( "No data in constructor array argument" );

    if ( ( data.length % 2 ) != 0 )
      throw new IpAddressException( "Odd number of bytes in constructor array argument" );

    octets = new short[data.length];
    for ( int i = 0; i < data.length; octets[i] = fixByte( data[i++] ) );
  }




  /**
   * Copy an existing IP Address into a new object.
   *
   * @param master the IpAddress to use to create this object
   */
  public IpAddress( IpAddress master ) {
    short[] aMaster = master.getOctets();
    octets = new short[aMaster.length];

    for ( int i = 0; i < aMaster.length; i++ ) {
      octets[i] = aMaster[i];
    }
  }




  /**
   * Clone this object.
   */
  public Object clone() {
    return new IpAddress( this );
  }




  /**
   * Constructor IpAddress
   *
   * @param octets the numeric values for each portion of the address
   */
  protected IpAddress( short[] octets ) {
    this.octets = octets;
  }




  /**
   * Tests to see if the given dotted representation of an address matches 
   * this instance.
   *
   * @param Octets the dotted representation of the address to test (i.e. "X.X.X.X")
   *
   * @return true if the address is equivalent, false otherwise
   */
  public boolean equals( String Octets ) {
    return this.toString().equals( Octets );
  }




  /**
   * Tests to see if the given address is equivalent to this address.
   *
   * @param addr the address to test
   *
   * @return true if the address is equivalent, false otherwise
   */
  public boolean equals( IpAddress addr ) {
    if ( addr == null ) {
      return false;
    }

    short[] addrOctets = addr.getOctets();
    boolean retval = ( addrOctets.length == octets.length );

    // If the lengths match
    if ( retval ) {
      // ...perform a byte-by-byte check
      for ( int i = 0; retval && ( i < octets.length ); i++ ) {
        retval &= ( octets[i] == addrOctets[i] );
      }
    }

    return retval;
  }




  /**
   * @return the IP Address as an array of Shorts.
   */
  public short[] getOctets() {
    return octets;
  }




  /**
   * @return the dotted notation of the IP Address as a string.
   */
  @Override
  public String toString() {
    StringBuffer sb = new StringBuffer();

    for ( int i = 0; i < octets.length; i++ ) {
      sb.append( octets[i] );

      if ( i < ( octets.length - 1 ) ) {
        sb.append( '.' );
      }
    }

    return sb.toString();
  }




  /**
   * @return the address reversed. IP 1.2.3.4 is returned as 4.3.2.1 as in a DNS
   *         record entry.
   */
  public String toNSFormat() {
    StringBuffer sb = new StringBuffer();

    for ( int i = ( octets.length - 1 ); i > -1; i-- ) {
      sb.append( octets[i] );

      if ( i > ( 0 ) ) {
        sb.append( '.' );
      }
    }

    return sb.toString();
  }




  /**
   * @return a new InetAddress object representing this IpAddress.
   */
  public InetAddress toInetAddress() {
    if ( netAddress == null ) {
      netAddress = toInetAddress( this );
    }
    return netAddress;
  }




  /**
   * Represent the given IP Address as a standard InetAddress.
   * 
   * @param addr the address to represent
   * 
   * @return a new InetAddress object representing the given IpAddress.
   */
  public static InetAddress toInetAddress( IpAddress addr ) {
    if ( addr != null ) {
      try {
        return InetAddress.getByAddress( addr.getBytes() );
      } catch ( UnknownHostException e ) {}
    }

    return null;
  }




  /**
   * @return the binary representation of the IP address.
   */
  public byte[] getBytes() {
    byte[] bytes = new byte[octets.length];

    for ( int i = 0; i < octets.length; i++ ) {
      bytes[i] = (byte)octets[i];
    }
    return bytes;
  }




  /**
   * Helper method to convert a dotted string representation into a short[] array.
   *
   * @param addr the string representation of the address to convert
   *
   * @return an array of octets representing the given address
   *
   * @throws IpAddressException if the string could not be parsed
   */
  public static short[] getOctets( String addr ) throws IpAddressException {
    int octetCount = 0;
    int digit = 0;
    short[] temp = new short[IP4_OCTETS];

    if ( addr.length() > 15 ) {
      temp = new short[IP6_OCTETS];
    }

    // create a character array for parsing
    char[] chars = addr.toCharArray();

    // create a StringBuffer to hold the character representation of the digits
    StringBuffer buf = new StringBuffer();

    // goofy for loop to make sure we parse everything, delimiting on both the
    // '.' character and the end of the array
    for ( int i = 0; i <= chars.length; i++ ) {
      if ( ( i == chars.length ) || ( chars[i] == '.' ) ) {
        try {
          digit = Integer.parseInt( buf.toString() );
        } catch ( NumberFormatException nfe ) {
          throw new IpAddressException( "Segment '" + buf.toString() + "' is not a number at position (" + i + ")" );
        }

        if ( ( digit < 0 ) || ( digit > 255 ) ) {
          throw new IpAddressException( "Segment '" + digit + "' out of range" );
        }

        temp[octetCount] = (short)digit;

        buf.setLength( 0 );

        octetCount++;

        // avoid over-runs
        if ( i == chars.length ) {
          break;
        }
      } else {
        buf.append( chars[i] );
      }

    }

    // If we did not parse anything...
    if ( octetCount == 0 ) {
      throw new IpAddressException( "Invalid address" );
    } else if ( octetCount < temp.length ) {
      // fillup the rest of the array
      for ( int i = octetCount; i < temp.length; i++ ) {
        temp[i] = 0;
      }
    }

    return temp;
  }




  /**
   * Apply a netmask to an address, leaving only the network portion of the
   * address.
   *
   * <p>For example: 192.168.100.195 netmask 255.255.255.224 returns
   * 192.168.100.192</p>
   *
   * @param mask the string representation of the network mask to apply
   *
   * @return the IpAddress representing the network
   *
   * @throws IpAddressException if there were problems parsing the mask
   */
  public IpAddress applyNetMask( String mask ) throws IpAddressException {
    return applyNetMask( new IpAddress( mask ) );
  }




  /**
   * Apply a netmask to an address, leaving only the network portion of the
   * address.
   *
   * <p><CODE>
   * IpAddress addr = new IpAddress("192.168.0.23");
   * addr.applyNetMask("255.255.255.0");
   * System.out.println(addr.toString());
   * // returns "192.168.0.0"
   * </CODE></p>
   *
   * @param netmask the network mask to apply
   *
   * @return the IpAddress representing the network
   */
  public IpAddress applyNetMask( IpAddress netmask ) {
    short[] retval;
    retval = netmask.getOctets();

    for ( int i = 0; i < octets.length; i++ ) {
      octets[i] &= retval[i];
    }

    return this;
  }




  /**
   * Get the DNS name of this address.
   * 
   * <p>The name will be calculated the first time this method is called and 
   * its value will be cached for all future accesses.<p>
   * 
   * <p>It is important to keep in mind that the return value will depend upon 
   * this host name resolver. If this IP address is located in a local host 
   * resolver file, then the return value will be whatever is specified in that 
   * file and a DNS lookup may never take place.</p>
   * 
   * @return The name to which this address resolves on this hosts resolver, or 
   *         null if the address is not valid or in DNS or the hosts resolver.
   */
  public String getDnsName() {
    if ( dnsName == null ) {
      InetAddress ina = toInetAddress();
      if ( ina != null ) {
        dnsName = ina.getHostName().toLowerCase();
      }
    }
    return dnsName;
  }




  /**
   * @return Just the domain portion of this hosts name.
   */
  public String getDomain() {
    String hostname = getDnsName();

    if ( hostname != null ) {
      hostname = hostname.toLowerCase();

      int indx = hostname.indexOf( '.' );

      if ( indx > 0 ) {
        String retval = hostname.substring( indx + 1 );

        // we should have a DOMAIN.COM type string
        if ( retval.indexOf( '.' ) > 0 ) {
          return retval;
        } else {
          // No domain...how odd is that?
          return hostname;
        }

      } // if there are segments

    } // hostname !null

    return null;
  }




  /**
   * @return The hostname of this address without the domain portion.
   */
  public String getRelativeHostname() {
    String retval = null;

    String hostname = getDnsName();

    if ( hostname != null ) {
      int pt = hostname.indexOf( '.' );
      if ( pt > 0 ) {
        retval = hostname.substring( 0, pt );

        if ( retval.length() < 3 ) {
          try {
            // Uh-Oh, this may be a number, if it is return the whole name
            Integer.parseInt( retval );
            return hostname;
          } catch ( NumberFormatException e ) {}

        } // len<3

      } // contains a '.'
      else {
        retval = hostname;
      }

    } //hostname !null

    return retval;
  }




  /**
  * bytes are signed; let's fix them...
  *
  * @param b the byte to convert
  *
  * @return the byte encoded as an unsigned value
  */
  public final static short fixByte( final byte b ) {
    if ( b < 0 ) {
      return (short)( b + 256 );
    }

    return b;
  }

}