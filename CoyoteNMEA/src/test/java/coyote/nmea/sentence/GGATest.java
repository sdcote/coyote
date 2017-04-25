package coyote.nmea.sentence;

//import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Test;

import coyote.nmea.CompassPoint;
import coyote.nmea.DataNotAvailableException;
import coyote.nmea.Datum;
import coyote.nmea.GpsFixQuality;
import coyote.nmea.NMEATime;
import coyote.nmea.Position;
import coyote.nmea.SentenceId;
import coyote.nmea.TalkerId;
import coyote.nmea.Units;


/**
 * Test the GGA sentence implementation
 */
public class GGATest {

  public static final String EXAMPLE = "$GPGGA,143503.000,4002.1083,N,08309.5833,W,2,8,1.2,360.1,M,-33.2,M,0.0,0000*70";
  private GGASentenceImpl gga;
  private GGASentenceImpl empty;




  @Before
  public void setUp() {
    try {
      empty = new GGASentenceImpl( TalkerId.GP );
      gga = new GGASentenceImpl( EXAMPLE );
    } catch ( final Exception e ) {
      fail( e.getMessage() );
    }
  }




  @Test
  public void testConstructor() {
    assertEquals( 14, empty.getFieldCount() );
  }




  @Test
  public void testGetAltitude() {
    assertEquals( 360.1, gga.getAltitude(), 0.001 );
  }




  @Test
  public void testGetAltitudeUnits() {
    assertEquals( Units.METER, gga.getAltitudeUnits() );
  }




  @Test
  public void testGetDgpsAge() {
    try {
      gga.getDgpsAge();
      //fail( "Did not throw ParseException" );
    } catch ( final DataNotAvailableException e ) {
      // ok
    } catch ( final Exception e ) {
      e.printStackTrace();
    }
  }




  @Test
  public void testGetDgpsStationId() {
    try {
      gga.getDgpsStationId();
      //fail( "Did not throw ParseException" );
    } catch ( final DataNotAvailableException e ) {
      // ok
    } catch ( final Exception e ) {
      fail( e.getMessage() );
    }
  }




  @Test
  public void testGetFixQuality() {
    assertEquals( GpsFixQuality.DGPS, gga.getFixQuality() );
  }




  @Test
  public void testGetGeoidalHeight() {
    assertEquals( -33.2, gga.getGeoidalHeight(), 0.001 );
  }




  @Test
  public void testGetGeoidalHeightUnits() {
    assertEquals( Units.METER, gga.getGeoidalHeightUnits() );
  }




  @Test
  public void testGetHorizontalDOP() {
    assertEquals( 1.2, gga.getHorizontalDOP(), 0.001 );
  }




  @Test
  public void testGetNumberOfSatellites() {
    assertEquals( 8, gga.getSatelliteCount() );
  }




  @Test
  public void testGetPosition() {
    // expected lat/lon values
    final double lat = 40 + ( 2.1083 / 60 );
    final double lon = ( 83 + ( 09.5833 / 60 ) ) * -1;
    final double alt = 360.1;
    
    final Position p = gga.getPosition();
    assertNotNull( p );
    assertEquals( lat, p.getLatitude(), 0.0000001 );
    assertEquals( CompassPoint.NORTH, p.getLatitudeHemisphere() );
    assertEquals( lon, p.getLongitude(), 0.0000001 );
    assertEquals( CompassPoint.WEST, p.getLongitudeHemisphere() );
    assertEquals( Datum.WGS84, p.getDatum() );
    assertEquals( alt, p.getAltitude(), 0.01 );
  }




  @Test
  public void testGetTime() {
    final NMEATime t = gga.getTime();
    assertNotNull( t );
    assertEquals( 14, t.getHour() );
    assertEquals( 35, t.getMinutes() );
    assertEquals( 3.0, t.getSeconds(), 0.001 );
  }




  @Test
  public void testGGAParser() {
    final GGASentenceImpl instance = new GGASentenceImpl( EXAMPLE );
    final SentenceId sid = SentenceId.valueOf( instance.getSentenceId() );
    assertEquals( SentenceId.GGA, sid );
  }




  @Test
  public void testSetAltitude() {
    final double alt = 11.11111;
    gga.setAltitude( alt );
    assertEquals( alt, gga.getAltitude(), 0.1 );
  }




  @Test
  public void testSetAltitudeUnits() {
    assertEquals( Units.METER, gga.getAltitudeUnits() );
    gga.setAltitudeUnits( Units.FEET );
    assertEquals( Units.FEET, gga.getAltitudeUnits() );
  }




  @Test
  public void testSetDgpsAge() {
    final double age = 33.333333;
    gga.setDgpsAge( age );
    assertEquals( age, gga.getDgpsAge(), 0.1 );
  }




  @Test
  public void testSetDgpsStationId() {
    gga.setDgpsStationId( "0001" );
    assertEquals( "0001", gga.getDgpsStationId() );
  }




  @Test
  public void testSetFixQuality() {
    assertEquals( GpsFixQuality.DGPS, gga.getFixQuality() );
    gga.setFixQuality( GpsFixQuality.INVALID );
    assertEquals( GpsFixQuality.INVALID, gga.getFixQuality() );
  }




  @Test
  public void testSetGeoidalHeight() {
    final double height = 3.987654;
    gga.setGeoidalHeight( height );
    assertEquals( height, gga.getGeoidalHeight(), 0.1 );
  }




  @Test
  public void testSetGeoidalHeightUnits() {
    assertEquals( Units.METER, gga.getGeoidalHeightUnits() );
    gga.setGeoidalHeightUnits( Units.FEET );
    assertEquals( Units.FEET, gga.getGeoidalHeightUnits() );
  }




  @Test
  public void testSetHorizontalDOP() {
    final double hdop = 0.123456;
    gga.setHorizontalDOP( hdop );
    assertEquals( hdop, gga.getHorizontalDOP(), 0.1 );
  }




  @Test
  public void testSetPosition() {
    final double lat = 40 + ( 2.1083 / 60 );
    final double lon = ( 83 + ( 09.5833 / 60 ) ) * -1;
    final double alt = 360.1;
    final Position p = new Position( lat, lon );
    p.setAltitude( alt );
    gga.setPosition( p );

    final String str = gga.toString();
    assertTrue( str.contains( ",4002.1083,N," ) );
    assertTrue( str.contains( ",08309.5833,W," ) );

    final Position wp = gga.getPosition();
    assertNotNull( wp );
    assertEquals( lat, wp.getLatitude(), 0.0000001 );
    assertEquals( lon, wp.getLongitude(), 0.0000001 );
    assertEquals( CompassPoint.NORTH, wp.getLatitudeHemisphere() );
    assertEquals( CompassPoint.WEST, wp.getLongitudeHemisphere() );
    assertEquals( alt, wp.getAltitude(), 0.01 );
  }




  @Test
  public void testSetTime() {
    final NMEATime t = new NMEATime( 1, 2, 3.456 );
    gga.setTime( t );
    assertTrue( gga.toString().contains( "GPGGA,010203.456,4002" ) );
  }




  @Test
  public void testBuild() {
    // $GPGGA,143503.000,4002.1083,N,08309.5833,W,2,8,1.2,360.1,M,-33.2,M,0.0,0000*70

    GGASentence subject = new GGASentenceImpl( TalkerId.GP );
    subject.setTime( new NMEATime( "143503.000" ) );

    final double lat = 40 + ( 2.1083 / 60 );
    final double lon = ( 83 + ( 09.5833 / 60 ) ) * -1;
    final double alt = 360.1;
    final Position p = new Position( lat, lon );
    p.setAltitude( alt );
    subject.setPosition( p );
    System.out.println( p );

    subject.setFixQuality( GpsFixQuality.DGPS );
    subject.setSatelliteCount( 8 ); // fix output
    subject.setHorizontalDOP( 1.18D );
    subject.setGeoidalHeight( -33.2 );
    subject.setGeoidalHeightUnits( Units.METER );
    subject.setDgpsAge( 0 ); // fix output
    subject.setDgpsStationId( "0000" );

    System.out.println( subject );
    
  }

}
