package coyote.nmea.sentence;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Test;

import coyote.nmea.FaaMode;
import coyote.nmea.TalkerId;


/**
 * 
 */
public class VTGTest {

  public static final String EXAMPLE = "$GPVTG,360.0,T,348.7,M,16.89,N,31.28,K,A";

  private VTGSentence empty;
  private VTGSentence vtg;




  @Before
  public void setUp() {
    try {
      empty = new VTGSentenceImpl( TalkerId.GP );
      vtg = new VTGSentenceImpl( EXAMPLE );
    } catch ( Exception e ) {
      fail( e.getMessage() );
    }
  }




  @Test
  public void testConstructor() {
    assertEquals( 9, empty.getFieldCount() );
  }




  @Test
  public void testGetMagneticCourse() {
    assertEquals( 348.7, vtg.getMagneticCourse(), 0.001 );
  }




  @Test
  public void testGetMode() {
    assertEquals( FaaMode.AUTOMATIC, vtg.getMode() );
  }




  @Test
  public void testGetSpeedKmh() {
    assertEquals( 31.28, vtg.getSpeedKmh(), 0.001 );
  }




  @Test
  public void testGetSpeedKnots() {
    assertEquals( 16.89, vtg.getSpeedKnots(), 0.001 );
  }




  @Test
  public void testGetTrueCourse() {
    assertEquals( 360.0, vtg.getTrueCourse(), 0.001 );
  }




  @Test
  public void testSetMagneticCourse() {
    final double mcog = 95.56789;
    vtg.setMagneticCourse( mcog );
    assertTrue( vtg.toString().contains( ",095.6,M," ) );
    assertEquals( mcog, vtg.getMagneticCourse(), 0.1 );
  }




  @Test
  public void testSetMagneticCourseWithNegativeValue() {
    try {
      vtg.setMagneticCourse( -0.001 );
      fail( "Did not throw exception" );
    } catch ( IllegalArgumentException e ) {
      assertTrue( e.getMessage().contains( "0..360" ) );
    } catch ( Exception e ) {
      fail( e.getMessage() );
    }
  }




  @Test
  public void testSetMagneticCourseWithValueGreaterThanAllowed() {
    try {
      vtg.setMagneticCourse( 360.001 );
      fail( "Did not throw exception" );
    } catch ( IllegalArgumentException e ) {
      assertTrue( e.getMessage().contains( "0..360" ) );
    } catch ( Exception e ) {
      fail( e.getMessage() );
    }
  }




  @Test
  public void testSetMode() {
    vtg.setMode( FaaMode.MANUAL );
    assertEquals( FaaMode.MANUAL, vtg.getMode() );
    vtg.setMode( FaaMode.SIMULATED );
    assertEquals( FaaMode.SIMULATED, vtg.getMode() );
  }




  @Test
  public void testSetModeWhenOmitted() {
    VTGSentenceImpl parser = new VTGSentenceImpl( "$GPVTG,360.0,T,348.7,M,16.89,N,31.28,K" );
    parser.setMode( FaaMode.MANUAL );
    assertEquals( FaaMode.MANUAL, parser.getMode() );
    parser.setMode( FaaMode.SIMULATED );
    assertEquals( FaaMode.SIMULATED, parser.getMode() );
  }




  @Test
  public void testSetSpeedKmh() {
    final double kmh = 12.56789;
    vtg.setSpeedKmh( kmh );
    assertTrue( vtg.toString().contains( ",12.57,K," ) );
    assertEquals( kmh, vtg.getSpeedKmh(), 0.01 );
  }




  @Test
  public void testSetSpeedKmhWithNegativeValue() {
    try {
      vtg.setSpeedKmh( -0.0001 );
    } catch ( IllegalArgumentException e ) {
      assertTrue( e.getMessage().contains( "cannot be negative" ) );
    }
  }




  @Test
  public void testSetSpeedKnots() {
    final double kn = 11.6789;
    vtg.setSpeedKnots( kn );
    assertTrue( vtg.toString().contains( ",11.68,N," ) );
    assertEquals( kn, vtg.getSpeedKnots(), 0.01 );
  }




  @Test
  public void testSetSpeedKnotsWithNegativeValue() {
    try {
      vtg.setSpeedKnots( -0.0001 );
    } catch ( IllegalArgumentException e ) {
      assertTrue( e.getMessage().contains( "cannot be negative" ) );
    }
  }




  @Test
  public void testSetTrueCourse() {
    final double tcog = 90.56789;
    vtg.setTrueCourse( tcog );
    assertTrue( vtg.toString().contains( ",090.6,T," ) );
    assertEquals( tcog, vtg.getTrueCourse(), 0.1 );
  }




  @Test
  public void testSetTrueCourseWithNegativeValue() {
    try {
      vtg.setTrueCourse( -0.001 );
      fail( "Did not throw exception" );
    } catch ( IllegalArgumentException e ) {
      assertTrue( e.getMessage().contains( "0..360" ) );
    } catch ( Exception e ) {
      fail( e.getMessage() );
    }
  }




  @Test
  public void testSetTrueCourseWithValueGreaterThanAllowed() {
    try {
      vtg.setTrueCourse( 360.001 );
      fail( "Did not throw exception" );
    } catch ( IllegalArgumentException e ) {
      assertTrue( e.getMessage().contains( "0..360" ) );
    } catch ( Exception e ) {
      fail( e.getMessage() );
    }
  }

}
