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
 * value if the existing field will be replaced.</p>
 * 
 * <p>The expressions can use the names of values in the context as variables 
 * in the expressions. Fields in the source, working and target frames are also 
 * accessable through a simple naming convention.</p>
 * 
 * <p>The configuration is:<pre>"Set" : { "Name": "type", "Condition": "some_expression", "Value": "0001.00", "Default": "0.0" }</pre>
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
 * templates, then evaluated as expressions to provide a high degree of 
 * configurability to the transformation.</p>
 * 
 * <p>If only {@code Name} and {@code Value} are configured, then the transform 
 * unconditionally sets the the specified value in the named field.</p>
 * 
 */
public class Set extends AbstractFrameTransform implements FrameTransform {

  // The name of the field we are to transform
  private String fieldName = null;
  private String fieldValue = null;
  private String defaultValue = null;
  private String expression = null;
  private Evaluator evaluator = new Evaluator();




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

    evaluator.setContext( context );

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

    token = findString( ConfigTag.CONDITION );
    if ( StringUtil.isNotBlank( token ) ) {
      expression = token.trim();

      try {
        evaluator.evaluateBoolean( expression );
      } catch ( EvaluationException e ) {
        context.setError( "Invalid boolean exception in SET transform: " + e.getMessage() );
      }
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

    frame.add( fieldName, super.resolveArgument( fieldValue ) );
    return frame;
  }

}
