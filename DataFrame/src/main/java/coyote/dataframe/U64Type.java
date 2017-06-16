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

import java.math.BigInteger;

import coyote.commons.ByteUtil;


/** Type representing an unsigned, 64-bit value in the range of 0 to 18,446,744,073,709,551,615 */
public class U64Type implements FieldType {
  private static final int _size = 8;

  private final static String _name = "U64";

  static final BigInteger MAX_VALUE;
  static final BigInteger MIN_VALUE;
  static {
    byte[] input = { (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff };
    MAX_VALUE = new BigInteger( 1, input );
    byte[] input2 = { (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00 };
    MIN_VALUE = new BigInteger( input2 );
  }




  public boolean checkType( Object obj ) {
    return ( ( obj instanceof java.lang.Byte && ( (Byte)obj ).byteValue() >= 0 ) || ( obj instanceof java.lang.Short && ( (Short)obj ).shortValue() >= 0 ) || ( obj instanceof java.lang.Integer && ( (Integer)obj ).intValue() >= 0 ) || ( obj instanceof java.lang.Long && ( (Long)obj ).longValue() >= 0 || ( obj instanceof java.math.BigInteger && ( (BigInteger)obj ).compareTo( MIN_VALUE ) >= 0 && ( (BigInteger)obj ).compareTo( MAX_VALUE ) <= 0 ) ) );
  }




  public Object decode( byte[] value ) {
    return new BigInteger( 1, value );
  }




  public byte[] encode( Object obj ) {
    return ByteUtil.renderBigInteger( (BigInteger)obj );
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
    BigInteger retval = null;
    try {
      BigInteger num = new BigInteger( text );
      if ( num.doubleValue() >= 0 && num.doubleValue() <= 18446744073709551615D ) {
        retval = num;
      }
    } catch ( NumberFormatException ignore ) {}
    return retval;
  }

}
