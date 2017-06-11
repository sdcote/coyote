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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;


/**
 * 
 */
public class CronEntryStepper {
  private static final String DEFAULT_DATE_FORMAT = "yyyy/MM/dd HH:mm:ss";

  static SimpleDateFormat DATEFORMAT = new SimpleDateFormat( DEFAULT_DATE_FORMAT );




  /**
   * @param args
   */
  public static void main( String[] args ) {
    CronEntry subject = new CronEntry();
    long millis;
    Calendar cal = new GregorianCalendar();

    cal.set( Calendar.MONTH, 0 );// 0=Jan in Java Calendar <sigh/>
    cal.set( Calendar.DAY_OF_MONTH, 15 );
    cal.set( Calendar.HOUR_OF_DAY, 11 );
    cal.set( Calendar.MINUTE, 57 );
    cal.set( Calendar.SECOND, 0 );
    cal.set( Calendar.MILLISECOND, 0 );
    System.out.println(cal.getTime());
    System.out.println(CronEntry.toPattern( cal ));

    try {
      // parse an entry which allows / accepts all dates and times
      subject = CronEntry.parse( null );

      // set the pattern to only allow February runs one month later
      subject.setMonthPattern( Integer.toString( 2 ) ); //  (Jan=0,Feb=1)
      System.out.println( subject );

      System.out.println(CronEntry.toPattern( cal ));
      // cannot run on 1/15
      assertFalse( subject.mayRunAt( cal ));

      millis = subject.getNextTime(cal);
      long now = System.currentTimeMillis();
      //assertTrue( ( millis - now ) <= 3600000 );

      Date date = new Date(millis);
      System.out.println( millis + " - " + date );
    } catch ( ParseException e ) {
      fail( e.getMessage() );
    }
  }

}
