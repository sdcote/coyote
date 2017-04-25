/*
 * $Id:$
 */
package coyote.commons.network.snmp;

/**
 * Exception thrown when attempt to set or get value of SNMP OID fails. 
 * 
 * <p>Reason could be that specified variable not supported by device, or that 
 * supplied community name has insufficient privileges.</p>
 */
public class SnmpException extends Exception
{

  /**
   * Constructor SnmpException.
   */
  public SnmpException()
  {
    super();
  }




  /**
   * Create exception with message string.
   *
   * @param s
   */
  public SnmpException( String s )
  {
    super( s );
  }

}