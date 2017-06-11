package coyote.dataframe;

import static org.junit.Assert.assertTrue;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;


/**
 * 32-bit floating point value in the range of +/-1.4013e-45 to +/-3.4028e+38.
 */
public class FloatTypeTest
{

  /** The data type under test. */
  static FloatType datatype = null;




  /**
   * @throws java.lang.Exception
   */
  @BeforeClass
  public static void setUpBeforeClass() throws Exception
  {
    datatype = new FloatType();
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
   * 
   */
  @Test
  public void testCheckType()
  {
    assertTrue( datatype.checkType( Float.MAX_VALUE ) );
    assertTrue( datatype.checkType( Float.MIN_VALUE ) );
    assertTrue( datatype.checkType( Float.MIN_NORMAL ) );
  }




  @Test
  public void testDecode()
  {
    Object obj = null;
    byte[] data = new byte[4];

    data[0] = (byte)127;
    data[1] = (byte)127;
    data[2] = (byte)-1;
    data[3] = (byte)-1;
    obj = datatype.decode( data );
    assertTrue( obj instanceof java.lang.Float );
    assertTrue( ( (Float)obj ).floatValue() == Float.MAX_VALUE );

    data[0] = 0;
    data[1] = 0;
    data[2] = 0;
    data[3] = (byte)1;
    obj = datatype.decode( data );
    assertTrue( obj instanceof java.lang.Float );
    assertTrue( ( (Float)obj ).floatValue() == Float.MIN_VALUE );

    data[0] = 0;
    data[1] = 0;
    data[2] = 0;
    data[3] = 0;
    obj = datatype.decode( data );
    assertTrue( obj instanceof java.lang.Float );
    assertTrue( ( (Float)obj ).floatValue() == 0 );

    data[0] = (byte)-65;
    data[1] = (byte)-128;
    data[2] = 0;
    data[3] = 0;
    obj = datatype.decode( data );
    assertTrue( obj instanceof java.lang.Float );
    assertTrue( ( (Float)obj ).floatValue() == -1 );
    
    data[0] = 0;
    data[1] = (byte)-128;
    data[2] = 0;
    data[3] = 0;
    obj = datatype.decode( data );
    assertTrue( obj instanceof java.lang.Float );
    assertTrue( ( (Float)obj ).floatValue() == Float.MIN_NORMAL );

  }




  /**
   * The test will look weird because Java only deals with signed numbers
   */
  @Test
  public void testEncode()
  {
    //3.4028235E38 = 0x7F7FFFFF = 01111111 01111111 11111111 11111111
    float value = Float.MAX_VALUE;
    byte[] data = datatype.encode( value );
    assertTrue( data.length == 4 );
    assertTrue( data[0] == 127 );
    assertTrue( data[1] == 127 );
    assertTrue( data[2] == -1 );
    assertTrue( data[3] == -1 );

    // 1.4E-45 = 0x80000001 = 00000000 00000000 00000000 00000001
    value = Float.MIN_VALUE;
    data = datatype.encode( value );
    assertTrue( data.length == 4 );
    assertTrue( data[0] == 0 );
    assertTrue( data[1] == 0 );
    assertTrue( data[2] == 0 );
    assertTrue( data[3] == 1 );

    //0 = 0x00000000 = 00000000 00000000 00000000 00000000
    value = 0;
    data = datatype.encode( value );
    assertTrue( data.length == 4 );
    assertTrue( data[0] == 0 );
    assertTrue( data[1] == 0 );
    assertTrue( data[2] == 0 );
    assertTrue( data[3] == 0 );

    //-1 = 0xBF800000 = 10111111 10000000 00000000 00000000
    value = -1;
    data = datatype.encode( value );
    assertTrue( data.length == 4 );
    assertTrue( data[0] == -65 );
    assertTrue( data[1] == -128 );
    assertTrue( data[2] == 0 );
    assertTrue( data[3] == 0 );

    //1.17549435E-38 = 0x008000 = 00000000 10000000 00000000 00000000
    value = Float.MIN_NORMAL;
    data = datatype.encode( value );
    assertTrue( data.length == 4 );
    assertTrue( data[0] == 0 );
    assertTrue( data[1] == -128 );
    assertTrue( data[2] == 0 );
    assertTrue( data[3] == 0 );

  }




  @Test
  public void testGetTypeName()
  {
    assertTrue( datatype.getTypeName().equals( "FLT" ) );
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
