package coyote.nmea;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Calendar;
import java.util.GregorianCalendar;

import org.junit.Before;
import org.junit.Test;

import coyote.nmea.NMEADate;
import coyote.nmea.NMEATime;


/**
 * 
 */
public class DateTest {

  private NMEADate instance;

  private GregorianCalendar cal;




  @Before
  public void setUp() throws Exception {
    instance = new NMEADate();
    cal = new GregorianCalendar();
  }




  @Test
  public void testConstructor() {
    assertEquals( cal.get( Calendar.YEAR ), instance.getYear() );
    assertEquals( cal.get( Calendar.MONTH ) + 1, instance.getMonth() );
    assertEquals( cal.get( Calendar.DAY_OF_MONTH ), instance.getDay() );
  }




  @Test
  public void testConstructorWithValues() {
    NMEADate d = new NMEADate( 2010, 6, 15 );
    assertEquals( 2010, d.getYear() );
    assertEquals( 6, d.getMonth() );
    assertEquals( 15, d.getDay() );
  }




  @Test
  public void testConstructorWithString() {
    NMEADate d = new NMEADate( "150610" );
    assertEquals( 2010, d.getYear() );
    assertEquals( 6, d.getMonth() );
    assertEquals( 15, d.getDay() );
  }




  @Test
  public void testEqualsAfterInit() {
    NMEADate d = new NMEADate();
    assertTrue( d.equals( instance ) );
    NMEADate one = new NMEADate( 2010, 6, 15 );
    NMEADate two = new NMEADate( 2010, 6, 15 );
    assertTrue( one.equals( two ) );
  }




  @Test
  public void testEqualsItself() {
    assertTrue( instance.equals( instance ) );
  }




  @Test
  public void testEqualsWhenChanged() {

    final int y = 2011;
    final int m = 6;
    final int d = 15;
    final NMEADate a = new NMEADate( y, m, d );
    final NMEADate b = new NMEADate( y, m, d );

    a.setDay( b.getDay() - 1 );
    assertFalse( a.equals( b ) );

    b.setDay( a.getDay() );
    assertTrue( a.equals( b ) );

    a.setMonth( b.getMonth() - 1 );
    assertFalse( a.equals( b ) );

    b.setMonth( a.getMonth() );
    assertTrue( a.equals( b ) );

    a.setYear( b.getYear() - 1 );
    assertFalse( a.equals( b ) );

    b.setYear( a.getYear() );
    assertTrue( a.equals( b ) );
  }




  @Test
  public void testEqualsWrongType() {
    Object str = new String( "foobar" );
    Object dbl = new Double( 123 );
    assertFalse( instance.equals( str ) );
    assertFalse( instance.equals( dbl ) );
  }




  @Test
  public void testGetDay() {
    assertEquals( cal.get( Calendar.DAY_OF_MONTH ), instance.getDay() );
  }




  @Test
  public void testGetMonth() {
    assertEquals( cal.get( Calendar.MONTH ) + 1, instance.getMonth() );
  }




  @Test
  public void testGetYear() {
    assertEquals( cal.get( Calendar.YEAR ), instance.getYear() );
  }




  @Test
  public void testSetDay() {
    final int day = 10;
    instance.setDay( day );
    assertEquals( day, instance.getDay() );
  }




  @Test
  public void testSetDayOutOfBounds() {
    int day = 0;
    try {
      instance.setDay( day );
      fail( "Did not throw exception" );
    } catch ( IllegalArgumentException e ) {
      // pass
    }

    day = 32;
    try {
      instance.setDay( day );
      fail( "Did not throw exception" );
    } catch ( IllegalArgumentException e ) {
      // pass
    }
  }




  @Test
  public void testSetMonth() {
    final int month = 10;
    instance.setMonth( month );
    assertEquals( month, instance.getMonth() );
  }




  @Test
  public void testSetMonthOutOfBounds() {
    int month = 0;
    try {
      instance.setMonth( month );
      fail( "Did not throw exception" );
    } catch ( IllegalArgumentException e ) {
      // pass
    }

    month = 32;
    try {
      instance.setMonth( month );
      fail( "Did not throw exception" );
    } catch ( IllegalArgumentException e ) {
      // pass
    }
  }




  @Test
  public void testSetYearFiveDigits() {
    try {
      instance.setYear( 10000 );
      fail( "Did not throw IllegalArgumentException" );
    } catch ( IllegalArgumentException e ) {
      // pass
    }
  }




  @Test
  public void testSetYearFourDigit() {
    for ( int year = 1000; year < 10000; year++ ) {
      instance.setYear( year );
      assertEquals( year, instance.getYear() );
    }
  }




  @Test
  public void testSetYearNegative() {
    try {
      instance.setYear( -1 );
      fail( "Did not throw IllegalArgumentException" );
    } catch ( IllegalArgumentException e ) {
      // pass
    }
  }




  @Test
  public void testSetYearThreeDigits() {
    try {
      instance.setYear( 100 );
      fail( "Did not throw IllegalArgumentException" );
    } catch ( IllegalArgumentException e ) {
      // pass
    }

    try {
      instance.setYear( 999 );
      fail( "Did not throw IllegalArgumentException" );
    } catch ( IllegalArgumentException e ) {
      // pass
    }
  }




  @Test
  public void testSetYearTwoDigit() {
    int century = 2000;
    for ( int year = 0; year < 100; year++ ) {
      instance.setYear( year );
      assertEquals( ( century + year ), instance.getYear() );
      if ( year == NMEADate.PIVOT_YEAR ) {
        century = 1900;
      }
    }
  }




  @Test
  public void testToStringTwoDigitYear() {
    NMEADate d = new NMEADate( 13, 9, 2 );
    assertEquals( "020913", d.toString() );
  }




  @Test
  public void testToStringFourDigitYear() {
    NMEADate d = new NMEADate( 2013, 9, 2 );
    assertEquals( "020913", d.toString() );
  }




  @Test
  public void testToISO8601TwoDigitYear() {
    NMEADate d = new NMEADate( 13, 9, 2 );
    assertEquals( "2013-09-02", d.toISO8601() );
  }




  @Test
  public void testToISO8601FourDigitYear() {
    NMEADate d = new NMEADate( 2013, 9, 2 );
    assertEquals( "2013-09-02", d.toISO8601() );
  }




  @Test
  public void testToISO8601WithTime() {
    NMEADate d = new NMEADate( 2013, 9, 2 );
    NMEATime t = new NMEATime( 2, 7, 9 );
    assertEquals( "2013-09-02T02:07:09+00:00", d.toISO8601( t ) );
  }




  @Test
  public void testToISO8601WithTimeAndZeroZone() {
    NMEADate d = new NMEADate( 2013, 9, 2 );
    NMEATime t = new NMEATime( 2, 7, 9, 0, 0 );
    assertEquals( "2013-09-02T02:07:09+00:00", d.toISO8601( t ) );
  }




  @Test
  public void testToISO8601WithTimeAndPositiveOffset() {
    NMEADate d = new NMEADate( 2013, 9, 2 );
    NMEATime t = new NMEATime( 2, 7, 9, 2, 0 );
    assertEquals( "2013-09-02T02:07:09+02:00", d.toISO8601( t ) );
  }




  @Test
  public void testToISO8601WithTimeAndNegativeOffset() {
    NMEADate d = new NMEADate( 2013, 9, 2 );
    NMEATime t = new NMEATime( 2, 7, 9, -2, 5 );
    assertEquals( "2013-09-02T02:07:09-02:05", d.toISO8601( t ) );
  }

}
