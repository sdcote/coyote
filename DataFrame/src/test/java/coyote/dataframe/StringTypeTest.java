/**
 * 
 */
package coyote.dataframe;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;


/**
 * This class models...StringTypeTest
 * 
 * @author Stephan D. Cote'
 */
public class StringTypeTest {
  /** The data type under test. */
  static StringType datatype = null;




  /**
   * @throws java.lang.Exception
   */
  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    datatype = new StringType();
  }




  /**
   * @throws java.lang.Exception
   */
  @AfterClass
  public static void tearDownAfterClass() throws Exception {
    datatype = null;
  }




  /**
   * Test method for {@link coyote.dataframe.StringType#checkType(java.lang.Object)}.
   */
  @Test
  public void testCheckType() {

    String data = "abc";
    assertTrue(datatype.checkType(data));
  }




  /**
   * Test method for {@link coyote.dataframe.StringType#decode(byte[])}.
   */
  @Test
  public void testDecode() {
    byte[] data = new byte[3];
    data[0] = 97;
    data[1] = 98;
    data[2] = 99;
    Object value = datatype.decode(data);
    assertTrue(value instanceof String);
    assertTrue(((String)value).equals("abc"));
  }




  /**
   * Test method for {@link coyote.dataframe.StringType#encode(java.lang.Object)}.
   */
  @Test
  public void testEncode() {
    String data = new String("abc");
    byte[] value = datatype.encode(data);
    assertTrue(value[0] == 97);
    assertTrue(value[1] == 98);
    assertTrue(value[2] == 99);
    //System.out.println( ByteUtil.dump( value ) );
  }




  /**
   * Test method for {@link coyote.dataframe.StringType#isNumeric()}.
   */
  @Test
  public void testIsNumeric() {
    assertFalse(datatype.isNumeric());
  }




  /**
   * Test method for {@link coyote.dataframe.StringType#getSize()}.
   */
  @Test
  public void testGetSize() {
    assertTrue(datatype.getSize() == -1);
  }




  /**
   * Test method for {@link coyote.dataframe.StringType#getTypeName()}.
   */
  @Test
  public void testGetTypeName() {
    assertTrue(datatype.getTypeName().equals("STR"));
  }

}
