/*
 * Copyright (c) 2006 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.dataframe;

/**
 * @author Steve.Cote
 *
 */
public class ByteArrayType implements FieldType {
  /** negative size indicates a variable length value is to be expected. */
  private static final int _size = -1;

  private final static String _name = "BYTE";




  public boolean checkType( Object obj ) {
    return ( obj instanceof byte[] );
  }




  public byte[] encode( Object obj ) {
    return (byte[])obj;
  }




  public Object decode( byte[] value ) {
    return value;
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
    Object obj = decode( val );
    if ( obj != null )
      return obj.toString();
    else
      return "";
  }




  /**
   * @see coyote.dataframe.FieldType#parse(java.lang.String)
   */
  @Override
  public Object parse( String text ) {
    System.err.println( "ByteArrayType.parse not implememted" );
    return null;
  }

}
