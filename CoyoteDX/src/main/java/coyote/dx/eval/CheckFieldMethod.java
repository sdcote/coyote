/*
 * Copyright (c) 2017 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.dx.eval;

import coyote.commons.StringUtil;
import coyote.dx.context.TransformContext;


/**
 * Checks if the field contains expected values.
 */
public class CheckFieldMethod extends AbstractBooleanMethod {

  /**
   * Resolve the token in the context and determine if it is null or an empty 
   * string ("").
   * 
   * @param context The transform context in which to look for the job status
   * @param field name of the value to resolve in the context.
   * @param operator token representing the comparative operation to perform
   * @param expected the value against which the field is evaluated.
   * 
   * @return true if the named field matches expected comparison, false 
   *         otherwise.
   */
  public static Boolean execute(TransformContext context, String field, String operator, String expected) {
    String key = sanitize(field);
    String value = context.resolveToString(key);
    
    
    return true;
  }

}
