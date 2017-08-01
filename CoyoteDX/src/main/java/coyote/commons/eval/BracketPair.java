package coyote.commons.eval;

/**
 *  A <a href="http://en.wikipedia.org/wiki/Bracket_(mathematics)">bracket pair</a>.
 */
public class BracketPair {

  /** The angle brackets pair: &lt;&gt;.*/
  public static final BracketPair ANGLES = new BracketPair('<', '>');
  /** The braces pair: {}.*/
  public static final BracketPair BRACES = new BracketPair('{', '}');
  /** The square brackets pair: [].*/
  public static final BracketPair BRACKETS = new BracketPair('[', ']');
  /** The parentheses pair: ().*/
  public static final BracketPair PARENTHESES = new BracketPair('(', ')');

  private final String close;
  private final String open;




  /**
   * Constructor defining the open and closed brackets.
   *
   * @param open The character used to open the brackets.
   * @param close The character used to close the brackets.
   */
  public BracketPair(final char open, final char close) {
    super();
    this.open = new String(new char[]{open});
    this.close = new String(new char[]{close});
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
