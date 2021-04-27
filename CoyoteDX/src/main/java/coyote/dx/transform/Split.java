/*
 * Copyright (c) 2015 Stephan D. Cote' - All rights reserved.
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
import coyote.loader.cfg.Config;
import coyote.loader.cfg.ConfigurationException;
import coyote.loader.log.Log;


/**
 * Split a fild into multiple fields
 * 
 * <p>This is useful when data such as a timestamp needs to be split into a 
 * date and a time field.
 * <p>Consider the datetime string of {@code 2017-11-02T10:21:32.076-0400}, 
 * where we want two fields one with and the other with {@code 2017-11-02} and 
 * the other with {@code 10:21:32.076}. We would first split on the 'T' then 
 * again on the '-' to remove the timezone.
 */
public class Split extends AbstractFieldTransform implements FrameTransform {
  private String delimiter = null;




  /**
   * @see coyote.dx.transform.AbstractFieldTransform#setConfiguration(coyote.loader.cfg.Config)
   */
  @Override
  public void setConfiguration(Config cfg) throws ConfigurationException {
    super.setConfiguration(cfg);

    delimiter = getConfiguration().getString(ConfigTag.DELIMITER);
    if (StringUtil.isBlank(delimiter)) {
      throw new ConfigurationException("Missing required '" + ConfigTag.DELIMITER + "' configuration parameter");
    }

  }




  /**
   * @see coyote.dx.FrameTransform#process(coyote.dataframe.DataFrame)
   */
  @Override
  public DataFrame process(final DataFrame frame) throws TransformException {
    DataFrame retval = frame;

    DataField field = retval.getField(getFieldName());
    if (field == null) {
      Log.warn("Could not retrive the field '" + getFieldName() + "'");
    } else {
      String data = field.getStringValue();
      if (data != null) {
        String[] tokens = data.split(getDelimiter());
        for (int x = 0; x < tokens.length; x++) {
          retval.put(field.getName() + "." + x, tokens[x]);
        }
      }
    }

    return retval;
  }




  /**
   * @return the delimiter
   */
  public String getDelimiter() {
    return delimiter;
  }




  /**
   * @param delimiter the delimiter to set
   */
  public void setDelimiter(String delimiter) {
    this.delimiter = delimiter;
  }

}
