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
 * Checks if the context value is null or empty
 */
public class EmptyMethod extends AbstractBooleanMethod {

  /**
   * Resolve the token in the context and determine if it is null or an empty 
   * string ("").
   * 
   * @param context The transform context in which to look for the job status
   * @param token name of the value to resolve in the context.
   * 
   * @return true if the token does not return a value or if the value 
   *         returned is an empty string, false if not null or empty.
   */
  public static Boolean execute(TransformContext context, String token) {
    String key = sanitize(token);
    String value = null;
    if (context != null) {
      value = context.resolveToString(key);
    }
    return StringUtil.isEmpty(value);
  }

}
