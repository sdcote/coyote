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

import coyote.batch.FrameTransform;
import coyote.batch.TransformException;
import coyote.dataframe.DataFrame;

/**
 * This sets the value of a field  to a particular value based on some 
 * condition.
 * 
 * <p>The expressions can use the names of values in the context as variables 
 * in the expressions. Fields in the source, working and target frames are also 
 * accessable through a simple namming convention.</p>
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
 */
public class Set extends AbstractFrameTransform implements FrameTransform {

  /**
   * 
   */
  public Set() {
    // TODO Auto-generated constructor stub
  }




  /**
   * @see coyote.batch.FrameTransform#process(coyote.dataframe.DataFrame)
   */
  @Override
  public DataFrame process( DataFrame frame ) throws TransformException {
    // TODO Auto-generated method stub
    return null;
  }

}
