package coyote.batch;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import coyote.commons.FileUtil;
import coyote.commons.GUID;
import coyote.commons.StringUtil;
import coyote.loader.AbstractLoader;
import coyote.loader.Loader;
import coyote.loader.cfg.Config;
import coyote.loader.cfg.ConfigurationException;


/**
 * This class represents the batch job to execute
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
   * The {@code batch.home} system property determines where the home directory 
   * is; make sure it exists.
   */
  public Job() {

    //Make sure we have a home directory we can use
    if ( System.getProperty( ConfigTag.HOMEDIR ) == null ) {
      System.setProperty( ConfigTag.HOMEDIR, DEFAULT_HOME );
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

  }




  /**
   * @see coyote.loader.AbstractLoader#configure(coyote.loader.cfg.Config)
   */
  @Override
  public void configure( Config cfg ) throws ConfigurationException {
    super.configure( cfg );

    List<Config> jobs = cfg.getSections( "Job" );

    if ( jobs.size() > 0 ) {

      Config job = jobs.get( 0 );

      // have the Engine Factory create a transformation engine based on the
      // configuration 
      engine = TransformEngineFactory.getInstance( job );

      // Make sure the context contains a name so it can find artifacts
      // related to this transformation
      if ( StringUtil.isBlank( engine.getName() ) ) {
        System.out.println( "Un-named configuration..." );
        engine.setName( GUID.randomGUID().toString() );
      }

      File workDirectory;

      // This is where we create the work directory for the transformation engines
      try {
        workDirectory = new File( System.getProperty( ConfigTag.HOMEDIR ) );
        workDirectory.mkdirs();

        // if we could not create the work directory or we have assess issues...
        if ( !workDirectory.exists() || !workDirectory.isDirectory() || !workDirectory.canWrite() ) {
          log.warn( "Unable to write to " + workDirectory.getAbsolutePath() + " - using home directory instead" );
          workDirectory = FileUtil.initHomeWorkDirectory( ".batch" );
          log.warn( "Cannot access '{}' directory, creating a working directory in the users home: {}", System.getProperty( ConfigTag.HOMEDIR ), workDirectory.getAbsolutePath() );
        }
      } catch ( final Exception e ) {
        log.error( e.getMessage() );
      }

      System.out.println( "Configured '" + engine.getName() + "' ..." );
    } else {
      System.out.println( "No job section found to run" );
    }
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
