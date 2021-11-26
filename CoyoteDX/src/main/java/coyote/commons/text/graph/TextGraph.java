package coyote.commons.text.graph;

import coyote.commons.SeriesUtil;
import coyote.commons.StringUtil;

import java.text.DecimalFormat;

/**
 * Inspiration: https://github.com/kroitor/asciichart
 */
public class TextGraph {
  private static final char LEFT_INTERSECT = (char) 9508; // '┤'
  private static final char CROSS = (char) 9532; // '┼'
  private static final char VERTICAL = (char) 9474; // '│'
  private static final char HORIZONTAL = (char) 9472; // '─'
  private static final char RIGHT_DOWN = (char) 9582; // '╮'
  private static final char RIGHT_UP = (char) 9583; // '╯'
  private static final char DOWN_RIGHT = (char) 9584; // '╰'
  private static final char UP_RIGHT = (char) 9581; // '╭';
  private static final int CENTER = 1;

  /**
   * The data series, with index being the x-axis and value being the y-axis.
   */
  private double[] series;

  /**
   * The labels for the y-axis.
   */
  private String[] labels = null;

  /**
   * The minimum value in the series.
   */
  private double min;

  /**
   * The maximum value in the series.
   */
  private double max;

  /**
   * The range of the data in the series.
   */
  private double range;

  /**
   * The number of rows in the graph.
   */
  private int numRows;

  /**
   * The number of columns in the graph, including the axis and ticks.
   */
  private int numCols;

  /**
   * How wide the ticks are. Ticks are left-padded with spaces to be this length.
   */
  private int tickWidth = 8;

  /**
   * How the ticks should be formatted.
   */
  private DecimalFormat tickFormat = new DecimalFormat("###0.00");

  /**
   * The index at which the axis starts.
   */
  private int axisColumnIndex;

  /**
   * This is the index at which the line starts.
   */
  private int lineIndex;


  /**
   * Private constructor since we use the builder pattern to generate the graph.
   *
   * @param series
   */
  private TextGraph(double[] series) {
    this.series = series;
  }


  /**
   * Creates a TextGraph instance from the given series.
   *
   * @param series The series of data, where index is the x-axis and value is the y-axis.
   * @return A new TextGraph instance.
   */
  public static TextGraph fromSeries(double[] series) {
    return new TextGraph(series);
  }


  /**
   * Calculates the instance fields used for plotting.
   */
  private void calculateFields() {

    // If we are using labels, then shrink the graph araa
    if (this.isUsingLabels() && numRows > 2) numRows -= 2;

    // Get minimum and maximum from series.
    double[] minMax = SeriesUtil.getMinAndMaxValues(this.series);
    this.min = minMax[0];
    this.max = minMax[1];
    this.range = max - min;

    axisColumnIndex = tickWidth + 1;
    lineIndex = axisColumnIndex + 1;

    // Since the graph is made of ASCII characters, it needs whole-number counts of rows and columns.
    this.numRows = numRows == 0 ? (int) Math.round(max - min) + 1 : numRows;

    // For columns, add the width of the tick marks, the width of the axis, and the length of the series.
    this.numCols = tickWidth + (axisColumnIndex - tickWidth) + series.length;
  }


  /**
   * Check to see if there is any data to be used as labels.
   *
   * @return true if there are labels, false if no labels are to be placed on the graph.
   */
  private boolean isUsingLabels() {
    return labels != null && labels.length > 0;
  }


  /**
   * Determines the number of rows in the graph.
   * By default, the number of rows will be equal to the range of the series + 1.
   *
   * @param numRows The number of rows desired. If 0, uses the default.
   * @return This instance.
   */
  public TextGraph withNumRows(int numRows) {
    this.numRows = numRows;
    return this;
  }


  /**
   * Determines the minimum width of the ticks on the axis.
   * Ticks will be left-padded with spaces if they are not already this length.
   * Defaults to 8.
   *
   * @param tickWidth The width of the ticks on the axis.
   * @return This instance.
   */
  public TextGraph withTickWidth(int tickWidth) {
    this.tickWidth = tickWidth;
    return this;
  }


  /**
   * Determines how the ticks will be formatted.
   * Defaults to "###0.00".
   *
   * @param tickFormat The format of the ticks.
   * @return This instance.
   */
  public TextGraph withTickFormat(DecimalFormat tickFormat) {
    this.tickFormat = tickFormat;
    return this;
  }


  /**
   * Plots the graph and returns it as a String.
   *
   * @return The string representation of the graph, using new lines.
   */
  public String plot() {
    // Possibly stutter the data to make room for the labels
    validateSeries();

    // Determine the size of the graph area and key points on the "canvas"
    calculateFields();

    // The graph is initially stored in a 2D array, later turned into Strings.
    char[][] graph = new char[numRows][numCols];

    // Fill the graph with space characters.
    for (int row = 0; row < numRows; row++) {
      for (int col = 0; col < graph[row].length; col++) {
        graph[row][col] = ' ';
      }
    }

    // Draw the ticks and graph.
    drawTicksAndAxis(graph);

    // Place the labels on the graph if defined
    placeLabels(graph);

    // Draw the line.
    drawLine(graph);

    // Convert the 2D char array graph to a String using newlines.
    return convertGraphToString(graph);
  }


  /**
   * We have to adjust the data series if we are using labels.
   *
   * <p>We must grow the size of the data set so it plots appropriately to fit the labels that will be placed
   * underneath the main chart.</p>
   */
  private void validateSeries() {
    if (this.isUsingLabels()) {
     int length = SeriesUtil.getMaxLength(labels);
      if (length > 1) {
        series = stutterDataPoints(series, length+1);
      }
    }
  }


  /**
   * Place the labels on the graph
   *
   * @param graph The 2D array of characters acting as our canvas
   */
  private void placeLabels(char[][] graph) {
    if (this.isUsingLabels()) {
      // the last row is where we place our labels
    }
  }


  /**
   * Adds the tick marks and axis to the graph.
   *
   * @param graph The graph.
   */
  private void drawTicksAndAxis(char[][] graph) {
    // Add the labels and the axis.
    for (int row = 0; row < graph.length; row++) {

      double y = determineYValueAtRow(row);

      // Compute and Format Tick
      char[] tick = formatTick(y).toCharArray();

      // Insert Tick
      System.arraycopy(tick, 0, graph[row], 0, tick.length);

      // Insert Axis line. '┼' is used at the origin.
      graph[row][axisColumnIndex] = (y == 0) ? CROSS : LEFT_INTERSECT;
    }
  }


  /**
   * Adds the line to the graph.
   *
   * @param graph The graph.
   */
  private void drawLine(char[][] graph) {
    // The row closest to y when x = 0.
    int initialRow = determineRowAtYValue(series[0]);
    // Modify the axis to show the start.
    graph[initialRow][axisColumnIndex] = CROSS;

    for (int x = 0; x < series.length - 1; x++) {
      // The start and end locations of the line.
      int startRow = determineRowAtYValue(series[x]);
      int endRow = determineRowAtYValue(series[x + 1]);

      if (startRow == endRow) { // The line is horizontal.
        graph[startRow][lineIndex + x] = HORIZONTAL;
      } else { // The line has slope.
        // Draw the curved lines.
        graph[startRow][lineIndex + x] = (startRow < endRow) ? RIGHT_DOWN : RIGHT_UP;
        graph[endRow][lineIndex + x] = (startRow < endRow) ? DOWN_RIGHT : UP_RIGHT;

        // If the rows are more than 1 row apart, we need to fill in the gap with vertical lines.
        int lowerRow = Math.min(startRow, endRow);
        int upperRow = Math.max(startRow, endRow);
        for (int row = lowerRow + 1; row < upperRow; row++) {
          graph[row][lineIndex + x] = VERTICAL;
        }
      }
    }
  }


  /**
   * Determines the row closest to the given y-axis value.
   *
   * <p>((yValue - min) / range) creates a ratio -- how deep the y-value is into the range. Multiply that by the number
   * of rows to determine how deep the y-value is into the number of rows. Then invert it buy subtracting it from the
   * number of rows, since 0 is actually the top.</p>
   *
   * <p>1 is subtracted from numRows since it is a length, and we start at 0.</p>
   *
   * @param yValue The value of y.
   * @return The closest row to the given y-axis value.
   */
  private int determineRowAtYValue(double yValue) {
    return (numRows - 1) - (int) Math.round(((yValue - min) / range) * (numRows - 1));
  }


  /**
   * Determines the y-axis value corresponding to the given row.
   *
   * <p>Compute the current y value by starting with the maximum and subtracting how far down we are in rows. Splitting
   * the range into chunks based on the number of rows gives us how much to subtract per row.</p>
   *
   * <p>-1 from the number of rows because it is a length, and the last row index is actually numRows - 1.</p>
   *
   * @param row The row.
   * @return The y-axis value at the given row.
   */
  private double determineYValueAtRow(int row) {
    return max - (row * (range / (numRows - 1)));
  }


  /**
   * Formats the given value as a tick mark on the graph.
   *
   * <p>Pads the tick mark with the correct number of spaces in order to be {@link TextGraph#tickWidth} characters
   * long.</p>
   *
   * @param value The value of the tick mark.
   * @return The formatted tick mark.
   */
  private String formatTick(double value) {
    StringBuilder retval = new StringBuilder();

    String formattedValue = tickFormat.format(value);
    for (int i = 0; i < tickWidth - formattedValue.length(); i++) {
      retval.append(' ');
    }
    return retval.append(formattedValue).toString();
  }


  /**
   * Converts the 2D char array representation of the graph into a String representation.
   *
   * @param graph The 2D char array representation of the graph.
   * @return The String representation of the graph.
   */
  private String convertGraphToString(char[][] graph) {
    StringBuilder retval = new StringBuilder();

    for (char[] row : graph) {
      retval.append(row).append('\n');
    }

    if (this.isUsingLabels()) appendLabels(retval);

    return retval.toString();
  }


  /**
   * Append labels across the y-axis.
   *
   * @param builder The StringBuilder to which we append the axis
   */
  private void appendLabels(StringBuilder builder) {
    builder.append(createString(' ',tickWidth+2));
    int width = SeriesUtil.getMaxLength(labels)+1;
    for(int x=0;x< labels.length;x++){
      builder.append( StringUtil.fixedLength(labels[x], width, CENTER, ' '));
    }
    builder.append('\n');
  }


  /**
   * Create a string of repeating characters the given length in size.
   *
   * @param c the character to use
   * @param size the number of characters to be placed in the string
   * @return the string of the given size
   */
  private String createString(char c, int size) {
    StringBuffer retval = new StringBuffer();
    for(int x=0;x<size;x++){
      retval.append(c);
    }
    return retval.toString();
  }


  /**
   * Instruct the TextGraph to use the given labels.
   *
   * <p>This will result in the expansion for the data set by the factor of the longest string in the array. It is
   * therefore recommended to keep the labels short as possible.</p>
   *
   * @param labels the labels for the y-axis.
   * @return This instance.
   */
  public TextGraph withLabels(String[] labels) {
    this.labels = labels;
    return this;
  }


  /**
   * Make copies of data points in the given array to spread each point across the x-axis.
   *
   * @param series the series of data to replicate
   * @param factor the factor to increase the size of the data
   * @return a new array with replicated data.
   */
  private double[] stutterDataPoints(double[] series, int factor) {
    final double[] retval = new double[series.length * factor];
    int index = 0;
    for (int x = 0; x < series.length; x++) {
      for (int y = 0; y < factor; y++) {
        retval[index++] = series[x];
      }
    }
    return retval;
  }

}
