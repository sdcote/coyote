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
package coyote.mbus.message;

/**
 * Exception thrown when there is a problem with message operations.
 */
public final class MessageException extends Exception {

  /**
   * 
   */
  private static final long serialVersionUID = 8436470639599616942L;




  /**
   * Constructor with no message.
   */
  public MessageException() {
    super();
  }




  /**
   * Constructor with a user message
   *
   * @param message The text of the message.
   */
  public MessageException( final String message ) {
    super( message );
  }




  /**
   * Constructor with a user message and a nested throwable object.
   *
   * @param message The text of the message.
   * @param excptn The throwable object (exception?) to nest in this exception
   */
  public MessageException( final String message, final Throwable excptn ) {
    super( message, excptn );
  }




  /**
   * Constructor with a nested throwable object.
   *
   * @param excptn The throwable object (exception?) to nest in this exception
   */
  public MessageException( final Throwable excptn ) {
    super( excptn );
  }
}
