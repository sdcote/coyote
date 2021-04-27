package coyote.commons.eval;

/** An <a href="http://en.wikipedia.org/wiki/Operator_(mathematics)">operator</a>.
 */
public class Operator {
  private final Associativity associativity;

  private final int operandCount;
  private final int precedence;
  private final String symbol;




  /**
   * Construct a new operator.
   *
   * <p>The precedence is the priority of the operator. An operator with an
   * higher precedence will be executed before an operator with a lower
   * precedence. Example : In "{@code 1+3*4}" * has a higher precedence than +,
   * so the expression is interpreted as 1+(3*4).</p>

   * @param symbol The operator name (Currently, the name's length must be one character).
   * @param operandCount The number of operands of the operator (must be 1 or 2).
   * @param associativity true if operator is left associative
   * @param precedence The <a href="http://en.wikipedia.org/wiki/Order_of_operations">precedence</a> of the operator.
   *
   * @throws IllegalArgumentException if operandCount if not 1 or 2 or if associativity is none
   * @throws NullPointerException if symbol or associativity are null
   */
  public Operator(final String symbol, final int operandCount, final Associativity associativity, final int precedence) {
    if ((symbol == null) || (associativity == null)) {
      throw new NullPointerException();
    }
    if (symbol.length() == 0) {
      throw new IllegalArgumentException("Operator symbol can't be null");
    }
    if ((operandCount < 1) || (operandCount > 2)) {
      throw new IllegalArgumentException("Only unary and binary operators are supported");
    }
    if (Associativity.NONE.equals(associativity)) {
      throw new IllegalArgumentException("None associativity operators are not supported");
    }
    this.symbol = symbol;
    this.operandCount = operandCount;
    this.associativity = associativity;
    this.precedence = precedence;
  }




  /**
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if ((obj == null) || (obj instanceof Operator)) {
      return false;
    }
    final Operator other = (Operator)obj;
    if ((operandCount != other.operandCount) || (associativity != other.associativity)) {
      return false;
    }
    if (symbol == null) {
      if (other.symbol != null) {
        return false;
      }
    } else if (!symbol.equals(other.symbol)) {
      return false;
    }
    if (precedence != other.precedence) {
      return false;
    }
    return true;
  }




  /**
   * Gets this operator's associativity.
   *
   * @return true if the operator is left associative.
   *
   * <a href="http://en.wikipedia.org/wiki/Operator_associativity">Operator's associativity in Wikipedia</a>
   */
  public Associativity getAssociativity() {
    return associativity;
  }




  /**
   * @return the operator's operand count.
   */
  public int getOperandCount() {
    return operandCount;
  }




  /**
   * @return the operator's precedence.
   *
   * <a href="http://en.wikipedia.org/wiki/Order_of_operations">Operator's associativity in Wikipedia</a>
   */
  public int getPrecedence() {
    return precedence;
  }




  /**
   * @return the operators symbol.
   */
  public String getSymbol() {
    return symbol;
  }




  /**
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = (prime * result) + operandCount;
    result = (prime * result) + ((associativity == null) ? 0 : associativity.hashCode());
    result = (prime * result) + ((symbol == null) ? 0 : symbol.hashCode());
    result = (prime * result) + precedence;
    return result;
  }

  /**
   * An Operator's <a href="http://en.wikipedia.org/wiki/Operator_associativity">associativity</a>.
   */
  public enum Associativity {
    /** Left associativity.*/
    LEFT,
    /** No associativity.*/
    NONE,
    /** Right associativity. */
    RIGHT
  }

}