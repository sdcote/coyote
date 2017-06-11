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

/**
 * Timers are devices for measuring the time something takes and the Timer
 * interface models a contract for all timers.
 *
 * <p>Timers measure the time between phases of execution. Once a Timer is
 * started, its stores the time and waits for a call to stop(). Once stopped,
 * it calculates the total elapsed time for that run.
 *
 * <p>Later a Timer (actually the entire set) can be rolled-up to provide the
 * number of invocations, mean, long and short elapsed intervals and several
 * other phase-oriented metrics in a manner similar to those of counters and
 * states.
 *
 * <p>The fixture tracks a Timer by its name where each name represents a
 * Master Timer that is used to accrue all the data of Timers with the same
 * name.
 *
 * <p>There are two types of Timers, a Timed Timer and a Null Timer. During
 * normal operation the fixture issues a Timed Timer that tracks the time
 * between its start and stop methods are called finally placing the results in
 * its master Timer. If monitoring has been disabled for either the entire
 * fixture or for a specific named Timer, then a Null Timer is issued. It
 * implements the exact same interface as the timed Timer, but the Null Timer
 * contains no logic thereby saving on processing when monitoring is not
 * desired.
 *
 * <p>A single Timer reference can be started and stopped several times, each
 * interval between the start-stop calls being added to the accrued value of
 * the Timer.
 */
public interface Timer {

  /**
   * Access the datum collected.
   *
   * @return The value of the datum collected.
   */
  public long getAccrued();




  /**
   * Access the master timer that tracks data for all timers in the named set.
   *
   * @return The master for this timer.
   */
  public TimerMaster getMaster();




  /**
   * Access the name of the timer.
   *
   * @return Name of the timer instance.
   */
  public String getName();




  /**
   * @return True if the time has been started, false if stopped.
   */
  public boolean isRunning();




  /**
   * Start this timer collecting datum.
   */
  public void start();




  /**
   * Stop this timer from collecting datum.
   */
  public void stop();

}
