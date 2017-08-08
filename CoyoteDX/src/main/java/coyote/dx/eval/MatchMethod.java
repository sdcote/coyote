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
 * Compares to (context) strings
 */
public class MatchMethod extends AbstractBooleanMethod {

  /**
   * Perform a case insensitive match between the two arguments.
   * 
   * <p>If the arguments did not return a frame value, assume a quoted string. 
   * And if the argument is still null, just use the raw argument.
   * 
   * @param context The transform context in which to look for the job status
   * @param arg1
   * @param arg2
   * 
   * @return true if a arguments match, false otherwise
   */
  public static boolean execute(TransformContext context, String arg1, String arg2) {
    if (context != null) {
      String value = context.resolveToString(arg1);
      if (value == null) {
        value = StringUtil.getQuotedValue(arg1);
        if (value == null) {
          value = arg1;
        }
      }
      String test = context.resolveToString(arg2);
      if (test == null) {
        test = StringUtil.getQuotedValue(arg2);
        if (test == null) {
          test = arg2;
        }
      }

      if (StringUtil.equalsIgnoreCase(value, test)) {
        return true;
      }
    } else {
      return false;
    }
    return false;
  }

}
