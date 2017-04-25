package coyote.nmea;

import static org.junit.Assert.assertEquals;




//import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

import coyote.nmea.Datum;
import coyote.nmea.Position;
import coyote.nmea.Waypoint;


public class WaypointTest {

  private final String id1 = "FOO";
  private final String id2 = "BAR";
  private final String desc = "Description text";
  Waypoint point;




  @Before
  public void setUp() {
    point = new Waypoint( id1, 60.0, 25.0, Datum.WGS84 );
  }




  @Test
  public void testDescription() {
    assertEquals( "", point.getDescription() );
    point.setDescription( desc );
    assertEquals( desc, point.getDescription() );
  }




  @Test
  public void testId() {
    assertEquals( id1, point.getId() );
    point.setId( id2 );
    assertEquals( id2, point.getId() );
  }




  @Test
  public void testToWaypoint() {
    final String name = "TEST";
    Position pos = new Position( 60.0, 25.0, Datum.WGS84 );

    final Waypoint wp = Waypoint.toWaypoint( name, pos );
    assertEquals( name, wp.getId() );
    assertEquals( "", wp.getDescription() );
    assertEquals( pos.getLatitude(), wp.getLatitude(), 0.00001 );
    assertEquals( pos.getLongitude(), wp.getLongitude(), 0.00001 );
    assertEquals( pos.getLatitudeHemisphere(), wp.getLatitudeHemisphere() );
    assertEquals( pos.getLongitudeHemisphere(), wp.getLongitudeHemisphere() );
    assertEquals( pos.getDatum(), wp.getDatum() );
  }

}
