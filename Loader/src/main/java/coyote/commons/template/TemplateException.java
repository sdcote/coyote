/*
 * Copyright (c) 2004 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 *
 * Contributors:
 *   Stephan D. Cote 
 *      - Initial concept and implementation
 */
package coyote.commons.template;

/**
 * Exception thrown when there is a problem with parsing template operations.
 */
public final class TemplateException extends Exception {
  /** */
  private static final long serialVersionUID = -7397711944655415190L;
  private String context = null;




  /**
   * Constructor
   */
  public TemplateException() {
    super();
  }




  /**
   * Constructor
   *
   * @param message Error message
   */
  public TemplateException( String message ) {
    super( message );
  }




  /**
   * Constructor TemplateException
   *
   * @param message
   * @param context
   */
  public TemplateException( String message, String context ) {
    super( message );

    this.context = context;
  }




  /**
   * Constructor
   *
   * @param message Error message
   * @param excptn
   */
  public TemplateException( String message, Throwable excptn ) {
    super( message, excptn );
  }




  /**
   * Constructor
   *
   * @param excptn
   */
  public TemplateException( Throwable excptn ) {
    super( excptn );
  }




  /**
   * @return the context
   */
  public String getContext() {
    return context;
  }

}