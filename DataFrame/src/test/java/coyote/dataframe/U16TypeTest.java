package coyote.dataframe;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;


/**
 * 16-bit value in the range of 0 to 65,535
 */
public class U16TypeTest
{

  /** The data type under test. */
  static U16Type datatype = null;




  /**
   * @throws java.lang.Exception
   */
  @BeforeClass
  public static void setUpBeforeClass() throws Exception
  {
    datatype = new U16Type();
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
    int value = 65535;
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
    byte[] data = new byte[2];
    data[0] = 0;
    data[1] = 0;

    // Minimum size for this type
    //0 = 0x0000 = 00000000 00000000
    Object obj = datatype.decode( data );
    assertTrue( obj instanceof java.lang.Integer );
    assertTrue( ( (Integer)obj ).intValue() == 0 );

    // Maximum size for this type
    //65535 = 0xFFFF = 11111111 11111111
    data[0] = -1;
    data[1] = -1;
    obj = datatype.decode( data );
    assertTrue( obj instanceof java.lang.Integer );
    assertTrue( ( (Integer)obj ).intValue() == 65535 );

  }




  /**
   * The test will look weird because Java only deals with signed numbers
   */
  @Test
  public void testEncode()
  {
    // 65535 =0xFFFF = 11111111 11111111
    int value = 65535;
    byte[] data = datatype.encode( value );
    assertTrue( data.length == 2 );
    assertTrue( data[0] == -1 );
    assertTrue( data[1] == -1 );

    // Overflow to 0
    value++;
    data = datatype.encode( value );
    assertTrue( data.length == 2 );
    assertTrue( data[0] == 0 );
    assertTrue( data[1] == 0 );

    //0 = 0x0000 = 00000000 00000000
    value = 0;
    data = datatype.encode( value );
    assertTrue( data.length == 2 );
    assertTrue( data[0] == 0 );
    assertTrue( data[1] == 0 );

    // underflow to 65535
    //65535 = 0xFFFF = 11111111 11111111
    value--;
    data = datatype.encode( value );
    assertTrue( data.length == 2 );
    assertTrue( data[0] == -1 );
    assertTrue( data[1] == -1 );

  }




  @Test
  public void testGetTypeName()
  {
    assertTrue( datatype.getTypeName().equals( "U16" ) );
  }




  @Test
  public void testIsNumeric()
  {
    assertTrue( datatype.isNumeric() );
  }




  @Test
  public void testGetSize()
  {
    assertTrue( datatype.getSize() == 2 );
  }

}
