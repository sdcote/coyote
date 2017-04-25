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
public class MWDTest {

  public static final String EXAMPLE = "$IIMWD,295.19,T,,M,5.09,N,2.62,M*56";

  private MWDSentence mwd;




  /**
   * @throws java.lang.Exception
   */
  @Before
  public void setUp() throws Exception {
    mwd = new MWDSentenceImpl( EXAMPLE );
  }




  @Test
  public void testMWDParserTalkerId() {
    MWDSentenceImpl mwdp = new MWDSentenceImpl( TalkerId.II );
    assertEquals( TalkerId.II, mwdp.getTalkerId() );
    assertEquals( SentenceId.MWD.toString(), mwdp.getSentenceId() );
  }




  @Test
  public void testGetMagneticWindDirection() {
    assertTrue( Double.isNaN( mwd.getMagneticWindDirection() ) );
  }




  @Test
  public void testGetTrueWindDirection() {
    assertEquals( 295.19, mwd.getTrueWindDirection(), 0.1 );
  }




  @Test
  public void testGetWindSpeed() {
    assertEquals( 2.62, mwd.getWindSpeed(), 0.1 );
  }




  @Test
  public void testGetWindSpeedKnots() {
    assertEquals( 5.09, mwd.getWindSpeedKnots(), 0.1 );
  }




  @Test
  public void testSetMagneticWindDirection() {
    mwd.setMagneticWindDirection( 123.4 );
    assertEquals( 123.4, mwd.getMagneticWindDirection(), 0.1 );
  }




  @Test
  public void testSetTrueWindDirection() {
    mwd.setTrueWindDirection( 234.5 );
    assertEquals( 234.5, mwd.getTrueWindDirection(), 0.1 );
  }




  @Test
  public void testSetWindSpeed() {
    mwd.setWindSpeed( 12.3 );
    assertEquals( 12.3, mwd.getWindSpeed(), 0.1 );
  }




  @Test
  public void testSetWindSpeedKnots() {
    mwd.setWindSpeedKnots( 6.2 );
    assertEquals( 6.2, mwd.getWindSpeedKnots(), 0.1 );
  }
}
