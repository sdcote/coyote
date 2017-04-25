/*
 * $Id:$
 */
package coyote.commons.network.snmp;

/**
 * Object representing the SNMP Null data type.
 */
public class SnmpNull extends SnmpObject
{

  protected byte tag = SnmpBerCodec.SNMPNULL;




  /**
   * Returns Java null reference.
   *
   * @return the null object value
   */
  public Object getValue()
  {
    return null;
  }




  /**
   * Always throws SnmpBadValueException.
   *
   * @param o
   *
   * @throws SnmpBadValueException
   */
  public void setValue( Object o ) throws SnmpBadValueException
  {
    throw new SnmpBadValueException( " Null: attempt to set value " );
  }




  /**
   * Return BER encoding for a null object: two bytes, tag and length of 0.
   *
   * @return
   */
  protected byte[] getBEREncoding()
  {
    byte[] encoding = new byte[2];

    // set tag byte
    encoding[0] = SnmpBerCodec.SNMPNULL;

    // len = 0 since no payload!
    encoding[1] = 0;

    // no V!

    return encoding;
  }




  /**
   * Returns String "Null"..
   *
   * @return the string
   */
  public String toString()
  {
    return new String( "Null" );
  }

}
