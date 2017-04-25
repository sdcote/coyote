/*
 * $Id:$
 */
package coyote.commons.network.snmp;

import java.io.ByteArrayOutputStream;


/**
 * Class representing a general string of octets.
 */
public class SnmpOctetString extends SnmpObject
{
  protected byte[] data;
  protected byte tag = SnmpBerCodec.SNMPOCTETSTRING;




  /**
   * Create a zero-length octet string.
   */
  public SnmpOctetString()
  {
    data = new byte[0];
  }




  /**
   * Create an octet string from the bytes of the supplied String.
   *
   * @param stringData
   */
  public SnmpOctetString( String stringData )
  {
    this.data = stringData.getBytes();
  }




  /**
   * Create an octet string from the supplied byte array. 
   * 
   * <p>The array may be either user-supplied, or part of a retrieved BER 
   * encoding. Note that the BER encoding of the data of an octet string is 
   * just the raw bytes.</p>
   *
   * @param enc
   */
  public SnmpOctetString( byte[] enc )
  {
    extractFromBEREncoding( enc );
  }




  /**
   * Return the array of raw bytes.
   *
   * @return the object value
   */
  public Object getValue()
  {
    return data;
  }




  /**
   * Used to set the value from a byte array.
   *
   * @param data
   * 
   * @throws SnmpBadValueException Indicates an incorrect object type supplied.
   */
  public void setValue( Object data ) throws SnmpBadValueException
  {
    if( data instanceof byte[] )
    {
      this.data = (byte[])data;
    }
    else if( data instanceof String )
    {
      this.data = ( (String)data ).getBytes();
    }
    else
    {
      throw new SnmpBadValueException( " Octet String: bad object supplied to set value " );
    }
  }




  /**
   * Returns the BER encoding for the octet string. 
   * 
   * <p>Note the the "value" part of the BER type,length,value triple is just 
   * the sequence of raw bytes.</p>
   *
   * @return
   */
  protected byte[] getBEREncoding()
  {
    ByteArrayOutputStream outBytes = new ByteArrayOutputStream();

    // calculate encoding for length of data
    byte[] len = SnmpBerCodec.encodeLength( data.length );

    // encode T,L,V info
    outBytes.write( tag );
    outBytes.write( len, 0, len.length );
    outBytes.write( data, 0, data.length );

    return outBytes.toByteArray();
  }




  /**
   * Method extractFromBEREncoding.
   *
   * @param enc
   */
  protected void extractFromBEREncoding( byte[] enc )
  {
    data = new byte[enc.length];

    // copy data
    for( int i = 0; i < enc.length; i++ )
    {
      data[i] = enc[i];
    }
  }




  /**
   * Returns a String constructed from the raw bytes. 
   * 
   * @return the string
   */
  public String toString()
  {
    String returnString;

    /*
     * if ((data.length == 4) || (data.length == 6))
     * {
     *   returnString = new String();
     *
     *   int convert = data[0];
     *   if (convert < 0)
     *   convert += 256;
     *   returnString += convert;
     *
     *   for (int i = 1; i < data.length; i++)
     *   {
     *     convert = data[i];
     *     if (convert < 0)
     *       convert += 256;
     *     returnString += "." + convert;
     *   }
     * }
     * else
     * returnString = new String(data);
     */
    /*
     * byte[] converted = new byte[data.length];
     *
     * for (int i = 0; i < data.length; i++)
     * {
     *   if (data[i] == 0)
     *     converted[i] = 0x20;    // space character
     *   else
     *     converted[i] = data[i];
     * }
     *
     * returnString = new String(converted);
     */
    returnString = new String( data );

    return returnString;

  }




  /**
   * Method hexByte.
   *
   * @param b
   *
   * @return
   */
  private String hexByte( byte b )
  {
    int pos = b;
    if( pos < 0 )
    {
      pos += 256;
    }

    String returnString = new String();
    returnString += Integer.toHexString( pos / 16 );
    returnString += Integer.toHexString( pos % 16 );

    return returnString;
  }




  /**
   * Returns a space-separated hex string corresponding to the raw bytes.
   *
   * @return the hex string
   */
  public String toHexString()
  {
    String returnString = new String();

    for( int i = 0; i < data.length; i++ )
    {
      returnString += hexByte( data[i] ) + " ";
    }

    return returnString;

  }

}
