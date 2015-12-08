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
import coyote.batch.ConfigTag;
import coyote.batch.FrameTransform;
import coyote.batch.TransformContext;
import coyote.batch.TransformException;
import coyote.batch.eval.EvaluationException;
import coyote.batch.eval.Evaluator;
import coyote.commons.StringUtil;
import coyote.dataframe.DataFrame;
import coyote.loader.log.Log;
import coyote.loader.log.LogMsg;


/**
 * This sets the value of a field  to a particular value based on some 
 * condition.
 * 
 * <p>The field will be added if it does not exist and if it does exist, the 
 * value if the existing field will be replaced. Note this is different from 
 * the {@code Add} transform which simply adds the named field to the working 
 * record and may result in multiple fields with the same name.</p>
 * 
 * <p>The expressions can use the names of values in the context as variables 
 * in the expressions. Fields in the source, working and target frames are also 
 * accessible through a simple naming convention.</p>
 * 
 * <p>The following is a real life example which sets the {@code terminator} to 
 * a value of "2" if the context is processing the last record or "1" otherwise
 * which indicates more records (frames) are to be expected:
 * <pre>"Set" : { "Name" : "terminator", "Condition": "islast", "Value" : "2", "Default" : "1" }</pre>
 * <ul>
 * <li>Name - the name of the field to set. (Required)</li>
 * <li>Condition - The boolean expression which must evaluate to true foe the 
 * value to be set. (defaults to "true")</li>
 * <li>Value - The value to set in the named field if the condition evaluates 
 * to true or is omitted. (Required)</li>
 * <li>Default - The value to set in the named field if the {@code Condition} 
 * evaluates to false. (optional, requires {@code Condition}</li>
 * </ul></p>
 * 
 * <p>{@code Condition}, {@code Value} and {@code Default} will be treated as 
 * templates to provide a high degree of configurability to the transformation.</p>
 * 
 * <p>If only {@code Name} and {@code Value} are configured, then the transform 
 * unconditionally sets the the specified value in the named field.</p>
 */
public class Set extends AbstractFrameTransform implements FrameTransform {

  // The name of the field we are to transform
  private String fieldName = null;
  private String fieldValue = null;
  private String defaultValue = null;

  // TODO: Support specifying a type...particularly when there is no existing value



  /**
   * 
   */
  public Set() {
    // TODO Auto-generated constructor stub
  }




  /**
   * @see coyote.batch.transform.AbstractFrameTransform#open(coyote.batch.TransformContext)
   */
  @Override
  public void open( TransformContext context ) {
    super.open( context );

    // get the name of the field to add
    String token = findString( ConfigTag.NAME );

    if ( StringUtil.isBlank( token ) ) {
      context.setError( "Set transform must contain a field name" );
    } else {
      fieldName = token.trim();
    }

    token = findString( ConfigTag.VALUE );
    if ( token == null ) {
      Log.warn( LogMsg.createMsg( Batch.MSG, "Transform.Set transform will set a null {} field to the working frames.", fieldName ) );
    } else {
      fieldValue = token;
    }

    token = findString( ConfigTag.DEFAULT );
    if ( token == null ) {
      Log.warn( LogMsg.createMsg( Batch.MSG, "Transform.Set transform will set a null {} field to the working frames by default.", fieldName ) );
    } else {
      defaultValue = token;
    }
  }




  /**
   * @see coyote.batch.FrameTransform#process(coyote.dataframe.DataFrame)
   */
  @Override
  public DataFrame process( DataFrame frame ) throws TransformException {

    // If there is a conditional expression
    if ( expression != null ) {

      try {
        // if the condition evaluates to true
        if ( evaluator.evaluateBoolean( expression ) ) {

          frame.put( fieldName, super.resolveArgument( fieldValue ) );

        } else {
          // if there is a default value,
          if ( defaultValue != null ) {
            // set it
            frame.put( fieldName, super.resolveArgument( defaultValue ) );
          }
        }
      } catch ( EvaluationException e ) {
        Log.warn( LogMsg.createMsg( Batch.MSG, "Transform.Set_boolean_evaluation_error", e.getMessage() ) );
      }

    } else {
      // unconditionally set the value
      frame.put( fieldName, super.resolveArgument( fieldValue ) );
    }

    return frame;
  }

}
