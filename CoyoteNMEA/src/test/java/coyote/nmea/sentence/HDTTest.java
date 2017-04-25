package coyote.nmea.sentence;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Test;

import coyote.nmea.DataNotAvailableException;
import coyote.nmea.SentenceId;
import coyote.nmea.TalkerId;


/**
 *
 */
public class HDTTest {

  public static final String EXAMPLE = "$HCHDT,90.1,T";
  HDTSentence hdt;




  /**
   * @throws java.lang.Exception
   */
  @Before
  public void setUp() throws Exception {
    hdt = new HDTSentenceImpl( EXAMPLE );
  }




  /**
   * 
   */
  @Test
  public void testConstructor() {
    HDTSentence empty = new HDTSentenceImpl( TalkerId.HE );
    assertEquals( TalkerId.HE, empty.getTalkerId() );
    assertEquals( SentenceId.HDT.toString(), empty.getSentenceId() );
    try {
      empty.getHeading();
    } catch ( DataNotAvailableException e ) {
      // pass
    } catch ( Exception e ) {
      fail( e.getMessage() );
    }
  }




  /**
   * 
   */
  @Test
  public void testIsTrue() {
    assertTrue( hdt.isTrue() );
  }




  /**
   * 
   */
  @Test
  public void testGetHeading() {
    double value = hdt.getHeading();
    assertEquals( 90.0, value, 0.1 );
  }




  /**
   * 
   */
  @Test
  public void testSetHeading() {
    hdt.setHeading( 123.45 );
    assertEquals( 123.5, hdt.getHeading(), 0.1 );
  }




  /**
   * 
   */
  @Test
  public void testSetNegativeHeading() {
    try {
      hdt.setHeading( -0.005 );
      fail( "Did not throw exception" );
    } catch ( IllegalArgumentException iae ) {
      // pass
    } catch ( Exception e ) {
      fail( e.getMessage() );
    }
  }




  /**
   * 
   */
  @Test
  public void testSetHeadingTooHigh() {
    try {
      hdt.setHeading( 360.0001 );
      fail( "Did not throw exception" );
    } catch ( IllegalArgumentException iae ) {
      // pass
    } catch ( Exception e ) {
      fail( e.getMessage() );
    }
  }

}
