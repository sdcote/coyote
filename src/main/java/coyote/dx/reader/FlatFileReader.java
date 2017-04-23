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
package coyote.dx.reader;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import coyote.commons.FileUtil;
import coyote.commons.LineIterator;
import coyote.commons.StringUtil;
import coyote.commons.UriUtil;
import coyote.dataframe.DataField;
import coyote.dataframe.DataFrame;
import coyote.dataframe.DataFrameException;
import coyote.dx.CDX;
import coyote.dx.ConfigTag;
import coyote.dx.ConfigurableComponent;
import coyote.dx.FieldDefinition;
import coyote.dx.FrameReader;
import coyote.dx.TransactionContext;
import coyote.dx.TransformContext;
import coyote.loader.log.Log;
import coyote.loader.log.LogMsg;


/**
 * <p>The parser has the ability to support multiple line formats which often 
 * occur in some EDI-style files. For example, a single file may contain 
 * Purchase Order records representing the PO header and line item details for 
 * each of those POs. In such cases, it is expected that the transforms will
 * be able to handle the different file formats read, or that filters will be 
 * in place to eliminate the unwanted record types from the transform.</p>
 */
public class FlatFileReader extends AbstractFrameReader implements FrameReader, ConfigurableComponent {

  LineIterator lines = null;
  Parser lineParser = new Parser();




  @Override
  public void open( TransformContext context ) {
    super.open( context );

    String source = getString( ConfigTag.SOURCE );
    Log.debug( LogMsg.createMsg( CDX.MSG, "Reader.using_source_uri", source ) );

    if ( StringUtil.isNotBlank( source ) ) {
      File sourceFile = null;
      URI uri = UriUtil.parse( source );
      if ( uri != null ) {
        sourceFile = UriUtil.getFile( uri );
        if ( sourceFile != null ) {
          Log.debug( LogMsg.createMsg( CDX.MSG, "Reader.using_source_file", sourceFile.getAbsolutePath() ) );
        } else {
          // try again with the file scheme in case they forgot
          uri = UriUtil.parse( "file://" + source );
          sourceFile = UriUtil.getFile( uri );
          if ( sourceFile != null ) {
            Log.debug( LogMsg.createMsg( CDX.MSG, "Reader.using_source_file", sourceFile.getAbsolutePath() ) );
          } else {
            String msg = LogMsg.createMsg( CDX.MSG, "Reader.source_uri_not_file", getClass().getName(), source ).toString();
            Log.error( msg );
            context.setError( msg );
          }
        }
      } else {
        sourceFile = new File( source );
        Log.debug( LogMsg.createMsg( CDX.MSG, "Reader.using_source_file", sourceFile.getAbsolutePath() ) );
      }

      if ( sourceFile.exists() && sourceFile.canRead() ) {
        lines = FileUtil.lineIterator( sourceFile );
      } else {
        String msg = LogMsg.createMsg( CDX.MSG, "Reader.could_not_read_from_source", getClass().getName(), sourceFile.getAbsolutePath() ).toString();
        Log.error( msg );
        context.setError( msg );
      }
    } else {
      String msg = LogMsg.createMsg( CDX.MSG, "Reader.no_source_specified", getClass().getName() ).toString();
      Log.error( msg );
      context.setError( msg );
    }

    // Now setup our field definitions
    DataFrame fieldcfg = getFrame( ConfigTag.FIELDS );
    if ( fieldcfg != null ) {

      // flag to trim values
      boolean trim = true;

      List<FieldDefinition> fields = new ArrayList<FieldDefinition>();

      for ( DataField field : fieldcfg.getFields() ) {
        try {
          DataFrame fielddef = (DataFrame)field.getObjectValue();

          // determine if values should be trimmed = defaults to true
          trim = true;
          if ( fielddef.containsIgnoreCase( ConfigTag.TRIM ) ) {
            try {
              trim = fielddef.getAsBoolean( ConfigTag.TRIM );
            } catch ( Exception e ) {
              trim = true;
            }
          }

          fields.add( new FieldDefinition( field.getName(), fielddef.getAsInt( ConfigTag.START ), fielddef.getAsInt( ConfigTag.LENGTH ), fielddef.getAsString( ConfigTag.TYPE ), fielddef.getAsString( ConfigTag.FORMAT ), trim ) );
        } catch ( Exception e ) {
          context.setError( "Problems loading field definition '" + field.getName() + "' - " + e.getClass().getSimpleName() + " : " + e.getMessage() );
          return;
        }
      }

      Log.debug( LogMsg.createMsg( CDX.MSG, "Reader.configured_field_definitions", fields.size() ) );

      // set this list of fields at the default list in the line parser
      lineParser.setDefaultFormat( fields );

    } else {
      DataFrame selectorcfg = getFrame( ConfigTag.LINEMAP );
      if ( selectorcfg != null ) {
        try {
          lineParser.configure( selectorcfg );
        } catch ( DataFrameException e ) {
          e.printStackTrace();
        }
        Log.debug( LogMsg.createMsg( CDX.MSG, "Reader.line_map_configured" ) );
      } else {
        context.setError( "There are no fields or line map configured in the reader" );
        return;
      }
    }

  }




  /**
   * @see coyote.dx.FrameReader#read(coyote.dx.TransactionContext)
   */
  @Override
  public DataFrame read( TransactionContext context ) {
    DataFrame retval = null;

    try {

      // sometimes there are blank lines in data, keep reading until data is 
      // returned or EOF
      while ( !eof() ) {
        String line = lines.nextLine();
        if ( StringUtil.isNotBlank( line ) ) {
          retval = lineParser.parse( line );

          // if there are no more lines, then set the last frame flag
          if ( !lines.hasNext() ) {
            context.setLastFrame( true );
          }
          break;
        }
      }
    } catch ( Exception e ) {
      context.setError( e.getMessage() );
    }

    // If there are no more lines to read, set the last frame flag to true in 
    // the context so components can process frames accordingly
    if ( !lines.hasNext() ) {
      context.setLastFrame( true );
    }

    return retval;
  }




  //  private DataFrame parseXXX( String line ) {
  //    DataFrame retval = new DataFrame();
  //    for ( FieldDefinition def : fields ) {
  //      retval.add( def.getName(), def.convert( line.substring( def.getStart(), def.getEnd() ) ) );
  //    }
  //    return retval;
  //  }

  @Override
  public boolean eof() {
    return !lines.hasNext();
  }

  //

  //

  //

  //

  /**
   * The thing that keeps track of the different line formats and determines when to use them
   */
  class Parser {

    private static final String KEYSTART = "keystart";
    private static final String KEYLENGTH = "keylength";
    private static final String LINES = "lines";
    private List<FieldDefinition> DEFAULT = null;

    /** The first character of the key field. Negative values start from the end of the line. */
    int keyStart = 0;

    /** How long the key is */
    int keyLength = 0;

    // Map of line types to field definition lists
    Map<String, List<FieldDefinition>> formats = new HashMap<String, List<FieldDefinition>>();




    /**
     * Configure this selector with the given data frame
     * 
     * @param cfg The configuration in the form of a dataframe
     * 
     * @throws DataFrameException
     */
    public void configure( DataFrame cfg ) throws DataFrameException {
      //configure key start
      DataField field = cfg.getFieldIgnoreCase( KEYSTART );
      keyStart = field != null ? cfg.getAsInt( field.getName() ) : 0;

      // configure key length
      field = cfg.getFieldIgnoreCase( KEYLENGTH );
      keyLength = field != null ? cfg.getAsInt( field.getName() ) : 0;

      // find the lines section
      field = cfg.getFieldIgnoreCase( LINES );
      if ( field != null ) {
        if ( field.isFrame() ) {

          // This should be a set of named frames 
          // The name of which is used to map the line definitions 

          // each field should itself be a frame so get the fields from this frame
          for ( DataField linecfg : cfg.getAsFrame( field.getName() ).getFields() ) {

            if ( StringUtil.isNotBlank( linecfg.getName() ) ) {

              if ( linecfg.isFrame() ) {
                List<FieldDefinition> fieldsx = new ArrayList<FieldDefinition>();

                for ( DataField linedef : ( (DataFrame)linecfg.getObjectValue() ).getFields() ) {

                  try {
                    DataFrame fielddef = (DataFrame)linedef.getObjectValue();

                    // determine if values should be trimmed for this field
                    boolean trim;
                    try {
                      trim = fielddef.getAsBoolean( ConfigTag.TRIM );
                    } catch ( Exception e ) {
                      trim = false;
                    }

                    fieldsx.add( new FieldDefinition( linedef.getName(), fielddef.getAsInt( ConfigTag.START ), fielddef.getAsInt( ConfigTag.LENGTH ), fielddef.getAsString( ConfigTag.TYPE ), fielddef.getAsString( ConfigTag.FORMAT ), trim ) );
                  } catch ( Exception e ) {
                    context.setError( "Problems loading field definition '" + field.getName() + "' - " + e.getClass().getSimpleName() + " : " + e.getMessage() );
                    return;
                  }
                }

                //           

                formats.put( linecfg.getName(), fieldsx );
              } else {
                Log.error( "The line configuration '" + linecfg.getName() + "' is not a section" );
              }

            } else {
              Log.warn( "cannot add unnamed lines section" );
            }
          }
        } else {
          throw new DataFrameException( "lines configuration should be a section, scalar found" );
        }
      } else {
        throw new DataFrameException( "No lines defined in linemap" );
      }
    }




    public void setDefaultFormat( List<FieldDefinition> fields ) {
      DEFAULT = fields;
    }




    public DataFrame parse( String line ) {
      DataFrame retval = new DataFrame();

      // This is the list of field definitions which will extract data from the string
      List<FieldDefinition> format = null;

      // if we only have one format to deal with, use that
      if ( DEFAULT != null ) {
        format = DEFAULT;
      } else {
        // look up the name of the format to use based on a key value somewhere in the line 
        String key;
        if ( keyStart >= 0 ) {
          key = line.substring( keyStart, keyStart + keyLength );
        } else {
          // negative starting position indicated a position from the end of the line
          int start = ( line.length() + keyStart );
          key = line.substring( start, start + keyLength );
        }

        format = formats.get( key );
        if ( format == null ) {
          Log.warn( LogMsg.createMsg( CDX.MSG, "Reader.could_not_find_format_for_key", key, context.getRow() + 1, keyStart, keyLength ) );
        } else {
          if ( Log.isLogging( Log.DEBUG_EVENTS ) )
            Log.debug( LogMsg.createMsg( CDX.MSG, "Reader.parsing_record_type", key, context.getRow() + 1 ) );
        }
      }

      for ( FieldDefinition def : format ) {
        retval.add( def.getName(), def.convert( line.substring( def.getStart(), def.getEnd() ) ) );
      }
      return retval;
    }

  }

}
