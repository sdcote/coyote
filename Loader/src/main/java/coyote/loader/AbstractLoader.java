/*
 * Copyright (c) 2015 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.loader;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import coyote.commons.FileUtil;
import coyote.commons.GUID;
import coyote.commons.StringUtil;
import coyote.commons.UriUtil;
import coyote.commons.template.SymbolTable;
import coyote.commons.template.Template;
import coyote.dataframe.DataField;
import coyote.dataframe.DataFrame;
import coyote.i13n.StatBoard;
import coyote.i13n.StatBoardImpl;
import coyote.loader.cfg.Config;
import coyote.loader.cfg.ConfigurationException;
import coyote.loader.component.ManagedComponent;
import coyote.loader.log.Log;
import coyote.loader.log.LogMsg;
import coyote.loader.log.LogMsg.BundleBaseName;
import coyote.loader.log.Logger;
import coyote.loader.thread.ScheduledJob;
import coyote.loader.thread.Scheduler;
import coyote.loader.thread.ThreadJob;
import coyote.loader.thread.ThreadPool;


/**
 * 
 */
public abstract class AbstractLoader extends ThreadJob implements Loader, Runnable {

  public static final BundleBaseName MSG;
  static {
    MSG = new BundleBaseName("LoaderMsg");
  }

  /** Constant to assist in determining the full class name of loggers */
  private static final String LOGGER_PKG = Log.class.getPackage().getName();

  /** The command line arguments used to invoke the loader */
  protected String[] commandLineArguments = null;

  /** A map of all the component configurations keyed by their instance */
  protected final HashMap<Object, Config> components = new HashMap<Object, Config>();

  /** A map of components to when they last checked in...helps detect hung components. */
  protected final HashMap<Object, Long> checkin = new HashMap<Object, Long>();

  /** A map of components to the interval to when they should be considered hung and should be restarted. */
  protected final HashMap<Object, Long> hangtime = new HashMap<Object, Long>();

  /** The time to pause (sleep) between idle loop cycles (dflt=3000ms) */
  protected long parkTime = 3000;

  /** Our configuration */
  protected Config configuration = new Config();

  /** Our very simple thread pool */
  protected final ThreadPool threadpool = new ThreadPool();

  protected Scheduler scheduler = null;

  private final Context context = new LoaderContext();

  /** The component responsible for tracking operational statistics for all the components in this runtime */
  protected final StatBoard stats = new StatBoardImpl();

  /** Logical identifier for this instance. May not be unique across the system.*/
  protected String instanceName = null;

  /** A symbol table to support basic template functions */
  protected static final SymbolTable symbols = new SymbolTable();




  /**
   * @see coyote.loader.Loader#getId()
   */
  @Override
  public String getId() {
    return stats.getId();
  }




  /**
   * @see coyote.loader.Loader#getName()
   */
  @Override
  public String getName() {
    return instanceName;
  }




  /**
   * @see coyote.loader.Loader#setName(java.lang.String)
   */
  @Override
  public void setName(String name) {
    instanceName = name;
  }




  /**
   * Default constructor for all loaders
   */
  public AbstractLoader() {
    stats.setVersion(Loader.API_NAME, Loader.API_VERSION);
    stats.setState(LOADER, INITIALIZING);
  }




  protected static void confirmAppHome() {
    // see if there is a system property with a shared configuration directory
    String path = System.getProperties().getProperty(APP_HOME);

    // if there is a application home directory specified
    if (StringUtil.isNotBlank(path)) {

      // remove all the relations and duplicate slashes
      String appDir = FileUtil.normalizePath(path);

      // create a file reference to that shared directory 
      File homeDir = new File(appDir);

      if (homeDir.exists()) {
        // make sure it is a directory
        if (homeDir.isDirectory()) {
          if (!homeDir.canRead()) {
            System.out.println("The app.home property specified an un-readable (permissions) directory: " + appDir);
          }
        } else {
          System.out.println("The app.home property does not specify a directory: " + appDir);
        }
      } else {
        System.out.println("The app.home property does not exist: " + appDir);
      }

    }

  }




  protected static void confirmAppWork() {
    String path = System.getProperties().getProperty(APP_WORK);

    if (StringUtil.isNotBlank(path)) {

      String workDir = FileUtil.normalizePath(path);

      File workingDir = new File(workDir);

      if (workingDir.exists()) {
        if (workingDir.isDirectory()) {
          if (!workingDir.canWrite()) {
            System.out.println("The app.work property specified an un-writable (permissions) directory: " + workDir);
          }
        } else {
          System.out.println("The app.work property does not specify a directory: " + workDir);
        }
      } else {
        try {
          FileUtil.makeDirectory(workingDir);
        } catch (IOException e) {
          System.err.print("Could not create working directory specified in app.work property: " + workDir + " - " + e.getMessage());
        }
      }

    }

  }




  /**
   * Add a shutdown hook into the JVM to help us shut everything down nicely.
   * 
   * @param loader The loader to terminate
   */
  protected static void registerShutdownHook(final Loader loader) {
    try {
      Runtime.getRuntime().addShutdownHook(new Thread("LoaderHook") {
        public void run() {
          Log.debug(LogMsg.createMsg(MSG, "Loader.runtime_terminating", new Date()));

          if (loader != null) {
            loader.shutdown();
          }

          Log.debug(LogMsg.createMsg(MSG, "Loader.runtime_terminated", new Date()));
        }
      });
    } catch (java.lang.NoSuchMethodError nsme) {
      // Ignore
    } catch (Throwable e) {
      // Ignore
    }
  }




  /**
   * @see coyote.loader.Loader#configure(coyote.loader.cfg.Config)
   */
  @Override
  public void configure(Config cfg) throws ConfigurationException {
    configuration = cfg;

    // Fill the symbol table with system properties
    symbols.readSystemProperties();

    // setup logging as soon as we can
    initLogging();
  }




  /**
   * Load loggers for the entire process.
   * 
   * <p>This looks for a section named logging in the main loader and loads the 
   * loggers from there.</p>
   */
  private void initLogging() {
    List<Config> loggers = configuration.getSections(ConfigTag.LOGGING);

    // There is a logger section, remove all the existing loggers and start 
    // from scratch so we don't wind up with duplicate messages. Even if it is 
    // empty, assume the configuration contains the exact state of logging 
    // desired.
    if (loggers.size() > 0) {
      Log.removeAllLoggers();
    }

    // for each of the logger sections
    for (Config cfg : loggers) {

      // Find the individual loggers
      for (DataField field : cfg.getFields()) {

        // each logger is a frame
        if (field.isFrame()) {

          DataFrame cfgFrame = (DataFrame)field.getObjectValue();
          // we need named sections, not arrays
          if (StringUtil.isNotBlank(field.getName())) {

            // start building the configuration for logger
            Config loggerConfiguration = new Config();

            // use the name of the section as the class name
            String className = field.getName();

            // Make sure the class is fully qualified 
            if (StringUtil.countOccurrencesOf(className, ".") < 1) {
              className = LOGGER_PKG + "." + className;
            }

            // put the name of the class in the logger configuration
            loggerConfiguration.put(ConfigTag.CLASS, className);

            // add each of the fields in the config frame to the logger config
            for (DataField lfield : cfgFrame.getFields()) {

              // handle the target...make sure it is relative to ????
              if (ConfigTag.TARGET.equalsIgnoreCase(lfield.getName())) {
                String cval = lfield.getStringValue();
                if (StringUtil.isNotEmpty(cval)) {
                  cval = Template.preProcess(cval, symbols);
                }

                // the targets for loggers MUST be a URI
                if (!("stdout".equalsIgnoreCase(cval) || "stderr".equalsIgnoreCase(cval))) {
                  URI testTarget = UriUtil.parse(cval);

                  if (testTarget != null) {
                    if (testTarget.getScheme() == null) {
                      cval = "file://" + cval;
                    }
                  } else {
                    File file = new File(cval);
                    URI fileUri = FileUtil.getFileURI(file);
                    if(fileUri != null){
                      cval = fileUri.toString();
                    }
                  }

                  URI testUri = UriUtil.parse(cval);
                  if (testUri != null) {
                    if (UriUtil.isFile(testUri)) {
                      File logfile = UriUtil.getFile(testUri);

                      // make it absolute to our job directory
                      if (!logfile.isAbsolute()) {

                        // as a loader, we use app.home as our home directory 
                        // and therefore logging should be in a "log" directory 
                        // off of app.home
                        String path = System.getProperties().getProperty(APP_HOME);

                        if (StringUtil.isBlank(path)) {
                          path = System.getProperties().getProperty("user.dir");
                        }
                        File logdir = new File(path + "/log");
                        logfile = new File(logdir, logfile.getPath());
                        testUri = FileUtil.getFileURI(logfile);
                        cval = testUri.toString();
                      }
                    }

                  } else {
                    System.out.println("Bad target URI '" + cval + "'");
                    System.exit(11);
                  }

                }

                // set the validated URI in the target field
                loggerConfiguration.put(ConfigTag.TARGET, cval);
              } else if (ConfigTag.CATEGORIES.equalsIgnoreCase(lfield.getName())) {
                // Categories should be normalized to upper case
                String cval = lfield.getStringValue();
                if (StringUtil.isNotEmpty(cval)) {
                  cval = cval.toUpperCase();
                  loggerConfiguration.put(ConfigTag.CATEGORIES, cval);
                }
              } else {
                // pass the rest of the attributes unmolested
                loggerConfiguration.add(lfield);
              }
            }

            // create the logger
            Logger logger = createLogger(loggerConfiguration);

            if (logger != null) {
              // Get the name of the logger
              String name = loggerConfiguration.getString(ConfigTag.NAME);

              // If there is no name, try looking for an ID
              if (StringUtil.isBlank(name)) {
                name = loggerConfiguration.getString(ConfigTag.ID);
              }

              //If no name or ID, assign it a name
              if (Log.isBlank(name)) {
                name = GUID.randomGUID().toString();
              }

              try {
                Log.addLogger(name, logger);
              } catch (Exception e) {
                System.out.println(LogMsg.createMsg(MSG, "Loader.Could not add configured logger", name, logger.getClass(), e.getMessage()));
                System.exit(11);
              }
            } else {
              System.err.println(LogMsg.createMsg(MSG, "Loader.Could not create an instance of the specified logger"));
              System.exit(11);
            }

          } else {
            System.err.println(LogMsg.createMsg(MSG, "Loader.no_logger_classname", cfgFrame.toString()));
            System.exit(11);
          }
        } else {
          System.err.println(LogMsg.createMsg(MSG, "Loader.invalid_logger_configuration_section"));
          System.exit(11);
        } // must be a frame/section

      } // for each logger 

    } // for each logger section

    Log.debug(LogMsg.createMsg(MSG, "Loader.logging_initiated", new Date()));

  }




  private static Logger createLogger(Config cfg) {
    Logger retval = null;
    if (cfg != null) {
      if (cfg.contains(ConfigTag.CLASS)) {
        String className = cfg.getAsString(ConfigTag.CLASS);

        try {
          Class<?> clazz = Class.forName(className);
          Constructor<?> ctor = clazz.getConstructor();
          Object object = ctor.newInstance();

          if (object instanceof Logger) {
            retval = (Logger)object;
            try {
              retval.setConfig(cfg);
            } catch (Exception e) {
              Log.error(LogMsg.createMsg(MSG, "Loader.could_not_configure_logger {} - {} : {}", object.getClass().getName(), e.getClass().getSimpleName(), e.getMessage()));
            }
          } else {
            Log.warn(LogMsg.createMsg(MSG, "Loader.instance_is_not_a_logger of {} is not configurable", className));
          }
        } catch (ClassNotFoundException | NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
          Log.error(LogMsg.createMsg(MSG, "Loader.Could not instantiate {} reason: {} - {}", className, e.getClass().getName(), e.getMessage()));
        }
      } else {
        Log.error(LogMsg.createMsg(MSG, "Loader.Configuration frame did not contain a class name"));
      }
    }

    return retval;
  }




  /**
   * Cycle through the configuration and load all the components defined 
   * therein.
   * 
   * <p>This looks for a section named {@code Components} or {@code Component} 
   * and treat each section as a component configuration. This will of course 
   * require at least one attribute ({@code Class}) which defines the class of 
   * the object to load and configure.</p>
   */
  protected void initComponents() {
    List<Config> sections = configuration.getSections(ConfigTag.COMPONENT);
    if (sections.size() == 0) {
      Log.debug(LogMsg.createMsg(MSG, "Loader.section_not_found", ConfigTag.COMPONENT));
    }
    for (Config section : sections) {
      if (section != null) {
        for (Config cfg : section.getSections()) {
          if (cfg != null && cfg.getFieldCount() > 0) {
            activate(loadComponent(cfg), cfg);
          }
        }
      }
    }
  }




  /**
   * This will use the configuration to load and configure the component
   * 
   * <p>This is normally called in two locations: when the loader first runs 
   * (from {@link #initComponents()}) and in the {@link #watchdog()} method 
   * which will shutdown an inactive / hung component and restart a fresh one 
   * in its place.</p>
   *   
   * @param config The configuration of the component to load
   * 
   * @return the loaded managed component or null if none was specified in the 
   *         configuration
   */
  protected Object loadComponent(Config config) {
    Object retval = null;
    String className = config.getString(ConfigTag.CLASS);

    // Create the component
    if (StringUtil.isNotBlank(className)) {

      try {
        Class<?> clazz = Class.forName(className);
        Constructor<?> ctor = clazz.getConstructor();
        Object object = ctor.newInstance();

        if (object instanceof ManagedComponent) {
          ManagedComponent cmpnt = (ManagedComponent)object;

          // set the shared operational context all component use to share data
          cmpnt.setContext(getContext());

          // configure the component
          cmpnt.setConfiguration(config);

          // Set this loader as the watchdog if the component is interested 
          cmpnt.setLoader(this);

          // Add it to the components map
          synchronized (components) {
            components.put(object, config);
          }

          // return the component
          retval = cmpnt;
        } else if (object instanceof Runnable) {
          // return the runnable
          retval = (Runnable)object;
        } else {
          System.err.println(LogMsg.createMsg(MSG, "Loader.class_is_not_logic_component", className));
        }

      } catch (ClassNotFoundException | NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
        System.err.println(LogMsg.createMsg(MSG, "Loader.component_instantiation_error", className, e.getClass().getName(), e.getMessage()));
        System.exit(8);
      }
    } else {
      Log.warn(LogMsg.createMsg(MSG, "Loader.no_class_specified"));
      Log.debug(LogMsg.createMsg(MSG, "Loader.no_class_specified_debug", config.toString()));
    }

    return retval;
  }




  /**
   * Activate (start, run, whatever) this component as is appropriate for the
   * Loader and the instance of the object.
   * 
   * <p>It is expected that specialized Loaders will override this method and 
   * activate the component based on the needs of the application and the type 
   * of object it is.</p>
   * 
   * <p>In some cases, the Loader may need to check the configuration for 
   * licensing data to determine if the object is allowed to run. If not, the 
   * component may be removed from the Loader instance.
   * 
   * <p>The default behavior of this class is to check to see if it is a 
   * {@code ScheduledJob} and if so, place it in the scheduler. Otherwise, the 
   * object is run in the thread pool if it is a {@code ThreadJob} or 
   * implements {@code Runnable}.</p>
   *  
   * @param component the component to activate
   * @param config the Configuration used to create and configure the object.
   */
  protected void activate(Object component, Config config) {
    if (component != null) {
      if (component instanceof ScheduledJob) {
        Log.trace("Loading " + component.getClass().getName() + " in the scheduler");
        getScheduler().schedule((ScheduledJob)component);
        Thread.yield(); // allow component to run
      } else if (component instanceof ThreadJob) {
        try {
          Log.trace("Loading " + component.getClass().getName() + " in the threadpool");
          getThreadPool().handle((ThreadJob)component);
          Thread.yield(); // allow component to run
        } catch (InterruptedException e) {
          Log.error(LogMsg.createMsg(MSG, "Loader.activation_threadjob_error", e.getMessage()));
        }
      } else if (component instanceof Runnable) {
        Log.trace("Running " + component.getClass().getName() + " in the threadpool");
        getThreadPool().run((Runnable)component);
        Thread.yield(); // allow component to run
      } else {
        Log.error(LogMsg.createMsg(MSG, "Loader.activation_unrecognized_error", component.getClass().getName()));
      }

    }

  }




  /**
   * Terminate/shutdown the given component and remove it from the loader.
   * 
   * <p>This method will attempt to shutdown the component and then remove it 
   * from the list of managed components.</p>
   * 
   * @param component The component to terminate and remove from the list of managed components. 
   */
  protected void removeComponent(Object component) {
    if (component != null) {
      Log.trace("Removing " + component.getClass().getName() + " from loader");

      DataFrame frame = new DataFrame();
      frame.put("Message", "Managed Removal");
      if (component instanceof ManagedComponent) {
        safeShutdown((ManagedComponent)component, frame);
      } else if (component instanceof ThreadJob) {
        ((ThreadJob)component).shutdown(); // May not work as expected
      }
      synchronized (components) {
        if (components.remove(component) != null) {
          Log.trace("Successfully removed " + component.getClass().getName() + " from loader");
        } else {
          Log.warn("Component " + component.getClass().getName() + " did not apper to be tracked by loader");
        }
      }
    }
  }




  /**
   * 
   */
  protected void terminateComponents() {
    DataFrame frame = new DataFrame();
    frame.put("Message", "Normal termination");

    synchronized (components) {
      for (final Iterator<Object> it = components.keySet().iterator(); it.hasNext();) {
        final Object cmpnt = it.next();
        if (cmpnt instanceof ManagedComponent) {
          safeShutdown((ManagedComponent)cmpnt, frame);
        } else if (cmpnt instanceof ThreadJob) {
          ((ThreadJob)cmpnt).shutdown(); // May not work as expected
        }

        // remove the component
        it.remove();
      }
    }
  }




  /**
   * @return the parkTime
   */
  public long getParkTime() {
    return parkTime;
  }




  /**
   * @param parkTime the parkTime to set
   */
  public void setParkTime(long parkTime) {
    this.parkTime = parkTime;
  }




  /**
   * Shut the component down in a separate thread.
   * 
   * <p>This is a way to ensure that the calling thread does not get hung in a
   * deadlocked component while trying to shutdown a component.</p>
   * 
   * @param cmpnt the managed component to terminate
   * @param frame the dataframe which contains any additional information 
   *        relating to the shutdown request. This may be null.
   *
   * @return the Thread in which the shutdown is occurring.
   */
  protected Thread safeShutdown(final ManagedComponent cmpnt, final DataFrame frame) {
    final Thread closer = new Thread(new Runnable() {
      public void run() {
        cmpnt.shutdown(frame);
      }
    });

    closer.start();

    // give the component a chance to wake up and terminate
    Thread.yield();

    return closer;
  }




  /**
   * The main execution loop.
   * 
   * <p>This is where the thread spends its time monitoring components it has 
   * loaded and performing housekeeping operations.</p>
   * 
   * <p>While it is called a watchdog, this does not detect when a component is 
   * hung. The exact API for components to "pet the dog" is still in the 
   * works.</p>
   */
  protected void watchdog() {
    setActiveFlag(true);

    stats.setState(LOADER, WAIT_FOR_ACTIVE);
    List<String> failedToInit = waitForActive(12000);

    if (failedToInit.size() == 0) {
      Log.info(LogMsg.createMsg(MSG, "Loader.operational"));
      stats.setState(LOADER, RUNNING);

      while (!isShutdown()) {

        // Make sure that all this loaders are active, otherwise remove the
        // reference to them and allow GC to remove them from memory
        synchronized (components) {
          for (final Iterator<Object> it = components.keySet().iterator(); it.hasNext();) {
            final Object cmpnt = it.next();
            if (cmpnt instanceof ManagedComponent) {

              // Don't shut down scheduled jobs...they are inactive while they 
              // are waiting in the scheduler for their next execution.

              if (!(cmpnt instanceof ScheduledJob) && !((ManagedComponent)cmpnt).isActive()) {
                Log.info(LogMsg.createMsg(MSG, "Loader.removing_inactive_cmpnt", cmpnt.toString()));

                // get a reference to the components configuration
                final Config config = components.get(cmpnt);

                // communicate the reason for the shutdown
                DataFrame frame = new DataFrame();
                frame.put("Message", "Terminating due to inactivity");

                // try to shut it down properly
                safeShutdown((ManagedComponent)cmpnt, frame);

                // remove the component
                it.remove();

                // re-load the component
                Object newCmpnt = loadComponent(config);

                // activate it
                if (newCmpnt != null) {
                  activate(newCmpnt, config);
                }
              }
            }
          }

          // TODO cycle through all the hangtime objects and check their last 
          // check-in time. If expired, log the event and restart them like the 
          // above active check

          // Monitor check-in map size; if it is too large, we have a problem
          if (checkin.size() > components.size()) {
            Log.fatal(LogMsg.createMsg(MSG, "Loader.check_in_map_size", checkin.size(), components.size()));
          }

          // If we have no components which are active, there is not need for this
          // loader to remain running
          if (components.size() == 0) {
            Log.warn(LogMsg.createMsg(MSG, "Loader.no_components"));
            this.shutdown();
          }

        } // synchronized

        // Yield to other threads and sleep(wait) for a time
        park(parkTime);

      }
    } else {
      Log.fatal("The following " + failedToInit.size() + " components did not initialize within the timeout period:\n" + failedToInit);
    }
    stats.setState(LOADER, SHUTDOWN);
    if (Log.isLogging(Log.DEBUG_EVENTS)) {
      Log.debug(LogMsg.createMsg(MSG, "Loader.terminating"));
    }

    terminate();

    setActiveFlag(false);
  }




  /**
   * @param timeout number of miliseconds to wait for all the components to go active 
   * @return list of components which have not yet reported as active
   */
  private List<String> waitForActive(int timeout) {
    // Wait for all the components to activate. This prevents us from removing a component in the watchdog thread which is still initializing.
    long expiry = System.currentTimeMillis() + timeout;
    List<String> failedToInitialize = null;
    synchronized (components) {
      do {
        failedToInitialize = new ArrayList<String>();
        for (final Iterator<Object> it = components.keySet().iterator(); it.hasNext();) {
          final Object cmpnt = it.next();
          if (cmpnt instanceof ManagedComponent && !(cmpnt instanceof ScheduledJob) && !((ManagedComponent)cmpnt).isActive()) {
            failedToInitialize.add(cmpnt.toString());
          }
        }
        park(500);
      }
      while (failedToInitialize.size() > 0 && System.currentTimeMillis() < expiry);
    }

    return failedToInitialize;
  }




  /**
   * @see coyote.loader.Loader#checkIn(java.lang.Object)
   */
  @Override
  public void checkIn(Object component) {
    checkin.put(component, new Long(System.currentTimeMillis()));
  }




  /**
   * @see coyote.loader.WatchDog#setHangTime(long, java.lang.Object, coyote.loader.cfg.Config)
   */
  @Override
  public void setHangTime(long millis, Object component, Config cfg) {
    // TODO Auto-generated method stub
  }




  /**
   * @see coyote.loader.Loader#getWatchdog()
   */
  @Override
  public WatchDog getWatchdog() {
    return this;
  }




  /**
   * @see coyote.loader.Loader#getScheduler()
   */
  @Override
  public synchronized Scheduler getScheduler() {
    if (scheduler == null) {
      try {
        scheduler = new Scheduler();
        scheduler.daemonize(Scheduler.CLASS);
      } catch (Exception e) {
        Log.append(Log.WARN, LogMsg.createMsg(MSG, "Loader.scheduler_creation_error", e.getClass().getName(), e.getMessage()));
        scheduler = null;
      }
    }

    // return a reference to the running scheduler
    return scheduler;
  }




  /**
   * @see coyote.loader.Loader#start()
   */
  @Override
  public void start() {
    // do-nothing implementation
  }




  /**
   * @see coyote.loader.Loader#getThreadPool()
   */
  @Override
  public ThreadPool getThreadPool() {
    return threadpool;
  }




  /**
   * @return the command line arguments used to invoke this loader
   */
  public String[] getCommandLineArguments() {
    return commandLineArguments;
  }




  /**
   * Initialize the symbol table in the context with system properties and 
   * other useful data.
   */
  public void initSymbolTable() {
    if (context != null) {
      if (context.getSymbols() != null) {
        // Fill the symbol table with system properties
        context.getSymbols().readSystemProperties();
      } else {
        Log.warn("no symbols in context to initialize");
      }
    } else {
      Log.warn("no context defined to init symbol table");
    }
  }




  /**
   * @param args the command line arguments to set
   */
  public void setCommandLineArguments(String[] args) {
    commandLineArguments = args;
  }




  /**
   * @see coyote.loader.Loader#getConfig()
   */
  @Override
  public Config getConfig() {
    if (configuration == null) {
      configuration = new Config();
    }
    return configuration;
  }




  /**
   * @see coyote.loader.Loader#getContext()
   */
  @Override
  public Context getContext() {
    return context;
  }




  /**
   * Access instrumentation services for this loader.
   * 
   * <p>This enables tracking operational statistics for all components in the 
   * runtime.
   * 
   * <p>Statistics tracking is disabled by default but can be toggled antime. 
   * 
   * @return the StatBoard for this server.
   */
  @Override
  public StatBoard getStats() {
    return stats;
  }

}
