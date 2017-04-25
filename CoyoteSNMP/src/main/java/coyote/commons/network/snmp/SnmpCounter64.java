/*
 * $Id:$
 */
package coyote.commons.network.snmp;

import java.math.BigInteger;


/**
 * Defines a 64-bit counter, whose value wraps if initialized with a larger
 * value. 
 * 
 * <p>For an indicator which "pegs" at its maximum value if initialized with a 
 * larger value, use SnmpGauge32; for a counter with a smaller range, use 
 * SnmpCounter32.</p>
 * 
 * @see coyote.commons.network.snmp.SnmpGauge32
 * @see coyote.commons.network.snmp.SnmpCounter32
 */
public class SnmpCounter64 extends SnmpInteger
{
  /** maximum value is 2^64 - 1; using approximation!! */
  private static BigInteger maxValue = new BigInteger( "18446744070000000000" );




  /**
   * Initialize value to 0.
   */
  public SnmpCounter64()
  {
    this( 0 );  // initialize value to 0
  }




  /**
   * Constructor SnmpCounter64.
   *
   * @param newValue
   */
  public SnmpCounter64( long newValue )
  {
    tag = SnmpBerCodec.SNMPCOUNTER64;

    value = new BigInteger( new Long( newValue ).toString() );

    // wrap if value > maxValue
    value = value.mod( maxValue );
  }




  /**
   * Used to initialize from the BER encoding, usually received in a response from
   * an SNMP device responding to an SNMPGetRequest.
   *
   * @param enc
   * 
   * @throws SnmpBadValueException Indicates an invalid BER encoding supplied. 
   *         Shouldn't occur in normal operation, i.e., when valid responses 
   *         are received from devices.
   */
  protected SnmpCounter64( byte[] enc ) throws SnmpBadValueException
  {
    tag = SnmpBerCodec.SNMPCOUNTER64;

    extractValueFromBEREncoding( enc );

    // wrap if value > maxValue
    value = value.mod( maxValue );
  }




  /**
   * Used to set the value with an instance of java.lang.Integer or
   * java.lang.BigInteger. 
   * 
   * <p>The value of the constructed SnmpCounter64 object is the supplied value 
   * mod 2^64.</p>
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
      value = value.mod( maxValue );  // wrap when value exceeds 2^64
    }
    else if( newValue instanceof Integer )
    {
      value = new BigInteger( newValue.toString() );
      value = value.mod( maxValue );  // wrap when value exceeds 2^64
    }
    else if( newValue instanceof String )
    {
      value = new BigInteger( (String)newValue );
      value = value.mod( maxValue );  // wrap when value exceeds 2^64
    }
    else
    {
      throw new SnmpBadValueException( " Counter64: bad object supplied to set value " );
    }
  }

}
