/*
 * Copyright (c) 2015 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.commons.eval;

import java.util.Iterator;


/**
 * 
 */
public class ExtendedDoubleEvaluator extends DoubleEvaluator {

  private static final Function SQRT = new Function("sqrt", 1);
  private static final Parameters PARAMETERS;
  static {
    // Gets the default DoubleEvaluator's parameters
    PARAMETERS = DoubleEvaluator.getDefaultParameters();
    // add the new sqrt function to these parameters
    PARAMETERS.add(SQRT);
  }




  public ExtendedDoubleEvaluator() {
    super(PARAMETERS);
  }




  /**
   * 
   * @see coyote.commons.eval.DoubleEvaluator#evaluate(coyote.commons.eval.Function, java.util.Iterator, java.lang.Object)
   */
  @Override
  protected Double evaluate(Function function, Iterator<Double> arguments, Object evaluationContext) {
    if (function == SQRT) {
      // Implements the new function
      return Math.sqrt(arguments.next());
    } else {
      // If it's another function, pass it to DoubleEvaluator
      return super.evaluate(function, arguments, evaluationContext);
    }
  }

}
