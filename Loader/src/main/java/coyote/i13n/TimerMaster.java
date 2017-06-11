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
 * The TimerMaster class models the master of all timers with a given name.
 */
public interface TimerMaster {
  /**
   * @return The name of this timer set.
   */
  public String getName();




  public void increase( long value );




  public void start( Timer mon );




  public void stop( Timer mon );

}
