package coyote.nmea.sentence;

//import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Test;

import coyote.nmea.FaaMode;
import coyote.nmea.GpsFixStatus;
import coyote.nmea.TalkerId;


/**
 * Tests the GSA sentence.
 */
public class GSATest {

  /** Example sentence */
  public static final String EXAMPLE = "$GPGSA,A,3,02,,,07,,09,24,26,,,,,1.6,1.6,1.0*3D";

  private GSASentence empty;
  private GSASentence instance;




  @Before
  public void setUp() {
    try {
      empty = new GSASentenceImpl( TalkerId.GP );
      instance = new GSASentenceImpl( EXAMPLE );
    } catch ( final Exception e ) {
      fail( e.getMessage() );
    }
  }




  @Test
  public void testConstructor() {
    assertEquals( 17, empty.getFieldCount() );
  }




  @Test
  public void testGetFaaMode() {
    assertEquals( FaaMode.AUTOMATIC, instance.getMode() );
  }




  @Test
  public void testGetFixStatus() {
    assertEquals( GpsFixStatus.GPS_3D, instance.getFixStatus() );
  }




  @Test
  public void testGetHorizontalDOP() {
    final double hdop = instance.getHorizontalDOP();
    assertEquals( 1.6, hdop, 0.001 );
  }




  @Test
  public void testGetPositionDOP() {
    final double pdop = instance.getPositionDOP();
    assertEquals( 1.6, pdop, 0.001 );
  }




  @Test
  public void testGetSatelliteIds() {
    final String[] satellites = instance.getSatelliteIds();
    assertEquals( 5, satellites.length );
    assertEquals( "02", satellites[0] );
    assertEquals( "07", satellites[1] );
    assertEquals( "09", satellites[2] );
    assertEquals( "24", satellites[3] );
    assertEquals( "26", satellites[4] );
  }




  @Test
  public void testGetVerticalDOP() {
    final double vdop = instance.getVerticalDOP();
    assertEquals( 1.0, vdop, 0.001 );
  }




  @Test
  public void testSetFaaMode() {
    instance.setMode( FaaMode.DGPS );
    assertTrue( instance.toString().contains( ",D," ) );
    assertEquals( FaaMode.DGPS, instance.getMode() );

    instance.setMode( FaaMode.SIMULATED );
    assertTrue( instance.toString().contains( ",S," ) );
    assertEquals( FaaMode.SIMULATED, instance.getMode() );
  }




  @Test
  public void testSetFixStatus() {
    instance.setFixStatus( GpsFixStatus.GPS_NA );
    assertTrue( instance.toString().contains( ",A,1," ) );
    assertEquals( GpsFixStatus.GPS_NA, instance.getFixStatus() );

    instance.setFixStatus( GpsFixStatus.GPS_2D );
    assertTrue( instance.toString().contains( ",A,2," ) );
    assertEquals( GpsFixStatus.GPS_2D, instance.getFixStatus() );

    instance.setFixStatus( GpsFixStatus.GPS_3D );
    assertTrue( instance.toString().contains( ",A,3," ) );
    assertEquals( GpsFixStatus.GPS_3D, instance.getFixStatus() );
  }




  @Test
  public void testSetHorizontalDOP() {
    final double hdop = 1.98765;
    instance.setHorizontalDOP( hdop );
    assertEquals( hdop, instance.getHorizontalDOP(), 0.1 );
  }




  @Test
  public void testSetPositionDOP() {
    final double pdop = 1.56788;
    instance.setPositionDOP( pdop );
    assertEquals( pdop, instance.getPositionDOP(), 0.1 );
  }




  @Test
  public void testSetSatelliteIds() {

    final String[] ids = { "02", "04", "06", "08", "10", "12" };
    instance.setSatelliteIds( ids );

    final String[] satellites = instance.getSatelliteIds();
    assertEquals( ids.length, satellites.length );

    int i = 0;
    for ( final String id : ids ) {
      assertEquals( id, satellites[i++] );
    }
  }




  @Test
  public void testSetVerticalDOP() {
    final double vdop = 1.56789;
    instance.setVerticalDOP( vdop );
    assertEquals( vdop, instance.getVerticalDOP(), 0.1 );
  }

}
