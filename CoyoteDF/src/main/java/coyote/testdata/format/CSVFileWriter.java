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
package coyote.testdata.format;

import java.util.List;

import coyote.testdata.DataSet;
import coyote.testdata.Row;


/**
 * 
 */
public class CSVFileWriter extends FileWriter {

  private final char _separator;

  private final char _quotechar;

  private final char _escapechar;

  private final String _linedelim;

  public static final int INITIAL_STRING_SIZE = 128;

  /** The character used for escaping quotes. */
  public static final char ESCAPE_CHARACTER = '"';

  /** The default separator to use if none is supplied to the constructor. */
  public static final char SEPARATOR = ',';

  /**
   * The default quote character to use if none is supplied to the
   * constructor.
   */
  public static final char QUOTE_CHARACTER = '"';

  /** The quote constant to use when you wish to suppress all quoting. */
  public static final char NO_QUOTE_CHARACTER = '\u0000';

  /** The escape constant to use when you wish to suppress all escaping. */
  public static final char NO_ESCAPE_CHARACTER = '\u0000';

  /** Default line terminator uses platform encoding. */
  public static final String LINE_DELIMITER = "\n";




  public CSVFileWriter( final String filename ) {
    super.filename = filename;
    _separator = SEPARATOR;
    _quotechar = NO_QUOTE_CHARACTER; //QUOTE_CHARACTER;
    _escapechar = ESCAPE_CHARACTER;
    _linedelim = LINE_DELIMITER;
  }




  private String processHeader( final DataSet dataset ) {
    final List<String> names = dataset.getColumnNames();
    final StringBuilder retval = new StringBuilder();
    if ( names.size() > 0 ) {
      for ( final String name : names ) {
        retval.append( name );
        retval.append( ',' );
      }
      retval.deleteCharAt( retval.length() - 1 );// remove last comma
      retval.append( _linedelim ); //end of line
    }

    return retval.toString();
  }




  protected StringBuilder processLine( final String nextElement ) {
    final StringBuilder sb = new StringBuilder( INITIAL_STRING_SIZE );
    for ( int j = 0; j < nextElement.length(); j++ ) {
      final char nextChar = nextElement.charAt( j );
      if ( ( _escapechar != NO_ESCAPE_CHARACTER ) && ( nextChar == _quotechar ) ) {
        sb.append( _escapechar ).append( nextChar );
      } else if ( ( _escapechar != NO_ESCAPE_CHARACTER ) && ( nextChar == _escapechar ) ) {
        sb.append( _escapechar ).append( nextChar );
      } else {
        sb.append( nextChar );
      }
    }

    return sb;
  }




  private boolean stringContainsSpecialCharacters( final String line ) {
    return ( line.indexOf( _quotechar ) != -1 ) || ( line.indexOf( _escapechar ) != -1 );
  }




  /**
   * @see coyote.testdata.format.Writer#write(coyote.testdata.DataSet)
   */
  @Override
  public void write( final DataSet dataset ) {
    if ( dataset == null ) {
      return;
    }
    if ( printHeader ) {
      writer.write( processHeader( dataset ) );

      printHeader = false;
    }

    // List of order column names
    final List<String> names = dataset.getColumnNames();

    // for each one of the rows
    for ( int index = 0; index < dataset.size(); index++ ) {
      final Row row = dataset.getRow( index );

      final StringBuilder sb = new StringBuilder( INITIAL_STRING_SIZE );

      // for each of the columns in that row
      for ( final String name : names ) {
        // the named value for that row
        Object obj = row.get( name );
        if ( obj == null ) {
          obj = "";
        }

        final String nextElement = obj.toString();

        if ( nextElement == null ) {
          continue;
        }

        if ( _quotechar != NO_QUOTE_CHARACTER ) {
          sb.append( _quotechar );
        }

        sb.append( stringContainsSpecialCharacters( nextElement ) ? processLine( nextElement ) : nextElement );

        if ( _quotechar != NO_QUOTE_CHARACTER ) {
          sb.append( _quotechar );
        }

        sb.append( _separator );
      }

      sb.deleteCharAt( sb.length() - 1 );// remove last comma

      sb.append( _linedelim );
      writer.write( sb.toString() );
    }
  }
}
