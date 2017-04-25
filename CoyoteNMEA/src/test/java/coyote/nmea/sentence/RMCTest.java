package coyote.nmea.sentence;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Test;

import coyote.nmea.CompassPoint;
import coyote.nmea.DataStatus;
import coyote.nmea.FaaMode;
import coyote.nmea.NMEADate;
import coyote.nmea.NMEATime;
import coyote.nmea.Position;
import coyote.nmea.TalkerId;


//import static org.junit.Assert.*;

/**
 * Tests the RMC sentence implementation.
 */
public class RMCTest {

  /** Example sentence */
  public static final String EXAMPLE = "$GPRMC,120044.567,A,6011.552,N,02501.941,E,000.0,360.0,160705,006.1,E,A*0B";

  RMCSentenceImpl empty;
  RMCSentenceImpl rmc;




  @Before
  public void setUp() {
    try {
      empty = new RMCSentenceImpl( TalkerId.GP );
      rmc = new RMCSentenceImpl( EXAMPLE );
    } catch ( final Exception e ) {
      fail( e.getMessage() );
    }
  }




  @Test
  public void testConstructor() {
    assertEquals( 12, empty.getFieldCount() );
  }




  @Test
  public void testGetCorrectedCourse() {
    final double expected = rmc.getCourse() + rmc.getVariation();
    assertEquals( expected, rmc.getCorrectedCourse(), 0.001 );
  }




  @Test
  public void testGetCourse() {
    assertEquals( 360.0, rmc.getCourse(), 0.001 );
  }




  @Test
  public void testGetDataStatus() {
    assertEquals( DataStatus.ACTIVE, rmc.getStatus() );
  }




  @Test
  public void testGetDate() {
    final NMEADate expected = new NMEADate( 2005, 7, 16 );
    final NMEADate parsed = rmc.getDate();
    assertEquals( expected, parsed );
  }




  @Test
  public void testGetDay() {
    assertEquals( 16, rmc.getDate().getDay() );
  }




  @Test
  public void testGetDirectionOfVariation() {
    assertTrue( rmc.getVariation() < 0 );
    assertEquals( CompassPoint.EAST, rmc.getDirectionOfVariation() );
  }




  @Test
  public void testGetFaaMode() {
    assertEquals( FaaMode.AUTOMATIC, rmc.getMode() );
  }




  @Test
  public void testGetMagneticVariation() {
    assertEquals( -6.1, rmc.getVariation(), 0.001 );
  }




  @Test
  public void testGetMonth() {
    assertEquals( 7, rmc.getDate().getMonth() );
  }




  @Test
  public void testGetPosition() {
    final double lat = 60 + ( 11.552 / 60 );
    final double lon = 25 + ( 1.941 / 60 );

    final Position p = rmc.getPosition();
    assertNotNull( p );
    assertEquals( lat, p.getLatitude(), 0.0000001 );
    assertEquals( lon, p.getLongitude(), 0.0000001 );
    assertEquals( CompassPoint.NORTH, p.getLatitudeHemisphere() );
    assertEquals( CompassPoint.EAST, p.getLongitudeHemisphere() );
  }




  @Test
  public void testGetSpeed() {
    assertEquals( 0.0, rmc.getSpeed(), 0.001 );
  }




  @Test
  public void testGetTime() {
    final NMEATime t = rmc.getTime();
    assertNotNull( t );
    assertEquals( 12, t.getHour() );
    assertEquals( 0, t.getMinutes() );
    assertEquals( 44.567, t.getSeconds(), 0.001 );
  }




  @Test
  public void testGetYear() {
    assertEquals( 2005, rmc.getDate().getYear() );
  }




  @Test
  public void testSetCourse() {
    final double cog = 90.55555;
    rmc.setCourse( cog );
    assertTrue( rmc.toString().contains( ",090.6," ) );
    assertEquals( cog, rmc.getCourse(), 0.1 );
  }




  @Test
  public void testSetDataStatus() {
    rmc.setStatus( DataStatus.ACTIVE );
    assertEquals( DataStatus.ACTIVE, rmc.getStatus() );
  }




  @Test
  public void testSetDate() {
    rmc.setDate( new NMEADate( 2010, 6, 9 ) );
    assertTrue( rmc.toString().contains( ",360.0,090610,006.1," ) );
    rmc.setDate( new NMEADate( 2010, 11, 12 ) );
    assertTrue( rmc.toString().contains( ",360.0,121110,006.1," ) );
  }




  @Test
  public void testSetDirectionOfVariation() {
    rmc.setDirectionOfVariation( CompassPoint.WEST );
    assertEquals( CompassPoint.WEST, rmc.getDirectionOfVariation() );
    rmc.setDirectionOfVariation( CompassPoint.EAST );
    assertEquals( CompassPoint.EAST, rmc.getDirectionOfVariation() );
  }




  @Test
  public void testSetDirectionOfVariationWithInvalidDirection() {
    try {
      rmc.setDirectionOfVariation( CompassPoint.NORTH );
      fail( "Did not throw exception" );
    } catch ( final IllegalArgumentException e ) {
      // pass
    } catch ( final Exception e ) {
      fail( e.getMessage() );
    }
  }




  @Test
  public void testSetFaaMode() {
    rmc.setMode( FaaMode.SIMULATED );
    assertEquals( FaaMode.SIMULATED, rmc.getMode() );
    rmc.setMode( FaaMode.ESTIMATED );
    assertEquals( FaaMode.ESTIMATED, rmc.getMode() );
  }




  @Test
  public void testSetFaaModeWhenOmitted() {
    final RMCSentenceImpl parser = new RMCSentenceImpl( "$GPRMC,120044.567,A,6011.552,N,02501.941,E,000.0,360.0,160705,006.1,E" );
    parser.setMode( FaaMode.SIMULATED );
    assertEquals( FaaMode.SIMULATED, parser.getMode() );
    parser.setMode( FaaMode.ESTIMATED );
    assertEquals( FaaMode.ESTIMATED, parser.getMode() );
  }




  @Test
  public void testSetPosition() {
    final double lat = 61 + ( 1.111 / 60 );
    final double lon = 27 + ( 7.777 / 60 );
    final Position p = new Position( lat, lon );
    rmc.setPosition( p );

    final String str = rmc.toString();
    final Position wp = rmc.getPosition();

    assertTrue( str.contains( ",6101.1110,N,02707.7770,E," ) );
    assertNotNull( wp );
    assertEquals( lat, wp.getLatitude(), 0.0000001 );
    assertEquals( lon, wp.getLongitude(), 0.0000001 );
    assertEquals( CompassPoint.NORTH, wp.getLatitudeHemisphere() );
    assertEquals( CompassPoint.EAST, wp.getLongitudeHemisphere() );
  }




  @Test
  public void testSetSpeed() {
    final double sog = 35.23456;
    rmc.setSpeed( sog );
    assertTrue( rmc.toString().contains( ",35.2," ) );
    assertEquals( sog, rmc.getSpeed(), 0.1 );
  }




  @Test
  public void testSetTime() {
    final NMEATime t = new NMEATime( 1, 2, 3.456 );
    rmc.setTime( t );
    assertTrue( rmc.toString().contains( "$GPRMC,010203.456,A," ) );
  }




  @Test
  public void testSetVariation() {
    final double var = 1.55555;
    rmc.setVariation( var );
    rmc.setDirectionOfVariation( CompassPoint.WEST );
    assertTrue( rmc.toString().contains( ",001.6,W," ) );
    assertEquals( var, rmc.getVariation(), 0.1 );
    assertEquals( CompassPoint.WEST, rmc.getDirectionOfVariation() );
  }

}
