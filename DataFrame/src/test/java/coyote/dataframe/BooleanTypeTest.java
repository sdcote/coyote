package coyote.dataframe;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;


public class BooleanTypeTest
{
  /** The data type under test. */
  static BooleanType datatype = null;




  /**
   * @throws java.lang.Exception
   */
  @BeforeClass
  public static void setUpBeforeClass() throws Exception
  {
    datatype = new BooleanType();
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
    assertTrue( datatype.getSize() == 1 );
  }




  @Test
  public void testCheckType()
  {
    assertTrue( datatype.checkType( true ) );
    assertTrue( datatype.checkType( false ) );
    assertFalse( datatype.checkType( "" ) );
    assertFalse( datatype.checkType( 1 ) );
    assertFalse( datatype.checkType( 0 ) );
  }




  @Test
  public void testEncode()
  {
    byte[] result = datatype.encode( true );
    assertNotNull( result );
    assertTrue( result.length == 1 );
    assertTrue( result[0] == 1 );
    
    result = datatype.encode( false );
    assertNotNull( result );
    assertTrue( result.length == 1 );
    assertTrue( result[0] == 0 );
  }




  @Test
  public void testDecode()
  {
    byte[] data = new byte[1];
    data[0]=1;
    Object obj = datatype.decode( data );
    assertNotNull( obj );
    System.out.println(obj);
  }




  @Test
  public void testGetTypeName()
  {
    assertTrue( datatype.getTypeName().equals( "BOL" ) );
  }




  @Test
  public void testIsNumeric()
  {
    assertFalse( datatype.isNumeric() );
  }

}
