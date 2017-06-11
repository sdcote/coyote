/*
 * $Id:$
 *
 * Copyright (C) 2003 Stephan D. Cote' - All rights reserved.
 */
package coyote.loader.thread;

/**
 * Class ThreadJobHarness
 * 
 * @author Stephan D. Cote' - Enterprise Architecture
 * @version $Revision:$
 */
public class ThreadJobHarness {

  /** Field pool */
  ThreadPool pool;




  /**
   * Constructor ThreadJobHarness
   */
  public ThreadJobHarness() {}




  /**
   * Method begin
   */
  public void begin() {
    TestJob subject = new TestJob( "SlowStarter" );
    subject.daemonize();
    System.out.println( "Waiting for job to go active" );
    System.out.flush();
    subject.waitForActive( 15000 );
    System.out.println( "Finished waiting for job to go active. Active = " + subject.isActive() );
    System.out.flush();
  }




  /**
   * Method main
   * 
   * @param args
   */
  public static void main( String[] args ) {
    ThreadJobHarness harness = new ThreadJobHarness();
    harness.begin();
  }
}