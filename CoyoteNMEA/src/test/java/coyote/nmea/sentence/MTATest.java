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
public class MTATest {

  public static final String EXAMPLE = "$IIMTA,21.5,C";

  private MTASentence mta;




  @Before
  public void setUp() throws Exception {
    mta = new MTASentenceImpl( EXAMPLE );
  }




  @Test
  public void testMTAParserString() {
    assertEquals( TalkerId.II, mta.getTalkerId() );
    assertEquals( SentenceId.MTA.name(), mta.getSentenceId() );
  }




  @Test
  public void testMTAParserTalkerId() {
    MTASentenceImpl empty = new MTASentenceImpl( TalkerId.WI );
    assertEquals( TalkerId.WI, empty.getTalkerId() );
    assertEquals( SentenceId.MTA.name(), empty.getSentenceId() );
    assertTrue( empty.getCharValue( 1 ) == 'C' );
  }




  @Test
  public void testGetTemperature() {
    assertEquals( 21.5, mta.getTemperature(), 0.01 );
  }




  @Test
  public void testSetTemperature() {
    mta.setTemperature( 15.3335 );
    assertEquals( 15.33, mta.getTemperature(), 0.01 );
  }

}
