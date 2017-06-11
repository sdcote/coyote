package coyote.dataframe;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;


/**
 * 32-bit value in the range of 0 to 4,294,967,295
 */
public class U32TypeTest
{

  /** The data type under test. */
  static U32Type datatype = null;




  /**
   * @throws java.lang.Exception
   */
  @BeforeClass
  public static void setUpBeforeClass() throws Exception
  {
    datatype = new U32Type();
  }




  /**
   * @throws java.lang.Exception
   */
  @AfterClass
  public static void tearDownAfterClass() throws Exception
  {
    datatype = null;
  }




  /**
   * These are really not necessary as Java handles all range checks
   */
  @Test
  public void testCheckType()
  {
    // Maximum size for this type
    long value = 4294967295L;
    assertTrue( datatype.checkType( value ) );

    // Too big of an integer for this type
    value++;
    assertFalse( datatype.checkType( value ) );

    // Minimum size for this type
    value = 0;
    assertTrue( datatype.checkType( value ) );

    // No negative numbers
    value--; // -1
    assertFalse( datatype.checkType( value ) );

  }




  @Test
  public void testDecode()
  {
    byte[] data = new byte[4];
    data[0] = 0;
    data[1] = 0;
    data[2] = 0;
    data[3] = 0;

    // Minimum size for this type
    //0 = 0x00000000 = 00000000 00000000 00000000 00000000
    Object obj = datatype.decode( data );
    assertTrue( obj instanceof java.lang.Long );
    assertTrue( ( (Long)obj ).longValue() == 0 );

    // Maximum value for this type
    //4294967295 = 0xFFFFFFFF = 11111111 11111111 11111111 11111111
    data[0] = -1;
    data[1] = -1;
    data[2] = -1;
    data[3] = -1;
    obj = datatype.decode( data );
    assertTrue( obj instanceof java.lang.Long );
    assertTrue( ( (Long)obj ).longValue() == 4294967295L );

  }




  @Test
  public void testEncode()
  {
    // 4294967295 =0xFFFFFFFF = 11111111 11111111 11111111 11111111
    long value = 4294967295L;
    byte[] data = datatype.encode( value );
    assertTrue( data.length == 4 );
    assertTrue( data[0] == -1 );
    assertTrue( data[1] == -1 );
    assertTrue( data[2] == -1 );
    assertTrue( data[3] == -1 );

    // Overflow to 0
    value++;
    data = datatype.encode( value );
    assertTrue( data.length == 4 );
    assertTrue( data[0] == 0 );
    assertTrue( data[1] == 0 );
    assertTrue( data[2] == 0 );
    assertTrue( data[3] == 0 );

    //0 = 0x00000000 = 00000000 00000000 00000000 00000000
    value = 0;
    data = datatype.encode( value );
    assertTrue( data.length == 4 );
    assertTrue( data[0] == 0 );
    assertTrue( data[1] == 0 );
    assertTrue( data[2] == 0 );
    assertTrue( data[3] == 0 );

    // underflow to 4294967295
    //4294967295 = 0xFFFFFFFF = 11111111 11111111 11111111 11111111
    value--;
    data = datatype.encode( value );
    assertTrue( data.length == 4 );
    assertTrue( data[0] == -1 );
    assertTrue( data[1] == -1 );
    assertTrue( data[2] == -1 );
    assertTrue( data[3] == -1 );

  }




  @Test
  public void testGetTypeName()
  {
    assertTrue( datatype.getTypeName().equals( "U32" ) );
  }




  @Test
  public void testIsNumeric()
  {
    assertTrue( datatype.isNumeric() );
  }




  @Test
  public void testGetSize()
  {
    assertTrue( datatype.getSize() == 4 );
  }

}
