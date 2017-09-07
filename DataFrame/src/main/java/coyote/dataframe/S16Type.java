/*
 * Copyright (c) 2006 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.dataframe;

import coyote.commons.ByteUtil;


/** signed, 16-bit value in the range of -32,768 to 32,767 */
public class S16Type implements FieldType {
  private static final int _size = 2;

  private final static String _name = "S16";




  public boolean checkType( Object obj ) {
    return ( obj instanceof java.lang.Short );
  }




  public Object decode( byte[] value ) {
    return new Short( ByteUtil.retrieveShort( value, 0 ) );
  }




  public byte[] encode( Object obj ) {
    return ByteUtil.renderShort( ( (Short)obj ).shortValue() );
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
    Short retval = null;
    try {
      retval = Short.parseShort( text );
    } catch ( NumberFormatException ignore ) {}
    return retval;
  }
}
