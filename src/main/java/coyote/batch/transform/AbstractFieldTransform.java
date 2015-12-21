/*
 * Copyright (c) 2015 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 *
 * Contributors:
 *   Stephan D. Cote 
 *      - Initial concept and initial implementation
 */
package coyote.batch.transform;

import coyote.batch.ConfigTag;
import coyote.batch.FrameTransform;
import coyote.batch.TransformContext;
import coyote.batch.eval.EvaluationException;
import coyote.batch.eval.Evaluator;
import coyote.commons.StringUtil;


/**
 * 
 */
public abstract class AbstractFieldTransform extends AbstractFrameTransform implements FrameTransform {

  // The name of the field we are to transform
  protected String fieldName = null;

  protected Evaluator evaluator = new Evaluator();
  protected String expression = null;




  /**
   * @see coyote.batch.Component#open(coyote.batch.TransformContext)
   */
  @Override
  public void open( final TransformContext context ) {
    super.setContext( context );

    // set the transform context in the evaluator so it can resolve variables
    evaluator.setContext( context );

    // get the name of the field to transform
    String token = findString( ConfigTag.NAME );

    if ( StringUtil.isBlank( token ) ) {
      context.setError( "Set transform must contain a field name" );
    } else {
      fieldName = token.trim();
    }

    // Look for a conditional statement the transform may use to control if it
    // processes or not
    token = findString( ConfigTag.CONDITION );
    if ( StringUtil.isNotBlank( token ) ) {
      expression = token.trim();

      try {
        evaluator.evaluateBoolean( expression );
      } catch ( final EvaluationException e ) {
        context.setError( "Invalid boolean expression in transform: " + e.getMessage() );
      }
    }

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
  protected void setFieldName( String fieldName ) {
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
  protected void setEvaluator( Evaluator evaluator ) {
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
  protected void setExpression( String expression ) {
    this.expression = expression;
  }

}