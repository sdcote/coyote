/*
 * $Id:$
 */
package coyote.commons.network.snmp;

import java.io.ByteArrayOutputStream;
import java.util.StringTokenizer;


/**
 * Class representing ASN.1 object identifiers. 
 * 
 * <p>These are unbounded sequences (arrays) of natural numbers, written as 
 * dot-separated strings.</p>
 */
public class SnmpObjectIdentifier extends SnmpObject
{
  int[] digits; // array of integers

  protected byte tag = SnmpBerCodec.SNMPOBJECTIDENTIFIER;




  /**
   * Create a new empty object identifier (0-length array).
   */
  public SnmpObjectIdentifier()
  {
    digits = new int[0];
  }




  /**
   * Create a new object identifier from the supplied string of dot-separated 
   * non-egative decimal integer values.
   *
   * @param digitString
   * 
   * @throws SnmpBadValueException Indicates incorrectly-formatted string 
   *         supplied.
   */
  public SnmpObjectIdentifier( String digitString ) throws SnmpBadValueException
  {
    convertDigitString( digitString );
  }




  /**
   * Create a new object identifier from the supplied array of nonegative
   * integer values.
   *
   * @param digits
   * 
   * @throws SnmpBadValueException Negative value(s) supplied.
   */
  public SnmpObjectIdentifier( int[] digits ) throws SnmpBadValueException
  {
    for( int i = 0; i < digits.length; i++ )
    {
      if( digits[i] < 0 )
      {
        throw new SnmpBadValueException( "Negative value supplied for SnmpObjectIdentifier." );
      }
    }

    this.digits = digits;
  }




  /**
   * Used to initialize from the BER encoding, as received in a response from 
   * an SNMP device responding to an SNMPGetRequest.
   *
   * @param enc
   * 
   * @throws SnmpBadValueException Indicates an invalid BER encoding supplied. Shouldn't
   * occur in normal operation, i.e., when valid responses are received from devices.
   */
  protected SnmpObjectIdentifier( byte[] enc ) throws SnmpBadValueException
  {
    extractFromBEREncoding( enc );
  }




  /**
   * Return array of integers corresponding to components of identifier.
   *
   * @return the object value
   */
  public Object getValue()
  {
    return digits;
  }




  /**
   * Used to set the value from an integer array containing the identifier 
   * components, or from a String containing a dot-separated sequence of 
   * non-egative values.
   *
   * @param digits
   * 
   * @throws SnmpBadValueException Indicates an incorrect object type supplied, 
   *         or negative array elements, or an incorrectly formatted String.
   */
  public void setValue( Object digits ) throws SnmpBadValueException
  {
    if( digits instanceof int[] )
    {
      for( int i = 0; i < ( (int[])digits ).length; i++ )
      {
        if( ( (int[])digits )[i] < 0 )
        {
          throw new SnmpBadValueException( "Negative value supplied for SnmpObjectIdentifier." );
        }
      }

      this.digits = (int[])digits;
    }
    else if( digits instanceof String )
    {
      convertDigitString( (String)digits );
    }
    else
    {
      throw new SnmpBadValueException( " Object Identifier: bad object supplied to set value " );
    }
  }




  /**
   * Return BER encoding for this object identifier.
   *
   * @return
   */
  protected byte[] getBEREncoding()
  {
    ByteArrayOutputStream outBytes = new ByteArrayOutputStream();

    byte type = SnmpBerCodec.SNMPOBJECTIDENTIFIER;

    // write contents of array of values
    byte[] data = encodeArray();

    // calculate encoding for length of data
    byte[] len = SnmpBerCodec.encodeLength( data.length );

    // encode T,L,V info
    outBytes.write( type );
    outBytes.write( len, 0, len.length );
    outBytes.write( data, 0, data.length );

    return outBytes.toByteArray();
  }




  /**
   * Method encodeArray.
   *
   * @return
   */
  private byte[] encodeArray()
  {
    ByteArrayOutputStream outBytes = new ByteArrayOutputStream();

    int numElements = digits.length;

    // encode first two identifier digits as one byte, using the 40*x + y rule;
    // of course, if only one element, just use 40*x; if none, do nothing
    if( numElements >= 2 )
    {
      outBytes.write( (byte)( 40 * digits[0] + digits[1] ) );
    }
    else if( numElements == 1 )
    {
      outBytes.write( (byte)( 40 * digits[0] ) );
    }

    for( int i = 2; i < numElements; ++i )
    {
      byte[] nextBytes = encodeValue( digits[i] );
      outBytes.write( nextBytes, 0, nextBytes.length );
    }

    return outBytes.toByteArray();
  }




  /**
   * Method encodeValue.
   *
   * @param v
   *
   * @return
   */
  private byte[] encodeValue( int v )
  {
    // see how many bytes are needed: each value uses just
    // 7 bits of each byte, with high-order bit functioning as
    // a continuation marker
    int numBytes = 0;
    int temp = v;

    do
    {
      ++numBytes;

      temp = (int)Math.floor( temp / 128 );
    }
    while( temp > 0 );

    byte[] enc = new byte[numBytes];
    // encode lowest-order byte, without setting high bit
    enc[numBytes - 1] = (byte)( v % 128 );
    v = (int)Math.floor( v / 128 );

    // .encode other bytes with high bit set
    for( int i = numBytes - 2; i >= 0; --i )
    {
      enc[i] = (byte)( ( v % 128 ) + 128 );
      v = (int)Math.floor( v / 128 );
    }

    return enc;
  }




  /**
   * Method convertDigitString.
   *
   * @param digitString
   *
   * @throws SnmpBadValueException
   */
  void convertDigitString( String digitString ) throws SnmpBadValueException
  {
    try
    {
      StringTokenizer st = new StringTokenizer( digitString, " ." );
      int size = 0;

      while( st.hasMoreTokens() )
      {
        // figure out how many values are in string
        size++;

        st.nextToken();
      }

      int[] returnDigits = new int[size];

      st = new StringTokenizer( digitString, " ." );

      for( int i = 0; i < size; i++ )
      {
        returnDigits[i] = Integer.parseInt( st.nextToken() );

        if( returnDigits[i] < 0 )
        {
          throw new SnmpBadValueException( " Object Identifier: bad string supplied to set value " );
        }
      }

      digits = returnDigits;

    }
    catch( NumberFormatException e )
    {
      throw new SnmpBadValueException( " Object Identifier: bad string supplied for object identifier value " );
    }

  }




  /**
   * Method extractFromBEREncoding.
   *
   * @param enc
   *
   * @throws SnmpBadValueException
   */
  private void extractFromBEREncoding( byte[] enc ) throws SnmpBadValueException
  {
    // note: masks must be ints; byte internal representation issue(?)
    int bitTest = 0x80;  // test for leading 1
    int highBitMask = 0x7F;  // mask out high bit for value

    // first, compute number of "digits";
    // will just be number of bytes with leading 0's
    int numInts = 0;
    for( int i = 0; i < enc.length; i++ )
    {
      if( ( enc[i] & bitTest ) == 0 )  // high-order bit not set; count
      {
        numInts++;
      }
    }

    if( numInts > 0 )
    {
      // create new int array to hold digits; since first value is 40*x + y,
      // need one extra entry in array to hold this.
      digits = new int[numInts + 1];

      int currentByte = -1;  // will be incremented to 0

      int value = 0;

      // read in values 'til get leading 0 in byte
      do
      {
        currentByte++;

        value = value * 128 + ( enc[currentByte] & highBitMask );
      }
      while( ( enc[currentByte] & bitTest ) > 0 );  // implies high bit set!

      // now handle 40a + b
      digits[0] = (int)Math.floor( value / 40 );
      digits[1] = value % 40;

      // now read in rest!
      for( int i = 2; i < numInts + 1; i++ )
      {
        // read in values 'til get leading 0 in byte
        value = 0;

        do
        {
          currentByte++;

          value = value * 128 + ( enc[currentByte] & highBitMask );
        }
        while( ( enc[currentByte] & bitTest ) > 0 );

        digits[i] = value;
      }

    }
    else
    {
      // no digits; create empty digit array
      digits = new int[0];
    }

  }




  /**
   * Test two obeject identifiers for equality.
   *
   * @param other
   *
   * @return true if this is equal with that, false otherwise
   */
  public boolean equals( SnmpObjectIdentifier other )
  {
    int[] otherDigits = (int[])( other.getValue() );

    boolean areEqual = true;

    if( digits.length != otherDigits.length )
    {
      areEqual = false;
    }
    else
    {
      for( int i = 0; i < digits.length; i++ )
      {
        if( digits[i] != otherDigits[i] )
        {
          areEqual = false;

          break;
        }
      }
    }

    return areEqual;

  }




  /**
   * Return dot-separated sequence of decimal values.
   *
   * @return the string
   */
  public String toString()
  {
    String valueString = new String();
    if( digits.length > 0 )
    {
      valueString += digits[0];

      for( int i = 1; i < digits.length; ++i )
      {
        valueString += "." + digits[i];
      }
    }

    return valueString;
  }

}
