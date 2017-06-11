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
package coyote.commons.security;

//import static org.junit.Assert.*;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import coyote.commons.network.IpAddress;
import coyote.commons.network.IpAddressException;
import coyote.commons.network.IpNetwork;


/**
 * 
 */
public class OperationFrequencyTest {

  /**
   * Test method for {@link coyote.commons.security.OperationFrequency#addNetwork(coyote.commons.network.IpNetwork, short, long)}.
   */
  @Test
  public void testAddNetwork() {
    OperationFrequency dosTable = new OperationFrequency();
    assertTrue( dosTable.getNetworkCount() == 0 );

    IpNetwork addr = null;
    try {
      addr = new IpNetwork( "192.168/16" );
    } catch ( IpAddressException ignore ) {}

    short limit = 64;
    long duration = 1000;
    dosTable.addNetwork( addr, limit, duration );
    assertTrue( dosTable.getNetworkCount() == 1 );
  }




  /**
   * Test method for {@link coyote.commons.security.OperationFrequency#addAddress(coyote.commons.network.IpAddress, short, long)}.
   */
  @Test
  public void testAddAddress() {
    OperationFrequency dosTable = new OperationFrequency();
    assertTrue( dosTable.getAddressCount() == 0 );
    IpAddress addr = null;
    try {
      addr = new IpAddress( "192.168.1.1" );
    } catch ( IpAddressException ignore ) {}

    short limit = 64;
    long duration = 1000;
    Object obj = dosTable.addAddress( addr, limit, duration );
    assertNotNull( obj );
    assertTrue( dosTable.getAddressCount() == 1 );
  }




  /**
   * Test method for {@link coyote.commons.security.OperationFrequency#check(coyote.commons.network.IpAddress)}.
   */
  @Test
  public void testAddressCheck() {
    OperationFrequency dosTable = new OperationFrequency();

    IpAddress addr = null;
    try {
      addr = new IpAddress( "192.168.1.1" );
    } catch ( IpAddressException ignore ) {}

    short limit = 3;
    long duration = 10000;
    Object obj = dosTable.addAddress( addr, limit, duration );
    assertNotNull( obj );
    assertTrue( dosTable.check( addr ) ); // 1 is fine
    assertTrue( dosTable.check( addr ) ); // 2 is OK
    assertTrue( dosTable.check( addr ) ); // 3 is the limit
    assertFalse( dosTable.check( addr ) ); // the 4th should be too much

  }




  /**
   * Test method for {@link coyote.commons.security.OperationFrequency#expire(long)}.
   */
  @Test
  public void testExpire() {
    OperationFrequency dosTable = new OperationFrequency();

    IpAddress addr = null;
    try {
      addr = new IpAddress( "192.168.1.1" );
    } catch ( IpAddressException ignore ) {}

    short limit = 3;
    long duration = 1000;

    dosTable.addAddress( addr, limit, duration );

    dosTable.expire( 0 ); // remove everything
    assertTrue( dosTable.getAddressCount() == 0 );
    
    // TODO test empty tables
    // TODO test filled tables
    // TODO test filled tables with some delay

  }

}
