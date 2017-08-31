/*
 * Copyright (c) 2015 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.dx.transform;

import coyote.commons.StringUtil;
import coyote.dx.ConfigTag;
import coyote.dx.FrameTransform;
import coyote.dx.context.TransformContext;
import coyote.dx.eval.Evaluator;
import coyote.loader.cfg.Config;
import coyote.loader.cfg.ConfigurationException;


/**
 * 
 */
public abstract class AbstractFieldTransform extends AbstractFrameTransform implements FrameTransform {

  protected java.util.regex.Pattern fieldPattern = null;
  protected String fieldName = null;
  protected Evaluator evaluator = new Evaluator();
  protected String expression = null;




  /**
   * @see coyote.dx.AbstractConfigurableComponent#setConfiguration(coyote.loader.cfg.Config)
   */
  @Override
  public void setConfiguration(Config cfg) throws ConfigurationException {
    super.setConfiguration(cfg);

    fieldName = getConfiguration().getString(ConfigTag.FIELD);
    if (StringUtil.isBlank(fieldName)) {
      throw new ConfigurationException("Transforms require a field name or pattern.");
    } else {
      fieldPattern = java.util.regex.Pattern.compile(fieldName.trim());
    }

    // Look for a conditional statement the transform may use to control if it
    // processes or not
    String token = getConfiguration().getString(ConfigTag.CONDITION);
    if (StringUtil.isNotBlank(token)) {
      expression = token.trim();

      try {
        evaluator.evaluateBoolean(expression);
      } catch (final IllegalArgumentException e) {
        context.setError("Invalid boolean expression in transform: " + e.getMessage());
      }
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

}
