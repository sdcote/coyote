/*
 * Copyright (c) 2015 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 *
 * Contributors:
 *   Stephan D. Cote 
 *      - Initial concept and implementation
 */
package coyote.dx.filter;

import coyote.dx.CDX;
import coyote.dx.FrameFilter;
import coyote.dx.TransactionContext;
import coyote.loader.log.Log;
import coyote.loader.log.LogMsg;


/**
 * If the conditions in this frame are met, then this frame is rejected, no 
 * other processing is performed.
 * 
 */
public class Reject extends AbstractFrameFilter implements FrameFilter {

  /**
   * @see coyote.dx.FrameFilter#process(coyote.dx.TransactionContext)
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
        Log.warn( LogMsg.createMsg( CDX.MSG, "Filter.reject_boolean_evaluation_error", e.getMessage() ) );
      }
    } else {

      // no expression in a reject filter causes the removal of the working frame 
      context.setWorkingFrame( null );
      return false;
    }

    return true;
  }

}
