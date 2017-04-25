/*
 * $Id:$
 */
package coyote.commons.network.snmp;

import java.net.InetAddress;


/**
 * IRequestListener is an interface that must be implemented by any class which 
 * wishes to act as a handler for request messages sent from remote SNMP 
 * management entities.
 * 
 * <p>The DefaultAgent class listens for request messages, and passes any it 
 * receives on to IRequestListener subclasses that have registered with it 
 * through its addRequestListener() method.</p>
 */
public interface IRequestListener
{

  /**
   * Handles Get- or Set- request messages. 
   * 
   * <p>The supplied request PDU may contain multiple OIDs; this method should 
   * process those OIDs it understands, and return an SnmpVarBindList 
   * containing those OIDs which it has handled and their corresponding values. 
   * The order of returned OID-value pairs is not important, as the 
   * DefaultAgent will order the information appropriately. Each implementer of 
   * IRequestListener will likely handle only a subset of the list of supplied 
   * OIDs; those OIDs which are not relevant to a particular listener should be 
   * ignored, to be handled by another IRequestListener.</p>
   * 
   * <p>If any OIDs remain unhandled after all listeners' processRequest() 
   * methods have been called, the DefaultAgent will return an appropriate 
   * error indication to the management entity.</p>
   *
   * @param requestPDU
   * @param communityName
   * @param source The source of the request to be used for access controls
   *
   * @throws SnmpGetException, SnmpSetException If a listener receives a 
   *         request for an OID which it is intended to handle, but there is a 
   *         problem with the request - e.g., a set-request for a value which 
   *         is read-only, or an incorrect value type for a set - the listener 
   *         should throw an SnmpGetException or SnmpSetException to indicate 
   *         the error. The exception should include both the index of the OID 
   *         in the list of supplied OIDs, as well as an error status code 
   *         (status values are provided as constants in the 
   *         SnmpRequestException class definition). The SnmpRequestException 
   *         class and subclasses provide constructors allowing the 
   *         specification of the error index and status code. Note that the 
   *         error index follows the SNMP convention of starting at 1, not 0: 
   *         thus if there is a problem with the first OID, the error index 
   *         should be 1. The SNMPAgentInterface will use the information in 
   *         the exception to communicate the error to the requesting 
   *         management entity. The community name should also be used to 
   *         determine if a request is valid for the supplied community name.
   * @throws SnmpSetException
   */
  public SnmpSequence processRequest( SnmpPdu requestPDU, String communityName, InetAddress source ) throws SnmpGetException, SnmpSetException;




  /**
   * Handles Get-Next- request messages. 
   * 
   * <p>The supplied request PDU may contain multiple OIDs; this* method should 
   * process those OIDs it understands, and return an SnmpVarBindList 
   * containing special variable pairs indicating those supplied OIDs which it 
   * has handled, i.e., it must indicate a supplied OID, the "next" OID, and 
   * the value of this next OID. To do this, the return value is a sequence of 
   * SnmpVariablePairs, in which the first component - the OID - is one of the 
   * supplied OIDs, and the second component - the value - is itself an 
   * SnmpVariablePair containing the "next" OID and its value:<pre>
   *
   * return value = sequence of SnmpVariablePair(original OID, SnmpVariablePair(following OID, value))
   *
   * </pre>In this way the DefaultAgent which calls this method will be able to 
   * determine to which the supplied OIDs each "next" OID corresponds.</p>
   *
   * <p>The order of returned "double" OID-(OID-value) pairs is not important, 
   * as the DefaultAgent will order the information appropriately in 
   * the response. Each implementer of IRequestListener will likely handle 
   * only a subset of the list of supplied OIDs; those OIDs which are not 
   * relevant to a particular listener should be ignored, to be handled by 
   * another IRequestListener.</p>
   * 
   * <p>If any OIDs remain unhandled after all listeners' processRequest() 
   * methods have been called, the DefaultAgent will return an appropriate 
   * error indication to the management entity.</p>
   *
   * @param requestPDU
   * @param communityName
   * @param source The source of the request to be used for access controls
   *
   * @throws SnmpGetException If a listener receives a request for an OID which
   *         it is intended to handle, but there is a problem with the request 
   *         - e.g., a get-next request for a value which is not readable for 
   *         the supplied community name - the listener should throw an 
   *         SnmpGetException to indicate the error. The exception should 
   *         include both the index of the OID in the list of supplied OIDs, as
   *         well as an error status code (status values are provided as 
   *         constants in the SnmpRequestException class definition). The 
   *         SnmpRequestException class and subclasses provide constructors 
   *         allowing the specification of the error index and status code. 
   *         Note that the error index follows the SNMP convention of starting 
   *         at 1, not 0: thus if there is a problem with the first OID, the 
   *         error index should be 1. The SnmpAgentInterface will use the 
   *         information in the exception to communicate the error to the 
   *         requesting management entity. The community name should also be 
   *         used to determine if a request is valid for the supplied community 
   *         name.
   */
  public SnmpSequence processGetNextRequest( SnmpPdu requestPDU, String communityName, InetAddress source ) throws SnmpGetException;

}
