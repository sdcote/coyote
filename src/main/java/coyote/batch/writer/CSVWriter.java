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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import coyote.batch.Batch;
import coyote.batch.ConfigTag;
import coyote.batch.ConfigurableComponent;
import coyote.batch.ConfigurationException;
import coyote.batch.FrameWriter;
import coyote.dataframe.DataField;
import coyote.dataframe.DataFrame;
import coyote.dataframe.DataFrameException;
import coyote.loader.log.Log;
import coyote.loader.log.LogMsg;


/**
 * 
 * <pre>"Writer":{
 *   "class" : "CSVWriter",
 *   "header" : true,
 *   "dateformat" : "yyyy/MM/dd",
 *   "target" : "workfile"
 * }</pre>
 * 
 */
public class CSVWriter extends AbstractFrameWriter implements FrameWriter, ConfigurableComponent {

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

  private static final String DEFAULT_DATE_FORMAT = "yyyy/MM/dd HH:mm:ss";
  SimpleDateFormat DATEFORMAT = new SimpleDateFormat( DEFAULT_DATE_FORMAT );




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
        Log.info( LogMsg.createMsg( Batch.MSG, "Writer.header_flag_is_not_valid " + frame.getAsString( ConfigTag.HEADER ) ) );
        writeHeaders = false;
      }
    } else {
      Log.debug( "No header config" );
    }
    Log.debug( LogMsg.createMsg( Batch.MSG, "Writer.header_flag_is_set_as", writeHeaders ) );

    // Check to see if a different date format is to be used
    if ( frame.contains( ConfigTag.DATEFORMAT ) ) {
      try {
        DATEFORMAT = new SimpleDateFormat( frame.getAsString( ConfigTag.DATEFORMAT ) );
      } catch ( final Exception e ) {
        Log.warn( LogMsg.createMsg( Batch.MSG, "Writer.date_format_pattern_is_not_valid", frame.getAsString( ConfigTag.DATEFORMAT ), e.getMessage() ) );
        DATEFORMAT = new SimpleDateFormat( DEFAULT_DATE_FORMAT );
      }
    } else {
      Log.debug( LogMsg.createMsg( Batch.MSG, "Writer.using_default_date_format",DATEFORMAT.toPattern() ) );
    }
    Log.debug( LogMsg.createMsg( Batch.MSG, "Writer.date_format_pattern_set_as", DATEFORMAT.toPattern() ) );

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

    // Increment the row number
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

      if ( field != null && !field.isNull() ) {
        try {

          if ( DataField.DATE == field.getType() ) {
            Date date = (Date)field.getObjectValue();
            token = DATEFORMAT.format( date );
          } else {
            // try to convert it into a string
            token = field.getStringValue();
          }
        } catch ( Exception e ) {
          Log.error( LogMsg.createMsg( Batch.MSG, "Writer.Problems writing {} - field {}", name, field.toString() ) );
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
