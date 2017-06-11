/**
 * 
 */
package coyote.dataframe;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;


/**
 * @author Steve.Cote
 *
 */
public class FrameTypeTest {
  /** The data type under test. */
  static FrameType datatype = null;




  /**
   * @throws java.lang.Exception
   */
  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    datatype = new FrameType();
  }




  /**
   * @throws java.lang.Exception
   */
  @AfterClass
  public static void tearDownAfterClass() throws Exception {
    datatype = null;
  }




  /**
   * Test method for {@link coyote.dataframe.FrameType#checkType(java.lang.Object)}.
   */
  @Test
  public void testCheckType() {
    DataFrame frame = new DataFrame();
    assertTrue( datatype.checkType( frame ) );
  }




  /**
   * Test method for {@link coyote.dataframe.FrameType#decode(byte[])}.
   */
  @Test
  public void testDecode() {
    byte[] data = new byte[14];
    data[0] = 4; // name of the field is 4 bytes long
    data[1] = 116; // t
    data[2] = 101; // e
    data[3] = 115; // s
    data[4] = 116; // t
    data[5] = 3; // data type code is 3 - String
    data[6] = 0; // first byte of unsigned integer for length
    data[7] = 0; // second byte of unsigned integer for length
    data[8] = 0; // third byte of unsigned integer for length
    data[9] = 4; // fourth byte of unsigned integer for length
    data[10] = 97; // a
    data[11] = 98; // b
    data[12] = 99; // c
    data[13] = 100; // c
    Object value = datatype.decode( data );
    assertTrue( value instanceof DataFrame );
    DataFrame frame = (DataFrame)value;
    assertTrue( frame.contains( "test" ) );
    assertTrue( frame.getFieldCount() == 1 );
    DataField field = frame.getField( 0 );
    assertNotNull( field );
    assertTrue( field.type == 3 );
    Object fieldvalue = field.getObjectValue();
    assertNotNull( fieldvalue );
    assertTrue( fieldvalue instanceof String );
    String stringvalue = (String)fieldvalue;
    assertTrue( stringvalue.equals( "abcd" ) );
  }




  /**
   * Test method for {@link coyote.dataframe.FrameType#encode(java.lang.Object)}.
   */
  @Test
  public void testEncode() {
    // empty frame should yield no bytes
    DataFrame data = new DataFrame();
    byte[] value = datatype.encode( data );
    assertTrue( value.length == 0 );

    //other tests actually depend on the encoding of each field and their field types.

    // Add a string with the name of test
    data.add( "test", "abc" );
    value = datatype.encode( data );
    assertTrue( value.length == 13 );
    assertTrue( value[0] == 4 ); // name of the field is 4 bytes long
    assertTrue( value[1] == 116 ); // t
    assertTrue( value[2] == 101 ); // e
    assertTrue( value[3] == 115 ); // s
    assertTrue( value[4] == 116 ); // t
    assertTrue( value[5] == 3 ); // data type code is 3 - String
    assertTrue( value[6] == 0 ); // first byte of unsigned integer for length
    assertTrue( value[7] == 0 ); // second byte of unsigned integer for length
    assertTrue( value[8] == 0 ); // third byte of unsigned integer for length
    assertTrue( value[9] == 3 ); // fourth byte of unsigned integer for length
    assertTrue( value[10] == 97 ); // a
    assertTrue( value[11] == 98 ); // b
    assertTrue( value[12] == 99 ); // c
  }




  /**
   * Test method for {@link coyote.dataframe.FrameType#isNumeric()}.
   */
  @Test
  public void testIsNumeric() {
    assertFalse( datatype.isNumeric() );
  }




  /**
   * Test method for {@link coyote.dataframe.FrameType#getSize()}.
   */
  @Test
  public void testGetSize() {
    assertTrue( datatype.getSize() == -1 );
  }




  /**
   * Test method for {@link coyote.dataframe.FrameType#getTypeName()}.
   */
  @Test
  public void testGetTypeName() {
    assertTrue( datatype.getTypeName().equals( "FRAME" ) );
  }

}
