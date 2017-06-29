/*
 * Copyright (c) 2015 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 *
 * Contributors:
 *   Stephan D. Cote 
 *      - Initial concept and implementation
 */
package coyote.dx.eval;

import java.util.Arrays;
import java.util.Iterator;

import coyote.commons.StringUtil;
import coyote.commons.eval.AbstractEvaluator;
import coyote.commons.eval.BracketPair;
import coyote.commons.eval.Constant;
import coyote.commons.eval.Method;
import coyote.commons.eval.Operator;
import coyote.commons.eval.Parameters;
import coyote.dx.context.TransformContext;


/**
 * 
 */
public class BooleanEvaluator extends AbstractEvaluator<Boolean> {

  private static final String LITERAL_TRUE = "true";
  private static final String LITERAL_FALSE = "false";

  /** The transformation context from which we retrieve data */
  TransformContext transformContext = null;

  // Operators
  /** The negate unary operator.*/
  public final static Operator NEGATE = new Operator( "!", 1, Operator.Associativity.RIGHT, 3 );

  /** The logical AND operator.*/
  private static final Operator AND = new Operator( "&&", 2, Operator.Associativity.LEFT, 2 );

  /** The logical OR operator.*/
  public final static Operator OR = new Operator( "||", 2, Operator.Associativity.LEFT, 1 );

  /** The logical Equals operator.*/
  public final static Operator EQUAL = new Operator( "==", 2, Operator.Associativity.LEFT, 2 );

  /** The standard whole set of predefined operators */
  private static final Operator[] OPERATORS = new Operator[] { NEGATE, AND, OR, EQUAL };

  // Methods
  /** Performs a case sensitive comparison between two string values */
  public static final Method EQUALS = new Method( "equals", 2 );

  /** Performs a regular expression match on the value of a field */
  public static final Method REGEX = new Method( "regex", 2 );

  /** Performs a case insensitive comparison between two string values*/
  public static final Method MATCH = new Method( "match", 2 );

  /** Checks if the given field contains a value */
  public static final Method EMPTY = new Method( "empty", 1 );

  /** Checks if the given field exists in the context */
  public static final Method EXISTS = new Method( "exists", 1 );

  /** The whole set of predefined functions */
  private static final Method[] METHODS = new Method[] { MATCH, EMPTY, EXISTS, REGEX, EQUALS };

  // Constants
  /** A constant that represents the current state of the isLastFrame() method call in the transaction context */
  public static final Constant LAST = new Constant( "islast" );

  /** A constant that represents the current error state of the transform context */
  public static final Constant CONTEXT_ERROR = new Constant( "contextError" );

  /** A constant that represents the current error state of the transaction context */
  public static final Constant TRANSACTION_ERROR = new Constant( "transactionError" );

  /** A constant that represents the state of no rows processed (i.e. currentRow=0) */
  public static final Constant NO_ROWS_PROCESSED = new Constant( "noRowsProcessed" );

  /** The whole set of predefined constants */
  private static final Constant[] CONSTANTS = new Constant[] { LAST, CONTEXT_ERROR, TRANSACTION_ERROR, NO_ROWS_PROCESSED };

  // Constants for use within function calls
  private static final String CURRENT_ROW = "currentRow";

  // Our default parameters
  private static Parameters DEFAULT_PARAMETERS;




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
    final Parameters retval = new Parameters();
    retval.addOperators( Arrays.asList( OPERATORS ) );
    retval.addMethods( Arrays.asList( METHODS ) );
    retval.addConstants( Arrays.asList( CONSTANTS ) );
    retval.addFunctionBracket( BracketPair.PARENTHESES );
    retval.addExpressionBracket( BracketPair.PARENTHESES );
    return retval;
  }




  /**
   * Return the value of a literal.
   * 
   * @see coyote.commons.eval.AbstractEvaluator#toValue(java.lang.String, java.lang.Object)
   */
  @Override
  protected Boolean toValue( String literal, Object evaluationContext ) {
    if ( LITERAL_TRUE.equalsIgnoreCase( literal ) || LITERAL_FALSE.equalsIgnoreCase( literal ) ) {
      return Boolean.valueOf( literal );
    } else {
      throw new IllegalArgumentException( "'" + literal + "' is not a valid boolean literal" );
    }
  }




  /**
   * Return the value of a method and its string arguments.
   * 
   * @see coyote.commons.eval.AbstractEvaluator#evaluate(coyote.commons.eval.Method, java.util.Iterator, java.lang.Object)
   */
  @Override
  protected Boolean evaluate( Method method, Iterator<String> arguments, Object evaluationContext ) {
    Boolean result;
    if ( EQUALS.equals( method ) ) {
      String arg2 = arguments.next();
      String arg1 = arguments.next();
      result = performEquals( arg1, arg2 );
    } else if ( REGEX.equals( method ) ) {
      String arg2 = arguments.next();
      String arg1 = arguments.next();
      result = performRegex( arg1, arg2 );
    } else if ( MATCH.equals( method ) ) {
      String arg2 = arguments.next();
      String arg1 = arguments.next();
      result = performMatch( arg1, arg2 );
    } else if ( EMPTY.equals( method ) ) {
      String arg1 = arguments.next();
      result = performEmpty( arg1 );
    } else if ( EXISTS.equals( method ) ) {
      String arg1 = arguments.next();
      result = performExists( arg1 );
    } else {
      result = super.evaluate( method, arguments, evaluationContext );
    }

    return result;

  }




  /**
   * Return the value of a constant.
   * 
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
    } else if ( CONTEXT_ERROR.equals( constant ) ) {
      if ( transformContext != null ) {
        Boolean retval = new Boolean( transformContext.isInError() );
        return retval;
      } else {
        return new Boolean( false );
      }
    } else if ( TRANSACTION_ERROR.equals( constant ) ) {
      if ( transformContext != null && transformContext.getTransaction() != null ) {
        Boolean retval = new Boolean( transformContext.getTransaction().isInError() );
        return retval;
      } else {
        return new Boolean( false );
      }
    } else if ( NO_ROWS_PROCESSED.equals( constant ) ) {
      if ( transformContext != null ) {
        Boolean retval = new Boolean( transformContext.getRow() == 0 );
        return retval;
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
    } else if ( operator == EQUAL ) {
      Object o1 = operands.next();
      Object o2 = operands.next();
      return o1.equals( o2 );
    } else {
      return super.evaluate( operator, operands, evaluationContext );
    }
  }




  public void setContext( TransformContext context ) {
    transformContext = context;
  }




  /**
   * Perform a case insensitive match between the two arguments.
   * 
   * <p>If the arguments did not return a frame value, assume a quoted string. 
   * And if the argument is still null, just use the raw argument.
   * 
   * @param arg1
   * @param arg2
   * 
   * @return true if a arguments match, false otherwise
   */
  private boolean performMatch( String arg1, String arg2 ) {
    if ( transformContext != null ) {
      String value = transformContext.resolveToString( arg1 );
      if ( value == null ) {
        value = StringUtil.getQuotedValue( arg1 );
        if ( value == null ) {
          value = arg1;
        }
      }
      String test = transformContext.resolveToString( arg2 );
      if ( test == null ) {
        test = StringUtil.getQuotedValue( arg2 );
        if ( test == null ) {
          test = arg2;
        }
      }

      if ( value.equalsIgnoreCase( test ) ) {
        return true;
      }
    } else {
      return false;
    }
    return false;
  }




  /**
   * Implements an equality check between the two arguments.
   * 
   * <p>Each of the arguments are passed to an function constant evaluator 
   * which will replace the arguments with any matching constants.
   * 
   * <p>Next the arguments are passed to the context to resolve them to named 
   * values in the transform context, its transaction context and its symbol 
   * table. Any matching keys are resolved to values. If no match is mage, the 
   * arguments are returned and assumed to be literals.
   * 
   * @param arg1 the value to test
   * @param arg2 the test against which the value is compared
   * 
   * @return true if the arguments evaluate to values which equal each other, 
   *         false otherwise.
   */
  private Boolean performEquals( String arg1, String arg2 ) {
    String op1 = sanitize( arg1 );
    op1 = evaluateFunctionConstant( op1 );
    String op2 = sanitize( arg2 );
    op2 = evaluateFunctionConstant( op2 );

    String value = op1;
    String test = op2;
    if ( transformContext != null ) {
      String rValue = transformContext.resolveToString( op1 );
      if ( rValue != null )
        value = rValue;

      String rTest = transformContext.resolveToString( op2 );
      if ( rTest != null )
        test = rTest;
    }

    if ( value != null ) {
      return value.equals( test );
    } else {
      return ( test == null );
    }
  }




  private String evaluateFunctionConstant( String token ) {
    if ( token != null ) {
      if ( token.equals( CURRENT_ROW ) ) {
        if ( transformContext != null ) {
          return Long.toString( transformContext.getRow() );
        }
      }
    }
    return token;
  }




  /**
   * Resolve the token in the context and determine if it is null or an empty 
   * string ("").
   * 
   * @param token name of the value to resolve in the context.
   * 
   * @return true if the token does not return a value or if the value 
   *         returned is an empty string, false if not null or empty.
   */
  private Boolean performEmpty( String token ) {
    String key = sanitize( token );
    String value = transformContext.resolveToString( key );
    return StringUtil.isEmpty( value );
  }




  private Boolean performExists( String token ) {
    String fieldname = sanitize( token );
    boolean retval = transformContext.containsField( fieldname );
    return retval;
  }




  private Boolean performRegex( String arg1, String arg2 ) {
    Boolean retval = Boolean.FALSE;
    // TODO Auto-generated method stub
    return retval;
  }




  /**
   * If the token starts and ends with a double quote, return the value 
   * contained therein.
   * @param token
   * @return
   */
  private String sanitize( String token ) {
    if ( token != null && token.startsWith( "\"" ) && token.endsWith( "\"" ) ) {
      String retval = StringUtil.getQuotedValue( token );
      if ( retval != null )
        return retval.trim();
      else
        return retval;
    }
    return token;
  }

}
