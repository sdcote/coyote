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
package coyote.batch.eval;

import coyote.batch.TransformContext;
import coyote.commons.eval.DoubleEvaluator;
import coyote.commons.eval.StaticVariableSet;


/**
 * This is a facade to the evaluation functions.
 */
public class Evaluator {
  private final StaticVariableSet<Boolean> bvs = new StaticVariableSet<Boolean>();
  private final StaticVariableSet<Double> dvs = new StaticVariableSet<Double>();
  private final BooleanEvaluator beval = new BooleanEvaluator();
  private final DoubleEvaluator deval = new DoubleEvaluator();




  /**
   * Default constructor
   */
  public Evaluator() {

  }




  /**
   * Construct an evaluator with the given operational context to be used to 
   * resolve variables.
   * 
   * @param context in which variables are to be resolved.
   */
  public Evaluator( final TransformContext context ) {
    setContext( context );
  }




  /**
   * Evaluate the given expression as a boolean expression.
   *  
   * @param expression the boolean expression to evaluate
   * 
   * @return the result of the boolean expression either true of false.
   * 
   * @throws EvaluationException if there were problems evaluating the expression
   */
  public boolean evaluateBoolean( final String expression ) throws EvaluationException {
    // use the boolean evaluator with the boolean variable set
    return beval.evaluate( expression, bvs );
  }




  /**
   * Evaluate the given expression as a numeric (double) expression.
   * 
   * <p>The returned value is of type double to allow for the widest possible 
   * set of values in the result.</p>
   *  
   * @param expression the expression to evaluate
   * 
   * @return the result of the expression
   * 
   * @throws EvaluationException if there were problems evaluating the expression
   */
  public double evaluateDouble( final String expression ) {
    // use the double evaluator with the double variable set
    return deval.evaluate( expression, dvs );
  }




  /**
   * Set the operational context in the evaluator.
   * 
  * @param context the context to set
  */
  public void setContext( final TransformContext context ) {
    beval.setContext( context );
  }

}
