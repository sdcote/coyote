/*
 * Copyright (c) 2004 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 *
 * Contributors:
 *   Stephan D. Cote 
 *      - Initial concept and initial implementation
 */
package coyote.commons;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Iterator;
import java.util.NoSuchElementException;


/**
 * 
 */
public class LineIterator implements Iterator<String> {

  /** The reader that is being read. */
  private final BufferedReader _reader;

  /** The current line. */
  private String _currentLine;

  /** A flag indicating if the iterator has been fully read. */
  private boolean _done = false;




  /**
   * Constructs an iterator of the lines for a <code>Reader</code>.
   *
   * @param reader the <code>Reader</code> to read from, not null
   * 
   * @throws IllegalArgumentException if the reader is null
   */
  public LineIterator( final Reader reader ) throws IllegalArgumentException {
    if ( reader == null ) {
      throw new IllegalArgumentException( "Reader must not be null" );
    }
    if ( reader instanceof BufferedReader ) {
      _reader = (BufferedReader)reader;
    } else {
      _reader = new BufferedReader( reader );
    }
  }




  /**
   * Indicates whether the <code>Reader</code> has more lines.
   * 
   * <p>If there is an <code>IOException</code> then {@link #close()} will
   * be called on this instance.</p>
   *
   * @return {@code true} if the Reader has more lines
   * 
   * @throws IllegalStateException if an IO exception occurs
   */
  public boolean hasNext() {
    if ( _currentLine != null ) {
      return true;
    } else if ( _done ) {
      return false;
    } else {
      try {
        while ( true ) {
          final String line = _reader.readLine();
          if ( line == null ) {
            _done = true;
            return false;
          } else if ( isValidLine( line ) ) {
            _currentLine = line;
            return true;
          }
        }
      } catch ( final IOException ioe ) {
        close();
        throw new IllegalStateException( ioe );
      }
    }
  }




  /**
   * Returns the next line in the wrapped <code>Reader</code>.
   *
   * @return the next line from the input
   * 
   * @throws NoSuchElementException if there is no line to return
   */
  public String next() {
    return nextLine();
  }




  /**
   * Returns the next line in the wrapped {@code Reader}.
   *
   * @return the next line from the input
   * 
   * @throws NoSuchElementException if there is no line to return
   */
  public String nextLine() {
    if ( !hasNext() ) {
      throw new NoSuchElementException( "No more lines" );
    }
    final String currentLine = _currentLine;
    _currentLine = null;
    return currentLine;
  }




  /**
   * Closes the underlying {@code Reader} quietly.
   * 
   * <p>This method is useful if you only want to process the first few lines 
   * of a file. If you do not close the iterator then the {@code Reader} 
   * remains open. This method can safely be called multiple times.</p>
   */
  public void close() {
    _done = true;
    FileUtil.close( _reader );
    _currentLine = null;
  }




  /**
   * The removal of a line from a file is not supported.
   *
   * @throws UnsupportedOperationException always
   */
  public void remove() {
    throw new UnsupportedOperationException( "Remove unsupported on LineIterator" );
  }




  /**
   * Method to validate each line that is returned.
   * 
   * <p>The {@link #hasNext()} method will call this method to determine if 
   * this line is valid for return to the user. If this method returns false, 
   * the line will be skipped and another line read.</p>
   * 
   * <p>This implementation always returns true. This method be overridden 
   * with other implementations which can check for empty or blank lines.</p>
   * 
   * @param line the line that is to be validated
   * 
   * @return true if valid, false to remove from the iterator
   */
  protected boolean isValidLine( final String line ) {
    return true;
  }

}