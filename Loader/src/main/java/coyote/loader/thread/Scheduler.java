/*
 * Copyright (c) 2004 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.loader.thread;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import coyote.commons.ExceptionUtil;
import coyote.loader.log.Log;


/**
 * Run a task repeatedly pausing a specified amount of time before each
 * execution cycle.
 *
 * <p>This is used when it is desired to execute a thread at regular intervals
 * as in system clean-up, log file rotation, and the like.</p>
 *
 * <p>This class is often used with the coyote.loader.thread.ThreadBatch class. 
 * The ThreadBatch class is a ThreadJob class that holds an array of ThreadJobs 
 * and runs them each in the order in which they were added.</p>
 * 
 * <p>The best way to use this is to create an instance of it and call the 
 * {@link #daemonize()} method to start it running in the background.</p>
 */
public class Scheduler extends ThreadJob {
  /** Tag used in various class identifying locations */
  public static final String CLASS = "Scheduler";

  private ScheduledJob nextJob = null;
  private ScheduledJob lastJob = null;
  private final Object mutex = new Object();
  private ThreadPool threadpool = null;
  private long WAIT_TIME = 50;
  public static final long SCHED = Log.getCode("SCHEDULER");

  public static final long SECOND_INTERVAL = 1000l;
  public static final long MINUTE_INTERVAL = SECOND_INTERVAL * 60;
  public static final long HOUR_INTERVAL = MINUTE_INTERVAL * 60;
  public static final long DAY_INTERVAL = HOUR_INTERVAL * 24;




  /**
   * @return the number of jobs currently in the scheduler
   */
  public int getJobCount() {
    int count = 0;

    synchronized (mutex) {
      if (this.nextJob != null) {
        ScheduledJob job = nextJob;

        for (count = 0; job != null; count++) {
          if (job == job.getNextJob()) {
            break;
          }

          job = job.getNextJob();
        }
      }
    }

    return count;
  }




  /**
   * Method initialize
   */
  public void initialize() {
    if (threadpool == null) {
      threadpool = new ThreadPool(CLASS);
    }

    threadpool.start();

    setIdleWait(WAIT_TIME);
  }




  /**
   * Keep cycling through the sorted list of scheduled jobs and place the next
   * job in the threadpool when it's execution time has arrived.
   *
   * <p>If the time we execute the next job is farther in the future than our
   * idleWaitInterval, then just just exit the method. Otherwise go into a wait
   * state here until the execution time arrives.</p>
   *
   * <p>Whenever a new ScheduledJob is added, the scheduler should be
   * interrupted from any waiting or idling and a fresh check should be made of
   * the next job in the queue.</p>
   */
  public void doWork() {
    synchronized (mutex) {
      if (nextJob != null) {
        long executionTime = System.currentTimeMillis();
        long jobTime = nextJob.getExecutionTime();

        // Check to see if it is time to run this job. If it is really close, 
        // wait around
        long millis = jobTime - executionTime;
        //Log.append( SCHED, nextJob + " to run at " + new Date( jobTime ) + " (" + jobTime + ")  diff:" + millis );

        // if the time we have to wait is less than or equal to the time we 
        // wait between calls to the doWork() method, wait for the time to 
        // elapse
        if (millis <= WAIT_TIME) {
          // If it is in the future, wait around for it, otherwise run it
          while (jobTime > System.currentTimeMillis()) {
            try {
              mutex.wait(millis);
            } catch (Exception ex) {
              // Exit the routine, the next time we enter, we'll re-check the
              // job list and process the possibly new nextJob reference
              return;
            }
          }

          // If we got here, it is time (or past the time) to execute the next
          // ScheduledJob referenced by nextJob
          try {
            executionTime = System.currentTimeMillis();
            Log.append(SCHED, "Execution Time: " + executionTime + " (" + new Date(executionTime) + "):\r\n" + dump());

            // Remove the job from the list and only work with the job which was removed
            ScheduledJob target = remove(nextJob);

            if (Log.isLogging(SCHED)) {
              if (nextJob != null) {
                Log.append(SCHED, "Handling '" + target + "' now - next job '" + nextJob + "' to run at " + new Date(nextJob.getExecutionTime()) + "\r\n" + dump());
              } else {
                Log.append(SCHED, "Handling '" + target + "' now - there is no other job to run\r\n" + dump());
              }
            }

            Log.append(SCHED, target + " enabled=" + target.isEnabled() + " cancelled=" + target.isCancelled() + " limit=" + target.getExecutionLimit() + " count=" + target.getExecutionCount() + " repeat=" + target.isRepeatable());
            if (!target.isCancelled() && ((target.getExecutionLimit() < 1) || (target.getExecutionLimit() > 0) && (target.getExecutionCount() < target.getExecutionLimit()))) {

              // Only run jobs which are enabled, otherwise reschedule them if 
              // necessary
              if (target.isEnabled()) {
                Log.append(SCHED, "Running " + target + " in threadpool");

                // Run the Scheduled Job in the thread pool
                threadpool.handle((ThreadJob)target);

                // / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / /
                // We should check that the threadpool does not get too full...
                // ...but if it does, there is not a lot we can do about it.
                // / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / /

                // Increment the execution counter
                target.incrementExecutionCount();
              } else {
                Log.append(SCHED, "Did not run disabled job " + target + " in threadpool");
              }

              // If the ScheduledJob is set for repetition
              if (target.isRepeatable()) {
                Log.append(SCHED, "Repeating job " + target + " execution time = " + executionTime + ",  target interval = " + target.getExecutionInterval());

                // If we have no limit or have not exceeded our limit...
                if ((target.getExecutionLimit() == 0) || (target.getExecutionLimit() > 0) && (target.getExecutionCount() < target.getExecutionLimit())) {
                  // ...reschedule the job
                  target.setExecutionTime(target.getExecutionInterval() + System.currentTimeMillis());
                  Log.append(SCHED, "Set execution time to " + new Date(target.getExecutionTime()) + " execution time = " + executionTime + ",  target interval = " + target.getExecutionInterval());
                  schedule(target);
                  Log.append(SCHED, "Scheduled repeating job " + target + " (runs=" + target.getExecutionCount() + " interval=" + target.getExecutionInterval() + ") will run again at " + new Date(target.getExecutionTime()) + "\r\n" + dump());
                }
              } else {
                Log.append(SCHED, "Job " + target + " is not flagged to be repeated, removed from execution list");
              }
            }

          } catch (Exception ex) {
            Log.warn(ex.getClass().getName() + " thrown in scheduler loop\r\n" + ExceptionUtil.stackTrace(ex));
          }

          Log.append(SCHED, "Next job '" + nextJob + "' to run at " + nextJob.getExecutionTime() + " (" + new Date(nextJob.getExecutionTime()) + ")");

        } // 

        // It is not time to execute the the next job yet, so exit the method
        // and let the threadjob check to see if we should shutdown

      } // nextJob !null

    } // sync

  }




  /**
   * Method terminate
   */
  public void terminate() {
    Log.append(SCHED, getClass().getName() + " is terminating");

    // Stop the threadpool
    threadpool.stop();

    // Do our superclass termination
    super.terminate();
  }




  /**
   * Reschedule the given job
   *
   * @param job the job to be rescheduled
   */
  protected void reschedule(ScheduledJob job) {
    // Remove the job from the list
    ScheduledJob target = remove(job);

    if (target != null) {
      Log.append(SCHED, "Rescheduling job " + target + " on request - was to run at " + new Date(target.getExecutionTime()));
      target.setExecutionTime(System.currentTimeMillis() + target.getExecutionInterval());
      schedule(target);
      Log.append(SCHED, "Rescheduled job " + target + " - will now run at " + new Date(target.getExecutionTime()));
    }
  }




  /**
   * Run the given runnable task as soon as possible and only once.
   *
   * @param task The task to run right now.
   */
  public void schedule(Runnable task) {
    schedule(task, System.currentTimeMillis(), 0, 0, 0);
  }




  /**
   * Add the given job to the list of scheduled jobs
   *
   * @param task the task to schedule
   * @param starts when the job is to next run
   */
  public void schedule(Runnable task, long starts) {
    schedule(task, starts, 0, 0, 0);
  }




  /**
   * Add the given job to the list of scheduled jobs
   *
   * @param task the task to schedule
   * @param starts when the job is to next run
   */
  public void schedule(Runnable task, Date starts) {
    schedule(task, starts.getTime(), 0, 0, 0);
  }




  /**
   * Add the given job to the list of scheduled jobs
   *
   * @param task the task to schedule
   * @param starts when the job is to next run
   */
  public void schedule(Runnable task, Calendar starts) {
    schedule(task, starts.getTime().getTime(), 0, 0, 0);
  }




  /**
   * Add the given job to the list of scheduled jobs.
   * 
   * <p>This creates a ScheduledJob which wraps the runnable task and sets 
   * several key attributes controlling how that task is run. The returned 
   * {@link ScheduledJob} reference can then be used to control how the job is
   * managed by the scheduler.</p>
   *
   * @param task the task to schedule
   * @param starts when the job is to next run
   * @param interval number of milliseconds between runs
   * @param ends the expiration time
   * @param limit to maximum number of times the task is to run
   * 
   * @return a reference to the job placed in the scheduler
   */
  public ScheduledJob schedule(Runnable task, long starts, long interval, long ends, long limit) {
    ScheduledJob job = new ScheduledJob(task);
    job.setExecutionTime(starts);
    job.setExecutionInterval(interval);
    job.setExpirationTime(ends);
    job.setExecutionLimit(limit);

    if ((interval > 0) || (limit > 0)) {
      job.setRepeatable(true);
    }

    schedule(job);

    return job;
  }




  /**
   * Place the job in the job list.
   *
   * <p>This will place the given scheduled job into the job list sorted by
   * execution time. If the given jobs execution time matches another in the
   * list, it will be placed behind the job in the list with the matching time.
   * This results in jobs being executed in the order in which they were placed
   * in the job list if all the execution times match.</p>
   *
   * @param job The ScheduledJob to place in the scheduler's job list
   */
  public void schedule(ScheduledJob job) {
    if (job != null) {
      Log.append(SCHED, "Scheduling job " + job + " to run at " + new Date(job.getExecutionTime()));

      synchronized (mutex) {
        // Start at the beginning
        ScheduledJob current = nextJob;
        ScheduledJob previous = null;

        // Loop through all the job references and find where the job belongs
        while (current != null) {
          if (current.getExecutionTime() > job.getExecutionTime()) {
            break;
          }

          previous = current;
          current = current.getNextJob();
        }

        // link current and previous jobs to this job
        if (!job.equals(current) && !job.equals(previous)) {
          job.setPreviousJob(previous);
          job.setNextJob(current);

          if (current != null) {
            current.setPreviousJob(job);
          } else {
            lastJob = job;
          }

          if (previous != null) {
            previous.setNextJob(job);
          } else {
            nextJob = job;
          }
        } else {
          Log.append(SCHED, "Aaaakkk! Circular Job reference");
        }

        // Let everyone know there is a new Job in the scheduler
        mutex.notifyAll();
      }

      Log.append(SCHED, "Job scheduled in list of " + getJobCount() + " jobs; next job '" + nextJob + "' to run at " + new Date(nextJob.getExecutionTime()));

      // Make sure we have the threads to process it
      if (threadpool != null) {
        threadpool.checkLoad();
      }
    }
  }




  /**
   * Remove a particular instance of a {@link ScheduledJob} from the scheduler.
   *
   * @param job the job to remove
   *
   * @return the removed job
   */
  public ScheduledJob remove(ScheduledJob job) {
    if (job == null) {
      return null;
    }

    synchronized (mutex) {
      if (this.nextJob != null) {
        // start at the top of the queue of jobs
        ScheduledJob test = nextJob;

        while (test != null) {

          // if the current job matches the job for which er are looking
          if (job.equals(test)) {

            if (test.getPreviousJob() != null) {
              test.getPreviousJob().setNextJob(test.getNextJob());
            } else {
              nextJob = test.getNextJob();
            }

            if (test.getNextJob() != null) {
              test.getNextJob().setPreviousJob(test.getPreviousJob());
            } else {
              lastJob = test.getPreviousJob();
            }

            break; // We found a match, we can exit
          }

          test = test.getNextJob();
        }
      }
    }

    return job;
  }




  /**
   * @return the next job scheduled for execution
   */
  public ScheduledJob getNextJob() {
    return nextJob;
  }




  /**
   * @return a string representation of the state of the scheduler
   */
  public String dump() {
    SimpleDateFormat DATEFORMAT = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
    StringBuffer retval = new StringBuffer("--[ JobList ]------------------------------------------------\r\n");
    retval.append("Next: " + nextJob + " - " + (nextJob == null ? 0 : nextJob.getExecutionTime()) + "\r\n");
    retval.append("Last: " + lastJob + " - " + (lastJob == null ? 0 : lastJob.getExecutionTime()) + "\r\n");
    retval.append("- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -\r\n");

    synchronized (mutex) {
      long i = 0;

      if (this.nextJob != null) {
        ScheduledJob test = nextJob;

        while (test != null) {
          retval.append("Job#" + i + " " + test.getExecutionTime() + " (" + DATEFORMAT.format(new Date(test.getExecutionTime())) + ")  - " + test + "\r\n");

          test = test.getNextJob();

          i++;
        }
      }
    }

    retval.append("-------------------------------------------------------------");

    return retval.toString();
  }
}