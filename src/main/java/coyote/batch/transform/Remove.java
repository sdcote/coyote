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
package coyote.batch.transform;

import coyote.batch.Batch;
import coyote.batch.FrameTransform;
import coyote.batch.TransactionContext;
import coyote.batch.TransformContext;
import coyote.batch.TransformException;
import coyote.dataframe.DataFrame;
import coyote.loader.log.Log;
import coyote.loader.log.LogMsg;


/**
 * If the conditions of this transform are met, then the named field is removed 
 * from the frame. 
 * 
 * <p>This only removes the first occurrence of the name field.</p>
 * 
 * <p>Processing continues after this filter is processed.</p>
 */
public class Remove extends AbstractFieldTransform implements FrameTransform {

  /**
   * @see coyote.batch.filter.AbstractFrameFilter#open(coyote.batch.TransformContext)
   */
  @Override
  public void open( final TransformContext context ) {
    super.open( context );

  }




  @Override
  public DataFrame process( final DataFrame frame ) throws TransformException {
    // If there is a conditional expression
    if ( expression != null ) {

      try {
        // if the condition evaluates to true
        if ( evaluator.evaluateBoolean( expression ) ) {

          // remove the named field
          if ( frame != null ) {
            frame.remove( fieldName );
          }

        }
      } catch ( final IllegalArgumentException e ) {
        Log.warn( LogMsg.createMsg( Batch.MSG, "Transform.remove_boolean_evaluation_error", e.getMessage() ) );
      }
    }
    return frame;
  }




  public boolean process( final TransactionContext context ) {
    // If there is a conditional expression
    if ( expression != null ) {

      try {
        // if the condition evaluates to true
        if ( evaluator.evaluateBoolean( expression ) ) {

          // remove the named field
          if ( context != null ) {
            if ( context.getWorkingFrame() != null ) {
              context.getWorkingFrame().remove( fieldName );
            }
          } else {
            Log.warn( LogMsg.createMsg( Batch.MSG, "Transform.context not set" ) );
          }

        }
      } catch ( final IllegalArgumentException e ) {
        Log.warn( LogMsg.createMsg( Batch.MSG, "Transform.remove_boolean_evaluation_error", e.getMessage() ) );
      }
    }

    return true;
  }

}
