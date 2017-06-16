/*
 * Copyright (c) 2004 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 *
 * Contributors:
 *   Stephan D. Cote 
 *      - Initial API and implementation
 */
package coyote.commons;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;


/**
 * Class DateUtil
 */
public class DateUtil {

  private static final DateFormat _DATE_TIME_FORMAT = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss" );
  private static final DateFormat _DATE_FORMAT = new SimpleDateFormat( "yyyy-MM-dd" );
  private static final DateFormat _TIME_FORMAT = new SimpleDateFormat( "HH:mm:ss" );

  private static final TimeZone GMT = TimeZone.getTimeZone( "GMT" );

  private static final String days[] = { "Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat" };

  private static final String months[] = { "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec" };

  private static final List<String> formatStrings = Arrays.asList(
      "yyyy-MM-dd'T'HH:mm:ss.SSSX",
      "yyyy-MM-dd'T'HH:mm:ss.SSS",
      "yyyy-MM-dd'T'HH:mm:ss",
      "yyyy-MM-dd HH:mm:ss.SSSX",
      "yyyy-MM-dd HH:mm:ss.SSS",
      "yyyy-MM-dd HH:mm:ss",
      "yyyy-MM-dd",
      "MM-dd-yyyy HH:mm:ss.SSSX",
      "MM-dd-yyyy HH:mm:ss.SSS",
      "MM-dd-yyyy HH:mm:ss",
      "MM/dd/yyyy HH:mm:ss",
      "EEE MMM dd HH:mm:ss zzz yyyy",
      "HH:mm:ss.SSSX",
      "HH:mm:ss.SSS",
      "HH:mm:ss",
      "HH:mm",
      "M/y", 
      "M-y", 
      "M/d/y", 
      "M-d-y", 
      "y/M/d", 
      "y-M-d");




  public static Date parseDateTime( String token ) {
    Date retval = null;
    try {
      retval = _DATE_TIME_FORMAT.parse( token );
    } catch ( final Exception e ) {
      retval = null;
    }
    return retval;
  }




  public static Date parseDate( String token ) {
    Date retval = null;
    try {
      retval = _DATE_FORMAT.parse( token );
    } catch ( final Exception e ) {
      retval = null;
    }
    return retval;
  }




  public static Date parseTime( String token ) {
    Date retval = null;
    try {
      retval = _TIME_FORMAT.parse( token );
    } catch ( final Exception e ) {
      retval = null;
    }
    return retval;
  }




  /**
   * This method tries several different formats to parsing a date.
   * 
   * <p>This method is useful if the actual format of the date is not known.
   * 
   * @param text the date time string to parse
   * 
   * @return the Date reference if parsing was successful or null if the text 
   *         could not be parsed into a date.
   */
  public static Date parse( String text ) {
    for ( String formatString : formatStrings ) {
      try {
        return new SimpleDateFormat( formatString ).parse( text );
      } catch ( ParseException e ) {}
    }
    return null;
  }




  /**
   * Formats the time portion of the given date in ISO8601 compatible format
   * with delimiters.
   *
   * @param date the date object to convert.
   *
   * @return String representing the time portion of the date in HH:mm:ss
   *         format.
   */
  public static String toExtendedTime( Date date ) {
    SimpleDateFormat formatter = new SimpleDateFormat( "HH':'mm':'ss" );
    return formatter.format( date );
  }




  /**
   * Method RFC822Format
   *
   * <p>From RFC 822:
   * <pre>
   * 5.  DATE AND TIME SPECIFICATION
   *
   * 5.1.  SYNTAX
   *
   * date-time   =  [ day &quot;,&quot; ] date time        ; dd mm yy
   *                                             ;  hh:mm:ss zzz
   *
   * day         =  &quot;Mon&quot;  / &quot;Tue&quot; /  &quot;Wed&quot;  / &quot;Thu&quot;
   *             /  &quot;Fri&quot;  / &quot;Sat&quot; /  &quot;Sun&quot;
   *
   * date        =  1*2DIGIT month 2DIGIT        ; day month year
   *                                             ;  e.g. 20 Jun 82
   *
   * month       =  &quot;Jan&quot;  /  &quot;Feb&quot; /  &quot;Mar&quot;  /  &quot;Apr&quot;
   *             /  &quot;May&quot;  /  &quot;Jun&quot; /  &quot;Jul&quot;  /  &quot;Aug&quot;
   *             /  &quot;Sep&quot;  /  &quot;Oct&quot; /  &quot;Nov&quot;  /  &quot;Dec&quot;
   *
   * time        =  hour zone                    ; ANSI and Military
   *
   * hour        =  2DIGIT &quot;:&quot; 2DIGIT [&quot;:&quot; 2DIGIT]
   *                                             ; 00:00:00 - 23:59:59
   *
   * zone        =  &quot;UT&quot;  / &quot;GMT&quot;                ; Universal Time
   *                                             ; North American : UT
   *             /  &quot;EST&quot; / &quot;EDT&quot;                ;  Eastern:  - 5/ - 4
   *             /  &quot;CST&quot; / &quot;CDT&quot;                ;  Central:  - 6/ - 5
   *             /  &quot;MST&quot; / &quot;MDT&quot;                ;  Mountain: - 7/ - 6
   *             /  &quot;PST&quot; / &quot;PDT&quot;                ;  Pacific:  - 8/ - 7
   *             /  1ALPHA                       ; Military: Z = UT;
   *                                             ;  A:-1; (J not used)
   *                                             ;  M:-12; N:+1; Y:+12
   *             / ( (&quot;+&quot; / &quot;-&quot;) 4DIGIT )        ; Local differential
   *                                             ;  hours+min. (HHMM)
   *
   * 5.2.  SEMANTICS
   *
   * If included, day-of-week must be the day implied by the date
   * specification.
   *
   * Time zone may be indicated in several ways.  &quot;UT&quot; is Univer Time (formerly
   * called &quot;Greenwich Mean Time&quot;); &quot;GMT&quot; is permitted as a reference to
   * Universal Time. The  military  standard uses a single character for each
   * zone. &quot;Z&quot; is Universal Time. &quot;A&quot; indicates one hour earlier, and &quot;M&quot;
   * indicates 12  hours  earlier; &quot;N&quot;  is  one  hour  later, and &quot;Y&quot; is 12
   * hours later. The letter &quot;J&quot; is not used. The other remaining two forms are
   * taken from ANSI standard X3.51-1975. One allows explicit indication of the
   * amount of offset from UT; the other uses common  3-character strings for
   * indicating time zones in North America.
   * </pre>
   *
   * @param date
   *
   * @return the formatted date
   */
  public static String RFC822Format( Date date ) {
    Calendar cal = GregorianCalendar.getInstance( GMT );

    if ( date != null ) {
      cal.setTime( date );
    }

    StringBuffer retval = new StringBuffer( 29 );
    retval.append( days[cal.get( Calendar.DAY_OF_WEEK ) - 1] );
    retval.append( ", " );
    retval.append( StringUtil.zeropad( cal.get( Calendar.DAY_OF_MONTH ), 2 ) );
    retval.append( " " );
    retval.append( months[cal.get( Calendar.MONTH )] );
    retval.append( " " );
    retval.append( StringUtil.zeropad( cal.get( Calendar.YEAR ), 4 ) );
    retval.append( " " );
    retval.append( StringUtil.zeropad( cal.get( Calendar.HOUR_OF_DAY ), 2 ) );
    retval.append( ":" );
    retval.append( StringUtil.zeropad( cal.get( Calendar.MINUTE ), 2 ) );
    retval.append( ":" );
    retval.append( StringUtil.zeropad( cal.get( Calendar.SECOND ), 2 ) );
    retval.append( " GMT" );

    return retval.toString();
  }




  /**
   * Method RFC822Format
   *
   * @return the formatted date
   */
  public static String RFC822Format() {
    return RFC822Format( new Date() );
  }




  /**
   * Return the current time in ISO8601 format adjusted to standard time.
   *
   * @return the formatted date
   */
  public static String ISO8601Format() {
    return ISO8601Format( new Date(), true, TimeZone.getDefault() );
  }




  /**
   * Return the ISO8601 formatted date-time string for the given number of
   * milliseconds past the epoch.
   *
   * @param millis the number of milliseconds past the epoch
   *
   * @return Properly ISO8601-formatted time string.
   */
  public static String ISO8601Format( long millis ) {
    return ISO8601Format( new Date( millis ), true, TimeZone.getDefault() );
  }




  /**
   * Return the given Date object in ISO8601 format adjusted to standard time.
   *
   * @param date the Date object to format
   *
   * @return Properly ISO8601-formatted time string.
   */
  public static String ISO8601Format( Date date ) {
    return ISO8601Format( date, true, TimeZone.getDefault() );
  }




  /**
   * Return the given Date object in ISO8601 format adjusted to standard time.
   *
   * @param date the Date object to format
   * @param adjust Adjust the time to standard time by removing the Daylight
   *          Savings Time offset.
   *
   * @return Properly ISO8601-formatted time string.
   */
  public static String ISO8601Format( Date date, boolean adjust ) {
    return ISO8601Format( date, adjust, TimeZone.getDefault() );
  }




  /**
   * Return the given Date object in ISO8601 format adjusted to standard GMT.
   *
   * <p>Take the given date, remove daylight savings time and the offset from
   * GMT resulting in a universally coordinated time in ISO8601 format.</p>
   *
   * @param date the Date object to format
   *
   * @return Properly ISO8601-formatted time string.
   */
  public static String ISO8601GMT( Date date ) {
    return ISO8601Format( date, true, GMT );
  }




  /**
   * Return the given Date in ISO8601 format optionally adjusting to standard
   * time.
   *
   * @param date the Date object to format
   * @param adjust Adjust the time to standard time by removing the Daylight
   *        Savings Time offset.
   * @param tz
   *
   * @return Properly ISO8601-formatted time string.
   */
  public static String ISO8601Format( Date date, boolean adjust, TimeZone tz ) {
    Calendar cal = GregorianCalendar.getInstance();

    if ( tz != null ) {
      cal.setTimeZone( tz );
    }

    if ( date != null ) {
      cal.setTime( date );
    } else {
      cal.setTime( new Date() );
    }

    if ( adjust ) {
      // Adjust Daylight time to standard time
      cal.add( Calendar.HOUR_OF_DAY, ( cal.get( Calendar.DST_OFFSET ) / ( 60 * 60 * 1000 ) ) * -1 );
    }

    StringBuffer retval = new StringBuffer();
    retval.append( StringUtil.zeropad( cal.get( Calendar.YEAR ), 4 ) );
    retval.append( StringUtil.zeropad( cal.get( Calendar.MONTH ) + 1, 2 ) );
    retval.append( StringUtil.zeropad( cal.get( Calendar.DATE ), 2 ) );
    retval.append( "T" );
    retval.append( StringUtil.zeropad( cal.get( Calendar.HOUR_OF_DAY ), 2 ) );
    retval.append( StringUtil.zeropad( cal.get( Calendar.MINUTE ), 2 ) );
    retval.append( StringUtil.zeropad( cal.get( Calendar.SECOND ), 2 ) );
    retval.append( StringUtil.zeropad( cal.get( Calendar.MILLISECOND ), 3 ) );

    int offset = ( cal.get( Calendar.ZONE_OFFSET ) / 1000 );
    int hours = offset / ( 60 * 60 );
    int minutes = offset - ( hours * ( 60 * 60 ) );

    if ( offset == 0 ) {
      retval.append( "Z" );
    } else {
      if ( offset < 0 ) {
        retval.append( "-" );

        hours *= -1;
      } else {
        retval.append( "+" );
      }

      retval.append( StringUtil.zeropad( hours, 2 ) );
      retval.append( StringUtil.zeropad( minutes, 2 ) );
    }

    return retval.toString();
  }




  /**
   * Print only the most significant portion of the time.
   *
   * <p>This is the two most significant units of time. Form will be something
   * like "3h 26m" indicating 3 hours 26 minutes and some insignificant number
   * of seconds. Formats are Xd Xh (days-hours), Xh Xm (Hours-minutes), Xm Xs
   * (minutes-seconds) and Xs (seconds).</p>
   *
   * @param seconds number of elapsed seconds NOT milliseconds.
   *
   * @return formatted string
   */
  public static String formatSignificantElapsedTime( final long seconds ) {
    final long days = seconds / 86400;
    final StringBuffer buffer = new StringBuffer();

    if ( days > 0 ) // Display days and hours
    {
      buffer.append( days );
      buffer.append( "d " );
      buffer.append( ( ( seconds / 3600 ) % 24 ) ); // hours
      buffer.append( "h" );

      return buffer.toString();
    }

    final int hours = (int)( ( seconds / 3600 ) % 24 );

    if ( hours > 0 ) // Display hours and minutes
    {
      buffer.append( hours );
      buffer.append( "h " );
      buffer.append( ( ( seconds / 60 ) % 60 ) ); // minutes
      buffer.append( "m" );

      return buffer.toString();
    }

    final int minutes = (int)( ( seconds / 60 ) % 60 );

    if ( minutes > 0 ) // Display minutes and seconds
    {
      buffer.append( minutes );
      buffer.append( "m " );
      buffer.append( ( seconds % 60 ) ); // seconds
      buffer.append( "s" );

      return buffer.toString();
    }

    final int secs = (int)( seconds % 60 );
    buffer.append( secs ); // seconds
    buffer.append( "s" );

    return buffer.toString();

  }

}