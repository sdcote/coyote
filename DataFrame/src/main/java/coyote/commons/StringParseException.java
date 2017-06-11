/*
 * Copyright (c) 2015 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 *
 * Contributors:
 *   Stephan D. Cote 
 *      - Initial concept and implementation
 */
package coyote.commons;

/**
 * Exception thrown when there is a problem with parsing a string.
 */
public class StringParseException extends Exception {
  /** */
  private static final long serialVersionUID = -7397711944655415190L;
  private String context = null;




  /**
   * Constructor
   */
  public StringParseException() {
    super();
  }




  /**
   * Constructor
   *
   * @param message Error message
   */
  public StringParseException( String message ) {
    super( message );
  }




  /**
   * Constructor StringParseException
   *
   * @param message
   * @param context
   */
  public StringParseException( String message, String context ) {
    super( message );

    this.context = context;
  }




  /**
   * Constructor
   *
   * @param message Error message
   * @param excptn
   */
  public StringParseException( String message, Throwable excptn ) {
    super( message, excptn );
  }




  /**
   * Constructor
   *
   * @param excptn
   */
  public StringParseException( Throwable excptn ) {
    super( excptn );
  }




  /**
   * @return the context
   */
  public String getContext() {
    return context;
  }

}