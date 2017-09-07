/*
 * Copyright (c) 2006 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.dataframe;

import coyote.commons.ByteUtil;


/** Type representing an signed, 8-bit value in the range of -128 to 127 */
public class S8Type implements FieldType {
  private static final int _size = 1;

  private final static String _name = "S8";




  public boolean checkType( Object obj ) {
    return ( ( obj instanceof java.lang.Byte && ( (Byte)obj ).byteValue() >= -128 && ( (Byte)obj ).byteValue() <= 127 ) || ( obj instanceof java.lang.Short && ( (Short)obj ).shortValue() >= -128 && ( (Short)obj ).shortValue() <= 127 ) );
  }




  public Object decode( byte[] value ) {
    return new java.lang.Short( value[0] );
  }




  public byte[] encode( Object obj ) {
    final byte[] retval = new byte[1];
    retval[0] = ByteUtil.renderShortByte( (Short)obj );
    return retval;
  }




  public String getTypeName() {
    return _name;
  }




  public boolean isNumeric() {
    return true;
  }




  public int getSize() {
    return _size;
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
    Byte retval = null;
    try {
      short num = Short.parseShort( text );
      if ( num >= -128 && num <= 127 ) {
        retval = (byte)num;
      }
    } catch ( NumberFormatException ignore ) {}
    return retval;
  }

}
