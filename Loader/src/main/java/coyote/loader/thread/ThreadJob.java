/*
 * Copyright (c) 2003 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.loader.thread;

/**
 * A class designed to be run in a ThreadPool of worker threads.
 *
 * <p>This class can be specialized to handle a variety of jobs, but is designed
 * to handle objects that implement the java.lang.runnable interface. In 
 * general, if the class may need to run in a multi-threaded environment, this 
 * class defines several utility methods to make life easier.</p>
 */
public class ThreadJob implements Runnable {

  /** Object we use to synchronize our operations */
  protected Object mutex = new Object();

  /** Indicates we have been asked to stop processing and shutdown */
  protected volatile boolean shutdown;

  /** Indicates that we should shutdown, reinitialize and start running again */
  protected volatile boolean restart = false;

  /** Indicates we have been asked to temporally stop processing */
  protected volatile boolean suspended;

  /** The flag which represents our idle status */
  protected volatile boolean idle = false;

  /** The time when we start idling due to inactivity */
  protected volatile long idle_time = 0;

  /** The time when we start idling due to inactivity */
  protected long idle_timeout = 30000;

  /** The how long we pause when idling due to inactivity */
  protected volatile long idle_wait_time = 1000;

  /** Indicates the Brady Bunch time we were started (in milliseconds) */
  protected volatile long started_time = 0l;

  /** Indicates we never should start idling */
  protected volatile boolean hyper = false;

  /** Reference to our current thread of execution */
  protected Thread current_thread = null;

  /** The optional runnable job we need to run */
  protected Runnable work = null;

  /** The object we use to synchronize our running flag operations */
  protected Object activeLock = new Object();

  /**
   * Flag indicating if we have entered and are currently active within the 
   * main run loop
   */
  protected volatile boolean active = false;

  /** Flag indicating that the doWork method should only be called once */
  protected volatile boolean doWorkOnce = false;

  /** Flag indicating park should use sleep() instead of wait() */
  protected volatile boolean parkSleep = false;




  /**
   *
   */
  public ThreadJob() {
    super();
  }




  /**
   *
   * @param job
   */
  public ThreadJob(final Runnable job) {
    work = job;
  }




  /**
   * Creates a thread, runs this job in that thread and exits leaving that
   * thread (and the JRE) running in memory.
   *
   * <p><strong>Note</strong> that this does not create a Java {@code daemon} 
   * thread as "daemon" threads will be terminated automatically when the JRE 
   * terminates. Only a {@code user} thread will keep the JRE running. The 
   * naming of the method is to characterize the "background process" nature of 
   * a daemon.</p>
   *
   * @return the thread in which this job is running.
   */
  public Thread daemonize() {
    return daemonize((String)null);
  }




  /**
   * Creates a thread, runs this job in that thread and exits leaving that
   * thread (and the JRE) running in memory with the given name.
   *
   * <p><strong>Note</strong> that this does not create a Java {@code daemon} 
   * thread as "daemon" threads will be terminated automatically when the JRE 
   * terminates. Only a {@code user} thread will keep the JRE running. The 
   * naming of the method is to characterize the "background process" nature of 
   * a daemon.</p>
   *
   * @param name The name to give the thread
   *
   * @return the thread in which this job is running.
   */
  public Thread daemonize(final String name) {
    final Thread newthread = new Thread(this);

    if ((name != null) && (name.length() > 0)) {
      newthread.setName(name);
    }

    return daemonize(newthread);
  }




  /**
   * Run the job in a background user thread.
   *
   * <p><strong>Note</strong> that this does not create a Java {@code daemon} 
   * thread as "daemon" threads will be terminated automatically when the JRE 
   * terminates. Only a {@code user} thread will keep the JRE running. The 
   * naming of the method is to characterize the "background process" nature of 
   * a daemon.</p>
   *
   * @param thread the thread in which to run this job.
   *
   * @return the thread in which this job is running.
   */
  public Thread daemonize(final Thread thread) {
    current_thread = thread;

    // only user threads keep the JVM running
    current_thread.setDaemon(false);

    // start it
    current_thread.start();

    // give the thread a chance to start
    Thread.yield();

    return current_thread;
  }




  /**
   * Wait for the ThreadJob to go active.
   *
   * <p>The main run loop will set the active flag to true when it enters the
   * main run loop AFTER the initialize() method has been called. The active
   * flag will not be set to false until the run loop has exited as the last
   * operation before leaving the run() method.</p>
   *
   * @param timeout The number of milliseconds to wait for the main run loop to
   *          be entered.
   */
  public void waitForActive(final long timeout) {
    if (!isActive()) {
      // determine the timeout sentinel value
      final long tout = System.currentTimeMillis() + timeout;

      // While we have not reached the sentinel time
      while (tout > System.currentTimeMillis()) {
        // wait on the active lock object
        synchronized (activeLock) {
          try {
            activeLock.wait(10);
          } catch (final Throwable t) {}
        }

        // if we are now active...
        if (isActive()) {
          // ... break out of the time-out while loop
          break;
        }

      } // while time-out not reached

    } // if not active

  }




  /**
   * Return whether or not the thread has entered and is currently within the
   * main run loop.
   *
   * @return True if the ThreadJob has been initialized and is cycling in the 
   *         run loop, False otherwise.
   */
  public boolean isActive() {
    synchronized (activeLock) {
      return active;
    }
  }




  /**
   * Return if this thread job has started processing yet.
   *
   * <p>This is useful when other threads are waiting for us to start before
   * they continue.</p>
   *
   * @return true if this instance has entered the run loop at least once, false
   *         if this instance has never started running.
   */
  public boolean hasStarted() {
    synchronized (mutex) {
      return (started_time > 0);
    }
  }




  /**
   * Called by the run() method when it enters and exits the main run loop.
   *
   * <p>When the thread enters the main run loop and is going to begin cycling
   * this method is called with true. It then notifies all threads that are
   * waiting for this object to be active via the waitForActive() method.</p>
   *
   * <p>This method is called again with false just prior to exiting the run()
   * method to set the active flag to false. No notifications are maid as the
   * join() method will notify all interested parties when this thread
   * exits.</p>
   *
   * @param flag The boolean value to set to the active flag.
   */
  protected void setActiveFlag(final boolean flag) {
    synchronized (activeLock) {
      active = flag;

      if (active) {
        activeLock.notifyAll();
      }
    }
  }




  /**
   * Request this object to shutdown.
   *
   * <p>This probably will not do what is expected. It will mark the ThreadJob
   * instance to shutdown, not the subclass instance of this ThreadJob class.
   * It is important to understand the scoping model, because one might think
   * that calling this method on all the references in a thread pool would
   * cause all the objects in that pool to have their shutdown flags set to
   * false. In fact, they do not!</p>
   *
   * <p>If you have a class called MyJob that extends ThreadJob, and you place
   * instances of MyJob in a ThreadPool and start them running, the only way
   * you can get MyJob to terminate gracefully is if you call shutdown() on the
   * reference to the MyJob object. Calling shutdown on the ThreadJob reference
   * never sets the shutdown flag in the MyJob reference!</p>
   *
   * <p>So be careful how you design your shutdown routines. Keep references to
   * objects that need to be shutdown and call their shutdown methods not their
   * super-class methods otherwise the sub-classes will not get the shudown
   * message.</p>
   */
  public void shutdown() {
    synchronized (mutex) {
      shutdown = true;

      if (current_thread != null) {
        current_thread.interrupt();

        // Make sure suspended threads are resumed so they can shutdown
        mutex.notifyAll();
      }
    }
  }




  /**
   * @return whether or not the current thread is set to shutdown.
   */
  public boolean isShutdown() {
    synchronized (mutex) {
      return shutdown;
    }
  }




  /**
   * Request this object to restart.
   *
   * <p>For the UNIX types, this is just like the HUP signal and causes, the
   * thread of execution to perform a normal shutdown, and re-initialize.</p>
   */
  public void restart() {
    synchronized (mutex) {
      restart = true;
      shutdown = true;

      if (current_thread != null) {
        current_thread.interrupt();

        // Make sure suspended threads are resumed so they can shutdown
        mutex.notifyAll();
      }
    }
  }




  /**
   * @return whether or not the current thread is set to restart.
   */
  public boolean isRestart() {
    synchronized (mutex) {
      return restart;
    }
  }




  /**
   * Suspend this object's thread of execution.
   */
  public void suspend() {
    synchronized (mutex) {
      suspended = true;
    }
  }




  /**
   * @return whether or not the current thread is in a suspended state.
   */
  public boolean isSuspended() {
    synchronized (mutex) {
      return suspended;
    }
  }




  /**
   * This resumes this object's thread of execution.
   */
  public void resume() {
    synchronized (mutex) {
      suspended = false;

      mutex.notifyAll();
    }
  }




  /**
   * Set this thread to idle.
   */
  public void idle() {
    synchronized (mutex) {
      idle = true;
    }
  }




  /**
   * @return whether or not the current thread is in an idle state.
   */
  public boolean isIdle() {
    synchronized (mutex) {
      return idle;
    }
  }




  /**
   * This "parks" the execution of this thread.
   *
   * <p>When this object's {@code suspend()} method is called, the code
   * will proceed to a place where it can stop processing. It will then call
   * this method to wait until the {@code resume()} method is called or 
   * the given number of milliseconds expire.</p>
   * 
   * <p>This method has two styles of parking. By default it will perform a 
   * {@code wait()} operation on this jobs mutex. This is the least CPU
   * taxing method. The second style of parking involves using the 
   * {@code sleep()} and actually is less efficient than 
   * {@code wait()}. The {@code sleep()} style is used in those JVMs
   * that have synchronization issues and tend to consume user-level CPU while
   * parked.</p>
   * 
   * <p>The style of sleeping is controlled by setting the park-sleep flag to 
   * true via the {@code setParkSleep(boolean)} method. The default is to 
   * perform the more efficient {@code wait()} style.</p>
   *  
   * @see #setParkSleep(boolean)
   * 
   * @param timeout The number of milliseconds to wait.
   */
  protected void park(final long timeout) {
    synchronized (mutex) {
      if (isShutdown()) {
        // Cannot suspend if shutdown is pending
        if (isSuspended()) {
          resume();
        }
      } else {
        if (current_thread != null) {
          try {
            if (parkSleep) {
              if (timeout == 0) {
                Thread.sleep(Long.MAX_VALUE);
              } else {
                Thread.sleep(timeout);
              }
            } else {
              mutex.wait(timeout);
            }
          } catch (final InterruptedException x) {
            current_thread.interrupt(); // re-throw
          }
        } else {}
      }
    }
  }




  /**
   * Parks the thread for the preset idle_wait_time interval.
   */
  public void park() {
    park(idle_wait_time);
  }




  /**
   * Sets the current thread to never idle, or run at full speed constantly.
   *
   * <p>If set to false, the thread will pause every iteration through the main
   * {@code run()} loop and pause for a predetermined amount of time when
   * the code determines it no longer needs to process at full speed.</p>
   *
   * @param on the value of the flag to set.
   */
  public void setHyper(final boolean on) {
    synchronized (mutex) {
      hyper = on;
    }
  }




  /**
   * @return whether or not the current thread is hyper (never idles).
   */
  public boolean isHyper() {
    synchronized (mutex) {
      return hyper;
    }
  }




  /**
   * Set all our flags and get ready to run.
   */
  private void init() {
    // Set all our flags
    restart = false;
    shutdown = false;
    suspended = false;
    idle = false;
    hyper = false;

    // Set the time we started
    started_time = System.currentTimeMillis();

    // Set the thread to time to go inactive at deactivate_time
    idle_time = started_time + idle_timeout;
  }




  /**
   * Perform any sub-class initialization.
   * 
   * <p>This method is called just before the main run loop is entered. It is 
   * called only once unless the job is restarted as this method is inside the 
   * restart loop. This implies this method is called everytime the job is 
   * restarted.</p>
   */
  public void initialize() {}




  /**
   * Final resource clean-up before exiting or restarting.
   *
   * <p>Normally this routine is called as the main run() loop exits. This
   * routine is also called when a restart() is requested. So design your
   * termination procedure with care.</p>
   */
  public void terminate() {}




  /**
   * Sleep for a while.
   *
   * @param millis
   *
   * @throws InterruptedException
   */
  public void sleep(final long millis) throws InterruptedException {
    Thread.sleep(millis);
  }




  /**
   * Sleep for a while.
   *
   * @param millis
   * @param nanos
   *
   * @throws InterruptedException
   */
  public void sleep(final long millis, final int nanos) throws InterruptedException {
    Thread.sleep(millis, nanos);
  }




  /**
   * Return the reference to the current thread.
   *
   * @return The reference to the current thread running this worker.
   */
  public Thread getThread() {
    return current_thread;
  }




  /**
   * Get the number of milliseconds of inactivity before starting to idle.
   *
   * @return the number of milliseconds.
   */
  public long getIdleTimeout() {
    return idle_timeout;
  }




  /**
   * Set the number of milliseconds of inactivity before starting to idle.
   *
   * @param millis the number of milliseconds.
   */
  public void setIdleTimeout(final long millis) {
    idle_timeout = millis;
  }




  /**
   * Get the number of milliseconds of we wait when idling.
   *
   * @return the number of milliseconds.
   */
  public long getIdleWait() {
    return idle_wait_time;
  }




  /**
   * Set the number of milliseconds we wait when idling.
   *
   * @param millis the number of milliseconds.
   */
  public void setIdleWait(final long millis) {
    idle_wait_time = millis;
  }




  /**
   * Set whether or not we are in an idle state.
   * 
   * <p>When idling, the thread pauses a short time between calls to the 
   * doWork() method. See ({@link #setIdleWait(long)} for setting the wait
   * interval.
   *
   * @param flag indicating our idle state.
   */
  public void setIdle(final boolean flag) {
    this.idle = flag;
  }




  /**
   * Run this thread job in your current thread of execution.
   *
   * <p>While in this loop, your thread will drive all processing of this job
   * and will not return until the shutdown method has been called.</p>
   */
  public void run() {
    current_thread = Thread.currentThread();

    do {
      // perform our own initialization
      init();

      // Setup everything we need to run
      initialize();

      // set the active flag true only after we have initialized, so others can
      // use waitForActive to wait for us to initialize
      setActiveFlag(true);

      while (!isShutdown()) {
        // Do whatever the sub-class wants
        doWork();

        // Yield to other threads
        Thread.yield();

        // If we are only supposed to perform the doWork method once...quit;
        if (doWorkOnce) {
          restart = false;

          break;
        }

        // If we are in an idle state, park for a while to save CPU resources
        if (isIdle()) {
          park(idle_wait_time);
        } else {
          // if not, check to see if it is time to begin idling
          if (!isHyper() && (System.currentTimeMillis() > idle_time)) {
            idle = true;
          }
        }

        // Check to see if we should suspend execution
        if (isSuspended()) {
          // park for an indefinite period of time (resume will interrupt park)
          park(0);
        }

      } // if !shutdown

      // Clean up after ourselves, although we may restart
      terminate();
    }
    while (restart);

    // We are no longer running active
    setActiveFlag(false);

  }




  /**
   * Waits for this thread to die.
   *
   * <p>This is a convenience wrapper around the Thread.join() method that
   * catches and consumes any exception that may be thrown within the join. If
   * this is not what is desired, a proper join should be performed on the
   * actual thread that is returned by the getThread() method call.</p>
   *
   * @see #getThread
   */
  public void join() {
    if (current_thread != null) {
      try {
        current_thread.join(0);
      } catch (final Exception e) {}
    }
  }




  /**
   * Waits for this thread to die for the specified number of milliseconds.
   *
   * <p>If the thread does not die within the given number of milliseconds, the
   * method quietly returns.</p>
   *
   * <p>This is a convenience wrapper around the Thread.join() method that
   * catches and consumes any exception that may be thrown within the join. If
   * this is not what is desired, a proper join should be performed on the
   * actual thread that is returned by the getThread() method call.</p>
   *
   * @see #getThread
   *
   * @param millis
   */
  public void join(final long millis) {
    if (current_thread != null) {
      try {
        current_thread.join(millis);
      } catch (final Exception e) {}
    }
  }




  /**
   * Stops us from idling for another idle-timeout milliseconds.
   * 
   * <p>There is some execution latency for the thread to transition into and 
   * out of an idle state, it is some times desirable to keep the thread active 
   * if the logic expects to perform more work that should not be inturrupted. 
   * This is like saying "don't start idling; we may have more work to do 
   * shortly".</p>
   */
  public void rev() {
    // Stop us from idling
    idle = false;

    // Set the thread to time to go start idling again at idle_time
    idle_time = System.currentTimeMillis() + idle_timeout;
  }




  /**
   * This is the business logic method of the ThreadJob.
   *
   * <p>It is expected that this method will be over-ridden by the derived class
   * to perform the actual work.</p>
   *
   * <p>The over-riding method MUST take care to respond to the
   * InterruptedException as it is used to signal shutdowns, suspends and idling
   * operations.</p>
   *
   * <p>The logic in this method allows for a Runnable object to be referenced
   * via the &quot;work&quot; attribute. When this method is called, the object
   * is run and this thread job is shutdown.</p>
   */
  public void doWork() {
    if (work == null) {
      try {
        Thread.sleep(50);
      } catch (final InterruptedException x) {}
    } else {
      work.run();
      shutdown();
    }
  }




  /**
   * Instruct the instance whether to execute the doWork method only once per
   * run or to continue looping until the shutdown method is called.
   *
   * @param flag Set to true to execute the doWork method once then exit, or 
   *        false, keep looping until shutdown is called.
   */
  public void setDoWorkOnce(final boolean flag) {
    doWorkOnce = flag;
  }




  /**
   * Returns the the parking strategy of the thread being used.
   *
   * @return True if the park method uses sleep() instead of wait().
   */
  public boolean isParkSleep() {
    return parkSleep;
  }




  /**
   * Sets the the parking strategy of the thread.
   *
   * <p>In most VMs, the most efficient way to suspend execution is to use the
   * wait() method. Some VMs use less CPU cycles if the sleep() method is used.
   * This method allows the caller to set the parking strategy when the thread
   * needs to idle and reduce the amount of processing it is consuming.</p>
   *
   * @param b True informs the thread to use the sleep() method call, false
   *          indicates use wait().
   */
  public void setParkSleep(final boolean b) {
    parkSleep = b;
  }

}