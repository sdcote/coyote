package coyote.nmea.sentence;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import coyote.nmea.DataNotAvailableException;
import coyote.nmea.FOOSentence;
import coyote.nmea.FOOSentenceImpl;
import coyote.nmea.ParseException;
import coyote.nmea.Sentence;
import coyote.nmea.SentenceId;
import coyote.nmea.TalkerId;


/**
 * Tests the sentence parser base class.
 */
@Ignore
public class AbstractSentenceTest {

  public static final String VDO_EXAMPLE = "!AIVDO,1,1,,,13:r`R5P1orpG60JeHgRSj4l0000,0*56";
  public static final String VDM_EXAMPLE = "!AIVDM,1,1,,B,177KQJ5000G?tO`K>RA1wUbN0TKH,0*5C";

  private AbstractSentence instance;




  /**
   * setUp
   */
  @Before
  public void setUp() {
    instance = new UnknownSentence( GLLTest.EXAMPLE );
  }




  @Test
  public void testConstructorForEmptySentence() {
    final Sentence s = new UnknownSentence( "$GPGLL,,,,,*7C" );
    assertEquals( "$GPGLL,,,,,*7C", s.toString() );
  }




  @Test
  public void testConstructorWithAIVDM() {
    final Sentence s = new UnknownSentence( VDM_EXAMPLE );
    assertTrue( s.isValid() );
    assertFalse( s.isProprietary() );
    assertEquals( Sentence.ALTERNATIVE_BEGIN_CHAR, s.getBeginChar() );
    assertEquals( VDM_EXAMPLE, s.toString() );
  }




  @Test
  public void testConstructorWithAIVDO() {
    final Sentence s = new UnknownSentence( VDO_EXAMPLE );
    assertTrue( s.isValid() );
    assertFalse( s.isProprietary() );
    assertEquals( Sentence.ALTERNATIVE_BEGIN_CHAR, s.getBeginChar() );
    assertEquals( VDO_EXAMPLE, s.toString() );
  }




  @Test
  public void testConstructorWithCustomParser() {

    final String foo = "FOO";
    final SentenceParser sf = SentenceParser.getInstance();
    sf.registerSentence( foo, FOOSentenceImpl.class );

    final String fooSentence = "$GPFOO,B,A,R";
    final FOOSentence fp = new FOOSentenceImpl( fooSentence );
    final Sentence s = sf.createSentence( fooSentence );

    assertTrue( s instanceof UnknownSentence );
    assertTrue( s instanceof FOOSentenceImpl );
    assertEquals( foo, s.getSentenceId() );
    assertEquals( TalkerId.GP, s.getTalkerId() );
    assertEquals( s, fp );
  }




  @Test
  public void testConstructorWithInvalidSentence() {
    try {
      final String sent = "GPBOD,234.9,T,228.8,M,RUSKI,*1D";
      new UnknownSentence( sent );
      fail( "Did not throw exception" );
    } catch ( final IllegalArgumentException se ) {
      // pass
    } catch ( final Exception e ) {
      fail( e.getMessage() );
    }
  }




  //  @Test
  //  public void testConstructorWithNulls() {
  //    try {
  //      new UnknownSentence( (String)null, (String)null );
  //      fail( "Did not throw exception" );
  //    } catch ( IllegalArgumentException iae ) {
  //      // OK
  //    } catch ( Exception e ) {
  //      fail( e.getMessage() );
  //    }
  //  }

  //  @Test
  //  public void testConstructorWithNullType() {
  //
  //    try {
  //      new UnknownSentence( GLLTest.EXAMPLE, (String)null );
  //      fail( "Did not throw exception" );
  //    } catch ( IllegalArgumentException iae ) {
  //      // OK
  //    } catch ( Exception e ) {
  //      fail( e.getMessage() );
  //    }
  //  }

  @Test
  public void testConstructorWithUnsupportedTalker() {
    try {
      new UnknownSentence( "$XZGGA,VALID,BUT,TALKER,NOT,SUPPORTED" );
      fail( "Did not throw exception" );
    } catch ( final IllegalArgumentException se ) {
      assertTrue( se.getMessage().endsWith( ".TalkerId.XZ" ) );
    } catch ( final Exception e ) {
      fail( e.getMessage() );
    }
  }




  //  @Test
  //  public void testConstructorWithWrongType() {
  //    try {
  //      new UnknownSentence( GLLTest.EXAMPLE, SentenceId.GLL.toString() );
  //      fail( "Did not throw exception" );
  //    } catch ( IllegalArgumentException iae ) {
  //      // OK
  //    } catch ( Exception e ) {
  //      fail( e.getMessage() );
  //    }
  //  }

  @Test
  public void testEquals() {
    assertTrue( instance.equals( new UnknownSentence( GLLTest.EXAMPLE ) ) );
  }




  @Test
  public void testEqualsWithNonEqual() {
    assertFalse( instance.equals( new UnknownSentence( GLLTest.EXAMPLE ) ) );
  }




  @Test
  public void testEqualsWithNull() {
    assertFalse( instance.equals( null ) );
  }




  @Test
  public void testEqualsWithSelf() {
    assertTrue( instance.equals( instance ) );
  }




  @Test
  public void testGetCharValueWithEmptyFields() {
    final String nmea = "$GPGLL,,,,,,";
    final UnknownSentence s = new UnknownSentence( nmea );
    try {
      s.getCharValue( 3 );
      fail( "Did not throw exception" );
    } catch ( final DataNotAvailableException ex ) {
      // pass
    } catch ( final Exception ex ) {
      fail( ex.getMessage() );
    }
  }




  @Test
  public void testGetDoubleValueWithEmptyFields() {
    final String nmea = "$GPGLL,,,,,,";
    final UnknownSentence s = new UnknownSentence( nmea );
    try {
      s.getDoubleValue( 2 );
      fail( "Did not throw exception" );
    } catch ( final DataNotAvailableException ex ) {
      // pass
    } catch ( final Exception ex ) {
      fail( ex.getMessage() );
    }
  }




  @Test
  public void testGetDoubleValueWithInvalidValue() {
    final String nmea = "$GPGLL,a,b,c,d,e,f";
    final UnknownSentence s = new UnknownSentence( nmea );
    try {
      s.getDoubleValue( 2 );
      fail( "Did not throw exception" );
    } catch ( final ParseException ex ) {
      // pass
    } catch ( final Exception ex ) {
      fail( ex.getMessage() );
    }
  }




  @Test
  public void testGetSentenceId() {
    final SentenceId sid = SentenceId.valueOf( instance.getSentenceId() );
    assertEquals( SentenceId.RMC, sid );
  }




  @Test
  public void testGetStringValue() {
    final String nmea = "$GPGLL,6011.552,N,02501.941,E,120045,A";
    final UnknownSentence s = new UnknownSentence( nmea );
    final String data = "6011.552,N,02501.941,E,120045,A";
    final String[] expected = data.split( ",", -1 );

    for ( int i = 0; i < expected.length; i++ ) {
      assertEquals( expected[i], s.getStringValue( i ) );
    }
  }




  @Test
  public void testGetStringValueWithEmptyFields() {
    final String nmea = "$GPGLL,,,,,,";
    final UnknownSentence s = new UnknownSentence( nmea );
    try {
      s.getStringValue( 2 );
      fail( "Did not throw exception" );
    } catch ( final DataNotAvailableException ex ) {
      // pass
    } catch ( final Exception ex ) {
      fail( ex.getMessage() );
    }
  }




  @Test
  public void testGetStringValueWithIndexGreaterThanAllowed() {
    try {
      instance.getStringValue( instance.getFieldCount() );
      fail( "Did not throw IndexOutOfBoundsException" );
    } catch ( final IndexOutOfBoundsException e ) {
      // pass
    } catch ( final Exception e ) {
      fail( "Unexpected exception was thrown" );
    }
  }




  @Test
  public void testGetStringValueWithNegativeIndex() {

    try {
      instance.getStringValue( -1 );
      fail( "Did not throw IndexOutOfBoundsException" );
    } catch ( final IndexOutOfBoundsException e ) {
      // pass
    } catch ( final Exception e ) {
      fail( "Unexpected exception was thrown" );
    }
  }




  @Test
  public void testGetStringValueWithValidIndex() {
    try {
      String val = instance.getStringValue( 0 );
      assertEquals( "120044.567", val );
      val = instance.getStringValue( instance.getFieldCount() - 1 );
      assertEquals( "A", val );
    } catch ( final IndexOutOfBoundsException e ) {
      fail( "Unexpected IndexOutOfBoundsException" );
    } catch ( final Exception e ) {
      fail( "Unexpected exception was thrown" );
    }
  }




  @Test
  public void testGetTalkerId() {
    assertEquals( TalkerId.GP, instance.getTalkerId() );
  }




  public void testIsProprietary() {
    assertFalse( instance.isProprietary() );
  }




  @Test
  public void testIsValid() {
    assertTrue( instance.isValid() );
    instance.setStringValue( 0, "\t" );
    assertFalse( instance.isValid() );
  }




  public void testSetBeginChar() {
    assertEquals( Sentence.BEGIN_CHAR, instance.getBeginChar() );
    instance.setBeginChar( Sentence.ALTERNATIVE_BEGIN_CHAR );
    assertEquals( Sentence.ALTERNATIVE_BEGIN_CHAR, instance.getBeginChar() );
  }




  @Test
  public void testSetDoubleValue() {
    final int field = 0;
    final double value = 123.456789;
    instance.setDoubleValue( field, value );
    assertEquals( String.valueOf( value ), instance.getStringValue( field ) );
  }




  @Test
  public void testSetDoubleValueWithPrecision() {

    instance.setDoubleValue( 0, 3.14, 0, 0 );
    assertEquals( "3", instance.getStringValue( 0 ) );

    instance.setDoubleValue( 0, 3.14, 2, 0 );
    assertEquals( "03", instance.getStringValue( 0 ) );

    instance.setDoubleValue( 0, 3.14, 1, 4 );
    assertEquals( "3.1400", instance.getStringValue( 0 ) );

    instance.setDoubleValue( 0, 678.910, 3, 3 );
    assertEquals( "678.910", instance.getStringValue( 0 ) );

    instance.setDoubleValue( 0, 123.456, 4, 1 );
    assertEquals( "0123.5", instance.getStringValue( 0 ) );

    instance.setDoubleValue( 0, 78.910, 1, 1 );
    assertEquals( "78.9", instance.getStringValue( 0 ) );

    instance.setDoubleValue( 0, 0.910, 0, 3 );
    assertEquals( ".910", instance.getStringValue( 0 ) );

    instance.setDoubleValue( 0, 0.910, 3, 2 );
    assertEquals( "000.91", instance.getStringValue( 0 ) );

    instance.setDoubleValue( 0, 0.910, 0, 2 );
    assertEquals( ".91", instance.getStringValue( 0 ) );
  }




  @Test
  public void testSetFieldCountHigher() {
    final UnknownSentence parser = new UnknownSentence( "$GPGGA,1,2,3,4" );
    parser.setFieldCount( 8 );
    assertEquals( 8, parser.getFieldCount() );
    assertTrue( parser.toString().startsWith( "$GPGGA,1,2,3,4,,,,*" ) );
  }




  @Test
  public void testSetFieldCountHigherByOne() {

    final int count = instance.getFieldCount() + 1;
    final int lastIndex = instance.getFieldCount() - 1;
    final String value = instance.getStringValue( lastIndex );

    instance.setFieldCount( count );
    assertEquals( count, instance.getFieldCount() );
    assertEquals( value, instance.getStringValue( lastIndex ) );
  }




  @Test
  public void testSetFieldCountLower() {
    final UnknownSentence parser = new UnknownSentence( "$GPGGA,1,2,3,4" );
    parser.setFieldCount( 2 );
    assertEquals( 2, parser.getFieldCount() );
    assertEquals( "1", parser.getStringValue( 0 ) );
    assertEquals( "2", parser.getStringValue( 1 ) );
    assertTrue( parser.toString().startsWith( "$GPGGA,1,2*" ) );
  }




  @Test
  public void testSetFieldCountLowerByOne() {

    final int count = instance.getFieldCount() - 1;
    final int lastIndex = instance.getFieldCount() - 2;
    final String value = instance.getStringValue( lastIndex );

    instance.setFieldCount( count );
    assertEquals( count, instance.getFieldCount() );
    assertEquals( value, instance.getStringValue( lastIndex ) );
  }




  @Test
  public void testSetIntValueWithLeading() {

    instance.setIntValue( 0, 0, 0 );
    assertEquals( "0", instance.getStringValue( 0 ) );

    instance.setIntValue( 0, 0, 1 );
    assertEquals( "0", instance.getStringValue( 0 ) );

    instance.setIntValue( 0, 1, 2 );
    assertEquals( "01", instance.getStringValue( 0 ) );

    instance.setIntValue( 0, 1, 3 );
    assertEquals( "001", instance.getStringValue( 0 ) );

    instance.setIntValue( 0, 12, 1 );
    assertEquals( "12", instance.getStringValue( 0 ) );

    instance.setIntValue( 0, 12, 2 );
    assertEquals( "12", instance.getStringValue( 0 ) );

    instance.setIntValue( 0, 12, 3 );
    assertEquals( "012", instance.getStringValue( 0 ) );

    instance.setIntValue( 0, -1, 3 );
    assertEquals( "-01", instance.getStringValue( 0 ) );
  }




  @Test
  public void testSetStringValuesReplaceAll() {
    final UnknownSentence parser = new UnknownSentence( "$GPGGA,1,2,3,4" );
    final String[] values = { "5", "6", "7" };
    parser.setStringValues( 0, values );
    assertEquals( 3, parser.getFieldCount() );
    assertEquals( "5", parser.getStringValue( 0 ) );
    assertEquals( "6", parser.getStringValue( 1 ) );
    assertEquals( "7", parser.getStringValue( 2 ) );
  }




  @Test
  public void testSetStringValuesReplaceTail() {
    final UnknownSentence parser = new UnknownSentence( "$GPGGA,1,2,3,4" );
    final String[] values = { "5", "6", "7" };
    parser.setStringValues( 1, values );
    assertEquals( 4, parser.getFieldCount() );
    assertEquals( "1", parser.getStringValue( 0 ) );
    assertEquals( "5", parser.getStringValue( 1 ) );
    assertEquals( "6", parser.getStringValue( 2 ) );
    assertEquals( "7", parser.getStringValue( 3 ) );
  }




  @Test
  public void testToSentenceWithMaxLength() {
    final int max = instance.toString().length() + 1;
    assertEquals( GLLTest.EXAMPLE, instance.toSentence( max ) );
  }




  @Test
  public void testToSentenceWithMaxLengthExceeded() {
    try {
      final int max = instance.toString().length() - 1;
      assertEquals( GLLTest.EXAMPLE, instance.toSentence( max ) );
      fail( "didn't throw exception" );
    } catch ( final Exception e ) {
      // pass
    }
  }




  @Test
  public void testToSentenceWithMaxLengthOnLimit() {
    final int max = instance.toString().length();
    assertEquals( GLLTest.EXAMPLE, instance.toSentence( max ) );
  }




  @Test
  public void testToString() {
    assertEquals( GLLTest.EXAMPLE, instance.toString() );
    assertEquals( instance.toString(), instance.toSentence() );
  }

}
