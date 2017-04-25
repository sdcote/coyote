package coyote.nmea.sentence;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import coyote.nmea.SentenceId;
import coyote.nmea.TalkerId;


public class VDRTest {

  public static final String EXAMPLE = "$IIVDR,123.4,T,124.5,M,1.2,N";

  private VDRSentence vdr;
  private VDRSentence empty;




  @Before
  public void setUp() throws Exception {
    vdr = new VDRSentenceImpl( EXAMPLE );
    empty = new VDRSentenceImpl( TalkerId.IN );
  }




  @Test
  public void testVDRParserString() {
    assertEquals( TalkerId.II, vdr.getTalkerId() );
    assertEquals( SentenceId.VDR.name(), vdr.getSentenceId() );
    assertEquals( 6, vdr.getFieldCount() );
  }




  @Test
  public void testVDRParserTalkerId() {
    assertEquals( TalkerId.IN, empty.getTalkerId() );
    assertEquals( SentenceId.VDR.name(), empty.getSentenceId() );
    assertEquals( 6, empty.getFieldCount() );
    assertTrue( empty.toString().startsWith( "$INVDR,,T,,M,,N*" ) );
  }




  @Test
  public void testGetMagneticDirection() {
    assertEquals( 124.5, vdr.getMagneticDirection(), 0.1 );
  }




  @Test
  public void testGetSpeed() {
    assertEquals( 1.2, vdr.getSpeed(), 0.1 );
  }




  @Test
  public void testGetTrueDirection() {
    assertEquals( 123.4, vdr.getTrueDirection(), 0.1 );
  }




  @Test
  public void testSetMagneticDirection() {
    final double dir = 45.123;
    empty.setMagneticDirection( dir );
    assertEquals( dir, empty.getMagneticDirection(), 0.1 );
  }




  @Test
  public void testSetSpeed() {
    final double speed = 2.124;
    empty.setSpeed( speed );
    assertEquals( speed, empty.getSpeed(), 0.1 );
  }




  @Test
  public void testSetTrueDirection() {
    final double dir = 55.234;
    empty.setTrueDirection( dir );
    assertEquals( dir, empty.getTrueDirection(), 0.1 );
  }

}
