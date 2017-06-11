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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;


/**
 * 
 */
public class DefaultStatBoardTests {

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
  public void test() {

    StatBoard scorecard = new StatBoardImpl();

    // All scorecards should have an identifier
    assertNotNull( scorecard.getId() );

    Timer mon = scorecard.startTimer( "TimerDemo" );
    assertNotNull( mon );

    // timing is disabled by default so we should get a null timer
    assertTrue( mon instanceof NullTimer );

    // enable all timers
    scorecard.enableTiming( true );

    mon = scorecard.startTimer( "TimerDemo" );
    assertNotNull( mon );

    assertTrue( mon instanceof TimingTimer );

    // timer should be running
    assertTrue( mon.isRunning() );

    // disable all timers
    scorecard.enableTiming( false );

    mon = scorecard.startTimer( "TimerDemo" );
    assertNotNull( mon );

    // timing is disabled so we should get a null timer
    assertTrue( mon instanceof NullTimer );

    // we need to enable timing since enable/disable at the scorecard level has 
    // precedence over individual timer control
    scorecard.enableTiming( true );

    scorecard.disableTimer( "Bob" );
    Timer bobsTimer = scorecard.startTimer( "Bob" );
    mon = scorecard.startTimer( "TimerDemo" );

    // Bob's time should be a NullTimer since his tag is disabled
    assertTrue( bobsTimer instanceof NullTimer );
    // our other timer should be a TimingTimer as that tag is not disabled
    assertTrue( mon instanceof TimingTimer );

    // Let's enable timers with the tag of "Bob"
    scorecard.enableTimer( "Bob" );
    bobsTimer = scorecard.startTimer( "Bob" );
    assertTrue( bobsTimer instanceof TimingTimer );

  }

}
