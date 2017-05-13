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
package coyote.dx.transform;

import coyote.commons.StringUtil;
import coyote.dataframe.DataFrame;
import coyote.dx.CDX;
import coyote.dx.ConfigTag;
import coyote.dx.FrameTransform;
import coyote.dx.TransformException;
import coyote.dx.context.TransformContext;
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
 * accessible through a simple naming convention.</p>
 * 
 * <p>The following is a real life example which sets the {@code terminator} to 
 * a value of "2" if the context is processing the last record or "1" otherwise
 * which indicates more records (frames) are to be expected:
 * <pre> "Set" : { "Name" : "cust_item_usage", "Value" : "0001.00" }
 * "Set" : { "Name" : "terminator", "Condition": "islast", "Value" : "2", "Default" : "1" }</pre>
 * <ul>
 * <li>Name - the name of the field to set. (Required)</li>
 * <li>Condition - The boolean expression which must evaluate to true foe the 
 * value to be set. (defaults to "true")</li>
 * <li>Value - The value to set in the named field if the condition evaluates 
 * to true or is omitted. (Required)</li>
 * <li>Default - The value to set in the named field if the {@code Condition} 
 * evaluates to false. (optional, requires {@code Condition}</li>
 * </ul>
 * 
 * <p>{@code Condition}, {@code Value} and {@code Default} will be treated as 
 * templates to provide a high degree of configurability to the transformation.</p>
 * 
 * <p>If only {@code Name} and {@code Value} are configured, then the transform 
 * unconditionally sets the the specified value in the named field.</p>
 */
public class Set extends AbstractFieldTransform implements FrameTransform {

  private String fieldValue = null;
  private String defaultValue = null;




  // TODO: Support specifying a type...particularly when there is no existing value

  /**
   * 
   */
  public Set() {

  }




  /**
   * @see coyote.dx.transform.AbstractFrameTransform#open(coyote.dx.context.TransformContext)
   */
  @Override
  public void open( final TransformContext context ) {
    super.open( context );

    // the value may be a string, boolean or numeric - match the type
    // TODO getConfiguration().getFieldIgnoreCase( ConfigTag.VALUE );

    // the default value may be a string, boolean or numeric - match the type
    // TODO getConfiguration().getFieldIgnoreCase( ConfigTag.DEFAULT );

    // the type of the data to set in the field...any of the data frame types
    // TODO getConfiguration().getFieldIgnoreCase( ConfigTag.TYPE );

    String token = findString( ConfigTag.VALUE );
    if ( token == null ) {
      Log.warn( LogMsg.createMsg( CDX.MSG, "Transform.Set_setting_null_to_field", fieldName ) );
    } else {
      fieldValue = token;
    }

    if ( StringUtil.isNotBlank( getExpression() ) ) {
      token = findString( ConfigTag.DEFAULT );
      if ( token == null ) {
        Log.warn( LogMsg.createMsg( CDX.MSG, "Transform.Set_setting_null_by_default", fieldName ) );
      } else {
        defaultValue = token;
      }
    }

  }




  /**
   * @see coyote.dx.FrameTransform#process(coyote.dataframe.DataFrame)
   */
  @Override
  public DataFrame process( final DataFrame frame ) throws TransformException {

    // If there is a conditional expression
    if ( getExpression() != null ) {

      try {
        // if the condition evaluates to true
        if ( evaluator.evaluateBoolean( getExpression() ) ) {

          frame.put( getFieldName(), resolveArgument( fieldValue ) );

        } else {
          // if there is a default value,
          if ( defaultValue != null ) {
            // set it
            frame.put( getFieldName(), resolveArgument( defaultValue ) );
          }
        }
      } catch ( final IllegalArgumentException e ) {
        Log.warn( LogMsg.createMsg( CDX.MSG, "Transform.Set_boolean_evaluation_error", e.getMessage() ) );
      }

    } else {
      // unconditionally set the value
      frame.put( getFieldName(), resolveArgument( fieldValue ) );
    }

    return frame;
  }

}
