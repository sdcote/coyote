package coyote.batch;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.List;

import coyote.commons.ExceptionUtil;
import coyote.commons.FileUtil;
import coyote.commons.StringUtil;
import coyote.commons.UriUtil;
import coyote.loader.AbstractLoader;
import coyote.loader.Loader;
import coyote.loader.cfg.Config;
import coyote.loader.cfg.ConfigurationException;
import coyote.loader.log.Log;
import coyote.loader.log.LogMsg;


/**
 * This class represents the batch job to execute.
 * 
 * <p>The working directory is determined based on the setting of the batch.home system property which causes all relative file operations to resolve to the batch.home/wrk/<job.name>/ directory
 * <p>If the batch.home property is not set, then the Job attempts to locate the file used to configure it and uses that directory as the working directory.
 */
public class Job extends AbstractLoader implements Loader {

  private static final String DEBUG_ARG = "-d";
  private static final String INFO_ARG = "-v";

  /**
   * If there is no specified directory in the HOMDIR system property, just use the current working directory
   */
  public static final String DEFAULT_HOME = new String( System.getProperty( "user.dir" ) );

  TransformEngine engine = null;




  /**
   * 
   */
  public Job() {

  }




  /**
   * @see coyote.loader.AbstractLoader#configure(coyote.loader.cfg.Config)
   */
  @Override
  public void configure( Config cfg ) throws ConfigurationException {
    super.configure( cfg );

    // Check the command line arguments for additional cfg info
    parseArgs();

    // calculate and normalize the appropriate value for "app.home"
    determineHomeDirectory();

    determineWorkDirectory();

    List<Config> jobs = cfg.getSections( "Job" );

    if ( jobs.size() > 0 ) {

      Config job = jobs.get( 0 );

      // have the Engine Factory create a transformation engine based on the
      // configuration 
      engine = TransformEngineFactory.getInstance( job );

      if ( StringUtil.isBlank( engine.getName() ) ) {
        Log.debug( LogMsg.createMsg( Batch.MSG, "Job.unnamed_engine_configured" ) );
      } else {
        Log.debug( LogMsg.createMsg( Batch.MSG, "Job.engine_configured", engine.getName() ) );
      }
    } else {
      Log.fatal( LogMsg.createMsg( Batch.MSG, "Job.no_job_section" ) );
    }
  }




  /**
   * This just looks for verbose and debug logging flags on the command line.
   */
  private void parseArgs() {
    for ( int x = 0; x < commandLineArguments.length; x++ ) {
      if ( DEBUG_ARG.equalsIgnoreCase( commandLineArguments[x] ) ) {
        Log.startLogging( Log.DEBUG );
      } else if ( INFO_ARG.equalsIgnoreCase( commandLineArguments[x] ) ) {
        Log.startLogging( Log.INFO );
      }
    }
  }




  /**
   * Determine the value of the "app.home" system property.
   * 
   * <p>If the app home property is already set, it is preserved, if not 
   * normalized. If there is no value, this attempts to determine the location 
   * of the configuration file used to configure this job and if found, uses 
   * that directory as the home directory of all transformation operations. The
   * reasoning is that all artifacts should be kept together. Also, it is 
   * probable that the batch job will be called from a central location while 
   * each batch job will live is its own project directory.</p>
   * 
   * <p>The most common use case is for the batch job to be called from a 
   * scheduler (e.g. cron) with an absolute path to a configuration file. 
   * Another very probable use case is the batch job being called from a 
   * project directory with one configuration file per directory.</p>
   * 
   * <p>It is possible that multiple files with different configurations will 
   * exist in one directory
   */
  private void determineHomeDirectory() {
    // If our home directory is not specified as a system property...
    if ( System.getProperty( Job.APP_HOME ) == null ) {

      // use the first argument to the bootstrap loader to determine the 
      // location of our configuration file
      File cfgFile = new File( super.getCommandLineArguments()[0] );

      // If that file exists, then use that files parent directory as our work
      // directory
      if ( cfgFile.exists() ) {
        System.setProperty( Job.APP_HOME, cfgFile.getParentFile().getAbsolutePath() );
      } else {
        // we could not determine the path to the configuration file, use the 
        // current working directory
        System.setProperty( Job.APP_HOME, DEFAULT_HOME );
      }
    } else {

      // Normalize the "." that sometimes is set in the app.home property
      if ( System.getProperty( Job.APP_HOME ).trim().equals( "." ) ) {
        System.setProperty( Job.APP_HOME, DEFAULT_HOME );
      } else if ( System.getProperty( Job.APP_HOME ).trim().length() == 0 ) {
        // catch empty home property and just use the home directory
        System.setProperty( Job.APP_HOME, DEFAULT_HOME );
      }
    }

    // Remove all the relations and extra slashes from the home path
    System.setProperty( Job.APP_HOME, FileUtil.normalizePath( System.getProperty( Job.APP_HOME ) ) );
    Log.debug( LogMsg.createMsg( Batch.MSG, "Job.home_dir_set", System.getProperty( Job.APP_HOME ) ) );
  }




  private void determineWorkDirectory() {

    File result = null;

    // First check for the app.work system property
    String path = System.getProperties().getProperty( APP_WORK );

    if ( StringUtil.isNotBlank( path ) ) {
      String workDir = FileUtil.normalizePath( path );
      File workingDir = new File( workDir );
      if ( workingDir.exists() ) {
        if ( workingDir.isDirectory() ) {
          if ( workingDir.canWrite() ) {
            result = workingDir;
          } else {
            Log.warn( "The app.work property specified an un-writable (permissions) directory: " + workDir );
          }
        } else {
          Log.warn( "The app.work property does not specify a directory: " + workDir );
        }
      } else {
        // Try making it
        try {
          FileUtil.makeDirectory( workingDir );
          result = workingDir;
        } catch ( IOException e ) {
          Log.error( "Could not create working directory specified in app.work property: " + workDir + " - " + e.getMessage() );
        }
      }

    } else {
      // No app.work defined, so try to locate the configuration file used to 
      // create this job
      URI cfgUri = UriUtil.parse( System.getProperty( coyote.loader.ConfigTag.CONFIG_URI ) );
      if ( cfgUri != null && UriUtil.isFile( cfgUri ) ) {
        File cfgFile = UriUtil.getFile( cfgUri );
        if ( cfgFile != null ) {
          File workingDir = new File( cfgFile.getParent() );
          if ( workingDir.exists() && workingDir.isDirectory() && workingDir.canWrite() ) {
            result = workingDir;
          }
        }
      }
    }

    // If we have a result,
    if ( result != null ) {
      // set it as our working directory
      System.setProperty( ConfigTag.WORKDIR, result.getAbsolutePath() );
    } else {
      // else just use the current working directory
      System.setProperty( ConfigTag.WORKDIR, DEFAULT_HOME );
    }

  }




  /**
   * @see coyote.loader.AbstractLoader#start()
   */
  @Override
  public void start() {

    if ( engine != null ) {
      Log.debug( LogMsg.createMsg( Batch.MSG, "Job.running" ) );

      // run the transformation
      // Note that depending on the configuration, this could be placed in the 
      // scheduler and run intermittently as a scheduled job or multiple 
      // transform engines could be run in the thread pool of the super-class.
      try {
        engine.run();
      } catch ( final Exception e ) {
        Log.fatal( LogMsg.createMsg( Batch.MSG, "Job.exception_running_engine", e.getClass().getSimpleName(), e.getMessage() ) );
        Log.fatal( ExceptionUtil.toString( e ) );
      }
      finally {

        try {
          engine.close();
        } catch ( final IOException ignore ) {}

        Log.debug( LogMsg.createMsg( Batch.MSG, "Job.completed", engine.getName() ) );
      } // try-catch-finally

    } else {
      Log.fatal( LogMsg.createMsg( Batch.MSG, "Job.no_engine" ) );
    }
  }




  /**
   * Shut everything down when the JRE terminates.
   * 
   * <p>There is a shutdown hook registered with the JRE when this Job is 
   * loaded. The shutdown hook will call this method when the JRE is 
   * terminating so that the Job can terminate any long-running processes.</p>
   * 
   * <p>Note: this is different from {@code close()} but {@code shutdown()} 
   * will normally result in {@code close()} being invoked at some point.</p>
   * 
   * @see coyote.loader.thread.ThreadJob#shutdown()
   */
  @Override
  public void shutdown() {
    //System.out.println( "Runtime termination, batch job shutting down..." );
    engine.shutdown();
  }

}
