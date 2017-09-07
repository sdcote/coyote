/*
 * Copyright (c) 2006 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.dataframe;

/**
 * Type representing an undefined value.
 */
public class UndefinedType implements FieldType {

  private static final byte[] NULLVALUE = new byte[0];

  private final static String _name = "UDEF";




  /** 
   * zero size implies null type
   * @see coyote.dataframe.FieldType#getSize()
   */
  public int getSize() {
    return 0;
  }




  /**
   * @see coyote.dataframe.FieldType#checkType(java.lang.Object)
   */
  public boolean checkType( Object obj ) {
    if ( obj == null )
      return true;
    else
      return false;
  }




  public byte[] encode( Object obj ) {
    return NULLVALUE;
  }




  public Object decode( byte[] value ) {
    return null;
  }




  public String getTypeName() {
    return _name;
  }




  public boolean isNumeric() {
    return false;
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
    return null;
  }

}
