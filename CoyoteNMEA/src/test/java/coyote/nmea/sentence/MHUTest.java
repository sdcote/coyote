package coyote.nmea.sentence;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import coyote.nmea.SentenceId;
import coyote.nmea.TalkerId;


/**
 *
 */
public class MHUTest {

  public static final String EXAMPLE = "$IIMHU,66.0,5.0,3.0,C";

  private MHUSentence mhu;
  private MHUSentence empty;




  @Before
  public void setUp() {
    mhu = new MHUSentenceImpl( EXAMPLE );
    empty = new MHUSentenceImpl( TalkerId.II );
    assertEquals( 4, mhu.getFieldCount() );
  }




  @Test
  public void testEmptySentenceConstructor() {
    assertEquals( TalkerId.II, empty.getTalkerId() );
    assertEquals( SentenceId.MHU.toString(), empty.getSentenceId() );
    assertEquals( 4, empty.getFieldCount() );
    assertEquals( 'C', empty.getDewPointUnit() );
  }




  @Test
  public void testGetRelativeHumidity() throws Exception {
    assertEquals( 66.0, mhu.getRelativeHumidity(), 0.1 );
  }




  @Test
  public void testGetAbsoluteHumidity() throws Exception {
    assertEquals( 5.0, mhu.getAbsoluteHumidity(), 0.1 );
  }




  @Test
  public void testGetDewPoint() throws Exception {
    assertEquals( 3.0, mhu.getDewPoint(), 0.1 );
  }




  @Test
  public void testGetDewPointUnit() throws Exception {
    assertEquals( 'C', mhu.getDewPointUnit() );
  }




  @Test
  public void testSetRelativeHumidity() throws Exception {
    mhu.setRelativeHumidity( 55.55555 );
    assertEquals( 55.6, mhu.getRelativeHumidity(), 0.1 );
  }




  @Test
  public void testSetAbsoluteHumidity() throws Exception {
    mhu.setAbsoluteHumidity( 6.1234 );
    assertEquals( 6.1, mhu.getAbsoluteHumidity(), 0.1 );
  }




  @Test
  public void testSetDewPoint() throws Exception {
    mhu.setDewPoint( 1.2356 );
    assertEquals( 1.2, mhu.getDewPoint(), 0.1 );
  }




  @Test
  public void testSetDewPointUnit() throws Exception {
    mhu.setDewPointUnit( 'F' );
    assertEquals( 'F', mhu.getDewPointUnit() );
  }
}