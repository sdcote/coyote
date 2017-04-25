/*
 * $Id:$
 */
package coyote.commons.network.snmp;

/**
 * Exception thrown whenever attempt made to create SnmpObject subclass with 
 * inappropriate data, or to set its value with inappropriate data.
 */
public class SnmpBadValueException extends Exception
{

  /**
   * Constructor SnmpBadValueException.
   */
  public SnmpBadValueException()
  {
    super();
  }




  /**
   * Create exception with message string.
   *
   * @param s
   */
  public SnmpBadValueException( String s )
  {
    super( s );
  }

}