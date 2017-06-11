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
 * The Metric class models a basic metric.
 */
public class Metric {
  protected String _name = null;
  protected long _updateCount = 0;




  /**
   *
   */
  public Metric( final String name ) {
    _name = name;
  }




  /**
   * @return The currently set name of this object.
   */
  public String getName() {
    return _name;
  }




  /**
   * @return The number of times the value was updated.
   */
  public long getUpdateCount() {
    return _updateCount;
  }




  /**
   * Included for balance but it should not be used by the uninitiated.
   *
   * @param name The new name to set.
   */
  void setName( final String name ) {
    _name = name;
  }
}