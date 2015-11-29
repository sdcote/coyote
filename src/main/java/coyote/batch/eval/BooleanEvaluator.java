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

import coyote.commons.eval.AbstractEvaluator;
import coyote.commons.eval.BracketPair;
import coyote.commons.eval.Parameters;


/**
 * 
 */
public class BooleanEvaluator extends AbstractEvaluator<Boolean> {

  private static Parameters DEFAULT_PARAMETERS;




  private static Parameters getParameters() {
    if ( DEFAULT_PARAMETERS == null ) {
      DEFAULT_PARAMETERS = getDefaultParameters();
    }
    return DEFAULT_PARAMETERS;
  }




  /**
   * 
   */
  public BooleanEvaluator() {
    this( getParameters() );
  }




  /**
   * @param parameters
   */
  private BooleanEvaluator( Parameters parameters ) {
    super( parameters );
    // TODO Auto-generated constructor stub
  }




  /**
   * Gets a copy of the default parameters.
   * 
   * <p>The returned parameters contains all the predefined operators, 
   * functions and constants.</p>
   * 
   * <p>Each call to this method create a new instance of Parameters.</p>
   *  
   * @return a Paramaters instance
   */
  public static Parameters getDefaultParameters() {
    final Parameters result = new Parameters();
    //    result.addOperators( Arrays.asList( OPERATORS ) );
    //    result.addFunctions( Arrays.asList( FUNCTIONS ) );
    //    result.addConstants( Arrays.asList( CONSTANTS ) );
    result.addFunctionBracket( BracketPair.PARENTHESES );
    result.addExpressionBracket( BracketPair.PARENTHESES );
    return result;
  }




  /**
   * @see coyote.commons.eval.AbstractEvaluator#toValue(java.lang.String, java.lang.Object)
   */
  @Override
  protected Boolean toValue( String literal, Object evaluationContext ) {
    // TODO Auto-generated method stub
    return null;
  }

}
