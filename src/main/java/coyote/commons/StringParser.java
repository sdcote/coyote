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

import java.io.IOException;
import java.io.Reader;


/**
 * A rather simple parser of strings.
 */
public class StringParser {

  /** The reader we use to read the string passed to this parser */
  private Reader reader;

  /**
   * The current line number (incremented every time we observe a new line
   * character)
   */
  private int lineNumber = 1;

  /** The current character of the current line. */
  private int charNumber = 0;

  /** The last character we read */
  private int lastChar = 0;

  /** History window buffer size for getPosition() */
  protected final static int BUFFER_SIZE = 20;

  /** Index within the history */
  private int index = 0;

  /** The window we use to display the history of our position in getPosiotion() */
  private int[] history = new int[BUFFER_SIZE];

  /**
   * The default token delimiters we use - SP(space), HT(horizontal tab) and
   * NL(new line)
   */
  private String defaultDelimiters = " \t\n";

  /** flag indicating the parser is consuming/ignoring comments */
  boolean consumingCodeComments = false;

  /** the currently collected comment */
  StringBuffer comment;




  /**
   * Creat a parser out of a string
   *
   * @param string the string to parse
   */
  public StringParser( String string ) {
    this.reader = new SimpleReader( string );
  }




  /**
   * Create a parser out of a reader
   * 
   * @param reader the reader to read
   */
  public StringParser( Reader reader ) {
    this.reader = reader;
  }




  /**
   * Create a parser out of a reader using the given token delimiters.
   * 
   * @param reader the reader to read
   * @param defaultDelimiters the token delimiters to use while parsing
   */
  public StringParser( Reader reader, String defaultDelimiters ) {
    this.reader = reader;
    this.defaultDelimiters = defaultDelimiters;
  }




  /**
   * Create a parser out of a string using the given token delimiters.
   * 
   * @param string the string to parse
   * @param defaultDelimiters the token delimiters to use while parsing
   */
  public StringParser( String string, String defaultDelimiters ) {
    this.reader = new SimpleReader( string );
    this.defaultDelimiters = defaultDelimiters;
  }




  /**
   * Checks to see if we are at the end of the file
   *
   * @return true if at the end of file, false otherwise.
   *
   * @throws IOException if we exceed our data (already at or past EOF)
   */
  public boolean eof() throws IOException {
    // skipWhitespace();

    return ( peek() == -1 );
  }




  /**
   * Skip all the whitespace in the reader, by reading each character and
   * stopping just before a non-whitespace character is found.
   *
   * @throws IOException
   */
  public void skipWhitespace() throws IOException {
    while ( true ) {
      // mark where we are in the reader allowing only to read 2 characters
      reader.mark( 2 );

      // Read the next character
      int ch = reader.read();

      // If the character is a whitespace...
      if ( Character.isWhitespace( (char)ch ) ) {
        reader.reset();
        read();
      } else {
        if ( ( ch == '/' ) && consumingCodeComments ) {
          ch = reader.read();

          reader.reset();

          if ( ch == '/' ) {
            readOneLineComment();
          } else {
            if ( ch == '*' ) {
              readMultiLineComment();
            } else {
              return;
            }
          }
        } else {
          reader.reset();

          return;
        }
      }
    }
  }




  /**
   * Skip ahead in the buffer the given amount.
   *
   * @param length the amount to skip ahead
   *
   * @throws IOException if we exceed our data
   */
  public void skip( int length ) throws IOException {
    // Just read the requested number of characters in the buffer
    for ( int i = 0; i < length; i++ ) {
      read();
    }
  }




  /**
   * Read and return the next character from the reader.
   *
   * @return the character read.
   *
   * @throws IOException if we exceed our data
   */
  public int read() throws IOException {
    // If the last character we found was a newline character...
    if ( lastChar == '\n' ) {
      // ...increment the line counter...
      ++lineNumber;

      // ... and set the character pointer to 0
      charNumber = 0;
    }

    // Read the next character
    int ch = reader.read();

    // Increment the current lines character pointer
    ++charNumber;

    // store the character we just read as the last character we just read
    lastChar = ch;

    // place the character in our window
    history[index] = ch;

    // If we reached the end of our window...
    if ( ++index == history.length ) {
      // ...go to the beginning of the window
      index = 0;
    }

    // return the character we just read
    return ch;
  }




  /**
   * Return the next character in the reader
   *
   * @return the character the next read operation will return
   *
   * @throws IOException if we exceed our data
   */
  public int peek() throws IOException {
    reader.mark( 1 );

    int next = reader.read();
    reader.reset();

    return next;
  }




  /**
   * Return the next token in the reader
   * 
   * @return the token the next {@code readToken()} operation will return
   *
   * @throws IOException if we exceed our data
   */
  public String peekToken() throws IOException {

    // mark our position; readAheadLimit does not matter to our simple reader 
    reader.mark( 1 );

    // read to the next delimiter
    String retval = this.readToDelimiter( defaultDelimiters );

    // reset the reader
    reader.reset();

    // return what was read
    return retval;

  }




  /**
   * Read the next token, consuming it and peek the next token after that, 
   * returning it.
   * 
   * @return the next token to be read (i.e. {@code peekToken()})
   * 
   * @throws IOException
   */
  public String readAndPeekToken() throws IOException {
    readToken();
    return peekToken();
  }




  /**
   * Return the string represented by the next given length characters in the
   * reader without advancing the reader.
   *
   * @param length how far ahead to peek
   *
   * @return the character the next read operation will return
   *
   * @throws IOException
   */
  public String peek( int length ) throws IOException {
    int[] array = new int[length];
    peek( array );

    StringBuffer buffer = new StringBuffer();

    for ( int i = 0; i < length; i++ ) {
      if ( array[i] != -1 ) {
        buffer.append( (char)array[i] );
      } else {
        break;
      }
    }

    return buffer.toString();
  }




  /**
   * Return all the text data up to the next occurrence of the given character.
   *
   * @param stop the sentinel character, when to stop
   *
   * @return all the characters up to the sentinel character
   *
   * @throws IOException if we exceed our data
   */
  public String peekToChar( char stop ) throws IOException {
    reader.mark( Integer.MAX_VALUE );

    StringBuffer buffer = new StringBuffer();

    while ( true ) {
      int c = reader.read();

      if ( ( c == -1 ) || ( c == stop ) ) {
        break;
      }

      buffer.append( (char)c );
    }

    reader.reset();

    return buffer.toString();
  }




  /**
   * Peek ahead in the parser and return the characters, in the given buffer.
   *
   * <p>The distance of the peek will be the the length of the given buffer.</p>
   *
   * @param buffer the array of integers to fill.
   *
   * @throws IOException
   */
  public void peek( int[] buffer ) throws IOException {
    reader.mark( buffer.length );

    for ( int i = 0; i < buffer.length; i++ ) {
      buffer[i] = reader.read();
    }

    reader.reset();
  }




  /**
   * Mark the given position in the reader.
   *
   * @param readAheadLimit Limit on the number of characters that may be read
   *          while still preserving the mark. After reading this many
   *          characters, attempting to reset the stream may fail.
   *
   * @throws IOException If the stream does not support mark(), or if some 
   *         other I/O error occurs
   */
  public void mark( int readAheadLimit ) throws IOException {
    reader.mark( readAheadLimit );
  }




  /**
   * Reset the stream.
   *
   * <p>If the stream has been marked, then attempt to reposition it at the
   * mark.  If the stream has not been marked, then attempt to reset it in some
   * way appropriate to the particular stream, for example by repositioning it
   * to its starting point.  Not all character-input streams support the
   * reset() operation, and some support reset() without supporting mark().<p>
   *

   * @throws IOException If the stream has not been marked, or if the mark
   *         has been invalidated, or if the stream does not support 
   *         reset(), or if some other I/O error occurs.
   */
  public void reset() throws IOException {
    reader.reset();
  }




  /**
   * Return a string that shows where the parser is currently within the reader.
   *
   * <p>The history window displays the previous characters read by the parser,
   * so the next character that is to be read is not yet in the history
   * window.</p>
   *
   * @return Context information used primarily during exceptions and debugging
   */
  public String getPosition() {
    StringBuffer buffer = new StringBuffer();

    // append our pointers
    buffer.append( "line " ).append( lineNumber ).append( ", char " ).append( charNumber );
    buffer.append( ": ..." );

    // Create the history window
    StringBuffer line = new StringBuffer();
    int count = 0;
    int start = index;

    while ( count++ < charNumber ) {
      if ( start-- == 0 ) {
        start = BUFFER_SIZE - 1; // go back one
      }

      if ( ( history[start] == '\n' ) || ( start == index ) ) {
        break;
      }

      line.append( (char)history[start] );
    }

    buffer.append( line.reverse() );

    buffer.append( " next char: " );

    try {
      if ( !eof() ) {
        buffer.append( "'" + (char)peek() + "'" );
      } else {
        buffer.append( "EOF" );
      }
    } catch ( IOException ioe ) {
      buffer.append( "ERROR: " + ioe.getMessage() );
    }

    return buffer.toString();
  }




  /**
   * Read the next character skipping any whitespace between the current
   * position and the next non-whitespace character>
   *
   * @return The next non-whitespace character in the parser.
   *
   * @throws IOException if EOF is encountered before a character is read.
   */
  public int readChar() throws IOException {
    // Skip all the whitespace
    skipWhitespace();

    // read the next non-whitespace character
    int ch = read();

    // If we are at the end of the file...
    if ( ch == -1 ) {
      // Throw an exception
      throw new IOException( "unexpected EOF" );
    }

    // return the character
    return ch;
  }




  /**
   * Read the next character, consuming it and peek the next character, 
   * returning it.
   * 
   * @return the next character to be read (i.e. {@code peek()})
   * 
   * @throws IOException
   */
  public int readAndPeek() throws IOException {
    read();
    return peek();
  }




  /**
   * Read from the reader until the given stop character is found.
   *
   * <p>The parser (reader) will be positioned to read the character
   * immediately following the given stop character.</p>
   *
   * @param stop The character past which the parser should read
   *
   * @return The string representing everything we read up to, but not
   *         including, that given character
   *
   * @throws IOException
   */
  public String readTo( int stop ) throws IOException {
    StringBuffer retval = new StringBuffer();

    // Keep reading until...
    while ( true ) {
      int c = read();

      // If we reached the end of the reader (EOF) before finding the expected
      // character...
      if ( c == -1 ) {
        // ...complain
        throw new IOException( "could not find stop char " + stop + "('" + (char)stop + "') before EOF" );
      } else {
        // If we found the stop character...
        if ( c == stop ) {
          // ... break out of the loop, we are done
          break;
        } else {
          // ...otherwise append the read character to the return buffer
          retval.append( (char)c );
        }
      }
    }

    // Return the buffer as a string
    return retval.toString();
  }




  /**
   * read the next token, or grouping of non-whitespace characters.
   *
   * @return a grouping of non-whitespace characters
   *
   * @throws IOException if life is bad at the moment.
   */
  public String readToken() throws IOException {
    return readToDelimiter( defaultDelimiters );
  }




  /**
   * Read the next token and throw an error if it does not match what is 
   * expected.
   * 
   * @param expected the expected token
   * 
   * @throws IOException if the read token does not match what is expected
   */
  public void readToken( String expected ) throws IOException {
    String got = readToken();

    if ( !expected.equals( got ) ) {
      throw new IOException( "expected \"" + expected + "\", got \"" + got + "\"" );
    }
  }




  /**
   * Reads from the parser up to one of the delimiter characters in the given
   * string.
   *
   * <p>The returned string will not include any preceding whitespace or
   *
   * @param delimiters a string of stop characters used to delimit tokens.
   *
   * @return The string of characters up, but not including the delimiter that
   *         marked the end of the token
   *
   * @throws IOException
   */
  public String readToDelimiter( String delimiters ) throws IOException {
    skipWhitespace();

    StringBuffer buffer = new StringBuffer();

    while ( true ) {
      int next = peek();

      // if we are at the end of the string, return the buffer
      if ( next == -1 ) {
        if ( buffer.length() <= 0 ) {
          throw new IOException( "unexpected EOF" );
        }
        read();
        return buffer.toString();
      }

      // if the next character is one of the delimiters return the buffer
      if ( delimiters.indexOf( next ) != -1 ) {
        if ( buffer.length() == 0 ) {
          buffer.append( (char)read() );
        }
        return buffer.toString();
      }

      // read the next character into the buffer
      buffer.append( (char)read() );
    }
  }




  /**
   * Return all the characters up to, but not including, the given pattern as a
   * String.
   *
   * <p>This method will return when either of two events occur: the given stop
   * pattern has been found or EOF has been reached.</p>
   *
   * @param pattern The stop pattern String
   *
   * @return all the characters, including whitespace that has been read up to
   *         the stop pattern or the end of the string we are parsing.
   *
   * @throws IOException if the EOF is unexpectedly reached during peeks
   */
  public String readToPattern( String pattern ) throws IOException {
    StringBuffer buffer = new StringBuffer();

    int ch = pattern.charAt( 0 );
    int length = pattern.length();

    while ( true ) {
      int next = peek();

      // Check for eof
      if ( next == -1 ) {
        // we are done, return what we have found so far
        if ( buffer.length() > 0 ) {
          return buffer.toString();
        }

        return null;
      }

      // If the character matches the first character of the stop pattern...
      if ( next == ch ) {
        // ...if the string is only one character in length, then we are done
        if ( length == 1 ) {
          if ( buffer.length() > 0 ) {
            return buffer.toString();
          }

          return null;
        } else {
          // ...otherwise see if the rest of the stop pattern is a match by
          // peeking into the reader
          int[] peekBuffer = new int[length];
          peek( peekBuffer );

          boolean match = true;

          for ( int i = 1; i < length; i++ ) {
            // If any of the characters do not match...
            if ( peekBuffer[i] != pattern.charAt( i ) ) {
              // flag a failed match
              match = false;

              // early exit
              break;
            }
          }

          // if we went through the entire peek buffer and match is still true
          if ( match ) {
            // return what we have placed in our buffer so far, leaving the
            // reader placed just before the stop pattern
            if ( buffer.length() > 0 ) {
              return buffer.toString();
            }

            return null;
          }
        }
      }

      // move on
      buffer.append( (char)read() );
    } // while(true);
  }




  /**
   * @return true if the parser is consuming comments, false otherwise
   */
  public boolean isConsumingCodeComments() {
    return consumingCodeComments;
  }




  /**
   * Set the parser to consuming (reading through) comments.
   * 
   * @param flag true to set the parser to consume/ignore comments, false to 
   *        treat them as retrievable tokens
   */
  public void setConsumingCodeComments( boolean flag ) {
    consumingCodeComments = flag;
  }




  /**
   * Read in that last comment parsed while skipping whitespace.
   *
   * @return The comment, or an empty string (not null) if no comment was found.
   */
  public String getComment() {
    return ( ( comment == null ) ? "" : comment.toString() );
  }




  /**
   * Clear out the last comment parsed while skipping whitespace.
   */
  public void clearComment() {
    comment = null;
  }




  /**
   * Read a Java style, one-line comment
   *
   * @throws IOException If EOF is encountered before a new line character.
   */
  private void readOneLineComment() throws IOException {
    if ( comment == null ) {
      comment = new StringBuffer();
    } else {
      if ( comment.length() > 0 ) {
        comment.append( "\r\n" );
      }
    }

    comment.append( readToPattern( "\n" ) );
  }




  /**
   * Read all the data into the comment buffer until a multi-line comment
   * termination string (asterisk followed by a slash) combination is found.
   *
   * @throws IOException if no comment termination is before EOF.
   */
  private void readMultiLineComment() throws IOException {
    if ( comment == null ) {
      comment = new StringBuffer();
    } else {
      if ( comment.length() > 0 ) {
        comment.append( "\r\n" );
      }
    }

    while ( true ) {
      int ch = read();

      if ( ch == -1 ) {
        throw new IOException( "missing */ on comment" );
      }

      comment.append( (char)ch );

      if ( ( ch == '*' ) && ( peek() == '/' ) ) {
        ch = read();

        comment.append( (char)ch );

        break;
      }
    }
  }

}