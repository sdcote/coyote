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
package coyote.nmea;

//import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

import coyote.nmea.Checksum;


/**
 * 
 */
public class ChecksumTest {

  @Test
  public void testAdd() {
    String a = "$GPGLL,6011.552,N,02501.941,E,120045,A";
    String b = "$GPGLL,6011.552,N,02501.941,E,120045,A*";
    String c = "$GPGLL,6011.552,N,02501.941,E,120045,A*00";
    final String expected = a.concat( "*26" );
    assertEquals( expected, Checksum.add( a ) );
    assertEquals( expected, Checksum.add( b ) );
    assertEquals( expected, Checksum.add( c ) );
  }




  //  @Test
  //  public void testCalculate() {
  //    assertEquals( "1D", Checksum.calculate( BODTest.EXAMPLE ) );
  //    assertEquals( "63", Checksum.calculate( GGATest.EXAMPLE ) );
  //    assertEquals( "26", Checksum.calculate( GLLTest.EXAMPLE ) );
  //    assertEquals( "0B", Checksum.calculate( RMCTest.EXAMPLE ) );
  //    assertEquals( "3D", Checksum.calculate( GSATest.EXAMPLE ) );
  //    assertEquals( "73", Checksum.calculate( GSVTest.EXAMPLE ) );
  //    assertEquals( "58", Checksum.calculate( RMBTest.EXAMPLE ) );
  //    assertEquals( "25", Checksum.calculate( RTETest.EXAMPLE ) );
  //  }

  @Test
  public void testDelimiterIndex() {
    assertEquals( 13, Checksum.index( "$GPGGA,,,,,,," ) );
    assertEquals( 13, Checksum.index( "$GPGGA,,,,,,,*00" ) );
  }

}
