package coyote.nmea;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;


/**
 * Represents a time of day in 24-hour clock, i.e. the UTC time used as default
 * in NMEA 0183.
 */
public class NMEATime {

  // ISO 8601 time format pattern with time zone
  private static final String TIME_PATTERN = "%02d:%02d:%02d%+03d:%02d";

  // hour of day
  private int hour = 0;
  // minute of hour
  private int minutes = 0;
  // seconds of a minute, may include decimal sub-second in some sentences
  private double seconds = 0.0;
  // time zone offset hours
  private int offsetHours = 0;
  // time zone offset minutes
  private int offsetMinutes = 0;




  /**
   * Creates a new instance of {@code Time} using the current system
   * time.
   */
  public NMEATime() {
    final GregorianCalendar c = new GregorianCalendar();
    hour = c.get( Calendar.HOUR_OF_DAY );
    minutes = c.get( Calendar.MINUTE );
    seconds = c.get( Calendar.SECOND );
  }




  /**
   * Creates a new instance of Time with hours, minutes and seconds.
   *
   * @param hour Hour of day
   * @param min Minute of hour
   * @param sec Second of minute
   */
  public NMEATime( final int hour, final int min, final double sec ) {
    setHour( hour );
    setMinutes( min );
    setSeconds( sec );
  }




  /**
   * Creates a new instance of Time with time zone offsets.
   *
   * @param hour Hour of day
   * @param min Minute of hour
   * @param sec Second of minute
   * @param offsetHrs Time zone offset hours
   * @param offsetMin Time zone offset minutes
   */
  public NMEATime( final int hour, final int min, final double sec, final int offsetHrs, final int offsetMin ) {
    setHour( hour );
    setMinutes( min );
    setSeconds( sec );
    setOffsetHours( offsetHrs );
    setOffsetMinutes( offsetMin );
  }




  /**
   * Creates a new instance of {@code Time} based on given String.
   * Assumes the {@code hhmmss.sss} formatting used in NMEA sentences.
   *
   * @param time Timestamp String
   */
  public NMEATime( final String time ) {
    setHour( Integer.parseInt( time.substring( 0, 2 ) ) );
    setMinutes( Integer.parseInt( time.substring( 2, 4 ) ) );
    setSeconds( Double.parseDouble( time.substring( 4 ) ) );
  }




  /**
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals( final Object obj ) {
    if ( obj == this ) {
      return true;
    }
    if ( obj instanceof NMEATime ) {
      final NMEATime d = (NMEATime)obj;
      return ( ( d.getHour() == getHour() ) && ( d.getMinutes() == getMinutes() ) && ( d.getSeconds() == getSeconds() ) && ( d.getOffsetHours() == getOffsetHours() ) && ( d.getOffsetMinutes() == getOffsetMinutes() ) );
    }
    return false;
  }




  /**
   * Get the hour of day.
   *
   * @return the hour
   */
  public int getHour() {
    return hour;
  }




  /**
   * Get time as milliseconds since beginning of day.
   *
   * @return Milliseconds
   */
  public long getMilliseconds() {
    long m = Math.round( getSeconds() * 1000 );
    m += getMinutes() * 60 * 1000;
    m += getHour() * 3600 * 1000;
    return m;
  }




  /**
   * Get the minute of hour.
   *
   * @return the minute
   */
  public int getMinutes() {
    return minutes;
  }




  /**
   * Get time zone offset hours. Defaults to 0 (UTC).
   */
  public int getOffsetHours() {
    return offsetHours;
  }




  /**
   * Get time zone offset minutes. Defaults to 0 (UTC).
   */
  public int getOffsetMinutes() {
    return offsetMinutes;
  }




  /**
   * Get the second of minute.
   *
   * @return the second
   */
  public double getSeconds() {
    return seconds;
  }




  /**
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final String s = String.format( "%2d%2d%2f", hour, minutes, seconds );
    return s.hashCode();
  }




  /**
   * Set the hour of day.
   *
   * @param hour the hour to set
   * 
   * @throws IllegalArgumentException If hour value out of bounds 0..23
   */
  public void setHour( final int hour ) {
    if ( ( hour < 0 ) || ( hour > 23 ) ) {
      throw new IllegalArgumentException( "Valid hour value is between 0..23" );
    }
    this.hour = hour;
  }




  /**
   * Set the minute of hour.
   *
   * @param minutes the minute to set
   * 
   * @throws IllegalArgumentException If minutes value out of bounds 0..59
   */
  public void setMinutes( final int minutes ) {
    if ( ( minutes < 0 ) || ( minutes > 59 ) ) {
      throw new IllegalArgumentException( "Valid minutes value is between 0..59" );
    }
    this.minutes = minutes;
  }




  /**
   * Set time zone offset hours.
   *
   * @param hours Offset to set (-13..13)
   * 
   * @throws IllegalArgumentException If offset out of bounds.
   */
  public void setOffsetHours( final int hours ) {
    if ( ( hours < -13 ) || ( hours > 13 ) ) {
      throw new IllegalArgumentException( "Offset out of bounds [-13..13]" );
    }
    offsetHours = hours;
  }




  /**
   * Set time zone offset minutes.
   *
   * @param minutes Offset to set (-59..59)
   * 
   * @throws IllegalArgumentException If offset out of bounds.
   */
  public void setOffsetMinutes( final int minutes ) {
    if ( ( minutes < -59 ) || ( minutes > 59 ) ) {
      throw new IllegalArgumentException( "Offset out of bounds [-59..59]" );
    }
    offsetMinutes = minutes;
  }




  /**
   * Set seconds of minute.
   *
   * @param seconds Seconds to set
   * 
   * @throws IllegalArgumentException If seconds out of bounds ( {@code 0 &lt; seconds &lt; 60})
   */
  public void setSeconds( final double seconds ) {
    if ( ( seconds < 0 ) || ( seconds >= 60 ) ) {
      throw new IllegalArgumentException( "Invalid value for second (0 < seconds < 60)" );
    }
    this.seconds = seconds;
  }




  /**
   * Set the time by {@link java.util.Date}. The date information of is
   * ignored, only hours, minutes and seconds are relevant. Notice also that
   * time zone offset is not affected by this method because
   * {@link java.util.Date} does not contain zone offset.
   *
   * @param d Date
   */
  public void setTime( final Date d ) {

    final GregorianCalendar cal = new GregorianCalendar();
    cal.setTime( d );

    final double seconds = cal.get( Calendar.SECOND ) + ( cal.get( Calendar.MILLISECOND ) / 1000.0 );

    setHour( cal.get( Calendar.HOUR_OF_DAY ) );
    setMinutes( cal.get( Calendar.MINUTE ) );
    setSeconds( seconds );
  }




  /**
   * Convert to {@link java.util.Date}. Notice that time zone information is
   * lost in conversion as {@link java.util.Date} does not contain time zone.
   *
   * @param d Date that defines year, month and day for time.
   * @return A Date that is combination of specified Date and Time
   */
  public Date toDate( final Date d ) {

    final double seconds = getSeconds();
    final int fullSeconds = (int)Math.floor( seconds );
    final int milliseconds = (int)Math.round( ( seconds - fullSeconds ) * 1000 );

    final GregorianCalendar cal = new GregorianCalendar();
    cal.setTime( d );
    cal.set( Calendar.HOUR_OF_DAY, getHour() );
    cal.set( Calendar.MINUTE, getMinutes() );
    cal.set( Calendar.SECOND, fullSeconds );
    cal.set( Calendar.MILLISECOND, milliseconds );

    return cal.getTime();
  }




  /**
   * Returns the ISO 8601 representation of time ({@code hh:mm:ss+hh:mm}).
   */
  public String toISO8601() {
    final int hr = getHour();
    final int min = getMinutes();
    final int sec = (int)Math.floor( getSeconds() );
    final int tzHr = getOffsetHours();
    final int tzMin = getOffsetMinutes();
    return String.format( TIME_PATTERN, hr, min, sec, tzHr, tzMin );
  }




  /**
   * Returns the String representation of {@code Time}. Formats the time
   * in {@code hhmmss.sss} format used in NMEA 0183 sentences. Seconds
   * are presented with three decimals regardless of precision returned by
   * {@link #getSeconds()}.
   */
  @Override
  public String toString() {
    String str = String.format( "%02d%02d", getHour(), getMinutes() );

    final DecimalFormat nf = new DecimalFormat( "00.000" );
    final DecimalFormatSymbols dfs = new DecimalFormatSymbols();
    dfs.setDecimalSeparator( '.' );
    nf.setDecimalFormatSymbols( dfs );

    str += nf.format( getSeconds() );
    return str;
  }

}
