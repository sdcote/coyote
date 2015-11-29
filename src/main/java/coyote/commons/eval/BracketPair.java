package coyote.commons.eval;

/**
 *  A <a href="http://en.wikipedia.org/wiki/Bracket_(mathematics)">bracket pair</a>.
 */
public class BracketPair {

  /** The parentheses pair: ().*/
  public static final BracketPair PARENTHESES = new BracketPair( '(', ')' );
  /** The square brackets pair: [].*/
  public static final BracketPair BRACKETS = new BracketPair( '[', ']' );
  /** The braces pair: {}.*/
  public static final BracketPair BRACES = new BracketPair( '{', '}' );
  /** The angle brackets pair: <>.*/
  public static final BracketPair ANGLES = new BracketPair( '<', '>' );

  private final String open;
  private final String close;




  /**
   * Constructor defining the open and closed brackets.
   * 
   * @param open The character used to open the brackets.
   * @param close The character used to close the brackets.
   */
  public BracketPair( final char open, final char close ) {
    super();
    this.open = new String( new char[] { open } );
    this.close = new String( new char[] { close } );
  }




  /**
   * @return the close bracket character.
   */
  public String getClose() {
    return close;
  }




  /** 
   * @return the open bracket character.
   */
  public String getOpen() {
    return open;
  }




  /**
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return open + close;
  }

}
