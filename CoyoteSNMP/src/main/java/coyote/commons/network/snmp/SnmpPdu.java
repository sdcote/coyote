/*
 * $Id:$
 */
package coyote.commons.network.snmp;

import java.math.BigInteger;
import java.util.Vector;


/**
 * The SnmpPdu class represents an SNMP PDU from RFC 1157, as indicated below. 
 * 
 * <p>This forms the payload of an SNMP message.</p>
 *
 * <p>protocol data units<pre>
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
 * -- variable bindings
 *
 * VarBind ::=
 *   SEQUENCE {
 *              name
 *              ObjectName,
 *
 *              value
 *              ObjectSyntax
 *            }
 *
 * VarBindList ::=
 *   SEQUENCE OF
 *   VarBind
 *
 * END</pre>
 * </p>
 */
public class SnmpPdu extends SnmpSequence
{

  /**
   * Create a new PDU of the specified type, with given request ID, error 
   * status, and error index, and containing the supplied SNMP sequence as data.
   *
   * @param pduType
   * @param requestID
   * @param errorStatus
   * @param errorIndex
   * @param varList
   *
   * @throws SnmpBadValueException
   */
  public SnmpPdu( byte pduType, int requestID, int errorStatus, int errorIndex, SnmpSequence varList ) throws SnmpBadValueException
  {
    super();

    Vector contents = new Vector();
    tag = pduType;

    contents.insertElementAt( new SnmpInteger( requestID ), 0 );
    contents.insertElementAt( new SnmpInteger( errorStatus ), 1 );
    contents.insertElementAt( new SnmpInteger( errorIndex ), 2 );
    contents.insertElementAt( varList, 3 );
    this.setValue( contents );
  }




  /**
   * Create a new PDU of the specified type from the supplied BER encoding.
   *
   * @param enc
   * @param pduType
   * 
   * @throws SnmpBadValueException Indicates invalid SNMP PDU encoding supplied 
   *         in enc.
   */
  protected SnmpPdu( byte[] enc, byte pduType ) throws SnmpBadValueException
  {
    tag = pduType;

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
    return (SnmpSequence)( contents.elementAt( 3 ) );
  }




  /**
   * A utility method that extracts the request ID number from this PDU.
   *
   * @return the ID as an int
   */
  public int getRequestID()
  {
    Vector contents = (Vector)( this.getValue() );
    return ( (BigInteger)( (SnmpInteger)( contents.elementAt( 0 ) ) ).getValue() ).intValue();
  }




  /**
   * A utility method that extracts the error status for this PDU; if nonzero, 
   * can get index of problematic variable using getErrorIndex().
   *
   * @return the status
   */
  public int getErrorStatus()
  {
    Vector contents = (Vector)( this.getValue() );
    return ( (BigInteger)( (SnmpInteger)( contents.elementAt( 1 ) ) ).getValue() ).intValue();
  }




  /**
   * A utility method that returns the error index for this PDU, identifying 
   * the problematic variable.
   *
   * @return index of the error
   */
  public int getErrorIndex()
  {
    Vector contents = (Vector)( this.getValue() );
    return ( (BigInteger)( (SnmpInteger)( contents.elementAt( 2 ) ) ).getValue() ).intValue();
  }




  /**
   * A utility method that returns the PDU type of this PDU.
   *
   * @return the typ of the PDU(0-255)
   */
  public byte getPDUType()
  {
    return tag;
  }

}
