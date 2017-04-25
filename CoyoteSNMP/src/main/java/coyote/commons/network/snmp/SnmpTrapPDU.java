/*
 * $Id:$
 */
package coyote.commons.network.snmp;

import java.math.BigInteger;
import java.util.Vector;


/**
 * The SnmpTrapPDU class represents an SNMP Trap PDU from RFC 1157, as 
 * indicated below. 
 * 
 * <p>This forms the payload of an SNMP Trap message.<pre>
 *
 * protocol data units
 *
 * PDUs ::=
 * CHOICE {
 * get-request
 * GetRequest-PDU,
 *
 * get-next-request
 * GetNextRequest-PDU,
 *
 * get-response
 * GetResponse-PDU,
 *
 * set-request
 * SetRequest-PDU,
 *
 * trap
 * Trap-PDU
 * }
 *
 * -- PDUs
 *
 * GetRequest-PDU ::=
 * [0]
 * IMPLICIT PDU
 *
 * GetNextRequest-PDU ::=
 * [1]
 * IMPLICIT PDU
 *
 * GetResponse-PDU ::=
 * [2]
 * IMPLICIT PDU
 *
 * SetRequest-PDU ::=
 * [3]
 * IMPLICIT PDU
 *
 * PDU ::=
 * SEQUENCE {
 * request-id
 * INTEGER,
 *
 * error-status      -- sometimes ignored
 * INTEGER {
 * noError(0),
 * tooBig(1),
 * noSuchName(2),
 * badValue(3),
 * readOnly(4),
 * genErr(5)
 * },
 *
 * error-index       -- sometimes ignored
 * INTEGER,
 *
 * variable-bindings -- values are sometimes ignored
 * VarBindList
 * }
 *
 *
 * Trap-PDU ::=
 * [4]
 * IMPLICIT SEQUENCE {
 * enterprise        -- type of object generating
 * -- trap, see sysObjectID in [5]
 *
 * OBJECT IDENTIFIER,
 *
 * agent-addr        -- address of object generating
 * NetworkAddress, -- trap
 *
 * generic-trap      -- generic trap type
 * INTEGER {
 * coldStart(0),
 * warmStart(1),
 * linkDown(2),
 * linkUp(3),
 * authenticationFailure(4),
 * egpNeighborLoss(5),
 * enterpriseSpecific(6)
 * },
 *
 * specific-trap  -- specific code, present even
 * INTEGER,   -- if generic-trap is not
 * -- enterpriseSpecific
 *
 * time-stamp     -- time elapsed between the last
 * TimeTicks, -- (re)initialization of the
 * network
 * -- entity and the generation of the
 * trap
 *
 * variable-bindings -- "interesting" information
 * VarBindList
 * }
 * -- variable bindings
 *
 * VarBind ::=
 * SEQUENCE {
 * name
 * ObjectName,
 *
 * value
 * ObjectSyntax
 * }
 *
 * VarBindList ::=
 * SEQUENCE OF
 * VarBind
 *
 * END</pre>
 * </p>
 */
public class SnmpTrapPDU extends SnmpSequence
{

  /**
   * Create a new Trap PDU of the specified type, with given request ID, error 
   * status, and error index, and containing the supplied SNMP sequence as data.
   *
   * @param enterpriseOID
   * @param agentAddress
   * @param genericTrap
   * @param specificTrap
   * @param timestamp
   * @param varList
   *
   * @throws SnmpBadValueException
   */
  public SnmpTrapPDU( SnmpObjectIdentifier enterpriseOID, SnmpIpAddress agentAddress, int genericTrap, int specificTrap, SnmpTimeTicks timestamp, SnmpSequence varList ) throws SnmpBadValueException
  {
    super();

    tag = SnmpBerCodec.SNMPTRAP;

    Vector contents = new Vector();

    contents.addElement( enterpriseOID );
    contents.addElement( agentAddress );
    contents.addElement( new SnmpInteger( genericTrap ) );
    contents.addElement( new SnmpInteger( specificTrap ) );
    contents.addElement( timestamp );
    contents.addElement( varList );

    this.setValue( contents );
  }




  /**
   * Create a new Trap PDU of the specified type, with given request ID, error 
   * status, and error index, and containing an empty SNMP sequence 
   * (VarBindList) as additional data.
   *
   * @param enterpriseOID
   * @param agentAddress
   * @param genericTrap
   * @param specificTrap
   * @param timestamp
   *
   * @throws SnmpBadValueException
   */
  public SnmpTrapPDU( SnmpObjectIdentifier enterpriseOID, SnmpIpAddress agentAddress, int genericTrap, int specificTrap, SnmpTimeTicks timestamp ) throws SnmpBadValueException
  {
    super();

    tag = SnmpBerCodec.SNMPTRAP;

    Vector contents = new Vector();

    contents.addElement( enterpriseOID );
    contents.addElement( agentAddress );
    contents.addElement( new SnmpInteger( genericTrap ) );
    contents.addElement( new SnmpInteger( specificTrap ) );
    contents.addElement( timestamp );
    contents.addElement( new SnmpVarBindList() );

    this.setValue( contents );
  }




  /**
   * Create a new PDU of the specified type from the supplied BER encoding.
   *
   * @param enc
   * 
   * @throws SnmpBadValueException Indicates invalid SNMP PDU encoding supplied 
   *         in enc.
   */
  protected SnmpTrapPDU( byte[] enc ) throws SnmpBadValueException
  {
    tag = SnmpBerCodec.SNMPTRAP;

    extractFromBEREncoding( enc );
  }




  /**
   * A utility method that extracts the variable binding list from the pdu. 
   * 
   * <p>Useful for retrieving the set of (object identifier, value) pairs 
   * returned in response to a request to an SNMP device. The variable binding 
   * list is just an SNMP sequence containing the identifier, value pairs.</p>
   * 
   * @see coyote.commons.network.snmp.SnmpVarBindList
   *
   * @return the sequence
   */
  public SnmpSequence getVarBindList()
  {
    Vector contents = (Vector)( this.getValue() );
    return (SnmpSequence)( contents.elementAt( 5 ) );
  }




  /**
   * A utility method that extracts the enterprise OID from this PDU.
   *
   * @return the identifier
   */
  public SnmpObjectIdentifier getEnterpriseOID()
  {
    Vector contents = (Vector)( this.getValue() );
    return (SnmpObjectIdentifier)contents.elementAt( 0 );
  }




  /**
   * A utility method that extracts the sending agent address this PDU.
   *
   * @return the address
   */
  public SnmpIpAddress getAgentAddress()
  {
    Vector contents = (Vector)( this.getValue() );
    return (SnmpIpAddress)contents.elementAt( 1 );
  }




  /**
   * A utility method that returns the generic trap code for this PDU.
   *
   * @return trap code
   */
  public int getGenericTrap()
  {
    Vector contents = (Vector)( this.getValue() );
    return ( (BigInteger)( (SnmpInteger)( contents.elementAt( 2 ) ) ).getValue() ).intValue();
  }




  /**
   * A utility method that returns the specific trap code for this PDU.
   *
   * @return the trap code
   */
  public int getSpecificTrap()
  {
    Vector contents = (Vector)( this.getValue() );
    return ( (BigInteger)( (SnmpInteger)( contents.elementAt( 3 ) ) ).getValue() ).intValue();
  }




  /**
   * A utility method that returns the timestamp for this PDU.
   *
   * @return the timestamp
   */
  public long getTimestamp()
  {
    Vector contents = (Vector)( this.getValue() );
    return ( (BigInteger)( (SnmpTimeTicks)( contents.elementAt( 4 ) ) ).getValue() ).longValue();
  }

}
