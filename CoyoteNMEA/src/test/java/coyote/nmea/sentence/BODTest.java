package coyote.nmea.sentence;

//import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import coyote.nmea.DataNotAvailableException;
import coyote.nmea.ParseException;
import coyote.nmea.TalkerId;


/**
 * Tests the BOD sentence.
 */
public class BODTest {

  public static final String EXAMPLE = "$GPBOD,234.9,T,228.8,M,RUSKI,*1D";

  private BODSentence empty;
  private BODSentence bod;




  @Before
  public void setUp() throws Exception {
    try {
      empty = new BODSentenceImpl( TalkerId.GP );
      bod = new BODSentenceImpl( EXAMPLE );
    } catch ( final Exception e ) {
      fail( e.getMessage() );
    }
  }




  @After
  public void tearDown() throws Exception {}




  @Test
  public void testConstructor() {
    assertEquals( 6, empty.getFieldCount() );
  }




  @Test
  public void testConstructorWithInvalidSentence() {
    try {
      new BODSentenceImpl( "$HUBBA,habba,doo,dah,doo" );
    } catch ( final IllegalArgumentException e ) {
      // OK
    } catch ( final Exception e ) {
      fail( e.getMessage() );
    }
  }




  @Test
  public void testConstructorWithNullString() {
    try {
      new BODSentenceImpl( (String)null );
    } catch ( final IllegalArgumentException e ) {
      // OK
    } catch ( final Exception e ) {
      fail( e.getMessage() );
    }
  }




  @Test
  public void testConstructorWithNullTalkerId() {
    try {
      new BODSentenceImpl( (TalkerId)null );
    } catch ( final IllegalArgumentException e ) {
      // OK
    } catch ( final Exception e ) {
      fail( e.getMessage() );
    }
  }




  @Test
  public void testConstructorWithRandomString() {
    try {
      new BODSentenceImpl( "foobar and baz" );
    } catch ( final IllegalArgumentException e ) {
      // OK
    } catch ( final Exception e ) {
      fail( e.getMessage() );
    }
  }




  @Test
  public void testGetDestinationWaypointId() {
    try {
      final String id = bod.getDestinationWaypointId();
      assertEquals( "RUSKI", id );
    } catch ( final ParseException e ) {
      fail( e.getMessage() );
    }
  }




  @Test
  public void testGetMagneticBearing() {
    try {
      final double b = bod.getMagneticBearing();
      assertEquals( 228.8, b, 0.001 );
    } catch ( final ParseException e ) {
      fail( e.getMessage() );
    }
  }




  @Test
  public void testGetOriginWaypointId() {

    try {
      bod.getOriginWaypointId();
    } catch ( final DataNotAvailableException e ) {
      // ok, field is empty
    } catch ( final Exception e ) {
      fail( e.getMessage() );
    }
  }




  @Test
  public void testGetTrueBearing() {
    try {
      final double b = bod.getTrueBearing();
      assertEquals( 234.9, b, 0.001 );
    } catch ( final ParseException e ) {
      fail( e.getMessage() );
    }
  }




  @Test
  public void testSetDestinationWaypointId() {
    try {
      bod.setDestinationWaypointId( "HILLIARD" );
      assertEquals( "HILLIARD", bod.getDestinationWaypointId() );
    } catch ( final Exception e ) {
      fail( e.getMessage() );
    }
  }




  @Test
  public void testSetDestinationWaypointIdWithEmptyStr() {
    try {
      bod.setDestinationWaypointId( "" );
      bod.getDestinationWaypointId();
    } catch ( final Exception e ) {
      assertTrue( e instanceof DataNotAvailableException );
    }
  }




  @Test
  public void testSetDestinationWaypointIdWithNull() {
    try {
      bod.setDestinationWaypointId( null );
      bod.getDestinationWaypointId();
    } catch ( final Exception e ) {
      assertTrue( e instanceof DataNotAvailableException );
    }
  }




  @Test
  public void testSetMagneticBearing() {
    final double bearing = 180.0;
    try {
      bod.setMagneticBearing( bearing );
      assertTrue( bearing == bod.getMagneticBearing() );
    } catch ( final Exception e ) {
      fail( e.getMessage() );
    }
  }




  @Test
  public void testSetMagneticBearingWithGreaterThanAllowed() {
    try {
      bod.setMagneticBearing( 360.01 );
      fail( "Did not throw exception" );
    } catch ( final Exception e ) {
      assertTrue( e instanceof IllegalArgumentException );
    }
  }




  @Test
  public void testSetMagneticBearingWithNegativeValue() {
    try {
      bod.setMagneticBearing( -0.01 );
      fail( "Did not throw exception" );
    } catch ( final Exception e ) {
      assertTrue( e instanceof IllegalArgumentException );
    }
  }




  @Test
  public void testSetMagneticBearingWithRounding() {
    final double bearing = 65.654321;
    try {
      bod.setMagneticBearing( bearing );
      assertTrue( bod.toString().contains( ",065.7," ) );
      assertEquals( bearing, bod.getMagneticBearing(), 0.1 );
    } catch ( final Exception e ) {
      fail( e.getMessage() );
    }
  }




  @Test
  public void testSetOriginWaypointId() {
    try {
      bod.setOriginWaypointId( "DUBLIN" );
      assertEquals( "DUBLIN", bod.getOriginWaypointId() );
    } catch ( final Exception e ) {
      fail( e.getMessage() );
    }
  }




  @Test
  public void testSetTrueBearing() {
    final double bearing = 180.0;
    try {
      bod.setTrueBearing( bearing );
      assertTrue( bearing == bod.getTrueBearing() );
    } catch ( final Exception e ) {
      fail( e.getMessage() );
    }
  }




  @Test
  public void testSetTrueBearingGreaterThanAllowed() {
    try {
      bod.setTrueBearing( 360.01 );
      fail( "Did not throw exception" );
    } catch ( final Exception e ) {
      assertTrue( e instanceof IllegalArgumentException );
    }
  }




  @Test
  public void testSetTrueBearingWithNegativeValue() {
    try {
      bod.setTrueBearing( -0.01 );
      fail( "Did not throw exception" );
    } catch ( final Exception e ) {
      assertTrue( e instanceof IllegalArgumentException );
    }
  }




  @Test
  public void testSetTrueBearingWithRounding() {
    final double bearing = 90.654321;
    try {
      bod.setTrueBearing( bearing );
      assertTrue( bod.toString().contains( ",090.7," ) );
      assertEquals( bearing, bod.getTrueBearing(), 0.1 );
    } catch ( final Exception e ) {
      fail( e.getMessage() );
    }
  }

}
