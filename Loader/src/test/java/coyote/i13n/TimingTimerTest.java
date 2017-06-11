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
public class TimingTimerTest {

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
   * Test method for {@link coyote.i13n.TimingTimer#start()}.
   */
  @Test
  public void testGetAccrued() {
    Timer monitor = new TimingTimer();

    for ( int x = 0; x < 10; x++ ) {
      try {
        monitor.start();
        Thread.sleep( 10 );
        monitor.stop();
        Thread.sleep( 500 );
        System.out.println( monitor.getAccrued() );
      } catch ( InterruptedException e ) {
        e.printStackTrace();
      }
    }
    assertTrue( "Too much time accrued", ( monitor.getAccrued() < 500 ) );

    System.out.println( monitor.getAccrued() );

  }

}