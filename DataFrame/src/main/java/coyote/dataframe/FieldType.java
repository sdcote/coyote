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

/**
 * This interface defines a data type
 */
public interface FieldType {

  /**
   * Check the object if it is the same type.
   * 
   * @param obj the object to check
   * 
   * @return true if this Field type supports the object, false otherwise
   */
  public boolean checkType( Object obj );




  /**
   * Decode the bytes into an object.
   * 
   * @param value the array of bytes to decode
   * 
   * @return an object representing
   */
  public Object decode( byte[] value );




  /**
   * Encode the given object into a byte array
   * 
   * @param obj object to encode
   * 
   * @return the wire format of the object 
   */
  public byte[] encode( Object obj );




  /**
   * Flag indicating the data type is numeric.
   * 
   * @return true if the type is numeric, false otherwise.
   */
  public boolean isNumeric();




  /**
   * Determine the size in bytes to be used in representing the value.
   * 
   * <p>A value of 0 means null; negative number means variable length type.
   * 
   * @return the size of the value to store or read.
   */
  public int getSize();




  /**
   * Get a simple name for the type to aid in formatting.
   * 
   * @return short name for the type
   */
  public String getTypeName();




  /**
   * Get the string value of this data type.
   * 
   * <p>This allows each supported type to customize its own string 
   * representation of the values it supports. For example, the string 
   * representation can be customized to be formatted in ISO 8601 or the Java
   * standard long date format.
   * 
   * @param val the bytes to decode into a string representation of this type.
   * 
   * @return The string representation of this type.
   */
  public String stringValue( byte[] val );

}
