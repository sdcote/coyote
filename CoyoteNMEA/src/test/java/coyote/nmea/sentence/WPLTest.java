package coyote.nmea.sentence;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Test;

import coyote.nmea.CompassPoint;
import coyote.nmea.TalkerId;
import coyote.nmea.Waypoint;


/**
 * WPLTest
 */
public class WPLTest {

  /** Example sentence */
  public static final String EXAMPLE = "$GPWPL,5536.200,N,01436.500,E,RUSKI*1F";

  private WPLSentence empty;
  private WPLSentence wpl;




  @Before
  public void setUp() {
    try {
      empty = new WPLSentenceImpl( TalkerId.GP );
      wpl = new WPLSentenceImpl( EXAMPLE );
    } catch ( Exception e ) {
      fail( e.getMessage() );
    }
  }




  @Test
  public void testConstructor() {
    assertEquals( 5, empty.getFieldCount() );
  }




  @Test
  public void testGetWaypoint() {
    final Double lat = new Double( 55 + ( 36.200 / 60 ) );
    final Double lon = new Double( 14 + ( 36.500 / 60 ) );

    Waypoint wp = wpl.getWaypoint();

    assertNotNull( wp );
    assertEquals( "RUSKI", wp.getId() );
    assertEquals( CompassPoint.NORTH, wp.getLatitudeHemisphere() );
    assertEquals( CompassPoint.EAST, wp.getLongitudeHemisphere() );
    assertEquals( lat, new Double( wp.getLatitude() ) );
    assertEquals( lon, new Double( wp.getLongitude() ) );
  }




  @Test
  public void testSetWaypointWithNonZeroValues() {

    final double lat = 60 + ( 11.552 / 60 );
    final double lon = 25 + ( 1.941 / 60 );

    Waypoint p2 = new Waypoint( "WAYP2", lat, lon );

    wpl.setWaypoint( p2 );

    String s2 = wpl.toString();
    assertTrue( s2.contains( ",6011.5520,N,02501.9410,E,WAYP2*" ) );

    Waypoint p = wpl.getWaypoint();
    assertNotNull( p );
    assertEquals( lat, p.getLatitude(), 0.0000001 );
    assertEquals( lon, p.getLongitude(), 0.0000001 );
  }




  @Test
  public void testSetWaypointWithZeroValues() {

    Waypoint p1 = new Waypoint( "WAYP1", 0.0, 0.0 );
    wpl.setWaypoint( p1 );

    String s1 = wpl.toString();
    assertTrue( s1.contains( ",0000.0000,N,00000.0000,E,WAYP1*" ) );

    Waypoint p = wpl.getWaypoint();
    assertNotNull( p );
    assertEquals( 0.0, p.getLatitude(), 0.0000001 );
    assertEquals( 0.0, p.getLongitude(), 0.0000001 );
  }

}
