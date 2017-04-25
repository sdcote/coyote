/*
 * $Id:$
 */
package coyote.commons.network.snmp;

import java.io.ByteArrayOutputStream;
import java.util.Enumeration;
import java.util.Vector;


/**
 * Represents a sequence of other SNMP data types.
 * 
 * <p>Virtually all compound structures are subclasses of SnmpSequence - for 
 * example, the top-level SnmpMessage, and the SnmpPdu it contains, are both 
 * just specializations of SnmpSequence. Sequences are frequently nested within 
 * other sequences.</p>
 */
public class SnmpSequence extends SnmpObject
{
  /** the vector  of whatever is in sequence */
  protected Vector sequence;

  protected byte tag = SnmpBerCodec.SNMPSEQUENCE;




  /**
   * Create a new empty sequence.
   */
  public SnmpSequence()
  {
    sequence = new Vector();
  }




  /**
   * Create a new SNMP sequence from the supplied Vector of SnmpObjects.
   *
   * @param v
   * 
   * @throws SnmpBadValueException Thrown if non-SNMP object supplied in the
   *         given Vector.
   */
  public SnmpSequence( Vector v ) throws SnmpBadValueException
  {
    Enumeration e = v.elements();

    while( e.hasMoreElements() )
    {
      if( !( e.nextElement() instanceof SnmpObject ) )
      {
        throw new SnmpBadValueException( "Non-SnmpObject supplied to SnmpSequence." );
      }
    }

    sequence = v;
  }




  /**
   * Construct an SnmpMessage from a received ASN.1 byte representation.
   *
   * @param enc The value of of a BER TLV
   * 
   * @throws SnmpBadValueException Indicates invalid SNMP sequence encoding 
   *         supplied.
   */
  protected SnmpSequence( byte[] enc ) throws SnmpBadValueException
  {
    extractFromBEREncoding( enc );
  }




  /**
   * Returns a Vector containing the SNMPObjects in the sequence.
   *
   * @return the object value
   */
  public Object getValue()
  {
    return sequence;
  }




  /**
   * Used to set the contained SNMP objects from a supplied Vector.
   *
   * @param newSequence
   * 
   * @throws SnmpBadValueException Indicates an incorrect object type supplied, 
   *         or that the supplied Vector contains non-SNMPObjects.
   */
  public void setValue( Object newSequence ) throws SnmpBadValueException
  {
    if( newSequence instanceof Vector )
    {
      // check that all objects in vector are SNMPObjects
      Enumeration e = ( (Vector)newSequence ).elements();

      while( e.hasMoreElements() )
      {
        if( !( e.nextElement() instanceof SnmpObject ) )
        {
          throw new SnmpBadValueException( "Non-SnmpObject supplied to SnmpSequence." );
        }
      }

      this.sequence = (Vector)newSequence;
    }
    else
    {
      throw new SnmpBadValueException( " Sequence: bad object supplied to set value " );
    }
  }




  /**
   * Return the number of SNMPObjects contained in the sequence.
   *
   * @return the size of the sequence
   */
  public int size()
  {
    return sequence.size();
  }




  /**
   * Add the SNMP object to the end of the sequence.
   *
   * @param obj The object to add to this sequence
   * 
   * @throws SnmpBadValueException Relevant only in subclasses
   */
  public void addSnmpObject( SnmpObject obj ) throws SnmpBadValueException
  {
    sequence.insertElementAt( obj, sequence.size() );
  }




  /**
   * Return the SNMP object at the specified index. Indices are 0-based.
   *
   * @param index
   *
   * @return the object at that index
   */
  public SnmpObject getSnmpObjectAt( int index )
  {
    return (SnmpObject)( sequence.elementAt( index ) );
  }




  /**
   * Return the BER encoding for the sequence.
   *
   * @return
   */
  protected byte[] getBEREncoding()
  {
    ByteArrayOutputStream outBytes = new ByteArrayOutputStream();

    // recursively write contents of Vector
    byte[] data = encodeVector();

    // calculate encoding for length of data
    byte[] len = SnmpBerCodec.encodeLength( data.length );

    // encode T,L,V info
    outBytes.write( tag );
    outBytes.write( len, 0, len.length );
    outBytes.write( data, 0, data.length );

    return outBytes.toByteArray();
  }




  /**
   * Method encodeVector.
   *
   * @return
   */
  private byte[] encodeVector()
  {
    ByteArrayOutputStream outBytes = new ByteArrayOutputStream();

    int numElements = sequence.size();
    for( int i = 0; i < numElements; ++i )
    {
      byte[] nextBytes = ( (SnmpObject)( sequence.elementAt( i ) ) ).getBEREncoding();
      outBytes.write( nextBytes, 0, nextBytes.length );
    }

    return outBytes.toByteArray();
  }




  /**
   * Method extractFromBEREncoding.
   *
   * @param enc
   *
   * @throws SnmpBadValueException
   */
  protected void extractFromBEREncoding( byte[] enc ) throws SnmpBadValueException
  {
    Vector newVector = new Vector();

    int totalLength = enc.length;
    int position = 0;

    while( position < totalLength )
    {
      SnmpTlv nextTLV = SnmpBerCodec.extractNextTLV( enc, position );
      newVector.insertElementAt( SnmpBerCodec.extractEncoding( nextTLV ), newVector.size() );

      position += nextTLV.totalLength;
    }

    sequence = newVector;

  }




  /**
   * Return a sequence of representations of the contained objects, separated 
   * by spaces and enclosed in parentheses.
   *
   * @return the string
   */
  public String toString()
  {
    String valueString = new String( "(" );

    for( int i = 0; i < sequence.size(); ++i )
    {
      valueString += " " + ( (SnmpObject)sequence.elementAt( i ) ).toString() + " ";
    }

    valueString += ")";

    return valueString;
  }

}
