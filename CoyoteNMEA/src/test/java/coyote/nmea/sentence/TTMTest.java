package coyote.nmea.sentence;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Test;

import coyote.nmea.AcquisitionType;
import coyote.nmea.NMEATime;
import coyote.nmea.TalkerId;
import coyote.nmea.TargetStatus;


/**
 * 
 */
public class TTMTest {

  /** Example sentence */
  public static final String EXAMPLE = "$RATTM,11,25.3,13.7,T,7.0,20.0,T,10.1,20.2,N,NAME,Q,,175550.24,A*34";

  TTMSentenceImpl empty;
  TTMSentenceImpl ttm;




  @Before
  public void setUp() {
    try {
      empty = new TTMSentenceImpl( TalkerId.RA );
      ttm = new TTMSentenceImpl( EXAMPLE );
    } catch ( Exception e ) {
      fail( e.getMessage() );
    }
  }




  @Test
  public void testConstructor() {
    assertEquals( 15, empty.getFieldCount() );
  }




  @Test
  public void testGetNumber() {
    assertEquals( 11, ttm.getNumber() );
  }




  @Test
  public void testGetDistance() {
    assertEquals( 25.3, ttm.getDistance(), 0.001 );
  }




  @Test
  public void testGetBearing() {
    assertEquals( 13.7, ttm.getBearing(), 0.001 );
  }




  @Test
  public void testGetSpeed() {
    assertEquals( 7.0, ttm.getSpeed(), 0.001 );
  }




  @Test
  public void testGetCourse() {
    assertEquals( 20.0, ttm.getCourse(), 0.001 );
  }




  @Test
  public void testGetDistanceOfCPA() {
    assertEquals( 10.1, ttm.getDistanceOfCPA(), 0.001 );
  }




  @Test
  public void testGetTimeToCPA() {
    assertEquals( 20.2, ttm.getTimeToCPA(), 0.001 );
  }




  @Test
  public void testGetName() {
    assertEquals( "NAME", ttm.getName() );
  }




  @Test
  public void testGetStatus() {
    assertEquals( TargetStatus.QUERY, ttm.getStatus() );
  }




  @Test
  public void testGetTime() {
    NMEATime t = ttm.getTime();
    assertNotNull( t );
    assertEquals( 17, t.getHour() );
    assertEquals( 55, t.getMinutes() );
    assertEquals( 50.24, t.getSeconds(), 0.001 );
  }




  @Test
  public void testGetAcquisitionType() {
    assertEquals( AcquisitionType.AUTO, ttm.getAcquisitionType() );
  }




  @Test
  public void testSetNumber() {
    final int number = 90;
    ttm.setNumber( number );
    assertTrue( ttm.toString().contains( ",90," ) );
  }




  @Test
  public void testSetDistance() {
    ttm.setDistance( 56.4 );
    assertTrue( ttm.toString().contains( ",56.4," ) );
    assertTrue( ttm.toString().contains( ",N," ) );
  }




  @Test
  public void testSetBearing() {
    ttm.setBearing( 34.1 );
    assertTrue( ttm.toString().contains( ",34.1,T," ) );
  }




  @Test
  public void testSetSpeed() {
    ttm.setBearing( 44.1 );
    assertTrue( ttm.toString().contains( ",44.1," ) );
    assertTrue( ttm.toString().contains( ",N," ) );
  }




  @Test
  public void testSetCourse() {
    ttm.setCourse( 234.9 );
    assertTrue( ttm.toString().contains( ",234.9,T," ) );
  }




  @Test
  public void testSetDistanceOfCPA() {
    ttm.setDistanceOfCPA( 55.2 );
    assertTrue( ttm.toString().contains( ",55.2," ) );
  }




  @Test
  public void testSetTimeToCPA() {
    ttm.setTimeToCPA( 15.0 );
    assertTrue( ttm.toString().contains( ",15.0," ) );
  }




  @Test
  public void testSetName() {
    ttm.setName( "TEST" );
    assertTrue( ttm.toString().contains( ",TEST," ) );
  }




  @Test
  public void testSetStatus() {
    ttm.setStatus( TargetStatus.LOST );
    assertTrue( ttm.toString().contains( ",T," ) );
  }




  @Test
  public void testSetReferenceTrue() {
    ttm.setReference( true );
    assertTrue( ttm.toString().contains( ",R," ) );
  }




  @Test
  public void testSetReferenceFalse() {
    ttm.setReference( false );
    assertTrue( !ttm.toString().contains( ",R," ) );
  }




  @Test
  public void testSetTime() {
    NMEATime t = new NMEATime( 1, 2, 3.45 );
    ttm.setTime( t );
    assertTrue( ttm.toString().contains( ",010203.45," ) );
  }




  @Test
  public void testSetAcquisitionType() {
    ttm.setAcquisitionType( AcquisitionType.MANUAL );
    assertTrue( ttm.toString().contains( ",M*" ) );
  }

}
