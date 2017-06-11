/*
 * Copyright (c) 2017 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 *
 * Contributors:
 *   Stephan D. Cote 
 *      - Initial concept and implementation
 */
package coyote.commons;

import static org.junit.Assert.fail;

import java.net.InetAddress;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;


/**
 * 
 */
public class NetUtilTest {

  /**
   * @throws java.lang.Exception
   */
  @BeforeClass
  public static void setUpBeforeClass() throws Exception {}




  /**
   * @throws java.lang.Exception
   */
  @AfterClass
  public static void tearDownAfterClass() throws Exception {}




  /**
   * Test method for {@link coyote.commons.NetUtil#getNextAvailablePort(java.net.InetAddress)}.
   */
  @Ignore
  public void testGetNextAvailablePortInetAddress() {
    fail( "Not yet implemented" ); // TODO
  }




  /**
   * Test method for {@link coyote.commons.NetUtil#getNextAvailablePort(int)}.
   */
  @Ignore
  public void testGetNextAvailablePortInt() {
    fail( "Not yet implemented" ); // TODO
  }




  /**
   * Test method for {@link coyote.commons.NetUtil#getNextAvailablePort(java.net.InetAddress, int)}.
   */
  @Ignore
  public void testGetNextAvailablePortInetAddressInt() {
    fail( "Not yet implemented" ); // TODO
  }




  /**
   * Test method for {@link coyote.commons.NetUtil#getNextServerSocket(java.net.InetAddress, int, int)}.
   */
  @Ignore
  public void testGetNextServerSocket() {
    fail( "Not yet implemented" ); // TODO
  }




  /**
   * Test method for {@link coyote.commons.NetUtil#validatePort(int)}.
   */
  @Ignore
  public void testValidatePort() {
    fail( "Not yet implemented" ); // TODO
  }




  /**
   * Test method for {@link coyote.commons.NetUtil#getLocalAddress()}.
   */
  @Ignore
  public void testGetLocalAddress() {
    fail( "Not yet implemented" ); // TODO
  }




  /**
   * Test method for {@link coyote.commons.NetUtil#getBroadcastAddress(java.lang.String, java.lang.String)}.
   */
  @Test
  public void testGetBroadcastAddress() {
    try {
      String mask = "255.255.0.0";
      InetAddress addr = NetUtil.getLocalBroadcast( mask );
      System.out.println( "Local broadcast for '" + mask + "' = " + addr.getHostName() );
    } catch ( Exception ex ) {
      fail( "Could calc local broadcast address " + ex.getMessage() );
    }

    try {
      String mask = "0.0.0.0";
      InetAddress addr = NetUtil.getLocalBroadcast( mask );
      System.out.println( "Local broadcast for '" + mask + "' = " + addr.getHostName() );
    } catch ( Exception ex ) {
      fail( "Could calc local broadcast address " + ex.getMessage() );
    }
  }

}
