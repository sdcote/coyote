/*
 * Copyright (c) 2016 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 *
 * Contributors:
 *   Stephan D. Cote 
 *      - Initial concept and implementation
 */
package coyote.dataframe;

/**
 * 
 */
public class DecodeException extends RuntimeException {

  private static final long serialVersionUID = 2175998560480891077L;

  private final int position;
  private final int previous;
  private final int fieldIndex;
  private final DataField field;
  private final byte[] bytes;

  private static final String MESSGE = "Decode exception";




  /**
   * 
   */
  public DecodeException() {
    this( MESSGE, null, true, true, 0, -1, -1, null, null );
  }




  /**
   * @param message
   */
  public DecodeException( String message ) {
    this( message, null, true, true, 0, -1, -1, null, null );
  }




  /**
   * @param cause
   */
  public DecodeException( Throwable cause ) {
    this( MESSGE, cause, true, true, 0, -1, -1, null, null );
  }




  /**
   * @param message
   * @param cause
   */
  public DecodeException( String message, Throwable cause ) {
    this( message, cause, true, true, 0, -1, -1, null, null );
  }




  /**
   * @param message
   * @param bytes
   */
  public DecodeException( String message, byte[] bytes ) {
    this( message, null, true, true, 0, -1, -1, null, bytes );
  }




  /**
   * @param message
   * @param cause
   * @param enableSuppression
   * @param writableStackTrace
   */
  public DecodeException( String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace ) {
    this( message, cause, enableSuppression, writableStackTrace, 0, -1, -1, null, null );
  }




  /**
   * @param message
   * @param cause
   * @param pos
   * @param prev 
   * @param indx
   * @param fld
   */
  public DecodeException( String message, Throwable cause, int pos, int prev, int indx, DataField fld ) {
    this( message, cause, true, true, pos, prev, indx, fld, null );
  }




  /**
   * @param message
   * @param cause
   * @param enableSuppression
   * @param writableStackTrace
   * @param pos
   * @param prev 
   * @param indx
   * @param fld
   * @param data 
   */
  public DecodeException( String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace, int pos, int prev, int indx, DataField fld, byte[] data ) {
    super( message, cause, enableSuppression, writableStackTrace );
    position = pos;
    previous = prev;
    fieldIndex = indx;
    field = fld;
    bytes = data;
  }




  /**
   * @return the offset in the stream where the error occurred, more accurately, the start of reading the field where the error occurred
   */
  public int getPosition() {
    return position;
  }




  /**
   * @return the previous position
   */
  public int getPreviousPosition() {
    return previous;
  }




  /**
   * @return the fieldIndex
   */
  public int getFieldIndex() {
    return fieldIndex;
  }




  /**
   * @return the field
   */
  public DataField getField() {
    return field;
  }




  /**
   * @return the byte array of the raw data
   */
  public byte[] getBytes() {
    return bytes;
  }

}
