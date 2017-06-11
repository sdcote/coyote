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

import coyote.loader.log.Log;


/**
 * The NullArm class models an ARM transaction that performs no work.
 */
public class NullArm implements ArmTransaction {
  protected static final long ARM_EVENTS = Log.getCode( ArmTransaction.LOG_CATEGORY );

  /** The master timer that accumulates our data. */
  protected ArmMaster _master = null;

  /** The parent of this transaction. */
  protected ArmTransaction _parent = null;

  protected String _crid = null;
  protected String _name = null;

  protected short _status = ArmTransaction.NEW;




  public NullArm( final ArmMaster master, final String name, final String crid ) {
    _master = master;
    _crid = crid;

    if ( ( name == null ) && ( _master != null ) ) {
      _name = _master._name;
    } else {
      _name = name;
    }
  }




  public NullArm( final ArmMaster master, final String name, final String crid, final ArmTransaction parent ) {
    _master = master;
    _crid = crid;
    _parent = parent;
    if ( ( name == null ) && ( _master != null ) ) {
      _name = _master._name;
    } else {
      _name = name;
    }
  }




  /**
   * see coyote.i13n.NullArm#decrement(java.lang.String, long)
   */
  @Override
  public long decrease( final String name, final long value ) {
    return 0;
  }




  /**
   * @see coyote.i13n.NullArm#decrement(java.lang.String)
   */
  @Override
  public long decrement( final String name ) {
    return 0;
  }




  /**
   * @see coyote.i13n.NullArm#destroy()
   */
  @Override
  public void destroy() {}




  /**
   * @see coyote.i13n.ArmTransaction#getCRID()
   */
  @Override
  public String getCRID() {
    return _crid;
  }




  /**
   * @see coyote.i13n.ArmTransaction#getMaster()
   */
  @Override
  public ArmMaster getMaster() {
    return _master;
  }




  /**
   * @see coyote.i13n.ArmTransaction#getName()
   */
  @Override
  public String getName() {
    return _name;
  }




  /**
   * @see coyote.i13n.ArmTransaction#getOverheadTime()
   */
  @Override
  public long getOverheadTime() {
    return 0;
  }




  /**
   * @see coyote.i13n.ArmTransaction#getStartTime()
   */
  @Override
  public long getStartTime() {
    return 0;
  }




  /**
   * @see coyote.i13n.ArmTransaction#getStatus()
   */
  @Override
  public short getStatus() {
    return _status;
  }




  /**
   * @see coyote.i13n.ArmTransaction#getStopTime()
   */
  @Override
  public long getStopTime() {
    return 0;
  }




  /**
   * @see coyote.i13n.ArmTransaction#getTotalTime()
   */
  @Override
  public long getTotalTime() {
    return 0;
  }




  /**
   * @see coyote.i13n.ArmTransaction#getWaitTime()
   */
  @Override
  public long getWaitTime() {
    return 0;
  }




  /**
   * @see coyote.i13n.NullArm#increase(java.lang.String, long)
   */
  @Override
  public long increase( final String name, final long value ) {
    return 0;
  }




  /**
   * @see coyote.i13n.NullArm#increment(java.lang.String)
   */
  @Override
  public long increment( final String name ) {
    return 0;
  }




  /**
   * @see coyote.i13n.ArmTransaction#setCRID(java.lang.String)
   */
  @Override
  public void setCRID( final String crid ) {
    _crid = crid;
  }




  /**
   * @see coyote.i13n.NullArm#start()
   */
  @Override
  public void start() {}




  /**
   * Create a child ARM.
   *
   * @param name
   *
   * @return a new no-op ARM
   */
  @Override
  public ArmTransaction startArm( final String name ) {
    return new NullArm( null, null, null );
  }




  @Override
  public ArmTransaction startArm( final String name, final String crid ) {
    return new NullArm( null, null, null );
  }




  /**
   * @see coyote.i13n.NullArm#stop()
   */
  @Override
  public long stop() {
    return 0;
  }




  /**
   * @see coyote.i13n.NullArm#stop(short)
   */
  @Override
  public long stop( final short status ) {
    return 0;
  }




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

    return "NullARM";
  }




  /**
   * @see coyote.i13n.NullArm#update(java.lang.String, java.lang.Object)
   */
  @Override
  public void update( final String name, final Object value ) {}

}
