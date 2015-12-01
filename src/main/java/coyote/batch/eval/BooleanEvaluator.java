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
import coyote.commons.eval.AbstractEvaluator;
import coyote.commons.eval.BracketPair;
import coyote.commons.eval.Constant;
import coyote.commons.eval.Function;
import coyote.commons.eval.Operator;
import coyote.commons.eval.Parameters;


/**
 * 
 */
public class BooleanEvaluator extends AbstractEvaluator<Boolean> {

  private static final String LITERAL_TRUE = "true";
  private static final String LITERAL_FALSE = "false";

  /** The transformation context from which we retrieve data */
  TransformContext transformContext = null;

  // = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = =
  // Operators
  // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
  /** The negate unary operator.*/
  public final static Operator NEGATE = new Operator( "!", 1, Operator.Associativity.RIGHT, 3 );

  /** The logical AND operator.*/
  private static final Operator AND = new Operator( "&&", 2, Operator.Associativity.LEFT, 2 );

  /** The logical OR operator.*/
  public final static Operator OR = new Operator( "||", 2, Operator.Associativity.LEFT, 1 );

  /** The standard whole set of predefined operators */
  private static final Operator[] OPERATORS = new Operator[] { NEGATE, AND, OR };
  // = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = =

  // = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = =
  // Functions
  // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
  /** Performs a case insensitive comparison between two string values*/
  public static final Function MATCH = new Function( "match", 2 );

  /** The whole set of predefined functions */
  private static final Function[] FUNCTIONS = new Function[] { MATCH };
  // = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = =

  // = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = =
  // Constants
  // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
  /** A constant that represents the current state of the isLastFrame() method call in the transaction context */
  public static final Constant LAST = new Constant( "islast" );

  /** The whole set of predefined constants */
  private static final Constant[] CONSTANTS = new Constant[] { LAST };
  // = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = =

  // Our default parameters
  private static Parameters DEFAULT_PARAMETERS;




  // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

  private static Parameters getParameters() {
    if ( DEFAULT_PARAMETERS == null ) {
      DEFAULT_PARAMETERS = getDefaultParameters();
    }
    return DEFAULT_PARAMETERS;
  }




  /**
   * Default constructor which uses the default evaluation parameters.
   */
  public BooleanEvaluator() {
    this( getParameters() );
  }




  /**
   * Our private constructor which uses the given evaluation parameters
   * 
   * @param parameters the evaluation parameters this evaluator should use
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
   * @return a Parameters instance
   */
  public static Parameters getDefaultParameters() {
    final Parameters result = new Parameters();
    result.addOperators( Arrays.asList( OPERATORS ) );
    result.addFunctions( Arrays.asList( FUNCTIONS ) );
    result.addConstants( Arrays.asList( CONSTANTS ) );
    result.addFunctionBracket( BracketPair.PARENTHESES );
    result.addExpressionBracket( BracketPair.PARENTHESES );
    return result;
  }




  /**
   * @see coyote.commons.eval.AbstractEvaluator#toValue(java.lang.String, java.lang.Object)
   */
  @Override
  protected Boolean toValue( String literal, Object evaluationContext ) {
    if ( LITERAL_TRUE.equalsIgnoreCase( literal ) || LITERAL_FALSE.equalsIgnoreCase( literal ) ) {
      return Boolean.valueOf( literal );
    } else {
      throw new IllegalArgumentException( "'"+literal + "' is not a valid boolean literal" );
    }
  }




  /**
   * @see coyote.commons.eval.AbstractEvaluator#evaluate(coyote.commons.eval.Constant, java.lang.Object)
   */
  @Override
  protected Boolean evaluate( final Constant constant, final Object evaluationContext ) {
    if ( LAST.equals( constant ) ) {
      if ( transformContext != null && transformContext.getTransaction() != null ) {
        return new Boolean( transformContext.getTransaction().isLastFrame() );
      } else {
        return new Boolean( false );
      }
    } else {
      return super.evaluate( constant, evaluationContext );
    }
  }




  /**
   * @see coyote.commons.eval.AbstractEvaluator#evaluate(coyote.commons.eval.Operator, java.util.Iterator, java.lang.Object)
   */
  @Override
  protected Boolean evaluate( Operator operator, Iterator<Boolean> operands, Object evaluationContext ) {
    if ( operator == NEGATE ) {
      return !operands.next();
    } else if ( operator == OR ) {
      Boolean o1 = operands.next();
      Boolean o2 = operands.next();
      return o1 || o2;
    } else if ( operator == AND ) {
      Boolean o1 = operands.next();
      Boolean o2 = operands.next();
      return o1 && o2;
    } else {
      return super.evaluate( operator, operands, evaluationContext );
    }
  }




  public void setContext( TransformContext context ) {
    transformContext = context;
  }

}
