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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import coyote.batch.mapper.DefaultFrameMapper;
import coyote.batch.mapper.MappingException;
import coyote.batch.validate.ValidationException;
import coyote.commons.FileUtil;
import coyote.commons.GUID;
import coyote.commons.StringUtil;
import coyote.commons.template.SymbolTable;
import coyote.dataframe.DataFrame;


/**
 * 
 */
public abstract class AbstractTransformEngine extends AbstractConfigurableComponent implements TransformEngine, ConfigurableComponent {

  /** The logger for the base class */
  final Logger log = LoggerFactory.getLogger( getClass() );

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

  /** The component which will record the frame somewhere. */
  protected FrameWriter writer = null;

  /** The list of tasks to perform after the transformation is complete. (e.g. posting a file to a FTP site) */
  protected List<TransformTask> postProcesses = new ArrayList<TransformTask>();

  /** The context for the entire transformation instance (i.e. job) */
  protected TransformContext transformContext = null;

  /** A list of components interested in context events */
  protected List<ContextListener> listeners = new ArrayList<ContextListener>();

  /** A symbol table to support basic template functions */
  protected static final SymbolTable symbols = new SymbolTable();

  // Consistent date and time representation
  private static final DateFormat _DATETIME_FORMAT = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss" );
  private static final DateFormat _DATE_FORMAT = new SimpleDateFormat( "yyyy-MM-dd" );
  private static final DateFormat _TIME_FORMAT = new SimpleDateFormat( "HH:mm:ss" );

  /** The directory this engine uses for file operations */
  private File jobDirectory = null;
  
  /** The shared directory where multiple engines may read and write data. */
  private File workDirectory = null;

  /** The current row number */
  protected volatile long currentRow = 0;




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

    // figure out our job directory
    determineJobDirectory();

    setRunDate();

    // Make sure the engine has a name
    if ( StringUtil.isBlank( getName() ) ) {
      setName( GUID.randomGUID().toString() );
    }

    if ( reader == null && writer != null )
      throw new IllegalStateException( "No reader configured, nothing to write" );

    // if no mapper, just use the default mapper with default settings
    if ( mapper == null ) {
      log.debug( "No mapper defined...using default settings" );
      mapper = new DefaultFrameMapper();
    }

    // Make sure we have a context with our listeners attached
    if ( getContext() != null ) {
      // set our list of listeners in the context 
      getContext().setListeners( listeners );
    } else {
      // Create a transformation context for components to share data
      setContext( new TransformContext( listeners ) );
      getContext().setEngine( this );
    }

    // Open / initialize the context
    getContext().open();

    // Set the symbol table for the context
    getContext().setSymbols( symbols );

    // fire the transformation start event
    getContext().start();

    // Execute all the pre-processing tasks
    for ( TransformTask task : preProcesses ) {
      try {
        task.open( transformContext );
        task.execute( transformContext );
      } catch ( TaskException e ) {
        transformContext.setError( e.getMessage() );
        transformContext.setStatus( "Pre-Processing Error" );
        break;
      }
    }
    // Close all the tasks after pre-processing is done regardless of outcome
    for ( TransformTask task : preProcesses ) {
      try {
        task.close();
      } catch ( IOException e ) {
        log.warn( "Problems closing {} - {}", task.getClass().getSimpleName(), e.getMessage() );
      }
    }

    // If pre-processing completed without error, start opening the rest of the 
    // components
    if ( transformContext.isNotInError() ) {

      // Open all the listeners first
      for ( ContextListener listener : listeners ) {
        listener.open( transformContext );
        if ( transformContext.isInError() ) {
          reportTransformContextError( transformContext );
          return;
        }
      }

      // If the reader is not null, open the core components using this context 
      // to share data. If the reader is null, there is no need to open the 
      // mapper and the writer
      if ( reader != null ) {
        reader.open( transformContext );
        if ( transformContext.isInError() ) {
          reportTransformContextError( transformContext );
          return;
        }

        mapper.open( transformContext );
        if ( transformContext.isInError() ) {
          reportTransformContextError( transformContext );
          return;
        }

        if ( writer != null ) {
          writer.open( transformContext );
          if ( transformContext.isInError() ) {
            reportTransformContextError( transformContext );
            return;
          }
        }
      }

      // Open all the filters
      for ( FrameFilter filter : filters ) {
        filter.open( transformContext );
        if ( transformContext.isInError() ) {
          reportTransformContextError( transformContext );
          return;
        }
      }

      // Open all the validators
      for ( FrameValidator validator : validators ) {
        validator.open( transformContext );
        if ( transformContext.isInError() ) {
          reportTransformContextError( transformContext );
          return;
        }
      }

      // Open all the transformers
      for ( FrameTransform transformer : transformers ) {
        transformer.open( transformContext );
        if ( transformContext.isInError() ) {
          reportTransformContextError( transformContext );
          return;
        }
      }

      // loop through all data read in by the reader until EOF or an error in 
      // the transform context occurs.
      while ( transformContext.isNotInError() && reader != null && !reader.eof() ) {

        // Create a new Transaction context with the list of listeners to react 
        // to events in the transaction.
        TransactionContext context = new TransactionContext( transformContext );
        // Start the clock and fire event listeners for the beginning of the
        // transaction
        context.start();

        // Read a frame into the given context (source frame)
        DataFrame retval = reader.read( context );

        // Sometimes readers read empty lines and the like, skip null dataframes!!!
        if ( retval != null ) {

          // Set the returned dataframe into the transaction context
          context.setSourceFrame( retval );
          // increment the row number in the contexts
          context.setRow( ++currentRow );
          transformContext.setRow( currentRow );
          symbols.put( "currentRow", currentRow );

          // fire the read event in all the listeners
          context.fireRead( context );

          //log.debug( "row {} - {}", context.getRow(), context.getSourceFrame().toString() );

          // ...pass it through the filters...
          for ( FrameFilter filter : filters ) {
            filter.process( context );
            if ( context.getWorkingFrame() == null )
              break;
          }

          // If the working frame did not get filtered out...
          if ( context.getWorkingFrame() != null ) {

            boolean passed = true;
            // pass it through the validation rules - errors are logged
            for ( FrameValidator validator : validators ) {
              try {
                if ( !validator.process( context ) ) {
                  passed = false;
                }
              } catch ( ValidationException e ) {
                context.setError( e.getMessage() );
              }
            }

            if ( !passed ) {
              transformContext.fireValidationFailed( "There were validation errors" );
            }

            if ( context.isNotInError() ) {

              // Pass the working frame through the transformers
              for ( FrameTransform transformer : transformers ) {
                try {
                  // Have the transformer process the frame
                  DataFrame resultFrame = transformer.process( context.getWorkingFrame() );
                  
                  // if the transformer generated any results...
                  if ( resultFrame != null ) {
                    // place it in the context
                    context.setWorkingFrame(resultFrame);
                  }
                } catch ( TransformException e ) {
                  context.setError( e.getMessage() );
                }
              }

              // Pass it through the mapper - only the required fields should 
              // exist in the target frame after the mapper is done.
              if ( context.isNotInError() ) {

                // We need to create a target frame into which the mapper will 
                // place fields...
                if ( context.getTargetFrame() == null ) {
                  context.setTargetFrame( new DataFrame() );
                }

                // Map / Move fields from the working to the target frame
                try {
                  mapper.process( context );
                } catch ( MappingException e ) {
                  context.setError( e.getMessage() );
                  context.setStatus( "Mapping Error" );
                }
              }

              // It is possible that a transform has been configured to simply
              // read and validate data so the writer may be null
              if ( context.isNotInError() && writer != null ) {
                try {
                  // Write the target (new) frame
                  writer.write( context.getTargetFrame() );
                  context.fireWrite( context );
                } catch ( Exception e ) {
                  e.printStackTrace();
                }
              }

            } // passed validators

          } // passed filters

          // Now end the transaction which should fire any listeners in the 
          // context to record the transaction if so configured
          context.end();

        } // if something was read in

      } // Reader !eof and context is without error

    } // transformContext ! err after pre-processing

    if ( transformContext.isInError() ) {
      reportTransformContextError( transformContext );
    } else {
      // Execute all the post-processing tasks
      for ( TransformTask task : postProcesses ) {
        try {
          task.open( transformContext );
          task.execute( transformContext );
        } catch ( TaskException e ) {
          transformContext.setError( e.getMessage() );
          transformContext.setStatus( "Post-Processing Error" );
        }
      }
      if ( transformContext.isInError() ) {
        reportTransformContextError( transformContext );
      }
    }

    getContext().end();

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
    if ( StringUtil.isBlank( System.getProperty( ConfigTag.HOMEDIR ) ) ) {
      System.setProperty( ConfigTag.HOMEDIR, System.getProperty( "user.dir" ) );
    }

    // if 
    if ( StringUtil.isBlank( System.getProperty( ConfigTag.WORKDIR ) ) ) {
      System.setProperty( ConfigTag.WORKDIR, System.getProperty( ConfigTag.HOMEDIR ) );
    }

    // Use our name to setup a job directory
    String name = getName();
    if ( StringUtil.isNotBlank( name ) ) {
      // replace the illegal filename characters by only allowing simple names
      String dirname = name.trim().replaceAll( "[^a-zA-Z0-9.-]", "_" );
      jobDir = new File( System.getProperty( ConfigTag.WORKDIR ) + FileUtil.FILE_SEPARATOR + dirname );

      try {
        jobDir.mkdirs();
        setJobDirectory( jobDir );
        setWorkDirectory( jobDir.getParentFile() );
      } catch ( final Exception e ) {
        log.error( e.getMessage() );
      }
    } else {
      // unnamed jobs just use the current directory
      jobDir = new File( System.getProperty( "user.dir" ) );
      setJobDirectory( jobDir );
      setWorkDirectory( jobDir );
    }
  }




  @SuppressWarnings("unchecked")
  protected void setRunDate() {
    // Place date and time values in the symbol table
    Calendar cal = Calendar.getInstance();

    Date date = new Date();
    if ( date != null ) {
      cal.setTime( date );
      symbols.put( "Date", formatDate( date ) );
      symbols.put( "Time", formatTime( date ) );
      symbols.put( "DateTime", formatDateTime( date ) );
      symbols.put( "Month", String.valueOf( cal.get( Calendar.MONTH ) + 1 ) );
      symbols.put( "Day", String.valueOf( cal.get( Calendar.DAY_OF_MONTH ) ) );
      symbols.put( "Year", String.valueOf( cal.get( Calendar.YEAR ) ) );
      symbols.put( "Hour", String.valueOf( cal.get( Calendar.HOUR ) ) );
      symbols.put( "Minute", String.valueOf( cal.get( Calendar.MINUTE ) ) );
      symbols.put( "Second", String.valueOf( cal.get( Calendar.SECOND ) ) );
      symbols.put( "Millisecond", String.valueOf( cal.get( Calendar.MILLISECOND ) ) );
      symbols.put( "MM", StringUtil.zeropad( cal.get( Calendar.MONTH ) + 1, 2 ) );
      symbols.put( "DD", StringUtil.zeropad( cal.get( Calendar.DAY_OF_MONTH ), 2 ) );
      symbols.put( "YYYY", StringUtil.zeropad( cal.get( Calendar.YEAR ), 4 ) );
      symbols.put( "hh", StringUtil.zeropad( cal.get( Calendar.HOUR ), 2 ) );
      symbols.put( "mm", StringUtil.zeropad( cal.get( Calendar.MINUTE ), 2 ) );
      symbols.put( "ss", StringUtil.zeropad( cal.get( Calendar.SECOND ), 2 ) );
      symbols.put( "zzz", StringUtil.zeropad( cal.get( Calendar.MILLISECOND ), 3 ) );

      // go back one day and get the "previous day" Month Day and Year 
      cal.add( Calendar.DATE, -1 );
      symbols.put( "PM", StringUtil.zeropad( cal.get( Calendar.MONTH ) + 1, 2 ) );
      symbols.put( "PD", StringUtil.zeropad( cal.get( Calendar.DAY_OF_MONTH ), 2 ) );
      symbols.put( "PYYY", StringUtil.zeropad( cal.get( Calendar.YEAR ), 4 ) );

    }
  }




  protected void reportTransformContextError( TransformContext context ) {
    log.error( context.getStatus() + " - " + context.getMessage() );
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




  /**
   * @see java.io.Closeable#close()
   */
  @Override
  public void close() throws IOException {

    if ( reader != null ) {
      try {
        reader.close();
      } catch ( Exception e ) {
        log.warn( "Problems closing reader: {} - {}", e.getClass().getSimpleName(), e.getMessage() );
      }
    }

    if ( writer != null ) {
      try {
        writer.close();
      } catch ( Exception e ) {
        log.warn( "Problems closing writer: {} - {}", e.getClass().getSimpleName(), e.getMessage() );
      }
    }

    if ( mapper != null ) {
      try {
        mapper.close();
      } catch ( Exception e ) {
        log.warn( "Problems closing mapper: {} - {}", e.getClass().getSimpleName(), e.getMessage() );
      }
    }

    for ( FrameFilter filter : filters ) {
      try {
        filter.close();
      } catch ( Exception e ) {
        log.warn( "Problems closing filter {} : {} - {}", filter.getClass().getName(), e.getClass().getSimpleName(), e.getMessage() );
      }
    }

    for ( FrameValidator validator : validators ) {
      try {
        validator.close();
      } catch ( Exception e ) {
        log.warn( "Problems closing validator {} : {} - {}", validator.getClass().getName(), e.getClass().getSimpleName(), e.getMessage() );
      }
    }

    for ( FrameTransform transformer : transformers ) {
      try {
        transformer.close();
      } catch ( Exception e ) {
        log.warn( "Problems closing transformer {} : {} - {}", transformer.getClass().getName(), e.getClass().getSimpleName(), e.getMessage() );
      }
    }

    for ( ContextListener listener : listeners ) {
      try {
        listener.close();
      } catch ( Exception e ) {
        log.warn( "Problems closing listener {} : {} - {}", listener.getClass().getName(), e.getClass().getSimpleName(), e.getMessage() );
      }
    }

    // close the connections in the transform contexts
    getContext().close();

  }




  /**
   * @see coyote.batch.TransformEngine#setReader(coyote.batch.FrameReader)
   */
  @Override
  public void setReader( FrameReader reader ) {
    this.reader = reader;
  }




  /**
   * @see coyote.batch.TransformEngine#setWriter(coyote.batch.FrameWriter)
   */
  @Override
  public void setWriter( FrameWriter writer ) {
    this.writer = writer;
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
   * @see coyote.batch.TransformEngine#getWriter()
   */
  @Override
  public FrameWriter getWriter() {
    return writer;
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
      return _DATETIME_FORMAT.format( date );
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
      return _DATE_FORMAT.format( date );
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
      return _TIME_FORMAT.format( date );
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

}
