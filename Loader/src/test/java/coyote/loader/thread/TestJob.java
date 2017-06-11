/*
 * $Id:$
 *
 * Copyright (C) 2003 Stephan D. Cote' - All rights reserved.
 */
package coyote.loader.thread;

/**
 * Class TestJob
 */
public class TestJob extends ThreadJob {
  private String display = ".";
  private int count = 0;




  /**
   * @param text
   */
  public TestJob( String text ) {
    if ( text != null ) {
      display = text;
    }
  }




  /**
   * Setup anything we want prior to doing our work
   */
  public void initialize() {
    // Always!
    super.initialize();

    if ( display.startsWith( "SlowStarter" ) ) {
      try {
        System.out.println( "Slow Starting " + current_thread.getName() );
        Thread.sleep( 2500 );
      } catch ( Exception ex ) {}
    }
  }




  /**
   * This is where we do our work.
   * 
   *  This method will be called continually until shutdown.
   */
  public void doWork() {
    // System.out.print(display);
    try {
      sleep( 100 );
    } catch ( InterruptedException x ) {}

    if ( count++ > 50 ) {
      shutdown();
    }
  }
  
  
  /**
   * Clean up our resources allocated during initialization and operation
   */
  public void terminate() {
    
    // Always do our termination logic before calling super class termination
    
    super.terminate();
    
    System.out.println( display + " Ran for " + ( System.currentTimeMillis() - started_time ) + " milliseconds" );
  }





}