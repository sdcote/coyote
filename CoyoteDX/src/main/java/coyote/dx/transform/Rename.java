/*
 * Copyright (c) 2021 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.dx.transform;

import coyote.commons.StringUtil;
import coyote.dataframe.DataField;
import coyote.dataframe.DataFrame;
import coyote.dx.ConfigTag;
import coyote.dx.FrameTransform;
import coyote.dx.TransformException;


/**
 * Copy one field to another.
 * 
 * <p>This can be configured thusly:<pre>
 * "Rename": { "source": "oldField", "field":"newField"}
 * "Rename": { "source": "oldField", "target":"newField"}
 * </pre>
 */
public class Rename extends Copy implements FrameTransform {

  /**
   * @see AbstractFieldTransform#performTransform(DataFrame)
   */
  @Override
  protected DataFrame performTransform(DataFrame frame) throws TransformException {
    DataFrame retval = frame;
    String sourceFieldName = getSource();
    String targetFieldName = getTarget();
    if (StringUtil.isNotBlank(sourceFieldName) && StringUtil.isNotBlank(targetFieldName)) {
      DataField field = frame.getField(sourceFieldName);
      if (field != null) {
        field.setName(targetFieldName);
      }
    }
    return retval;
  }

}
