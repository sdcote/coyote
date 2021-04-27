/*
 * Copyright (c) 2015 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.dx.transform;

import coyote.commons.StringUtil;
import coyote.commons.template.Template;
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
 * <pre>"Append": { "Field": "comments", "Condition": "equals(working.Error Code,'1')", "Value" : " reason: 1-No username" }</pre>
 * <ul>
 * <li>Field - the name of the field to set. (Required)</li>
 * <li>Condition - The boolean expression which must evaluate to true foe the 
 * value to be set. (defaults to "true")</li>
 * <li>Value - The value to set in the named field if the condition evaluates 
 * to true or is omitted. (Required)</li>
 * </ul>
 * 
 * <p>{@code Condition} and {@code Value} will be treated as templates to 
 * provide a high degree of configurability to the transformation.</p>
 * 
 * <p>If only {@code Field} and {@code Value} are configured, then the transform 
 * unconditionally appends the the specified value in the named field.</p>
 */
public class Append extends AbstractFieldTransform implements FrameTransform {

  private String fieldValue = null;




  /**
   * @see coyote.dx.transform.AbstractFieldTransform#open(coyote.dx.context.TransformContext)
   */
  @Override
  public void open(final TransformContext context) {
    super.open(context);

    String token = getConfiguration().getString(ConfigTag.VALUE);
    if (token == null) {
      Log.warn(LogMsg.createMsg(CDX.MSG, "Transform.Append_setting_null_to_field", getFieldName()));
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
          // set the value
          append(frame);
        }
      } catch (final IllegalArgumentException e) {
        Log.warn(LogMsg.createMsg(CDX.MSG, "Transform.Append_boolean_evaluation_error", e.getMessage()));
      }
    } else {
      // unconditionally set the value
      append(frame);
    }
    return frame;
  }




  private void append(final DataFrame frame) {

    StringBuffer b = new StringBuffer();

    String value = frame.getAsString(getFieldName());

    if (value != null) {
      b.append(value);
    }

    // see if it resolves to a field value first
    String fval = resolveField(fieldValue);
    if (StringUtil.isBlank(fval)) {
      // not a field value, so resolve it normally
      b.append(resolveArgument(fieldValue));
    } else {
      // use the field value and treat is like a template, but pre-process it
      b.append(Template.preProcess(fval, getContext().getSymbols()));
    }

    frame.put(getFieldName(), b.toString());

  }

}
