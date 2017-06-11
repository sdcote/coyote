/**
 * 
 */
package coyote.dataframe;

//import static org.junit.Assert.*;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import coyote.commons.ByteUtil;


/**
 *
 */
public class DateTypeTest {
  /** The data type under test. */
  static DateType datatype = null;
  static SimpleDateFormat dateFormat = null;
  static byte[] datedata = new byte[8];
  static Calendar cal = null;




  @BeforeClass
  public static void setUpBeforeClass() throws Exception
  {
    datatype = new DateType();
    dateFormat = new SimpleDateFormat( "yyyy/MM/dd HH:mm:ss" );
    datedata[0] = (byte)0;
    datedata[1] = (byte)0;
    datedata[2] = (byte)0;
    datedata[3] = (byte)191;
    datedata[4] = (byte)169;
    datedata[5] = (byte)97;
    datedata[6] = (byte)245;
    datedata[7] = (byte)248;

    cal = new GregorianCalendar();
    cal.set( Calendar.YEAR, 1996 );
    cal.set( Calendar.MONTH, 1 );
    cal.set( Calendar.DAY_OF_MONTH, 1 );
    cal.set( Calendar.HOUR_OF_DAY, 8 );
    cal.set( Calendar.MINUTE, 15 );
    cal.set( Calendar.SECOND, 23 );
    cal.set( Calendar.MILLISECOND, 0 );
    cal.set( Calendar.ZONE_OFFSET, TimeZone.getTimeZone( "US/Eastern" ).getRawOffset() );
  }




  @AfterClass
  public static void tearDownAfterClass() throws Exception
  {
    datatype = null;
  }




  @Test
  public void testCheckType()
  {
    Date date = new Date();
    assertTrue( datatype.checkType( date ) );
  }




  @Test
  public void testDecode()
  {
    System.out.println( "=================================================================================\r\n" );
    System.out.println( "Decoding test data:\r\n" + ByteUtil.dump( datedata ) );
    Object obj = datatype.decode( datedata );
    assertNotNull( obj );
    assertTrue( obj instanceof Date );
    Date date = (Date)obj;
    System.out.println( "Decoded as: " + date );
    System.out.println( "a long value of: " + date.getTime() );
    assertTrue( cal.getTime().equals( date ) );
    System.out.println( "Test Completed Successfully =====================================================\r\n" );
  }




  /**
   * Test method for
   * {@link coyote.dataframe.DateType#encode(java.lang.Object)}.
   */
  @Test
  public void testEncode()
  {
    System.out.println( "=================================================================================\r\n" );

    Date date = cal.getTime();
    System.out.println( "Encoding date '" + date.toString() + "' as long value: " + date.getTime() );
    System.out.println( "Expecting an encoding of:\r\n" + ByteUtil.dump( datedata ) );
    byte[] data = datatype.encode( date );
    assertNotNull( data );
    System.out.println( "Encoded as:\r\n" + ByteUtil.dump( data ) );
    assertTrue( "Dat should be 8 bytes in length, is actually " + data.length + " bytes", data.length == 8 );
    for( int i = 0; i < datedata.length; i++ ) {
      assertTrue( "element " + i + " should be " + datedata[i] + " but is '" + data[i] + "'", data[i] == datedata[i] );
    }
    System.out.println( "Test Completed Successfully =====================================================\r\n" );
  }




  @Test
  public void testGetTypeName()
  {
    assertTrue( datatype.getTypeName().equals( "DAT" ) );
  }




  @Test
  public void testIsNumeric()
  {
    assertFalse( datatype.isNumeric() );
  }




  @Test
  public void testGetSize()
  {
    assertTrue( datatype.getSize() == 8 );
  }

}
