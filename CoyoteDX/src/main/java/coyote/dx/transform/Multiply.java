/*
 * Copyright (c) 2017 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.dx.transform;

import java.math.BigDecimal;

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
 * Multiply a field by a factor.
 * 
 * <p>If there is a source attribute that field will be used as a factor and 
 * the result is placed in the field.
 * 
 * <pre>
 * "Multiply": { "field": "TotalCost", "factor": "100" }
 * "Multiply": { "field": "TotalCost", "source": "Count", "factor": "Cost", "setsymbol": false }
 * </pre>
 */
public class Multiply extends AbstractMathTransform implements FrameTransform {

  public static final String FACTOR = "factor";

  private String source = null;
  private String factor = null;




  /**
   * @see coyote.dx.transform.AbstractFieldTransform#setConfiguration(coyote.loader.cfg.Config)
   */
  @Override
  public void setConfiguration(Config cfg) throws ConfigurationException {
    super.setConfiguration(cfg);
    setSource(getString(ConfigTag.SOURCE));
    setFactor(getString(FACTOR));
    if (StringUtil.isBlank(getFactor())) {
      throw new ConfigurationException("Multiply transform requires a multiplication factor");
    }
  }




  /**
   * @see coyote.dx.FrameTransform#process(coyote.dataframe.DataFrame)
   */
  @Override
  public DataFrame performTransform(DataFrame frame) throws TransformException {
    DataFrame retval = frame;
    DataField field;
    Log.debug("MULTIPLY<" + frame.toString());

    String fieldString;
    if (StringUtil.isNotBlank(getSource())) {
      field = frame.getField(getSource());
      if (field == null) {
        Log.warn("Could not retrive the source field '" + getSource() + "' setting to zero");
        fieldString = "0";
      } else {
        fieldString = field.getStringValue();
      }
    } else {
      field = frame.getField(getFieldName());
      if (field == null) {
        Log.warn("Could not retrive the field '" + getFieldName() + "' setting to zero");
        fieldString = "0";
      } else {
        fieldString = field.getStringValue();
      }
    }

    String factorString = null;
    if (frame.contains(getFactor())) {
      factorString = frame.getAsString(getFactor());
    } else {
      factorString = getFactor();
    }

    if (isFractional(factorString) || isFractional(fieldString)) {
      double result = multiplyDouble(field, factorString);
      retval.put(getFieldName(), result);
      if (isSetSymbol()) {
        getContext().getSymbols().put(getFieldName(), Double.toString(result));
      }
    } else {
      long result = multiplyLong(field, factorString);
      retval.put(getFieldName(), result);
      if (isSetSymbol()) {
        getContext().getSymbols().put(getFieldName(), Long.toString(result));
      }
    }
    Log.debug("MULTIPLY>" + retval.toString());
    return retval;
  }




  private long multiplyLong(DataField field, String factor) throws TransformException {
    long retval = 0L;
    long val = getAsLong(field);
    long ftr = getLongNumber(factor);
    retval = val * ftr;
    return retval;
  }




  private double multiplyDouble(DataField field, String factor) throws TransformException {
    double retval = 0D;
    BigDecimal val = new BigDecimal(field.getStringValue());
    BigDecimal ftr = new BigDecimal(factor);
    retval = val.multiply(ftr).doubleValue();
    return retval;
  }




  /**
   * Retrieve the multiplication factor for this transform.
   * 
   * @return the factor for the multiplication
   */
  private String getFactor() {
    return factor;
  }




  /**
   * Set the multiplication factor for this transform.
   * 
   * @param factor the factor to set
   */
  public void setFactor(String factor) {
    this.factor = factor;
  }




  /**
   * Retrieve the source field of the multiplication operation.
   *  
   * @return the source field for the multiplication
   */
  public String getSource() {
    return source;
  }




  /**
   * Test the source factor of the multiplication operation.
   * 
   * @param src the source field to set
   */
  public void setSource(String src) {
    source = src;
  }

}
