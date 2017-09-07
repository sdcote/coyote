/*
 * Copyright (c) 2006 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.dataframe;

import coyote.commons.ByteUtil;


/** Type representing a 32-bit floating point value in the range of +/-1.4013e-45 to +/-3.4028e+38. */
public class FloatType implements FieldType {
  private static final int _size = 4;

  private final static String _name = "FLT";




  public boolean checkType( Object obj ) {
    return obj instanceof Float;
  }




  public Object decode( byte[] value ) {
    return new Float( ByteUtil.retrieveFloat( value, 0 ) );
  }




  public byte[] encode( Object obj ) {
    return ByteUtil.renderFloat( ( (Float)obj ).floatValue() );
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
    Float retval = null;
    try {
      retval = Float.parseFloat( text );
    } catch ( NumberFormatException ignore ) {}
    return retval;
  }

}
