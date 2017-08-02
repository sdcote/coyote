/*
 * Copyright (c) 2017 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.dx.eval;

import java.util.regex.Pattern;

import coyote.dx.context.TransformContext;


/**
 * Checks if the context variable value matches a given regular expression.
 */
public class RegexMethod extends AbstractBooleanMethod {

  /**
   * Checks to see if the named token matches the given regular expression.
   * 
   * @param context The transform context in which to look for the job status
   * @param token name of the context variable to check
   * @param regex the regular expression
   * 
   * @return true if there is a match false otherwise
   */
  public static Boolean execute(TransformContext context, String token, String regex) {
    boolean retval = false;
    String key = sanitize(token);
    String value = context.resolveToString(key);
    if (value != null)
      retval = Pattern.compile(regex).matcher(value).find();
    return retval;
  }

}
