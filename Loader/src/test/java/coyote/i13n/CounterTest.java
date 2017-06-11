/*
 * Copyright (c) 2014 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 *
 * Contributors:
 *   Stephan D. Cote 
 *      - Initial concept and implementation
 */
package coyote.i13n;

import static org.junit.Assert.assertTrue;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;


/**
 * 
 */
public class CounterTest {

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
   * Test method for {@link coyote.i13n.Counter#increment()}.
   */
  @Test
  public void testIncrement() {
    String NAME = "testIncrement";
    long LIMIT = 10;

    Counter counter = new Counter( NAME );

    for ( int x = 0; x < LIMIT; x++ ) {
      counter.increment();
    }

    assertTrue( "Value is " + counter.getValue() + " and should be " + LIMIT, counter.getValue() == LIMIT );
    assertTrue( "MaxValue is " + counter.getMaxValue() + " and should be " + LIMIT, counter.getMaxValue() == LIMIT );
    assertTrue( "MinValue is " + counter.getMinValue() + " and should be 0", counter.getMinValue() == 0 );
    assertTrue( "UpdateCount is " + counter.getUpdateCount() + " and should be " + LIMIT, counter.getUpdateCount() == LIMIT );
  }




  @Test
  public void testConstructor() {
    String NAME = "test";
    Counter counter = new Counter( NAME );

    assertTrue( "Name is wrong", counter.getName().equals( NAME ) );
    assertTrue( "Value is wrong", counter.getValue() == 0 );
    assertTrue( "MaxValue is wrong", counter.getMaxValue() == 0 );
    assertTrue( "MunValue is wrong", counter.getMinValue() == 0 );
    assertTrue( "Units is wrong", counter.getUnits() == null );
    assertTrue( "UpdateCount is wrong", counter.getUpdateCount() == 0 );
  }




  /**
   * Test method for {@link coyote.i13n.Counter#reset()}.
   */
  @Test
  public void testReset() {
    String NAME = "testReset";
    long LIMIT = 10;

    Counter counter = new Counter( NAME );

    for ( int x = 0; x < LIMIT; x++ ) {
      counter.increment();
    }

    Counter delta = counter.reset();

    assertTrue( "Delta Name is " + delta.getName() + " and should be " + NAME, delta.getName().equals( NAME ) );
    assertTrue( "Delta Value is " + delta.getValue() + " and should be " + LIMIT, delta.getValue() == LIMIT );
    assertTrue( "Delta MaxValue is " + delta.getMaxValue() + " and should be " + LIMIT, delta.getMaxValue() == LIMIT );
    assertTrue( "Delta MinValue is " + delta.getMinValue() + " and should be 0", delta.getMinValue() == 0 );
    assertTrue( "Delta Units are " + delta.getUnits() + " and should be null", delta.getUnits() == null );
    assertTrue( "Delta UpdateCount is " + delta.getUpdateCount() + " and should be " + LIMIT, delta.getUpdateCount() == LIMIT );

    assertTrue( "Counter Name is " + counter.getName() + " and should be " + NAME, counter.getName().equals( NAME ) );
    assertTrue( "Counter Value is " + counter.getValue() + " and should be 0", counter.getValue() == 0 );
    assertTrue( "Counter MaxValue is " + counter.getMaxValue() + " and should be 0", counter.getMaxValue() == 0 );
    assertTrue( "Counter MinValue is " + counter.getMinValue() + " and should be 0", counter.getMinValue() == 0 );
    assertTrue( "Counter Units are " + counter.getUnits() + " and should be null", counter.getUnits() == null );
    assertTrue( "Counter UpdateCount is " + counter.getUpdateCount() + " and should be 0", counter.getUpdateCount() == 0 );

  }

}
