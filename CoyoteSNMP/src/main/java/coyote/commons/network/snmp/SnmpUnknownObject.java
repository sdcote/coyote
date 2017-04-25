/*
 * $Id:$
 */
package coyote.commons.network.snmp;

import java.io.ByteArrayOutputStream;


/**
 * Used when an unknown SNMP object type is encountered. 
 * 
 * <p>Just takes a byte array for its constructor, and uses this as raw 
 * bytes.</p>
 */
public class SnmpUnknownObject extends SnmpObject
{
  private byte[] data;
  protected byte tag = SnmpBerCodec.SNMPUNKNOWNOBJECT;

  


  /**
   * Just takes a byte array, and uses this as raw bytes.
   *
   * @param enc
   */
  public SnmpUnknownObject( byte[] enc )
  {
    data = enc;
  }




  /**
   * Return a byte array containing the raw bytes supplied.
   *
   * @return the object value
   */
  public Object getValue()
  {
    return data;
  }




  /**
   * Takes a byte array containing the raw bytes stored as the value.
   *
   * @param data
   *
   * @throws SnmpBadValueException
   */
  public void setValue( Object data ) throws SnmpBadValueException
  {
    if( data instanceof byte[] )
    {
      this.data = (byte[])data;
    }
    else
    {
      throw new SnmpBadValueException( " Unknown Object: bad object supplied to set value " );
    }
  }




  /**
   * Return the BER encoding of this object.
   *
   * @return
   */
  protected byte[] getBEREncoding()
  {

    ByteArrayOutputStream outBytes = new ByteArrayOutputStream();

    byte type = SnmpBerCodec.SNMPUNKNOWNOBJECT;

    // calculate encoding for length of data
    byte[] len = SnmpBerCodec.encodeLength( data.length );

    // encode T,L,V info
    outBytes.write( type );
    outBytes.write( len, 0, len.length );
    outBytes.write( data, 0, data.length );

    return outBytes.toByteArray();
  }




  /**
   * Return String created from raw bytes of this object.
   *
   * @return the string
   */
  public String toString()
  {
    return new String( data );
  }

}
