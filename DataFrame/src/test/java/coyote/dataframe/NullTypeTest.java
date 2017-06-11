package coyote.dataframe;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;


public class NullTypeTest
{
  /** The data type under test. */
  static UndefinedType datatype = null;




  /**
   * @throws java.lang.Exception
   */
  @BeforeClass
  public static void setUpBeforeClass() throws Exception
  {
    datatype = new UndefinedType();
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
  public void testGetSize()
  {
    assertTrue( datatype.getSize() == 0 );
  }




  @Test
  public void testCheckType()
  {
    assertTrue( datatype.checkType( null ) );
    assertFalse( datatype.checkType( "" ) );
  }




  @Test
  public void testEncode()
  {
    byte[] result = datatype.encode( null );
    assertNotNull( result );
    assertTrue( result.length == 0 );
  }




  @Test
  public void testDecode()
  {
    byte[] data = new byte[0];
    Object obj = datatype.decode( data );
    assertNull( obj );
  }




  @Test
  public void testGetTypeName()
  {
    assertTrue( datatype.getTypeName().equals( "UDEF" ) );
  }




  @Test
  public void testIsNumeric()
  {
    assertFalse( datatype.isNumeric() );
  }

}
