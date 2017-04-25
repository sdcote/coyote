/*
 * $Id:$
 */
package coyote.commons.network.snmp;

import java.io.ByteArrayOutputStream;


/**
 * SnmpBerCodec defines methods for converting from ASN.1 BER encoding to 
 * SnmpObject subclasses. 
 * 
 * <p>The extraction process usually produces a tree structure of objects with 
 * an SnmpSequence object at the root; thisis the usual behavior when a 
 * received encoded message is received from an SNMP device.</p>
 */
public class SnmpBerCodec
{
  public static final byte SNMPINTEGER = 0x02;
  public static final byte SNMPBITSTRING = 0x03;
  public static final byte SNMPOCTETSTRING = 0x04;
  public static final byte SNMPNULL = 0x05;
  public static final byte SNMPOBJECTIDENTIFIER = 0x06;
  public static final byte SNMPSEQUENCE = 0x30;
  public static final byte SNMPIPADDRESS = (byte)0x40;
  public static final byte SNMPCOUNTER32 = (byte)0x41;
  public static final byte SNMPGAUGE32 = (byte)0x42;
  public static final byte SNMPTIMETICKS = (byte)0x43;
  public static final byte SNMPOPAQUE = (byte)0x44;
  public static final byte SNMPNSAPADDRESS = (byte)0x45;
  public static final byte SNMPCOUNTER64 = (byte)0x46;
  public static final byte SNMPUINTEGER32 = (byte)0x47;
  public static final byte SNMPGETREQUEST = (byte)0xA0;
  public static final byte SNMPGETNEXTREQUEST = (byte)0xA1;
  public static final byte SNMPGETRESPONSE = (byte)0xA2;
  public static final byte SNMPSETREQUEST = (byte)0xA3;
  public static final byte SNMPTRAP = (byte)0xA4;

  // SNMPv2p constants; unused!!

  public static final byte SNMPv2pCOMMUNICATION = (byte)0xA2;
  public static final byte SNMPv2pAUTHORIZEDMESSAGE = (byte)0xA1;
  public static final byte SNMPv2pENCRYPTEDMESSAGE = (byte)0xA1;
  public static final byte SNMPv2TRAP = (byte)0xA7;
  public static final byte SNMPv2pENCRYPTEDDATA = (byte)0xA1;
  public static final byte SNMPUNKNOWNOBJECT = 0x00;

  


  /**
   * Extracts an SNMP object given its type, length, value triple as an SnmpTlv object.
   * Called by SnmpObject subclass constructors.
   *
   * @param theTLV
   *
   * @throws SnmpBadValueException Indicates byte array in value field is 
   *         uninterprettable for specified SNMP object type.
   */
  public static SnmpObject extractEncoding( SnmpTlv theTLV ) throws SnmpBadValueException
  {

    switch( theTLV.tag )
    {

      case SNMPINTEGER:
      {
        return new SnmpInteger( theTLV.value );
      }

      case SNMPSEQUENCE:
      {
        return new SnmpSequence( theTLV.value );
      }

      case SNMPOBJECTIDENTIFIER:
      {
        return new SnmpObjectIdentifier( theTLV.value );
      }

      case SNMPOCTETSTRING:
      {
        return new SnmpOctetString( theTLV.value );
      }

      case SNMPBITSTRING:
      {
        return new SnmpBitString( theTLV.value );
      }

      case SNMPIPADDRESS:
      {
        return new SnmpIpAddress( theTLV.value );
      }

      case SNMPCOUNTER32:
      {
        return new SnmpCounter32( theTLV.value );
      }

      case SNMPGAUGE32:
      {
        return new SnmpGauge32( theTLV.value );
      }

      case SNMPTIMETICKS:
      {
        return new SnmpTimeTicks( theTLV.value );
      }

      case SNMPNSAPADDRESS:
      {
        return new SnmpNsapAddress( theTLV.value );
      }

      case SNMPCOUNTER64:
      {
        return new SnmpCounter64( theTLV.value );
      }

      case SNMPUINTEGER32:
      {
        return new SnmpUInteger32( theTLV.value );
      }

      case SNMPGETREQUEST:
      case SNMPGETNEXTREQUEST:
      case SNMPGETRESPONSE:
      case SNMPSETREQUEST:
      {
        return new SnmpPdu( theTLV.value, theTLV.tag );
      }

      case SNMPTRAP:
      {
        return new SnmpTrapPDU( theTLV.value );
      }

      case SNMPNULL:
      case SNMPOPAQUE:
      {
        return new SnmpNull();
      }

      default:
      {
        System.out.println( "Unrecognized tag" );

        // return new SnmpOctetString(theTLV.value);
        return new SnmpUnknownObject( theTLV.value );
      }
    }

  }




  /**
   * Extracts the type, length and value of the SNMP object whose BER encoding 
   * begins at the specified position in the given byte array.
   * 
   * <p>Refer to ISO 8825-1 for details.</p>
   *
   * @param payload
   * @param position
   *
   * @return Type Length Value
   */
  public static SnmpTlv extractNextTLV( byte[] payload, int position )
  {
    SnmpTlv retval = new SnmpTlv();
    int currentPos = position;

    // / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / /
    // single byte tag; extract value
    retval.tag = payload[currentPos];
    currentPos++;  // now at start of length info

    if(retval.getTagNumber() > 30)
    {
      // TODO support tag properly we only support single octet tags!
    }
    
    // / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / /
    // get length of data
    int dataLength;

    int unsignedValue = payload[currentPos];
    if( unsignedValue < 0 )
    {
      unsignedValue += 256;
    }

    // Determine short definite or long definite representation
    if( ( unsignedValue / 128 ) < 1 )
    {
      // short definite, single byte length; extract value
      dataLength = unsignedValue;
    }
    else
    {
      // long definite, multiple byte length; first byte's value (minus first 
      // bit) is # of length bytes
      int numBytes = ( unsignedValue % 128 );

      dataLength = 0;

      for( int i = 0; i < numBytes; i++ )
      {
        currentPos++;

        unsignedValue = payload[currentPos];

        if( unsignedValue < 0 )
        {
          unsignedValue += 256;
        }

        dataLength = dataLength * 256 + unsignedValue;
      }
    }

    currentPos++;  // now at start of data

    // set total length
    retval.totalLength = currentPos - position + dataLength;

    // / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / /
    // extract data portion
    ByteArrayOutputStream outBytes = new ByteArrayOutputStream();
    outBytes.write( payload, currentPos, dataLength );

    retval.value = outBytes.toByteArray();

    return retval;
  }




  /**
   * Utility function for encoding a length as a BER byte sequence
   *
   * @param length
   *
   * @return encoded length
   */
  public static byte[] encodeLength( int length )
  {
    ByteArrayOutputStream outBytes = new ByteArrayOutputStream();

    // see if can be represented in single byte
    // don't forget the first bit is the "long field test" bit!!
    if( length < 128 )
    {
      byte[] len = {(byte)length};
      outBytes.write( len, 0, 1 );
    }
    else
    {
      // too big for one byte
      // see how many are needed:
      int numBytes = 0;
      int temp = length;
      while( temp > 0 )
      {
        ++numBytes;

        temp = (int)Math.floor( temp / 256 );
      }

      byte num = (byte)numBytes;
      num += 128;  // set the "long format" bit

      outBytes.write( num );

      byte[] len = new byte[numBytes];
      for( int i = numBytes - 1; i >= 0; --i )
      {
        len[i] = (byte)( length % 256 );
        length = (int)Math.floor( length / 256 );
      }

      outBytes.write( len, 0, numBytes );

    }

    return outBytes.toByteArray();
  }

}
