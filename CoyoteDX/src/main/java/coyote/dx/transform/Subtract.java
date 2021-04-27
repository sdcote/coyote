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
import coyote.loader.log.Log;


/**
 * Subtract transformations.
 * 
 * <p>Simply subtract one field from another or use literals if desired.
 * 
 * This can be configured thusly:<pre>
 * "Subtract": { "field": "Revenue", "subtrahend ": "Expenses" }
 * "Subtract": { "field": "Revenue", "subtrahend ": "3.50" }
 * "Subtract": { "field": "Profit", "minuend": "Price", "subtrahend ": "Cost" }
 * "Subtract": { "field": "Profit", "minuend": "Price", "subtrahend ": "3.50" }
 * "Subtract": { "field": "Profit", "minuend": "Price", "subtrahend ": "Cost", "setsymbol": false }
 * "Subtract": { "field": "Profit", "minuend": "71.25", "subtrahend ": "Cost", "setsymbol": false }
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

    if (StringUtil.isEmpty(getMinuend())) {
      setMinuend(getFieldName());
    }

  }




  /**
   * @see coyote.dx.FrameTransform#process(coyote.dataframe.DataFrame)
   */
  @SuppressWarnings("unchecked")
  @Override
  public DataFrame performTransform(DataFrame frame) throws TransformException {
    DataFrame retval = frame;
    Log.debug("SUBTRACT<" + frame.toString());

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

    if (isFractional(minuend) || isFractional(subtrahend)) {
      double result = subtractDouble(minuend, subtrahend);
      retval.put(getFieldName(), result);
      if (isSetSymbol()) {
        getContext().getSymbols().put(getFieldName(), Double.toString(result));
      }
    } else {
      long result = subtractLong(minuend, subtrahend);
      retval.put(getFieldName(), result);
      if (isSetSymbol()) {
        getContext().getSymbols().put(getFieldName(), Long.toString(result));
      }
    }
    Log.debug("SUBTRACT>" + retval.toString());
    return retval;
  }




  private double subtractDouble(String minuend, String subtrahend) throws TransformException {
    double retval = 0L;
    double min = getDouble(minuend);
    double sub = getDouble(subtrahend);
    retval = min - sub;
    return retval;
  }




  private long subtractLong(String minuend, String subtrahend) throws TransformException {
    long retval = 0L;
    long min = getLongNumber(minuend);
    long sub = getLongNumber(subtrahend);
    retval = min - sub;
    return retval;
  }




  private String getMinuend() {
    return minuendValue;
  }




  private void setMinuend(String val) {
    minuendValue = val;
  }




  private String getSubtrahend() {
    return subtrahendValue;
  }




  private void setSubtrahend(String val) {
    subtrahendValue = val;
  }

}
