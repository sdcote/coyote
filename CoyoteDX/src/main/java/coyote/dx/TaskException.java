/*
 * Copyright (c) 2015 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 *
 * Contributors:
 *   Stephan D. Cote 
 *      - Initial concept and initial implementation
 */
package coyote.dx;

/**
 * 
 */
public class TaskException extends Exception {

  private static final long serialVersionUID = -2441004868041042937L;




  public TaskException() {
    super();
  }




  public TaskException( String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace ) {
    super( message, cause, enableSuppression, writableStackTrace );
  }




  public TaskException( String message, Throwable cause ) {
    super( message, cause );
  }




  public TaskException( String message ) {
    super( message );
  }




  public TaskException( Throwable cause ) {
    super( cause );
  }

}
