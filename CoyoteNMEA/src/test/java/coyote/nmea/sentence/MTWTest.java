package coyote.nmea.sentence;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import coyote.nmea.TalkerId;


/**
 * 
 */
public class MTWTest {

  public static final String EXAMPLE = "$IIMTW,17.75,C";

  private MTWSentence mtw;




  @Before
  public void setUp() throws Exception {
    mtw = new MTWSentenceImpl( EXAMPLE );
  }




  @Test
  public void testMTWParserString() {
    assertEquals( "MTW", mtw.getSentenceId() );
    assertEquals( TalkerId.II, mtw.getTalkerId() );
  }




  @Test
  public void testMTWParserTalkerId() {
    MTWSentenceImpl empty = new MTWSentenceImpl( TalkerId.II );
    assertEquals( "MTW", empty.getSentenceId() );
    assertEquals( TalkerId.II, empty.getTalkerId() );
    assertEquals( 2, empty.getFieldCount() );
    assertEquals( 'C', empty.getCharValue( 1 ) );
  }




  @Test
  public void testGetTemperature() {
    assertEquals( 17.75, mtw.getTemperature(), 0.01 );
  }




  @Test
  public void testSetTemperature() {
    mtw.setTemperature( 12.345 );
    assertEquals( 12.345, mtw.getTemperature(), 0.01 );
  }

}