package coyote.nmea;

//import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import coyote.nmea.sentence.AbstractSentence;
import coyote.nmea.sentence.BODSentence;
import coyote.nmea.sentence.BODTest;
import coyote.nmea.sentence.SentenceParser;
import coyote.nmea.sentence.UnknownSentence;


/**
 * 
 */
public class SentenceParserTest {

  private final SentenceParser instance = SentenceParser.getInstance();




  @Before
  public void setUp() throws Exception {
    instance.reset();
  }




  @Test
  public void testCreateCustomParser() {

    try {
      instance.registerSentence( "FOO", FOOSentenceImpl.class );
      assertTrue( instance.hasSentence( "FOO" ) );
    } catch ( final Exception e ) {
      fail( "parser registering failed" );
    }

    Sentence s = null;
    try {
      s = instance.createSentence( "$IIFOO,aa,bb,cc" );
    } catch ( final Exception e ) {
      fail( "sentence parsing failed" );
    }

    assertNotNull( s );
    assertTrue( s instanceof Sentence );
    assertTrue( s instanceof AbstractSentence );
    assertTrue( s instanceof FOOSentenceImpl );
    assertEquals( TalkerId.II, s.getTalkerId() );
    assertEquals( "FOO", s.getSentenceId() );
    assertEquals( "aa", ( (FOOSentence)s ).getValueA() );
    assertEquals( "bb", ( (FOOSentence)s ).getValueB() );
    assertEquals( "cc", ( (FOOSentence)s ).getValueC() );
  }




  @Test
  public void testCreateEmptyCustomParser() {

    try {
      instance.registerSentence( "FOO", FOOSentenceImpl.class );
      assertTrue( instance.hasSentence( "FOO" ) );
    } catch ( final Exception e ) {
      fail( "parser registering failed" );
    }

    final Sentence s = instance.createSentence( TalkerId.II, "FOO" );
    assertNotNull( s );
    assertTrue( s instanceof Sentence );
    assertTrue( s instanceof AbstractSentence );
    assertTrue( s instanceof FOOSentenceImpl );
    assertEquals( "FOO", s.getSentenceId() );
  }




  @Test
  @Ignore
  // won't pass until all IDs are implemented
  public void testCreateEmptyParserWithSentenceId() {
    for ( final SentenceId id : SentenceId.values() ) {
      final Sentence s = instance.createSentence( TalkerId.II, id );
      assertNotNull( s );
      assertTrue( s instanceof Sentence );
      assertTrue( s instanceof AbstractSentence );
      assertEquals( TalkerId.II, s.getTalkerId() );
      assertEquals( id.name(), s.getSentenceId() );
    }
  }




  @Test
  @Ignore
  // won't pass until all IDs are implemented
  public void testCreateEmptyParserWithSentenceIdStr() {
    for ( final SentenceId id : SentenceId.values() ) {
      final Sentence s = instance.createSentence( TalkerId.II, id.name() );
      assertNotNull( s );
      assertTrue( s instanceof Sentence );
      assertTrue( s instanceof AbstractSentence );
    }
  }




  @Test
  public void testCreateParser() {
    final Sentence bod = instance.createSentence( BODTest.EXAMPLE );
    assertNotNull( bod );
    assertTrue( bod instanceof Sentence );
    assertTrue( bod instanceof BODSentence );
    assertEquals( BODTest.EXAMPLE, bod.toSentence() );
  }




  @Test
  public void testCreateParserWithEmptyString() {
    try {
      instance.createSentence( "" );
      fail( "Did not throw exception" );
    } catch ( final IllegalArgumentException e ) {
      // pass
    }
  }




  @Test
  public void testCreateParserWithNull() {
    try {
      instance.createSentence( null );
      fail( "Did not throw exception" );
    } catch ( final IllegalArgumentException e ) {
      // pass
    }
  }




  @Test
  public void testCreateParserWithRandom() {
    try {
      instance.createSentence( "asdqas,dwersa,dsdfas,das" );
      fail( "Did not throw exception" );
    } catch ( final IllegalArgumentException e ) {
      // pass
    }
  }




  @Test
  public void testCreateParserWithUnregistered() {
    try {
      instance.createSentence( "$GPXYZ,1,2,3,4,5,6,7,8" );
    } catch ( final IllegalArgumentException e ) {
      fail( "Did not create an Unknown sentance from valid data" );
    } catch ( final UnsupportedSentenceException e ) {
      Sentence sentence = ( (UnsupportedSentenceException)e ).getSentence();
      assertNotNull( sentence );
      assertTrue( sentence instanceof UnknownSentence );
    }
  }




  @Test
  public void testGetInstance() {
    assertNotNull( instance );
    assertTrue( instance == SentenceParser.getInstance() );
    assertEquals( instance, SentenceParser.getInstance() );
  }




  @Test
  public void testHasParser() {
    assertTrue( instance.hasSentence( "GLL" ) );
    assertFalse( instance.hasSentence( "ABC" ) );
  }




  @Test
  @Ignore
  // won't pass until all IDs are implemented
  public void testListParsers() {
    final List<String> types = instance.listSentences();
    assertEquals( SentenceId.values().length, types.size() );
    for ( final SentenceId id : SentenceId.values() ) {
      assertTrue( types.contains( id.name() ) );
    }
  }




  @Test
  public void testRegisterInvalidParser() {
    try {
      instance.registerSentence( "BAR", BARSentenceImpl.class );
      fail( "did not throw exception" );
    } catch ( final IllegalArgumentException iae ) {
      // pass
    } catch ( final Exception e ) {
      fail( e.getMessage() );
    }
  }




  @Test
  public void testRegisterParserWithAlternativeBeginChar() {

    try {
      instance.registerSentence( "VDM", VDMSentenceImpl.class );
      assertTrue( instance.hasSentence( "VDM" ) );
    } catch ( final Exception e ) {
      fail( "parser registering failed" );
    }

    final Sentence s = instance.createSentence( "!AIVDM,1,2,3" );
    assertNotNull( s );
    assertTrue( s instanceof Sentence );
    assertTrue( s instanceof AbstractSentence );
    assertTrue( s instanceof VDMSentenceImpl );
    instance.unregisterSentence( VDMSentenceImpl.class );
    assertFalse( instance.hasSentence( "VDM" ) );
  }




  @Test
  @Ignore
  public void testSupportedTypesRegistered() {
    for ( final SentenceId id : SentenceId.values() ) {
      final String msg = "Parser not registered: " + id;
      assertTrue( msg, instance.hasSentence( id.toString() ) );
    }
  }




  @Test
  public void testUnregisterParser() {
    instance.registerSentence( "FOO", FOOSentenceImpl.class );
    assertTrue( instance.hasSentence( "FOO" ) );
    instance.unregisterSentence( FOOSentenceImpl.class );
    assertFalse( instance.hasSentence( "FOO" ) );
  }

}
