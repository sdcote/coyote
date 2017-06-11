package coyote.dataframe.marshal;

/**
 * An unchecked exception to indicate that an input does not qualify as valid.
 */
public class ParseException extends RuntimeException {

  private static final long serialVersionUID = 3846985517841261730L;
  private final int offset;
  private final int line;
  private final int column;
  private final int character;




  public ParseException( final String message, final int offset, final int line, final int column, final int character ) {

    super( message + " at line:" + line + " column:" + column + " offset:" + offset + " char: '" + (char)character + "' (" + character + ")" );
    this.offset = offset;
    this.line = line;
    this.column = column;
    this.character = character;
  }




  /**
   * Returns the value of the character which caused the error.
   * 
   * <p>This is usually the 'current' character the parser is considering.
   * 
   * @return the integer value of the character which caused the error.
   */
  public int getCharacter() {
    return character;
  }




  /**
   * Returns the index of the character at which the error occurred, relative to the line. The
   * index of the first character of a line is 1.
   *
   * @return the column in which the error occurred, will be &gt;= 1
   */
  public int getColumn() {
    return column;
  }




  /**
   * Returns the number of the line in which the error occurred. The first line counts as 1.
   *
   * @return the line in which the error occurred, will be &gt;= 1
   */
  public int getLine() {
    return line;
  }




  /**
   * Returns the absolute index of the character at which the error occurred. The
   * index of the first character of a document is 0.
   *
   * @return the character offset at which the error occurred, will be &gt;= 0
   */
  public int getOffset() {
    return offset;
  }
}
