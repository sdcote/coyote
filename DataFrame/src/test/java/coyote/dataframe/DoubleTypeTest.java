package coyote.dataframe;

import static org.junit.Assert.assertTrue;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;


/**
 * 32-bit floating point value in the range of +/-1.4013e-45 to +/-3.4028e+38.
 */
public class DoubleTypeTest
{

  /** The data type under test. */
  static DoubleType datatype = null;




  /**
   * @throws java.lang.Exception
   */
  @BeforeClass
  public static void setUpBeforeClass() throws Exception
  {
    datatype = new DoubleType();
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
    assertTrue( datatype.checkType( Double.MAX_VALUE ) );
    assertTrue( datatype.checkType( Double.MIN_VALUE ) );
    assertTrue( datatype.checkType( Double.MIN_NORMAL ) );
  }




  @Test
  public void testDecode()
  {
    Object obj = null;
    byte[] data = new byte[8];

    data[0] = (byte)127;
    data[1] = (byte)-17;
    data[2] = (byte)-1;
    data[3] = (byte)-1;
    data[4] = (byte)-1;
    data[5] = (byte)-1;
    data[6] = (byte)-1;
    data[7] = (byte)-1;
    obj = datatype.decode( data );
    assertTrue( obj instanceof java.lang.Double );
    assertTrue( ( (Double)obj ).doubleValue() == Double.MAX_VALUE );

    data[0] = 0;
    data[1] = 0;
    data[2] = 0;
    data[3] = 0;
    data[4] = 0;
    data[5] = 0;
    data[6] = 0;
    data[7] = (byte)1;
    obj = datatype.decode( data );
    assertTrue( obj instanceof java.lang.Double );
    assertTrue( ( (Double)obj ).doubleValue() == Double.MIN_VALUE );

    data[0] = 0;
    data[1] = 0;
    data[2] = 0;
    data[3] = 0;
    data[4] = 0;
    data[5] = 0;
    data[6] = 0;
    data[7] = 0;
    obj = datatype.decode( data );
    assertTrue( obj instanceof java.lang.Double );
    assertTrue( ( (Double)obj ).doubleValue() == 0 );

    data[0] = (byte)-65;
    data[1] = (byte)-16;
    data[2] = 0;
    data[3] = 0;
    data[4] = 0;
    data[5] = 0;
    data[6] = 0;
    data[7] = 0;
    obj = datatype.decode( data );
    assertTrue( obj instanceof java.lang.Double );
    assertTrue( ( (Double)obj ).doubleValue() == -1 );

    data[0] = 0;
    data[1] = (byte)16;
    data[2] = 0;
    data[3] = 0;
    data[4] = 0;
    data[5] = 0;
    data[6] = 0;
    data[7] = 0;
    obj = datatype.decode( data );
    assertTrue( obj instanceof java.lang.Double );
    assertTrue( ( (Double)obj ).doubleValue() == Double.MIN_NORMAL );

  }




  @Test
  public void testEncode()
  {
    double value = Double.MAX_VALUE;
    byte[] data = datatype.encode( value );
    assertTrue( data.length == 8 );
    assertTrue( data[0] == 127 );
    assertTrue( data[1] == -17 );
    assertTrue( data[2] == -1 );
    assertTrue( data[3] == -1 );
    assertTrue( data[4] == -1 );
    assertTrue( data[5] == -1 );
    assertTrue( data[6] == -1 );
    assertTrue( data[7] == -1 );

    value = Double.MIN_VALUE;
    data = datatype.encode( value );
    assertTrue( data.length == 8 );
    assertTrue( data[0] == 0 );
    assertTrue( data[1] == 0 );
    assertTrue( data[2] == 0 );
    assertTrue( data[3] == 0 );
    assertTrue( data[4] == 0 );
    assertTrue( data[5] == 0 );
    assertTrue( data[6] == 0 );
    assertTrue( data[7] == 1 );

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

    value = -1;
    data = datatype.encode( value );
    assertTrue( data.length == 8 );
    assertTrue( data[0] == -65 );
    assertTrue( data[1] == -16 );
    assertTrue( data[2] == 0 );
    assertTrue( data[3] == 0 );
    assertTrue( data[4] == 0 );
    assertTrue( data[5] == 0 );
    assertTrue( data[6] == 0 );
    assertTrue( data[7] == 0 );

    value = Double.MIN_NORMAL;
    data = datatype.encode( value );
    assertTrue( data.length == 8 );
    assertTrue( data[0] == 0 );
    assertTrue( data[1] == 16 );
    assertTrue( data[2] == 0 );
    assertTrue( data[3] == 0 );
    assertTrue( data[4] == 0 );
    assertTrue( data[5] == 0 );
    assertTrue( data[6] == 0 );
    assertTrue( data[7] == 0 );
  }




  @Test
  public void testGetTypeName()
  {
    assertTrue( datatype.getTypeName().equals( "DBL" ) );
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
