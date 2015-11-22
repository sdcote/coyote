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
import java.util.List;

import coyote.commons.FileUtil;
import coyote.commons.StringUtil;
import coyote.commons.template.Template;
import coyote.dataframe.DataField;
import coyote.dataframe.DataFrame;
import coyote.dataframe.marshal.JSONMarshaler;
import coyote.dataframe.marshal.MarshalException;
import coyote.loader.log.Log;


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
        Log.warn( "Cannot persist property'" + key + "' - " + e.getMessage() );
      }
    }

    // add the current value of the run counter
    frame.put( Symbols.RUN_COUNT, runcount );

    // write the context to disk using JSON 
    FileUtil.stringToFile( JSONMarshaler.toFormattedString( frame ), contextFile.getAbsolutePath() );

  }
}
