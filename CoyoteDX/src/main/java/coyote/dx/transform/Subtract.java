/*
 * Copyright (c) 2017 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.dx.transform;

import coyote.commons.StringUtil;
import coyote.dataframe.DataFrame;
import coyote.dx.FrameTransform;
import coyote.dx.TransformException;
import coyote.loader.cfg.Config;
import coyote.loader.cfg.ConfigurationException;


/**
 * Subtract transformations.
 * 
 * 
 * <pre>
 * "Subtract": { "field": "Revenue", "subtrahend ": "Expenses" }
 * "Subtract": { "field": "Profit", "minuend": "Price", "subtrahend ": "Cost" }
 * "Subtract": { "field": "Profit", "minuend": "Price", "subtrahend ": "Cost", "setsymbol": false }
 * </pre>
 */
public class Subtract extends AbstractMathTransform implements FrameTransform {

  public static final String MINUEND = "minuend";
  public static final String SUBTRAHEND = "subtrahend";

  private String minuendValue = null;
  private String subtrahendValue = null;




  /**
   * @see coyote.dx.transform.AbstractFieldTransform#setConfiguration(coyote.loader.cfg.Config)
   */
  @Override
  public void setConfiguration(Config cfg) throws ConfigurationException {
    super.setConfiguration(cfg);
    setMinuend(getString(MINUEND));
    setSubtrahend(getString(SUBTRAHEND));

    if (StringUtil.isBlank(getSubtrahend())) {
      throw new ConfigurationException("Subtract transform requires a subtrahend");
    }
  }




  /**
   * @see coyote.dx.FrameTransform#process(coyote.dataframe.DataFrame)
   */
  @Override
  public DataFrame performTransform(DataFrame frame) throws TransformException {
    DataFrame retval = frame;
    String minuend;
    if (frame.contains(getMinuend())) {
      minuend = frame.getAsString(getMinuend());
    } else {
      minuend = getMinuend();
    }

    String subtrahend;
    if (frame.contains(getSubtrahend())) {
      subtrahend = frame.getAsString(getSubtrahend());
    } else {
      subtrahend = getSubtrahend();
    }


    
    
    
    
    return retval;
  }




  /**
   * @return the minuend
   */
  private String getMinuend() {
    return minuendValue;
  }




  /**
   * @param minuend the minuend to set
   */
  private void setMinuend(String minuend) {
    minuendValue = minuend;
  }




  /**
   * @return the subtrahend
   */
  private String getSubtrahend() {
    return subtrahendValue;
  }




  /**
   * @param subtrahend the subtrahend to set
   */
  private void setSubtrahend(String subtrahend) {
    subtrahendValue = subtrahend;
  }

}
