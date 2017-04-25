package coyote.nmea.sentence;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Calendar;
import java.util.GregorianCalendar;

import org.junit.Before;
import org.junit.Test;

import coyote.nmea.NMEADate;
import coyote.nmea.NMEATime;
import coyote.nmea.TalkerId;


/**
 * 
 */
public class ZDATest {

  /** Example sentence */
  public static final String EXAMPLE = "$GPZDA,032915,07,08,2004,00,00*4D";

  private ZDASentence empty;
  private ZDASentence zda;




  @Before
  public void setUp() {
    try {
      empty = new ZDASentenceImpl( TalkerId.GP );
      zda = new ZDASentenceImpl( EXAMPLE );
    } catch ( Exception e ) {
      fail( e.getMessage() );
    }
  }




  @Test
  public void testConstructor() {
    assertEquals( 6, empty.getFieldCount() );
  }




  @Test
  public void testGetDate() {
    NMEADate expected = new NMEADate( 2004, 8, 7 );
    NMEADate parsed = zda.getDate();
    assertEquals( expected, parsed );
  }




  @Test
  public void testGetDay() {
    assertEquals( 7, zda.getDate().getDay() );
  }




  @Test
  public void testGetLocalZoneHours() {
    assertEquals( 0, zda.getLocalZoneHours() );
  }




  @Test
  public void testSetLocalZoneHours() {
    final int hours = 7;
    zda.setLocalZoneHours( hours );
    assertTrue( zda.toString().contains( ",2004,07,00*" ) );
    assertEquals( hours, zda.getLocalZoneHours() );
  }




  @Test
  public void testGetLocalZoneMinutes() {
    assertEquals( 0, zda.getLocalZoneMinutes() );
  }




  @Test
  public void testSetLocalZoneMinutes() {
    final int min = 9;
    zda.setLocalZoneMinutes( min );
    assertTrue( zda.toString().contains( ",2004,00,09*" ) );
    assertEquals( min, zda.getLocalZoneMinutes() );
  }




  @Test
  public void testGetMonth() {
    assertEquals( 8, zda.getDate().getMonth() );
  }




  @Test
  public void testGetTime() {
    NMEATime t = zda.getTime();
    assertNotNull( t );
    assertEquals( 3, t.getHour() );
    assertEquals( 29, t.getMinutes() );
    assertEquals( 15.0, t.getSeconds(), 0.1 );
  }




  @Test
  public void testGetYear() {
    assertEquals( 2004, zda.getDate().getYear() );
  }




  @Test
  public void testSetDate() {
    zda.setDate( new NMEADate( 10, 6, 9 ) );
    assertTrue( zda.toString().contains( ",032915,09,06,2010,00," ) );
    zda.setDate( new NMEADate( 85, 12, 11 ) );
    assertTrue( zda.toString().contains( ",032915,11,12,1985,00," ) );
  }




  @Test
  public void testSetTime() {
    // 09:08:07.6
    NMEATime t = new NMEATime( 9, 8, 7.6 );
    zda.setTime( t );
    assertTrue( zda.toString().startsWith( "$GPZDA,090807.600,07," ) );
  }




  @Test
  public void testSetTimeAndLocalZone() {
    // 09:08:07.6+01:02
    NMEATime t = new NMEATime( 9, 8, 7.6, 1, 2 );
    zda.setTimeAndLocalZone( t );
    assertEquals( 1, zda.getLocalZoneHours() );
    assertEquals( 2, zda.getLocalZoneMinutes() );
    assertTrue( zda.toString().startsWith( "$GPZDA,090807.600,07," ) );
    assertTrue( zda.toString().contains( "2004,01,02*" ) );
  }




  @Test
  public void testToDate() {

    NMEADate d = new NMEADate( 2010, 6, 15 );
    NMEATime t = new NMEATime( 12, 15, 30.246, 2, 0 );
    zda.setDate( d );
    zda.setTime( t );

    GregorianCalendar cal = new GregorianCalendar();
    cal.set( Calendar.YEAR, 2010 );
    cal.set( Calendar.MONTH, 5 );
    cal.set( Calendar.DAY_OF_MONTH, 15 );
    cal.set( Calendar.HOUR_OF_DAY, 12 );
    cal.set( Calendar.MINUTE, 15 );
    cal.set( Calendar.SECOND, 30 );
    cal.set( Calendar.MILLISECOND, 246 );

    java.util.Date result = zda.toDate();
    java.util.Date expected = cal.getTime();

    assertEquals( expected, result );
    assertEquals( expected.getTime(), result.getTime() );
  }
}
