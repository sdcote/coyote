package coyote.dataframe;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;


public class U8TypeTest
{

  /** The data type under test. */
  static U8Type datatype = null;




  /**
   * @throws java.lang.Exception
   */
  @BeforeClass
  public static void setUpBeforeClass() throws Exception
  {
    datatype = new U8Type();
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
    short value = (short)255;

    assertTrue( datatype.checkType( value ) );

    value++;
    assertFalse( datatype.checkType( value ) );

    // test for overflow
    value = 0;
    assertTrue( datatype.checkType( value ) );

    // don't allow negatives
    value--;
    assertFalse( datatype.checkType( value ) );

  }




  @Test
  public void testDecode()
  {
    byte[] data = new byte[1];
    byte value = -1;
    data[0] = value; // should be single octet of all 1's

    Object obj = datatype.decode( data );
    assertTrue( obj instanceof java.lang.Short );
    assertTrue( ( (Short)obj ).shortValue() == 255 );

    //test for overflow
    value++;
    data[0] = value;
    obj = datatype.decode( data );
    assertTrue( obj instanceof java.lang.Short );
    assertTrue( ( (Short)obj ).shortValue() == 0 );

    value++;
    data[0] = value;
    obj = datatype.decode( data );
    assertTrue( obj instanceof java.lang.Short );
    assertTrue( ( (Short)obj ).shortValue() == 1 );

  }




  /**
   * Test might look a little goofy because Java only deals with signed numbers
   * and we have to test single byte values as if they are signed.
   */
  @Test
  public void testEncode()
  {
    short value = (short)254;
    byte[] data = datatype.encode( value );
    assertTrue( data.length == 1 );
    assertTrue( data[0] == -2 );

    value++;
    data = datatype.encode( value );
    assertTrue( data.length == 1 );
    assertTrue( data[0] == -1 );

    // Test for overflow
    value++;
    data = datatype.encode( value );
    assertTrue( data.length == 1 );
    assertTrue( data[0] == 0 );

    value++;
    data = datatype.encode( value );
    assertTrue( data.length == 1 );
    assertTrue( data[0] == 1 );

  }




  @Test
  public void testGetTypeName()
  {
    assertTrue( datatype.getTypeName().equals( "U8" ) );
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
