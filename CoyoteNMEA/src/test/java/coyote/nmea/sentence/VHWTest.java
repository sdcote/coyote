package coyote.nmea.sentence;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import coyote.nmea.SentenceId;
import coyote.nmea.TalkerId;


/**
 *
 */
public class VHWTest {

  public static final String EXAMPLE = "$VWVHW,000.0,T,001.5,M,1.0,N,1.85,K";

  private VHWSentenceImpl vhw;




  @Before
  public void setUp() throws Exception {
    vhw = new VHWSentenceImpl( EXAMPLE );
  }




  @Test
  public void testConstructorString() {
    assertTrue( vhw.getTalkerId() == TalkerId.VW );
    assertTrue( SentenceId.valueOf( vhw.getSentenceId() ) == SentenceId.VHW );
  }




  @Test
  public void testConstructorTalkerId() {
    VHWSentenceImpl empty = new VHWSentenceImpl( TalkerId.II );
    assertEquals( 8, empty.getFieldCount() );
    assertTrue( 'T' == empty.getCharValue( 1 ) );
    assertTrue( 'M' == empty.getCharValue( 3 ) );
    assertTrue( 'N' == empty.getCharValue( 5 ) );
    assertTrue( 'K' == empty.getCharValue( 7 ) );
    assertEquals( "VHW", empty.getSentenceId() );
    assertTrue( empty.getTalkerId() == TalkerId.II );
  }




  @Test
  public void testGetHeading() {
    assertEquals( 0.0, vhw.getHeading(), 0.1 );
  }




  @Test
  public void testGetMagneticHeading() {
    assertEquals( 1.5, vhw.getMagneticHeading(), 0.1 );
  }




  @Test
  public void testGetSpeedKilometres() {
    assertEquals( 1.85, vhw.getSpeedKmh(), 0.01 );
  }




  @Test
  public void testGetSpeedKnots() {
    assertEquals( 1.0, vhw.getSpeedKnots(), 0.1 );
  }




  @Test
  public void testIsTrue() {
    // should always return true
    assertTrue( vhw.isTrue() );
  }




  @Test
  public void testSetHeading() {
    vhw.setHeading( 90.456 );
    assertEquals( 90.5, vhw.getHeading(), 0.1 );
  }




  @Test
  public void testSetMagneticHeading() {
    vhw.setMagneticHeading( 123.4567 );
    assertEquals( 123.5, vhw.getMagneticHeading(), 0.1 );
  }




  @Test
  public void testSetSpeedKilometres() {
    vhw.setSpeedKmh( 5.5555 );
    assertEquals( 5.6, vhw.getSpeedKmh(), 0.1 );
  }




  @Test
  public void testSetSpeedKnots() {
    vhw.setSpeedKnots( 12.155 );
    assertEquals( 12.2, vhw.getSpeedKnots(), 0.1 );
  }

}