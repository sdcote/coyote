/*
 * Copyright (c) 2003 Stephan D. Cote' - All rights reserved.
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

import coyote.commons.ArrayUtil;
import coyote.commons.ExceptionUtil;
import coyote.loader.log.Log;


/**
 * Represents a batch of Runnable tasks to be performed
 */
public class ThreadBatch extends ThreadJob {

  /** the array of Runnable objects */
  Runnable tasks[];




  /**
   * Constructor
   */
  public ThreadBatch() {
    tasks = new Runnable[0];
  }




  /**
   * Over-rided the ThreadJob's doWork() method to run all the Runnable objects
   * we have.
   *
   * <p>If an exception is thrown during the running of a task, it is written
   * out as a warning to the logs</p>
   */
  public void doWork() {
    for ( int i = 0; i < tasks.length; i++ ) {
      try {
        // Run each task
        tasks[i].run();
      } catch ( Exception e ) {
        // Ready any caught exceptions for the loggers
        Log.warn( "ThreadBatch. Task '" + tasks[i].getClass().getName() + "' reported an exception: " + e.getClass().getName() + "-" + e.getMessage() + "\n" + ExceptionUtil.stackTrace( e ) );
      }
      finally {
        // Reset any inturrpted state before moving on to the next job
        Thread.interrupted();
      }
    }

    // We are done processing, so set shutdown to true
    shutdown = true;
  }




  /**
   * Add a Runnable object to our list of jobs
   *
   * @param runnable Object that we are to run next time around
   */
  public void addJob( Runnable runnable ) {
    tasks = (Runnable[])ArrayUtil.addElement( tasks, runnable );
  }




  /**
   * Remove a runnable object from our list of jobs
   *
   * @param runnable Object we are to remove
   */
  public void removeJob( Runnable runnable ) {
    tasks = (Runnable[])ArrayUtil.removeElement( tasks, runnable );
  }
}