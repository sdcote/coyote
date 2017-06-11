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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;


/**
 * 
 */
public class IpAclTest {

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




  @Test
  public void testConstrucor() {
    try {
      IpAcl acl = new IpAcl();
      acl.add( "192.168/16", true );
      acl.add( "10/8", false );
    } catch ( Exception ex ) {
      fail( "Could not construct: " + ex.getMessage() );
    }
  }




  @Test
  public void testAllows() {
    try {
      IpAcl acl = new IpAcl( IpAcl.DENY );
      acl.add( "192.168/16", true );
      acl.add( "10/8", false );

      String arg = "192.168.1.100";
      assertTrue( "Should allow '" + arg + "'", acl.allows( arg ) );

      arg = "10.8.107.12";

      assertTrue( "Should NOT allow '" + arg + "'", !acl.allows( arg ) );

      // if( acl.allows( arg ) )
      // {
      // System.out.println( "Error: ACL allows '" + arg + "'" );
      // }
      // else
      // {
      // System.out.println( "ACL denies '" + arg + "'" );
      // }
    } catch ( Exception ex ) {
      fail( "Could not construct: " + ex.getMessage() );
    }

    try {
      IpAcl acl = new IpAcl( IpAcl.DENY );

      // Only allow this one IP address
      acl.add( "192.168.1.100/32", IpAcl.ALLOW );

      // This should pass
      String arg = "192.168.1.100";
      assertTrue( "Should allow '" + arg + "'", acl.allows( arg ) );

      // These should not pass
      arg = "10.8.107.12";

      assertTrue( "Should NOT allow '" + arg + "'", !acl.allows( arg ) );

      arg = "192.168.1.101";

      assertTrue( "Should NOT allow '" + arg + "'", !acl.allows( arg ) );
    } catch ( Exception ex ) {
      fail( "Could not construct: " + ex.getMessage() );
    }

    // Test the ordering, 192.168.100 subnet is denied, but the rest of 192.168 
    // is allowed
    try {
      IpAcl acl = new IpAcl( IpAcl.DENY );
      acl.add( "192.168.100/24", false );
      acl.add( "192.168/16", true );

      String arg = "192.168.100.23";
      assertFalse( "Should NOT allow '" + arg + "'", acl.allows( arg ) );

      arg = "192.168.23.100";
      assertTrue( "Should allow '" + arg + "'", acl.allows( arg ) );

      arg = "10.8.107.12";
      assertFalse( "Should NOT allow '" + arg + "'", acl.allows( arg ) );

    } catch ( Exception ex ) {
      fail( "Could not construct: " + ex.getMessage() );
    }

  }

}