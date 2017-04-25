/*
 * $Id:$
 */
package coyote.commons.network.snmp;

/**
 * Abstract base class of all SNMP data type classes.
 */
public abstract class SnmpObject
{

  /**
   * Must return a Java object appropriate to represent the value/data 
   * contained in the SNMP object
   *
   * @return the object value
   */
  public abstract Object getValue();




  /**
   * Must set the value of the SNMP object when supplied with an appropriate
   * Java object containing an appropriate value.
   *
   * @param o
   *
   * @throws SnmpBadValueException
   */
  public abstract void setValue( Object o ) throws SnmpBadValueException;




  /**
   * Should return an appropriate human-readable representation of the stored 
   * value.
   *
   * @return the string representation of ths object
   */
  public abstract String toString();




  /**
   * Must return the BER byte encoding (type, length, value) of the SNMP 
   * object.
   *
   * @return
   */
  protected abstract byte[] getBEREncoding();

}