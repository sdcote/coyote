package coyote.nmea.sentence;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import coyote.nmea.TalkerId;


/**
 *
 */
public class VDOTest {

  public static final String EXAMPLE = "!AIVDO,1,1,,B,H1c2;qA@PU>0U>060<h5=>0:1Dp,2*7D";
  public static final String PART1 = "!AIVDO,2,1,5,B,E1c2;q@b44ah4ah0h:2ab@70VRpU<Bgpm4:gP50HH`Th`QF5,0*7B";
  public static final String PART2 = "!AIVDO,2,2,5,B,1CQ1A83PCAH0,0*60";

  private AISSentence vdo;
  private AISSentence frag1;
  private AISSentence frag2;




  @Before
  public void setUp() throws Exception {
    vdo = new VDOSentenceImpl( EXAMPLE );
    frag1 = new VDOSentenceImpl( PART1 );
    frag2 = new VDOSentenceImpl( PART2 );
  }




  @Test
  public void testVDOParserTalkerId() {
    AISSentence empty = new VDOSentenceImpl( TalkerId.AI );
    assertEquals( '!', empty.getBeginChar() );
    assertEquals( TalkerId.AI, empty.getTalkerId() );
    assertEquals( "VDO", empty.getSentenceId() );
    assertEquals( 6, empty.getFieldCount() );
  }




  @Test
  public void testGetNumberOfFragments() {
    assertEquals( 1, vdo.getNumberOfFragments() );
    assertEquals( 2, frag1.getNumberOfFragments() );
    assertEquals( 2, frag2.getNumberOfFragments() );
  }




  @Test
  public void testGetFragmentNumber() {
    assertEquals( 1, vdo.getFragmentNumber() );
    assertEquals( 1, frag1.getFragmentNumber() );
    assertEquals( 2, frag2.getFragmentNumber() );
  }




  @Test
  public void testGetMessageId() {
    assertEquals( "5", frag1.getMessageId() );
    assertEquals( "5", frag2.getMessageId() );
  }




  @Test
  public void testGetRadioChannel() {
    assertEquals( "B", vdo.getRadioChannel() );
    assertEquals( "B", frag1.getRadioChannel() );
    assertEquals( "B", frag2.getRadioChannel() );
  }




  @Test
  public void testGetPayload() {
    final String pl = "H1c2;qA@PU>0U>060<h5=>0:1Dp";
    final String f1 = "E1c2;q@b44ah4ah0h:2ab@70VRpU<Bgpm4:gP50HH`Th`QF5";
    final String f2 = "1CQ1A83PCAH0";
    assertEquals( pl, vdo.getPayload() );
    assertEquals( f1, frag1.getPayload() );
    assertEquals( f2, frag2.getPayload() );
  }




  @Test
  public void testGetFillBits() {
    assertEquals( 2, vdo.getFillBits() );
    assertEquals( 0, frag1.getFillBits() );
    assertEquals( 0, frag2.getFillBits() );
  }




  @Test
  public void testIsFragmented() {
    assertFalse( vdo.isFragmented() );
    assertTrue( frag1.isFragmented() );
    assertTrue( frag2.isFragmented() );
  }




  @Test
  public void testIsFirstFragment() {
    assertTrue( vdo.isFirstFragment() );
    assertTrue( frag1.isFirstFragment() );
    assertFalse( frag2.isFirstFragment() );
  }




  @Test
  public void testIsLastFragment() {
    assertTrue( vdo.isLastFragment() );
    assertFalse( frag1.isLastFragment() );
    assertTrue( frag2.isLastFragment() );
  }




  @Test
  public void testIsPartOfMessage() {
    assertFalse( vdo.isPartOfMessage( frag1 ) );
    assertFalse( vdo.isPartOfMessage( frag2 ) );
    assertFalse( frag1.isPartOfMessage( vdo ) );
    assertFalse( frag2.isPartOfMessage( vdo ) );
    assertTrue( frag1.isPartOfMessage( frag2 ) );
    assertFalse( frag2.isPartOfMessage( frag1 ) );
  }




  @Test
  public void testToStringWithAIS() {
    AISSentence example = new VDOSentenceImpl( EXAMPLE );
    AISSentence empty = new VDOSentenceImpl( TalkerId.AI );
    assertEquals( EXAMPLE, example.toString() );
    assertEquals( "!AIVDO,,,,,,*55", empty.toString() );
  }

}
