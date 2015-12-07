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
import coyote.batch.eval.EvaluationException;
import coyote.loader.log.Log;
import coyote.loader.log.LogMsg;


/**
 * If the condition is met, then this frame is accepted as it is, no other rule 
 * processing is performed.
 */
public class Accept extends AbstractFrameFilter implements FrameFilter {

 
  public Accept() {
  }

  public Accept( String condition ) {
    super( condition );
  }


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

          // signal that other filters should not run
          return false;

        }
      } catch ( EvaluationException e ) {
        Log.warn( LogMsg.createMsg( Batch.MSG, "Filter.accept_boolean_evaluation_error", e.getMessage() ) );
      }
    }

    return true;
  }

}
