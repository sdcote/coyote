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

//import static org.junit.Assert.*;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;


/**
 * 
 */
public class TimerDemo {

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
   * Test method for {@link coyote.i13n.TimerBase#getName()}.
   */
  @Test
  public void simpleDemo() {

    // Use a scorecard to get a timer
    StatBoard scorecard = new StatBoardImpl();

    // timing instrumentation is disabled by default so we have to explicitly 
    // enable it
    scorecard.enableTiming( true );

    // Call the start timer method on the scorecard to start a timer with a 
    // correlating name
    Timer t1 = scorecard.startTimer( "Demo" );

    // Different named timers roll-up statistics separately
    Timer t2 = scorecard.startTimer( "Test" );

    // Stopping a timer totals the number of milliseconds between the start and 
    // stop calls
    t1.stop();

    // Timers can be re-started and stopped as necessary to accrue total time
    // this is helpful when trying to measure only the time spent in methods and
    // not waiting for calls to external systems.
    t1.start();
    t1.stop();
    // ... making a call to an external system not to be included in our time
    t1.start();
    t1.stop(); // finally completed our processing

    // Measure total time spent in this method
    t2.stop(); // Test timer is stopped only once

    System.out.println( t1 );
    System.out.println( t2 );

    t1.getName();

    System.out.flush();

    try {
      Thread.sleep( 1000 );
    } catch ( InterruptedException e ) {}

  }

}
