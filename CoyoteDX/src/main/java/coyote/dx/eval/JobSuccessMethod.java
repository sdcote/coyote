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
package coyote.dx.eval;

import java.util.Map;

import coyote.dx.context.TransformContext;


/**
 * Boolean method to determine if the named job was successfully executed.
 */
public class JobSuccessMethod extends AbstractBooleanMethod {

  /**
   * Returns if the job was executed successfully or not.
   * 
   * <p>This looks in the given context for an object with the given job name, 
   * expecting to find a map representing a context for the named job. If it 
   * is there, a transform disposition map is retrieved. If there is a 
   * disposition map in that context map, the Error State flag is checked. If 
   * it is there and false then true is returned.
   * 
   * <p>Not that if there is no job disposition found for the named job, this 
   * will return false which may not indicate the job failed. It may indicate 
   * that job does not exist. 
   * 
   * @param context The transform context in which to look for the job status
   * @param token the name of the job to query
   * 
   * @return true if a job with that name had a disposition with an error 
   *         state of false, otherwise false will be returned.
   */
  public static Boolean execute(TransformContext context, String token) {
    boolean retval = false;
    String key = sanitize(token);
    Object value = context.get(key);
    if (value != null && value instanceof Map) {
      Map ctx = (Map)value;
      Object obj = ctx.get(TransformContext.DISPOSITION);
      if (obj != null && obj instanceof Map) {
        Object flag = ((Map)obj).get(TransformContext.ERROR_STATE);
        if (flag != null && flag instanceof Boolean) {
          retval = !((Boolean)flag).booleanValue();
        }
      }
    }
    return retval;
  }

}
