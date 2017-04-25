package coyote.nmea.sentence;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import coyote.nmea.CompassPoint;
import coyote.nmea.Position;
import coyote.nmea.SentenceId;


/**
 * Unit tests for AbstractPositionSentence class.
 */
public class AbstractPositionTest {

  private AbstractPositionSentence instance;




  @Before
  public void setUp() throws Exception {
    instance = new AbstractPositionSentence( GLLTest.EXAMPLE, SentenceId.GLL ) {};
  }




  @Test
  public void testParseHemisphereLat() {
    assertEquals( CompassPoint.NORTH, instance.parseHemisphereLat( 1 ) );
  }




  @Test
  public void testParseHemisphereLon() {
    assertEquals( CompassPoint.EAST, instance.parseHemisphereLon( 3 ) );
  }




  @Test
  public void testParseLatitude() {
    // 6011.552
    final double lat = 60 + ( 11.552 / 60 );
    assertEquals( lat, instance.parseLatitude( 0 ), 0.000001 );
  }




  @Test
  public void testParseLongitude() {
    // 02501.941
    final double lat = 25 + ( 01.941 / 60 );
    assertEquals( lat, instance.parseLongitude( 2 ), 0.000001 );
  }




  @Test
  public void testSetLatHemisphere() {
    instance.setLatHemisphere( 1, CompassPoint.SOUTH );
    assertTrue( instance.toString().contains( ",S," ) );
    assertEquals( CompassPoint.SOUTH, instance.parseHemisphereLat( 1 ) );
  }




  @Test
  public void testSetLatitude() {
    // 2501.941
    final double lat = 25 + ( 01.941 / 60 );
    instance.setLatitude( 0, lat );
    assertTrue( instance.toString().contains( ",02501.941" ) );
    assertEquals( lat, instance.parseLatitude( 0 ), 0.000001 );
  }




  @Test
  public void testSetLongitude() {
    // 02801.941
    final double lon = 28 + ( 01.941 / 60 );
    instance.setLongitude( 2, lon );
    assertTrue( instance.toString().contains( ",02801.941" ) );
    assertEquals( lon, instance.parseLongitude( 2 ), 0.000001 );
  }




  @Test
  public void testSetLonHemisphere() {
    instance.setLonHemisphere( 3, CompassPoint.WEST );
    assertTrue( instance.toString().contains( ",W," ) );
    assertEquals( CompassPoint.WEST, instance.parseHemisphereLon( 3 ) );
  }




  @Test
  public void testSetPositionValuesNE() {

    final double lat = 60 + ( 11.552 / 60 );
    final double lon = 25 + ( 1.941 / 60 );
    final Position p2 = new Position( lat, lon );
    instance.setPositionValues( p2, 0, 1, 2, 3 );

    final String s2 = instance.toString();
    final Position p = instance.parsePosition( 0, 1, 2, 3 );

    assertTrue( s2.contains( ",6011.5520,N," ) );
    assertTrue( s2.contains( ",02501.9410,E," ) );
    assertNotNull( p );
    assertEquals( lat, p.getLatitude(), 0.0000001 );
    assertEquals( lon, p.getLongitude(), 0.0000001 );
  }




  @Test
  public void testSetPositionValuesSW() {

    final double lat = -60 - ( 11.552 / 60 );
    final double lon = -25 - ( 1.941 / 60 );
    final Position p2 = new Position( lat, lon );
    instance.setPositionValues( p2, 0, 1, 2, 3 );

    final String s2 = instance.toString();
    final Position p = instance.parsePosition( 0, 1, 2, 3 );

    assertTrue( s2.contains( ",6011.5520,S," ) );
    assertTrue( s2.contains( ",02501.9410,W," ) );
    assertNotNull( p );
    assertEquals( lat, p.getLatitude(), 0.0000001 );
    assertEquals( lon, p.getLongitude(), 0.0000001 );
  }

}
