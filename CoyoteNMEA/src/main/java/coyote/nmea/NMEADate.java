package coyote.nmea;

import java.util.Calendar;
import java.util.GregorianCalendar;

import coyote.nmea.sentence.DateSentence;


/**
 * Represents a calendar date (day-month-year) transmitted in sentences that
 * implement {@link DateSentence}.
 */
public class NMEADate {

  // ISO 8601 date format pattern
  private static final String DATE_PATTERN = "%d-%02d-%02d";

  /**
   * A pivot value that is used to determine century for two-digit year
   * values. Two-digit values lower than or equal to pivot are assigned to
   * 21th century, while greater values are assigned to 20th century.
   */
  public static final int PIVOT_YEAR = 50;

  // day of month 1..31
  private int day;
  // month 1..12
  private int month;
  // four-digit year
  private int year;




  /**
   * Creates a new instance of {@code Date} using the current date.
   */
  public NMEADate() {
    final GregorianCalendar c = new GregorianCalendar();
    year = c.get( Calendar.YEAR );
    month = c.get( Calendar.MONTH ) + 1;
    day = c.get( Calendar.DAY_OF_MONTH );
  }




  /**
   * Constructor with date values.
   *
   * @param year Year, two or four digit value [0..99] or [1000..9999]
   * @param month Month [1..12]
   * @param day Day [1..31]
   * 
   * @throws IllegalArgumentException If any of the parameter is out of
   *             bounds.
   */
  public NMEADate( final int year, final int month, final int day ) {
    setYear( year );
    setMonth( month );
    setDay( day );
  }




  /**
   * Creates a new instance of {@code Date}, assumes the default NMEA
   * 0183 date formatting, {@code ddmmyy} or {@code ddmmyyyy}.
   */
  public NMEADate( final String date ) {
    setDay( Integer.parseInt( date.substring( 0, 2 ) ) );
    setMonth( Integer.parseInt( date.substring( 2, 4 ) ) );
    setYear( Integer.parseInt( date.substring( 4 ) ) );
  }




  /**
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals( final Object obj ) {
    if ( obj == this ) {
      return true;
    }
    if ( obj instanceof NMEADate ) {
      final NMEADate d = (NMEADate)obj;
      if ( ( d.getDay() == getDay() ) && ( d.getMonth() == getMonth() ) && ( d.getYear() == getYear() ) ) {
        return true;
      }
    }
    return false;
  }




  /**
   * Get day of month.
   *
   * @return the day
   */
  public int getDay() {
    return day;
  }




  /**
   * Get month, valid values are 1-12 where 1 denotes January, 2 denotes
   * February etc.
   *
   * @return the month
   */
  public int getMonth() {
    return month;
  }




  /**
   * Get year. 
   * 
   * <p>The date fields in NMEA 0183 may present year by using either two or 
   * four digits. In case of only two digits, the century is determined by 
   * comparing the value against {@link #PIVOT_YEAR}. Values lower than or 
   * equal to pivot are added to 2000, while values greater than pivot are 
   * added to 1900.
   *
   * @return The four-digit year
   * 
   * @see #PIVOT_YEAR
   */
  public int getYear() {
    return year;
  }




  /**
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    return toISO8601().hashCode();
  }




  /**
   * Set day of month.
   *
   * @param day the day to set
   */
  public void setDay( final int day ) {
    if ( ( day < 1 ) || ( day > 31 ) ) {
      throw new IllegalArgumentException( "Day out of bounds [1..31]" );
    }
    this.day = day;
  }




  /**
   * Get month, valid values are 1-12 where 1 denotes January, 2 denotes
   * February etc.
   *
   * @param month the month to set
   * 
   * @throws IllegalArgumentException If specified value is out of bounds [1..12]
   */
  public void setMonth( final int month ) {
    if ( ( month < 1 ) || ( month > 12 ) ) {
      throw new IllegalArgumentException( "Month value out of bounds [1..12]" );
    }
    this.month = month;
  }




  /**
   * Set year.
   * 
   * <p>The date fields in NMEA 0183 may present year by using either two or 
   * four digits. In case of only two digits, the century is determined by 
   * comparing the value against {@link #PIVOT_YEAR}. Values lower than or 
   * equal to pivot are added to 2000, while values greater than pivot are 
   * added to 1900.
   *
   * @param year Year to set, two or four digit value.
   * 
   * @see #PIVOT_YEAR
   * 
   * @throws IllegalArgumentException If specified value is negative or three-
   *         digit value.
   */
  public void setYear( final int year ) {
    if ( ( year < 0 ) || ( ( year > 99 ) && ( year < 1000 ) ) || ( year > 9999 ) ) {
      throw new IllegalArgumentException( "Year must be two or four digit value" );
    }
    if ( ( year < 100 ) && ( year > PIVOT_YEAR ) ) {
      this.year = 1900 + year;
    } else if ( ( year < 100 ) && ( year <= PIVOT_YEAR ) ) {
      this.year = 2000 + year;
    } else {
      this.year = year;
    }
  }




  /**
   * Converts to {@link java.util.Date}, time of day set to 00:00:00.000.
   *
   * @return java.util.Date
   */
  public java.util.Date toDate() {
    final GregorianCalendar cal = new GregorianCalendar();
    cal.set( Calendar.YEAR, getYear() );
    cal.set( Calendar.MONTH, getMonth() - 1 );
    cal.set( Calendar.DAY_OF_MONTH, getDay() );
    cal.set( Calendar.HOUR_OF_DAY, 0 );
    cal.set( Calendar.MINUTE, 0 );
    cal.set( Calendar.SECOND, 0 );
    cal.set( Calendar.MILLISECOND, 0 );
    return cal.getTime();
  }




  /**
   * Returns the date in ISO 8601 format ({@code yyyy-mm-dd}).
   */
  public String toISO8601() {
    return String.format( DATE_PATTERN, getYear(), getMonth(), getDay() );
  }




  /**
   * Returns a timestamp in ISO 8601 format ({@code yyyy-mm-ddThh:mm:ss+hh:mm}).
   *
   * @param t Time to format with date
   */
  public String toISO8601( final NMEATime t ) {
    return toISO8601().concat( "T" ).concat( t.toISO8601() );
  }




  /**
   * Returns the String representation of {@code Date}. 
   * 
   * <p>Formats the date in {@code ddmmyy} format used in NMEA 0183 
   * sentences.
   */
  @Override
  public String toString() {
    final int y = getYear();
    final String ystr = String.valueOf( y );
    final String year = ystr.substring( 2 );
    final String date = String.format( "%02d%02d%s", getDay(), getMonth(), year );
    return date;
  }

}
