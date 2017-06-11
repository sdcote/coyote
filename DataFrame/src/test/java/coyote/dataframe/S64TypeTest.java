package coyote.dataframe;

import static org.junit.Assert.assertTrue;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;


/**
 * 64-bit value in the range of -9,223,372,036,854,775,808 to 9,223,372,036,854,775,807
 */
public class S64TypeTest
{

  /** The data type under test. */
  static S64Type datatype = null;




  /**
   * @throws java.lang.Exception
   */
  @BeforeClass
  public static void setUpBeforeClass() throws Exception
  {
    datatype = new S64Type();
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
    //9223372036854775807 = 0x7FFFFFFFFFFFFFFF = 01111111 11111111 11111111 11111111 11111111 11111111 11111111 11111111
    long value = 9223372036854775807L;
    assertTrue( datatype.checkType( value ) );

    // This overflows in Java to -9223372036854775808
    value++;
    assertTrue( datatype.checkType( value ) );

    value = 0;
    assertTrue( datatype.checkType( value ) );

    value--; // -1
    assertTrue( datatype.checkType( value ) );

    //-9223372036854775808 = 0x8000000000000000 = 10000000 00000000 00000000 00000000 00000000 00000000 00000000 00000000
    value = -9223372036854775808L;
    assertTrue( datatype.checkType( value ) );

    // This overflows in Java to 9223372036854775807
    value--;
    assertTrue( datatype.checkType( value ) );

  }




  @Test
  public void testDecode()
  {
    Object obj = null;
    byte[] data = new byte[8];
    //-9223372036854775808 = 0x8000000000000000 = 10000000 00000000 00000000 00000000 00000000 00000000 00000000 00000000
    data[0] = (byte)128;
    data[1] = 0;
    data[2] = 0;
    data[3] = 0;
    data[4] = 0;
    data[5] = 0;
    data[6] = 0;
    data[7] = 0;
    obj = datatype.decode( data );
    assertTrue( obj instanceof java.lang.Long );
    assertTrue( ( (Long)obj ).longValue() == -9223372036854775808L );

    //9223372036854775807 = 0x7FFFFFFFFFFFFFFF = 01111111 11111111 11111111 11111111 11111111 11111111 11111111 11111111
    data[0] = (byte)127;
    data[1] = -1;
    data[2] = -1;
    data[3] = -1;
    data[4] = -1;
    data[5] = -1;
    data[6] = -1;
    data[7] = -1;
    obj = datatype.decode( data );
    assertTrue( obj instanceof java.lang.Long );
    assertTrue( ( (Long)obj ).longValue() == 9223372036854775807L );

    //0 = 0x0000000000000000 = 00000000 00000000 00000000 00000000 00000000 00000000 00000000 00000000
    data[0] = 0;
    data[1] = 0;
    data[2] = 0;
    data[3] = 0;
    data[4] = 0;
    data[5] = 0;
    data[6] = 0;
    data[7] = 0;
    obj = datatype.decode( data );
    assertTrue( obj instanceof java.lang.Long );
    assertTrue( ( (Long)obj ).longValue() == 0 );

    //-1 = 0xFFFFFFFFFFFFFFFF = 11111111 11111111 11111111 11111111 11111111 11111111 11111111 11111111
    data[0] = -1;
    data[1] = -1;
    data[2] = -1;
    data[3] = -1;
    data[4] = -1;
    data[5] = -1;
    data[6] = -1;
    data[7] = -1;
    obj = datatype.decode( data );
    assertTrue( obj instanceof java.lang.Long );
    assertTrue( ( (Long)obj ).longValue() == -1 );

  }




  @Test
  public void testEncode()
  {
    //9223372036854775807 = 0x7FFFFFFFFFFFFFFF = 01111111 11111111 11111111 11111111 11111111 11111111 11111111 11111111
    long value = 9223372036854775807L;
    byte[] data = datatype.encode( value );
    assertTrue( data.length == 8 );
    assertTrue( data[0] == 127 );
    assertTrue( data[1] == -1 );
    assertTrue( data[2] == -1 );
    assertTrue( data[3] == -1 );
    assertTrue( data[4] == -1 );
    assertTrue( data[5] == -1 );
    assertTrue( data[6] == -1 );
    assertTrue( data[7] == -1 );

    //-9223372036854775808 = 0x8000000000000000 = 10000000 00000000 00000000 00000000 00000000 00000000 00000000 00000000
    value++;
    data = datatype.encode( value );
    assertTrue( data.length == 8 );
    assertTrue( data[0] == -128 );
    assertTrue( data[1] == 0 );
    assertTrue( data[2] == 0 );
    assertTrue( data[3] == 0 );
    assertTrue( data[4] == 0 );
    assertTrue( data[5] == 0 );
    assertTrue( data[6] == 0 );
    assertTrue( data[7] == 0 );

    //0 = 0x0000000000000000 = 00000000 00000000 00000000 00000000 00000000 00000000 00000000 00000000
    value = 0;
    data = datatype.encode( value );
    assertTrue( data.length == 8 );
    assertTrue( data[0] == 0 );
    assertTrue( data[1] == 0 );
    assertTrue( data[2] == 0 );
    assertTrue( data[3] == 0 );
    assertTrue( data[4] == 0 );
    assertTrue( data[5] == 0 );
    assertTrue( data[6] == 0 );
    assertTrue( data[7] == 0 );

    //-1 = 0xFFFFFFFFFFFFFFFF = 11111111 11111111 11111111 11111111 11111111 11111111 11111111 11111111
    value--;
    data = datatype.encode( value );
    assertTrue( data.length == 8 );
    assertTrue( data[0] == -1 );
    assertTrue( data[1] == -1 );
    assertTrue( data[2] == -1 );
    assertTrue( data[3] == -1 );
    assertTrue( data[4] == -1 );
    assertTrue( data[5] == -1 );
    assertTrue( data[6] == -1 );
    assertTrue( data[7] == -1 );

  }




  @Test
  public void testGetTypeName()
  {
    assertTrue( datatype.getTypeName().equals( "S64" ) );
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
