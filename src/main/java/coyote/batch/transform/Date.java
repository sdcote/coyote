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
 * Convert the field from one date to another
 * 
 * empty value to a specific date (or now)
 * String to an actual date value
 * Date in one time zone to another time zone
 */
public class Date extends AbstractFieldTransform implements FrameTransform {

  /**
   * @see coyote.batch.FrameTransform#process(coyote.dataframe.DataFrame)
   */
  @Override
  public DataFrame process( final DataFrame frame ) throws TransformException {
    // TODO Auto-generated method stub
    return null;
  }

}
