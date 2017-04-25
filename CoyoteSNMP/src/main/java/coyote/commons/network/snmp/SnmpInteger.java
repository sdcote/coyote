/*
 * $Id:$
 */
package coyote.commons.network.snmp;

import java.io.ByteArrayOutputStream;
import java.math.BigInteger;


/**
 * Defines an arbitrarily-sized integer value; there is no limit on size due to 
 * the use of Java.lang.BigInteger to store the value internally. 
 * 
 * <p>For an indicator which "pegs" at its maximum value if initialized with a 
 * larger value, use SnmpGauge32; for a counter which wraps, use SnmpCounter32 
 * or SnmpCounter64.</p>
 * 
 * @see coyote.commons.network.snmp.SnmpCounter32
 * @see coyote.commons.network.snmp.SnmpGauge32
 * @see coyote.commons.network.snmp.SnmpCounter64
 */
public class SnmpInteger extends SnmpObject
{
  protected BigInteger value;
  protected byte tag = SnmpBerCodec.SNMPINTEGER;




  /**
   * Initialize value to 0.
   */
  public SnmpInteger()
  {
    this( 0 );  // initialize value to 0
  }




  /**
   * Constructor SnmpInteger.
   *
   * @param value
   */
  public SnmpInteger( long value )
  {
    this.value = new BigInteger( new Long( value ).toString() );
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
  protected SnmpInteger( byte[] enc ) throws SnmpBadValueException
  {
    extractValueFromBEREncoding( enc );
  }




  /**
   * Returns a java.lang.BigInteger object with the current value.
   *
   * @return the object value
   */
  public Object getValue()
  {
    return value;
  }




  /**
   * Used to set the value with an instance of java.lang.Integer or
   * java.lang.BigInteger.
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
    }
    else if( newValue instanceof Integer )
    {
      value = new BigInteger( ( (Integer)newValue ).toString() );
    }
    else if( newValue instanceof String )
    {
      value = new BigInteger( (String)newValue );
    }
    else
    {

      throw new SnmpBadValueException( " Integer: bad object supplied to set value " );
    }
  }




  /**
   * Returns the full BER encoding (type, length, value) of the SnmpInteger 
   * subclass.
   *
   * @return
   */
  protected byte[] getBEREncoding()
  {
    ByteArrayOutputStream outBytes = new ByteArrayOutputStream();

    // write contents
    byte[] data = value.toByteArray();

    // calculate encoding for length of data
    byte[] len = SnmpBerCodec.encodeLength( data.length );

    // encode T,L,V info
    outBytes.write( tag );
    outBytes.write( len, 0, len.length );
    outBytes.write( data, 0, data.length );

    return outBytes.toByteArray();
  }




  /**
   * Used to extract a value from the BER encoding of the value. 
   * 
   * <p>Called in constructors for SnmpInteger subclasses.</p>
   *
   * @param enc
   * 
   * @throws SnmpBadValueException Indicates an invalid BER encoding supplied. 
   *         Shouldn't occur in normal operation, i.e., when valid responses 
   *         are received from devices.
   */
  public void extractValueFromBEREncoding( byte[] enc ) throws SnmpBadValueException
  {
    try
    {
      value = new BigInteger( enc );
    }
    catch( NumberFormatException e )
    {
      throw new SnmpBadValueException( " Integer: bad BER encoding supplied to set value " );
    }
  }




  /**
   * Method toString.
   *
   * @return the string
   */
  public String toString()
  {
    return value.toString();
  }




  public long toLong()
  {
    return value.longValue();
  }




  public int toInteger()
  {
    return (int)value.longValue();
  }
}
