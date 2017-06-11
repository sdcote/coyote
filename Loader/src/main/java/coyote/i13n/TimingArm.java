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

import java.util.ArrayList;
import java.util.HashMap;


/**
 * The TimingArm class models an Application Response Measurement transaction
 * that actually measures the transaction and updates the ARM Master.
 */
public final class TimingArm extends NullArm {
  /** Primary correlation identifier of this and all other related ARMs */
  String _crid = null;

  volatile private long _startTime = 0;
  volatile private long _stopTime = 0;
  volatile boolean _isRunningFlag = false;
  volatile long _accrued;
  final ArrayList<ArmTransaction> children = new ArrayList<ArmTransaction>();

  /** Counters attached to this ARM */
  private final HashMap<String, Counter> counters = new HashMap<String, Counter>();




  /**
  * Create a ARM transaction using the given ARM master.
  */
  public TimingArm( final ArmMaster master, final String name, final String crid ) {
    super( master, name, crid );
  }




  public TimingArm( final ArmMaster master, final String name, final String crid, final ArmTransaction parent ) {
    super( master, name, crid, parent );
  }




  /**
   * @see coyote.i13n.ArmTransaction#decrease(java.lang.String, long)
   */
  @Override
  public long decrease( final String tag, final long value ) {
    return getCounter( tag ).decrease( value );
  }




  /**
   * @see coyote.i13n.ArmTransaction#decrement(java.lang.String)
   */
  @Override
  public long decrement( final String tag ) {
    return getCounter( tag ).decrement();
  }




  /* (non-Javadoc)
   * @see coyote.i13n.ArmTransaction#destroy()
   */
  @Override
  public void destroy() {
    // TODO Auto-generated method stub
  }




  /**
   * Return the counter with the given name.
   *
   * <p>If the counter does not exist, one will be created and added to the
   * static list of counters for later retrieval.
   *
   * @param name The name of the counter to return.
   *
   * @return The counter with the given name.
   */
  public Counter getCounter( final String name ) {
    Counter counter = null;
    synchronized( counters ) {
      counter = counters.get( name );
      if ( counter == null ) {
        counter = new Counter( name );
        counters.put( name, counter );
      }
    }
    return counter;
  }




  /**
   * Return the amount of time this transaction has not been waiting for
   * another transaction.
   *
   * @see coyote.i13n.NullArm#getOverheadTime()
   */
  @Override
  public long getOverheadTime() {
    return getTotalTime() - getWaitTime();
  }




  /**
   * Get the epoch GMT time in milliseconds this transaction was started.
   *
   * @see coyote.i13n.NullArm#getStartTime()
   */
  @Override
  public long getStartTime() {
    return _startTime;
  }




  /**
   * Get the epoch GMT time in milliseconds this transaction was stopped.
   *
   * @see coyote.i13n.NullArm#getStopTime()
   */
  @Override
  public long getStopTime() {
    return _stopTime;
  }




  @Override
  public long getTotalTime() {
    return _accrued + timeElapsedSinceLastStart();
  }




  /**
   * Return the total number of milliseconds this transaction has been waiting
   * for callout (child) transactions.
   *
   * @see coyote.i13n.NullArm#getWaitTime()
   */
  @Override
  public long getWaitTime() {
    long retval = 0;
    for ( int x = 0; x < children.size(); retval += ( children.get( x++ ) ).getTotalTime() ) {
      ;
    }
    return retval;

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
    if ( _isRunningFlag ) {
      _accrued += value;
    }
  }




  /**
   * @see coyote.i13n.ArmTransaction#increase(java.lang.String, long)
   */
  @Override
  public long increase( final String tag, final long value ) {
    return getCounter( tag ).increase( value );
  }




  /**
   * @see coyote.i13n.ArmTransaction#increment(java.lang.String)
   */
  @Override
  public long increment( final String tag ) {
    return getCounter( tag ).increment();
  }




  /* (non-Javadoc)
   * @see coyote.i13n.ArmTransaction#start()
   */
  @Override
  public void start() {
    if ( !_isRunningFlag ) {
      _startTime = System.currentTimeMillis();
      _isRunningFlag = true;
      if ( _master != null ) {
        _master.start( this );
      }
      _status = ArmTransaction.RUNNING;
    }
  }




  /**
   * @see coyote.i13n.ArmTransaction#startArm(java.lang.String)
   */
  @Override
  public ArmTransaction startArm( final String name ) {
    return startArm( name, null );
  }




  /**
   * @see coyote.i13n.ArmTransaction#startArm(java.lang.String,java.lang.String)
   */
  @Override
  public ArmTransaction startArm( final String name, final String crid ) {
    // create an arm
    final ArmTransaction retval = new TimingArm( null, name, crid );

    // start it
    retval.start();

    // add it to or list of children
    children.add( retval );

    // return it
    return retval;
  }




  /* (non-Javadoc)
   * @see coyote.i13n.ArmTransaction#stop()
   */
  @Override
  public long stop() {
    if ( _isRunningFlag ) {
      _stopTime = System.currentTimeMillis();
      increase( timeElapsedSinceLastStart() );
      if ( _master != null ) {
        _master.increase( _accrued );
        _master.stop( this );
      }
      _isRunningFlag = false;
      _status = ArmTransaction.COMPLETE;
    }

    return 0;
  }




  @Override
  public long stop( final short status ) {
    final long retval = stop();

    _status = status;

    return retval;
  }




  /**
   * Get a number of miliseconds since the last start.
   *
   * @return the number of milliseconds since the last start.
   */
  private long timeElapsedSinceLastStart() {
    if ( _isRunningFlag ) {
      return System.currentTimeMillis() - _startTime;
    } else {
      return 0;
    }
  }




  /**
   * Return the string representation of the timer.
   *
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    final StringBuffer b = new StringBuffer();
    if ( _master != null ) {
      b.append( _master._name );
    } else {
      b.append( "ARM" );
    }

    if ( _crid != null ) {
      b.append( '[' );
      b.append( _crid );
      b.append( ']' );
    }
    b.append( ": " );
    b.append( getTotalTime() );
    b.append( " ms" );

    if ( _isRunningFlag ) {
      b.append( " - still running" );
    }
    return b.toString();
  }




  /**
   * @see coyote.i13n.ArmTransaction#update(java.lang.String, java.lang.Object)
   */
  @Override
  public void update( final String name, final Object value ) {
    // TODO Auto-generated method stub
  }
}
