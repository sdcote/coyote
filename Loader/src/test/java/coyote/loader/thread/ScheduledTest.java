/*
 * Copyright (C) 2003 Stephan D. Cote' - All rights reserved.
 */
package coyote.loader.thread;

/**
 *
 */
public class ScheduledTest extends ScheduledJob {
  private int count = 0;




  /**
   * @return the count
   */
  public int getCount() {
    return count;
  }




  /**
   * @param text
   */
  public ScheduledTest( String text ) {
    // Always a good practice
    super();
    super.setName( text );
  }




  /**
   * Method initialize
   */
  public void initialize() {
    // Always initialize the super class first
    super.initialize();

    if ( name != null && name.startsWith( "SlowStarter" ) ) {
      try {
        System.out.println( "Slow Starting " + current_thread.getName() );
        Thread.sleep( 2500 );
      } catch ( Exception ex ) {}
    }
  }




  /**
   *
   */
  public void terminate() {
    super.terminate();
  }




  /**
   *
   */
  public void doWork() {
    count++;
    if ( getName() != null ) {
      System.out.println( getName() + "(" + getCount() + ")" );
    } else {
      System.out.println( "." );
    }

    try {
      sleep( 100 );
    } catch ( InterruptedException x ) {}

    shutdown();
  }

}