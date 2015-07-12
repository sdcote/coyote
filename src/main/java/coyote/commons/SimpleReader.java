/*
 * Copyright (c) 2003 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 *
 * Contributors:
 *   Stephan D. Cote 
 *      - Initial API and implementation
 */
package coyote.commons;

import java.io.Reader;


/**
 * SimpleReader is a scaled-down version, slightly faster version of
 * StringReader.
 */
public final class SimpleReader extends Reader {

  /** Field string */
  String string;

  /** Field length */
  int length;

  /** Field next */
  int next;

  /** Field mark */
  int mark;




  /**
   * Constructor FastReader
   *
   * @param text The string to be read by the reader
   */
  public SimpleReader( String text ) {
    next = 0;
    mark = 0;
    string = text;
    length = text.length();
  }




  /**
   * Read the next char
   *
   * @return the char read
   */
  public int read() {
    return ( next < length ) ? string.charAt( next++ ) : -1;
  }




  /**
   * Read data into the buffer
   *
   * @param buffer the destination character array
   * @param offset offset in this reader
   * @param length how many bytes to read
   *
   * @return the number of bytes read
   */
  public int read( char buffer[], int offset, int length ) {
    if ( length == 0 ) {
      return 0;
    }

    if ( next >= length ) {
      return -1;
    } else {
      int bytesToRead = Math.min( length - next, length );
      string.getChars( next, next + bytesToRead, buffer, offset );

      next += bytesToRead;

      return bytesToRead;
    }
  }




  /**
   * Method skip
   *
   * @param amount the number of bytes to skip
   *
   * @return The number skipped
   */
  public long skip( long amount ) {
    if ( next >= length ) {
      return 0L;
    } else {
      long skipped = Math.min( length - next, amount );
      next += skipped;

      return skipped;
    }
  }




  /**
   * Is the reader ready
   *
   * @return always returns true
   */
  public boolean ready() {
    return true;
  }




  /**
   * Is mark supported?
   * 
   * @return always returns true
   */
  public boolean markSupported() {
    return true;
  }




  /**
   * Method mark
   *
   * @param limit
   */
  public void mark( int limit ) {
    mark = next;
  }




  /**
   * reset the next character to the mark.
   */
  public void reset() {
    next = mark;
  }




  /**
   * Close the reader
   */
  public void close() {
    string = null;
  }
}
