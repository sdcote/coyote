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

import java.io.UnsupportedEncodingException;


/**
 * Type representing a string of characters.
 */
public class StringType implements FieldType {
  /** negative size indicates a variable length value is to be expected. */
  private static final int _size = -1;

  private final static String _name = "STR";

  /** US standard default encoding, also known as Latin-1 */
  static final String ENC_8859_1 = "8859_1";

  /** The default character encoding used */
  public static String DEFAULT_ENCODING = StringType.ENC_8859_1;

  // setup the string encoding of field names
  static {
    try {
      StringType.DEFAULT_ENCODING = System.getProperty( "file.encoding", StringType.ENC_8859_1 );
    } catch ( final SecurityException _ex ) {
      StringType.DEFAULT_ENCODING = StringType.ENC_8859_1;
      System.err.println( "Security settings preclude accessing Java System Property \"file.encoding\" - Using default string encoding of " + StringType.DEFAULT_ENCODING + " instead." );
    } catch ( final Exception _ex ) {
      StringType.DEFAULT_ENCODING = StringType.ENC_8859_1;
    }
  }

  /** The default encoding for string objects into byte arrays (ISO 5589-1) */
  protected static String strEnc = StringType.DEFAULT_ENCODING;




  /**
   * @see coyote.dataframe.FieldType#checkType(java.lang.Object)
   */
  public boolean checkType( Object obj ) {
    return ( obj instanceof String );
  }




  /**
   * @see coyote.dataframe.FieldType#decode(byte[])
   */
  public Object decode( byte[] value ) {
    try {
      return new String( value, StringType.strEnc );
    } catch ( UnsupportedEncodingException e ) {
      e.printStackTrace();
      return new String( value );
    }
  }




  /**
   * @see coyote.dataframe.FieldType#encode(java.lang.Object)
   */
  public byte[] encode( Object obj ) {
    try {
      return ( (String)obj ).getBytes( StringType.strEnc );
    } catch ( final UnsupportedEncodingException e ) {
      e.printStackTrace();
      return ( (String)obj ).getBytes();
    }

  }




  /**
   * @see coyote.dataframe.FieldType#isNumeric()
   */
  public boolean isNumeric() {
    return false;
  }




  /**
   * @see coyote.dataframe.FieldType#getSize()
   */
  public int getSize() {
    return _size;
  }




  /**
   * @see coyote.dataframe.FieldType#getTypeName()
   */
  public String getTypeName() {
    return _name;
  }




  /**
   * @see coyote.dataframe.FieldType#stringValue(byte[])
   */
  @Override
  public String stringValue( byte[] val ) {
    if ( val == null ) {
      return "";
    } else {
      Object obj = decode( val );
      if ( obj != null )
        return obj.toString();
      else
        return "";
    }
  }




  /**
   * @see coyote.dataframe.FieldType#parse(java.lang.String)
   */
  @Override
  public Object parse( String text ) {
    return text;
  }

}
