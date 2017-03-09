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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import coyote.batch.mapper.DefaultFrameMapper;
import coyote.batch.mapper.MappingException;
import coyote.batch.validate.ValidationException;
import coyote.commons.ExceptionUtil;
import coyote.commons.FileUtil;
import coyote.commons.GUID;
import coyote.commons.StringUtil;
import coyote.commons.template.SymbolTable;
import coyote.commons.template.Template;
import coyote.dataframe.DataFrame;
import coyote.loader.Loader;
import coyote.loader.log.Log;
import coyote.loader.log.LogMsg;


/**
 * 
 */
public abstract class AbstractTransformEngine extends AbstractConfigurableComponent implements TransformEngine, ConfigurableComponent {

  /** Tasks to perform prior to the transform. (e.g. Read from FTP site) */
  protected List<TransformTask> preProcesses = new ArrayList<TransformTask>();

  /** The component which will read frames into the transformation engine.*/
  protected FrameReader reader = null;

  /** List of filters which will remove unwanted frames from the transformation stream */
  protected List<FrameFilter> filters = new ArrayList<FrameFilter>();

  /** List of validation rule from the stream. The frame must match all validators to continue on through the stream. */
  protected List<FrameValidator> validators = new ArrayList<FrameValidator>();

  /** The list of transformations to be applied to the frame. */
  protected List<FrameTransform> transformers = new ArrayList<FrameTransform>();

  /** The mapping of the frame to a new frame; the component which create the desired frame. */
  protected FrameMapper mapper = null;

  /** The list of writers which will be given the chance to record the frame somewhere. */
  protected List<FrameWriter> writers = new ArrayList<FrameWriter>();

  /** The list of tasks to perform after the transformation is complete. (e.g. posting a file to a FTP site) */
  protected List<TransformTask> postProcesses = new ArrayList<TransformTask>();

  /** The context for the entire transformation instance (i.e. job) */
  protected TransformContext transformContext = null;

  /** A list of components interested in context events */
  protected List<ContextListener> listeners = new ArrayList<ContextListener>();

  /** A symbol table to support basic template functions */
  protected static final SymbolTable symbols = new SymbolTable();

  /** The directory this engine uses for file operations */
  private File jobDirectory = null;

  /** The shared directory where multiple engines may read and write data. */
  private File workDirectory = null;

  /** The current frame number */
  protected volatile long currentFrameNumber = 0;

  /** The facade to log management functions */
  protected LogManager logManager = null;




  public AbstractTransformEngine() {

    // Fill the symbol table with system properties
    symbols.readSystemProperties();

  }




  /**
   * Return the working directory for this engine.
   * 
   * @see coyote.batch.TransformEngine#getJobDirectory()
   */
  @Override
  public File getJobDirectory() {
    return jobDirectory;
  }




  /**
   * Set the private directory for this engine.
   * 
   * @param dir the private directory to set
   */
  @SuppressWarnings("unchecked")
  protected void setJobDirectory( File dir ) {
    jobDirectory = dir;
    if ( dir != null ) {
      symbols.put( Symbols.JOB_DIRECTORY, jobDirectory.getAbsolutePath() );
    } else {
      symbols.put( Symbols.JOB_DIRECTORY, null );
    }
  }




  /**
   * Return the common working directory.
   * 
   * @see coyote.batch.TransformEngine#getWorkDirectory()
   */
  @Override
  public File getWorkDirectory() {
    return workDirectory;
  }




  /**
   * @param dir the common working directory to set
   */
  @SuppressWarnings("unchecked")
  public void setWorkDirectory( File dir ) {
    workDirectory = dir;
    if ( dir != null ) {
      symbols.put( Symbols.WORK_DIRECTORY, workDirectory.getAbsolutePath() );
    } else {
      symbols.put( Symbols.WORK_DIRECTORY, null );
    }
  }




  /**
   * @see java.lang.Runnable#run()
   */
  @Override
  public void run() {
    Log.trace( "Engine '" + getName() + "' running..." );

    // figure out our job directory
    determineJobDirectory();

    // prime the symbol table with the run date/time
    Date rundate = setRunDate();

    // Make sure the engine has a name
    if ( StringUtil.isBlank( getName() ) ) {
      setName( GUID.randomGUID().toString() );
    }

    if ( reader == null && writers.size() > 0 )
      throw new IllegalStateException( "No reader configured, nothing to write" );

    // Make sure we have a context
    if ( getContext() == null ) {
      // Create a transformation context for components to share data
      setContext( new TransformContext() );
    }

    // get the command line arguments from the symbol table and post the array
    // in the context for other components to use
    getCommandLineArguments();

    // set our list of listeners in the context 
    getContext().setListeners( listeners );

    // Set this engine as the context's engine
    getContext().setEngine( this );

    // Set the symbol table to the one this engine uses
    getContext().setSymbols( symbols );

    // Open / initialize the context - This is done before the listeners are 
    // opened so the listeners can be opened within an initialized context and 
    // have their configuration arguments resolved. The trade-off is that 
    // listeners will never have their open event fired on the opening of the 
    // Transform Context, only Transaction Contexts.  
    getContext().open();

    // Open all the listeners first so the transform context will trigger
    for ( ContextListener listener : listeners ) {
      listener.open( getContext() );
      if ( getContext().isInError() ) {
        reportTransformContextError( getContext() );
        return;
      }
    }

    // Set the run date in the context after it was opened (possibly loaded)
    getContext().set( Symbols.DATETIME, rundate );

    // Open the log manager with the current transform context
    if ( logManager != null )
      logManager.open( getContext() );

    Log.trace( "Engine '" + getName() + "' starting transform; rundate:" + rundate );

    // fire the transformation start event
    getContext().start();

    // Execute all the pre-processing tasks
    for ( TransformTask task : preProcesses ) {
      try {
        if ( task.isEnabled() ) {
          task.open( getContext() );
          task.execute();
        }
      } catch ( TaskException e ) {
        getContext().setError( e.getMessage() );
        getContext().setStatus( "Pre-Processing Error" );
        break;
      }
    }
    // Close all the tasks after pre-processing is done regardless of outcome
    for ( TransformTask task : preProcesses ) {
      try {
        if ( task.isEnabled() ) {
          task.close();
        }
      } catch ( IOException e ) {
        Log.warn( LogMsg.createMsg( Batch.MSG, "Engine.problems_closing_preprocess_task", task.getClass().getName(), e.getClass().getSimpleName(), e.getMessage() ) );
      }
    }

    try {
      // If pre-processing completed without error, start opening the rest of the 
      // tooling
      if ( getContext().isNotInError() ) {

        // If the reader is not null, open the core components using this context 
        // to share data. If the reader is null, there is no need to open the 
        // mapper and the writer
        if ( reader != null ) {
          reader.open( getContext() );
          if ( getContext().isInError() ) {
            reportTransformContextError( getContext() );
            return;
          }

          // if no mapper, just use the default mapper with default settings
          if ( mapper == null ) {
            Log.debug( "No mapper defined...using default settings" );
            mapper = new DefaultFrameMapper();
          }

          mapper.open( getContext() );
          if ( getContext().isInError() ) {
            reportTransformContextError( getContext() );
            return;
          }

          // Open all the writers
          for ( FrameWriter writer : writers ) {
            writer.open( getContext() );
            if ( getContext().isInError() ) {
              reportTransformContextError( getContext() );
              return;
            }
          }
        }

        // Open all the filters
        for ( FrameFilter filter : filters ) {
          filter.open( getContext() );
          if ( getContext().isInError() ) {
            reportTransformContextError( getContext() );
            return;
          }
        }

        // Open all the validators
        for ( FrameValidator validator : validators ) {
          validator.open( getContext() );
          if ( getContext().isInError() ) {
            reportTransformContextError( getContext() );
            return;
          }
        }

        // Open all the transformers
        for ( FrameTransform transformer : transformers ) {
          transformer.open( getContext() );
          if ( getContext().isInError() ) {
            reportTransformContextError( getContext() );
            return;
          }
        }
        Log.trace( "Engine '" + getName() + "' entering read loop" );

        // loop through all data read in by the reader until EOF or an error in 
        // the transform context occurs.
        while ( getContext().isNotInError() && reader != null && !reader.eof() ) {

          // Create a new Transaction context with the list of listeners to react 
          // to events in the transaction.
          TransactionContext txnContext = new TransactionContext( getContext() );
          // place a reference to the transaction in the transform context
          getContext().setTransaction( txnContext );

          // Create a component to place in the Templates to give them access to
          // all the data in the contexts and advanced functions
          TemplateAccess access = new TemplateAccess( getContext() );
          Template.put( "Context", access );

          // Start the clock and fire event listeners for the beginning of the
          // transaction
          txnContext.start();

          // Read a frame into the given context (source frame)
          DataFrame retval = reader.read( txnContext );

          // Sometimes readers read empty lines and the like, skip null dataframes!!!
          if ( retval != null ) {

            // Set the returned dataframe into the transaction context
            txnContext.setSourceFrame( retval );
            // increment the row number in the contexts
            txnContext.setRow( ++currentFrameNumber );
            getContext().setRow( currentFrameNumber );
            getContext().getSymbols().put( Symbols.CURRENT_FRAME, currentFrameNumber );
            getContext().getSymbols().put( Symbols.LAST_FRAME, txnContext.isLastFrame() );

            // fire the read event in all the listeners
            txnContext.fireRead( txnContext, reader );

            //log.debug( "row {} - {}", context.getRow(), context.getSourceFrame().toString() );

            // ...pass it through the filters...
            for ( FrameFilter filter : filters ) {
              if ( !filter.process( txnContext ) ) {
                // filter signaled to discontinue filter checks (early exit)
                break;
              }
              if ( txnContext.getWorkingFrame() == null )
                break;
            }

            // If the working frame did not get filtered out...
            if ( txnContext.getWorkingFrame() != null ) {

              boolean passed = true;
              // pass it through the validation rules - errors are logged
              for ( FrameValidator validator : validators ) {
                try {
                  if ( !validator.process( txnContext ) ) {
                    passed = false;
                  }
                } catch ( ValidationException e ) {
                  txnContext.setError( e.getMessage() );
                }
              }

              // if there were validation errors
              if ( !passed ) {
                // fire the event for any listeners
                getContext().fireFrameValidationFailed( txnContext );
              }

              if ( txnContext.isNotInError() ) {

                // Pass the working frame through the transformers
                for ( FrameTransform transformer : transformers ) {
                  try {
                    // Have the transformer process the frame
                    DataFrame resultFrame = transformer.process( txnContext.getWorkingFrame() );

                    // place the results of the transformation in the context
                    txnContext.setWorkingFrame( resultFrame );

                  } catch ( Exception e ) {
                    // catch any manner of transformation exception
                    txnContext.setError( e.getMessage() );
                    txnContext.setStatus( "Transformation Error" );
                  }
                }

                // Pass it through the mapper - only the required fields should 
                // exist in the target frame after the mapper is done.
                if ( txnContext.isNotInError() ) {

                  // We need to create a target frame into which the mapper will 
                  // place fields...
                  if ( txnContext.getTargetFrame() == null ) {
                    txnContext.setTargetFrame( new DataFrame() );
                  }

                  // Map / Move fields from the working to the target frame
                  try {
                    mapper.process( txnContext );
                  } catch ( MappingException e ) {
                    txnContext.setError( e.getMessage() );
                    txnContext.setStatus( "Mapping Error" );
                  }
                }

                // It is possible that a transform has been configured to simply
                // read and validate data so the writer may be null
                if ( txnContext.isNotInError() && writers.size() > 0 ) {
                  // Pass the frame to all the writers
                  for ( FrameWriter writer : writers ) {
                    try {
                      // Write the target (new) frame
                      writer.write( txnContext.getTargetFrame() );
                      txnContext.fireWrite( txnContext, writer );
                    } catch ( Exception e ) {
                      Log.warn( LogMsg.createMsg( Batch.MSG, "Engine.write_error", e.getClass().getSimpleName(), e.getMessage(), ExceptionUtil.stackTrace( e ) ) );
                    }
                  }
                }

              } // passed validators

            } // passed filters

            // Now end the transaction which should fire any listeners in the 
            // context to record the transaction if so configured
            txnContext.end();

          } // if something was read in

        } // Reader !eof and context is without error

      } // transformContext ! err after pre-processing

      Log.trace( "Engine '" + getName() + "' reads completed - Error=" + getContext().isInError() + " EOF=" + reader == null ? "NoReader" : reader.eof() + " Reads=" + getContext().getRow() );

      if ( getContext().isInError() ) {
        reportTransformContextError( getContext() );
      } else {

        // close any internal components like readers and writers which may 
        // interfere with post-processing tasks from completing properly
        closeInternalComponents();

        // Execute all the post-processing tasks
        for ( TransformTask task : postProcesses ) {
          try {
            if ( task.isEnabled() ) {
              task.open( getContext() );
              task.execute();
            }
          } catch ( TaskException e ) {
            getContext().setError( e.getMessage() );
            getContext().setStatus( "Post-Processing Error" );
          }
        }

        // Close all the tasks after post-processing is done regardless of outcome
        for ( TransformTask task : postProcesses ) {
          try {
            if ( task.isEnabled() ) {
              task.close();
            }
          } catch ( IOException e ) {
            Log.warn( LogMsg.createMsg( Batch.MSG, "Engine.problems_closing_postprocess_task", task.getClass().getName(), e.getClass().getSimpleName(), e.getMessage() ) );
          }
        }

        if ( getContext().isInError() ) {
          reportTransformContextError( getContext() );
        }
      }
    }
    finally {
      // signal the end of the context
      getContext().end();

      // close all the tooling, it will be re-opened when we run the next time (if scheduled)
      closeTooling();

      // reset the frame pointer
      currentFrameNumber = 0;
    }

    Log.trace( "Engine '" + getName() + "' transformation completed" );

    // reset the context so it can be reused in the next run (when scheduled)
    getContext().reset();
  }




  /**
   * 
   */
  private void getCommandLineArguments() {
    List<String> list = new ArrayList<String>();

    // retrieve a list of command line arguments from the symbol table in order
    for ( int x = 0; x < 1024; x++ ) {
      Object obj = getSymbolTable().get( Symbols.COMMAND_LINE_ARG_PREFIX + x );
      if ( obj != null ) {
        list.add( obj.toString() );
      } else {
        break; // ran out of arguments
      }
    }

    getContext().set( ContextKey.COMMAND_LINE_ARGS, list.toArray( new String[list.size()] ) );
  }




  /**
   * Determines the proper value for this engines working directory.
   * 
   * If there is no working directory set, use batch home
   *  and create a working directory within it.
   */
  private void determineJobDirectory() {
    File jobDir;

    // Make sure we have a home directory
    if ( StringUtil.isBlank( System.getProperty( Loader.APP_HOME ) ) ) {
      System.setProperty( Loader.APP_HOME, System.getProperty( "user.dir" ) );
    }

    // Use our name to setup a job directory
    String name = getName();
    if ( StringUtil.isNotBlank( name ) ) {
      // replace the illegal filename characters by only allowing simple names
      String dirname = name.trim().replaceAll( "[^a-zA-Z0-9.-]", "_" );
      jobDir = new File( System.getProperty( Job.APP_WORK ) + FileUtil.FILE_SEPARATOR + dirname );

      try {
        jobDir.mkdirs();
        setJobDirectory( jobDir );
        setWorkDirectory( jobDir.getParentFile() );
        Log.debug( LogMsg.createMsg( Batch.MSG, "Engine.calculated_job_directory", jobDir.getAbsolutePath(), jobDir.getParentFile() ) );

      } catch ( final Exception e ) {
        Log.error( e.getMessage() );
      }
    } else {
      // unnamed jobs just use the current directory
      jobDir = new File( System.getProperty( "user.dir" ) );
      setJobDirectory( jobDir );
      setWorkDirectory( jobDir );
    }
  }




  @SuppressWarnings("unchecked")
  protected Date setRunDate() {

    // Place date and time values in the symbol table
    Calendar cal = Calendar.getInstance();

    Date rundate = new Date();
    if ( rundate != null ) {
      cal.setTime( rundate );

      symbols.put( Symbols.DATE, formatDate( rundate ) );
      symbols.put( Symbols.TIME, formatTime( rundate ) );
      symbols.put( Symbols.DATETIME, formatDateTime( rundate ) );

      // duplicates the above, but in the case of persistent contexts, make for more readable configuration files when combined with PREVIOUS_RUN_*
      symbols.put( Symbols.CURRENT_RUN_DATE, formatDate( rundate ) );
      symbols.put( Symbols.CURRENT_RUN_TIME, formatTime( rundate ) );
      symbols.put( Symbols.CURRENT_RUN_DATETIME, formatTime( rundate ) );

      symbols.put( Symbols.MONTH, String.valueOf( cal.get( Calendar.MONTH ) + 1 ) );
      symbols.put( Symbols.DAY, String.valueOf( cal.get( Calendar.DAY_OF_MONTH ) ) );
      symbols.put( Symbols.YEAR, String.valueOf( cal.get( Calendar.YEAR ) ) );
      symbols.put( Symbols.HOUR, String.valueOf( cal.get( Calendar.HOUR ) ) );
      symbols.put( Symbols.MINUTE, String.valueOf( cal.get( Calendar.MINUTE ) ) );
      symbols.put( Symbols.SECOND, String.valueOf( cal.get( Calendar.SECOND ) ) );
      symbols.put( Symbols.MILLISECOND, String.valueOf( cal.get( Calendar.MILLISECOND ) ) );
      symbols.put( Symbols.MONTH_MM, StringUtil.zeropad( cal.get( Calendar.MONTH ) + 1, 2 ) );
      symbols.put( Symbols.DAY_DD, StringUtil.zeropad( cal.get( Calendar.DAY_OF_MONTH ), 2 ) );
      symbols.put( Symbols.YEAR_YYYY, StringUtil.zeropad( cal.get( Calendar.YEAR ), 4 ) );
      symbols.put( Symbols.HOUR_24, StringUtil.zeropad( cal.get( Calendar.HOUR_OF_DAY ), 2 ) );
      symbols.put( Symbols.HOUR_HH, StringUtil.zeropad( cal.get( Calendar.HOUR ), 2 ) );
      symbols.put( Symbols.MINUTE_MM, StringUtil.zeropad( cal.get( Calendar.MINUTE ), 2 ) );
      symbols.put( Symbols.SECOND_SS, StringUtil.zeropad( cal.get( Calendar.SECOND ), 2 ) );
      symbols.put( Symbols.MILLISECOND_ZZZ, StringUtil.zeropad( cal.get( Calendar.MILLISECOND ), 3 ) );

      // go back to midnight
      cal.set( Calendar.HOUR_OF_DAY, 0 );
      cal.set( Calendar.MINUTE, 0 );
      cal.set( Calendar.SECOND, 0 );
      cal.set( Calendar.MILLISECOND, 0 );
      symbols.put( Symbols.SECONDS_PAST_MIDNIGHT, StringUtil.zeropad( ( rundate.getTime() - cal.getTimeInMillis() ) / 1000, 5 ) );

      // go to tomorrow midnight
      cal.add( Calendar.DATE, +1 );
      symbols.put( Symbols.SECONDS_TILL_MIDNIGHT, StringUtil.zeropad( ( cal.getTimeInMillis() - rundate.getTime() ) / 1000, 5 ) );

      // reset to todays date/time      
      cal.setTime( rundate );

      // go back one day and get the "previous day" Month Day and Year 
      cal.add( Calendar.DATE, -1 );
      symbols.put( Symbols.PREV_MONTH_PM, StringUtil.zeropad( cal.get( Calendar.MONTH ) + 1, 2 ) );
      symbols.put( Symbols.PREV_DAY_PD, StringUtil.zeropad( cal.get( Calendar.DAY_OF_MONTH ), 2 ) );
      symbols.put( Symbols.PREV_YEAR_PYYY, StringUtil.zeropad( cal.get( Calendar.YEAR ), 4 ) );

      // reset to todays date/time      
      cal.setTime( rundate );

      // go back a month
      cal.add( Calendar.MONTH, -1 );
      symbols.put( Symbols.PREV_MONTH_LM, StringUtil.zeropad( cal.get( Calendar.MONTH ) + 1, 2 ) );
      symbols.put( Symbols.PREV_YEAR_LMYY, StringUtil.zeropad( cal.get( Calendar.YEAR ), 4 ) );

    }

    return rundate;
  }




  protected void reportTransformContextError( TransformContext context ) {
    Log.error( context.getStatus() + " - " + context.getErrorMessage() );
  }




  @Override
  public void setContext( TransformContext context ) {
    transformContext = context;
  }




  /**
   * @see coyote.batch.Component#open(coyote.batch.TransformContext)
   */
  @Override
  public void open( TransformContext context ) {
    transformContext = context;
  }




  /**
   * @see coyote.batch.Component#getContext()
   */
  @Override
  public TransformContext getContext() {
    return transformContext;
  }




  private void closeTooling() {
    if ( reader != null ) {
      try {
        reader.close();
      } catch ( Exception e ) {
        Log.warn( LogMsg.createMsg( Batch.MSG, "Engine.problems_closing_reader", reader.getClass().getName(), e.getClass().getSimpleName(), e.getMessage() ) );
      }
    }

    for ( FrameWriter writer : writers ) {
      try {
        writer.close();
      } catch ( Exception e ) {
        Log.warn( LogMsg.createMsg( Batch.MSG, "Engine.problems_closing_writer", writer.getClass().getName(), e.getClass().getSimpleName(), e.getMessage() ) );
      }
    }

    if ( mapper != null ) {
      try {
        mapper.close();
      } catch ( Exception e ) {
        Log.warn( LogMsg.createMsg( Batch.MSG, "Engine.problems_closing_mapper", mapper.getClass().getName(), e.getClass().getSimpleName(), e.getMessage() ) );
      }
    }

    for ( FrameFilter filter : filters ) {
      try {
        filter.close();
      } catch ( Exception e ) {
        Log.warn( LogMsg.createMsg( Batch.MSG, "Engine.problems_closing_filter", filter.getClass().getName(), e.getClass().getSimpleName(), e.getMessage() ) );
      }
    }

    for ( FrameValidator validator : validators ) {
      try {
        validator.close();
      } catch ( Exception e ) {
        Log.warn( LogMsg.createMsg( Batch.MSG, "Engine.problems_closing_validator", validator.getClass().getName(), e.getClass().getSimpleName(), e.getMessage() ) );
      }
    }

    for ( FrameTransform transformer : transformers ) {
      try {
        transformer.close();
      } catch ( Exception e ) {
        Log.warn( LogMsg.createMsg( Batch.MSG, "Engine.problems_closing_transformer", transformer.getClass().getName(), e.getClass().getSimpleName(), e.getMessage() ) );
      }
    }

    for ( ContextListener listener : listeners ) {
      try {
        listener.close();
      } catch ( Exception e ) {
        Log.warn( LogMsg.createMsg( Batch.MSG, "Engine.problems_closing_listener", listener.getClass().getName(), e.getClass().getSimpleName(), e.getMessage() ) );
      }
    }

  }




  /**
   * @see java.io.Closeable#close()
   */
  @Override
  public void close() throws IOException {

    // close the connections in the transform contexts
    getContext().close();

    if ( logManager != null )
      logManager.close();

  }




  /**
   * Close any internal components like readers and writers which may interfere 
   * with post-processing tasks from completing properly such as moving or 
   * deleting files. 
   */
  private void closeInternalComponents() {
    if ( reader != null ) {
      try {
        reader.close();
      } catch ( Exception e ) {
        Log.warn( LogMsg.createMsg( Batch.MSG, "Engine.problems_closing_reader", reader.getClass().getName(), e.getClass().getSimpleName(), e.getMessage() ) );
      }
    }

    for ( FrameWriter writer : writers ) {
      try {
        writer.close();
      } catch ( Exception e ) {
        Log.warn( LogMsg.createMsg( Batch.MSG, "Engine.problems_closing_writer", writer.getClass().getName(), e.getClass().getSimpleName(), e.getMessage() ) );
      }
    }
  }




  /**
   * @see coyote.batch.TransformEngine#setReader(coyote.batch.FrameReader)
   */
  @Override
  public void setReader( FrameReader reader ) {
    this.reader = reader;
  }




  /**
   * @see coyote.batch.TransformEngine#addWriter(coyote.batch.FrameWriter)
   */
  @Override
  public void addWriter( FrameWriter writer ) {
    writers.add( writer );
  }




  /**
   * @see coyote.batch.TransformEngine#setMapper(coyote.batch.FrameMapper)
   */
  @Override
  public void setMapper( FrameMapper mapper ) {
    this.mapper = mapper;
  }




  /**
   * @see coyote.batch.TransformEngine#addListener(coyote.batch.ContextListener)
   */
  @Override
  public void addListener( ContextListener listener ) {
    listeners.add( listener );
  }




  /**
   * @see coyote.batch.TransformEngine#getMapper()
   */
  @Override
  public FrameMapper getMapper() {
    return mapper;
  }




  /**
   * @see coyote.batch.TransformEngine#getReader()
   */
  @Override
  public FrameReader getReader() {
    return reader;
  }




  /**
   * @see coyote.batch.TransformEngine#getName()
   */
  @Override
  public String getName() {
    return configuration.getAsString( ConfigTag.NAME );
  }




  /**
   * @see coyote.batch.TransformEngine#setName(java.lang.String)
   */
  @Override
  public void setName( String value ) {
    configuration.put( ConfigTag.NAME, value );
  }




  /**
   * Format the string using the default formatting for all actions.
   * 
   * @param date The date to format.
   * 
   * @return The formatted date string.
   */
  public static String formatDateTime( Date date ) {
    if ( date == null )
      return "null";
    else
      return Batch.DEFAULT_DATETIME_FORMAT.format( date );
  }




  /**
   * Format the date only returning the date portion of the date  (i.e. no time representation).
   * 
   * @param date the date/time to format
   * 
   * @return only the date portion formatted
   */
  public static String formatDate( Date date ) {
    if ( date == null )
      return "null";
    else
      return Batch.DEFAULT_DATE_FORMAT.format( date );
  }




  /**
   * Format the date returning only the time portion of the date (i.e. no month, day or year).
   * 
   * @param date the date/time to format
   * 
   * @return only the time portion formatted
   */
  public static String formatTime( Date date ) {
    if ( date == null )
      return "null";
    else
      return Batch.DEFAULT_TIME_FORMAT.format( date );
  }




  /**
   * @see coyote.batch.TransformEngine#getSymbolTable()
   */
  @Override
  public SymbolTable getSymbolTable() {
    return symbols;
  }




  /**
   * @see coyote.batch.TransformEngine#addPreProcessTask(coyote.batch.TransformTask)
   */
  @Override
  public int addPreProcessTask( TransformTask task ) {
    if ( task != null ) {
      preProcesses.add( task );
      return preProcesses.size() - 1;
    } else {
      return 0;
    }
  }




  /**
   * @see coyote.batch.TransformEngine#addPostProcessTask(coyote.batch.TransformTask)
   */
  @Override
  public int addPostProcessTask( TransformTask task ) {
    if ( task != null ) {
      postProcesses.add( task );
      return postProcesses.size() - 1;
    } else {
      return 0;
    }
  }




  /**
   * @see coyote.batch.TransformEngine#addDatabase(coyote.batch.Database)
   */
  @Override
  public void addDatabase( Database database ) {

    if ( getContext() == null ) {
      setContext( new TransformContext( listeners ) );
    }

    getContext().addDataStore( database );
  }




  /**
   * @see coyote.batch.TransformEngine#addValidator(coyote.batch.FrameValidator)
   */
  @Override
  public void addValidator( FrameValidator validator ) {
    if ( validator != null ) {
      validators.add( validator );
    }
  }




  /**
   * @see coyote.batch.TransformEngine#addTransformer(coyote.batch.FrameTransform)
   */
  @Override
  public void addTransformer( FrameTransform transformer ) {
    if ( transformer != null ) {
      transformers.add( transformer );
    }
  }




  /**
   * Called by the Loader when the runtime terminates to signal long-running 
   * processes to terminate and to force any clean-up.
   * 
   * <p>This is part of the Loader life cycle and not a part of the Batch life 
   * cycle. It will always be called when the JRE terminates even after normal 
   * termination of the transform engine. You shouldn't need to call this.</p>
   * 
   * <p>This is a good place to set a shutdown flag in the main loop so that 
   * the job can terminate cleanly when the user presses [ctrl-c] or the 
   * process is signaled to terminate via the operating system such as SIG HUP 
   * or the host is shutting down.</p> 
   * 
   * @see coyote.batch.TransformEngine#shutdown()
   */
  @Override
  public void shutdown() {

  }




  /**
   * @see coyote.batch.TransformEngine#setLogManager(coyote.batch.LogManager)
   */
  @Override
  public void setLogManager( LogManager logmgr ) {
    logManager = logmgr;
  }




  /**
   * @see coyote.batch.TransformEngine#addFilter(coyote.batch.FrameFilter)
   */
  @Override
  public int addFilter( FrameFilter filter ) {
    if ( filter != null ) {
      filters.add( filter );
      return filters.size() - 1;
    } else {
      return 0;
    }
  }

}
