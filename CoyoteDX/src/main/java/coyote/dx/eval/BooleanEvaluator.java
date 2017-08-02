/*
 * Copyright (c) 2015 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
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
 * An infix evaluator of boolean expressions.
 */
public class BooleanEvaluator extends AbstractEvaluator<Boolean> {

  private static final String LITERAL_TRUE = "true";
  private static final String LITERAL_FALSE = "false";

  /** The transformation context from which we retrieve data */
  TransformContext transformContext = null;

  // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
  // Operators
  /** The negate unary operator.*/
  public final static Operator NEGATE = new Operator("!", 1, Operator.Associativity.RIGHT, 3);

  /** The logical AND operator.*/
  private static final Operator AND = new Operator("&&", 2, Operator.Associativity.LEFT, 2);

  /** The logical OR operator.*/
  public final static Operator OR = new Operator("||", 2, Operator.Associativity.LEFT, 1);

  /** The logical Equals operator.*/
  public final static Operator EQUAL = new Operator("==", 2, Operator.Associativity.LEFT, 2);

  /** The standard whole set of predefined operators */
  private static final Operator[] OPERATORS = new Operator[]{NEGATE, AND, OR, EQUAL};

  // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
  // Methods
  /** Performs a case sensitive comparison between two string values */
  public static final Method EQUALS = new Method("equals", 2);

  /** Performs a regular expression match on the value of a field */
  public static final Method REGEX = new Method("regex", 2);

  /** Performs a case insensitive comparison between two string values*/
  public static final Method MATCH = new Method("match", 2);

  /** Checks if the given field contains a value */
  public static final Method EMPTY = new Method("empty", 1);

  /** Checks if the given field exists in the context */
  public static final Method EXISTS = new Method("exists", 1);

  /** Checks if the named job completed successfully */
  public static final Method JOB_SUCCESS = new Method("jobSuccess", 1);

  /** Checks if the named job failed to complete successfully */
  public static final Method JOB_FAILURE = new Method("jobFailure", 1);

  /** The whole set of predefined functions */
  private static final Method[] METHODS = new Method[]{MATCH, EMPTY, EXISTS, REGEX, EQUALS, JOB_SUCCESS, JOB_FAILURE};

  // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
  // Constants
  /** A constant that represents the current state of the isLastFrame() method call in the transaction context */
  public static final Constant LAST = new Constant("islast");

  /** A constant that represents the current error state of the transform context */
  public static final Constant CONTEXT_ERROR = new Constant("contextError");

  /** A constant that represents the current error state of the transaction context */
  public static final Constant TRANSACTION_ERROR = new Constant("transactionError");

  /** A constant that represents the state of no rows processed (i.e. currentRow=0) */
  public static final Constant NO_ROWS_PROCESSED = new Constant("noRowsProcessed");

  /** The whole set of predefined constants */
  private static final Constant[] CONSTANTS = new Constant[]{LAST, CONTEXT_ERROR, TRANSACTION_ERROR, NO_ROWS_PROCESSED};

  // Constants for use within function calls
  private static final String CURRENT_ROW = "currentRow";

  // Our default parameters
  private static Parameters DEFAULT_PARAMETERS;




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
    retval.addOperators(Arrays.asList(OPERATORS));
    retval.addMethods(Arrays.asList(METHODS));
    retval.addConstants(Arrays.asList(CONSTANTS));
    retval.addFunctionBracket(BracketPair.PARENTHESES);
    retval.addExpressionBracket(BracketPair.PARENTHESES);
    return retval;
  }




  private static Parameters getParameters() {
    if (DEFAULT_PARAMETERS == null) {
      DEFAULT_PARAMETERS = getDefaultParameters();
    }
    return DEFAULT_PARAMETERS;
  }




  /**
   * Default constructor which uses the default evaluation parameters.
   */
  public BooleanEvaluator() {
    this(getParameters());
  }




  /**
   * Our private constructor which uses the given evaluation parameters
   *
   * @param parameters the evaluation parameters this evaluator should use
   */
  private BooleanEvaluator(final Parameters parameters) {
    super(parameters);
  }




  public void setContext(final TransformContext context) {
    transformContext = context;
  }




  private String evaluateFunctionConstant(final String token) {
    if (token != null) {
      if (token.equals(CURRENT_ROW)) {
        if (transformContext != null) {
          return Long.toString(transformContext.getRow());
        }
      }
    }
    return token;
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
  private Boolean performEquals(final String arg1, final String arg2) {
    String op1 = sanitize(arg1);
    op1 = evaluateFunctionConstant(op1);
    String op2 = sanitize(arg2);
    op2 = evaluateFunctionConstant(op2);

    String value = op1;
    String test = op2;
    if (transformContext != null) {
      final String rValue = transformContext.resolveToString(op1);
      if (rValue != null) {
        value = rValue;
      }

      final String rTest = transformContext.resolveToString(op2);
      if (rTest != null) {
        test = rTest;
      }
    }

    if (value != null) {
      return value.equals(test);
    } else {
      return (test == null);
    }
  }




  /**
   * If the token starts and ends with a double quote, return the value
   * contained therein.
   *
   * @param token
   *
   * @return just the bare token
   */
  private String sanitize(final String token) {
    if ((token != null) && token.startsWith("\"") && token.endsWith("\"")) {
      final String retval = StringUtil.getQuotedValue(token);
      if (retval != null) {
        return retval.trim();
      } else {
        return retval;
      }
    }
    return token;
  }




  /**
   * Return the value of a constant.
   *
   * @see coyote.commons.eval.AbstractEvaluator#evaluate(coyote.commons.eval.Constant, java.lang.Object)
   */
  @Override
  protected Boolean evaluate(final Constant constant, final Object evaluationContext) {
    if (LAST.equals(constant)) {
      if ((transformContext != null) && (transformContext.getTransaction() != null)) {
        return new Boolean(transformContext.getTransaction().isLastFrame());
      } else {
        return new Boolean(false);
      }
    } else if (CONTEXT_ERROR.equals(constant)) {
      if (transformContext != null) {
        final Boolean retval = new Boolean(transformContext.isInError());
        return retval;
      } else {
        return new Boolean(false);
      }
    } else if (TRANSACTION_ERROR.equals(constant)) {
      if ((transformContext != null) && (transformContext.getTransaction() != null)) {
        final Boolean retval = new Boolean(transformContext.getTransaction().isInError());
        return retval;
      } else {
        return new Boolean(false);
      }
    } else if (NO_ROWS_PROCESSED.equals(constant)) {
      if (transformContext != null) {
        final Boolean retval = new Boolean(transformContext.getRow() == 0);
        return retval;
      } else {
        return new Boolean(false);
      }
    } else {
      return super.evaluate(constant, evaluationContext);
    }
  }




  /**
   * Return the value of a method and its string arguments.
   *
   * @see coyote.commons.eval.AbstractEvaluator#evaluate(coyote.commons.eval.Method, java.util.Iterator, java.lang.Object)
   */
  @Override
  protected Boolean evaluate(final Method method, final Iterator<String> arguments, final Object evaluationContext) {
    Boolean result;
    if (EQUALS.equals(method)) {
      final String arg2 = arguments.next();
      final String arg1 = arguments.next();
      result = performEquals(arg1, arg2);
    } else if (REGEX.equals(method)) {
      final String arg2 = arguments.next();
      final String arg1 = arguments.next();
      result = RegexMethod.execute(transformContext, arg1, arg2);
    } else if (MATCH.equals(method)) {
      final String arg2 = arguments.next();
      final String arg1 = arguments.next();
      result = MatchMethod.execute(transformContext, arg1, arg2);
    } else if (EMPTY.equals(method)) {
      final String arg1 = arguments.next();
      result = EmptyMethod.execute(transformContext, arg1);
    } else if (EXISTS.equals(method)) {
      final String arg1 = arguments.next();
      result = ExistsMethod.execute(transformContext, arg1);
    } else if (JOB_SUCCESS.equals(method)) {
      final String arg1 = arguments.next();
      result = JobSuccessMethod.execute(transformContext, arg1);
    } else if (JOB_FAILURE.equals(method)) {
      final String arg1 = arguments.next();
      result = !JobSuccessMethod.execute(transformContext, arg1);
    } else {
      result = super.evaluate(method, arguments, evaluationContext);
    }

    return result;

  }




  /**
   * @see coyote.commons.eval.AbstractEvaluator#evaluate(coyote.commons.eval.Operator, java.util.Iterator, java.lang.Object)
   */
  @Override
  protected Boolean evaluate(final Operator operator, final Iterator<Boolean> operands, final Object evaluationContext) {
    if (NEGATE.equals(operator)) {
      return !operands.next();
    } else if (OR.equals(operator)) {
      final Boolean o1 = operands.next();
      final Boolean o2 = operands.next();
      return o1 || o2;
    } else if (AND.equals(operator)) {
      final Boolean o1 = operands.next();
      final Boolean o2 = operands.next();
      return o1 && o2;
    } else if (EQUAL.equals(operator)) {
      final Object o1 = operands.next();
      final Object o2 = operands.next();
      return o1.equals(o2);
    } else {
      return super.evaluate(operator, operands, evaluationContext);
    }
  }




  /**
   * Return the value of a literal.
   *
   * @see coyote.commons.eval.AbstractEvaluator#toValue(java.lang.String, java.lang.Object)
   */
  @Override
  protected Boolean toValue(final String literal, final Object evaluationContext) {
    if (LITERAL_TRUE.equalsIgnoreCase(literal) || LITERAL_FALSE.equalsIgnoreCase(literal)) {
      return Boolean.valueOf(literal);
    } else {
      throw new IllegalArgumentException("'" + literal + "' is not a valid boolean literal");
    }
  }

}
