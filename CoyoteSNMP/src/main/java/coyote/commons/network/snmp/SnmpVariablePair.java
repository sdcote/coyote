/*
 * $Id:$
 */
package coyote.commons.network.snmp;

import java.util.Vector;


/**
 * The SnmpVariablePair class implements the VarBind specification detailed 
 * below from RFC 1157.
 * 
 * <p>It is a specialization of SnmpSequence, defining a 2-element sequence 
 * containing a single (object identifier, value) pair. Note that the values 
 * are themselves SnmpObjects.</p>
 *
 *<p><pre>
 * variable bindings
 *
 * VarBind ::=
 * SEQUENCE {
 * name
 * ObjectName,
 *
 * value
 * ObjectSyntax
 * }
 *</pre></p>
 */
public class SnmpVariablePair extends SnmpSequence
{

  /**
   * Create a new variable pair having the supplied object identifier and 
   * value.
   *
   * @param objectID
   * @param value
   *
   * @throws SnmpBadValueException
   */
  public SnmpVariablePair( SnmpObjectIdentifier objectID, SnmpObject value ) throws SnmpBadValueException
  {
    super();

    Vector contents = new Vector();
    contents.insertElementAt( objectID, 0 );
    contents.insertElementAt( value, 1 );
    this.setValue( contents );
  }

}
