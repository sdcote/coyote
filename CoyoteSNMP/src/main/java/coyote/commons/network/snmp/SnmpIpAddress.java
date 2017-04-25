/*
 * $Id:$
 */
package coyote.commons.network.snmp;

import java.util.StringTokenizer;


/**
 * Class to hold IP addresses; special case of SNMP Octet String.
 */
public class SnmpIpAddress extends SnmpOctetString
{
  // length limited to 4 octets

  /**
   * Initialize to 0.0.0.0
   */
  public SnmpIpAddress()
  {
    // initialize to 0.0.0.0
    tag = SnmpBerCodec.SNMPIPADDRESS;
    data = new byte[4];

    for( int i = 0; i < 4; i++ )
    {
      data[i] = 0;
    }
  }




  /**
   * Used to initialize from a string containing a standard "dotted" IP address.
   *
   * @param string
   * 
   * @throws SnmpBadValueException Indicates an invalid string supplied: more 
   * than 4 components, component values not between 0 and 255, etc.
   */
  public SnmpIpAddress( String string ) throws SnmpBadValueException
  {
    tag = SnmpBerCodec.SNMPIPADDRESS;
    this.data = parseIPAddress( string );
  }




  /**
   * Used to initialize from the BER encoding, as received in a response from
   * an SNMP device responding to an SNMPGetRequest, or from a supplied byte 
   * array containing the address components.
   *
   * @param enc
   * 
   * @throws SnmpBadValueException Indicates an invalid array supplied: must 
   * have length 4.
   */
  public SnmpIpAddress( byte[] enc ) throws SnmpBadValueException
  {

    tag = SnmpBerCodec.SNMPIPADDRESS;

    if( enc.length == 4 )
    {
      data = enc;
    }
    else
    {
      throw new SnmpBadValueException( " IPAddress: bad BER encoding supplied to set value " );
    }
  }




  /**
   * Used to set the value from a byte array containing the address.
   *
   * @param newAddress
   * 
   * @throws SnmpBadValueException Indicates an incorrect object type supplied, 
   * or array of incorrect size.
   */
  public void setValue( Object newAddress ) throws SnmpBadValueException
  {
    if( ( newAddress instanceof byte[] ) && ( ( (byte[])newAddress ).length == 4 ) )
    {
      data = (byte[])newAddress;
    }
    else if( newAddress instanceof String )
    {
      data = parseIPAddress( (String)newAddress );
    }
    else
    {
      throw new SnmpBadValueException( " IPAddress: bad data supplied to set value " );
    }
  }




  /**
   * Return pretty-printed IP address.
   *
   * @return the string
   */
  public String toString()
  {
    String returnString = new String();

    if( data.length > 0 )
    {
      int convert = data[0];
      if( convert < 0 )
      {
        convert += 256;
      }

      returnString += convert;

      for( int i = 1; i < data.length; i++ )
      {
        convert = data[i];

        if( convert < 0 )
        {
          convert += 256;
        }

        returnString += "." + convert;
      }
    }

    return returnString;
  }




  /**
   * Method parseIPAddress.
   *
   * @param addressString
   *
   * @return
   *
   * @throws SnmpBadValueException
   */
  private byte[] parseIPAddress( String addressString ) throws SnmpBadValueException
  {
    try
    {
      StringTokenizer st = new StringTokenizer( addressString, " ." );
      int size = 0;

      while( st.hasMoreTokens() )
      {
        // figure out how many values are in string
        size++;

        st.nextToken();
      }

      if( size != 4 )
      {
        throw new SnmpBadValueException( " IPAddress: wrong number of components supplied to set value " );
      }

      byte[] returnBytes = new byte[size];

      st = new StringTokenizer( addressString, " ." );

      for( int i = 0; i < size; i++ )
      {
        int addressComponent = ( Integer.parseInt( st.nextToken() ) );
        if( ( addressComponent < 0 ) || ( addressComponent > 255 ) )
        {
          throw new SnmpBadValueException( " IPAddress: invalid component supplied to set value " );
        }

        returnBytes[i] = (byte)addressComponent;
      }

      return returnBytes;

    }
    catch( NumberFormatException e )
    {
      throw new SnmpBadValueException( " IPAddress: invalid component supplied to set value " );
    }

  }

}
