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
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;

import coyote.loader.log.Log;


/**
 * 
 */
public class ThreadPoolTest {

  /** Field pool */
  ThreadPool pool = new ThreadPool( "TestPool" );




  /**
   * @throws java.lang.Exception
   */
  @Before
  public void setUp() throws Exception {
    pool.setJobWaitTime( 5000 );
    pool.start();
  }




  /**
   * @throws java.lang.Exception
   */
  @After
  public void tearDown() throws Exception {
    pool.stop();
  }




  //@Test
  public void testEightJobs() {
    try {
      pool.handle( new TestJob( "1" ) );
      pool.handle( new TestJob( "2" ) );
      pool.handle( new TestJob( "3" ) );
      pool.handle( new TestJob( "4" ) );
      pool.handle( new TestJob( "5" ) );
      pool.handle( new TestJob( "6" ) );
      pool.handle( new TestJob( "7" ) );
      pool.handle( new TestJob( "8" ) );

      try {
        Thread.sleep( 20000 );
      } catch ( Exception e ) {}
    } catch ( InterruptedException ie ) {
      fail( "Could not run job" );
    }
  }




  //@Test
  public void testPauser() {
    TestJob pauser = new TestJob( "P" );

    try {
      pool.handle( pauser );
    } catch ( InterruptedException ie ) {
      System.err.println( "Could not run pauser job" );
    }

    try {
      Thread.sleep( 1000 );
    } catch ( Exception e ) {}

    pauser.suspend();

    try {
      Thread.sleep( 2000 );
    } catch ( Exception e ) {}

    pauser.resume();

    try {
      Thread.sleep( 5000 );
    } catch ( Exception e ) {}
  }




  //@Test
  public void testTooMany() {
    System.out.println( "------------------------------------" );

    try {
      Thread.sleep( 2000 );
    } catch ( Exception e ) {}

    System.out.println( "------------------------------------" );
    pool.setMaxThreadCount( 3 );
    pool.setMinThreadCount( 2 );

    try {
      pool.handle( new TestJob( "A" ) );
      pool.handle( new TestJob( "B" ) );
      pool.handle( new TestJob( "C" ) );
      pool.handle( new TestJob( "D" ) );
      pool.handle( new TestJob( "E" ) );
      pool.handle( new TestJob( "F" ) );
      pool.handle( new TestJob( "G" ) );
      pool.handle( new TestJob( "H" ) );

      try {
        Thread.sleep( 20000 );
      } catch ( Exception e ) {}
    } catch ( InterruptedException ie ) {
      fail( "Could not run job" );
    }
  }




  //@Test
  public void testJobIdle() {
    pool.setMaxThreadCount( 3 );
    pool.setMinThreadCount( 0 );
    pool.setIdleTimeout( 3000L );
    Log.startLogging( Log.DEBUG );
    Log.debug( "Starting idle test" );
    Log.debug( "Idle Timeout is set to " + pool.getIdleTimeout() );

    try {
      pool.handle( new TestJob( "A" ) );
      pool.handle( new TestJob( "B" ) );
      pool.handle( new TestJob( "C" ) );

      try {
        Thread.sleep( 20000 );
      } catch ( Exception e ) {}
    } catch ( InterruptedException ie ) {
      fail( "Could not run job" );
    }

    Log.stopLogging( Log.DEBUG );
  }




  //@Test
  public void testWaitFor() {
    pool.setMaxThreadCount( 3 );
    pool.setMinThreadCount( 0 );

    try {
      TestJob subject = new TestJob( "SlowStarterC" );
      pool.handle( new TestJob( "SlowStarterA" ) );
      pool.handle( new TestJob( "SlowStarterB" ) );
      pool.handle( subject );

      subject.waitForActive( 5000 );

      try {
        Thread.sleep( 20000 );
      } catch ( Exception e ) {}
    } catch ( InterruptedException ie ) {
      fail( "Could not run job" );
    }

    Log.stopLogging( Log.DEBUG );
  }

}
