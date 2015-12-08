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
 * Attempt to perform an in-place type conversion.
 * 
 * <p>This is useful when data is read in as a string and we need to to be 
 * numeric, boolean or a date.</p>
 */
public class Type extends AbstractFrameTransform implements FrameTransform {

  /**
   * @see coyote.batch.FrameTransform#process(coyote.dataframe.DataFrame)
   */
  @Override
  public DataFrame process( DataFrame frame ) throws TransformException {
    // TODO Auto-generated method stub
    return null;
  }

}
