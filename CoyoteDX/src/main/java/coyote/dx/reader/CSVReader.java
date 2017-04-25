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
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.text.ParseException;

import coyote.commons.StringUtil;
import coyote.commons.UriUtil;
import coyote.dataframe.DataFrame;
import coyote.dataframe.DataFrameException;
import coyote.dx.CDX;
import coyote.dx.ConfigTag;
import coyote.dx.ConfigurableComponent;
import coyote.dx.FrameReader;
import coyote.dx.Symbols;
import coyote.dx.TransactionContext;
import coyote.dx.TransformContext;
import coyote.loader.cfg.ConfigurationException;
import coyote.loader.log.Log;
import coyote.loader.log.LogMsg;


/**
 * Character Separated Value Reader
 */
public class CSVReader extends AbstractFrameReader implements FrameReader, ConfigurableComponent {

  /** The component responsible for reading CSV files into frames */
  private coyote.commons.csv.CSVReader reader = null;

  /** Flag indicating all data should be loaded into and read from memory. */
  private boolean preload = false;

  /** Flag indicating that the first line of data should be considered column names. */
  private boolean hasHeader = false;

  /** The column names read in from the first line */
  private String[] header = new String[0];

  public char SEPARATOR = ',';




  /**
   * Default constructor, expecting subsequent configuration.
   */
  public CSVReader() {

  }




  /**
   * @see coyote.dx.AbstractConfigurableComponent#setConfiguration(coyote.dataframe.DataFrame)
   */
  @Override
  public void setConfiguration( DataFrame frame ) throws ConfigurationException {
    super.setConfiguration( frame );

    // Check if we are to load all the data into memory and read from there
    if ( frame.contains( ConfigTag.PRELOAD ) ) {
      try {
        preload = frame.getAsBoolean( ConfigTag.PRELOAD );
      } catch ( DataFrameException e ) {
        Log.info( "Preload not valid " + e.getMessage() );
        preload = false;
      }
    }
    Log.debug( LogMsg.createMsg( CDX.MSG, "Reader.preload_is", preload ) );

    // Check if we are to treat the first line as the header names
    if ( frame.contains( ConfigTag.HEADER ) ) {
      try {
        hasHeader = frame.getAsBoolean( ConfigTag.HEADER );
      } catch ( DataFrameException e ) {
        Log.info( "Header flag not valid " + e.getMessage() );
        hasHeader = false;
      }
    } else {
      Log.debug( "No header config" );
    }
    Log.debug( LogMsg.createMsg( CDX.MSG, "Reader.header_flag_is", hasHeader ) );

    // Check if we are to use a different separator than the default ',' (comma)
    if ( frame.contains( ConfigTag.CHARACTER ) ) {
      String value = frame.getAsString( ConfigTag.HEADER );

      if ( StringUtil.isNotEmpty( value ) ) {
        SEPARATOR = value.charAt( 0 );
      } else {
        Log.info( "Character value not valid " );
      }
    } else {
      Log.debug( "No separator config" );
    }
    Log.debug( LogMsg.createMsg( CDX.MSG, "Reader.separator_character_is", SEPARATOR, (int)SEPARATOR ) );

    // TODO: support a different separator character including "/t"

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
        String[] data = reader.readNext();

        // Deal with empty lines which may be in the file
        reader.consumeEmptyLines();

        if ( data != null ) {
          retval = new DataFrame();
          for ( int x = 0; x < data.length; x++ ) {
            retval.add( x < header.length ? header[x] : new String( "COL" + x ), data[x] );
          }
          break;
        }
      }
    } catch ( IOException | ParseException e ) {
      context.setError( e.getMessage() );
    }

    // TODO: consume empty lines: sometimes CSV files have empty lines at the end of the file...we should consume them

    // Support the concept of last frame    
    if ( reader.eof() ) {
      context.setLastFrame( true );
    }

    return retval;
  }




  /**
   * @see coyote.dx.FrameReader#eof()
   */
  @Override
  public boolean eof() {
    return reader.eof();
  }




  /**
   * @see java.io.Closeable#close()
   */
  @Override
  public void close() throws IOException {
    if ( reader != null ) {
      reader.close();
    }
  }




  /**
   * @see coyote.dx.Component#open(coyote.dx.TransformContext)
   */
  @Override
  public void open( TransformContext context ) {
    super.context = context;

    // check for a source in our configuration, if not there use the transform 
    // context as it may have been set by a previous operation
    String source = super.getString( ConfigTag.SOURCE );
    Log.debug( LogMsg.createMsg( CDX.MSG, "Reader.configured_source_is", source ) );
    if ( StringUtil.isNotBlank( source ) ) {

      File sourceFile = null;
      URI uri = UriUtil.parse( source );
      if ( uri != null ) {
        sourceFile = UriUtil.getFile( uri );
        if ( sourceFile == null ) {
          if ( uri.getScheme() == null ) {
            // Assume a file if there is no scheme
            Log.debug( "Source URI did not contain a scheme, assuming a filename" );
            sourceFile = new File( source );
          } else {
            Log.warn( LogMsg.createMsg( CDX.MSG, "Reader.source_is_not_file", source ) );
          }
        }
      } else {
        Log.debug( "Source could not be parsed into a URI, assuming a filename" );
        sourceFile = new File( source );
      }
      Log.debug( "Using a source file of " + sourceFile.getAbsolutePath() );

      // if not absolute, use the current job directory
      if ( !sourceFile.isAbsolute() ) {
        sourceFile = new File( context.getSymbols().getString( Symbols.JOB_DIRECTORY ), sourceFile.getPath() );
      }
      Log.debug( "Using an absolute source file of " + sourceFile.getAbsolutePath() );

      // Basic checks

      if ( sourceFile == null ) {
        context.setError( LogMsg.createMsg( CDX.MSG, "Reader.no_source_file_on_open", source ).toString() );
      } else {

        if ( sourceFile.exists() && sourceFile.canRead() ) {
          try {
            reader = new coyote.commons.csv.CSVReader( new FileReader( sourceFile ), SEPARATOR );
            if ( hasHeader ) {
              header = reader.readNext();
            }
          } catch ( Exception e ) {
            Log.error( "Could not create reader: " + e.getMessage() );
            context.setError( e.getMessage() );
          }
        } else {
          context.setError( LogMsg.createMsg( CDX.MSG, "Reader.could_not_read_from_source", getClass().getName(), sourceFile.getAbsolutePath() ).toString() );
        }
      }
    } else {
      Log.error( "No source specified" );
      context.setError( getClass().getName() + " could not determine source" );
    }

  }




  /**
   * @return true indicating the reader will load all available data into and read records from memory, false to get one or (batch size) at a time.
   */
  public boolean isPreload() {
    try {
      return configuration.getAsBoolean( ConfigTag.PRELOAD );
    } catch ( DataFrameException e ) {
      return false;
    }
  }




  /**
   * Set if the reader should read all data into memory first and read each 
   * record from memory.
   * 
   * <p>While this can be faster in some cases, it does take more memory to 
   * process which can affect processing.</p>
   * 
   * @param flag true to read the entire data set into memory before returning the first record, false reads data from the source on each call to the {@code read()} method. 
   */
  public void setPreload( boolean flag ) {
    configuration.put( ConfigTag.PRELOAD, flag );
  }




  /**
   * @return true for treating the first line(s) of data as a header, false means treat the first line of data as the first record.
   */
  public boolean isUsingHeader() {
    try {
      return configuration.getAsBoolean( ConfigTag.HEADER );
    } catch ( DataFrameException e ) {
      return false;
    }
  }




  /**
   * Set the reader to treat the first line(s) of data as a header.
   * 
   * @param flag true to set the read to process the first line(s) as a header, false to treat the first line as the first record.
   */
  public void setHeaderFlag( boolean flag ) {
    configuration.put( ConfigTag.HEADER, flag );
  }




  /**
   * @return the URI representing the source from which data is to be read
   */
  public String getSource() {
    return configuration.getAsString( ConfigTag.SOURCE );
  }




  /**
   * Set the URI representing the source from which data is to be read.
   * 
   * @param value The URI from which data is to be read.
   */
  public void setSource( String value ) {
    configuration.put( ConfigTag.SOURCE, value );
  }

}
