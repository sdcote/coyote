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
 * The State class models a metric that stores a discrete, scalar value.
 *
 * <p>Each fixture manages a dynamic set of named scalar values that record the
 * state of any named condition. Similar to counters, the named states allow
 * developers to use state machine patterns in their application designs
 * without having to write extra code.
 *
 * <p>This class is thread-safe in that all the methods synchronize on the name
 * of the state to avoid double synchronizations.
 */
public class State extends Metric {
  public static final short UNKNOWN_TYPE = 0;

  public static final short OBJECT_TYPE = 1;
  public static final short DOUBLE_TYPE = 2;
  public static final short LONG_TYPE = 3;
  short _type = 0;

  long lastUpdated = 0;

  long _longValue = 0;
  Object _objectValue = null;
  double _doubleValue = 0.0;




  /**
   * Create an empty state that can be set to any type initially.
   */
  public State( final String name ) {
    super( name );
  }




  /**
   * @return  Returns the time the state was last updated.
   */
  public long getLastUpdated() {
    return lastUpdated;
  }




  /**
   * @return The value of this state metric as a string.
   */
  public String getStringValue() {
    synchronized( _name ) {
      switch ( _type ) {
        case OBJECT_TYPE:
          return _objectValue.toString();
        case LONG_TYPE:
          return Long.toString( _longValue );
        case DOUBLE_TYPE:
          return Double.toString( _doubleValue );
        default:
          return null;
      }
    }
  }




  /**
   * @return The object value of this state; with float and double being
   *         returned as their respective wrapper types.
   */
  public Object getValue() {
    synchronized( _name ) {
      switch ( _type ) {
        case LONG_TYPE:
          return new Long( _longValue );
        case DOUBLE_TYPE:
          return new Double( _doubleValue );
        default:
          return _objectValue;
      }
    }
  }




  /**
   * Set the state to the given double value.
   *
   * @param val The value to set in the state.
   *
   * @throws IllegalArgumentException if the existing data if of type LONG or
   *         OBJECT.
   */
  public void set( final double val ) {
    synchronized( _name ) {
      lastUpdated = System.currentTimeMillis();

      if ( ( _type == State.UNKNOWN_TYPE ) || ( _type == State.DOUBLE_TYPE ) ) {
        _type = State.DOUBLE_TYPE;
        _doubleValue = val;
        _updateCount++;
      } else {
        throw new IllegalArgumentException( "State metric is not a double type" );
      }
    }
  }




  /**
   * Set the state to the given long value.
   *
   * @param val The value to set in the state.
   *
   * @throws IllegalArgumentException if the existing data if of type DOUBLE or
   *         OBJECT.
   */
  public void set( final long val ) {
    synchronized( _name ) {
      lastUpdated = System.currentTimeMillis();

      if ( ( _type == State.UNKNOWN_TYPE ) || ( _type == State.LONG_TYPE ) ) {
        _type = State.LONG_TYPE;
        _longValue = val;
        _updateCount++;
      } else {
        throw new IllegalArgumentException( "State metric is not a long type" );
      }
    }
  }




  /**
   * Set the state to the given object value.
   *
   * @param val The value to set in the state.
   *
   * @throws IllegalArgumentException if the existing data if of type LONG or
   *         DOUBLE.
   */
  public void set( final Object val ) {
    if ( val == null ) {
      return;
    }

    synchronized( _name ) {
      lastUpdated = System.currentTimeMillis();

      if ( ( _type == State.UNKNOWN_TYPE ) || ( _type == State.OBJECT_TYPE ) ) {
        _type = State.OBJECT_TYPE;
        _objectValue = val;
        _updateCount++;
      } else {
        throw new IllegalArgumentException( "State metric is not an object type" );
      }
    }
  }




  /**
   * @return The human-readable representation of this object as a string.
   */
  @Override
  public String toString() {
    synchronized( _name ) {
      final StringBuffer buff = new StringBuffer( _name );
      buff.append( "=" );
      buff.append( getStringValue() );
      return buff.toString();
    }
  }

}