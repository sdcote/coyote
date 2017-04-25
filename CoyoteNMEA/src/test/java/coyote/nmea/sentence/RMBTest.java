package coyote.nmea.sentence;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Test;

import coyote.nmea.CompassPoint;
import coyote.nmea.DataNotAvailableException;
import coyote.nmea.DataStatus;
import coyote.nmea.Direction;
import coyote.nmea.TalkerId;
import coyote.nmea.Waypoint;


/**
 * Tests the RMB sentence implementation.
 */
public class RMBTest {

  public static final String EXAMPLE = "$GPRMB,A,0.00,R,,RUSKI,5536.200,N,01436.500,E,432.3,234.9,,V*58";

  private RMBSentence empty;
  private RMBSentence rmb;




  /**
   * setUp
   */
  @Before
  public void setUp() {
    try {
      empty = new RMBSentenceImpl( TalkerId.GP );
      rmb = new RMBSentenceImpl( EXAMPLE );
    } catch ( Exception e ) {
      fail( e.getMessage() );
    }
  }




  @Test
  public void testConstructor() {
    assertEquals( 13, empty.getFieldCount() );
  }




  @Test
  public void testArrivalStatus() {

    assertEquals( DataStatus.VOID, rmb.getArrivalStatus() );
    assertFalse( rmb.hasArrived() );

    rmb.setArrivalStatus( DataStatus.ACTIVE );
    assertEquals( DataStatus.ACTIVE, rmb.getArrivalStatus() );
    assertTrue( rmb.hasArrived() );

    rmb.setArrivalStatus( DataStatus.VOID );
    assertEquals( DataStatus.VOID, rmb.getArrivalStatus() );
    assertFalse( rmb.hasArrived() );
  }




  @Test
  public void testGetBearing() {
    assertEquals( 234.9, rmb.getBearing(), 0.001 );
  }




  @Test
  public void testGetCrossTrackError() {
    assertEquals( 0.0, rmb.getCrossTrackError(), 0.001 );
  }




  @Test
  public void testGetDestination() {
    final String id = "RUSKI";
    final double lat = 55 + ( 36.200 / 60 );
    final double lon = 14 + ( 36.500 / 60 );

    Waypoint wp = rmb.getDestination();
    assertNotNull( wp );
    assertEquals( id, wp.getId() );
    assertEquals( lat, wp.getLatitude(), 0.0000001 );
    assertEquals( lon, wp.getLongitude(), 0.0000001 );
    assertEquals( CompassPoint.NORTH, wp.getLatitudeHemisphere() );
    assertEquals( CompassPoint.EAST, wp.getLongitudeHemisphere() );
  }




  @Test
  public void testGetOriginId() {
    try {
      assertEquals( "", rmb.getOriginId() );
      fail( "Did not throw ParseException" );
    } catch ( Exception e ) {
      assertTrue( e instanceof DataNotAvailableException );
    }
  }




  @Test
  public void testGetRange() {
    assertEquals( 432.3, rmb.getRange(), 0.001 );
  }




  @Test
  public void testGetStatus() {
    assertEquals( DataStatus.ACTIVE, rmb.getStatus() );
  }




  @Test
  public void testGetSteerTo() {
    assertEquals( Direction.RIGHT, rmb.getSteerTo() );
  }




  @Test
  public void testGetVelocity() {
    try {
      assertEquals( 0.0, rmb.getVelocity(), 0.001 );
      fail( "Did not throw ParseException" );
    } catch ( Exception e ) {
      assertTrue( e instanceof DataNotAvailableException );
    }
  }




  @Test
  public void testSetBearing() {
    final double brg = 90.56789;
    rmb.setBearing( brg );
    assertTrue( rmb.toString().contains( ",090.6," ) );
    assertEquals( brg, rmb.getBearing(), 0.1 );
  }




  @Test
  public void testSetBearingWithNegativeValue() {
    try {
      rmb.setBearing( -0.001 );
      fail( "Did not throw exception" );
    } catch ( IllegalArgumentException e ) {
      assertTrue( e.getMessage().contains( "0..360" ) );
    }
  }




  @Test
  public void testSetBearingWithValueGreaterThanAllowed() {
    try {
      rmb.setBearing( 360.001 );
      fail( "Did not throw exception" );
    } catch ( IllegalArgumentException e ) {
      assertTrue( e.getMessage().contains( "0..360" ) );
    }
  }




  @Test
  public void testSetCrossTrackError() {
    final double xte = 2.56789;
    rmb.setCrossTrackError( xte );
    assertTrue( rmb.toString().contains( ",2.57," ) );
    assertEquals( xte, rmb.getCrossTrackError(), 0.2 );
  }




  @Test
  public void testSetDestination() {

    final String id = "MYDEST";
    final double lat = 61 + ( 1.111 / 60 );
    final double lon = 27 + ( 7.777 / 60 );
    Waypoint d = new Waypoint( id, lat, lon );

    rmb.setDestination( d );

    String str = rmb.toString();
    Waypoint wp = rmb.getDestination();

    assertTrue( str.contains( ",MYDEST,6101.1110,N,02707.7770,E," ) );
    assertNotNull( wp );
    assertEquals( id, wp.getId() );
    assertEquals( lat, wp.getLatitude(), 0.0000001 );
    assertEquals( lon, wp.getLongitude(), 0.0000001 );
    assertEquals( CompassPoint.NORTH, wp.getLatitudeHemisphere() );
    assertEquals( CompassPoint.EAST, wp.getLongitudeHemisphere() );
  }




  @Test
  public void testSetOriginId() {
    rmb.setOriginId( "ORIGIN" );
    assertTrue( rmb.toString().contains( ",ORIGIN,RUSKI," ) );
    assertEquals( "ORIGIN", rmb.getOriginId() );
  }




  @Test
  public void testSetRange() {
    final double range = 12.3456;
    rmb.setRange( range );
    assertTrue( rmb.toString().contains( ",12.3," ) );
    assertEquals( range, rmb.getRange(), 0.1 );
  }




  @Test
  public void testSetStatus() {
    rmb.setStatus( DataStatus.ACTIVE );
    assertEquals( DataStatus.ACTIVE, rmb.getStatus() );
  }




  @Test
  public void testSetSteerTo() {
    rmb.setSteerTo( Direction.LEFT );
    assertTrue( rmb.toString().contains( ",L," ) );
    assertEquals( Direction.LEFT, rmb.getSteerTo() );
  }




  @Test
  public void testSetSteerToWithNull() {
    try {
      rmb.setSteerTo( null );
      fail( "Did not throw IllegalArgumentException" );
    } catch ( IllegalArgumentException e ) {
      assertTrue( e.getMessage().contains( "LEFT or RIGHT" ) );
    } catch ( Exception e ) {
      fail( e.getMessage() );
    }

  }




  @Test
  public void testSetVelocity() {
    final double v = 40.66666;
    rmb.setVelocity( v );
    assertTrue( rmb.toString().contains( ",40.7," ) );
    assertEquals( v, rmb.getVelocity(), 0.1 );
  }




  @Test
  public void testSetVelocityWithNegativeValue() {
    final double v = -0.123;
    rmb.setVelocity( v );
    assertTrue( rmb.toString().contains( ",-0.1," ) );
    assertEquals( v, rmb.getVelocity(), 0.1 );
  }

}
