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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Random;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;


/**
 * 
 */
public class TimingMasterTests {

  public static int binarySearch( final long[] array, final long target ) {
    //assert array.length > 0;
    int indx = 0;
    int size = array.length - 1;
    while ( indx <= size ) {
      final int i = ( indx + size ) >> 1;
      final long value = array[i];
      if ( value < target ) {
        indx = i + 1;
      } else if ( value > target ) {
        size = i - 1;
      } else {
        return i;
      }
    }
    return -( indx + 1 );
  }




  public static void doWork() {
    // setup an array to sort, simulating some real work. This should be added 
    // to our overhead calculation
    final int ARRAY_LENGTH = 500000;
    final Random generator = new Random();
    final long[] array = new long[ARRAY_LENGTH];
    for ( int i = 0; i < array.length; array[i++] = generator.nextLong() );

    final long target = array[0];

    // sort the array
    Arrays.sort( array );

    binarySearch( array, target );
  }




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
   * Test method for {@link coyote.i13n.TimingMaster#getName()}.
   */
  @Test
  public void testGetName() {
    TimingMaster subject = new TimingMaster( "Bob" );
    assertNotNull( "Name should not be null", subject.getName() );
    assertTrue( "Timer name did not match", "Bob".equals( subject.getName() ) );
  }




  /**
   * Test method for {@link coyote.i13n.TimingMaster#isEnabled()}.
   */
  @Test
  public void testIsEnabled() {
    TimingMaster subject = new TimingMaster( "testIsEnabled" );
    assertTrue( "Timer should be enabled by default", subject.isEnabled() );
  }




  @Test
  public void testSetEnabled() {
    TimingMaster subject = new TimingMaster( "testSetEnabled" );

    Timer monitor = subject.createTimer();
    assertNotNull( "TimingMaster did not return a monitor", monitor );

    subject.setEnabled( false );
    assertFalse( "Timer should be disabled now", subject.isEnabled() );

    monitor = subject.createTimer();
    assertNotNull( "TimingMaster did not return a monitor when disabled", monitor );

    assertTrue( "Returned monitor was not a NullTimer type", ( monitor instanceof NullTimer ) );
  }




  @Test
  public void testCreateTimer() {
    TimingMaster subject = new TimingMaster( "testCreateTimer" );
    Timer monitor = subject.createTimer();
    assertNotNull( "TimingMaster did not return a monitor", monitor );
  }




  @Test
  public void testResetThis() {
    TimingMaster subject = new TimingMaster( "testResetThis" );
    subject.increase( 5 );
    subject.resetThis();
    assertTrue( "Accrued value was not reset", subject.accrued == 0 );
  }




  @Test
  public void testIncrease() {
    TimingMaster subject = new TimingMaster( "testIncrease" );
    subject.increase( 5 );
    assertTrue( "Accrued value was not incremented", subject.accrued == 5 );
  }




  /**
   */
  @Test
  public void testGetCurrentActive() {
    TimingMaster subject = new TimingMaster( "testGetCurrentActive" );
    assertTrue( "CurrentActive did not start out at zero", subject.getCurrentActive() == 0 );
    Timer m1 = subject.createTimer();
    assertTrue( "CurrentActive did not remain at zero after creating monitor", subject.getCurrentActive() == 0 );
    Timer m2 = subject.createTimer();
    assertTrue( "CurrentActive did not remain at zero after creating monitor", subject.getCurrentActive() == 0 );
    Timer m3 = subject.createTimer();
    assertTrue( "CurrentActive did not remain at zero after creating monitor", subject.getCurrentActive() == 0 );

    m1.start();
    assertTrue( "CurrentActive did not increment to 1 after starting monitor", subject.getCurrentActive() == 1 );
    m2.start();
    assertTrue( "CurrentActive did not increment to 2 after starting monitor", subject.getCurrentActive() == 2 );
    m3.start();
    assertTrue( "CurrentActive did not increment to 3 after starting monitor", subject.getCurrentActive() == 3 );

    m1.stop();
    assertTrue( "CurrentActive did not decrement to 2 after stopping monitor 1", subject.getCurrentActive() == 2 );
    m2.stop();
    assertTrue( "CurrentActive did not decrement to 1 after stopping monitor 2", subject.getCurrentActive() == 1 );
    m3.stop();
    assertTrue( "CurrentActive did not decrement to 0 after stopping monitor 3", subject.getCurrentActive() == 0 );

  }




  @Test
  public void simpleTest() {
    TimingMaster subject = new TimingMaster( "simpleTest" );
    System.out.println( subject );
    Timer t1 = null;
    for ( int i = 0; i < 10; i++ ) {
      t1 = subject.createTimer();
      t1.start();
      doWork();
      t1.stop();
    }
    System.out.println( subject );

  }




  /**
   */
  //@Test poorly designed test, no way to quantify global activity especially in multi-threaded environments
  public void testGetGloballyActive() {
    TimingMaster subject = new TimingMaster( "testGetGloballyActive" );
    TimingMaster otherMaster = new TimingMaster( "OtherMasterTimer" );
    assertTrue( "GloballyActive started out at " + subject.getGloballyActive() + " not zero", subject.getGloballyActive() == 0 );

    Timer m1 = subject.createTimer();
    assertTrue( "GloballyActive did not remain at zero after creating monitor", subject.getGloballyActive() == 0 );
    Timer m2 = otherMaster.createTimer();
    assertTrue( "GloballyActive did not remain at zero after creating monitor", subject.getGloballyActive() == 0 );

    m1.start();
    assertTrue( "GloballyActive did not increment to 1 after starting a monitor", subject.getGloballyActive() == 1 );
    m2.start();
    assertTrue( "GloballyActive did not increment to 2 after starting a monitor", subject.getGloballyActive() == 2 );

    m1.stop();
    assertTrue( "GloballyActive did not decrement to 1 after stopping monitor 1", subject.getGloballyActive() == 1 );
    m2.stop();
    assertTrue( "GloballyActive did not decrement to 0 after stopping monitor 2", subject.getGloballyActive() == 0 );

  }

}
