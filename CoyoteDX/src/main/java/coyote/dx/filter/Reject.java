/*
 * Copyright (c) 2015 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.dx.filter;

import coyote.dx.CDX;
import coyote.dx.FrameFilter;
import coyote.dx.context.TransactionContext;
import coyote.loader.log.Log;
import coyote.loader.log.LogMsg;


/**
 * If the conditions in this frame are met, then this frame is rejected, no 
 * other processing is performed.
 * 
 * <p>An example configuration is as follows:<pre>
 * "Filter": {
 *     "Reject": { "condition": "match(Working.Record Type,LN)" }
 * }, </pre>
 * 
 * The above will only reject one type of record and allow all other to pass 
 * through. 
 *  
 *  <p>A Reject filter with no condition, is an un-conditional rejection of 
 *  all frames. This is useful when implementing a white list where Accepts 
 *  are placed before an unconditional reject:<pre>
 * "Filter": {
 *     "Accept": { "condition": "match(Working.Record Type,LN)" }
 *     "Reject": {  }
 * },</pre>
 * The above will only allow "Record Type" with a value of "LN" to pass 
 * through, all others are rejected. 
 */
public class Reject extends AbstractFrameFilter implements FrameFilter {

  /**
   * @see coyote.dx.FrameFilter#process(coyote.dx.context.TransactionContext)
   */
  @Override
  public boolean process(TransactionContext context) {

    // If there is a conditional expression
    if (expression != null) {

      try {
        // if the condition evaluates to true
        if (evaluator.evaluateBoolean(expression)) {

          if (Log.isLogging(Log.DEBUG_EVENTS))
            Log.debug("Rejected frame " + context.getRow());

          // remove the working frame from the context
          context.setWorkingFrame(null);

          // signal that other filters should not run since the frame has been rejected
          return false;
        }
      } catch (IllegalArgumentException e) {
        Log.warn(LogMsg.createMsg(CDX.MSG, "Filter.reject_boolean_evaluation_error", e.getMessage()));
      }
    } else {

      // no expression in a reject filter causes the removal of the working frame 
      context.setWorkingFrame(null);
      return false;
    }

    return true;
  }

}
