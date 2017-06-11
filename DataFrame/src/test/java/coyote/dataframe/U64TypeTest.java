package coyote.dataframe;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.math.BigInteger;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;


/**
 * 64-bit value in the range of 0 to 18,446,744,073,709,551,615
 */
public class U64TypeTest
{

  /** The data type under test. */
  static U64Type datatype = null;

  static BigInteger MIN_VALUE = null;
  static BigInteger MAX_VALUE = null;




  /**
   * @throws java.lang.Exception
   */
  @BeforeClass
  public static void setUpBeforeClass() throws Exception
  {
    datatype = new U64Type();

    byte[] input = { (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff };
    MAX_VALUE = new BigInteger( 1, input );

    byte[] input2 = { (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00 };
    MIN_VALUE = new BigInteger( 1, input2 );
  }




  /**
   * @throws java.lang.Exception
   */
  @AfterClass
  public static void tearDownAfterClass() throws Exception
  {
    datatype = null;
    MIN_VALUE = null;
    MAX_VALUE = null;
  }




  @Test
  public void testCheckType()
  {
    // Maximum size for this type = 18446744073709551615;
    assertTrue( datatype.checkType( MAX_VALUE ) );

    // Try to exceed the maximum value
    BigInteger overflow = MAX_VALUE.add( BigInteger.valueOf( 1 ) );
    assertFalse( datatype.checkType( overflow ) );

    // Minimum size for this type
    assertTrue( datatype.checkType( MIN_VALUE ) );

    // No negative numbers
    assertFalse( datatype.checkType( -1 ) );
    BigInteger negative = BigInteger.valueOf( -1 );
    assertFalse( datatype.checkType( negative ) );

  }




  @Test
  public void testDecode()
  {
    // Minimum value for this type
    byte[] data = new byte[8];
    data[0] = 0;
    data[1] = 0;
    data[2] = 0;
    data[3] = 0;
    data[4] = 0;
    data[5] = 0;
    data[6] = 0;
    data[7] = 0;
    Object obj = datatype.decode( data );
    assertTrue( obj instanceof java.math.BigInteger );
    assertTrue( ( (BigInteger)obj ).compareTo( MIN_VALUE ) == 0 );

    // Maximum value for this type
    data[0] = -1;
    data[1] = -1;
    data[2] = -1;
    data[3] = -1;
    data[4] = -1;
    data[5] = -1;
    data[6] = -1;
    data[7] = -1;
    obj = datatype.decode( data );
    assertTrue( obj instanceof java.math.BigInteger );
    assertTrue( ( (BigInteger)obj ).compareTo( MAX_VALUE ) == 0 );

    // Try some random small number
    data[0] = 0;
    data[1] = 0;
    data[2] = 0;
    data[3] = 0;
    data[4] = 0;
    data[5] = 0;
    data[6] = 14;
    data[7] = 73;
    obj = datatype.decode( data );
    assertTrue( obj instanceof java.math.BigInteger );
    assertTrue( ( (BigInteger)obj ).compareTo( BigInteger.valueOf( 3657 ) ) == 0 );
  }




  @Test
  public void testEncode()
  {
    byte[] data = datatype.encode( MAX_VALUE );
    assertTrue( data.length == 8 );
    assertTrue( data[0] == -1 );
    assertTrue( data[1] == -1 );
    assertTrue( data[2] == -1 );
    assertTrue( data[3] == -1 );
    assertTrue( data[4] == -1 );
    assertTrue( data[5] == -1 );
    assertTrue( data[6] == -1 );
    assertTrue( data[7] == -1 );

    data = datatype.encode( MIN_VALUE );
    assertTrue( data.length == 8 );
    assertTrue( data[0] == 0 );
    assertTrue( data[1] == 0 );
    assertTrue( data[2] == 0 );
    assertTrue( data[3] == 0 );
    assertTrue( data[4] == 0 );
    assertTrue( data[5] == 0 );
    assertTrue( data[6] == 0 );
    assertTrue( data[7] == 0 );

    data = datatype.encode( BigInteger.valueOf( 3657 ) );
    assertTrue( data.length == 8 );
    assertTrue( data[0] == 0 );
    assertTrue( data[1] == 0 );
    assertTrue( data[2] == 0 );
    assertTrue( data[3] == 0 );
    assertTrue( data[4] == 0 );
    assertTrue( data[5] == 0 );
    assertTrue( data[6] == 14 );
    assertTrue( data[7] == 73 );
  }




  @Test
  public void testGetTypeName()
  {
    assertTrue( datatype.getTypeName().equals( "U64" ) );
  }




  @Test
  public void testIsNumeric()
  {
    assertTrue( datatype.isNumeric() );
  }




  @Test
  public void testGetSize()
  {
    assertTrue( datatype.getSize() == 8 );
  }

}
