/*
 * 
 */
package coyote.util;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import java.util.UUID;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import coyote.commons.ByteUtil;


/**
 * 
 */
public class ByteUtilTest
{

  /**
   * @throws java.lang.Exception
   */
  @BeforeClass
  public static void setUpBeforeClass() throws Exception
  {
  }




  /**
   * @throws java.lang.Exception
   */
  @AfterClass
  public static void tearDownAfterClass() throws Exception
  {
  }




  /**
   * @throws java.lang.Exception
   */
  @Before
  public void setUp() throws Exception
  {
  }




  /**
   * @throws java.lang.Exception
   */
  @After
  public void tearDown() throws Exception
  {
  }




  @Test
  public void testRenderShortByte()
  {
    short value = 255;
    byte[] data = new byte[1];
    data[0] = ByteUtil.renderShortByte( value );

    assertTrue( ByteUtil.retrieveShortByte( data, 0 ) == -1 );
    assertTrue( ByteUtil.retrieveUnsignedShortByte( data, 0 ) == 255 );
  }




  @Test
  public void testRenderShort()
  {
    short value = Short.MIN_VALUE;
    byte[] data = null;
    data = ByteUtil.renderShort( value );

    assertTrue( ByteUtil.retrieveShort( data, 0 ) == Short.MIN_VALUE );
    assertTrue( ByteUtil.retrieveUnsignedShort( data, 0 ) == 32768 );

    value = Short.MAX_VALUE;
    data = ByteUtil.renderShort( value );

    assertTrue( ByteUtil.retrieveShort( data, 0 ) == Short.MAX_VALUE );
    assertTrue( ByteUtil.retrieveUnsignedShort( data, 0 ) == 32767 );

    value = -1;
    data = ByteUtil.renderShort( value );

    assertTrue( ByteUtil.retrieveShort( data, 0 ) == -1 );
    assertTrue( ByteUtil.retrieveUnsignedShort( data, 0 ) == 65535 );

    // Test the maximum unsigned value in the unsigned renderer
    int intvalue = 65535;
    data = ByteUtil.renderUnsignedShort( intvalue );

    assertTrue( ByteUtil.retrieveShort( data, 0 ) == -1 );
    assertTrue( ByteUtil.retrieveUnsignedShort( data, 0 ) == 65535 );
  }




  @Test
  public void testRenderInt()
  {
    int value = Integer.MIN_VALUE;
    byte[] data = null;
    data = ByteUtil.renderInt( value );

    assertTrue( ByteUtil.retrieveInt( data, 0 ) == Integer.MIN_VALUE );
    assertTrue( ByteUtil.retrieveUnsignedInt( data, 0 ) == 2147483648L );

    value = Integer.MAX_VALUE;
    data = ByteUtil.renderInt( value );

    assertTrue( ByteUtil.retrieveInt( data, 0 ) == Integer.MAX_VALUE );
    assertTrue( ByteUtil.retrieveUnsignedInt( data, 0 ) == 2147483647L );

    value = -1;
    data = ByteUtil.renderInt( value );

    assertTrue( ByteUtil.retrieveInt( data, 0 ) == -1 );
    assertTrue( ByteUtil.retrieveUnsignedInt( data, 0 ) == 4294967295L );

    // Test the maximum unsigned value in the unsigned renderer
    long longvalue = 4294967295L;
    data = ByteUtil.renderUnsignedInt( longvalue );

    assertTrue( ByteUtil.retrieveInt( data, 0 ) == -1 );
    assertTrue( ByteUtil.retrieveUnsignedInt( data, 0 ) == 4294967295L );
  }




  @Test
  public void testRenderLong()
  {
    long value = Long.MIN_VALUE;
    byte[] data = null;
    data = ByteUtil.renderLong( value );

    assertTrue( ByteUtil.retrieveLong( data, 0 ) == Long.MIN_VALUE );

    // assertTrue(ByteUtil.retrieveUnsignedLong(data,0) == 2147483648L);

    value = Long.MAX_VALUE;
    data = ByteUtil.renderLong( value );

    assertTrue( ByteUtil.retrieveLong( data, 0 ) == Long.MAX_VALUE );

    // assertTrue(ByteUtil.retrieveUnsignedLong(data,0) == 2147483647L);

    value = -1;
    data = ByteUtil.renderLong( value );

    assertTrue( ByteUtil.retrieveLong( data, 0 ) == -1 );
    // assertTrue(ByteUtil.retrieveUnsignedLong(data,0) == 4294967295L);

    // Test the maximum unsigned value in the unsigned renderer
    // long longvalue = 4294967295L;
    // data = ByteUtil.renderUnsignedILong(longvalue);
    // assertTrue(ByteUtil.retrieveLong(data,0) == -1);
    // assertTrue(ByteUtil.retrieveUnsignedLong(data,0) == 4294967295L);
  }




  @Test
  public void testRenderFloat()
  {
    float value = Float.MIN_VALUE;
    byte[] data = null;
    data = ByteUtil.renderFloat( value );

    assertTrue( ByteUtil.retrieveFloat( data, 0 ) == Float.MIN_VALUE );

    value = Float.MAX_VALUE;
    data = ByteUtil.renderFloat( value );

    assertTrue( ByteUtil.retrieveFloat( data, 0 ) == Float.MAX_VALUE );

    value = -1;
    data = ByteUtil.renderFloat( value );

    assertTrue( ByteUtil.retrieveFloat( data, 0 ) == -1 );
  }




  @Test
  public void testRenderDouble()
  {
    double value = Double.MIN_VALUE;
    byte[] data = null;
    data = ByteUtil.renderDouble( value );

    assertTrue( ByteUtil.retrieveDouble( data, 0 ) == Double.MIN_VALUE );

    value = Double.MAX_VALUE;
    data = ByteUtil.renderDouble( value );

    assertTrue( ByteUtil.retrieveDouble( data, 0 ) == Double.MAX_VALUE );

    value = -1;
    data = ByteUtil.renderDouble( value );

    assertTrue( ByteUtil.retrieveDouble( data, 0 ) == -1 );
    // System.out.println( "renderDouble(" + value + ")\r\n" +
    // ByteUtil.dump(
    // data ) );
    // System.out.println( "retrieveDouble=" + ByteUtil.retrieveDouble(
    // data, 0
    // ) );
    // System.out.println();
  }




  /**
   * Test method for {@link coyote.commons.ByteUtil#renderBoolean(boolean)}.
   */
  @Test
  public void testRenderBoolean()
  {
    boolean value = true;
    byte[] data = null;
    data = ByteUtil.renderBoolean( value );

    assertTrue( data.length == 1 );
    assertTrue( "Byte0=" + data[0], data[0] == 1 );

    value = false;
    data = ByteUtil.renderBoolean( value );

    assertTrue( data.length == 1 );
    assertTrue( "Byte0=" + data[0], data[0] == 0 );

    assertTrue( ByteUtil.retrieveBoolean( data, 0 ) == false );
  }




  /**
   * Test method for
   * {@link coyote.commons.ByteUtil#retrieveBoolean(byte[], int)}.
   */
  @Test
  public void testRetrieveBoolean()
  {
    byte[] data = new byte[1];
    data[0] = 0;
    assertFalse( ByteUtil.retrieveBoolean( data, 0 ) );

    data[0] = 1;
    assertTrue( ByteUtil.retrieveBoolean( data, 0 ) );
  }




  @Test
  public void testRenderDate()
  {
    // Wed Aug 11 10:26:04 EDT 2004
    // Time: 1092234364465
    // +000:00--+001:01--+002:02--+003:03--+004:04--+005:05--+006:06--+007:07--+
    // |00000000|00000000|00000000|11111110|01001110|00111101|11000110|00110001|
    // |000:00: |000:00: |000:00: |254:fe: |078:4e:N|061:3d:=|198:c6: |049:31:1|
    // +--------+--------+--------+--------+--------+--------+--------+--------+
    final long TESTMILLIS = 1092234364465L;

    Calendar cal = new GregorianCalendar();
    cal.set( Calendar.YEAR, 2004 );
    cal.set( Calendar.MONTH, 7 );
    cal.set( Calendar.DAY_OF_MONTH, 11 );
    cal.set( Calendar.HOUR_OF_DAY, 10 );
    cal.set( Calendar.MINUTE, 26 );
    cal.set( Calendar.SECOND, 04 );
    cal.set( Calendar.MILLISECOND, 465 );
    cal.set( Calendar.ZONE_OFFSET, TimeZone.getTimeZone( "US/Eastern" ).getRawOffset() );
    Date date = cal.getTime();

    byte[] data = ByteUtil.renderDate( date );

    long longval = ByteUtil.retrieveLong( data, 0 );

    assertTrue( "Byte0=" + data[0], data[0] == 0 );
    assertTrue( "Byte1=" + data[1], data[1] == 0 );
    assertTrue( "Byte2=" + data[2], data[2] == 0 );
    assertTrue( "Byte3=" + data[3], data[3] == -2 ); // signed value
    assertTrue( "Byte4=" + data[4], data[4] == 78 );

    if( longval - TESTMILLIS == 0 )
    {
      assertTrue( "Byte5=" + data[5], data[5] == 61 );
      assertTrue( "Byte6=" + data[6], data[6] == -58 ); // signed value
      assertTrue( "Byte7=" + data[7], data[7] == 49 );
    }
    else if( longval - TESTMILLIS != 3600000 ) // Daylight saving time issue
    {
      fail( "Time discrepancy" );
    }
  }




  /**
   * Test method for
   * {@link coyote.commons.ByteUtil#retrieveDate(byte[], int)}.
   */
  @Test
  public void testRetrieveDate()
  {
    Date value = new Date();
    byte[] data = null;
    data = ByteUtil.renderDate( value );

    assertTrue( ByteUtil.retrieveDate( data, 0 ).getTime() == value.getTime() );

    data = ByteUtil.renderDate( null );

    assertTrue( ByteUtil.retrieveDate( data, 0 ).getTime() == 0 );
  }




  @Test
  public void testRenderUUID()
  {
    UUID uuid = UUID.randomUUID();
    byte[] data = ByteUtil.renderUUID( uuid );

    String text = uuid.toString();
    text = text.replace( "-", "" );
    assertNotNull( data );
    assertTrue( data.length == 16 );

    int x = 0;
    for( int i = 0; i < text.length(); i += 2 )
    {
      assertTrue( ( data[x++] == (byte)Integer.parseInt( text.substring( i, i + 2 ), 16 ) ) );
    }
  }




  // We need to support UUIDs in the future
  public void testRetrieveUUID()
  {
    byte[] data = new byte[16];
    data[0] = (byte)104;
    data[1] = (byte)186;
    data[2] = (byte)137;
    data[3] = (byte)118;
    data[4] = (byte)105;
    data[5] = (byte)164;
    data[6] = (byte)722;
    data[7] = (byte)151;
    data[8] = (byte)160;
    data[9] = (byte)113;
    data[10] = (byte)215;
    data[11] = (byte)163;
    data[12] = (byte)178;
    data[13] = (byte)82;
    data[14] = (byte)226;
    data[15] = (byte)232;

    System.out.println( ByteUtil.dump( data ) );
    System.out.println();

    UUID uuid = ByteUtil.retrieveUUID( data, 0 );

    byte[] byts = ByteUtil.renderUUID( uuid );
    System.out.println( ByteUtil.dump( byts ) );

  }

}
