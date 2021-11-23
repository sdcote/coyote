package coyote.commons.text.table;

/**
 * The default, UTF-8 based table format for {@link TextTable}.
 */
public class UTF8TableFormat extends AbstractTableFormat {
  private static final char BOTTOM_RIGHT_CORNER = (char) 9565;
  private static final char TOP_RIGHT_CORNER = (char) 9559;
  private static final char BOTTOM_LEFT_CORNER = (char) 9562;
  private static final char TOP_LEFT_CORNER = (char) 9556;
  private static final char TOP_EDGE_BORDER_DIVIDER = (char) 9572;
  private static final char BOTTOM_EDGE_BORDER_DIVIDER = (char) 9575;
  private static final char LEFT_EDGE_BORDER_DIVIDER_HEADER = (char) 9568;
  private static final char LEFT_EDGE_BORDER_DIVIDER = (char) 9567;
  private static final char RIGHT_EDGE_BORDER_DIVIDER_HEADER = (char) 9571;
  private static final char RIGHT_EDGE_BORDER_DIVIDER = (char) 9570;
  private static final char HORIZONTAL_BORDER_FILL_HEADER = (char) 9552;
  private static final char HORIZONTAL_BORDER_FILL = (char) 9472;
  private static final char VERTICAL_BORDER_FILL_HEADER = (char) 9553;
  private static final char VERTICAL_BORDER_FILL = (char) 9474;
  private static final char CROSS_HEADER_EMPTY = (char) 9575;
  private static final char CROSS_HEADER = (char) 9578;
  private static final char CROSS = (char) 9532;

  @Override
  public char getTopLeftCorner() {
    return TOP_LEFT_CORNER;
  }

  @Override
  public char getTopRightCorner() {
    return TOP_RIGHT_CORNER;
  }

  @Override
  public char getBottomLeftCorner() {
    return BOTTOM_LEFT_CORNER;
  }

  @Override
  public char getBottomRightCorner() {
    return BOTTOM_RIGHT_CORNER;
  }

  @Override
  public char getTopEdgeBorderDivider() {
    return TOP_EDGE_BORDER_DIVIDER;
  }

  @Override
  public char getBottomEdgeBorderDivider() {
    return BOTTOM_EDGE_BORDER_DIVIDER;
  }

  @Override
  public char getLeftEdgeBorderDivider(boolean underHeaders) {
    if (underHeaders)
      return LEFT_EDGE_BORDER_DIVIDER_HEADER;
    else
      return LEFT_EDGE_BORDER_DIVIDER;
  }

  @Override
  public char getRightEdgeBorderDivider(boolean underHeaders) {
    if (underHeaders)
      return RIGHT_EDGE_BORDER_DIVIDER_HEADER;
    else
      return RIGHT_EDGE_BORDER_DIVIDER;
  }

  @Override
  public char getHorizontalBorderFill(boolean edge, boolean underHeaders) {
    if (edge || underHeaders)
      return HORIZONTAL_BORDER_FILL_HEADER;
    else
      return HORIZONTAL_BORDER_FILL;
  }

  @Override
  public char getVerticalBorderFill(boolean edge) {
    if (edge)
      return VERTICAL_BORDER_FILL_HEADER;
    else
      return VERTICAL_BORDER_FILL;
  }

  @Override
  public char getCross(boolean underHeaders, boolean emptyData) {
    if (underHeaders) {
      if (emptyData)
        return CROSS_HEADER_EMPTY;
      else
        return CROSS_HEADER;
    } else
      return CROSS;
  }

}
