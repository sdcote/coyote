package coyote.batch;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import coyote.commons.FileUtil;
import coyote.commons.StringUtil;
import coyote.loader.AbstractLoader;
import coyote.loader.Loader;
import coyote.loader.cfg.Config;
import coyote.loader.cfg.ConfigurationException;


/**
 * This class represents the batch job to execute.
 * 
 * <p>The working directory is determined based on the setting of the batch.home system property which causes all relative file operations to resolve to the batch.home/wrk/<job.name>/ directory
 * <p>If the batch.home property is not set, then the Job attempts to locate the file used to configure it and uses that directory as the working directory.
 */
public class Job extends AbstractLoader implements Loader {

  /** The logger for this class */
  final Logger log = LoggerFactory.getLogger( getClass() );

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

    // calculate and normalize the appropriate value for "batch.home"
    determineHomeDirectory();

    List<Config> jobs = cfg.getSections( "Job" );

    if ( jobs.size() > 0 ) {

      Config job = jobs.get( 0 );

      // have the Engine Factory create a transformation engine based on the
      // configuration 
      engine = TransformEngineFactory.getInstance( job );

      if ( StringUtil.isBlank( engine.getName() ) ) {
        System.out.println( "Configured unnamed engine ..." );
      } else {
        System.out.println( "Configured '" + engine.getName() + "' ..." );
      }
    } else {
      System.out.println( "No job section found to run" );
    }
  }




  /**
   * Determine the value of the "batch.home" system property.
   * 
   * <p>If the batch home property is already set, it is preserved, if not 
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
    if ( System.getProperty( ConfigTag.HOMEDIR ) == null ) {

      // use the first argument to the bootstrap loader to determine the 
      // location of our configuration file
      File cfgFile = new File( super.getCommandLineArguments()[0] );

      // If that file exists, then use that files parent directory as our work
      // directory
      if ( cfgFile.exists() ) {
        System.setProperty( ConfigTag.HOMEDIR, cfgFile.getParentFile().getAbsolutePath() );
      } else {
        // we could not determine the path to the configuration file, use the 
        // current working directory
        System.setProperty( ConfigTag.HOMEDIR, DEFAULT_HOME );
      }
    } else {
      // Normalize the "." that sometimes is set in the HOMEDIR property
      if ( System.getProperty( ConfigTag.HOMEDIR ).trim().equals( "." ) ) {
        System.setProperty( ConfigTag.HOMEDIR, DEFAULT_HOME );
      } else if ( System.getProperty( ConfigTag.HOMEDIR ).trim().length() == 0 ) {
        // catch empty home property and just use the home directory
        System.setProperty( ConfigTag.HOMEDIR, DEFAULT_HOME );
      }
    }

    // Remove all the relations and extra slashes from the home path
    System.setProperty( ConfigTag.HOMEDIR, FileUtil.normalizePath( System.getProperty( ConfigTag.HOMEDIR ) ) );
    log.debug( "Home directory set to {}", System.getProperty( ConfigTag.HOMEDIR ) );

  }




  /**
   * @see coyote.loader.AbstractLoader#start()
   */
  @Override
  public void start() {

    if ( engine != null ) {
      System.out.println( "Running..." );

      // run the transformation
      // Note that depending on the configuration, this could be placed in the 
      // scheduler and run intermittently as a scheduled job or multiple 
      // transform engines could be run in the thread pool of the super-class.
      try {
        engine.run();
      } catch ( final Exception e ) {
        System.out.println( "Encountered a '" + e.getClass().getSimpleName() + "' exception running the engine: " + e.getMessage() );
        e.printStackTrace();
      }
      finally {

        try {
          engine.close();
        } catch ( final IOException ignore ) {}

        System.out.println( "Job '" + engine.getName() + "' completed." );
      } // try-catch-finally

      System.out.println( "...Done." );
    } else {
      System.err.println( "No engine to run" );
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
