/*
 * $Id:$
 *
 * Copyright (C) 2003 Stephan D. Cote' - All rights reserved.
 */
package coyote.loader.thread;

/**
 * Class Stepper
 * 
 * @author Stephan D. Cote' - Enterprise Architecture
 * @version $Revision:$
 */
public class Stepper {
  private static Scheduler scheduler = new Scheduler();




  /**
   * Constructor Stepper
   */
  Stepper() {}




  /**
   * Method testMethod
   */
  public void testMethod() {
    // long startTime = System.currentTimeMillis() + 3000;
    long startTime = System.currentTimeMillis();

    ScheduledJob task0 = new ScheduledTest( "------------------->Repeater" );
    task0.setExecutionTime( startTime );
    task0.setRepeatable( true );
    task0.setExecutionInterval( 1000 );
    task0.setExecutionLimit( 3 );

    ScheduledJob task1 = new ScheduledTest( "===================>Repeater" );
    task1.setExecutionTime( startTime + 500 );
    task1.setRepeatable( true );
    task1.setExecutionInterval( 1000 );
    task1.setExecutionLimit( 3 );

    scheduler.schedule( task1 );
    scheduler.schedule( task0 );

    System.out.println( "Scheduler has " + scheduler.getJobCount() + " jobs scheduled" );

    // run the scheduler in a background thread
    Thread thread = scheduler.daemonize();

    System.out.println( "Scheduler is running in thread " + thread );

    try {
      Thread.sleep( 20000 );
    } catch ( Exception ex ) {}

    System.out.println( "Scheduler has " + scheduler.getJobCount() + " jobs scheduled" );

    scheduler.shutdown();
    Thread.yield();

    while ( scheduler.isActive() ) {
      try {
        Thread.sleep( 1000 );

        if ( scheduler.isActive() ) {
          System.out.println( "Scheduler is still active" );
        }
      } catch ( Exception ex ) {}
    }

    System.out.println( "Scheduler has shutdown" );
  }




  private void testScheduler() {

    long startTime = System.currentTimeMillis();

    ScheduledJob task0 = new ScheduledTest( "Task0" );
    task0.setExecutionTime( startTime );

    ScheduledJob task1 = new ScheduledTest( "Task1" );
    task1.setExecutionTime( startTime);

    ScheduledJob task2 = new ScheduledTest( "Task2" );
    task2.setExecutionTime( startTime );

    scheduler.schedule( task0 );
    scheduler.schedule( task1 );
    scheduler.schedule( task2 );
    
    ScheduledJob firstJob = scheduler.getNextJob();
    ScheduledJob secondJob = firstJob.getNextJob();
    ScheduledJob thirdJob = secondJob.getNextJob();
    ScheduledJob fourthJob = thirdJob.getNextJob();
    System.out.println(scheduler.dump());
    
    ScheduledJob nextJob = scheduler.getNextJob();
    ScheduledJob target = scheduler.remove( nextJob );
    // They should be the same
    
    System.out.println(scheduler.dump());
    scheduler.schedule( target );
    System.out.println(scheduler.dump());

    
  }
  
  
  /**
   * Method main
   * 
   * @param args
   */
  public static void main( String[] args ) {
    Stepper test = new Stepper();
    //test.testMethod();
    test.testScheduler();
  }




}