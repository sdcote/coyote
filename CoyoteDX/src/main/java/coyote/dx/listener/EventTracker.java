package coyote.dx.listener;

import coyote.commons.StringUtil;
import coyote.commons.text.graph.TextGraph;
import coyote.commons.text.table.TextTable;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.regex.Pattern;

public class EventTracker {
  private final Map<Integer, Long> eventCountsByHour = new HashMap<>();
  private final Map<Integer, Long> eventCountsByDayOfMonth = new HashMap<>();
  private final Map<Integer, Long> filteredCountsByHour = new HashMap<>();
  private final Map<Integer, Long> filteredCountsByDayOfMonth = new HashMap<>();
  private final Map<String, Long> stringOccurrences = new HashMap<>();
  private final List<Pattern> includePatterns = new ArrayList<>();
  private final List<Pattern> excludePatterns = new ArrayList<>();
  private String name = null;
  private int occurrenceLimit = 0;


  /**
   * Construct an EventTracker with the given name.
   *
   * @param name The name of this tracker
   */
  public EventTracker(String name) {
    this.name = name;
    for (int i = 0; i < 24; eventCountsByHour.put(i++, 0L)) ;
    for (int i = 0; i < 31; eventCountsByDayOfMonth.put(i++, 0L)) ;
    for (int i = 0; i < 24; filteredCountsByHour.put(i++, 0L)) ;
    for (int i = 0; i < 31; filteredCountsByDayOfMonth.put(i++, 0L)) ;
  }


  /**
   * Add the given date and numeric value to the sample data.
   *
   * @param date  the date to sample
   * @param value the numeric value to sample
   */
  public void sample(Date date, double value) {
  }


  /**
   * Add the given date and string value to the sample data.
   *
   * @param date  the date to sample
   * @param value the string value to sample
   */
  public void sample(Date date, String value) {
    sampleDate(date, eventCountsByHour, eventCountsByDayOfMonth);
    if (occurrenceLimit > 0) sampleOccurrence(date, value);
  }


  /**
   * Add the value to the occurrence data
   *
   * @param value the value to sample
   */
  private void sampleOccurrence(Date date, String value) {
    String text = filterValue(value);
    if (text != null) {
      sampleDate(date, filteredCountsByHour, filteredCountsByDayOfMonth);

      if (stringOccurrences.containsKey(text)) {
        long count = stringOccurrences.get(text);
        count++;
        stringOccurrences.put(text, count);
      } else {
        stringOccurrences.put(text, 1L);
      }
    }
  }


  /**
   * Filter out unwanted values based on the set of include and exclude regex patterns.
   *
   * @param value the value to be sampled
   * @return the value if it passed the filter, or null if it was filtered out.
   */
  private String filterValue(String value) {
    String retval = null;

    if (includePatterns.size() > 0) {
      for (Pattern pattern : includePatterns) {
        if (pattern.matcher(value).find()) {
          retval = value;
          break;
        }
      }
    } else {
      retval = value;
    }

    if (retval != null) {
      boolean exclude = false;
      if (excludePatterns.size() > 0) {
        for (Pattern pattern : excludePatterns) {
          if (pattern.matcher(retval).find()) {
            exclude = true;
            break;
          }
        }
      }
      if (exclude) retval = null;
    }
    return retval;
  }


  /**
   * Add the date to the tracked data.
   *
   * @param date    the date to sample
   * @param dayMap  the map of values to record the hour of day for the occurrence
   * @param hourMap the map of values to record the day or month for the occurrence
   */
  private void sampleDate(Date date, Map<Integer, Long> hourMap, Map<Integer, Long> dayMap) {
    if (date != null) {
      LocalDateTime datetime = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
      int hour = datetime.getHour();
      long count = hourMap.get(hour);
      hourMap.put(hour, ++count);

      int dom = datetime.getDayOfMonth()-1;
      count = dayMap.get(dom);
      dayMap.put(dom, ++count);
    }
  }


  /**
   * Generate a report from this tracker.
   *
   * @return the formatted report of the data in this tracker.
   */
  @Override
  public String toString() {
    StringBuffer b = new StringBuffer();
    b.append("Total Events by Day Of Month:\n");
    plotEventsByDayOfMonth(b);
    b.append("\n");
    b.append("Total Events by Hour:\n");
    plotEventsByHour(b);
    if (occurrenceLimit > 0) {
      b.append("\n");
      b.append("Filtered Events by Day Of Month:\n");
      plotFilteredByDayOfMonth(b);
      b.append("\n");
      b.append("Filtered Events by Hour:\n");
      plotFilteredByHour(b);
      b.append("\n");
      b.append("Top " + occurrenceLimit + " Unique Values Ranked by Occurrence:\n");
      showOccurrences(b, occurrenceLimit);
    }
    return b.toString();
  }


  /**
   * Show the number of events by each day of the month.
   *
   * @param buffer the buffer to fill
   */
  private void plotEventsByDayOfMonth(StringBuffer buffer) {
    double[] series = new double[31];
    String[] labels = new String[31];
    for (int i = 0; i < 31; i++) {
      series[i] = (double) eventCountsByDayOfMonth.get(i);
      labels[i] = String.format("%02d", i + 1);
    }
    buffer.append(TextGraph.fromSeries(series).withNumRows(20).withLabels(labels).plot());
  }


  /**
   * Show the number of filtered events by each day of the month.
   *
   * @param buffer the buffer to fill
   */
  private void plotFilteredByDayOfMonth(StringBuffer buffer) {
    double[] series = new double[31];
    String[] labels = new String[31];
    for (int i = 0; i < 31; i++) {
      series[i] = (double) filteredCountsByDayOfMonth.get(i);
      labels[i] = String.format("%02d", i + 1);
    }
    buffer.append(TextGraph.fromSeries(series).withNumRows(20).withLabels(labels).plot());
  }


  /**
   * Shows the number of occurrences of each value sorted by number of occurrences, descending.
   *
   * @param buffer the buffer to fill
   * @param limit  the maximum number of rows to show
   */
  private void showOccurrences(StringBuffer buffer, int limit) {
    // Sort the stringOccurrences by descending value
    List<Map.Entry<String, Long>> list = new ArrayList<Map.Entry<String, Long>>(stringOccurrences.entrySet());
    Collections.sort(list, new Comparator<Map.Entry<String, Long>>() {
      public int compare(Map.Entry<String, Long> entry1, Map.Entry<String, Long> entry2) {
        return entry2.getValue().compareTo(entry1.getValue()); // reverse order
      }
    });
    Map<String, Long> mapSortedByValues = new LinkedHashMap<String, Long>();
    for (Map.Entry<String, Long> entry : list) {
      mapSortedByValues.put(entry.getKey(), entry.getValue());
    }

    String[] headers = new String[]{"Count", "Resource"};
    String[][] data = new String[limit][2];

    int recordCount = 0;
    for (Iterator i = mapSortedByValues.entrySet().iterator(); i.hasNext() && recordCount < limit; ) {
      Map.Entry me = (Map.Entry) i.next();
      data[recordCount][0] = me.getValue().toString();
      data[recordCount][1] = me.getKey().toString();
      recordCount++;
    }
    buffer.append(TextTable.fromData(headers, data));
    buffer.append("\n");
  }


  /**
   * Show the number of events by each hour.
   *
   * @param buffer the buffer to fill
   */
  private void plotEventsByHour(StringBuffer buffer) {
    double[] series = new double[24];
    String[] labels = new String[24];
    for (int i = 0; i < 24; i++) {
      series[i] = (double) eventCountsByHour.get(i);
      labels[i] = String.format("%02d", i);
    }
    buffer.append(TextGraph.fromSeries(series).withNumRows(20).withLabels(labels).plot());
  }


  /**
   * Show the number of filtered events by each hour.
   *
   * @param buffer the buffer to fill
   */
  private void plotFilteredByHour(StringBuffer buffer) {
    double[] series = new double[24];
    String[] labels = new String[24];
    for (int i = 0; i < 24; i++) {
      series[i] = (double) filteredCountsByHour.get(i);
      labels[i] = String.format("%02d", i);
    }
    buffer.append(TextGraph.fromSeries(series).withNumRows(20).withLabels(labels).plot());
  }


  /**
   * Set the limit of occurrence tracking.
   *
   * @param limit the number of unique occurrences to report. 0 (default) means do not track or report.
   */
  public void setLimit(int limit) {
    this.occurrenceLimit = limit;
  }


  /**
   * Add the given regular expression to the list of patterns used to control what values are tracked.
   *
   * @param regexPattern A java regular expression
   */
  public void addIncludePattern(String regexPattern) {
    if (StringUtil.isNotEmpty(regexPattern)) includePatterns.add(Pattern.compile(regexPattern));
  }


  /**
   * Add the given regular expression to the list of patterns used to exclude values to be tracked.
   *
   * @param regexPattern A java regular expression
   */
  public void addExcludePattern(String regexPattern) {
    if (StringUtil.isNotEmpty(regexPattern)) excludePatterns.add(Pattern.compile(regexPattern));
  }

}
