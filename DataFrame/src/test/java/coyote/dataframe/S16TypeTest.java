package coyote.dataframe;

import static org.junit.Assert.assertTrue;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;


/**
 * -32,768 to 32,767
 */
public class S16TypeTest
{

  /** The data type under test. */
  static S16Type datatype = null;




  /**
   * @throws java.lang.Exception
   */
  @BeforeClass
  public static void setUpBeforeClass() throws Exception
  {
    datatype = new S16Type();
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
    short value = (short)32767;
    assertTrue( datatype.checkType( value ) );
    //0x7FFF = 01111111 11111111

    // This overflows in Java to -32768
    value++;
    assertTrue( datatype.checkType( value ) );

    value = 0;
    assertTrue( datatype.checkType( value ) );

    value--; // -1
    assertTrue( datatype.checkType( value ) );

    value = -32768;
    assertTrue( datatype.checkType( value ) );
    //0x8000 = 10000000 00000000

    // This overflows in Java to 32767
    value--;
    assertTrue( datatype.checkType( value ) );

  }




  @Test
  public void testDecode()
  {
    byte[] data = new byte[2];
    data[0] = (byte)128;
    data[1] = 0;

    //-32768 = 0x8000 = 10000000 00000000
    Object obj = datatype.decode( data );
    assertTrue( obj instanceof java.lang.Short );
    assertTrue( ( (Short)obj ).shortValue() == -32768 );

    //32767 = 0x7FFF = 01111111 11111111
    data[0] = (byte)127;
    data[1] = -1;
    obj = datatype.decode( data );
    assertTrue( obj instanceof java.lang.Short );
    assertTrue( ( (Short)obj ).shortValue() == 32767 );

    //0 = 0x0000 = 00000000 00000000
    data[0] = 0;
    data[1] = 0;
    obj = datatype.decode( data );
    assertTrue( obj instanceof java.lang.Short );
    assertTrue( ( (Short)obj ).shortValue() == 0 );

    //-1 = 0xFFFF = 11111111 11111111
    data[0] = -1;
    data[1] = -1;
    obj = datatype.decode( data );
    assertTrue( obj instanceof java.lang.Short );
    assertTrue( ( (Short)obj ).shortValue() == -1 );

  }




  /**
   * The test will look weird because Java only deals with signed numbers
   */
  @Test
  public void testEncode()
  {
    //32767 = 0x7FFF = 01111111 11111111
    short value = (short)32767;
    byte[] data = datatype.encode( value );
    assertTrue( data.length == 2 );
    assertTrue( data[0] == 127 );
    assertTrue( data[1] == -1 );

    // Overflow to -32768
    value++;
    data = datatype.encode( value );
    assertTrue( data.length == 2 );
    assertTrue( data[0] == -128 );
    assertTrue( data[1] == 0 );

    //0 = 0x0000 = 00000000 00000000
    value = 0;
    data = datatype.encode( value );
    assertTrue( data.length == 2 );
    assertTrue( data[0] == 0 );
    assertTrue( data[1] == 0 );

    //-1 = 0xFFFF = 11111111 11111111
    value--;
    data = datatype.encode( value );
    assertTrue( data.length == 2 );
    assertTrue( data[0] == -1 );
    assertTrue( data[1] == -1 );

  }




  @Test
  public void testGetTypeName()
  {
    assertTrue( datatype.getTypeName().equals( "S16" ) );
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
