/*
 * Copyright (c) 2015 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 *
 * Contributors:
 *   Stephan D. Cote 
 *      - Initial concept and implementation
 */
package coyote.loader;

import coyote.commons.Version;
import coyote.i13n.StatBoard;
import coyote.loader.cfg.Config;
import coyote.loader.cfg.ConfigurationException;
import coyote.loader.thread.Scheduler;
import coyote.loader.thread.ThreadPool;


/**
 * 
 */
public interface Loader extends WatchDog {

  public static final Version API_VERSION = new Version( 1, 1, 5, Version.DEVELOPMENT );
  public static final String API_NAME = "CoyoteLoader";
  public static final String LOADER = "Loader";
  public static final String INITIALIZING = "Initializing";
  public static final String WAIT_FOR_ACTIVE = "WaitingForActive";
  public static final String RUNNING = "Watchdog";
  public static final String SHUTDOWN = "Shutdown";
  public static final String TERMINATED = "Terminated";
  public static final String ENCRYPT_PREFIX = "ENC:";

  public static final String APP_HOME = "app.home";
  public static final String APP_WORK = "app.work";
  public static final String ENCRYPT = "encrypt";




  /**
   * Return an identifier so this instance can be discerned from others.
   * 
   * <p>Two instances of a component may be running with one as the primary 
   * and the other as a back-up. Names will be identical, but identifier 
   * should not.
   * 
   * @return a unique physical identifier for this instance.
   */
  public String getId();




  /**
   * Return a logical name for this loader.
   * 
   * @return a logical identifier for this instance.
   */
  public String getName();




  /**
   * Set a logical name for this loader.
   * 
   * @param name the logical name of this loader
   */
  public void setName( String name );




  /**
   * Configure this loader with the given Config data.
   * 
   * @param cfg the configuration to apply to this loader.
   * 
   * @throws ConfigurationException if there were problems configuring the loader.
   */
  public void configure( Config cfg ) throws ConfigurationException;




  /**
   * Get a configuration object for this loader.
   * 
   * <p>This will never return null. If a configuration does not exist, an 
   * empty one will be created, set as the main configuration and returned.</p>
   * 
   * @return the currently set configuration 
   */
  public Config getConfig();




  /**
   * Get a context reference for this loader.
   * 
   * <p>This will never return null. All loaders must return a reference to a 
   * shared context for all components to use in the sharing of data.</p>
   * 
   * @return the currently set operational context for this loader 
   */
  public Context getContext();




  /**
   * Start the loader running.
   * 
   * <p>This is a blocking call. The thread will remain in this method until 
   * the loader terminates or an exception is thrown. Keep in mind that some 
   * loaders will daemonize and this call will return immediately. In such 
   * cases, the loader will terminate when the JVM terminates.</p>
   */
  public void start();




  /**
   * This allows the component to access the watchdog thread which runs 
   * continually to keep components running and detect when components become 
   * hung.
   * 
   * @return Return the watchdog component.
   */
  public WatchDog getWatchdog();




  /**
   * This allows components to access the scheduler which runs job on a 
   * re-occurring schedule; something like cron but limited to the runtime 
   * instance.
   * 
   * @return the Scheduler for this loader.
   */
  public Scheduler getScheduler();




  /**
   * This allows components to run simple components in a pool of threads.
   * 
   * @return The ThreadPool object used by this loader.
   */
  public ThreadPool getThreadPool();




  /**
   * Called by the shutdown hook when the JVM terminates.
   */
  public void shutdown();




  /**
   * Set the command line arguments read in
   * 
   * <p>This is set by any factory-style creation components such as the 
   * bootstrap loader so any command line arguments can be passed to the actual
   * loader. It is entirely possible that there are no command line arguments 
   * since loaders can be created from events within other components.</p>
   * 
   * @param args command line arguments
   */
  public void setCommandLineArguments( String[] args );




  /**
   * Initialize the symbol table in the context with system properties and 
   * other useful data.
   */
  public void initSymbolTable();




  /**
   * Access instrumentation services for this loader.
   * 
   * <p>This enables tracking operational statistics for all components in the 
   * runtime.
   * 
   * <p>Statistics tracking is disabled by default but can be toggled anytime. 
   * 
   * @return the StatBoard for this server.
   */
  public StatBoard getStats();

}
