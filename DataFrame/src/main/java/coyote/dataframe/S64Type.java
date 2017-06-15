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


/** Type representing an signed, 64-bit value in the range of -9,223,372,036,854,775,808 to 9,223,372,036,854,775,807 */
public class S64Type implements FieldType {
  private static final int _size = 8;

  private final static String _name = "S64";




  public boolean checkType( Object obj ) {
    return obj instanceof Long;
  }




  public Object decode( byte[] value ) {
    return new Long( ByteUtil.retrieveLong( value, 0 ) );
  }




  public byte[] encode( Object obj ) {
    return ByteUtil.renderLong( ( (Long)obj ).longValue() );
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
