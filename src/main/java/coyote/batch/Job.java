package coyote.batch;

import java.io.IOException;
import java.util.List;

import coyote.commons.GUID;
import coyote.commons.StringUtil;
import coyote.loader.AbstractLoader;
import coyote.loader.Loader;
import coyote.loader.cfg.Config;
import coyote.loader.cfg.ConfigurationException;


public class Job extends AbstractLoader implements Loader {

  TransformEngine engine = null;




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
      System.out.println( "No engine to run" );
    }
  }




  /**
   * @see coyote.loader.thread.ThreadJob#shutdown()
   */
  @Override
  public void shutdown() {
    System.out.println( "Shutting down..." );
  }

}
