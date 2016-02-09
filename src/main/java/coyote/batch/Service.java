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

import java.io.IOException;
import java.util.List;

import coyote.batch.http.HttpManager;
import coyote.commons.network.http.HTTPD;
import coyote.loader.Loader;
import coyote.loader.cfg.Config;
import coyote.loader.cfg.ConfigurationException;
import coyote.loader.log.Log;
import coyote.loader.log.LogMsg;


/**
 * This runs the Coyote Batch as a background Service
 */
public class Service extends AbstractBatchLoader implements Loader {

  /** Tag used in various class identifying locations. */
  public static final String CLASS = Service.class.getSimpleName();

  private HTTPD server = null;




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

    // Figure out the working directory
    determineWorkDirectory();

    // Start the management API
    startManager();

    // store the command line arguments in the configuration
    //      for ( int x = 0; x < commandLineArguments.length; x++ ) {
    //        job.getEngine().getSymbolTable().put( Symbols.COMMAND_LINE_ARG_PREFIX + x, commandLineArguments[x] );
    //      }

  }




  /**
   * Start the HTTP server to provide user management of the service.
   */
  private void startManager() {

    try {
      server = new HttpManager();

      // / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / /
      // this is where we would configure the HTTP server
      // / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / /

      server.start( HTTPD.SOCKET_READ_TIMEOUT, false );
    } catch ( IOException ioe ) {
      System.err.println( "Couldn't start server:\n" + ioe );
      System.exit( -1 );
    }
  }




  /**
   * This loads all the "Jobs" in the configuration.
   * 
   * <p>One of the functions of this method is to ensure the configuration has 
   * all the data required for the scheduled job to load properly. This 
   * includes a class name and any other configuration attributes needed to 
   * ensure the job operates in concert with the other jobs.</p>
   * 
   * @see coyote.loader.AbstractLoader#initComponents()
   */
  @Override
  protected void initComponents() {

    // Now load "Jobs"

    List<Config> sections = configuration.getSections( "Job" );

    // Look for the COMPONENTS section
    for ( Config section : sections ) {

      // make sure the configuration has a class
      section.setClassName( ScheduledBatchJob.class.getName() );

      // make sure the configuration has the command line arguments
      // TODO: cfg.put( "CommandLine", value );

      loadComponent( section );
    }

  }




  /**
   * Start the components running.
   * 
   * @see coyote.loader.AbstractLoader#start()
   */
  @Override
  public void start() {
    // only start once, this is not foolproof as the active flag is set only when 
    // the watchdog loop is entered
    if ( isActive() ) {
      return;
    }

    // Save the name of the thread that is running this class
    final String oldName = Thread.currentThread().getName();

    // Rename this thread to the name of this class
    Thread.currentThread().setName( CLASS );

    // very important to get park(millis) to operate
    current_thread = Thread.currentThread();

    // Parse through the configuration and initialize all the components
    initComponents();

    Log.info( LogMsg.createMsg( "Loader.components_initialized" ) );

    // By this time all loggers (including the catch-all logger) should be open
    final StringBuffer b = new StringBuffer( CLASS );
    b.append( " initialized - Runtime: " );
    b.append( System.getProperty( "java.version" ) );
    b.append( " (" );
    b.append( System.getProperty( "java.vendor" ) );
    b.append( ")" );
    b.append( " - Platform: " );
    b.append( System.getProperty( "os.arch" ) );
    b.append( " OS: " );
    b.append( System.getProperty( "os.name" ) );
    b.append( " (" );
    b.append( System.getProperty( "os.version" ) );
    b.append( ")" );
    Log.info( b );

    // enter a loop performing watchdog and maintenance functions
    watchdog();

    // The watchdog loop has exited, so we are done processing
    terminateComponents();

    Log.info( LogMsg.createMsg( "Loader.terminated" ) );

    // Rename the thread back to what it was called before we were being run
    Thread.currentThread().setName( oldName );

  }




  /**
   * Shut everything down when the JRE terminates.
   * 
   * <p>There is a shutdown hook registered with the JRE when this Service is 
   * loaded. The shutdown hook will call this method when the JRE is 
   * terminating so that the Service can terminate any long-running processes.</p>
   * 
   * <p>Note: this is different from {@code close()} but {@code shutdown()} 
   * will normally result in {@code close()} being invoked at some point.</p>
   * 
   * @see coyote.loader.thread.ThreadJob#shutdown()
   */
  @Override
  public void shutdown() {
    // call the threadjob shutdown to exit the watchdog routine
    // this will free up the MAIN thread
    super.shutdown();

    // shutdown the scheduler

    if ( server != null ) {
      server.stop();
    }

    //System.out.println( "Runtime termination, batch job shutting down..." );
    //engine.shutdown();
  }

}
