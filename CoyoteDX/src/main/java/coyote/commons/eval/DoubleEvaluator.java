package coyote.commons.eval;

import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Locale;


/**
 * An evaluator that is able to evaluate arithmetic expressions on real numbers.
 *
 * <p>Built-in operators:<ul>
 * <li>+: Addition</li>
 * <li>-: Subtraction</li>
 * <li>-: Unary minus</li>
 * <li>*: Multiplication</li>
 * <li>/: Division</li>
 * <li>^: Exponentiation.<br>Warning: Exponentiation is implemented using java.lang.Math.pow which has some limitations (please read Oracle documentation about this method to known details).<br>For example (-1)^(1/3) returns NaN.</li>
 * <li>%: Modulo</li>
 * </ul>
 *
 * <p>Built-in functions:<ul>
 * <li>abs: absolute value</li>
 * <li>acos: arc cosine</li>
 * <li>asin: arc sine</li>
 * <li>atan: arc tangent</li>
 * <li>average: average of arguments</li>
 * <li>ceil: nearest upper integer</li>
 * <li>cos: cosine</li>
 * <li>cosh: hyperbolic cosine</li>
 * <li>floor: nearest lower integer</li>
 * <li>ln: natural logarithm (base e)</li>
 * <li>log: base 10 logarithm</li>
 * <li>max: maximum of arguments</li>
 * <li>min: minimum of arguments</li>
 * <li>round: nearest integer</li>
 * <li>sin: sine</li>
 * <li>sinh: hyperbolic sine</li>
 * <li>sum: sum of arguments</li>
 * <li>tan: tangent</li>
 * <li>tanh: hyperbolic tangent</li>
 * <li>random: pseudo-random number (between 0 and 1)</li>
 * </ul>
 *
 * <p>Built-in constants:<ul>
 * <li>e: Base of natural logarithms</li>
 * <li>pi: Ratio of the circumference of a circle to its diameter</li>
 * </ul>
 */
public class DoubleEvaluator extends AbstractEvaluator<Double> {
  /** Returns the absolute value of a number */
  public static final Function ABS = new Function("abs", 1);

  /** Returns the trigonometric arc-cosine of an angle. The angle is expressed in radian.*/
  public static final Function ACOSINE = new Function("acos", 1);
  /** Returns the trigonometric arc-sine of an angle. The angle is expressed in radian.*/
  public static final Function ASINE = new Function("asin", 1);
  /** Returns the trigonometric arc-tangent of an angle. The angle is expressed in radian.*/
  public static final Function ATAN = new Function("atan", 1);
  /** Returns the average of n numbers (n&gt;=1) */
  public static final Function AVERAGE = new Function("avg", 1, Integer.MAX_VALUE);
  /** Returns the smallest integer &gt;= argument */
  public static final Function CEIL = new Function("ceil", 1);
  /** Returns the trigonometric cosine of an angle. The angle is expressed in radian.*/
  public static final Function COSINE = new Function("cos", 1);
  /** Returns the hyperbolic cosine of a number.*/
  public static final Function COSINEH = new Function("cosh", 1);
  /** The division operator.*/
  public static final Operator DIVIDE = new Operator("/", 2, Operator.Associativity.LEFT, 2);
  /** A constant that represents e (2.718281...) */
  public static final Constant E = new Constant("e");
  /** The exponentiation operator.*/
  public static final Operator EXPONENT = new Operator("^", 2, Operator.Associativity.LEFT, 4);
  /** Returns the largest integer &lt;= argument */
  public static final Function FLOOR = new Function("floor", 1);
  /** Returns the natural logarithm of a number */
  public static final Function LN = new Function("ln", 1);
  /** Returns the decimal logarithm of a number */
  public static final Function LOG = new Function("log", 1);
  /** Returns the maximum of n numbers (n&gt;=1) */
  public static final Function MAX = new Function("max", 1, Integer.MAX_VALUE);
  /** Returns the minimum of n numbers (n&gt;=1) */
  public static final Function MIN = new Function("min", 1, Integer.MAX_VALUE);
  /** The subtraction operator.*/
  public static final Operator MINUS = new Operator("-", 2, Operator.Associativity.LEFT, 1);
  /** The <a href="http://en.wikipedia.org/wiki/Modulo_operation">modulo operator</a>.*/
  public static final Operator MODULO = new Operator("%", 2, Operator.Associativity.LEFT, 2);
  /** The multiplication operator.*/
  public static final Operator MULTIPLY = new Operator("*", 2, Operator.Associativity.LEFT, 2);
  /** The negate unary operator in the standard operator precedence.*/
  public static final Operator NEGATE = new Operator("-", 1, Operator.Associativity.RIGHT, 3);
  /** The negate unary operator in the Excel like operator precedence.*/
  public static final Operator NEGATE_HIGH = new Operator("-", 1, Operator.Associativity.RIGHT, 5);
  /** A constant that represents pi (3.14159...) */
  public static final Constant PI = new Constant("pi");
  /** The addition operator.*/
  public static final Operator PLUS = new Operator("+", 2, Operator.Associativity.LEFT, 1);
  /** Returns a pseudo random number */
  public static final Function RANDOM = new Function("random", 0);
  /** Returns the closest integer of a number */
  public static final Function ROUND = new Function("round", 1);
  /** Returns the trigonometric sine of an angle. The angle is expressed in radian.*/
  public static final Function SINE = new Function("sin", 1);
  /** Returns the hyperbolic sine of a number.*/
  public static final Function SINEH = new Function("sinh", 1);
  /** Returns the sum of n numbers (n&gt;=1) */
  public static final Function SUM = new Function("sum", 1, Integer.MAX_VALUE);
  /** Returns the trigonometric tangent of an angle. The angle is expressed in radian.*/
  public static final Function TANGENT = new Function("tan", 1);
  /** Returns the hyperbolic tangent of a number.*/
  public static final Function TANGENTH = new Function("tanh", 1);
  /** The whole set of predefined constants */
  private static final Constant[] CONSTANTS = new Constant[]{PI, E};
  private static Parameters DEFAULT_PARAMETERS;
  private static final ThreadLocal<NumberFormat> FORMATTER = new ThreadLocal<NumberFormat>() {
    @Override
    protected NumberFormat initialValue() {
      return NumberFormat.getNumberInstance(Locale.US);
    }
  };
  /** The whole set of predefined functions */
  private static final Function[] FUNCTIONS = new Function[]{SINE, COSINE, TANGENT, ASINE, ACOSINE, ATAN, SINEH, COSINEH, TANGENTH, MIN, MAX, SUM, AVERAGE, LN, LOG, ROUND, CEIL, FLOOR, ABS, RANDOM};
  /** The standard whole set of predefined operators */
  private static final Operator[] OPERATORS = new Operator[]{NEGATE, MINUS, PLUS, MULTIPLY, DIVIDE, EXPONENT, MODULO};

  /** The excel like whole set of predefined operators */
  private static final Operator[] OPERATORS_EXCEL = new Operator[]{NEGATE_HIGH, MINUS, PLUS, MULTIPLY, DIVIDE, EXPONENT, MODULO};




  /**
   * Gets a copy of DoubleEvaluator standard default parameters.
   *
   * <p>The returned parameters contains all the predefined operators,
   * functions and constants.</p>
   *
   * <p>Each call to this method create a new instance of Parameters.</p>
   *
   * @return a Paramaters instance
   *
   * @see Style
   */
  public static Parameters getDefaultParameters() {
    return getDefaultParameters(Style.STANDARD);
  }




  /**
   * Gets a copy of DoubleEvaluator default parameters.
   *
   * <p>The returned parameters contains all the predefined operators,
   * functions and constants.</p>
   *
   * <p>Each call to this method create a new instance of Parameters.</p>
   *
   * @return a Paramaters instance
   */
  public static Parameters getDefaultParameters(final Style style) {
    final Parameters result = new Parameters();
    result.addOperators(style == Style.STANDARD ? Arrays.asList(OPERATORS) : Arrays.asList(OPERATORS_EXCEL));
    result.addFunctions(Arrays.asList(FUNCTIONS));
    result.addConstants(Arrays.asList(CONSTANTS));
    result.addFunctionBracket(BracketPair.PARENTHESES);
    result.addExpressionBracket(BracketPair.PARENTHESES);
    return result;
  }




  /**
   * @return the default parameters supported by this evaluator.
   */
  protected static Parameters getParameters() {
    if (DEFAULT_PARAMETERS == null) {
      DEFAULT_PARAMETERS = getDefaultParameters();
    }
    return DEFAULT_PARAMETERS;
  }




  /**
   * This default constructor builds an instance with all predefined operators,
   * functions and constants.
   */
  public DoubleEvaluator() {
    this(getParameters());
  }




  /**
   * This constructor can be used to reduce the set of supported operators,
   * functions or constants, or to localize some function or constant's names.
   *
   * @param parameters The parameters of the evaluator.
   */
  public DoubleEvaluator(final Parameters parameters) {
    super(parameters);
  }




  private void errIfNaN(final Double result, final Function function) {
    if (result.equals(Double.NaN)) {
      throw new IllegalArgumentException("Invalid argument passed to " + function.getName());
    }
  }




  /**
   * @see coyote.commons.eval.AbstractEvaluator#evaluate(coyote.commons.eval.Constant, java.lang.Object)
   */
  @Override
  protected Double evaluate(final Constant constant, final Object evaluationContext) {
    if (PI.equals(constant)) {
      return Math.PI;
    } else if (E.equals(constant)) {
      return Math.E;
    } else {
      return super.evaluate(constant, evaluationContext);
    }
  }




  /**
   * @see coyote.commons.eval.AbstractEvaluator#evaluate(coyote.commons.eval.Function, java.util.Iterator, java.lang.Object)
   */
  @Override
  protected Double evaluate(final Function function, final Iterator<Double> arguments, final Object evaluationContext) {
    Double result;
    if (ABS.equals(function)) {
      result = Math.abs(arguments.next());
    } else if (CEIL.equals(function)) {
      result = Math.ceil(arguments.next());
    } else if (FLOOR.equals(function)) {
      result = Math.floor(arguments.next());
    } else if (ROUND.equals(function)) {
      final Double arg = arguments.next();
      if ((arg == Double.NEGATIVE_INFINITY) || (arg == Double.POSITIVE_INFINITY)) {
        result = arg;
      } else {
        result = (double)Math.round(arg);
      }
    } else if (SINEH.equals(function)) {
      result = Math.sinh(arguments.next());
    } else if (COSINEH.equals(function)) {
      result = Math.cosh(arguments.next());
    } else if (TANGENTH.equals(function)) {
      result = Math.tanh(arguments.next());
    } else if (SINE.equals(function)) {
      result = Math.sin(arguments.next());
    } else if (COSINE.equals(function)) {
      result = Math.cos(arguments.next());
    } else if (TANGENT.equals(function)) {
      result = Math.tan(arguments.next());
    } else if (ACOSINE.equals(function)) {
      result = Math.acos(arguments.next());
    } else if (ASINE.equals(function)) {
      result = Math.asin(arguments.next());
    } else if (ATAN.equals(function)) {
      result = Math.atan(arguments.next());
    } else if (MIN.equals(function)) {
      result = arguments.next();
      while (arguments.hasNext()) {
        result = Math.min(result, arguments.next());
      }
    } else if (MAX.equals(function)) {
      result = arguments.next();
      while (arguments.hasNext()) {
        result = Math.max(result, arguments.next());
      }
    } else if (SUM.equals(function)) {
      result = 0.;
      while (arguments.hasNext()) {
        result = result + arguments.next();
      }
    } else if (AVERAGE.equals(function)) {
      result = 0.;
      int nb = 0;
      while (arguments.hasNext()) {
        result = result + arguments.next();
        nb++;
      }
      result = result / nb;
    } else if (LN.equals(function)) {
      result = Math.log(arguments.next());
    } else if (LOG.equals(function)) {
      result = Math.log10(arguments.next());
    } else if (RANDOM.equals(function)) {
      result = Math.random();
    } else {
      result = super.evaluate(function, arguments, evaluationContext);
    }
    errIfNaN(result, function);
    return result;
  }




  /**
   * @see coyote.commons.eval.AbstractEvaluator#evaluate(coyote.commons.eval.Operator, java.util.Iterator, java.lang.Object)
   */
  @Override
  protected Double evaluate(final Operator operator, final Iterator<Double> operands, final Object evaluationContext) {
    if (NEGATE.equals(operator) || NEGATE_HIGH.equals(operator)) {
      return -operands.next();
    } else if (MINUS.equals(operator)) {
      return operands.next() - operands.next();
    } else if (PLUS.equals(operator)) {
      return operands.next() + operands.next();
    } else if (MULTIPLY.equals(operator)) {
      return operands.next() * operands.next();
    } else if (DIVIDE.equals(operator)) {
      return operands.next() / operands.next();
    } else if (EXPONENT.equals(operator)) {
      return Math.pow(operands.next(), operands.next());
    } else if (MODULO.equals(operator)) {
      return operands.next() % operands.next();
    } else {
      return super.evaluate(operator, operands, evaluationContext);
    }
  }




  /**
   * @see coyote.commons.eval.AbstractEvaluator#toValue(java.lang.String, java.lang.Object)
   */
  @Override
  protected Double toValue(final String literal, final Object evaluationContext) {
    final ParsePosition p = new ParsePosition(0);
    final Number result = FORMATTER.get().parse(literal, p);
    if ((p.getIndex() == 0) || (p.getIndex() != literal.length())) {
      throw new IllegalArgumentException(literal + " is not a number");
    }
    return result.doubleValue();
  }

  /**
   * The order of operation (operator precedence) is not clearly defined,
   * especially between the unary minus operator and exponentiation operator
   * (see <a href="http://en.wikipedia.org/wiki/Order_of_operations#Exceptions_to_the_standard">http://en.wikipedia.org/wiki/Order_of_operations</a>).
   * These constants define the operator precedence styles.
   */
  public static enum Style {
    /** The operator precedence used by Excel, or bash shell script language, where the unary minus as a higher precedence than the exponentiation.
     * <br>With this style, -2^2=4.
     */
    EXCEL,
    /**
     * The most commonly operator precedence, where the unary minus as a lower precedence than the exponentiation.
     * <p>With this style, used by Google, Wolfram alpha, and many others, -2^2=-4.</p>
     */
    STANDARD
  }

}
