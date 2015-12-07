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
import coyote.batch.TransformContext;
import coyote.batch.eval.EvaluationException;
import coyote.loader.log.Log;
import coyote.loader.log.LogMsg;


/**
 * If the conditions of this filter are met, then the named field is removed 
 * from the frame. 
 * 
 * <p>Processing continues after this filter is processed.</p>
 */
public class Remove extends AbstractFrameFilter implements FrameFilter {

  /**
   * @see coyote.batch.filter.AbstractFrameFilter#open(coyote.batch.TransformContext)
   */
  @Override
  public void open( TransformContext context ) {
    super.open( context );

    // TODO look for the field we are supposed to remove
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

          // TODO: remove the named field

          // signal that other filters should still run
          return true;

        }
      } catch ( EvaluationException e ) {
        Log.warn( LogMsg.createMsg( Batch.MSG, "Filter.accept_boolean_evaluation_error", e.getMessage() ) );
      }
    }

    return true;
  }

}
