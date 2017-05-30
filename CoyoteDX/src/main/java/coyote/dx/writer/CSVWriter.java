/*
 * Copyright (c) 2015 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 *
 * Contributors:
 *   Stephan D. Cote 
 *      - Initial concept and implementation
 */
package coyote.dx.writer;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import coyote.dataframe.DataField;
import coyote.dataframe.DataFrame;
import coyote.dataframe.DataFrameException;
import coyote.dx.CDX;
import coyote.dx.ConfigTag;
import coyote.dx.ConfigurableComponent;
import coyote.dx.FieldDefinition;
import coyote.dx.FrameWriter;
import coyote.dx.context.TransformContext;
import coyote.loader.cfg.Config;
import coyote.loader.cfg.ConfigurationException;
import coyote.loader.log.Log;
import coyote.loader.log.LogMsg;


/**
 * Writes dataframes to a RFC 4180 formatted file.
 * 
 * <p>This is a basic CSV writer which takes dataframes as input and writes 
 * them to a file suitable for import into other systems. It's easy to 
 * configure:<pre>
 * "Writer":{
 *   "class" : "CSVWriter",
 *   "target" : "workfile.csv"
 * }</pre>
 * 
 * <p>The configuration may also contain a fields section which defines the 
 * order the fields are written and controls any desired formatting:<pre>
 *  "fields": {
 *    "fieldname": { "trim": true },
 *    "category": {},
 *    "receive_date": { "format": "MMddYYYY" },
 *    "count": { "format" : "0000.00" }
 *  }</pre>
 *  
 *  <p>The writer also supports other configuration properties:<pre>
 *   "header" : true,
 *   "dateformat" : "yyyy/MM/dd",</pre>
 *  
 */
public class CSVWriter extends AbstractFrameFileWriter implements FrameWriter, ConfigurableComponent {

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

  private boolean writeHeaders = true;

  /** The list of fields we are to write in the order they are to be written */
  private final List<FieldDefinition> fields = new ArrayList<FieldDefinition>();

  public static final char separator = SEPARATOR;




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
    return ( token.indexOf( QUOTE_CHARACTER ) != -1 ) || ( token.indexOf( separator ) != -1 ) || ( token.indexOf( ESCAPE_CHARACTER ) != -1 ) || ( token.contains( "\n" ) ) || ( token.contains( "\r" ) );
  }




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
   * @see coyote.dx.writer.AbstractFrameFileWriter#open(coyote.dx.context.TransformContext)
   */
  @Override
  public void open( final TransformContext context ) {
    // open the super class first
    super.open( context );

    // Now setup our field definitions if they exist
    final DataFrame fieldcfg = getFrame( ConfigTag.FIELDS );

    if ( fieldcfg != null ) {
      boolean trim = false;// flag to trim values
      String format = null;

      for ( final DataField field : fieldcfg.getFields() ) {
        try {
          final DataFrame fielddef = (DataFrame)field.getObjectValue();

          // if the field definition is empty "{}" the it will be null
          if ( fielddef != null ) {

            // determine if values should be trimmed for this field
            try {
              trim = fielddef.getAsBoolean( ConfigTag.TRIM );
            } catch ( final Exception e ) {
              trim = false;
            }

            // extract the format string (if any)
            format = fielddef.getAsString( ConfigTag.FORMAT );
          } else {
            // apparently an empty body 
            trim = false;
            format = null;
          }

          fields.add( new FieldDefinition( field.getName(), format, trim ) );

        } catch ( final Exception e ) {
          context.setError( "Problems loading field definition '" + field.getName() + "' - " + e.getClass().getSimpleName() + " : " + e.getMessage() );
          return;
        }
      }
    }

  }




  /**
   * @see coyote.dx.AbstractConfigurableComponent#setConfiguration(coyote.loader.cfg.Config)
   */
  @Override
  public void setConfiguration( final Config cfg ) throws ConfigurationException {
    super.setConfiguration( cfg );

    // Check if we are to treat the first line as the header names
    if ( cfg.contains( ConfigTag.HEADER ) ) {
      try {
        writeHeaders = cfg.getAsBoolean( ConfigTag.HEADER );
      } catch ( final DataFrameException e ) {
        Log.info( LogMsg.createMsg( CDX.MSG, "Writer.header_flag_is_not_valid " + cfg.getAsString( ConfigTag.HEADER ) ) );
        writeHeaders = false;
      }
    } else {
      Log.debug( "No header config" );
    }
    Log.debug( LogMsg.createMsg( CDX.MSG, "Writer.header_flag_is_set_as", writeHeaders ) );

    // Check to see if a different date format is to be used
    if ( cfg.contains( ConfigTag.DATEFORMAT ) ) {
      try {
        DATEFORMAT = new SimpleDateFormat( cfg.getAsString( ConfigTag.DATEFORMAT ) );
      } catch ( final Exception e ) {
        Log.warn( LogMsg.createMsg( CDX.MSG, "Writer.date_format_pattern_is_not_valid", cfg.getAsString( ConfigTag.DATEFORMAT ), e.getMessage() ) );
        DATEFORMAT = new SimpleDateFormat( DEFAULT_DATE_FORMAT );
      }
    } else {
      Log.debug( LogMsg.createMsg( CDX.MSG, "Writer.using_default_date_format", DATEFORMAT.toPattern() ) );
    }
    Log.debug( LogMsg.createMsg( CDX.MSG, "Writer.date_format_pattern_set_as", DATEFORMAT.toPattern() ) );

    // TODO: support a different separator character including "/t"
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
   * @see coyote.dx.writer.AbstractFrameFileWriter#write(coyote.dataframe.DataFrame)
   */
  @Override
  public void write( final DataFrame frame ) {

    // If there is a conditional expression
    if ( expression != null ) {

      try {
        // if the condition evaluates to true...
        if ( evaluator.evaluateBoolean( expression ) ) {
          writeFrame( frame );
        }
      } catch ( final IllegalArgumentException e ) {
        Log.warn( LogMsg.createMsg( CDX.MSG, "Writer.boolean_evaluation_error", expression, e.getMessage() ) );
      }
    } else {
      // Unconditionally writing frame
      writeFrame( frame );
    }

  }




  /**
   * This is where we actually write the frame.
   * 
   * @param frame the frame to be written
   */
  private void writeFrame( final DataFrame frame ) {
    // The first frame sets the columns and column order
    if ( rowNumber == 0 ) {

      // If we have no field definitions, create a set
      if ( fields.size() < 1 ) {
        String format = null;

        for ( final DataField field : frame.getFields() ) {

          if ( field.getType() == DataField.DATE ) {
            format = DEFAULT_DATE_FORMAT;
          } else {
            format = null;
          }
          fields.add( new FieldDefinition( field.getName(), field.getTypeName(), format, false ) );
        }
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
    if ( fields.size() > 0 ) {
      for ( final FieldDefinition def : fields ) {
        retval.append( def.getName() );
        retval.append( separator );
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
    for ( final FieldDefinition def : fields ) {
      // the named value for that row
      final DataField field = frame.getField( def.getName() );

      if ( ( field != null ) && !field.isNull() ) {
        try {

          // if there is a formatter for this field, format the value
          if ( def.hasFormatter() ) {
            token = def.getFormattedValue( field );
          } else {
            token = field.getStringValue();
          }

          // if the value is to be trimmed, remove leading and trailing spaces
          if ( def.isTrimming() ) {
            token = token.trim();
          }

        } catch ( final Exception e ) {
          Log.error( LogMsg.createMsg( CDX.MSG, "Writer.Problems writing {%s} - field {%s}", def.getName(), field.toString() ) );
          token = "";
        }
      } else {
        // handle null fields with an empty strin value
        token = "";
      }

      // escape any special characters otherwise just use the token as is
      retval.append( tokenContainsSpecialCharacters( token ) ? processToken( token ) : token );
      retval.append( separator );
    }
    if ( retval.length() > 0 ) {
      retval.deleteCharAt( retval.length() - 1 );// remove last comma
    }

    retval.append( LINE_DELIMITER );
    printwriter.write( retval.toString() );
    printwriter.flush();

  }

}
