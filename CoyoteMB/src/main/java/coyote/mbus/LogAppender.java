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
package coyote.mbus;

/**
 * Very simple interface to a logging object.
 */
public interface LogAppender {

  /**
   * Append the given message to the log.
   * 
   * @param message The message to append.
   */
  public void append( String message );




  /**
   * @return True if logging is enabled, fales otherwise.
   */
  public boolean isEnabled();




  /**
   * @param enabled set to true to enable logging, false to disable
   */
  public void setEnabled( boolean enabled );

}
