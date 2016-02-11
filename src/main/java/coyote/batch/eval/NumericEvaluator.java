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

import java.util.Arrays;
import java.util.Iterator;

import coyote.batch.TransformContext;
import coyote.commons.eval.DoubleEvaluator;
import coyote.commons.eval.Method;
import coyote.commons.eval.Parameters;


/**
 * 
 */
public class NumericEvaluator extends DoubleEvaluator {

  // = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = =
  // Methods
  // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

  /** Performs a case sensitive comparison between two string values */
  public static final Method DUMMY = new Method( "dummy", 2 );

  /** The whole set of predefined functions */
  private static final Method[] METHODS = new Method[] { DUMMY };
  // = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = =

  /** The transformation context from which we retrieve data */
  TransformContext transformContext = null;




  /**
   * 
   */
  public NumericEvaluator() {
    this( getParameters() );
  }




  public NumericEvaluator( Parameters parameters ) {
    super( parameters );
    getExtraParameters();
  }




  /**
   * Gets a copy of the extra parameters we can support.
   * @return a Parameters instance
   */
  private static Parameters getExtraParameters() {
    final Parameters retval = new Parameters();
    retval.addMethods( Arrays.asList( METHODS ) );
    return retval;
  }




  /**
   * Set the context in which methods will resolve their data.
   * 
   * @param context The transformation context all components share
   */
  public void setContext( TransformContext context ) {
    transformContext = context;
  }




  /**
   * @see coyote.commons.eval.AbstractEvaluator#evaluate(coyote.commons.eval.Method, java.util.Iterator, java.lang.Object)
   */
  @Override
  protected Double evaluate( Method method, Iterator<String> arguments, Object evaluationContext ) {
    return 0.;
  }

}
