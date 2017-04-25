package coyote.nmea.sentence;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Test;

import coyote.nmea.DataStatus;
import coyote.nmea.SentenceId;
import coyote.nmea.TalkerId;
import coyote.nmea.Units;


/**
 *
 */
public class MWVTest {

  public static final String EXAMPLE = "$IIMWV,125.1,T,5.5,M,A";

  private MWVSentence mwv;




  @Before
  public void setUp() throws Exception {
    mwv = new MWVSentenceImpl( EXAMPLE );
  }




  @Test
  public void testMWVParserTalkerId() {
    MWVSentenceImpl mwvp = new MWVSentenceImpl( TalkerId.II );
    assertEquals( TalkerId.II, mwvp.getTalkerId() );
    assertEquals( SentenceId.MWV.toString(), mwvp.getSentenceId() );
    assertEquals( DataStatus.VOID, mwvp.getStatus() );
  }




  @Test
  public void testGetAngle() {
    assertEquals( 125.1, mwv.getAngle(), 0.1 ); // "$IIMWV,125.1,T,5.5,A"
  }




  @Test
  public void testGetSpeed() {
    assertEquals( 5.5, mwv.getSpeed(), 0.1 );
  }




  @Test
  public void testGetSpeedUnit() {
    assertEquals( Units.METER, mwv.getSpeedUnit() );
  }




  @Test
  public void testGetStatus() {
    assertEquals( DataStatus.ACTIVE, mwv.getStatus() );
  }




  @Test
  public void testIsTrue() {
    assertTrue( mwv.isTrue() );
  }




  @Test
  public void testSetAngle() {
    final double angle = 88.123;
    mwv.setAngle( angle );
    assertEquals( angle, mwv.getAngle(), 0.1 );
  }




  @Test
  public void testSetNegativeAngle() {
    final double angle = -0.1;
    try {
      mwv.setAngle( angle );
      fail( "Did not throw exception" );
    } catch ( IllegalArgumentException iae ) {
      // pass
    } catch ( Exception e ) {
      fail( e.getMessage() );
    }
  }




  @Test
  public void testSetAngleOutOfRange() {
    final double angle = 360.1;
    try {
      mwv.setAngle( angle );
      fail( "Did not throw exception" );
    } catch ( IllegalArgumentException iae ) {
      // pass
    } catch ( Exception e ) {
      fail( e.getMessage() );
    }
  }




  @Test
  public void testSetSpeed() {
    final double speed = 7.75;
    mwv.setSpeed( speed );
    assertEquals( speed, mwv.getSpeed(), 0.1 );
  }




  @Test
  public void testSetNegativeSpeed() {
    final double speed = -0.01;
    try {
      mwv.setSpeed( speed );
      fail( "Did not throw exception" );
    } catch ( IllegalArgumentException iae ) {
      // pass
    } catch ( Exception e ) {
      fail( e.getMessage() );
    }
  }




  @Test
  public void testSetSpeedUnit() {
    mwv.setSpeedUnit( Units.KMH );
    assertEquals( Units.KMH, mwv.getSpeedUnit() );
  }




  @Test
  public void testSetInvalidSpeedUnit() {
    try {
      mwv.setSpeedUnit( Units.FATHOMS );
      fail( "Did not throw exception" );
    } catch ( IllegalArgumentException iae ) {
      // pass
    } catch ( Exception e ) {
      fail( e.getMessage() );
    }
  }




  @Test
  public void testSetStatus() {
    mwv.setStatus( DataStatus.VOID );
    assertEquals( DataStatus.VOID, mwv.getStatus() );
  }




  @Test
  public void testSetTrue() {
    assertTrue( mwv.isTrue() );
    mwv.setTrue( false );
    assertFalse( mwv.isTrue() );
  }

}