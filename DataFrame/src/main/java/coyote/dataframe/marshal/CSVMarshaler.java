/*
 * Copyright (c) 2014 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 *
 * Contributors:
 *   Stephan D. Cote 
 *      - Initial concept and implementation
 */
package coyote.dataframe.marshal;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.List;

import coyote.dataframe.DataField;
import coyote.dataframe.DataFrame;
import coyote.dataframe.FrameSet;


/**
 * 
 */
public class CSVMarshaler {

  /** The character used for escaping quotes. */
  public static final char ESCAPE_CHARACTER = '"';

  /** The default separator. */
  public static final char SEPARATOR = ',';

  /** The default quote character. */
  public static final char QUOTE_CHARACTER = '"';

  /** Default line terminator uses platform encoding. */
  public static final String LINE_DELIMITER = "\r\n";

  /** Initial size of StringBuilder lines */
  public static final int INITIAL_STRING_SIZE = 128;

  /** The quote constant to use when you wish to suppress all quoting. */
  public static final char NO_QUOTE_CHARACTER = '\u0000';

  /** The escape constant to use when you wish to suppress all escaping. */
  public static final char NO_ESCAPE_CHARACTER = '\u0000';




  /**
   * Reads a frameset from the named file using CSV formatting.
   * 
   * @param filename The name of the file to read.
   * 
   * @return A set of data frames representing the data in the CSV
   *  
   * @throws MarshalException if there were problems marshaling the data
   */
  public static FrameSet read( String filename ) throws MarshalException {
    File file = new File( filename );

    // perform all the necessary checks, the delegate to

    return read( file );
  }




  /**
   * @param tempFile
   * 
   * @return the frameset read in from the file
   */
  public static FrameSet read( File tempFile ) {
    FrameSet retval = new FrameSet();

    // TODO: complete this!

    return retval;
  }




  /**
   * Write the given frameset to the given named file in CSV format.
   *  
   * @param frameset
   * @param filename
   */
  public static void write( FrameSet frameset, String filename ) {
    File file = new File( filename );

    // perform all the necessary checks, the delegate to

    write( frameset, file );
  }




  /**
   * 
   * @param set
   * @param tempFile
   */
  public static void write( FrameSet set, File tempFile ) {
    Writer fwriter = null;
    try {
      fwriter = new FileWriter( tempFile );
      write( set, fwriter );
      fwriter.close();
    } catch ( final IOException e ) {
      e.printStackTrace();
    }

  }

  // / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / /

  // 




  // / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / /

  /**
   * Write the given frameset to the given writer as a CSV
   * 
   * @param set The frameset to format
   * @param writer The instance to which the formatted is to be written 
   */
  public static void write( FrameSet set, Writer writer ) {
    PrintWriter printwriter = new PrintWriter( writer );
    processHeader( set, printwriter );
    processRows( set, printwriter );
    printwriter.close();
  }




  /**
   * Generate the header row for the CSV data
   * 
   * @param frameset the set of frames containing the date from which column names are extracted.
   */
  private static void processHeader( final FrameSet frameset, PrintWriter printwriter ) {
    final List<String> names = frameset.getColumns();
    final StringBuilder retval = new StringBuilder();
    if ( names.size() > 0 ) {
      for ( final String name : names ) {
        retval.append( name );
        retval.append( SEPARATOR );
      }
      retval.deleteCharAt( retval.length() - 1 );// remove last separator
    }
    retval.append( LINE_DELIMITER );
    printwriter.write( retval.toString() );
  }




  /**
   * Process all the DataFrames as rows in this frameset.
   * 
   * @param frameset the set of frames containing the rows of data.
   */
  private static void processRows( final FrameSet frameset, PrintWriter printwriter ) {

    // List of column names in order they should be output
    final List<String> names = frameset.getColumns();

    String token = null;
    // for each one of the frames/rows
    for ( int index = 0; index < frameset.size(); index++ ) {
      final DataFrame frame = frameset.get( index );
      final StringBuilder retval = new StringBuilder();

      // for each of the columns in that row
      for ( final String name : names ) {
        // the named value for that row
        DataField field = frame.getField( name );

        // try to convert it into a string
        token = field.getStringValue();

        // escape any special characters otherwise just use the toke as is
        retval.append( stringContainsSpecialCharacters( token ) ? processLine( token ) : token );
        retval.append( ',' );
      }
      if ( retval.length() > 0 ) {
        retval.deleteCharAt( retval.length() - 1 );// remove last comma
      }

      retval.append( LINE_DELIMITER );
      printwriter.write( retval.toString() );
      printwriter.flush();
    }

  }




  private static boolean stringContainsSpecialCharacters( final String line ) {
    return ( line.indexOf( QUOTE_CHARACTER ) != -1 ) || ( line.indexOf( SEPARATOR ) != -1 ) || ( line.indexOf( ESCAPE_CHARACTER ) != -1 );
  }




  protected static StringBuilder processLine( final String token ) {
    final StringBuilder sb = new StringBuilder( INITIAL_STRING_SIZE );

    if ( token.indexOf( SEPARATOR ) != -1 )
      sb.append( QUOTE_CHARACTER );

    for ( int indx = 0; indx < token.length(); indx++ ) {
      final char nextChar = token.charAt( indx );
      if ( ( ESCAPE_CHARACTER != NO_ESCAPE_CHARACTER ) && ( nextChar == QUOTE_CHARACTER ) ) {
        sb.append( ESCAPE_CHARACTER ).append( nextChar );
      } else if ( ( ESCAPE_CHARACTER != NO_ESCAPE_CHARACTER ) && ( nextChar == ESCAPE_CHARACTER ) ) {
        sb.append( ESCAPE_CHARACTER ).append( nextChar );
      } else {
        sb.append( nextChar );
      }
    }
    if ( token.indexOf( SEPARATOR ) != -1 )
      sb.append( QUOTE_CHARACTER );

    return sb;
  }




  /**
   * Return the frame as a set of comma-delimited values.
   * 
   * <p>No field names are output and the values are listed in the order they 
   * appear in the frame.
   * 
   * @param frame The dataFrame to represent in CSV format.
   * 
   * @return The field values as strings delimited with commas, or an empty 
   *         string of no frame is given (will not return null)
   */
  public static String marshal( DataFrame frame ) {
    final StringBuilder retval = new StringBuilder();
    if ( frame != null ) {
      for ( int x = 0; x < frame.getFieldCount(); x++ ) {
        DataField field = frame.getField( x );
        String token = field.getStringValue();
        retval.append( stringContainsSpecialCharacters( token ) ? processLine( token ) : token );
        if ( x + 1 < frame.getFieldCount() ) {
          retval.append( ',' );
        }
      }
    }
    return retval.toString();
  }
}
