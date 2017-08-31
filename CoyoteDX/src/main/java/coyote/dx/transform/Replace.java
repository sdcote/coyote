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
import coyote.dx.CDX;
import coyote.dx.ConfigTag;
import coyote.dx.FrameTransform;
import coyote.dx.TransformException;
import coyote.loader.cfg.Config;
import coyote.loader.cfg.ConfigurationException;
import coyote.loader.log.Log;
import coyote.loader.log.LogMsg;


/**
 * This will replace the {@code target} string with the {@code value} in the 
 * given named field.
 * 
 * <p><strong>NOTE:</strong> This will result with the field being represented 
 * as a string type in the working frame. If this is not desired, the Convert 
 * transform should be used to convert it to the desired type.
 * 
 * <p>If a {@code target} is specified, the Java String Replace method is used 
 * to perform the transformation. If the {@code pattern} attribute is used 
 * instead, the Java String ReplaceAll method is used. 
 * 
 * <p>Replace can be configured thusly:<pre>
 * "Replace":{"field":"somefield","target":"\n","value":" "}
 * </pre>
 */
public class Replace extends AbstractFieldTransform implements FrameTransform {
  private String target = null;
  private String regex = null;
  private String replacement = null;




  /**
   * @see coyote.dx.AbstractConfigurableComponent#setConfiguration(coyote.loader.cfg.Config)
   */
  @Override
  public void setConfiguration(Config cfg) throws ConfigurationException {
    super.setConfiguration(cfg);
    
    String fieldname = getConfiguration().getString(ConfigTag.FIELD);
    if( StringUtil.isBlank(fieldname)){
      throw new ConfigurationException("Transforms require a field name or pattern.");
    } else{
      fieldPattern = java.util.regex.Pattern.compile(fieldname.trim());
    }

    target = getString(ConfigTag.TARGET);
    regex = getString(ConfigTag.PATTERN);
    replacement = getString(ConfigTag.VALUE);

    if (target==null && regex==null) {
      throw new ConfigurationException("Replace transform requires a target or pattern for which to search");
    }

    if (target!=null && regex!=null) {
      throw new ConfigurationException("Replace transform requires either target or pattern attribute but not both");
    }

    if (replacement == null) {
      replacement = "";
    }
  }




  /**
   * @see coyote.dx.FrameTransform#process(coyote.dataframe.DataFrame)
   */
  @Override
  public DataFrame process(DataFrame frame) throws TransformException {
    DataFrame retval;
    if (frame != null) {
      if (getExpression() != null) {
        try {
          if (evaluator.evaluateBoolean(getExpression())) {
            retval = perfomReplace(frame);
          } else {
            retval = frame;
          }
        } catch (final IllegalArgumentException e) {
          Log.warn(LogMsg.createMsg(CDX.MSG, "Transform.boolean_evaluation_error",this.getClass().getName(), e.getMessage()));
          retval = frame;
        }
      } else {
        retval = perfomReplace(frame);
      }
    } else {
      Log.warn(LogMsg.createMsg(CDX.MSG, "Transform.no_working_frame",this.getClass().getName()));
      retval = new DataFrame();
    }
    return retval;
  }




  /**
   * Perform the actual replacement
   * 
   * @param frame the frame on which to operate
   * 
   * @return the transformed frame
   */
  private DataFrame perfomReplace(DataFrame frame) {
    DataFrame retval = new DataFrame();
    String value = null;
    for (DataField field : frame.getFields()) {
      if (field.getName() != null && fieldPattern.matcher(field.getName()).matches()) {
        value = field.getStringValue();
        if (StringUtil.isNotEmpty(value)) {
          String newval;
          if (target!=null) {
            newval = value.replace(target, replacement);
          } else {
            newval = value.replaceAll(regex, replacement);
          }
          retval.add(new DataField(field.getName(), newval));
        } else {
          retval.add(field);
        }
      } else {
        retval.add(field);
      }
    }
    return retval;
  }

}
