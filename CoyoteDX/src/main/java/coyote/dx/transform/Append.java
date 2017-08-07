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
import coyote.dx.ConfigTag;
import coyote.dx.FrameTransform;
import coyote.dx.TransformException;
import coyote.dx.context.TransformContext;
import coyote.loader.log.Log;
import coyote.loader.log.LogMsg;


/**
 * This appends to the value of a field with a particular value based on some 
 * condition.
 * 
 * <p>The field will be added if it does not exist and if it does exist, the 
 * new value will be appended to the existing value.</p>
 * 
 * <p>The expressions can use the names of values in the context as variables 
 * in the expressions. Fields in the source, working and target frames are also 
 * accessible through a simple naming convention.</p>
 * 
 * <p>The following is a real life example which appends a error message to the 
 * existing comments field based on the value of the error code field:
 * <pre>"Append": { "Name": "comments", "Condition": "equals(working.Error Code,'1')", "Value" : " reason: 1-No username" }</pre>
 * <ul>
 * <li>Name - the name of the field to set. (Required)</li>
 * <li>Condition - The boolean expression which must evaluate to true foe the 
 * value to be set. (defaults to "true")</li>
 * <li>Value - The value to set in the named field if the condition evaluates 
 * to true or is omitted. (Required)</li>
 * </ul>
 * 
 * <p>{@code Condition} and {@code Value} will be treated as templates to 
 * provide a high degree of configurability to the transformation.</p>
 * 
 * <p>If only {@code Name} and {@code Value} are configured, then the transform 
 * unconditionally appends the the specified value in the named field.</p>
 */
public class Append extends AbstractFieldTransform implements FrameTransform {

  private String fieldValue = null;




  // TODO: Support specifying a type...particularly when there is no existing value

  /**
   * 
   */
  public Append() {

  }




  /**
   * @see coyote.dx.transform.AbstractFrameTransform#open(coyote.dx.context.TransformContext)
   */
  @Override
  public void open(final TransformContext context) {
    super.open(context);

    // the value may be a string, boolean or numeric - match the type
    // TODO getConfiguration().getFieldIgnoreCase( ConfigTag.VALUE );

    // the type of the data to set in the field...any of the data frame types
    // TODO getConfiguration().getFieldIgnoreCase( ConfigTag.TYPE );

    String token = getConfiguration().getString(ConfigTag.VALUE);
    if (token == null) {
      Log.warn(LogMsg.createMsg(CDX.MSG, "Transform.Append_setting_null_to_field", fieldName));
    } else {
      fieldValue = token;
    }

  }




  /**
   * @see coyote.dx.FrameTransform#process(coyote.dataframe.DataFrame)
   */
  @Override
  public DataFrame process(final DataFrame frame) throws TransformException {

    // If there is a conditional expression
    if (getExpression() != null) {

      try {
        // if the condition evaluates to true
        if (evaluator.evaluateBoolean(getExpression())) {

          StringBuffer b = new StringBuffer();

          String value = frame.getAsString(getFieldName());

          if (value != null) {
            b.append(value);
          }
          b.append(resolveArgument(fieldValue));

          frame.put(getFieldName(), b.toString());

        }
      } catch (final IllegalArgumentException e) {
        Log.warn(LogMsg.createMsg(CDX.MSG, "Transform.Append_boolean_evaluation_error", e.getMessage()));
      }

    } else {
      // unconditionally set the value
      frame.put(getFieldName(), resolveArgument(fieldValue));
    }

    return frame;
  }

}
