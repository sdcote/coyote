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

import coyote.commons.ByteUtil;


/** Type representing an unsigned, 32-bit value in the range of 0 to 4,294,967,295 */
public class U32Type implements FieldType {
  private static final int _size = 4;

  private final static String _name = "U32";




  public boolean checkType( Object obj ) {
    return ( ( obj instanceof java.lang.Byte && ( (Byte)obj ).byteValue() >= 0 ) || ( obj instanceof java.lang.Short && ( (Short)obj ).shortValue() >= 0 ) || ( obj instanceof java.lang.Integer && ( (Integer)obj ).intValue() >= 0 ) || ( obj instanceof java.lang.Long && ( (Long)obj ).longValue() >= 0 && ( (Long)obj ).longValue() <= 4294967295L ) );
  }




  public Object decode( byte[] value ) {
    return new Long( ByteUtil.retrieveUnsignedInt( value, 0 ) );
  }




  public byte[] encode( Object obj ) {
    return ByteUtil.renderUnsignedInt( ( (Long)obj ).longValue() );
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




  @Override
  public Object parse( String text ) {
    // TODO Auto-generated method stub
    return null;
  }

}
