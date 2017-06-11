/*
 * Copyright (c) 2015 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 *
 * Contributors:
 *   Stephan D. Cote 
 *      - Initial concept and implementation
 */
package coyote.loader.thread;

//import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;


/**
 * 
 */
public class SchedulerTest {

  private static Scheduler scheduler = null;




  /**
   * @throws java.lang.Exception
   */
  @Before
  public void setUp() throws Exception {
    scheduler = new Scheduler();
  }




  /**
   * @throws java.lang.Exception
   */
  @After
  public void tearDown() throws Exception {
    scheduler.shutdown();
  }




  /**
   * The scheduler should place all tasks with the same time in the queue in 
   * the order they were inserted. 
   */
  @Test
  public void testScheduler() {
    long startTime = System.currentTimeMillis();

    // create three tasks with the same execution time
    ScheduledJob task0 = new ScheduledTest( "Task0" );
    task0.setExecutionTime( startTime );

    ScheduledJob task1 = new ScheduledTest( "Task1" );
    task1.setExecutionTime( startTime );

    ScheduledJob task2 = new ScheduledTest( "Task2" );
    task2.setExecutionTime( startTime );

    // place them in the scheduler
    scheduler.schedule( task0 );
    scheduler.schedule( task1 );
    scheduler.schedule( task2 );

    // retrieve them in their order to ensure they were inserted properly
    ScheduledJob firstJob = scheduler.getNextJob();
    assertEquals( task0, firstJob );

    ScheduledJob secondJob = firstJob.getNextJob();
    assertEquals( task1, secondJob );

    ScheduledJob thirdJob = secondJob.getNextJob();
    assertEquals( task2, thirdJob );

    ScheduledJob fourthJob = thirdJob.getNextJob();
    assertNull( fourthJob );

    //System.out.println( scheduler.dump() );

    // mimic the action of the doWork()
    ScheduledJob nextJob = scheduler.getNextJob();
    ScheduledJob target = scheduler.remove( nextJob );
    assertEquals( 2, scheduler.getJobCount() );
    assertEquals( nextJob, target );
    assertEquals( task0, target ); // should've been the first

    // place it back in the queue, it should go to the bottom (last)
    scheduler.schedule( target );
    assertEquals( 3, scheduler.getJobCount() );

    // Check the order of the job queue
    firstJob = scheduler.getNextJob();
    assertEquals( task1, firstJob );
    secondJob = firstJob.getNextJob();
    assertEquals( task2, secondJob );
    thirdJob = secondJob.getNextJob();
    assertEquals( task0, thirdJob );
    fourthJob = thirdJob.getNextJob();
    assertNull( fourthJob );

    // pull task1 off the top of the job queue    
    nextJob = scheduler.getNextJob();
    target = scheduler.remove( nextJob );
    assertEquals( 2, scheduler.getJobCount() );
    assertEquals( nextJob, target );
    assertEquals( target, task1 );

    // set the execution time less than the rest
    target.setExecutionTime( startTime - 1 );
    // place it back in the queue
    scheduler.schedule( target );

    // The next job should still be task1, the one with the smallest time
    nextJob = scheduler.getNextJob();
    assertEquals( nextJob, task1 );

  }




  //@Test
  public void testOne() {
    long startTime = System.currentTimeMillis() + 3000;
    // long startTime = S;

    ScheduledJob task0 = new ScheduledTest( "Hello0" );
    task0.setExecutionTime( startTime );

    ScheduledJob task1 = new ScheduledTest( "Hello1" );
    task1.setExecutionTime( startTime + 1000 );

    ScheduledJob task2 = new ScheduledTest( "Hello2" );
    task2.setExecutionTime( startTime + 2000 );

    ScheduledJob task3 = new ScheduledTest( "Hello3" );
    task3.setExecutionTime( startTime + 3000 );

    ScheduledJob task4 = new ScheduledTest( "Hello4" );
    task4.setExecutionTime( startTime + 4000 );

    // Place them in a different order than should be executed
    scheduler.schedule( task3 );
    scheduler.schedule( task4 );
    scheduler.schedule( task1 );
    scheduler.schedule( task2 );
    scheduler.schedule( task0 );

    scheduler.daemonize();

    try {
      Thread.sleep( 2500 );
      assertTrue( scheduler.getJobCount() == 5 );
    } catch ( Exception ex ) {}

    try {
      Thread.sleep( 1000 );
      assertTrue( scheduler.getJobCount() == 4 );
    } catch ( Exception ex ) {}

    try {
      Thread.sleep( 5000 );
      assertTrue( scheduler.getJobCount() == 0 );
    } catch ( Exception ex ) {}

  }




  //@Test
  public void testRepeat1() {
    // long startTime = System.currentTimeMillis() + 3000;
    long startTime = System.currentTimeMillis();

    ScheduledJob task0 = new ScheduledTest( "Repeater" );
    task0.setExecutionTime( startTime );
    task0.setRepeatable( true );
    task0.setExecutionInterval( 1000 );
    task0.setExecutionLimit( 3 );

    scheduler.schedule( task0 );
    System.out.println( "Scheduler has " + scheduler.getJobCount() + " jobs scheduled" );

    scheduler.daemonize();

    try {
      Thread.sleep( 3500 );
    } catch ( Exception ex ) {}

    System.out.println( "Scheduler has " + scheduler.getJobCount() + " jobs scheduled" );

    assertTrue( task0.getExecutionCount() == 3 );
    assertTrue( scheduler.getJobCount() == 0 );
  }

}
