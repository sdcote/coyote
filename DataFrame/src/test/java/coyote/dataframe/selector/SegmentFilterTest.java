/*
 * Copyright (c) 2003 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 *
 * Contributors:
 *   Stephan D. Cote 
 *      - Initial API and implementation
 */
package coyote.dataframe.selector;

//import static org.junit.Assert.*;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

import coyote.commons.SegmentFilter;


/**
 */
public class SegmentFilterTest {

  /**
   * Method testConstructor0
   */
  @Test
  public void testConstructor0()
  {
    SegmentFilter filter = null;
    String testPattern = "my.sample.subject";

    try {
      filter = new SegmentFilter( testPattern );
    }
    catch( Exception ex ) {
      fail( "Could not parse '" + testPattern + "'" );
    }

    String[] segments = filter.getSegments();
    assertTrue( "SegmentCount wrong", segments.length == 3 );
    assertTrue( "SegmentContent wrong", segments[0].equals( "my" ) );
    assertTrue( "SegmentContent wrong", segments[1].equals( "sample" ) );
    assertTrue( "SegmentContent wrong", segments[2].equals( "subject" ) );
  }




  /**
   * Method testConstructor1
   */
  @Test
  public void testConstructor1()
  {
    SegmentFilter filter = null;
    String testPattern = "my.sample.>";

    try {
      filter = new SegmentFilter( testPattern );
    }
    catch( Exception ex ) {
      fail( "Could not parse '" + testPattern + "'" );
    }

    String[] segments = filter.getSegments();
    assertTrue( "SegmentCount wrong", segments.length == 3 );
    assertTrue( "SegmentContent wrong", segments[0].equals( "my" ) );
    assertTrue( "SegmentContent wrong", segments[1].equals( "sample" ) );
    assertTrue( "SegmentContent wrong", segments[2].equals( ">" ) );
  }




  /**
   * Method testConstructor2
   */
  @Test
  public void testConstructor2()
  {
    SegmentFilter filter = null;
    String testPattern = "my.simple.*.subject.>";

    try {
      filter = new SegmentFilter( testPattern );
    }
    catch( Exception ex ) {
      fail( "Could not parse '" + testPattern + "'" );
    }

    String[] segments = filter.getSegments();
    assertTrue( "SegmentCount wrong", segments.length == 5 );
    assertTrue( "SegmentContent wrong", segments[0].equals( "my" ) );
    assertTrue( "SegmentContent wrong", segments[1].equals( "simple" ) );
    assertTrue( "SegmentContent wrong", segments[2].equals( "*" ) );
    assertTrue( "SegmentContent wrong", segments[3].equals( "subject" ) );
    assertTrue( "SegmentContent wrong", segments[4].equals( ">" ) );

    // System.out.println( filter.toString() );
    // for( int i = 0; i < segments.length; i++ )
    // {
    // System.out.println( "segment" + i + ": '" + segments[i] + "'" );
    // }

  }




  /**
   * Method testConstructor3
   */
  @Test
  public void testConstructor3()
  {
    String testPattern = "my.si>mple.*.subject.>";

    try {
      new SegmentFilter( testPattern );

      fail( "Should not have parsed '" + testPattern + "'" );
    }
    catch( Exception ex ) {
      // good
    }
  }




  /**
   * Method testMatch0
   */
  @Test
  public void testMatch0()
  {
    SegmentFilter filter = null;
    String testPattern = "my.simple.*.subject.>";

    try {
      filter = new SegmentFilter( testPattern );
    }
    catch( Exception ex ) {
      fail( "Could not parse '" + testPattern + "'" );
    }

    assertTrue( filter.matches( "my.simple.wildcard.subject.test" ) );
  }




  /**
   * Method testMatch1
   */
  @Test
  public void testMatch1()
  {
    SegmentFilter filter = null;
    String testPattern = "net.bralyn.util.SegmentFilter.>";

    try {
      filter = new SegmentFilter( testPattern );
    }
    catch( Exception ex ) {
      fail( "Could not parse '" + testPattern + "'" );
    }

    assertTrue( filter.matches( "net.bralyn.util.SegmentFilter.class" ) );
    assertTrue( filter.matches( "net.bralyn.util.SegmentFilter.java" ) );

  }




  /**
   * Method testMatch2
   */
  @Test
  public void testMatch2()
  {
    SegmentFilter filter = null;
    String testPattern = "EVENT.>";

    try {
      filter = new SegmentFilter( testPattern );
    }
    catch( Exception ex ) {
      fail( "Could not parse '" + testPattern + "'" );
    }

    assertTrue( filter.matches( "EVENT.Message" ) );
    assertTrue( filter.matches( "EVENT.Metric" ) );

    if( filter.matches( "METRIC.EVENT.description" ) ) {
      fail( "Failed to filter 'METRIC'" );
    }

  }

}