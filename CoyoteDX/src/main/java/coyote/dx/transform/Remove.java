/*
 * Copyright (c) 2015 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.dx.transform;

import coyote.dataframe.DataFrame;
import coyote.dx.CDX;
import coyote.dx.FrameTransform;
import coyote.dx.TransformException;
import coyote.dx.context.TransactionContext;
import coyote.loader.log.Log;
import coyote.loader.log.LogMsg;


/**
 * If the conditions of this transform are met, then the named field is removed 
 * from the frame. 
 * 
 * <p>This only removes the first occurrence of the named field.</p>
 * 
 * <p>Processing continues after this filter is processed.</p>
 */
public class Remove extends AbstractFieldTransform implements FrameTransform {

  /**
   * @see coyote.dx.FrameTransform#process(coyote.dataframe.DataFrame)
   */
  @Override
  public DataFrame process(final DataFrame frame) throws TransformException {
    // If there is a conditional expression
    if (getCondition() != null) {

      try {
        // if the condition evaluates to true
        if (evaluator.evaluateBoolean(getCondition())) {

          // remove the named field
          if (frame != null) {
            frame.remove(getFieldName());
          }

        }
      } catch (final IllegalArgumentException e) {
        Log.warn(LogMsg.createMsg(CDX.MSG, "Transform.remove_boolean_evaluation_error", e.getMessage()));
      }
    }
    return frame;
  }




  public boolean process(final TransactionContext context) {
    // If there is a conditional expression
    if (getCondition() != null) {

      try {
        // if the condition evaluates to true
        if (evaluator.evaluateBoolean(getCondition())) {

          // remove the named field
          if (context != null) {
            if (context.getWorkingFrame() != null) {
              context.getWorkingFrame().remove(getFieldName());
            }
          } else {
            Log.warn(LogMsg.createMsg(CDX.MSG, "Transform.context not set"));
          }

        }
      } catch (final IllegalArgumentException e) {
        Log.warn(LogMsg.createMsg(CDX.MSG, "Transform.remove_boolean_evaluation_error", e.getMessage()));
      }
    }

    return true;
  }

}
