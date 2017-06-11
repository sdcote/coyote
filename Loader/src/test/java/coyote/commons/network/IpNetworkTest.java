/*
 * Copyright (c) 2016 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 *
 * Contributors:
 *   Stephan D. Cote 
 *      - Initial concept and implementation
 */
package coyote.commons.network;

//import static org.junit.Assert.*;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Iterator;

import org.junit.Test;


/**
 * 
 */
public class IpNetworkTest {

  /**
   * Test method for {@link coyote.commons.network.IpNetwork#IpNetwork(coyote.commons.network.IpAddress, coyote.commons.network.IpAddress)}.
   */
  @Test
  public void testIpNetworkIpAddressIpAddress() throws IpAddressException {
    IpAddress address = new IpAddress( "192.168.1.1" );
    IpAddress netMask = new IpAddress( "255.255.0.0" );
    new IpNetwork( address, netMask );

    address = new IpAddress( "150.10.10.10" );
    netMask = new IpAddress( "255.255.252.0" );
    new IpNetwork( address, netMask );
  }




  /**
   * Test method for {@link coyote.commons.network.IpNetwork#getBroadcastAddress()}.
   */
  @Test
  public void testGetBroadcastAddress() throws IpAddressException {
    IpAddress address = new IpAddress( "192.168.1.1" );
    IpAddress netMask = new IpAddress( "255.255.0.0" );
    IpNetwork ipNetwork = new IpNetwork( address, netMask );
    assertTrue( "192.168.255.255".equals( ipNetwork.getBroadcastAddress().toString() ) );

    address = new IpAddress( "150.10.10.10" );
    netMask = new IpAddress( "255.255.252.0" );
    ipNetwork = new IpNetwork( address, netMask );
    assertTrue( "150.10.11.255".equals( ipNetwork.getBroadcastAddress().toString() ) );
  }




  /**
   * Test method for {@link coyote.commons.network.IpNetwork#IpNetwork(java.lang.String, java.lang.String)}.
   */
  @Test
  public void testIpNetworkStringString() throws IpAddressException {
    new IpNetwork( "192.168.1.1", "255.255.0.0" );
  }




  /**
   * Test method for {@link coyote.commons.network.IpNetwork#IpNetwork(java.lang.String)}.
   */
  @Test
  public void testIpNetworkString() throws IpAddressException {
    new IpNetwork( "206.13.01.48/25" );
  }




  /**
   * 
   */
  @Test
  public void testHostOnlyNetwork() throws IpAddressException {
    IpNetwork network = new IpNetwork( IpAddress.IPV4_LOOPBACK_ADDRESS, IpNetwork.HOSTMASK );
    if ( !network.contains( IpAddress.IPV4_LOOPBACK_ADDRESS ) ) {
      fail( "Address should be included in host-only network" );
    }
  }




  /**
   * Test method for {@link coyote.commons.network.IpNetwork#iterator()}.
   */
  @Test
  public void testIterator() throws IpAddressException {
    IpAddress address = new IpAddress( "150.10.10.10" );
    IpAddress netMask = new IpAddress( "255.255.252.0" );
    IpNetwork network = new IpNetwork( address, netMask );

    Iterator<IpAddress> iter = network.iterator();
    int ipaddresscount = 0;
    while ( iter.hasNext() ) {
      iter.next();
      ipaddresscount++;
    }
    assertTrue( ipaddresscount == 1022 );

    // A more common example
    address = new IpAddress( "192.168.1.1" );
    netMask = new IpAddress( "255.255.0.0" );
    network = new IpNetwork( address, netMask );

    iter = network.iterator();
    ipaddresscount = 0;
    while ( iter.hasNext() ) {
      iter.next();
      ipaddresscount++;
    }
    assertTrue( ipaddresscount == 65534 );
  }

}
