package coyote.nmea.sentence;

//import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Test;

import coyote.nmea.CompassPoint;
import coyote.nmea.DataStatus;
import coyote.nmea.NMEATime;
import coyote.nmea.Position;
import coyote.nmea.TalkerId;


/**
 * Tests the GLL sentence.
 */
public class GLLTest {

  /**
   * Example sentence
   */
  public static final String EXAMPLE = "$GPGLL,6011.552,N,02501.941,E,120045,A*26";

  private GLLSentence empty;
  private GLLSentence instance;




  @Before
  public void setUp() {
    try {
      empty = new GLLSentenceImpl( TalkerId.GP );
      instance = new GLLSentenceImpl( EXAMPLE );
    } catch ( final Exception e ) {
      fail( e.getMessage() );
    }
  }




  @Test
  public void testConstructor() {
    assertEquals( 6, empty.getFieldCount() );
  }




  @Test
  public void testGetDataStatus() {
    assertEquals( DataStatus.ACTIVE, instance.getStatus() );
  }




  @Test
  public void testGetPosition() {
    final double lat = 60 + ( 11.552 / 60 );
    final double lon = 25 + ( 1.941 / 60 );

    final Position p = instance.getPosition();
    assertNotNull( p );
    assertEquals( lat, p.getLatitude(), 0.0000001 );
    assertEquals( lon, p.getLongitude(), 0.0000001 );
    assertEquals( CompassPoint.NORTH, p.getLatitudeHemisphere() );
    assertEquals( CompassPoint.EAST, p.getLongitudeHemisphere() );
  }




  @Test
  public void testGetTime() {
    final NMEATime t = instance.getTime();
    assertNotNull( t );
    assertEquals( 12, t.getHour() );
    assertEquals( 0, t.getMinutes() );
    assertEquals( 45.0, t.getSeconds(), 0.1 );
  }




  @Test
  public void testSetDataStatus() {
    assertEquals( DataStatus.ACTIVE, instance.getStatus() );
    instance.setStatus( DataStatus.VOID );
    assertEquals( DataStatus.VOID, instance.getStatus() );
  }




  @Test
  public void testSetPositionWithNonZeroValues() {

    final double lat = 60 + ( 11.552 / 60 );
    final double lon = 25 + ( 1.941 / 60 );
    final Position p2 = new Position( lat, lon );
    instance.setPosition( p2 );

    final String s2 = instance.toString();
    final Position p = instance.getPosition();

    assertTrue( s2.contains( ",6011.5520,N," ) );
    assertTrue( s2.contains( ",02501.9410,E," ) );
    assertNotNull( p );
    assertEquals( lat, p.getLatitude(), 0.0000001 );
    assertEquals( lon, p.getLongitude(), 0.0000001 );
  }




  @Test
  public void testSetPositionWithZeroValues() {

    final Position p1 = new Position( 0.0, 0.0 );
    instance.setPosition( p1 );

    final String s1 = instance.toString();
    final Position p = instance.getPosition();

    assertTrue( s1.contains( ",0000.0000,N," ) );
    assertTrue( s1.contains( ",00000.0000,E," ) );
    assertNotNull( p );
    assertEquals( 0.0, p.getLatitude(), 0.0000001 );
    assertEquals( 0.0, p.getLongitude(), 0.0000001 );
  }




  @Test
  public void testSetTime() {
    final NMEATime t = new NMEATime( 1, 2, 3.4 );
    instance.setTime( t );
    assertTrue( instance.toString().contains( ",E,010203.400,A*" ) );
  }

}
