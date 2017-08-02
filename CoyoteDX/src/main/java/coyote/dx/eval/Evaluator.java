/*
 * Copyright (c) 2015 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.dx.eval;

import coyote.dx.context.TransformContext;


/**
 * This is a facade to the evaluation functions.
 */
public class Evaluator {
  private final BooleanEvaluator beval = new BooleanEvaluator();
  private final NumericEvaluator neval = new NumericEvaluator();




  // TODO: will probably need a StringEvaluator with methods, constants and literals which allows data to be concatenated and to access substring, etc.
  // private final StringEvaluator seval = new StringEvaluator();

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
   * @throws IllegalArgumentException if there were problems evaluating the expression
   */
  public boolean evaluateBoolean( final String expression ) throws IllegalArgumentException {
    return beval.evaluate( expression );
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
   * @throws IllegalArgumentException if there were problems evaluating the expression
   */
  public double evaluateNumeric( final String expression ) {
    return neval.evaluate( expression );
  }




  /**
   * Evaluate the given expression as a string expression.
   * 
   * <p>This allows the expression to evaluated into a string with many 
   * methods, constants and support for literals. String expressions have 
   * complete access to the transform and current transaction context with 
   * access to each field in the source, working and target frames.</p>
   *  
   * @param expression the expression to evaluate
   * 
   * @return the result of the expression
   * 
   * @throws EvaluationException if there were problems evaluating the expression
   */
  //  public String evaluateString( final String expression ) {
  //    return seval.evaluate( expression );
  //  }

  /**
   * Set the operational context in the evaluator.
   * 
  * @param context the context to set
  */
  public void setContext( final TransformContext context ) {
    beval.setContext( context );
    neval.setContext( context );
    // TODO: seval.setContext( context );
  }

}
