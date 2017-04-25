/*
 * $Id:$
 */
package coyote.commons.network.snmp;

import java.util.Vector;


/**
 * The SnmpVarBindList class is a specialization of SnmpSequence that contains 
 * a list of SnmpVariablePair objects.
 * 
 * <p>variable bindings<pre>
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
 * END
 *</pre>
 *</p>
 * @see coyote.commons.network.snmp.SnmpVariablePair
 */
public class SnmpVarBindList extends SnmpSequence
{

  /**
   * Create a new empty variable binding list.
   */
  public SnmpVarBindList()
  {
    super();
  }




  /**
   * Return the variable pairs in the list, separated by newlines.
   *
   * @return the string
   */
  public String toString()
  {
    Vector sequence = (Vector)( this.getValue() );

    String valueString = new String();

    for( int i = 0; i < sequence.size(); ++i )
    {
      valueString += ( (SnmpObject)sequence.elementAt( i ) ).toString() + "\n";
    }

    return valueString;
  }

}