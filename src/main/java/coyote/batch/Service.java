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


/**
 * This runs the Coyote Batch as a background Service
 */
public class Service extends AbstractBatchLoader implements Loader {

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

    List<Config> jobs = cfg.getSections( "Job" );

    for ( Config config : jobs ) {

      // Load each of the jobs in the configuration file and set them in the scheduler
      
      // Each of these are components we will monitor in the watchdog thread

    }

  }




  /**
   * Start the HTTP server to provide user management of the service.
   */
  private void startManager() {


    try {
      server = new HttpManager();
      
      // TODO: set some things from the configuration
      
      server.start( HTTPD.SOCKET_READ_TIMEOUT, false );
    } catch ( IOException ioe ) {
      System.err.println( "Couldn't start server:\n" + ioe );
      System.exit( -1 );
    }
  }




  /**
   * @see coyote.loader.AbstractLoader#start()
   */
  @Override
  public void start() {
    // Start everything running
    
    // enter the watchdog routine
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
    
    // exit the watchdog routine...this will free up the MAIN thread
    
    // shutdown the scheduler
    
    if( server != null ){
      server.stop();
    }
    
    
    //System.out.println( "Runtime termination, batch job shutting down..." );
    //engine.shutdown();
  }

}
