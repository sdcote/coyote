package coyote.commons.text.table;

/**
 * Determines the characters that make up a TextTable.
 */
public abstract class AbstractTableFormat {


  /**
   * @return The top left corner of the table. Ex: ╔
   */
  public abstract char getTopLeftCorner();


  /**
   * @return The top right corner of the table. Ex: ╗
   */
  public abstract char getTopRightCorner();


  /**
   * @return The bottom left corner of the table.
   */
  public abstract char getBottomLeftCorner();


  /**
   * @return The bottom right corner of the table.
   */
  public abstract char getBottomRightCorner();


  /**
   * @return The character used when dividing columns on the top edge of the table.
   */
  public abstract char getTopEdgeBorderDivider();


  /**
   * @return The character used when dividing columns on the bottom edge of the table.
   */
  public abstract char getBottomEdgeBorderDivider();


  /**
   * @param underHeaders True if the border is directly between the headers of the table and the first row.
   * @return The character used when dividing rows on the left edge of the table.
   */
  public abstract char getLeftEdgeBorderDivider(boolean underHeaders);


  /**
   * @param underHeaders True if the border is directly between the headers of the table and the first row.
   * @return The character used when dividing rows on the right edge of the table.
   */
  public abstract char getRightEdgeBorderDivider(boolean underHeaders);


  /**
   * @param edge         True if the border is on the top or bottom edge of the table.
   * @param underHeaders True if the border is directly between the headers of the table and the first row.
   * @return The character used for horizontal stretches in the table.
   */
  public abstract char getHorizontalBorderFill(boolean edge, boolean underHeaders);


  /**
   * @param edge True if the border is on the left or right edge of the table.
   * @return The character used for vertical stretches in the table.
   */
  public abstract char getVerticalBorderFill(boolean edge);


  /**
   * @param underHeaders True if the border is directly between the headers of the table and the first row.
   * @param emptyData    True if the table has no data. In this case it can be more aesthetically pleasing to use a
   *                     bottom edge divider to provide a flat surface for the bottom of the border.
   *                     (The cross will only appear under the headers at this point, so underHeaders is true when emptyData is true).
   * @return The character used where horizontal and vertical borders intersect.
   */
  public abstract char getCross(boolean underHeaders, boolean emptyData);

}
