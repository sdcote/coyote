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


/** Type representing a boolean value */
public class BooleanType implements FieldType {
  private static final int _size = 1;

  private final static String _name = "BOL";




  public boolean checkType( Object obj ) {
    return obj instanceof Boolean;
  }




  public Object decode( byte[] value ) {
    return new Boolean( ByteUtil.retrieveBoolean( value, 0 ) );
  }




  public byte[] encode( Object obj ) {
    return ByteUtil.renderBoolean( ( (Boolean)obj ).booleanValue() );
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
    Boolean retval = null;
    if ( text != null ) {
      String str = text.toLowerCase();
      if ( "true".equals( str ) || "1".equals( str ) || "yes".equals( str ) ) {
        retval = true;
      } else if ( "false".equals( str ) || "0".equals( str ) || "no".equals( str ) ) {
        retval = false;
      }
    }
    return retval;
  }

}
