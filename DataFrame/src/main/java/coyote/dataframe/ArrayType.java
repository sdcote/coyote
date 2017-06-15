/*
 * Copyright (c) 2006 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 *
 * Contributors:
 *   Stephan D. Cote 
 *      - Initial API and implementation
 */
package coyote.dataframe;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import coyote.commons.ByteUtil;


/**
 * Type representing an ordered array of values.
 * 
 * <p>The current design involves encoding a name, type (array), length (number 
 * of elements) and a set of Type, Length, Value (TLV) triplets for each array
 * element.
 */
public class ArrayType implements FieldType {

  private static final Object[] EMPTY_ARRAY = new Object[0];

  private static final int _size = -1;

  private final static String _name = "ARY";




  public boolean checkType( Object obj ) {
    return obj instanceof Object[];
  }




  /**
   * This will return a DataFrame with unnamed fields
   */
  public Object decode( byte[] value ) {
    DataFrame retval = new DataFrame();

    short type = 0;
    byte[] data = null;
    FieldType datatype = null;

    if ( value != null ) {

      try {
        // Create a data input stream
        final ByteArrayInputStream bais = new ByteArrayInputStream( value );
        final DataInputStream dis = new DataInputStream( bais );

        while ( dis.available() > 0 ) {
          // the next field we read is the data type
          type = dis.readByte();

          try {
            // get the proper field type
            datatype = DataField.getDataType( type );
          } catch ( Throwable ball ) {
            throw new IOException( "non supported type: '" + type + "'" );
          }

          // if the file type is a variable length (i.e. size < 0), read in the length
          if ( datatype.getSize() < 0 ) {
            final int length = dis.readUnsignedShort();

            if ( length < 0 ) {
              throw new IOException( "read length bad value: length = " + length + " type = " + type );
            }

            int i = dis.available();

            if ( i < length ) {
              throw new IOException( "value underflow: length specified as " + length + " but only " + i + " octets are available" );
            }

            data = new byte[length];

            if ( length > 0 ) {
              dis.read( data, 0, length );
            }
          } else {
            data = new byte[datatype.getSize()];
            dis.read( data );
          }

          // now get the object value of the data
          retval.add( datatype.decode( data ) );

        } // while there is data available to read

      } catch ( Exception e ) {
        throw new IllegalArgumentException( "Could not decode value", e );
      }

    }

    return retval;
  }




  /**
   * Encode the payload portion of the array (i.e., no name or type)
   */
  public byte[] encode( Object obj ) {
    final Object[] ary = (Object[])obj;

    final ByteArrayOutputStream out = new ByteArrayOutputStream();
    final DataOutputStream dos = new DataOutputStream( out );

    for ( int x = 0; x < ary.length; x++ ) {
      try {
        // This will throw an exception for any unsupported data types.
        final short tipe = DataField.getType( ary[x] );
        final byte[] data = DataField.encode( ary[x], tipe );
        final int size = DataField.getDataType( tipe ).getSize();

        // Write the type field
        dos.write( ByteUtil.renderShortByte( tipe ) );

        if ( data != null ) {
          // If the value is variable in length
          if ( size < 0 ) {
            // write the length
            dos.writeShort( data.length );
          }

          // write the value itself
          dos.write( data );
        } else {
          dos.writeShort( 0 );// null value
        }
      } catch ( final Throwable t ) {
        System.err.println( "Array object of type " + ary[x].getClass().getSimpleName() + " is not supported in DataFrames" );
        // just skip the offending object and add the rest.
      }
    } // for each

    return out.toByteArray();
  }




  public String getTypeName() {
    return _name;
  }




  public boolean isNumeric() {
    return false;
  }




  public int getSize() {
    return _size;
  }




  /**
   * @see coyote.dataframe.FieldType#stringValue(byte[])
   */
  @Override
  public String stringValue( byte[] val ) {
    StringBuffer b = new StringBuffer();
    Object obj = decode( val );
    if ( obj != null ) {
      if ( obj instanceof Object[] ) {
        Object[] orray = (Object[])obj;
        for ( int x = 0; x < orray.length; x++ ) {
          Object oval = orray[x];
          if ( oval instanceof Boolean ) {
            b.append( Boolean.toString( (Boolean)oval ) );
          }else if ( oval instanceof Number ) {
            b.append( ( (Number)oval ).toString() );
          } else {
            b.append( "\"" );
            b.append( oval.toString() );
            b.append( "\"" );
          }

          if ( x + 1 < orray.length ) {
            b.append( "," );
          }
        }

      } else {
        b.append( obj.toString() );
      }
    }

    return b.toString();

  }




  @Override
  public Object parse( String text ) {
    // TODO Auto-generated method stub
    return null;
  }

}
