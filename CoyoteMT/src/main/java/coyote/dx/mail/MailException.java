/*
 * Copyright (c) 2017 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 *
 * Contributors:
 *   Stephan D. Cote 
 *      - Initial concept and implementation
 */
package coyote.dx.mail;

/**
 * 
 */
public class MailException extends Exception {

  /**
   * 
   */
  private static final long serialVersionUID = -8977541322875778842L;




  /**
   * 
   */
  public MailException() {
    // TODO Auto-generated constructor stub
  }




  /**
   * @param message
   */
  public MailException( String message ) {
    super( message );
    // TODO Auto-generated constructor stub
  }




  /**
   * @param cause
   */
  public MailException( Throwable cause ) {
    super( cause );
    // TODO Auto-generated constructor stub
  }




  /**
   * @param message
   * @param cause
   */
  public MailException( String message, Throwable cause ) {
    super( message, cause );
    // TODO Auto-generated constructor stub
  }




  /**
   * @param message
   * @param cause
   * @param enableSuppression
   * @param writableStackTrace
   */
  public MailException( String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace ) {
    super( message, cause, enableSuppression, writableStackTrace );
    // TODO Auto-generated constructor stub
  }

}
