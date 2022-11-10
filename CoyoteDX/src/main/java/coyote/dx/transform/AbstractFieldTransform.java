/*
 * Copyright (c) 2015 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.dx.transform;

import coyote.commons.StringUtil;
import coyote.dataframe.DataFrame;
import coyote.dx.CDX;
import coyote.dx.ConfigTag;
import coyote.dx.FrameTransform;
import coyote.dx.TransformException;
import coyote.dx.context.TransformContext;
import coyote.dx.eval.Evaluator;
import coyote.loader.cfg.Config;
import coyote.loader.cfg.ConfigurationException;
import coyote.loader.log.Log;
import coyote.loader.log.LogMsg;


/**
 * 
 */
public abstract class AbstractFieldTransform extends AbstractFrameTransform implements FrameTransform {

  protected java.util.regex.Pattern fieldPattern = null;
  private String fieldName = null;
  protected Evaluator evaluator = new Evaluator();
  private String expression = null;
  protected boolean setSymbol = false;




  /**
   * @see coyote.dx.AbstractConfigurableComponent#setConfiguration(coyote.loader.cfg.Config)
   */
  @Override
  public void setConfiguration(Config cfg) throws ConfigurationException {
    super.setConfiguration(cfg);

    String fname = getConfiguration().getString(ConfigTag.FIELD);
    if (StringUtil.isBlank(fname)) {
      fname = getConfiguration().getString(ConfigTag.TARGET);
    }

    if (StringUtil.isBlank(fname)) {
      throw new ConfigurationException("Transforms require a field name or pattern");
    } else {
      setFieldName(fname.trim());
      fieldPattern = java.util.regex.Pattern.compile(fname.trim());
    }

    if (getConfiguration().containsIgnoreCase(ConfigTag.SET_SYMBOL)) {
      try {
        setSymbol = getConfiguration().getBoolean(ConfigTag.SET_SYMBOL);
      } catch (NumberFormatException e) {
        throw new ConfigurationException("SetSymbol attribute did not contain a valid boolean value");
      }
    }

    // Look for a conditional statement the transform may use to control if it
    // processes the frame or not
    String token = getConfiguration().getString(ConfigTag.CONDITION);
    if (StringUtil.isNotBlank(token)) {
      expression = token.trim();
    }

  }




  /**
   * @see coyote.dx.Component#open(coyote.dx.context.TransformContext)
   */
  @Override
  public void open(final TransformContext context) {
    super.setContext(context);

    // set the transform context in the evaluator so it can resolve variables
    evaluator.setContext(context);

  }




  /**
   * Subclasses should probably override {@link #performTransform(DataFrame)} instead of 
   * this method so as to enable this class to handle conditional checks.
   * 
   * @see coyote.dx.FrameTransform#process(coyote.dataframe.DataFrame)
   */
  @Override
  public DataFrame process(DataFrame frame) throws TransformException {
    DataFrame retval = null;
    if (isEnabled()) {
      if (getCondition() != null) {
        try {
          if (evaluator.evaluateBoolean(getCondition())) {
            retval = performTransform(frame);
          } else {
            if (Log.isLogging(Log.DEBUG_EVENTS)) {
              Log.debug(LogMsg.createMsg(CDX.MSG, "Transform.boolean_evaluation_false", getCondition()));
            }
          }
        } catch (final IllegalArgumentException e) {
          Log.error(LogMsg.createMsg(CDX.MSG, "Transform.boolean_evaluation_error", getCondition(), e.getMessage()));
        }
      } else {
        retval = performTransform(frame);
      }
    }
    return retval;
  }




  /**
   * This method is called by the AbstractFieldTransform when the 
   * {@link #process(DataFrame)} method is called but only if the enabled flag 
   * is set and any set conditional expression is matched or there is no 
   * conditional expression.
   * 
   * <p>Overriding this transform instead of {@link #process(DataFrame)} allows the 
   * AbstractFieldTransform to handle all checks in a uniform manner for all 
   * subclasses.
   * 
   * @throws TransformException
   */
  protected DataFrame performTransform(DataFrame frame) throws TransformException {
    // do nothing method
    return null;
  }




  /**
   * Return the conditional expression from the configuration.
   * 
   * @return the condition which must evaluate to true before the task is to 
   *         execute.
   */
  public String getCondition() {
    if (configuration.containsIgnoreCase(ConfigTag.CONDITION)) {
      return configuration.getString(ConfigTag.CONDITION);
    }
    return null;
  }




  /**
   * @return the name of the field being transformed
   */
  protected String getFieldName() {
    return fieldName;
  }




  /**
   * @param fieldName the name of the field being transformed
   */
  protected void setFieldName(String fieldName) {
    this.fieldName = fieldName;
  }




  /**
   * @return the evaluator for this transform
   */
  protected Evaluator getEvaluator() {
    return evaluator;
  }




  /**
   * @param evaluator the evaluator for this transform
   */
  protected void setEvaluator(Evaluator evaluator) {
    this.evaluator = evaluator;
  }




  /**
   * @return the boolean expression being used to determine if the transform should occur
   */
  protected String getExpression() {
    return expression;
  }




  /**
   * @param expression the boolean expression to use in determining if the transform should occur
   */
  protected void setExpression(String expression) {
    this.expression = expression;
  }




  /**
   * Indicates if the transform is to set a symbol in the transform context 
   * symbol table with the results of the transform.
   * 
   * <p>The default is false.
   * 
   * @return true if the transform is to set a symbol in the symbol table with 
   *         the same filed name, false otherwise.
   */
  public boolean isSetSymbol() {
    return setSymbol;
  }

}
