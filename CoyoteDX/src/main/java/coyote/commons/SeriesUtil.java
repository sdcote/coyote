package coyote.commons;

import java.util.Arrays;

public class SeriesUtil {

  /**
   * From the series, determines the minimum and maximum values.
   *
   * @param series The series.
   * @return An array of size 2, containing the minimum value of the series at index 0, and the maximum at index 1.
   * @throws IllegalArgumentException If the series does not contain at least one value, or is null.
   */
  public static double[] getMinAndMaxValues(double[] series) {
    if (series == null || series.length == 0)
      throw new IllegalArgumentException("The series must have at least one value.");

    double[] retval = new double[]{Double.MAX_VALUE, Double.MIN_VALUE};

    Arrays.stream(series).forEach(value -> {
      retval[0] = Math.min(retval[0], value);
      retval[1] = Math.max(retval[1], value);
    });

    return retval;
  }


  /**
   * Determine the maximum length of the strings in the given array.
   *
   * @param series the series of strings to check
   * @return the length of the longest string in the series.
   */
  public static int getMaxLength(String[] series) {
    int retval = 0;
    for (int row = 0; row < series.length; row++) {
      if (series[row].length() > retval) retval = series[row].length();
    }
    return retval;
  }

}

