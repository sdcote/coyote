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
 * Creates a do-nothing timer to facilitate the disabling of timers while
 * not affecting the compiled code of any callers.
 *
 * <p>When a NullTimer is returned, performs no logic when it is stopped and
 * therefore allows for very fast operation when the timer is disabled.
 *
 * <p>See Martin Fowler's refactoring book for details on using Null Objects in
 * software.
 */
public class NullTimer extends TimerBase {

  public static void main( final String[] args ) {}




  /**
   * Create a new timer with a null master.
   */
  public NullTimer() {
    super( TimerBase.NULL_MASTER );
  }




  /**
   * @param master
   */
  public NullTimer( final TimingMaster master ) {
    super( master );
  }
}
