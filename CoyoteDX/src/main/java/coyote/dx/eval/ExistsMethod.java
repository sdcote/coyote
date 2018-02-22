/*
 * Copyright (c) 2017 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.dx.eval;

import coyote.dx.context.TransformContext;


/**
 * Checks to see if a variable exists in the context
 */
public class ExistsMethod extends AbstractBooleanMethod {

  /**
   * Determine if the named variable exists in the context.
   * 
   * <p>This does not check the value of the variable. it may be null, empty 
   * or all whitespace.
   * 
   * @param context The transform context in which to look for the job status
   * @param token name of the value to resolve in the context.
   * 
   * @return true if there is a context variable with the given name, false 
   * otherwise.
   */
  public static Boolean execute(TransformContext context, String token) {
    String fieldname = sanitize(token);
    boolean retval = false;
    if (context != null) {
      retval = context.containsField(fieldname);
    }
    return retval;
  }

}
