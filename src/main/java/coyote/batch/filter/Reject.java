/*
 * Copyright (c) 2015 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 *
 * Contributors:
 *   Stephan D. Cote 
 *      - Initial concept and initial implementation
 */
package coyote.batch.filter;

import coyote.batch.Batch;
import coyote.batch.FrameFilter;
import coyote.batch.TransactionContext;
import coyote.loader.log.Log;
import coyote.loader.log.LogMsg;


/**
 * If the conditions in this frame are met, then this frame is rejected, no 
 * other processing is performed.
 * 
 */
public class Reject extends AbstractFrameFilter implements FrameFilter {

  /**
   * @see coyote.batch.FrameFilter#process(coyote.batch.TransactionContext)
   */
  @Override
  public boolean process( TransactionContext context ) {

    // If there is a conditional expression
    if ( expression != null ) {

      try {
        // if the condition evaluates to true
        if ( evaluator.evaluateBoolean( expression ) ) {

          if ( Log.isLogging( Log.DEBUG_EVENTS ) )
            Log.debug( "Rejected frame " + context.getRow() );

          // remove the working frame from the context
          context.setWorkingFrame( null );

          // signal that other filters should not run since the frame has been rejected
          return false;
        }
      } catch ( IllegalArgumentException e ) {
        Log.warn( LogMsg.createMsg( Batch.MSG, "Filter.reject_boolean_evaluation_error", e.getMessage() ) );
      }
    } else {

      // no expression in a reject filter causes the removal of the working frame 
      context.setWorkingFrame( null );
      return false;
    }

    return true;
  }

}
