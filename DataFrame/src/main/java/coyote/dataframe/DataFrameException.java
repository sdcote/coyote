/*
 * Copyright (c) 2006 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 *
 * Contributors:
 *   Stephan D. Cote 
 *      - Initial API and implementation
 */
package coyote.dataframe;

/**
 * Exception thrown when there is a problem with DataFrame operations.
 */
public final class DataFrameException extends Exception {

  /**
   * 
   */
  private static final long serialVersionUID = 8436470639599616942L;




  /**
   * Constructor with no message.
   */
  public DataFrameException() {
    super();
  }




  /**
   * Constructor with a user message
   *
   * @param message The text of the message.
   */
  public DataFrameException( final String message ) {
    super( message );
  }




  /**
   * Constructor with a user message and a nested throwable object.
   *
   * @param message The text of the message.
   * @param excptn The throwable object (exception?) to nest in this exception
   */
  public DataFrameException( final String message, final Throwable excptn ) {
    super( message, excptn );
  }




  /**
   * Constructor with a nested throwable object.
   *
   * @param excptn The throwable object (exception?) to nest in this exception
   */
  public DataFrameException( final Throwable excptn ) {
    super( excptn );
  }
}
