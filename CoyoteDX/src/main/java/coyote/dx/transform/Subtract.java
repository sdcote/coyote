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
    long min = getLong(minuend);
    long sub = getLong(subtrahend);
    retval = min - sub;
    return retval;
  }




  private long getLong(String token) throws TransformException {
    long retval = 0L;
    try {
      retval = Long.parseLong(token);
    } catch (Exception e) {
      throw new TransformException("Token '" + token + "' could not be converted into a long");
    }
    return retval;
  }




  private double getDouble(String token) throws TransformException {
    double retval = 0D;
    try {
      retval = Double.parseDouble(token);
    } catch (Exception e) {
      throw new TransformException("Token '" + token + "' could not be converted into a double");
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
   * @param val the minuend to set
   */
  private void setMinuend(String val) {
    minuendValue = val;
  }




  /**
   * @return the subtrahend
   */
  private String getSubtrahend() {
    return subtrahendValue;
  }




  /**
   * @param val the subtrahend to set
   */
  private void setSubtrahend(String val) {
    subtrahendValue = val;
  }

}
