package coyote.nmea.sentence;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import coyote.nmea.SatelliteInfo;
import coyote.nmea.TalkerId;


//import static org.junit.Assert.*;

/**
 * Test the GSV sentence.
 */
public class GSVTest {

  /** Example sentence */
  public static final String EXAMPLE = "$GPGSV,3,2,12,15,56,182,51,17,38,163,47,18,63,058,50,21,53,329,47*73";

  private GSVSentence empty;
  private GSVSentence gsv;




  @Before
  public void setUp() {
    try {
      empty = new GSVSentenceImpl( TalkerId.GP );
      gsv = new GSVSentenceImpl( EXAMPLE );
    } catch ( final Exception e ) {
      fail( e.getMessage() );
    }
  }




  @Test
  public void testConstructor() {
    assertEquals( 19, empty.getFieldCount() );
  }




  @Test
  public void testGetSatelliteCount() {
    assertEquals( 12, gsv.getSatelliteCount() );
  }




  @Test
  public void testGetSatelliteInfo() {
    final List<SatelliteInfo> sat = gsv.getSatelliteInfo();
    assertEquals( 4, sat.size() );
    testSatelliteInfo( sat.get( 0 ), "15", 56, 182, 51 );
    testSatelliteInfo( sat.get( 1 ), "17", 38, 163, 47 );
    testSatelliteInfo( sat.get( 2 ), "18", 63, 58, 50 );
    testSatelliteInfo( sat.get( 3 ), "21", 53, 329, 47 );
  }




  @Test
  public void testGetSatelliteInfoWithEmptyFields() {

    final GSVSentence g = new GSVSentenceImpl( "$GPGSV,3,2,12,15,56,182,51,17,38,163,47,18,,,,21,53,329,47" );
    final List<SatelliteInfo> sat = g.getSatelliteInfo();

    assertEquals( 3, sat.size() );
    testSatelliteInfo( sat.get( 0 ), "15", 56, 182, 51 );
    testSatelliteInfo( sat.get( 1 ), "17", 38, 163, 47 );
    testSatelliteInfo( sat.get( 2 ), "21", 53, 329, 47 );
  }




  @Test
  public void testGetSatelliteInfoWithShortSentence() {

    final GSVSentence g = new GSVSentenceImpl( "$GPGSV,3,2,12,15,56,182,51,17,38,163,47" );
    final List<SatelliteInfo> sat = g.getSatelliteInfo();

    assertEquals( 2, sat.size() );
    testSatelliteInfo( sat.get( 0 ), "15", 56, 182, 51 );
    testSatelliteInfo( sat.get( 1 ), "17", 38, 163, 47 );
  }




  @Test
  public void testGetSentenceCount() {
    assertEquals( 3, gsv.getSentenceCount() );
  }




  @Test
  public void testGetSentenceIndex() {
    assertEquals( 2, gsv.getSentenceIndex() );
  }




  @Test
  public void testIsFirst() {
    assertFalse( gsv.isFirst() );
  }




  @Test
  public void testIsLast() {
    assertFalse( gsv.isLast() );
  }




  @Test
  public void testParserGlonassGSV() {
    final GSVSentenceImpl gl = new GSVSentenceImpl( "$GLGSV,2,1,07,70,28,145,44,71,67,081,46,72,34,359,40,77,16,245,35,1*76" );
    assertEquals( TalkerId.GL, gl.getTalkerId() );
  }




  private void testSatelliteInfo( final SatelliteInfo si, final String id, final int elevation, final int azimuth, final int noise ) {
    assertEquals( id, si.getId() );
    assertEquals( elevation, si.getElevation(), 0.1 );
    assertEquals( azimuth, si.getAzimuth(), 0.1 );
    assertEquals( noise, si.getNoise(), 0.1 );
  }




  @Test
  public void testSetSatelliteCount() {
    gsv.setSatelliteCount( 5 );
    assertEquals( 5, gsv.getSatelliteCount() );
    gsv.setSatelliteCount( 10 );
    assertEquals( 10, gsv.getSatelliteCount() );
  }




  @Test
  public void testSetSatelliteInfo() {
    final List<SatelliteInfo> si = new ArrayList<SatelliteInfo>();
    si.add( new SatelliteInfo( "01", 11, 12, 13 ) );
    si.add( new SatelliteInfo( "02", 21, 22, 23 ) );
    si.add( new SatelliteInfo( "03", 31, 32, 33 ) );
    gsv.setSatelliteInfo( si );

    assertTrue( gsv.toString().contains( ",03,31,032,33,,,,*" ) );
    final List<SatelliteInfo> sat = gsv.getSatelliteInfo();
    assertEquals( 3, sat.size() );
    testSatelliteInfo( sat.get( 0 ), "01", 11, 12, 13 );
    testSatelliteInfo( sat.get( 1 ), "02", 21, 22, 23 );
    testSatelliteInfo( sat.get( 2 ), "03", 31, 32, 33 );
  }




  @Test
  public void testSetSentenceCount() {
    gsv.setSentenceCount( 1 );
    assertEquals( 1, gsv.getSentenceCount() );
    gsv.setSentenceCount( 2 );
    assertEquals( 2, gsv.getSentenceCount() );
  }

}
