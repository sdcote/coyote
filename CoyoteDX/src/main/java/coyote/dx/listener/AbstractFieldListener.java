/*
 * Copyright (c) 2017 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.dx.listener;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.text.DecimalFormat;

import coyote.commons.StringUtil;
import coyote.dataframe.DataField;
import coyote.dx.CDX;
import coyote.dx.ConfigTag;
import coyote.dx.context.ContextListener;
import coyote.dx.context.TransactionContext;
import coyote.loader.cfg.Config;
import coyote.loader.cfg.ConfigurationException;
import coyote.loader.log.Log;
import coyote.loader.log.LogMsg;


/**
 * 
 */
public abstract class AbstractFieldListener extends AbstractListener implements ContextListener {
  protected static final MathContext MATH_CONTEXT = new MathContext(32, RoundingMode.HALF_UP);
  protected static final DecimalFormat FORMATTER = new DecimalFormat();

  private String fieldName = null;

  static {
    FORMATTER.setMaximumFractionDigits(8);
    FORMATTER.setMinimumFractionDigits(0);
    FORMATTER.setGroupingUsed(false);
    FORMATTER.setRoundingMode(RoundingMode.HALF_EVEN);
  }




  /**
   * @see coyote.dx.AbstractConfigurableComponent#setConfiguration(coyote.loader.cfg.Config)
   */
  @Override
  public void setConfiguration(Config cfg) throws ConfigurationException {
    super.setConfiguration(cfg);

    String fname = getConfiguration().getString(ConfigTag.FIELD);
    if (StringUtil.isBlank(fname)) {
      throw new ConfigurationException("Field Listeners require a field name or pattern.");
    } else {
      setFieldName(fname.trim());
    }

  }




  /**
   * @see coyote.dx.listener.AbstractListener#onMap(coyote.dx.context.TransactionContext)
   */
  @Override
  public void onMap(TransactionContext txnContext) {
    super.onMap(txnContext);

    if (isEnabled()) {
      if (getCondition() != null) {
        try {
          if (evaluator.evaluateBoolean(getCondition())) {
            process(txnContext);
          } else {
            if (Log.isLogging(Log.DEBUG_EVENTS)) {
              Log.debug(LogMsg.createMsg(CDX.MSG, "Listener.boolean_evaluation_false", getCondition()));
            }
          }
        } catch (final IllegalArgumentException e) {
          Log.error(LogMsg.createMsg(CDX.MSG, "Listener.boolean_evaluation_error", getCondition(), e.getMessage()));
        }
      } else {
        process(txnContext);
      }
    }
  }




  /**
   * @param txnContext
   */
  protected abstract void process(TransactionContext txnContext);




  /**
   * @return the name of the field being transformed
   */
  protected String getFieldName() {
    return fieldName;
  }




  /**
   * @param name the name of the field being transformed
   */
  protected void setFieldName(String name) {
    fieldName = name;
  }




  /**
   * @return the value of the targeted field or null if not a number (NaN or unparseable)
   */
  protected BigDecimal getFieldValue(TransactionContext context) {
    BigDecimal retval = null;
    if (context != null) {
      if (context.getTargetFrame() != null) {
        DataField field = context.getTargetFrame().getField(getFieldName());
        if (field != null) {
          try {
            switch (field.getType()) {
              case DataField.DOUBLE:
                retval = new BigDecimal((double)field.getObjectValue());
                break;
              case DataField.FLOAT:
                retval = new BigDecimal((float)field.getObjectValue());
                break;
              case DataField.S64:
              case DataField.U32:
                retval = new BigDecimal((long)field.getObjectValue());
                break;
              case DataField.S32:
              case DataField.U16:
                retval = new BigDecimal((int)field.getObjectValue());
                break;
              case DataField.S16:
              case DataField.U8:
                retval = new BigDecimal((short)field.getObjectValue());
                break;
              case DataField.STRING:
                retval = new BigDecimal((String)field.getObjectValue());
                break;
              default:
                Log.error("Data field '" + field.getName() + "' of type : " + field.getTypeName() + " cannot be formatted to a decimal value");
                break;
            }
          } catch (NumberFormatException nfe) {
            Log.error("Could not convert field '" + field.getName() + "' to a decimal value: " + nfe.getMessage());
          }
        }
      }
    } else {
      Log.warn(LogMsg.createMsg(CDX.MSG, "Listener.context not set"));
    }
    return retval;
  }




  private double getDouble(DataField field) {
    double retval = -1D;
    if (field != null && field.isNotNull()) {
      Object val = field.getObjectValue();

      if (val instanceof Double) {
        retval = new BigDecimal(((Double)val).doubleValue(), MATH_CONTEXT).doubleValue();
      } else {
        try {
          retval = new BigDecimal(val.toString(), MATH_CONTEXT).doubleValue();
        } catch (Exception e) {
          Log.error(this.getClass().getSimpleName() + " could not parse '" + val.toString() + "' into a double value");
        }
      }
    }
    return retval;
  }
}
