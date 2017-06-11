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
 * The TimingTimer class models an actual working implementation of an
 * Timer as opposed to the NullTimer.
 */
public class TimingTimer extends TimerBase {
  volatile private long _startTime = 0;

  volatile long _accrued;




  /**
   * Create a new timer with a null master.
   */
  public TimingTimer() {
    super( TimerBase.NULL_MASTER );
  }




  /**
   *
   */
  public TimingTimer( final TimingMaster master ) {
    super( master );
  }




  /**
   * @return the time that the Timer has been running in milliseconds 
   */
  @Override
  public long getAccrued() {
    return _accrued + timeElapsedSinceLastStart();
  }




  /**
   * Increase the time by the specified amount of milliseconds.
   *
   * <p>This is the method that keeps track of the various statistics being
   * tracked.
   *
   * @param value the amount to increase the accrued value.
   */
  public void increase( final long value ) {
    if ( isRunning() ) {
      _accrued += value;
    }
  }




  @Override
  public void start() {
    if ( !_isRunningFlag ) {
      _startTime = System.currentTimeMillis();
      _isRunningFlag = true;
      _master.start( this );
    }
  }




  @Override
  public void stop() {
    if ( _isRunningFlag ) {
      increase( timeElapsedSinceLastStart() );
      _master.increase( _accrued );
      _master.stop( this );
      _isRunningFlag = false;
    }
  }




  /**
   * Get a number of milliseconds since the last start.
   *
   * @return the number of milliseconds since the last start.
   */
  private long timeElapsedSinceLastStart() {
    if ( isRunning() ) {
      return System.currentTimeMillis() - _startTime;
    } else {
      return 0;
    }
  }

}
