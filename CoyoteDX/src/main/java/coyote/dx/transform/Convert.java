/*
 * Copyright (c) 2015 Stephan D. Cote' - All rights reserved.
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
 * Attempt to perform an in-place type conversion.
 * 
 * <p>This is useful when data is read in as a string and we need to to be 
 * numeric, boolean or a date.</p>
 */
public class Convert extends AbstractFieldTransform implements FrameTransform {

  /**
   * @see coyote.dx.FrameTransform#process(coyote.dataframe.DataFrame)
   */
  @Override
  public DataFrame process(final DataFrame frame) throws TransformException {
    return null;
  }

}
