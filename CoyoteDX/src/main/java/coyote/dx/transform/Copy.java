/*
 * Copyright (c) 2017 Stephan D. Cote' - All rights reserved.
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
 * "Copy": { "source": "oldField", "field":"newField"}
 * "Copy": { "source": "oldField", "target":"newField"}
 * </pre>
 */
public class Copy extends AbstractFieldTransform implements FrameTransform {

  /**
   * @see coyote.dx.transform.AbstractFieldTransform#performTransform(coyote.dataframe.DataFrame)
   */
  @Override
  protected DataFrame performTransform(DataFrame frame) throws TransformException {
    DataFrame retval = frame;
    String sourceFieldName = getSource();
    String targetFieldName = getTarget();
    if (StringUtil.isNotBlank(sourceFieldName) && StringUtil.isNotBlank(targetFieldName)) {
      DataField field = frame.getField(sourceFieldName);
      if (field != null) {
        DataField newField = (DataField)field.clone();
        newField.setName(targetFieldName);
        retval.add(newField);
      }
    }
    return retval;
  }




  private String getTarget() {
    String retval = getFieldName();
    if (StringUtil.isBlank(retval)) {
      retval = getConfiguration().getString(ConfigTag.TARGET);
    }
    return retval;
  }




  private String getSource() {
    return getConfiguration().getString(ConfigTag.SOURCE);
  }

}
