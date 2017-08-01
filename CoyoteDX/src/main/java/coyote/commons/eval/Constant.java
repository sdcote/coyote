package coyote.commons.eval;

/**
 * A constant in an expression.
 *
 * <p>Some expressions needs constants. For instance it is impossible to
 * perform trigonometric calculus without using pi. A constant allows you to
 * use mnemonic in your expressions instead of the raw value of the constant.</p>
 *
 * <p>A constant for pi would be defined by :<pre>
 * Constant&lt;Double&gt; pi = new Constant&lt;Double&gt;("pi");</pre>
 *
 * <p>With such a constant, you will be able to evaluate the expression
 * "sin(pi/4)".</p>
 *
 * @see AbstractEvaluator#evaluate(Constant, Object)
 */
public class Constant {
  private final String name;




  /**
   * Constructor specifying the name used in expressions to identify the constants.
   *
   * @param name The mnemonic of the constant.
   */
  public Constant(final String name) {
    this.name = name;
  }




  /**
   * Gets the mnemonic of the constant.
   *
   * @return the id
   */
  public String getName() {
    return name;
  }

}
