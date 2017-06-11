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

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import coyote.commons.ByteUtil;


/**
 * Type Length Value data structure.
 * 
 * <p>This is an Abstract Data Type that represents itself in a fairly self-
 * describing format where each attribute of the instance is named and typed in
 * its native binary format.
 * 
 * <p>The first octet is unsigned integer (0-255) indicating the length of the 
 * name of the field. If non-zero, the given number of octets are read and 
 * parsed into a UTF-8 string.
 * 
 * <p>Next, another byte representing an unsigned integer (0-255) is read in 
 * and used to indicate the type of the field. If it is a numeric or other 
 * fixed type, the appropriate number of bytes are read in. If a variable type 
 * is indicated then the next U32 integer (4-bytes) is read as the length of 
 * the data. U32 is used to support nesting of frames within frames which can 
 * quickly exceed U16 values of 65535 bytes in length.
 * 
 * <p>This utility class packages up a tagged value pair with a length field so 
 * as to allow for reliable reading of data from various transport streams.
 */
public class DataField implements Cloneable {

  /** array of data types supported */
  private static final ArrayList<FieldType> _types = new ArrayList<FieldType>();

  /** (0) Type code representing a nested data frame */
  public static final short FRAMETYPE = 0;

  /** (1) Type code representing an undefined type */
  public static final short UDEF = 1;

  public static final short BYTEARRAY = 2;
  public static final short STRING = 3;
  public static final short S8 = 4;
  public static final short U8 = 5;
  public static final short S16 = 6;
  public static final short U16 = 7;
  public static final short S32 = 8;
  public static final short U32 = 9;
  public static final short S64 = 10;
  public static final short U64 = 11;
  public static final short FLOAT = 12;
  public static final short DOUBLE = 13;

  /** (14) Type code representing a boolean */
  public static final short BOOLEANTYPE = 14;

  public static final short DATE = 15;
  public static final short URI = 16;
  public static final short ARRAY = 17;

  static final String ENC_UTF8 = "UTF8";

  public static String DEFAULT_ENCODING = DataField.ENC_UTF8;
  protected static String strEnc = DataField.DEFAULT_ENCODING;

  // setup the string encoding of field names
  static {
    try {
      DataField.DEFAULT_ENCODING = System.getProperty( "file.encoding", DataField.ENC_UTF8 );
    } catch ( final SecurityException _ex ) {
      DataField.DEFAULT_ENCODING = DataField.ENC_UTF8;
      System.err.println( "Security settings preclude accessing Java System Property \"file.encoding\" - Using default string encoding of " + DataField.DEFAULT_ENCODING + " instead." );
    } catch ( final Exception _ex ) {
      DataField.DEFAULT_ENCODING = DataField.ENC_UTF8;
    }

    /** (0) Type code representing a nested data frame */
    DataField.addType( FRAMETYPE, new FrameType() );

    /** (1) Type code representing a NULL value - undefined type and a therefore empty value */
    DataField.addType( UDEF, new UndefinedType() );

    /** (2) Type code representing a byte array */
    DataField.addType( BYTEARRAY, new ByteArrayType() );

    /** (3) Type code representing a String object */
    DataField.addType( STRING, new StringType() );

    /** (4) Type code representing an signed, 8-bit value in the range of -128 to 127 */
    DataField.addType( S8, new S8Type() );

    /** (5) Type code representing an unsigned, 8-bit value in the range of 0 to 255 */
    DataField.addType( U8, new U8Type() );

    /** (6) Type code representing an signed, 16-bit value in the range of -32,768 to 32,767 */
    DataField.addType( S16, new S16Type() );

    /** (7) Type code representing an unsigned, 16-bit value in the range of 0 to 65,535 */
    DataField.addType( U16, new U16Type() );

    /** (8) Type code representing a signed, 32-bit value in the range of -2,147,483,648 to 2,147,483,647 */
    DataField.addType( S32, new S32Type() );

    /** (9) Type code representing an unsigned, 32-bit value in the range of 0 to 4,294,967,295 */
    DataField.addType( U32, new U32Type() );

    /** (10) Type code representing an signed, 64-bit value in the range of -9,223,372,036,854,775,808 to 9,223,372,036,854,775,807 */
    DataField.addType( S64, new S64Type() );

    /** (11) Type code representing an unsigned, 64-bit value in the range of 0 to 18,446,744,073,709,551,615 */
    DataField.addType( U64, new U64Type() );

    /** (12) Type code representing a 32-bit floating point value in the range of +/-1.4013e-45 to +/-3.4028e+38. */
    DataField.addType( FLOAT, new FloatType() );

    /** (13) Type code representing a 64-bit floating point value in the range of +/-4.9406e-324 to +/-1.7977e+308. */
    DataField.addType( DOUBLE, new DoubleType() );

    /** (14) Type code representing a boolean value */
    DataField.addType( BOOLEANTYPE, new BooleanType() );

    /** (15) Type code representing a unsigned 32-bit epoch time in milliseconds */
    DataField.addType( DATE, new DateType() );

    /** (16) Type code representing a uniform resource identifier */
    DataField.addType( URI, new UriType() );

    /** (17) Type code representing an ordered array of values (DataFields) */
    DataField.addType( ARRAY, new ArrayType() );
  }

  /** Name of this field */
  String name = null;

  /** The type of data being held. */
  short type;

  /** The actual value being held. Empty arrays are equivalent to a null value. */
  byte[] value;




  /**
   * Add a Data type for fields
   * 
   * @param indx where in the array to add the
   * @param type the object handling the type
   */
  static void addType( int indx, FieldType type ) {
    _types.add( indx, type );
  }




  /**
   * Protected no-arg constructor used for cloning
   */
  protected DataField() {}




  /**
   * Create a DataField with a specified type and value.
   * 
   * <p>Used by the ArrayType in decoding arrays of values.
   * 
   * @param type the type code representing the type of data held.
   * @param value the encoded value of the field.
   */
  protected DataField( short type, byte[] value ) {
    this.type = type;
    this.value = value;
  }




  /**
   * Create a DataField with a specified name, type and value.
   * 
   * <p>It is possible to use this to force a data field into a specific type 
   * even if the value is null.
   * 
   * @param name The name of this DataField
   * @param type the type code representing the type of data held.
   * @param value the encoded value of the field. An empty array is equivalent 
   *        to a null value.
   *
   * @throws IllegalArgumentException if the name exceeds 255 characters   
   */
  public DataField( String name, short type, byte[] value ) {
    this.name = DataField.nameCheck( name );
    this.type = type;
    this.value = value;
  }




  /**
   * Create a DataField for the specific object.
   *
   * @param obj The object to use as the value of the field
   */
  public DataField( final Object obj ) {
    type = DataField.getType( obj );
    value = DataField.encode( obj, type );
  }




  /**
   * Constructor DataField
   *
   * @param name The name of this DataField
   * @param obj The object value to encode
   *
   * @throws IllegalArgumentException
   */
  public DataField( final String name, final Object obj ) throws IllegalArgumentException {
    this.name = DataField.nameCheck( name );
    type = DataField.getType( obj );
    value = DataField.encode( obj, type );
  }




  /**
   * Create a deep-copy of this DataField.
   * 
   * <p>The name and type references are shared and the value is copied to an 
   * new byte array.
   *
   * @return A mutable copy of this DataField.
   */
  public Object clone() {
    final DataField retval = new DataField();

    // strings are immutable
    retval.name = name;
    retval.type = type;

    if ( value != null ) {
      retval.value = new byte[value.length];

      System.arraycopy( value, 0, retval.value, 0, value.length );
    }

    return retval;
  }




  /**
   * Checks to see if the name is valid.
   * 
   * <p>Right now only a check for size is performed. The size of a name must 
   * be less than 256 characters.
   *
   * @param name The name to check
   *
   * @return The validated name.
   *
   * @throws IllegalArgumentException
   */
  private static String nameCheck( final String name ) throws IllegalArgumentException {
    if ( name != null && name.length() > 255 ) {
      throw new IllegalArgumentException( "Name too long - 255 char limit" );
    }

    return name;
  }




  /**
   * Construct the data field from data read in from the given input stream.
   *
   * @param dis The input stream from which the data field will be read
   *
   * @throws IOException if there was a problem reading the stream.
   */
  public DataField( final DataInputStream dis ) throws IOException, DecodeException {
    // The first octet is the length of the name to read in
    final int nameLength = dis.readUnsignedByte();

    // If there is a name of any length, read it in as a String
    if ( nameLength > 0 ) {
      final int i = dis.available();

      if ( i < nameLength ) {
        throw new DecodeException( "value underflow: name length specified as " + nameLength + " but only " + i + " octets are available" );
      }

      final byte[] nameData = new byte[nameLength];
      dis.readFully( nameData );

      name = new String( nameData, DataField.strEnc );
    }

    // the next field we read is the data type
    type = dis.readByte();
    FieldType datatype = null;
    try {
      // get the proper field type
      datatype = getDataType( type );
    } catch ( Throwable ball ) {
      if ( nameLength > 0 ) {
        throw new DecodeException( "non supported type: '" + type + "' for field: '" + name + "'" );
      } else {
        throw new DecodeException( "non supported type: '" + type + "'" );
      }
    }

    // if the file type is a variable length (i.e. size < 0), read in the length
    if ( datatype.getSize() < 0 ) {
      // FIXME: This can only read in 2GB! not a real U32!!!
      final int length = dis.readInt();

      if ( length < 0 ) {
        throw new DecodeException( "read length bad value: length = " + length + " type = " + type );
      }

      final int i = dis.available();

      if ( i < length ) {
        throw new DecodeException( "value underflow: length specified as " + length + " but only " + i + " octets are available" );
      }

      value = new byte[length];

      if ( length > 0 ) {
        dis.read( value, 0, length );
      }
    } else {
      value = new byte[datatype.getSize()];
      dis.read( value );
    }
  }




  /**
   * Get the numeric code representing the type of the passed object
   *
   * @param obj The object to check
   *
   * @return the numeric type as it would be encoded in the field
   *
   * @throws IllegalArgumentException if the passed object is an unsupported type.
   */
  public static short getType( final Object obj ) throws IllegalArgumentException {
    for ( short x = 0; x < _types.size(); x++ ) {
      if ( _types.get( x ).checkType( obj ) )
        return x;
    }
    throw new IllegalArgumentException( "Unsupported Object Type" );
  }




  /**
   * @return the list of supported type names
   */
  public static List<String> getTypeNames() {
    List<String> retval = new ArrayList<String>();
    for ( short x = 0; x < _types.size(); x++ ) {
      retval.add( _types.get( x ).getTypeName() );
    }
    return retval;
  }




  /**
   * Return the field type with the given name
   * 
   * @param name The name of the type to retrieve
   * 
   * @return the ViledType with the given name or null if not found
   */
  public static FieldType getFieldType( String name ) {
    for ( short x = 0; x < _types.size(); x++ ) {
      if ( _types.get( x ).getTypeName().equals( name ) ) {
        return _types.get( x );
      }
    }
    return null;
  }




  /**
   * Convert the object into a binary representation of a DataField
   *
   * @param obj The object to encode.
   *
   * @return The bytes representing the object in DataFrame format.
   *
   * @throws IllegalArgumentException If the object is not supported.
   */
  public static byte[] encode( final Object obj ) throws IllegalArgumentException {
    return DataField.encode( obj, DataField.getType( obj ) );
  }




  /**
   * Return an array of bytes representing the given object using the given 
   * type specification.
   *
   * @param obj The object to encode.
   * @param type The type encoding to use.
   *
   * @return the value of the given object using the given type specification.
   *
   * @throws IllegalArgumentException
   */
  public static byte[] encode( final Object obj, final short type ) throws IllegalArgumentException {
    FieldType datatype = getDataType( type );
    return datatype.encode( obj );
  }




  /**
   * @return The numeric type of this field.
   */
  public short getType() {
    return type;
  }




  /**
   * @return The number of octets this fields value uses.
   */
  public int getLength() {
    return value.length;
  }




  /**
   * @return The encoded value of this field.
   */
  public byte[] getValue() {
    return value;
  }




  /**
   * @return The value of this field as an object.
   */
  public Object getObjectValue() {
    return getObjectValue( type, value );
  }




  /**
   * Decode the field into an object reference.
   * 
   * @return the object value of the data encoded in the value attribute.
   */
  private Object getObjectValue( final short typ, final byte[] val ) {
    if ( val == null || val.length == 0 ) {
      return null;
    } else {
      FieldType datatype = getDataType( typ );
      return datatype.decode( val );
    }
  }




  /**
   * Write the field to the output stream.
   *
   * @param dos The DataOutputStream on which the field is to be written.
   *
   * @throws IOException if there is a problem writing to the output stream.
   */
  public void write( final DataOutputStream dos ) throws IOException {
    // If we have a name...
    if ( name != null ) {
      // write the length and name fields
      final byte[] nameField = name.getBytes( DataField.strEnc );
      final int nameLength = nameField.length;
      dos.write( ByteUtil.renderShortByte( (short)nameLength ) );
      dos.write( nameField );
    } else {
      // indicate a name field length of 0
      dos.write( ByteUtil.renderShortByte( (short)0 ) );
    }

    // Write the type field
    dos.write( ByteUtil.renderShortByte( type ) );

    if ( value != null ) {

      FieldType datatype = getDataType( type );

      // If the value is variable in length
      if ( datatype.getSize() < 0 ) {
        // write the length
        dos.write( ByteUtil.renderUnsignedInt( (long)value.length ) );
      }

      // write the value itself
      dos.write( value );
    } else {
      dos.writeShort( 0 );
    }

    return;
  }




  /**
   * Get the wire format of the Data.
   *
   * @return binary representation of the field.
   */
  public byte[] getBytes() {
    final ByteArrayOutputStream out = new ByteArrayOutputStream();
    final DataOutputStream dos = new DataOutputStream( out );

    try {
      write( dos );
    } catch ( final IOException ioe ) {}

    return out.toByteArray();
  }




  /**
   * Access the name of this field.
   * 
   * @return The name of this field.
   */
  public String getName() {
    return name;
  }




  /**
   * Set the name of this field.
   * 
   * @param string Then name of this field.
   */
  public void setName( final String string ) {
    name = string;
  }




  /**
   * Get the name of the type for the given code
   *
   * @param code The code representing the data field type
   *
   * @return The name of the type represented by the code
   */
  private static String getTypeName( final short code ) {
    return getDataType( code ).getTypeName();
  }




  /**
   * Get the size of the given type.
   * 
   * @param code the type code used in encoded fields
   * 
   * @return the number of octets used to represent the data type in its 
   * encoded form.
   */
  protected static int getTypeSize( final short code ) {
    return getDataType( code ).getSize();
  }




  /**
   * Return the appropriate FieldType for the given type identifier.
   * 
   * @param typ the identifier of the type to retrieve
   * 
   * @return The FieldType object which handles the data of the identified type
   */
  protected static FieldType getDataType( short typ ) {
    FieldType retval = null;

    try {
      // get the proper field type
      retval = _types.get( typ );
    } catch ( Throwable ball ) {
      throw new IllegalArgumentException( "Unsupported data type of '" + typ + "'" );
    }

    if ( retval == null )
      throw new IllegalArgumentException( "Null type field for type: '" + typ + "'" );
    else
      return retval;
  }




  /**
   * Return the name of the data type this field contains/
   *
   * @return The name of the data type for this instance
   */
  public String getTypeName() {
    return DataField.getTypeName( type );
  }




  /**
   * @return True if the value is numeric, false otherwise.
   */
  public boolean isNumeric() {
    return getDataType( type ).isNumeric();
  }




  /**
   * @return True if the value is not numeric, false if it is numeric.
   */
  public boolean isNotNumeric() {
    return !isNumeric();
  }




  /**
   * @return True if the value is a frame, false otherwise.
   */
  public boolean isFrame() {
    return type == FRAMETYPE;
  }




  /**
   * @return True if the value is not a frame, false if it is a frame.
   */
  public boolean isNotFrame() {
    return type != FRAMETYPE;
  }




  /**
   * @return True if the value represents a null value, false otherwise.
   */
  public boolean isUndefined() {
    return type == UDEF;
  }




  /**
   * Human readable format of the data field.
   *
   * @return a string representation of the data field instance
   */
  @Override
  public String toString() {
    final StringBuffer buf = new StringBuffer( "DataField:" );
    buf.append( " name='" + name + "'" );
    buf.append( " type=" + this.getTypeName() );
    buf.append( "(" + type + ")" );
    if ( value.length > 32 ) {
      byte[] sample = new byte[32];
      System.arraycopy( value, 0, sample, 0, sample.length );
      buf.append( " value=[" + ByteUtil.bytesToHex( sample ) + " ...]" );
    } else
      buf.append( " value=[" + ByteUtil.bytesToHex( value ) + "]" );

    return buf.toString();
  }




  /**
   * @return the number of types currently supported/
   */
  static int typeCount() {
    return _types.size();
  }




  /**
   * @return The value of this field as a String.
   */
  public String getStringValue() {
    return getStringValue( type, value );
  }




  /**
   * Decode the field into an string representation.
   * 
   * <p>This is useful when using this field as a value and needing to output 
   * it in human readable form. This is similar to the toString function except
   * this represents the value carried/encapsulated in this field, not the 
   * field itself.
   * 
   * @return the string representation of the object value of the data encoded 
   * in the value attribute.
   */
  private String getStringValue( final short typ, final byte[] val ) {
    FieldType datatype = getDataType( typ );
    return datatype.stringValue( val );
  }




  /**
   * Test to see if this field has a value.
   * 
   * @return true if there is no value, false if there is data in this field
   */
  public boolean isNull() {
    return ( value == null || value.length == 0 );
  }




  /**
   * Test to see if this field has a value.
   * 
   * @return true if there is a value, false if there is no data in this field
   */
  public boolean isNotNull() {
    return !isNull();
  }




  /**
   * @return True if the value is an array, false otherwise.
   */
  public boolean isArray() {
    return type == ARRAY;
  }

}
