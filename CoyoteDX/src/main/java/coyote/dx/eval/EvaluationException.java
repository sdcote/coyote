/*
 * Copyright (c) 2015 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.dx.eval;

/**
 * 
 */
public class EvaluationException extends Exception {

  private static final long serialVersionUID = 8874750787868623675L;




  /**
   * 
   */
  public EvaluationException() {
    super();
  }




  /**
   * @param message
   */
  public EvaluationException( final String message ) {
    super( message );
  }




  /**
   * @param message
   * @param cause
   */
  public EvaluationException( final String message, final Throwable cause ) {
    super( message, cause );
  }




  /**
   * @param message
   * @param cause
   * @param enableSuppression
   * @param writableStackTrace
   */
  public EvaluationException( final String message, final Throwable cause, final boolean enableSuppression, final boolean writableStackTrace ) {
    super( message, cause, enableSuppression, writableStackTrace );
  }




  /**
   * @param cause
   */
  public EvaluationException( final Throwable cause ) {
    super( cause );
  }

}
