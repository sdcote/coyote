package coyote.nmea.sentence;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Test;

import coyote.nmea.DataNotAvailableException;
import coyote.nmea.DataStatus;
import coyote.nmea.SentenceId;
import coyote.nmea.TalkerId;


/**
 *
 */
public class ROTTest {

  public static final String EXAMPLE = "$HCROT,-0.3,A";
  public static final String INVALID_EXAMPLE = "$HCROT,-0.3,V";
  ROTSentence rot;
  ROTSentence irot;




  @Before
  public void setUp() throws Exception {
    rot = new ROTSentenceImpl( EXAMPLE );
    irot = new ROTSentenceImpl( INVALID_EXAMPLE );
  }




  @Test
  public void testConstructor() {
    ROTSentence empty = new ROTSentenceImpl( TalkerId.HE );
    assertEquals( TalkerId.HE, empty.getTalkerId() );
    assertEquals( SentenceId.ROT.toString(), empty.getSentenceId() );
    try {
      empty.getRateOfTurn();
    } catch ( DataNotAvailableException e ) {
      // pass
    } catch ( Exception e ) {
      fail( e.getMessage() );
    }
  }




  @Test
  public void testGetStatus() {
    assertEquals( DataStatus.ACTIVE, rot.getStatus() );
    assertEquals( DataStatus.VOID, irot.getStatus() );
  }




  @Test
  public void testSetStatus() {
    rot.setStatus( DataStatus.VOID );
    assertEquals( DataStatus.VOID, rot.getStatus() );
  }




  @Test
  public void testGetRateOfTurn() {
    double value = rot.getRateOfTurn();
    assertEquals( -0.3, value, 0.1 );
  }




  @Test
  public void testSetRateOfTurn() {
    final double newValue = 0.5;
    rot.setRateOfTurn( newValue );
    assertEquals( newValue, rot.getRateOfTurn(), 0.1 );
  }
  
}
