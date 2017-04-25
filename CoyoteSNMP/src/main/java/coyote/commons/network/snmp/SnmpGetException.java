/*
 * $Id:$
 */
package coyote.commons.network.snmp;

/**
 * Exception thrown when attempt to get value of SNMP OID from device fails. 
 * 
 * <p>Reason could be that specified variable not supported by device, or that 
 * supplied community name has insufficient privileges.</p>
 */
public class SnmpGetException extends SnmpRequestException
{
  /*
   * public SnmpGetException()
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
   * public SnmpGetException(String s)
   * {
   * super(s);
   * }
   */
  /**
   * Create exception with errorIndex and errorStatus
   */
  public SnmpGetException( int errorIndex, int errorStatus )
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
  public SnmpGetException( String message, int errorIndex, int errorStatus )
  {
    super( message, errorIndex, errorStatus );
  }

}
