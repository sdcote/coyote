package coyote.batch;

import java.io.IOException;
import java.util.List;

import coyote.commons.ExceptionUtil;
import coyote.commons.StringUtil;
import coyote.loader.Loader;
import coyote.loader.cfg.Config;
import coyote.loader.cfg.ConfigurationException;
import coyote.loader.log.Log;
import coyote.loader.log.LogMsg;


/**
 * This class represents the batch job to execute.
 * 
 * <p>The working directory is determined based on the setting of the 
 * batch.home system property which causes all relative file operations to 
 * resolve to the batch.home/wrk/&lt;job.name&gt;/ directory. If the batch.home 
 * property is not set, then the Job attempts to locate the file used to 
 * configure it and uses that directory as the working directory.</p>
 */
public class Job extends AbstractBatchLoader implements Loader {

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

    // calculate and normalize the appropriate value for "app.home"
    determineHomeDirectory();

    determineWorkDirectory();

    List<Config> jobs = cfg.getSections( "Job" );

    if ( jobs.size() > 0 ) {

      Config job = jobs.get( 0 );

      // have the Engine Factory create a transformation engine based on the
      // configuration 
      engine = TransformEngineFactory.getInstance( job );

      // store the command line arguments in the symbol table of the engine
      for ( int x = 0; x < commandLineArguments.length; x++ ) {
        engine.getSymbolTable().put( Symbols.COMMAND_LINE_ARG_PREFIX + x, commandLineArguments[x] );
      }

      if ( StringUtil.isBlank( engine.getName() ) ) {
        Log.trace( LogMsg.createMsg( Batch.MSG, "Job.unnamed_engine_configured" ) );
      } else {
        Log.trace( LogMsg.createMsg( Batch.MSG, "Job.engine_configured", engine.getName() ) );
      }
    } else {
      Log.fatal( LogMsg.createMsg( Batch.MSG, "Job.no_job_section" ) );
    }
  }




  /**
   * @see coyote.loader.AbstractLoader#start()
   */
  @Override
  public void start() {

    if ( engine != null ) {
      Log.trace( LogMsg.createMsg( Batch.MSG, "Job.running", engine.getName() ) );

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

        Log.trace( LogMsg.createMsg( Batch.MSG, "Job.completed", engine.getName() ) );
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
    engine.shutdown();
  }

}
