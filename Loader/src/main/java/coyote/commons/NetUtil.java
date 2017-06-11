/*
 * Copyright (c) 2006 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 *
 * Contributors:
 *   Stephan D. Cote 
 *      - Initial API and implementation
 */
package coyote.commons;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;

import coyote.commons.network.IpAddress;
import coyote.commons.network.IpNetwork;


public class NetUtil {

  private static InetAddress localAddress = null;




  /**
   * Return a port number that can be used to create a socket on the given
   * address starting with port 1.
   *
   * @param address The address on which to find the next available port 
   *
   * @return the next available port on the given address
   */
  public static int getNextAvailablePort( InetAddress address ) {
    return getNextAvailablePort( address, 1 );
  }




  /**
   * Return a port number that can be used to create a socket with the given
   * port on the local address.
   * 
   * <p>If you are looking for port 80, but it is taken, this method will 
   * return the next higher port starting with that port.  This will keep your 
   * ports grouped together.
   *
   * @param port The number of the port on which to start looking
   *
   * @return the next available port on the local address.
   */
  public static int getNextAvailablePort( int port ) {
    return getNextAvailablePort( null, port );
  }




  /**
   * Return a port number that can be used to create a socket on the given
   * address starting with the given port.
   *
   * <p>If the given port can be used to create a server socket (TCP) then that
   * port number will be used, otherwise, the port number will be incremented
   * and tested until a free port is found.
   *
   * <p>This is not thread-safe nor fool-proof. A valid value can be returned,
   * yet when a call is made to open a socket at that port, another thread may
   * have already opened a socket on that port. A better way would be to use
   * the <code>getNextServerSocket(address,port)</code> method if it desired to
   * obtain the next available server.
   *
   * @param address The address on which to find the next available port 
   * @param port The number of the port on which to start looking
   *
   * @return the next available port on the given address.
   */
  public static int getNextAvailablePort( InetAddress address, int port ) {
    ServerSocket socket = getNextServerSocket( address, port, 0 );
    int retval = -1;

    if ( socket != null ) {
      // Get the port as a return value
      retval = socket.getLocalPort();

      // Close the un-needed socket
      try {
        socket.close();
      } catch ( IOException e ) {
        // Ignore it
      }
    }

    return retval;
  }




  /**
   * Return a TCP server socket on the given address and port, incrementing the
   * port until a server socket can be opened.
   *
   * @param address The address on which to find the next available port 
   * @param port The number of the port on which to start looking
   * @param backlog requested maximum length of the queue of incoming connections.
   *
   * @return the next available port on the local address.
   */
  public static ServerSocket getNextServerSocket( InetAddress address, int port, int backlog ) {
    int i = port;
    ServerSocket socket = null;

    // If no address was given, then try to determine our local address so we
    // can use our main address instead of 127.0.0.1 which may be chosen by the
    // VM if it is not specified in the ServerSocket constructor
    if ( address == null ) {
      address = getLocalAddress();
    }

    while ( validatePort( i ) != 0 ) {
      try {
        if ( address == null ) {
          socket = new ServerSocket( i, backlog );
        } else {
          socket = new ServerSocket( i, backlog, address );
        }

        if ( socket != null ) {
          return socket;
        }

      } catch ( IOException e ) {
        i++;
      }
    }

    return null;
  }




  /**
   * Validate if the port is within the valid range for an IP port.
   * 
   * @param port the port to check
   *
   * @return the value of the port if it is within range, 0 otherwise.
   */
  public static int validatePort( int port ) {
    if ( ( port < 0 ) || ( port > 0xFFFF ) ) {
      return 0;
    } else {
      return port;
    }
  }




  /**
   * Get the IP Address by which the rest of the world knows us.
   *
   * <p>This is useful in helping insure that we don't accidently start binding
   * to or otherwise using the local loopback address.
   *
   * <p>This requires some type of IP address resolver to be installed, like
   * DNS, NIS or at least hostname lookup.
   *
   * @return The InetAddress representing the host on the network and NOT the
   *         loopback address.
   */
  public static InetAddress getLocalAddress() {
    // If we already looked this up, use the cached result to save time
    if ( localAddress != null ) {
      return localAddress;
    }

    // No cached result, figure it out and cache it for later
    InetAddress addr = null;

    // Make sure we get the IP Address by which the rest of the world knows us
    // or at least, our host's default network interface
    try {
      // This helps insure that we do not get localhost (127.0.0.1)
      addr = InetAddress.getByName( InetAddress.getLocalHost().getHostName() );
    } catch ( UnknownHostException e ) {
      // Aaaaww Phooey! DNS is not working or we are not in it.
      addr = null;
    }

    // If it looks like a unique address, return it, otherwise try again
    if ( ( addr != null ) && !addr.getHostAddress().equals( "127.0.0.1" ) && !addr.getHostAddress().equals( "0.0.0.0" ) ) {
      localAddress = addr;
      return addr;
    }

    // Try it the way it's supposed to work
    try {
      addr = InetAddress.getLocalHost();
    } catch ( Exception ex ) {
      addr = null;
    }

    localAddress = addr;

    return addr;
  }




  /**
   * Return a InetAddress that is suitable for use as a broadcast address.
   *
   * <p>Take a mask in the form of "255.255.111.0" and apply it to the given
   * address to calculate the broadcast address for the given subnet mask.</p>
   *
   * @param addr InetAddress representing a node in a subnet.
   * @param mask Valid dotted-quad netmask.
   *
   * @return an InetAddress capable of being used as a broadcast address in the
   *         given nodes subnet.
   */
  public static InetAddress getBroadcastAddress( String addr, String mask ) {
    InetAddress node = null;

    if ( mask != null ) {
      try {
        node = InetAddress.getByName( addr );

        IpNetwork network = new IpNetwork( addr, mask );
        IpAddress adr = network.getBroadcastAddress();
        return InetAddress.getByName( adr.toString() );
      } catch ( Exception ignore ) {
        // just return the node address
        try {
          node = InetAddress.getByName( "255.255.255.255" );
        } catch ( Exception e ) {
          // should always work
        }
      }
    }

    return node;
  }




  /**
   * Return a InetAddress that is suitable for use as a broadcast address.
   *
   * <p>Take a mask in the form of "255.255.111.0" and apply it to the local
   * address to calculate the broadcast address for the given subnet mask.</p>
   *
   * @param mask Valid dotted-quad netmask.
   *
   * @return an InetAddress capable of being used as a broadcast address
   */
  public static InetAddress getLocalBroadcast( String mask ) {
    InetAddress retval = getLocalAddress();

    if ( retval != null ) {
      return getBroadcastAddress( retval, mask );
    }

    return retval;
  }




  /**
   * Return a InetAddress that is suitable for use as a broadcast address.
   *
   * <p>Take a mask in the form of "255.255.111.0" and apply it to the given
   * address to calculate the broadcast address for the given subnet mask.</p>
   *
   * @param addr InetAddress representing a node in a subnet.
   * @param mask Valid dotted-quad netmask.
   *
   * @return an InetAddress capable of being used as a broadcast address in the
   *         given nodes subnet.
   */
  public static InetAddress getBroadcastAddress( InetAddress addr, String mask ) {
    if ( mask != null ) {
      try {
        IpNetwork network = new IpNetwork( addr.getHostAddress(), mask );
        IpAddress adr = network.getBroadcastAddress();
        return InetAddress.getByName( adr.toString() );
      } catch ( Exception e ) {
        // just return the address
      }
    }

    return addr;
  }

}
