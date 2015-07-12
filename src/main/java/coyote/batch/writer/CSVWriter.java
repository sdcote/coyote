/*
 * Copyright (c) 2015 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 *
 * Contributors:
 *   Stephan D. Cote 
 *      - Initial concept and initial implementation
 */
package coyote.batch.writer;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import coyote.batch.ConfigTag;
import coyote.batch.ConfigurableComponent;
import coyote.batch.ConfigurationException;
import coyote.batch.FrameWriter;
import coyote.dataframe.DataField;
import coyote.dataframe.DataFrame;
import coyote.dataframe.DataFrameException;


/**
 * 
 */
public class CSVWriter extends AbstractFrameWriter implements FrameWriter, ConfigurableComponent {

  /** The logger for the base class */
  final Logger log = LoggerFactory.getLogger( getClass() );

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
   * Place the token (cell) within quotes if necessary, escaping the necessary 
   * characters where appropriate.
   * 
   * @param token The data to process
   * 
   * @return a string builder with the data representing the processed line.
   */
  private static StringBuilder processToken( final String token ) {
    final StringBuilder sb = new StringBuilder( INITIAL_STRING_SIZE );

    // determine if we are to surround the token in quotes
    final boolean surroundToken = tokenContainsSpecialCharacters( token );

    // start the quoted string
    if ( surroundToken ) {
      sb.append( QUOTE_CHARACTER );
    }

    // now make sure we escape characters in the quoted string appropriately
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

    // end the quoted string
    if ( surroundToken ) {
      sb.append( QUOTE_CHARACTER );
    }

    return sb;
  }




  /**
   * Determine if we need to surround the token in quotes
   * 
   * @param token the string to check
   * 
   * @return true if the token contains special characters and needs to be surrounded in quotes, false otherwise
   */
  private static boolean tokenContainsSpecialCharacters( final String token ) {
    return ( token.indexOf( QUOTE_CHARACTER ) != -1 ) || ( token.indexOf( SEPARATOR ) != -1 ) || ( token.indexOf( ESCAPE_CHARACTER ) != -1 ) || ( token.contains( "\n" ) ) || ( token.contains( "\r" ) );
  }

  private boolean writeHeaders = true;

  private final List<String> columns = new ArrayList<String>();




  /**
   * @return true indicates the writer will write a header before the data, false otherwise
   */
  public boolean isUsingHeader() {
    try {
      return configuration.getAsBoolean( ConfigTag.HEADER );
    } catch ( final DataFrameException e ) {
      return false;
    }
  }




  /**
   * @see coyote.batch.AbstractConfigurableComponent#setConfiguration(coyote.dataframe.DataFrame)
   */
  @Override
  public void setConfiguration( final DataFrame frame ) throws ConfigurationException {
    super.setConfiguration( frame );

    // Check if we are to treat the first line as the header names
    if ( frame.contains( ConfigTag.HEADER ) ) {
      try {
        writeHeaders = frame.getAsBoolean( ConfigTag.HEADER );
      } catch ( final DataFrameException e ) {
        log.info( "Header flag not valid " + e.getMessage() );
        writeHeaders = false;
      }
    } else {
      log.debug( "No header config" );
    }
    log.debug( "Header flag is set to {}", writeHeaders );

  }




  /**
   * Set whether or not the writer should output a header before writing data.
   * 
   * @param flag true to instruct the writer to write a header before the first line of data, false to skip writing the header.
   */
  public void setHeaderFlag( final boolean flag ) {
    configuration.put( ConfigTag.HEADER, flag );
  }




  /**
   * @see coyote.batch.writer.AbstractFrameWriter#write(coyote.dataframe.DataFrame)
   */
  @Override
  public void write( final DataFrame frame ) {
    // The first frame sets the columns and column order
    if ( rowNumber == 0 ) {

      for ( final DataField field : frame.getFields() ) {
        columns.add( field.getName() );
      }
      if ( isUsingHeader() ) {
        writeHeader();
      }
    }

    // Increment to row number
    rowNumber++;

    // write the frame
    writeRow( frame );

  }




  /**
   * Generate the header row for the CSV data
   */
  private void writeHeader() {
    final StringBuilder retval = new StringBuilder();
    if ( columns.size() > 0 ) {
      for ( final String name : columns ) {
        retval.append( name );
        retval.append( SEPARATOR );
      }
      retval.deleteCharAt( retval.length() - 1 );// remove last separator
    }
    retval.append( LINE_DELIMITER );
    printwriter.write( retval.toString() );

  }




  /**
   * Write a single row of data.
   * 
   * @param frame the row of data to write.
   */
  private void writeRow( final DataFrame frame ) {

    String token = null;
    final StringBuilder retval = new StringBuilder();

    // for each of the columns in that row
    for ( final String name : columns ) {
      // the named value for that row
      final DataField field = frame.getField( name );

      if ( field != null ) {
        try {
          // try to convert it into a string
          token = field.getStringValue();
        } catch ( Exception e ) {
          log.error( "Problems writing {} - field {}", name, field.toString() );
          token = "";
        }
      } else {
        // not sure why this happens...
        token = "";
      }
      
      // escape any special characters otherwise just use the token as is
      retval.append( tokenContainsSpecialCharacters( token ) ? processToken( token ) : token );
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
