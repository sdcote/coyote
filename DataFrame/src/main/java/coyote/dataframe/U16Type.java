/*
 * Copyright (c) 2006 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.dataframe;

import coyote.commons.ByteUtil;


/** Type representing an unsigned, 16-bit value in the range of 0 to 65,535 */
public class U16Type implements FieldType {
  private static final int _size = 2;

  private final static String _name = "U16";




  /**
   * This can support multiple types and values, but really not efficient for 
   * Byte and Short types.
   */
  public boolean checkType( Object obj ) {
    return ( ( obj instanceof java.lang.Byte && ( (Byte)obj ).byteValue() >= 0 ) || ( obj instanceof java.lang.Short && ( (Short)obj ).shortValue() >= 0 ) || ( obj instanceof java.lang.Integer && ( (Integer)obj ).intValue() >= 0 && ( (Integer)obj ).intValue() <= 65535 ) );
  }




  public Object decode( byte[] value ) {
    return new Integer( ByteUtil.retrieveUnsignedShort( value, 0 ) );
  }




  public byte[] encode( Object obj ) {
    return ByteUtil.renderUnsignedShort( ( (Integer)obj ).shortValue() );
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
    Integer retval = null;
    try {
      long num = Integer.parseInt( text );
      if ( num >= 0 && num <= 65535 ) {
        retval = (int)num;
      }
    } catch ( NumberFormatException ignore ) {}
    return retval;
  }

}
