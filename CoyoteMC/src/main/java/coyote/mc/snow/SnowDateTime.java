/*
 * Copyright (c) 2015 Stephan D. Cote' - All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the MIT License which accompanies this distribution, and is
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.mc.snow;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;


/**
 * This class holds a SNow DateTime field.
 *
 * <p>It can convert a SNow DateTime string to a Java Date and vice versa.</p>
 */
public class SnowDateTime implements Comparable<SnowDateTime> {

  private static final TimeZone gmt = TimeZone.getTimeZone("GMT");
  private static DateFormat datetimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
  private static DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
  private static DateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");


  private final Date date;

  {
    datetimeFormat.setTimeZone(gmt);
    dateFormat.setTimeZone(gmt);
  }


  /**
   * Constructor using a date value
   *
   * @param value the date to wrap
   */
  public SnowDateTime(final Date value) {
    date = value;
  }


  /**
   * Convert a string to a date.
   *
   * @param value a string in the format of yyyy-MM-dd HH:mm:ss
   * @throws ParseException if the string could not be parsed into a date
   */
  public SnowDateTime(final String value) throws ParseException {
    date = datetimeFormat.parse(value);
  }


  /**
   * Klugey way to support alternate datetime formats...likely to be removed in subsequent versions.
   *
   * @param value      the date value to parse
   * @param timeFormat the time format used to parse the value
   * @throws ParseException if the string could not be parsed into a date
   * @deprecated - this will be removed in future versions
   */
  @Deprecated
  public SnowDateTime(final String value, final DateFormat timeFormat) throws ParseException {
    datetimeFormat = timeFormat;
    datetimeFormat.setTimeZone(gmt);
    date = datetimeFormat.parse(value);
  }


  @Override
  public int compareTo(final SnowDateTime another) {
    return date.compareTo(another.toDate());
  }


  public boolean equals(final SnowDateTime another) {
    return date.equals(another);
  }


  /**
   * @return the date value this represents
   */
  public Date toDate() {
    return date;
  }


  /**
   * @return The date represented in the yyyy-MM-dd format
   */
  public String toDateFormat() {
    return dateFormat.format(date);
  }


  /**
   * @return The date represented in the yyyy-MM-dd HH:mm:ss format
   */
  public String toDateTimeFormat() {
    return datetimeFormat.format(date);
  }


  /**
   * @return The date represented in the HH:mm:ss format
   */
  public String toTimeFormat() {
    return timeFormat.format(date);
  }


  /**
   * @return The date represented in the yyyy-MM-dd HH:mm:ss format
   * @see Object#toString()
   */
  @Override
  public String toString() {
    return toDateTimeFormat();
  }


  /**
   * @return a string ServiceNow can use to limit queries - Note the time is not included
   */
  public String toQueryFormat() {
    return "javascript:gs.dateGenerate('" + dateFormat.format(date) + "','00:00:00')";
  }

}
