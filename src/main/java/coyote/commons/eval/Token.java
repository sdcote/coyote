package coyote.commons.eval;

/**
 * A token of an expression.
 * 
 * <p>When evaluating an expression, it is first split into tokens. These 
 * tokens can be operators, constants, etc ...</p>
 */
public class Token {
  private enum Kind {
    OPEN_BRACKET, CLOSE_BRACKET, FUNCTION_SEPARATOR, FUNCTION, OPERATOR, LITERAL
  }

  static final Token FUNCTION_ARG_SEPARATOR = new Token( Kind.FUNCTION_SEPARATOR, null );




  static Token buildCloseToken( final BracketPair pair ) {
    return new Token( Kind.CLOSE_BRACKET, pair );
  }




  static Token buildFunction( final Function function ) {
    return new Token( Kind.FUNCTION, function );
  }




  static Token buildLiteral( final String literal ) {
    return new Token( Kind.LITERAL, literal );
  }




  static Token buildOpenToken( final BracketPair pair ) {
    return new Token( Kind.OPEN_BRACKET, pair );
  }




  static Token buildOperator( final Operator ope ) {
    return new Token( Kind.OPERATOR, ope );
  }

  private final Kind kind;

  private final Object content;




  private Token( final Kind kind, final Object content ) {
    super();
    if ( ( kind.equals( Kind.OPERATOR ) && !( content instanceof Operator ) ) || ( kind.equals( Kind.FUNCTION ) && !( content instanceof Function ) ) || ( kind.equals( Kind.LITERAL ) && !( content instanceof String ) ) ) {
      throw new IllegalArgumentException();
    }
    this.kind = kind;
    this.content = content;
  }




  Operator.Associativity getAssociativity() {
    return getOperator().getAssociativity();
  }




  BracketPair getBrackets() {
    return (BracketPair)content;
  }




  Function getFunction() {
    return (Function)content;
  }




  Kind getKind() {
    return kind;
  }




  String getLiteral() {
    if ( !kind.equals( Kind.LITERAL ) ) {
      throw new IllegalArgumentException();
    }
    return (String)content;
  }




  Operator getOperator() {
    return (Operator)content;
  }




  int getPrecedence() {
    return getOperator().getPrecedence();
  }




  /**
   * Tests whether the token is a close bracket.
   * 
   * @return true if the token is a close bracket
   */
  public boolean isCloseBracket() {
    return kind.equals( Kind.CLOSE_BRACKET );
  }




  /**
   * Tests whether the token is a function.
   * 
   * @return true if the token is a function
   */
  public boolean isFunction() {
    return kind.equals( Kind.FUNCTION );
  }




  /**
   * Tests whether the token is a function argument separator.
   * 
   * @return true if the token is a function argument separator
   */
  public boolean isFunctionArgumentSeparator() {
    return kind.equals( Kind.FUNCTION_SEPARATOR );
  }




  /**
   * Tests whether the token is a literal or a constant or a variable name.
   * 
   * @return true if the token is a literal, a constant or a variable name
   */
  public boolean isLiteral() {
    return kind.equals( Kind.LITERAL );
  }




  /**
   * Tests whether the token is an open bracket.
   * 
   * @return true if the token is an open bracket
   */
  public boolean isOpenBracket() {
    return kind.equals( Kind.OPEN_BRACKET );
  }




  /**
   * Tests whether the token is an operator.
   * 
   * @return true if the token is an operator
   */
  public boolean isOperator() {
    return kind.equals( Kind.OPERATOR );
  }

}
