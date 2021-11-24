package coyote.dx.listener;

import coyote.commons.text.graph.TextGraph;
import coyote.commons.text.table.TextTable;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

public class EventTracker {
  private final Map<Integer, Long> eventCountsByHour = new HashMap<>();
  private final Map<String, Long> stringOccurrences = new HashMap<>();
  private String name = null;


  /**
   * Constrict an EventTracker with the given name.
   *
   * @param name The name of this tracker
   */
  public EventTracker(String name) {
    this.name = name;
    for (int i = 0; i < 24; eventCountsByHour.put(i++, 0L)) ;
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
    sampleDate(date);
    sampleOccurrence(value);
  }


  /**
   * Add the value to the occurrence data
   *
   * @param value the value to sample
   */
  private void sampleOccurrence(String value) {
    if (stringOccurrences.containsKey(value)) {
      long count = stringOccurrences.get(value);
      count++;
      stringOccurrences.put(value, count);
    } else {
      stringOccurrences.put(value, 1L);
    }
  }


  /**
   * Add the date to the tracked data.
   *
   * @param date the date to sample
   */
  private void sampleDate(Date date) {
    if (date != null) {
      LocalDateTime datetime = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
      int hour = datetime.getHour();
      long count = eventCountsByHour.get(hour);
      eventCountsByHour.put(hour, ++count);
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
    plotEventsByHour(b);
    showOccurrences(b, 25);
    return b.toString();
  }


  /**
   * Shows the number of occurrences of each value sorted by number of occurrences, decending.
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
    final double[] series = new double[24];
    for (int i = 0; i < 24; series[i] = (double) eventCountsByHour.get(i++)) ;
    buffer.append(TextGraph.fromSeries(series).withNumRows(20).plot());
    buffer.append("\n");
  }


}
