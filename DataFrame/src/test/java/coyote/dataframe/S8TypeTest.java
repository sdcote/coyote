package coyote.dataframe;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;


public class S8TypeTest
{

  /** The data type under test. */
  static S8Type datatype = null;




  /**
   * @throws java.lang.Exception
   */
  @BeforeClass
  public static void setUpBeforeClass() throws Exception
  {
    datatype = new S8Type();
  }




  /**
   * @throws java.lang.Exception
   */
  @AfterClass
  public static void tearDownAfterClass() throws Exception
  {
    datatype = null;
  }




  @Test
  public void testCheckType()
  {
    short value = (short)127;
    assertTrue( datatype.checkType( value ) );
    //0x7F = 01111111

    value++;
    assertFalse( datatype.checkType( value ) );

    value = 0;
    assertTrue( datatype.checkType( value ) );
    //0x00 = 00000000

    value--; // -1
    assertTrue( datatype.checkType( value ) );
    //0xFF = 11111111

    value = -128;
    assertTrue( datatype.checkType( value ) );
    //0x80 = 10000000

    value--;
    assertFalse( datatype.checkType( value ) );

  }




  @Test
  public void testDecode()
  {
    byte[] data = new byte[1];
    byte value = -128;
    data[0] = value;

    //0x80 = 10000000
    Object obj = datatype.decode( data );
    assertTrue( obj instanceof java.lang.Short );
    assertTrue( ( (Short)obj ).shortValue() == -128 );

    //test for overflow
    //0x81 = 10000001
    value++;
    data[0] = value;
    obj = datatype.decode( data );
    assertTrue( obj instanceof java.lang.Short );
    assertTrue( ( (Short)obj ).shortValue() == -127);

    //0x82 = 10000010
    value++;
    data[0] = value;
    obj = datatype.decode( data );
    assertTrue( obj instanceof java.lang.Short );
    assertTrue( ( (Short)obj ).shortValue() == -126 );

    //0x7F = 01111111
    value = 127;
    data[0] = value;
    obj = datatype.decode( data );
    assertTrue( obj instanceof java.lang.Short );
    assertTrue( ( (Short)obj ).shortValue() == 127 );
    
    //test for overflow
    //0x80 = 10000000
    value++;
    data[0] = value;
    obj = datatype.decode( data );
    assertTrue( obj instanceof java.lang.Short );
    assertTrue( ( (Short)obj ).shortValue() == -128);

  }




  @Test
  public void testEncode()
  {
    //0x7F = 01111111
    short value = (short)127;
    byte[] data = datatype.encode( value );
    assertTrue( data.length == 1 );
    assertTrue( data[0] == 127 );

    // Test for overflow
    value++;
    data = datatype.encode( value );
    assertTrue( data.length == 1 );
    assertTrue( data[0] == -128 );

    //0x80 = 10000000
    value = -128;
    data = datatype.encode( value );
    assertTrue( data.length == 1 );
    assertTrue( data[0] == -128 );

    // overflow - but Java corrects for it since we use a byte primitive and 
    // Java uses signed numbers
    value--;
    data = datatype.encode( value );
    assertTrue( data.length == 1 );
    assertTrue( data[0] == 127 );

  }




  @Test
  public void testGetTypeName()
  {
    assertTrue( datatype.getTypeName().equals( "S8" ) );
  }




  @Test
  public void testIsNumeric()
  {
    assertTrue( datatype.isNumeric() );
  }




  @Test
  public void testGetSize()
  {
    assertTrue( datatype.getSize() == 1 );
  }

}
