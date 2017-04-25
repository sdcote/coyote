/*
 * $Id:$
 */
package coyote.commons.network.snmp;

import java.util.Vector;


/**
 * Defines the SnmpMessage class as a special case of SnmpSequence. 
 * 
 * <p>Defines a top-level SNMP message, as per the following definitions from 
 * RFC 1157 and RFC 1901.
 *<pre>
 *
 * RFC1157-SNMP DEFINITIONS
 *
 * IMPORTS FROM RFC1155-SMI;
 *
 * -- top-level message
 *
 * Message ::=
 * SEQUENCE {
 * version        -- version-1 for this RFC
 * INTEGER {
 * version-1(0)
 * },
 *
 * community      -- community name
 * OCTET STRING,
 *
 * data           -- e.g., PDUs if trivial
 * ANY        -- authentication is being used
 * }
 *
 *
 * -- From RFC 1901:
 *
 * COMMUNITY-BASED-SNMPv2 DEFINITIONS ::= BEGIN
 *
 * -- top-level message
 *
 * Message ::=
 * SEQUENCE {
 * version
 * INTEGER {
 * version(1)  -- modified from RFC 1157
 * },
 *
 * community           -- community name
 * OCTET STRING,
 *
 * data                -- PDUs as defined in [4]
 * ANY
 * }
 * }
 *
 * END
 *</pre></p>
 */
public class SnmpMessage extends SnmpSequence
{

  /**
   * Create an SNMP message with specified version, community, and pdu.
   * 
   * <p>Use version = 0 for SNMP version 1, or version = 1 for enhanced 
   * capapbilities provided through RFC 1157.
   *
   * @param version
   * @param community
   * @param pdu
   */
  public SnmpMessage( int version, String community, SnmpPdu pdu )
  {
    super();

    Vector contents = new Vector();
    contents.insertElementAt( new SnmpInteger( version ), 0 );
    contents.insertElementAt( new SnmpOctetString( community ), 1 );
    contents.insertElementAt( pdu, 2 );

    try
    {
      this.setValue( contents );
    }
    catch( SnmpBadValueException e )
    {
      // can't happen! all supplied Vector elements are SNMP Object subclasses
    }
  }




  /**
   * Create an SNMP message with specified version, community, and trap pdu.
   * 
   * <p>Use version = 0 for SNMP version 1, or version = 1 for enhanced 
   * capapbilities provided through RFC 1157.
   *
   * @param version
   * @param community
   * @param pdu
   */
  public SnmpMessage( int version, String community, SnmpTrapPDU pdu )
  {
    super();

    Vector contents = new Vector();
    contents.insertElementAt( new SnmpInteger( version ), 0 );
    contents.insertElementAt( new SnmpOctetString( community ), 1 );
    contents.insertElementAt( pdu, 2 );

    try
    {
      this.setValue( contents );
    }
    catch( SnmpBadValueException ignore )
    {
      // can't happen! all supplied Vector elements are SNMP Object subclasses
    }
  }




  /**
   * Construct an SnmpMessage from a received ASN.1 byte representation.
   *
   * @param data
   * 
   * @throws SnmpBadValueException Indicates invalid SNMP message encoding 
   *         supplied.
   */
  protected SnmpMessage( byte[] data ) throws SnmpBadValueException
  {
    super( data );
  }




  /**
   * Utility method which returns the PDU contained in the SNMP message.
   *  
   * <p>The pdu is the third component of the sequence, after the version and 
   * community name.</p>
   *
   * @return the PDU
   *
   * @throws SnmpBadValueException
   */
  public SnmpPdu getPDU() throws SnmpBadValueException
  {
    Vector contents = (Vector)( this.getValue() );
    Object pdu = contents.elementAt( 2 );

    if( !( pdu instanceof SnmpPdu ) )
    {
      throw new SnmpBadValueException( "Wrong PDU type in message: expected SnmpPdu, have " + pdu.getClass().toString() );
    }

    return (SnmpPdu)pdu;
  }




  /**
   * Utility method which returns the PDU contained in the SNMP message as an 
   * SnmpTrapPDU. 
   * 
   * <p>The pdu is the third component of the sequence, after the version and 
   * community name.</p>
   *
   * @return the Trap PDU
   *
   * @throws SnmpBadValueException
   */
  public SnmpTrapPDU getTrapPDU() throws SnmpBadValueException
  {
    Vector contents = (Vector)( this.getValue() );
    Object pdu = contents.elementAt( 2 );

    if( !( pdu instanceof SnmpTrapPDU ) )
    {
      throw new SnmpBadValueException( "Wrong PDU type in message: expected SnmpTrapPDU, have " + pdu.getClass().toString() );
    }

    return (SnmpTrapPDU)pdu;
  }




  /**
   * Utility method which returns the community name contained in the SNMP 
   * message. 
   * 
   * <p>The community name is the second component of the sequence, after the 
   * version.</p>
   *
   * @return the community name
   *
   * @throws SnmpBadValueException
   */
  public String getCommunityName() throws SnmpBadValueException
  {
    Vector contents = (Vector)( this.getValue() );
    Object communityName = contents.elementAt( 1 );

    if( !( communityName instanceof SnmpOctetString ) )
    {
      throw new SnmpBadValueException( "Wrong SNMP type for community name in message: expected SnmpOctetString, have " + communityName.getClass().toString() );
    }

    return ( (SnmpOctetString)communityName ).toString();
  }




  public int getVersion() throws SnmpBadValueException
  {
    Vector contents = (Vector)( this.getValue() );
    Object version = contents.elementAt( 0 );

    if( !( version instanceof SnmpInteger ) )
    {
      throw new SnmpBadValueException( "Wrong SNMP type for version in message: expected SnmpInteger, have " + version.getClass().toString() );
    }

    return ( (SnmpInteger)version ).toInteger();
  }
}
