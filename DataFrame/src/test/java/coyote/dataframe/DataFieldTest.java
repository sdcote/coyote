/*
 * 
 */
package coyote.dataframe;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;

import org.junit.Test;


/**
 * 
 */
public class DataFieldTest {

  @Test
  public void testConstructor() {
    String nulltag = null;
    Object nullval = null;

    DataField field = new DataField( "" );
    field.getValue();

    field = new DataField( nullval );
    field = new DataField( new Long( 0 ) );

    field = new DataField( new String(), new String() );
    new DataField( nulltag, nullval );

    field = new DataField( 0l );
    field = new DataField( new String(), 0l );
    field = new DataField( nulltag, 0l );

    field = new DataField( 0 );
    field = new DataField( new String(), 0 );
    field = new DataField( nulltag, 0 );

    field = new DataField( (short)0 );
    field = new DataField( new String(), (short)0 );
    field = new DataField( nulltag, (short)0 );

    field = new DataField( new byte[0] );
    field = new DataField( new String(), new byte[0] );
    field = new DataField( nulltag, new byte[0] );

    field = new DataField( (byte[])null );
    field = new DataField( nulltag, (byte[])null );

    field = new DataField( 0f );
    field = new DataField( new String(), 0f );
    field = new DataField( nulltag, 0f );

    field = new DataField( 0d );
    field = new DataField( new String(), 0d );
    field = new DataField( nulltag, 0d );

    field = new DataField( true );
    field = new DataField( new String(), true );
    field = new DataField( nulltag, true );

    field = new DataField( new Date() );
    field = new DataField( new String(), new Date() );
    field = new DataField( null, new Date() );

    try {
      field = new DataField( new URI( "" ) );
    } catch ( IllegalArgumentException e ) {
      fail( e.getMessage() );
    } catch ( URISyntaxException e ) {
      fail( e.getMessage() );
    }
    try {
      field = new DataField( new String(), new URI( "" ) );
    } catch ( IllegalArgumentException e ) {
      fail( e.getMessage() );
    } catch ( URISyntaxException e ) {
      fail( e.getMessage() );
    }

    try {
      field = new DataField( nulltag, new URI( "" ) );
    } catch ( IllegalArgumentException e ) {
      fail( e.getMessage() );
    } catch ( URISyntaxException e ) {
      fail( e.getMessage() );
    }

    //field = new DataField(null); //ambiguous; DataInputStream or Object?

  }




  /**
   * Test method for {@link coyote.dataframe.DataField#DataField(boolean)}.
   */
  @Test
  public void testDataFieldBoolean() {
    DataField field = new DataField( true );
    byte[] data = field.getBytes();
    //System.out.println(ByteUtil.dump( data ));
    assertTrue( data.length == 3 );
    assertTrue( data[0] == 0 );
    assertTrue( data[1] == 14 );
    assertTrue( data[2] == 1 );

  }




  /**
   * Test method for {@link coyote.dataframe.DataField#DataField(java.lang.String, boolean)}.
   */
  @Test
  public void testDataFieldStringBoolean() {
    DataField field = new DataField( "Test", true );
    byte[] data = field.getBytes();
    //System.out.println(ByteUtil.dump( data ));
    assertTrue( data.length == 7 );
    assertTrue( data[0] == 4 );
    assertTrue( data[1] == 84 );
    assertTrue( data[2] == 101 );
    assertTrue( data[3] == 115 );
    assertTrue( data[4] == 116 );
    assertTrue( data[5] == 14 );
    assertTrue( data[6] == 1 );

    field = new DataField( "Test", false );
    data = field.getBytes();
    //System.out.println(ByteUtil.dump( data ));
    assertTrue( data.length == 7 );
    assertTrue( data[0] == 4 );
    assertTrue( data[1] == 84 );
    assertTrue( data[2] == 101 );
    assertTrue( data[3] == 115 );
    assertTrue( data[4] == 116 );
    assertTrue( data[5] == 14 );
    assertTrue( data[6] == 0 );
  }




  /**
   * Test method for {@link coyote.dataframe.DataField#clone()}.
   */
  @Test
  public void testClone() {
    DataField original = new DataField( "Test", 17345 );

    Object copy = original.clone();

    assertNotNull( copy );
    assertTrue( copy instanceof DataField );
    DataField field = (DataField)copy;
    assertTrue( "Test".equals( field.name ) );
    assertTrue( field.type == 7 );
    Object obj = field.getObjectValue();
    assertNotNull( obj );
    assertTrue( obj instanceof Integer );
    assertTrue( ( (Integer)obj ).intValue() == 17345 );
  }




  /**
   * Test method for {@link coyote.dataframe.DataField#isNumeric()}.
   */
  @Test
  public void testIsNumeric() {
    DataField subject = new DataField( "Test", 32767 );
    assertTrue( subject.isNumeric() );
    subject = new DataField( "Test", "32767" );
    assertFalse( subject.isNumeric() );
  }




  /**
   * Test method for {@link coyote.dataframe.DataField#toString()}.
   */
  @Test
  public void testToString() {
    DataField subject = new DataField( "Test", 32767 );
    String text = subject.toString();
    assertNotNull( text );
    assertTrue( text.length() == 48 );

    // Test truncation of long values
    subject = new DataField( "Test", "01234567890123456789012345678901234567890123456789" );
    text = subject.toString();
    assertNotNull( text );
    assertTrue( text.length() < 170 );
  }

}
