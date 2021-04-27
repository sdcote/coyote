package coyote.commons.eval;

/**
 * A token of an expression.
 *
 * <p>When evaluating an expression, it is first split into tokens. These
 * tokens can be operators, constants, etc ...
 */
public class Token {
  static final Token FUNCTION_ARG_SEPARATOR = new Token(Kind.FUNCTION_SEPARATOR, null);
  private final Object content;
  private final Kind kind;




  protected static Token buildCloseToken(final BracketPair pair) {
    return new Token(Kind.CLOSE_BRACKET, pair);
  }




  protected static Token buildFunction(final Function function) {
    return new Token(Kind.FUNCTION, function);
  }




  protected static Token buildLiteral(final String literal) {
    return new Token(Kind.LITERAL, literal);
  }




  protected static Token buildMethod(final Method method) {
    return new Token(Kind.METHOD, method);
  }




  protected static Token buildOpenToken(final BracketPair pair) {
    return new Token(Kind.OPEN_BRACKET, pair);
  }




  protected static Token buildOperator(final Operator ope) {
    return new Token(Kind.OPERATOR, ope);
  }




  private Token(final Kind kind, final Object content) {
    super();
    if ((kind.equals(Kind.OPERATOR) && !(content instanceof Operator)) || (kind.equals(Kind.FUNCTION) && !(content instanceof Function)) || (kind.equals(Kind.METHOD) && !(content instanceof Method)) || (kind.equals(Kind.LITERAL) && !(content instanceof String))) {
      throw new IllegalArgumentException();
    }
    this.kind = kind;
    this.content = content;
  }




  /**
   * Tests whether the token is a close bracket.
   *
   * @return true if the token is a close bracket
   */
  public boolean isCloseBracket() {
    return kind.equals(Kind.CLOSE_BRACKET);
  }




  /**
   * Tests whether the token is a function.
   *
   * @return true if the token is a function
   */
  public boolean isFunction() {
    return kind.equals(Kind.FUNCTION);
  }




  /**
   * Tests whether the token is a function argument separator.
   *
   * @return true if the token is a function argument separator
   */
  public boolean isFunctionArgumentSeparator() {
    return kind.equals(Kind.FUNCTION_SEPARATOR);
  }




  /**
   * Tests whether the token is a literal or a constant or a variable name.
   *
   * @return true if the token is a literal, a constant or a variable name
   */
  public boolean isLiteral() {
    return kind.equals(Kind.LITERAL);
  }




  /**
   * Tests whether the token is a method.
   *
   * @return true if the token is a method
   */
  public boolean isMethod() {
    return kind.equals(Kind.METHOD);
  }




  /**
   * Tests whether the token is an open bracket.
   *
   * @return true if the token is an open bracket
   */
  public boolean isOpenBracket() {
    return kind.equals(Kind.OPEN_BRACKET);
  }




  /**
   * Tests whether the token is an operator.
   *
   * @return true if the token is an operator
   */
  public boolean isOperator() {
    return kind.equals(Kind.OPERATOR);
  }




  /**
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return content != null ? content.toString() : "NULL";
  }




  protected Operator.Associativity getAssociativity() {
    return getOperator().getAssociativity();
  }




  protected BracketPair getBrackets() {
    return (BracketPair)content;
  }




  protected Function getFunction() {
    return (Function)content;
  }




  protected Kind getKind() {
    return kind;
  }




  protected String getLiteral() {
    if (!kind.equals(Kind.LITERAL)) {
      throw new IllegalArgumentException();
    }
    return (String)content;
  }




  protected Method getMethod() {
    return (Method)content;
  }




  protected Operator getOperator() {
    return (Operator)content;
  }




  protected int getPrecedence() {
    return getOperator().getPrecedence();
  }

  private enum Kind {
    CLOSE_BRACKET, FUNCTION, FUNCTION_SEPARATOR, LITERAL, METHOD, OPEN_BRACKET, OPERATOR
  }

}
