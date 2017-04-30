/*
 * Copyright (c) 2014 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 *
 * Contributors:
 *   Stephan D. Cote 
 *      - Initial concept and implementation
 */
package coyote.testdata;

import java.util.Calendar;
import java.util.Date;


/**
 * 
 */
public class DateGenerator extends AbstractGenerator {

  private Date start = null;
  private Date end = null;




  public DateGenerator() {}




  public DateGenerator( final Date minDate ) {
    this( minDate, new Date() );
  }




  public DateGenerator( final Date minDate, final Date maxDate ) {
    if ( minDate != null ) {
      start = minDate;
    } else {
      final Calendar cal = Calendar.getInstance();
      cal.add( Calendar.YEAR, -5 );
    }
    if ( maxDate != null ) {
      end = maxDate;
    } else {
      end = new Date();
    }

  }




  /**
   * @see coyote.testdata.AbstractGenerator#generateData(java.lang.String, coyote.testdata.Row)
   */
  @Override
  public void generateData( final String name, final Row row ) {
    Date retval = null;
    if ( start != null ) {
      if ( end != null ) {
        retval = this.getDateBetween( start, end );
      } else {
        retval = this.getDateBetween( start, new Date() );
      }
    } else {
      retval = this.getBirthDate();
    }
    row.set( name, retval );
  }




  public Date getBirthDate() {
    final Date base = new Date( 0 );
    return getDate( base, -365 * 15, 365 * 15 );
  }




  /**
   * Returns a random date which is in the range <code>baseDate</code> +
   * <code>minDaysFromDate</code> to <code>baseData</code> +
   * <code>maxDaysFromDate</code>. This method does not alter the time
   * component and the time is set to the time value of the base date.
   */
  public Date getDate( final Date baseDate, final int minDaysFromDate, final int maxDaysFromDate ) {
    final Calendar cal = Calendar.getInstance();
    cal.setTime( baseDate );
    final int diff = minDaysFromDate + ( random.nextInt( maxDaysFromDate - minDaysFromDate ) );
    cal.add( Calendar.DATE, diff );
    return cal.getTime();
  }




  /**
   * Builds a date from the year, month, day values passed in
   */
  public Date getDate( final int year, final int month, final int day ) {
    final Calendar cal = Calendar.getInstance();
    cal.clear();
    cal.set( year, month - 1, day, 0, 0, 0 );
    return cal.getTime();
  }




  /**
   * Returns a random date between two dates. This method will alter the time
   * component of the dates
   */
  public Date getDateBetween( final Date minDate, final Date maxDate ) {
    // this can break if seconds is an int
    long seconds = ( maxDate.getTime() - minDate.getTime() ) / 1000;
    seconds = (long)( random.nextDouble() * seconds );
    final Date result = new Date();
    result.setTime( minDate.getTime() + ( seconds * 1000 ) );
    return result;
  }

}
