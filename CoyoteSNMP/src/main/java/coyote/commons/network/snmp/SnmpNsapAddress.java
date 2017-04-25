/*
 * $Id:$
 */
package coyote.commons.network.snmp;

import java.util.StringTokenizer;


/**
 * Defines class for holding physical 6-byte NSAP addresses.
 * 
 * <p>A Network Service Access Point (NSAP) addresses, defined in ISO/IEC 8348, 
 * are identifying labels for network endpoints used in OSI networking.</p>
 */
public class SnmpNsapAddress extends SnmpOctetString
{
  // length limited to 6 octets

  /**
   * Initialize address to 0.0.0.0.0.0.
   */
  public SnmpNsapAddress()
  {
    tag = SnmpBerCodec.SNMPNSAPADDRESS;

    // initialize to 0.0.0.0.0.0
    data = new byte[6];

    for( int i = 0; i < 6; i++ )
    {
      data[i] = 0;
    }
  }




  /**
   * Constructor SnmpNsapAddress.
   *
   * @param string
   *
   * @throws SnmpBadValueException
   */
  public SnmpNsapAddress( String string ) throws SnmpBadValueException
  {
    tag = SnmpBerCodec.SNMPNSAPADDRESS;

    data = parseNSAPAddress( string );
  }




  /**
   * Used to initialize from the BER encoding, as received in a response from
   * an SNMP device responding to an SNMPGetRequest, or from a supplied byte 
   * array containing the address components.
   *
   * @param enc
   * 
   * @throws SnmpBadValueException Indicates an invalid array supplied: must 
   * have length 6.
   */
  public SnmpNsapAddress( byte[] enc ) throws SnmpBadValueException
  {
    tag = SnmpBerCodec.SNMPNSAPADDRESS;

    if( enc.length == 6 )
    {
      data = enc;
    }
    else
    {
      throw new SnmpBadValueException( " NSAPAddress: bad BER encoding supplied to set value " );
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
    if( ( newAddress instanceof byte[] ) && ( ( (byte[])newAddress ).length == 6 ) )
    {
      data = (byte[])newAddress;
    }
    else if( newAddress instanceof String )
    {
      data = parseNSAPAddress( (String)newAddress );
    }
    else
    {
      throw new SnmpBadValueException( " NSAPAddress: bad length byte string supplied to set value " );
    }
  }




  /**
   * Return pretty-printed (dash-separated) address.
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

      returnString += Integer.toHexString( convert );

      for( int i = 1; i < data.length; i++ )
      {
        convert = data[i];

        if( convert < 0 )
        {
          convert += 256;
        }

        returnString += "-" + Integer.toHexString( convert );
      }
    }

    return returnString;
  }




  /**
   * Method parseNSAPAddress.
   *
   * @param addressString
   *
   * @return
   *
   * @throws SnmpBadValueException
   */
  private byte[] parseNSAPAddress( String addressString ) throws SnmpBadValueException
  {
    try
    {
      StringTokenizer st = new StringTokenizer( addressString, " .-" );  // break on spaces, dots or dashes
      int size = 0;

      while( st.hasMoreTokens() )
      {
        // figure out how many values are in string
        size++;

        st.nextToken();
      }

      if( size != 6 )
      {
        throw new SnmpBadValueException( " NSAPAddress: wrong number of components supplied to set value " );
      }

      byte[] returnBytes = new byte[size];

      st = new StringTokenizer( addressString, " .-" );

      for( int i = 0; i < size; i++ )
      {
        int addressComponent = ( Integer.parseInt( st.nextToken(), 16 ) );
        if( ( addressComponent < 0 ) || ( addressComponent > 255 ) )
        {
          throw new SnmpBadValueException( " NSAPAddress: invalid component supplied to set value " );
        }

        returnBytes[i] = (byte)addressComponent;
      }

      return returnBytes;

    }
    catch( NumberFormatException e )
    {
      throw new SnmpBadValueException( " NSAPAddress: invalid component supplied to set value " );
    }

  }

}
