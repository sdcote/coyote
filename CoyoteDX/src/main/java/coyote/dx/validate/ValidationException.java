/*
 * Copyright (c) 2015 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.dx.validate;

/**
 * 
 */
public class ValidationException extends Exception {

  private static final long serialVersionUID = 2195504109617659621L;

  /**
   * @param msg
   */
  public ValidationException(String msg) {
    super(msg);
  }

}
