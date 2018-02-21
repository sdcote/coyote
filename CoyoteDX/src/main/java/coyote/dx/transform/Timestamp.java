/*
 * Copyright (c) 2017 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.dx.transform;

import coyote.dataframe.DataFrame;
import coyote.dx.FrameTransform;
import coyote.dx.TransformException;


/**
 * Places the current data-time in the configured field.
 */
public class Timestamp extends AbstractFieldTransform implements FrameTransform {

  /**
   * @see coyote.dx.transform.AbstractFieldTransform#performTransform(coyote.dataframe.DataFrame)
   */
  @Override
  protected DataFrame performTransform(DataFrame frame) throws TransformException {
    DataFrame retval = frame;
    retval.put(getFieldName(), new java.util.Date());
    return retval;
  }

}
