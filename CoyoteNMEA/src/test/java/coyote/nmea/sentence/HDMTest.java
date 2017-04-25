package coyote.nmea.sentence;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Test;

import coyote.nmea.DataNotAvailableException;
import coyote.nmea.SentenceId;
import coyote.nmea.TalkerId;


/**
 * 
 */
public class HDMTest {

  public static final String EXAMPLE = "$GPHDM,90.0,M";

  HDMSentence hdm;




  /**
   * @throws java.lang.Exception
   */
  @Before
  public void setUp() throws Exception {
    hdm = new HDMSentenceImpl( EXAMPLE );
  }




  @Test
  public void testConstructor() {
    HDMSentence empty = new HDMSentenceImpl( TalkerId.HC );
    assertEquals( TalkerId.HC, empty.getTalkerId() );
    assertEquals( SentenceId.HDM.toString(), empty.getSentenceId() );
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
    assertFalse( hdm.isTrue() );
  }




  /**
   * 
   */
  @Test
  public void testGetHeading() {
    double value = hdm.getHeading();
    assertEquals( 90.0, value, 0.1 );
  }




  /**
   * 
   */
  @Test
  public void testSetHeading() {
    hdm.setHeading( 123.45 );
    assertEquals( 123.5, hdm.getHeading(), 0.1 );
  }




  /**
   * 
   */
  @Test
  public void testSetNegativeHeading() {
    try {
      hdm.setHeading( -0.005 );
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
      hdm.setHeading( 360.0001 );
      fail( "Did not throw exception" );
    } catch ( IllegalArgumentException iae ) {
      // pass
    } catch ( Exception e ) {
      fail( e.getMessage() );
    }
  }
  
}
