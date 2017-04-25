/*
 * $Id:$
 */
package coyote.commons.network.snmp;

/**
 * Class representing a general string of bits.
 */
public class SnmpBitString extends SnmpOctetString
{

  protected byte tag = SnmpBerCodec.SNMPBITSTRING;




  /**
   * Create a zero-length bit string.
   */
  public SnmpBitString()
  {
    this.data = new byte[0];
  }




  /**
   * Create a bit string from the bytes of the supplied String.
   *
   * @param stringData
   */
  public SnmpBitString( String stringData )
  {
    this.data = stringData.getBytes();
  }




  /**
   * Create a bit string from the supplied byte array. 
   * 
   * <p>The array may be either user-supplied, or part of a retrieved BER 
   * encoding. Note that the BER encoding of the data of a bit string is just 
   * the raw bytes.</p>
   *
   * @param enc
   */
  public SnmpBitString( byte[] enc )
  {
    extractFromBEREncoding( enc );
  }

}
