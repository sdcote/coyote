/*
 * Copyright (c) 2006 Stephan D. Cote' - All rights reserved.
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
 * The TimerBase class models the base class for all timers.
 */
class TimerBase implements Timer {
  /** Our master timer used to correlate all timer metrics */
  protected static final TimerMaster NULL_MASTER = new NullMaster();

  /** The master timer that accumulates our data. */
  protected TimerMaster _master = null;

  /** Flag indicating if we have been started or not. */
  volatile boolean _isRunningFlag = false;




  /**
   *
   */
  public TimerBase( final TimerMaster master ) {
    super();
    _master = master;
  }




  /**
   * Implemented to meet Timer interface requirements.
   */
  @Override
  public long getAccrued() {
    return 0;
  }




  /**
   * Implemented to meet Timer interface requirements.
   *
   * @see coyote.i13n.Timer#getMaster()
   */
  @Override
  public TimerMaster getMaster() {
    return _master;
  }




  @Override
  public String getName() {
    return _master.getName();
  }




  /**
   * @see coyote.i13n.Timer#isRunning()
   */
  @Override
  public boolean isRunning() {
    return _isRunningFlag;
  }




  /**
   * Implemented to meet Timer interface requirements.
   */
  @Override
  public void start() {}




  /**
   * Implemented to meet Timer interface requirements.
   */
  @Override
  public void stop() {}




  /**
   * Return the string representation of the timer.
   *
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    if ( _master != null ) {
      return _master.toString();
    }

    return "";
  }
}
