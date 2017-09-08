/*
 * Copyright (c) 2017 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.dx.transform;

import coyote.commons.GUID;
import coyote.dataframe.DataFrame;
import coyote.dx.ConfigTag;
import coyote.dx.FrameTransform;
import coyote.dx.TransformException;
import coyote.loader.cfg.Config;
import coyote.loader.cfg.ConfigurationException;


/**
 * Place a random globally unique identifier (GUID) in the named field.
 * 
 * <p>This can be configured thusly:<pre>
 * "Guid": { "field": "RecordId"}
 * "Guid": { "field": "RecordId", "secure": true }</pre>
 */
public class Guid extends AbstractFieldTransform implements FrameTransform {
  private boolean secure = false;




  private boolean isSecure() {
    return secure;
  }




  /**
   * @see coyote.dx.transform.AbstractFieldTransform#setConfiguration(coyote.loader.cfg.Config)
   */
  @Override
  public void setConfiguration(Config cfg) throws ConfigurationException {
    super.setConfiguration(cfg);

    if (cfg.containsIgnoreCase(ConfigTag.SECURE)) {
      try {
        secure = cfg.getBoolean(ConfigTag.SECURE);
      } catch (Throwable ball) {
        throw new ConfigurationException("Invalid boolean value");
      }
    }
  }




  /**
   * @see coyote.dx.transform.AbstractFieldTransform#performTransform(coyote.dataframe.DataFrame)
   */
  @Override
  protected DataFrame performTransform(DataFrame frame) throws TransformException {
    DataFrame retval = frame;
    String guid;
    if (isSecure()) {
      guid = GUID.randomSecureGUID().toString();
    } else {
      guid = GUID.randomGUID().toString();
    }
    retval.put(getFieldName(), guid);
    return retval;
  }

}
