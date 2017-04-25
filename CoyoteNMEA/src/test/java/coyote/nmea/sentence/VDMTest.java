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
public class VDMTest {

  public static final String EXAMPLE = "!AIVDM,1,1,,A,403OviQuMGCqWrRO9>E6fE700@GO,0*4D";
  public static final String PART1 = "!AIVDM,2,1,1,A,55?MbV02;H;s<HtKR20EHE:0@T4@Dn2222222216L961O5Gf0NSQEp6ClRp8,0*1C";
  public static final String PART2 = "!AIVDM,2,2,1,A,88888888880,2*25";

  private AISSentence vdm;
  private AISSentence frag1;
  private AISSentence frag2;




  @Before
  public void setUp() throws Exception {
    vdm = new VDMSentenceImpl( EXAMPLE );
    frag1 = new VDMSentenceImpl( PART1 );
    frag2 = new VDMSentenceImpl( PART2 );
  }




  @Test
  public void testVDMParserTalkerId() {
    AISSentence empty = new VDMSentenceImpl( TalkerId.AI );
    assertEquals( TalkerId.AI, empty.getTalkerId() );
    assertEquals( "VDM", empty.getSentenceId() );
    assertEquals( 6, empty.getFieldCount() );
  }




  @Test
  public void testGetNumberOfFragments() {
    assertEquals( 1, vdm.getNumberOfFragments() );
    assertEquals( 2, frag1.getNumberOfFragments() );
    assertEquals( 2, frag2.getNumberOfFragments() );
  }




  @Test
  public void testGetFragmentNumber() {
    assertEquals( 1, vdm.getFragmentNumber() );
    assertEquals( 1, frag1.getFragmentNumber() );
    assertEquals( 2, frag2.getFragmentNumber() );
  }




  @Test
  public void testGetMessageId() {
    assertEquals( "1", frag1.getMessageId() );
    assertEquals( "1", frag2.getMessageId() );
  }




  @Test
  public void testGetRadioChannel() {
    assertEquals( "A", vdm.getRadioChannel() );
    assertEquals( "A", frag1.getRadioChannel() );
    assertEquals( "A", frag2.getRadioChannel() );
  }




  @Test
  public void testGetPayload() {
    assertEquals( "403OviQuMGCqWrRO9>E6fE700@GO", vdm.getPayload() );
    assertEquals( "88888888880", frag2.getPayload() );
  }




  @Test
  public void testGetFillBits() {
    assertEquals( 0, vdm.getFillBits() );
    assertEquals( 0, frag1.getFillBits() );
    assertEquals( 2, frag2.getFillBits() );
  }




  @Test
  public void testIsFragmented() {
    assertFalse( vdm.isFragmented() );
    assertTrue( frag1.isFragmented() );
    assertTrue( frag2.isFragmented() );
  }




  @Test
  public void testIsFirstFragment() {
    assertTrue( vdm.isFirstFragment() );
    assertTrue( frag1.isFirstFragment() );
    assertFalse( frag2.isFirstFragment() );
  }




  @Test
  public void testIsLastFragment() {
    assertTrue( vdm.isLastFragment() );
    assertFalse( frag1.isLastFragment() );
    assertTrue( frag2.isLastFragment() );
  }




  @Test
  public void testIsPartOfMessage() {
    assertFalse( vdm.isPartOfMessage( frag1 ) );
    assertFalse( vdm.isPartOfMessage( frag2 ) );
    assertFalse( frag1.isPartOfMessage( vdm ) );
    assertFalse( frag2.isPartOfMessage( vdm ) );
    assertTrue( frag1.isPartOfMessage( frag2 ) );
    assertFalse( frag2.isPartOfMessage( frag1 ) );
  }




  @Test
  public void testToStringWithAIS() {
    AISSentence vdm = new VDMSentenceImpl( EXAMPLE );
    AISSentence empty = new VDMSentenceImpl( TalkerId.AI );
    assertEquals( EXAMPLE, vdm.toString() );
    assertEquals( "!AIVDM,,,,,,*57", empty.toString() );
  }

}