package coyote.dataframe;

import static org.junit.Assert.assertTrue;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;


/**
 * -2,147,483,648 to 2,147,483,647
 */
public class S32TypeTest
{

  /** The data type under test. */
  static S32Type datatype = null;




  /**
   * @throws java.lang.Exception
   */
  @BeforeClass
  public static void setUpBeforeClass() throws Exception
  {
    datatype = new S32Type();
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
    //2147483647 = 0x7FFFFFFF = 01111111 11111111 11111111 11111111
    int value = 2147483647;
    assertTrue( datatype.checkType( value ) );

    // This overflows in Java to -2147483648
    value++;
    assertTrue( datatype.checkType( value ) );

    value = 0;
    assertTrue( datatype.checkType( value ) );

    value--; // -1
    assertTrue( datatype.checkType( value ) );

    //-2147483648 = 0x80000000 = 10000000 00000000 00000000 00000000
    value = -2147483648;
    assertTrue( datatype.checkType( value ) );

    // This overflows in Java to 2147483647
    value--;
    assertTrue( datatype.checkType( value ) );

  }




  @Test
  public void testDecode()
  {
    Object obj = null;
    byte[] data = new byte[4];
    //-2147483648 = 0x80000000 = 10000000 00000000 00000000 00000000
    data[0] = (byte)128;
    data[1] = 0;
    data[2] = 0;
    data[3] = 0;

    obj = datatype.decode( data );
    assertTrue( obj instanceof java.lang.Integer );
    assertTrue( ( (Integer)obj ).intValue() == -2147483648 );

    //2147483647 = 0x7FFFFFFF = 01111111 11111111 11111111 11111111
    data[0] = (byte)127;
    data[1] = -1;
    data[2] = -1;
    data[3] = -1;
    obj = datatype.decode( data );
    assertTrue( obj instanceof java.lang.Integer );
    assertTrue( ( (Integer)obj ).intValue() == 2147483647 );

    //0 = 0x00000000 = 00000000 00000000 00000000 00000000
    data[0] = 0;
    data[1] = 0;
    data[2] = 0;
    data[3] = 0;
    obj = datatype.decode( data );
    assertTrue( obj instanceof java.lang.Integer );
    assertTrue( ( (Integer)obj ).intValue() == 0 );

    //-1 = 0xFFFFFFFF = 11111111 11111111 11111111 11111111
    data[0] = -1;
    data[1] = -1;
    data[2] = -1;
    data[3] = -1;
    obj = datatype.decode( data );
    assertTrue( obj instanceof java.lang.Integer );
    assertTrue( ( (Integer)obj ).intValue() == -1 );

  }




  /**
   * The test will look weird because Java only deals with signed numbers
   */
  @Test
  public void testEncode()
  {
    //2147483647 = 0x7FFFFFFF = 01111111 11111111 11111111 11111111
    int value = 2147483647;
    byte[] data = datatype.encode( value );
    assertTrue( data.length == 4 );
    assertTrue( data[0] == 127 );
    assertTrue( data[1] == -1 );
    assertTrue( data[2] == -1 );
    assertTrue( data[3] == -1 );

    // Overflow to -2147483648 = 0x80000000 = 10000000 00000000 00000000 00000000
    value++;
    data = datatype.encode( value );
    assertTrue( data.length == 4 );
    assertTrue( data[0] == -128 );
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

    //-1 = 0xFFFFFFFF = 11111111 11111111 11111111 11111111
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
    assertTrue( datatype.getTypeName().equals( "S32" ) );
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
