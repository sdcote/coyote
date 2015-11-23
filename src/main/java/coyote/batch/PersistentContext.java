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
package coyote.batch;

import java.io.File;
import java.text.ParseException;
import java.util.Date;
import java.util.List;

import coyote.commons.FileUtil;
import coyote.commons.StringUtil;
import coyote.commons.template.Template;
import coyote.dataframe.DataField;
import coyote.dataframe.DataFrame;
import coyote.dataframe.marshal.JSONMarshaler;
import coyote.dataframe.marshal.MarshalException;
import coyote.loader.log.Log;
import coyote.loader.log.LogMsg;


/**
 * This is a context which is persisted at the end of a transform and read in 
 * when it is started to persist values in the transform.
 * 
 * <p>An example use case is the sequential numbering of an output file after 
 * each run of a transform. After the transform completes successfully, its 
 * data is persisted to disk so when it initializes the next time, it can 
 * increment values to be used in naming files.</p>
 * 
 * <p>
 * 
 * <p>This class is created by the TransformEngineFactory and keys off the name of the context to determine if it is a regular context or a persistent context.
 * 
 * <p>Context are opened and closed like other components so this component has the ability to read itself from a file on opening and persist itself to disk on closing. 
 * 
 * <p>Because Persistent contexts are simple text files, they can be edited prior to their respective transforms being run
 */
public class PersistentContext extends TransformContext {
  private static final String FILENAME = "context.json";
  File contextFile = null;
  long runcount = 0;
  DataFrame configuration = null;
  Date lastRunDate = null;




  public PersistentContext( DataFrame cfg ) {
    configuration = cfg;
  }




  /**
   * @see coyote.batch.TransformContext#open()
   */
  @Override
  public void open() {

    contextFile = new File( engine.getJobDirectory(), FILENAME );
    Log.debug( "Reading context from " + contextFile.getAbsolutePath() );
    String contents = FileUtil.fileToString( contextFile );

    // fill the context with data previously persisted to the file (if any)
    if ( StringUtil.isNotBlank( contents ) ) {
      try {
        List<DataFrame> frames = JSONMarshaler.marshal( contents );
        if ( frames.get( 0 ) != null ) {
          for ( DataField field : frames.get( 0 ).getFields() ) {
            set( field.getName(), field.getObjectValue() );
          }
        }
      } catch ( MarshalException e ) {
        Log.warn( "Could not load context: " + e.getClass().getSimpleName() + " - " + e.getMessage() );
      }
    }

    incrementRunCount();

    setPreviousRunDate();

    // If we have a configuration...
    if ( configuration != null ) {
      // fill the context with configuration data
      for ( DataField field : configuration.getFields() ) {
        if ( !field.isFrame() ) {
          if ( StringUtil.isNotBlank( field.getName() ) && !field.isNull() ) {
            String token = field.getStringValue();
            String value = Template.resolve( token, engine.getSymbolTable() );
            engine.getSymbolTable().put( field.getName(), value );
            set( field.getName(), value );
          } //name-value check
        }// if frame
      } // for
    }
  }




  private void setPreviousRunDate() {
    Object value = get( Symbols.PREVIOUS_RUN_DATETIME );

    if ( value != null ) {

      // clear it from the context to reduce confusion
      set( Symbols.PREVIOUS_RUN_DATETIME,null);

      try {
        Date prevrun = Batch.DEFAULT_DATETIME_FORMAT.parse( value.toString() );

        // Set the previous run date
        set( Symbols.PREVIOUS_RUN_DATE, prevrun );

        // set the new value in the symbol table
        if ( this.symbols != null ) {
          symbols.put( Symbols.PREVIOUS_RUN_DATE, Batch.DEFAULT_DATE_FORMAT.format( prevrun ) );
          symbols.put( Symbols.PREVIOUS_RUN_TIME, Batch.DEFAULT_TIME_FORMAT.format( prevrun ) );
          symbols.put( Symbols.PREVIOUS_RUN_DATETIME, Batch.DEFAULT_DATETIME_FORMAT.format( prevrun ) );
        }

      } catch ( ParseException e ) {
        Log.warn( LogMsg.createMsg( Batch.MSG, "Context.previous_run_date_parsing_error", value, e.getClass().getSimpleName(), e.getMessage() ) );
      }
    }

  }




  /**
   * Increments the run counter by 1
   */
  private void incrementRunCount() {

    // Get the current value
    Object value = get( Symbols.RUN_COUNT );

    if ( value != null ) {
      // if a number...
      if ( value instanceof Number ) {
        // set it
        runcount = ( (Number)value ).longValue();
      } else {
        // try parsing it as a string
        try {
          runcount = Long.parseLong( value.toString() );
        } catch ( NumberFormatException e ) {
          Log.warn( "Could not parse '" + Symbols.RUN_COUNT + "'  value [" + value.toString() + "] into a number " );
        } // try
      } // numeric check
    } // !null

    // increment the counter
    runcount++;

    set( Symbols.RUN_COUNT, runcount );

    // set the new value in the symbol table
    if ( this.symbols != null ) {
      symbols.put( Symbols.RUN_COUNT, runcount );
    }

    Log.debug( "Runcount is " + runcount );
  }




  /**
   * @see coyote.batch.TransformContext#close()
   */
  @Override
  public void close() {
    super.close();

    // create a data frame to structure our data
    DataFrame frame = new DataFrame();

    // Add each property in the context to the frame
    for ( String key : properties.keySet() ) {
      try {
        frame.add( key, properties.get( key ) );
      } catch ( Exception e ) {
        Log.debug( "Cannot persist property '" + key + "' - " + e.getMessage() );
      }
    }

    // add the current value of the run counter
    frame.put( Symbols.RUN_COUNT, runcount );

    // Save the current run date
    Object rundate = get( Symbols.DATETIME );
    if ( rundate != null ) {
      // it should be a date reference
      if ( rundate instanceof Date ) {
        // format it in the default format
        frame.put( Symbols.PREVIOUS_RUN_DATETIME, Batch.DEFAULT_DATETIME_FORMAT.format( (Date)rundate ) );
      } else {
        Log.warn( LogMsg.createMsg( Batch.MSG, "Context.run_date_reset", rundate ) );
      }
    }

    // write the context to disk using JSON 
    FileUtil.stringToFile( JSONMarshaler.toFormattedString( frame ), contextFile.getAbsolutePath() );

  }
}