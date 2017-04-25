/*
 * $Id:$
 */
package coyote.commons.network.snmp;

import java.math.BigInteger;


/**
 * Defines a 32-bit gauge, whose value "pegs" at the maximum if initialized 
 * with a larger value. 
 * 
 * <p>For an indicator which wraps when it reaches its maximum value, use 
 * SnmpCounter32; for a counter with a wider range, use SnmpCounter64.</p>
 * 
 * @see coyote.commons.network.snmp.SnmpCounter32
 * @see coyote.commons.network.snmp.SnmpCounter64
 */
public class SnmpGauge32 extends SnmpInteger
{
  /** maximum value is 2^32 - 1 (hack w/ 4*107...) */
  private static BigInteger maxValue = new BigInteger( "4294967295" );




  /**
   * Initialize value to 0.
   */
  public SnmpGauge32()
  {
    this( 0 );  // initialize value to 0
  }




  /**
   * Constructor SnmpGauge32.
   *
   * @param newValue
   */
  public SnmpGauge32( long newValue )
  {
    tag = SnmpBerCodec.SNMPGAUGE32;

    value = new BigInteger( new Long( newValue ).toString() );

    // peg if value > maxValue
    value = value.min( maxValue );
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
  protected SnmpGauge32( byte[] enc ) throws SnmpBadValueException
  {
    tag = SnmpBerCodec.SNMPGAUGE32;

    extractValueFromBEREncoding( enc );

    // peg if value > maxValue
    value = value.min( maxValue );
  }




  /**
   * Used to set the value with an instance of java.lang.Integer or
   * java.lang.BigInteger. 
   * 
   * <p>The value of the constructed SnmpGauge32 object is the supplied value 
   * or 2^32, whichever is less.</p>
   *
   * @param newValue
   * 
   * @throws SnmpBadValueException Indicates an incorrect object type supplied.
   */
  public void setValue( Object newValue ) throws SnmpBadValueException
  {
    // plateau when value hits maxValue
    if( newValue instanceof BigInteger )
    {
      value = (BigInteger)newValue;
      value = value.min( maxValue );
    }
    else if( newValue instanceof Integer )
    {
      value = new BigInteger( newValue.toString() );
      value = value.min( maxValue );
    }
    else if( newValue instanceof String )
    {
      value = new BigInteger( (String)newValue );
      value = value.min( maxValue );
    }
    else
    {
      throw new SnmpBadValueException( " Gauge32: bad object supplied to set value " );
    }
  }

}
