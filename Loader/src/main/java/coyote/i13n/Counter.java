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
 * The Counter class models an object that tracks a numerical value.
 *
 * <p>Each fixture manages a dynamic set of named numeric counters accessible
 * by a name. These counters allow applications to increment, decrement and
 * reset these values allowing for easy metric collection.
 *
 * <p>This class is thread-safe in that all the methods synchronize on the name
 * of the counter to avoid double synchronization on the class itself.
 *
 * @author Stephan D. Cote' - Enterprise Architecture
 * @version $Revision$
 */
public class Counter extends Metric implements Cloneable {
  private String _units = null;
  private long _value = 0;
  private long _minValue = 0;
  private long _maxValue = 0;




  /**
   * Create a counter with a name.
   */
  public Counter( final String name ) {
    super( name );
  }




  /**
   * Create a deep copy of this counter.
   */
  @Override
  public Object clone() {
    final Counter retval = new Counter( _name );
    retval._units = _units;
    retval._value = _value;
    retval._minValue = _minValue;
    retval._maxValue = _maxValue;
    retval._updateCount = _updateCount;
    return retval;
  }




  /**
   * Decrease the counter by the given amount.
   *
   * @param amt The amount to subtract from the counter.
   *
   * @return The final value of the counter after the operation.
   */
  public long decrease( final long amt ) {
    synchronized( _name ) {
      _updateCount++;
      _value -= amt;
      if ( _value < _minValue ) {
        _minValue = _value;
      }
      if ( _value > _maxValue ) {
        _maxValue = _value;
      }
      return _value;
    }
  }




  /**
   * Decrement the counter by one.
   *
   * @return The final value of the counter after the operation.
   */
  public long decrement() {
    synchronized( _name ) {
      _updateCount++;
      _value--;
      if ( _value < _minValue ) {
        _minValue = _value;
      }
      return _value;
    }
  }




  /**
   * @return Returns the maximum value the counter ever represented.
   */
  public long getMaxValue() {
    synchronized( _name ) {
      return _maxValue;
    }
  }




  /**
   * @return Returns the minimum value the counter ever represented.
   */
  public long getMinValue() {
    synchronized( _name ) {
      return _minValue;
    }
  }




  /**
   * @return Returns the units the counter measures.
   */
  public String getUnits() {
    return _units;
  }




  /**
   * @return Returns the current value of the counter.
   */
  public long getValue() {
    synchronized( _name ) {
      return _value;
    }
  }




  /**
   * Increase the counter by the given amount.
   *
   * @param amt The amount to add to the counter.
   *
   * @return The final value of the counter after the operation.
   */
  public long increase( final long amt ) {
    synchronized( _name ) {
      _updateCount++;
      _value += amt;
      if ( _value < _minValue ) {
        _minValue = _value;
      }
      if ( _value > _maxValue ) {
        _maxValue = _value;
      }
      return _value;
    }
  }




  /**
   * Increment the counter by one.
   *
   * @return The final value of the counter after the operation.
   */
  public long increment() {
    synchronized( _name ) {
      _updateCount++;
      _value++;
      if ( _value > _maxValue ) {
        _maxValue = _value;
      }
      return _value;
    }
  }




  /**
   * Set the current, update count and Min/Max values to zero.
   *
   * <p>The return value will represent a copy of the counter prior to the
   * reset and is useful for applications that desire delta values. These delta
   * values are simply the return values of successive reset calls.
   *
   * @return a counter representing the state prior to the reset.
   */
  public Counter reset() {
    synchronized( _name ) {
      final Counter retval = (Counter)clone();

      _value = 0;
      _minValue = 0;
      _maxValue = 0;
      _updateCount = 0;

      return retval;
    }
  }




  /**
   * Sets the units the counter measures.
   *
   * @param units The units to set.
   */
  public void setUnits( final String units ) {
    synchronized( _name ) {
      _units = units;
    }
  }




  /**
   * Return the human-readable form of this counter.
   */
  @Override
  public String toString() {
    synchronized( _name ) {
      final StringBuffer buff = new StringBuffer( _name );
      buff.append( "=" );
      buff.append( Long.toString( _value ) );
      if ( _units != null ) {
        buff.append( _units );
      }
      buff.append( "[min=" );
      buff.append( Long.toString( _minValue ) );
      buff.append( ":max=" );
      buff.append( Long.toString( _maxValue ) );
      buff.append( "]" );

      return buff.toString();
    }
  }
}
