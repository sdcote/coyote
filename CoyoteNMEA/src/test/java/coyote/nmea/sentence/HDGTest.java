package coyote.nmea.sentence;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
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
public class HDGTest {

  public static final String EXAMPLE = "$HCHDG,123.4,1.2,E,1.2,W";

  HDGSentence hdg;




  @Before
  public void setUp() throws Exception {
    hdg = new HDGSentenceImpl( EXAMPLE );
  }



  @Test
  public void testConstructor() {
    HDGSentence empty = new HDGSentenceImpl( TalkerId.HC );
    assertEquals( TalkerId.HC, empty.getTalkerId() );
    assertEquals( SentenceId.HDG.toString(), empty.getSentenceId() );
    try {
      empty.getHeading();
    } catch ( DataNotAvailableException e ) {
      // pass
    } catch ( Exception e ) {
      fail( e.getMessage() );
    }
  }




  @Test
  public void testIsTrue() {
    assertFalse( hdg.isTrue() );
  }




  @Test
  public void testHDGParserString() {
    assertTrue( hdg.isValid() );
    assertEquals( TalkerId.HC, hdg.getTalkerId() );
    assertEquals( "HDG", hdg.getSentenceId() );
  }




  @Test
  public void testHDGParserTalkerId() {
    HDGSentenceImpl hdgp = new HDGSentenceImpl( TalkerId.HC );
    assertTrue( hdgp.isValid() );
    assertEquals( TalkerId.HC, hdgp.getTalkerId() );
    assertEquals( "HDG", hdgp.getSentenceId() );
  }




  @Test
  public void testGetDeviation() {
    assertEquals( 1.2, hdg.getDeviation(), 0.1 );
  }




  @Test
  public void testGetHeading() {
    assertEquals( 123.4, hdg.getHeading(), 0.1 );
  }




  @Test
  public void testGetVariation() {
    // 1.2 degrees west -> -1.2
    assertEquals( -1.2, hdg.getVariation(), 0.1 );
  }




  @Test
  public void testSetDeviationWest() {
    final double dev = -5.5;
    hdg.setDeviation( dev );
    assertEquals( dev, hdg.getDeviation(), 0.1 );
    assertTrue( hdg.toString().contains( ",005.5,W," ) );
  }




  @Test
  public void testSetDeviationEast() {
    final double dev = 5.5;
    hdg.setDeviation( dev );
    assertEquals( dev, hdg.getDeviation(), 0.1 );
    assertTrue( hdg.toString().contains( ",005.5,E," ) );
  }




  @Test
  public void testSetDeviationTooHigh() {
    final double value = 180.000001;
    try {
      hdg.setDeviation( value );
      fail( "Did not throw exception" );
    } catch ( IllegalArgumentException iae ) {
      // pass
    } catch ( Exception e ) {
      fail( e.getMessage() );
    }
  }




  @Test
  public void testSetDeviationTooLow() {
    final double value = -180.000001;
    try {
      hdg.setHeading( value );
      fail( "Did not throw exception" );
    } catch ( IllegalArgumentException iae ) {
      // pass
    } catch ( Exception e ) {
      fail( e.getMessage() );
    }
  }




  @Test
  public void testSetHeading() {
    final double value = 359.9;
    hdg.setHeading( value );
    assertEquals( value, hdg.getHeading(), 0.1 );
  }




  @Test
  public void testSetHeadingTooHigh() {
    final double value = 360.000001;
    try {
      hdg.setHeading( value );
      fail( "Did not throw exception" );
    } catch ( IllegalArgumentException iae ) {
      // pass
    } catch ( Exception e ) {
      fail( e.getMessage() );
    }
  }




  @Test
  public void testSetHeadingTooLow() {
    final double value = -0.000001;
    try {
      hdg.setHeading( value );
      fail( "Did not throw exception" );
    } catch ( IllegalArgumentException iae ) {
      // pass
    } catch ( Exception e ) {
      fail( e.getMessage() );
    }
  }




  @Test
  public void testSetVariationEast() {
    final double var = 179.9;
    hdg.setVariation( var );
    assertEquals( var, hdg.getVariation(), 0.1 );
    assertTrue( hdg.toString().contains( ",179.9,E*" ) );
  }




  @Test
  public void testSetVariationWest() {
    final double var = -0.1;
    hdg.setVariation( var );
    assertEquals( var, hdg.getVariation(), 0.1 );
    assertTrue( hdg.toString().contains( ",000.1,W*" ) );
  }




  @Test
  public void testSetVariationTooHigh() {
    final double var = 180.00001;
    try {
      hdg.setVariation( var );
      fail( "Did not throw exception" );
    } catch ( IllegalArgumentException iae ) {
      // pass
    } catch ( Exception e ) {
      fail( e.getMessage() );
    }
  }




  @Test
  public void testSetVariationTooLow() {
    final double var = -180.00001;
    try {
      hdg.setVariation( var );
      fail( "Did not throw exception" );
    } catch ( IllegalArgumentException iae ) {
      // pass
    } catch ( Exception e ) {
      fail( e.getMessage() );
    }
  }

}