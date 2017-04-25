/*
 * $Id:$
 */
package coyote.commons.network.snmp;

/**
 * Exception thrown when attempt to set the value of an SNMP OID on a device 
 * fails. 
 * 
 * <p>Reason could be that specified variable not supported by device, or that 
 * supplied community name has insufficient privileges.</p>
 */
public class SnmpSetException extends SnmpRequestException
{

  /*
   * public SnmpSetException()
   * {
   * super();
   * }
   */
  /**
   * Create exception with message string.
   *
   * @param errorIndex
   * @param errorStatus
   */
  /*
   * public SnmpSetException(String s)
   * {
   * super(s);
   * }
   */
  /**
   * Create exception with errorIndex and errorStatus
   */
  public SnmpSetException( int errorIndex, int errorStatus )
  {
    super( errorIndex, errorStatus );
  }




  /**
   * Create exception with errorIndex, errorStatus and message string
   *
   * @param message
   * @param errorIndex
   * @param errorStatus
   */
  public SnmpSetException( String message, int errorIndex, int errorStatus )
  {
    super( message, errorIndex, errorStatus );
  }

}