/*
 * Copyright (c) 2016 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 *
 * Contributors:
 *   Stephan D. Cote 
 *      - Initial concept and implementation
 */
package coyote.commons;

//import static org.junit.Assert.*;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TreeSet;

import org.junit.Test;


/**
 * 
 */
public class CronEntryTest {
  static DecimalFormat MILLIS = new DecimalFormat("000");
  private static final String DEFAULT_DATE_FORMAT = "yyyy/MM/dd HH:mm:ss";
  static SimpleDateFormat DATEFORMAT = new SimpleDateFormat(DEFAULT_DATE_FORMAT);




  /**
   * Get a formatted string representing the difference between the two times.
   * 
   * @param millis number of elapsed milliseconds.
   * 
   * @return formatted string representing weeks, days, hours minutes and seconds.
   */
  public static String formatElapsed(long millis) {
    if (millis < 0 || millis == Long.MAX_VALUE) {
      return "?";
    }

    long secondsInMilli = 1000;
    long minutesInMilli = secondsInMilli * 60;
    long hoursInMilli = minutesInMilli * 60;
    long daysInMilli = hoursInMilli * 24;
    long weeksInMilli = daysInMilli * 7;

    long elapsedWeeks = millis / weeksInMilli;
    millis = millis % weeksInMilli;

    long elapsedDays = millis / daysInMilli;
    millis = millis % daysInMilli;

    long elapsedHours = millis / hoursInMilli;
    millis = millis % hoursInMilli;

    long elapsedMinutes = millis / minutesInMilli;
    millis = millis % minutesInMilli;

    long elapsedSeconds = millis / secondsInMilli;
    millis = millis % secondsInMilli;

    StringBuilder b = new StringBuilder();

    if (elapsedWeeks > 0) {
      b.append(elapsedWeeks);
      if (elapsedWeeks > 1)
        b.append(" wks ");
      else
        b.append(" wk ");
    }
    if (elapsedDays > 0) {
      b.append(elapsedDays);
      if (elapsedDays > 1)
        b.append(" days ");
      else
        b.append(" day ");

    }
    if (elapsedHours > 0) {
      b.append(elapsedHours);
      if (elapsedHours > 1)
        b.append(" hrs ");
      else
        b.append(" hr ");
    }
    if (elapsedMinutes > 0) {
      b.append(elapsedMinutes);
      b.append(" min ");
    }
    b.append(elapsedSeconds);
    if (millis > 0) {
      b.append(".");
      b.append(MILLIS.format(millis));
    }
    b.append(" sec");

    return b.toString();
  }




  /**
   * Test method for {@link coyote.commons.CronEntry#parse(java.lang.String)}.
   */
  @Test
  public void testParse() {
    CronEntry subject = null;

    try {
      subject = CronEntry.parse(null);
      //System.out.println(subject);
    } catch (ParseException e) {
      fail(e.getMessage());
    }

    String pattern = "* * * * *";
    try {
      subject = CronEntry.parse(pattern);
      //System.out.println(subject);
    } catch (ParseException e) {
      fail(e.getMessage());
    }

    pattern = "? ? ? ? ?";
    try {
      subject = CronEntry.parse(pattern);
      //System.out.println(subject);
    } catch (ParseException e) {
      fail(e.getMessage());
    }

    pattern = "/15 3 * * ?";
    try {
      subject = CronEntry.parse(pattern);
      //System.out.println(subject);
    } catch (ParseException e) {
      fail(e.getMessage());
    }

    pattern = "*/15 3 */2 * 1-6";
    try {
      subject = CronEntry.parse(pattern);
      //System.out.println(subject);
    } catch (ParseException e) {
      fail(e.getMessage());
    }

    pattern = "B A D * *";
    try {
      subject = CronEntry.parse(pattern);
      fail("Did not detect invalid pattern of '" + pattern + "'");
    } catch (ParseException e) {}

    pattern = "";
    try {
      subject = CronEntry.parse(pattern);
      //System.out.println(subject);
    } catch (ParseException e) {
      fail(e.getMessage());
    }

    pattern = "* * * * * * * * * * * * * *";
    try {
      subject = CronEntry.parse(pattern);
      //System.out.println(subject);
    } catch (ParseException e) {
      fail(e.getMessage());
    }
  }




  /**
   * Test method for {@link coyote.commons.CronEntry#mayRunAt(java.util.Calendar)}.
   */
  @Test
  public void testMayRunAt() {
    StringBuffer b = new StringBuffer();
    Calendar cal = new GregorianCalendar();

    CronEntry subject = null;
    try {
      subject = CronEntry.parse(null);

      // set the minute pattern to the current minute
      subject.setMinutePattern(Integer.toString(cal.get(Calendar.MINUTE)));
      subject.setHourPattern(Integer.toString(cal.get(Calendar.HOUR_OF_DAY)));
      subject.setDayPattern(Integer.toString(cal.get(Calendar.DAY_OF_MONTH)));
      subject.setMonthPattern(Integer.toString(cal.get(Calendar.MONTH) + 1));
      subject.setDayOfWeekPattern(Integer.toString(cal.get(Calendar.DAY_OF_WEEK) - 1));

      //System.out.println( subject );
      assertTrue(subject.mayRunAt(cal));
    } catch (ParseException e) {
      fail(e.getMessage());
    }

  }




  /**
   * Test method for {@link coyote.commons.CronEntry#mayRunNow()}.
   */
  @Test
  public void testMayRunNow() {
    String pattern = "* * * * *";
    CronEntry subject = null;
    try {
      subject = CronEntry.parse(pattern);
      assertTrue(subject.mayRunNow());

      subject = CronEntry.parse(null);
      Calendar cal = new GregorianCalendar();
      subject.setMinutePattern(Integer.toString(cal.get(Calendar.MINUTE)));
      subject.setHourPattern(Integer.toString(cal.get(Calendar.HOUR_OF_DAY)));
      subject.setDayPattern(Integer.toString(cal.get(Calendar.DAY_OF_MONTH)));
      subject.setMonthPattern(Integer.toString(cal.get(Calendar.MONTH) + 1));
      subject.setDayOfWeekPattern(Integer.toString(cal.get(Calendar.DAY_OF_WEEK) - 1));
      assertTrue(subject.mayRunNow());

      //System.out.println( subject );      
    } catch (ParseException e) {
      fail(e.getMessage());
    }
  }




  /**
   * Test method for {@link coyote.commons.CronEntry#getNextTime()}.
   */
  @Test
  public void testGetNextTime() {
    CronEntry subject = new CronEntry();
    long millis;
    Calendar cal = new GregorianCalendar();

    cal.set(Calendar.MONTH, 0); // Java Calendar: 0=Jan
    cal.set(Calendar.DAY_OF_MONTH, 15);
    cal.set(Calendar.HOUR_OF_DAY, 11);
    cal.set(Calendar.MINUTE, 57);
    cal.set(Calendar.SECOND, 0);
    cal.set(Calendar.MILLISECOND, 0);

    try {
      // parse an entry which allows / accepts all dates and times
      subject = CronEntry.parse(null);

      // set the pattern to only allow February runs (one month later)
      subject.setMonthPattern(Integer.toString(2)); // Cron: 2=Feb
      //System.out.println( subject );

      // cannot run on 1/15
      assertFalse(subject.mayRunAt(cal));

      millis = subject.getNextTime(cal);
      long now = System.currentTimeMillis();

      //assertTrue( ( millis - now ) <= 3600000 );

      Date date = new Date(millis);
      //System.out.println( millis + " - " + date );
    } catch (ParseException e) {
      fail(e.getMessage());
    }
  }




  /**
   * Test method for {@link coyote.commons.CronEntry#getNextInterval()}.
   */
  @Test
  public void testGetNextInterval() {
    CronEntry subject = new CronEntry();
    long millis;
    Calendar cal = new GregorianCalendar();

    //System.out.println();
    //System.out.println( subject.dump() );

    // set the pattern to one hour in the future
    cal.add(Calendar.HOUR_OF_DAY, 1);
    subject.setHourPattern(Integer.toString(cal.get(Calendar.HOUR_OF_DAY))); // adjustment
    assertFalse(subject.mayRunNow());
    millis = subject.getNextInterval();
    // System.out.println( millis + " - " + formatElapsed( millis ) );
    assertTrue(millis <= 3600000);
    assertTrue(millis >= 0);
    //System.out.println();

    //System.out.println( "\r\n30 minute test Part 1" );
    subject = new CronEntry();
    subject.setMinutePattern("0,30");
    //System.out.println( subject.dump() );
    millis = subject.getNextInterval();
    //System.out.println( millis + " - " + formatElapsed( millis ) );
    assertTrue("30mP1 " + millis + "!<=1800000", millis <= 1800000);
    assertTrue(millis >= 0);
    //System.out.println();

    //System.out.println( "\r\n30 minute test Part 2" );
    subject = new CronEntry();
    subject.setMinutePattern("*/30");
    //System.out.println( subject.dump() );
    millis = subject.getNextInterval();
    //System.out.println( millis + " - " + formatElapsed( millis ) );
    assertTrue("30mP2 " + millis + "!<=1800000", millis <= 1800000);
    assertTrue(millis >= 0);
    //System.out.println();

    //System.out.println( "\r\n5 minute test" );
    subject = new CronEntry();
    subject.setMinutePattern("*/5");
    //System.out.println( subject.dump() );
    millis = subject.getNextInterval();
    //System.out.println( millis + " - " + formatElapsed( millis ) );
    assertTrue("15m " + millis + "!<=300000", millis <= 300000);
    assertTrue(millis >= 0);
    //System.out.println();

    subject = new CronEntry();
    int hr = cal.get(Calendar.HOUR_OF_DAY);
    hr = (hr < 23) ? hr + 1 : 0;
    String hrp = Integer.toString(hr);
    //System.out.println( "HRP:" + hrp );
    subject.setHourPattern(hrp); // adjustment
    //System.out.println( subject.dump() );
    millis = subject.getNextInterval();
    //System.out.println( millis + " - " + formatElapsed( millis ) );
    assertTrue("1d " + millis + "!<=86400000", millis <= 86400000);
    assertTrue(millis >= 0);
    //System.out.println();

  }




  /**
   *
   */
  @Test
  public void testParseRangeParam() {
    CronEntry subject = new CronEntry();
    try {
      subject.setHourPattern("30");
      fail("There are not 30 hours in a day");
    } catch (Exception e) {}
  }




  /**
   * Test method for {@link coyote.commons.CronEntry#getNext(TreeSet, int, int)}.
   */
  @Test
  public void testGetNext() {

    TreeSet<String> timemap = new TreeSet<String>();
    timemap.add("0");
    timemap.add("30");

    CronEntry subject = new CronEntry();
    int next = subject.getNext(timemap, 45, 59);
    assertTrue(next == 0);
    next = subject.getNext(timemap, 60, 59);
    assertTrue(next == 0);
    next = subject.getNext(timemap, -1, 59); // proper call to get 0
    assertTrue(next == 0);
    next = subject.getNext(timemap, 0, 59); // using 0 will miss 0
    assertTrue(next == 30);
    next = subject.getNext(timemap, 1, 59); // using 1 will miss 0
    assertTrue(next == 30);

    timemap.clear();
    timemap.add("12");
    timemap.add("13");
    timemap.add("0");

    // this is the preferred way to check for the start of a new period
    next = subject.getNext(timemap, -1, 59);
    assertTrue(next == 0);

    next = subject.getNext(timemap, 1, 59);
    assertTrue(next == 12);

    next = subject.getNext(timemap, 11, 59);
    assertTrue(next == 12);

    next = subject.getNext(timemap, 11, 15);//wrong size
    assertTrue(next == 12);
    next = subject.getNext(timemap, 11, 13);//wrong size
    assertTrue(next == 12);

  }




  @Test
  public void anotherTest() {
    CronEntry subject = new CronEntry();
    long millis;
    Calendar now = new GregorianCalendar();
    //now.add( Calendar.MINUTE, -15 );
    //System.out.println( "NOW:      " + DATEFORMAT.format( now.getTime() ) + " - " + CronEntry.toPattern( now ) );
    //System.out.println();

    Calendar cal = new GregorianCalendar();

    int hr = cal.get(Calendar.HOUR_OF_DAY);
    hr = (hr < 23) ? hr + 1 : 0;
    String hrp = Integer.toString(hr);
    //System.out.println( "HRP:" + hrp );

    // set the pattern to one hour in the future
    subject.setHourPattern(hrp); // adjustment
    //System.out.println(subject.dump());

    millis = subject.getNextTime(now);
    Date result = new Date(millis);

    //System.out.println();
    //System.out.println( "RESULT:   " + DATEFORMAT.format( result ) );
    //System.out.println( "INTERVAL: " + millis + " - " + CronEntryTest.formatElapsed( millis - nowmillis ) );
  }




  @Test
  public void range() {
    CronEntry subject = new CronEntry();

    try {
      subject.setHourPattern("24");
      fail("Allows invalid hour pattern");
    } catch (IllegalArgumentException expected) {
      // should be too large
    }

    try {
      subject.setMinutePattern("60");
      fail("Allows invalid minute pattern");
    } catch (IllegalArgumentException expected) {
      // should be too large
    }

    try {
      subject.setDayOfWeekPattern("7");
      fail("Allows invalid day of week pattern");
    } catch (IllegalArgumentException expected) {
      // should be too large
    }

    try {
      subject.setDayPattern("32");
      fail("Allows invalid day of week pattern");
    } catch (IllegalArgumentException expected) {
      // should be too large
    }

    try {
      subject.setMonthPattern("13");
      fail("Allows invalid month pattern");
    } catch (IllegalArgumentException expected) {
      // should be too large
    }

    try {
      subject.setHourPattern("-1");
      fail("Allows invalid hour pattern");
    } catch (IllegalArgumentException expected) {
      // should be too small
    }

    try {
      subject.setMinutePattern("-1");
      fail("Allows invalid minute pattern");
    } catch (IllegalArgumentException expected) {
      // should be too small
    }

    try {
      subject.setDayOfWeekPattern("-1");
      fail("Allows invalid day of week pattern");
    } catch (IllegalArgumentException expected) {
      // should be too small
    }

    try {
      subject.setDayPattern("-1");
      fail("Allows invalid day of week pattern");
    } catch (IllegalArgumentException expected) {
      // should be too small
    }

    try {
      subject.setMonthPattern("-1");
      fail("Allows invalid month pattern");
    } catch (IllegalArgumentException expected) {
      // should be too small
    }

    try {
      CronEntry.parse("60 * * * *");
      fail("Allows invalid minute pattern");
    } catch (ParseException expected) {
      // should be too large
    }

    try {
      CronEntry.parse("-1 * * * *");
      fail("Allows invalid minute pattern");
    } catch (ParseException expected) {
      // should be too small
    }

    try {
      CronEntry.parse("* 60 * * *");
      fail("Allows invalid minute pattern");
    } catch (ParseException expected) {
      // should be too large
    }

    try {
      CronEntry.parse("* -1 * * *");
      fail("Allows invalid minute pattern");
    } catch (ParseException expected) {
      // should be too small
    }

    try {
      CronEntry.parse("* * 32 * *");
      fail("Allows invalid day of month pattern");
    } catch (ParseException expected) {
      // should be too large
    }

    try {
      CronEntry.parse("* * -1 * *");
      fail("Allows invalid day of month pattern");
    } catch (ParseException expected) {
      // should be too small
    }

    try {
      CronEntry.parse("* * * 13 *");
      fail("Allows invalid month pattern");
    } catch (ParseException expected) {
      // should be too large
    }

    try {
      CronEntry.parse("* * * -1 *");
      fail("Allows invalid month pattern");
    } catch (ParseException expected) {
      // should be too small
    }

    try {
      CronEntry.parse("* * * * 7");
      fail("Allows invalid month pattern");
    } catch (ParseException expected) {
      // should be too large
    }

    try {
      CronEntry.parse("* * * * -1");
      fail("Allows invalid month pattern");
    } catch (ParseException expected) {
      // should be too small
    }

  }

}
