/*
 * $Id:$
 */
package coyote.commons.network.snmp;

import java.math.BigInteger;


/**
 * Defines a 32-bit unsigned integer value; wraps if initialized with a larger 
 * value.
 * 
 * @see coyote.commons.network.snmp.SnmpInteger
 */
public class SnmpUInteger32 extends SnmpInteger
{
  /** maximum value is 2^32 - 1 */
  private static BigInteger maxValue = new BigInteger( "4294967295" );




  /**
   * Initialize value to 0.
   */
  public SnmpUInteger32()
  {
    this( 0 );  // initialize value to 0
  }




  /**
   * Initialize value to newValue; wrap if newValue too big for 32 bits.
   *
   * @param newValue
   */
  public SnmpUInteger32( long newValue )
  {
    tag = SnmpBerCodec.SNMPUINTEGER32;

    value = new BigInteger( new Long( newValue ).toString() );

    // wrap if value > maxValue
    value = value.mod( maxValue );
  }




  /**
   * Used to initialize from the BER encoding, usually received in a response 
   * from an SNMP device responding to an SNMPGetRequest.
   *
   * @param enc
   * 
   * @throws SnmpBadValueException Indicates an invalid BER encoding supplied. 
   *         Shouldn't occur in normal operation, i.e., when valid responses 
   *         are received from devices.
   */
  protected SnmpUInteger32( byte[] enc ) throws SnmpBadValueException
  {
    tag = SnmpBerCodec.SNMPUINTEGER32;

    extractValueFromBEREncoding( enc );

    // wrap if value > maxValue
    value = value.mod( maxValue );
  }




  /**
   * Used to set the value with an instance of java.lang.Integer or
   * java.lang.BigInteger. 
   * 
   * <p>The value of the constructed SnmpUInteger32 object is the supplied 
   * value mod 2^32.</p>
   *
   * @param newValue
   * 
   * @throws SnmpBadValueException Indicates an incorrect object type supplied.
   */
  public void setValue( Object newValue ) throws SnmpBadValueException
  {
    if( newValue instanceof BigInteger )
    {
      value = (BigInteger)newValue;
      value = value.mod( maxValue );  // wrap when value exceeds 2^32
    }
    else if( newValue instanceof Integer )
    {
      value = new BigInteger( newValue.toString() );
      value = value.mod( maxValue );  // wrap when value exceeds 2^32
    }
    else if( newValue instanceof String )
    {
      value = new BigInteger( (String)newValue );
      value = value.mod( maxValue );  // wrap when value exceeds 2^32
    }
    else
    {
      throw new SnmpBadValueException( " Unsigned Integer: bad object supplied to set value " );
    }
  }

}
