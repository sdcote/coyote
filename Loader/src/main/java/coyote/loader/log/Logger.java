/*
 * Copyright (c) 2007 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.loader.log;

import java.net.URI;

import coyote.loader.cfg.Config;


/**
 * Logger declares methods that are implemented by all loggers.
 */
public interface Logger {
  /** The name of the configuration attribute containing the URI to the target location of the log. */
  public static final String TARGET_TAG = "Target";

  /** The name of the configuration attribute containing the categories this logger should log. */
  public static final String CATEGORY_TAG = "Categories";

  /** The category delimiters used in properties. */
  public static final String CATEGORY_DELIMS = ",:;| ";

  /** the name of the configuration property indicating if this logger is enabled. (default=true) */
  public static final String ENABLED_TAG = "Enabled";




  /**
   * Return the mask that defines which events the logger should log.
   * 
   * @return TODO Complete Documentation
   */
  public abstract long getMask();




  /**
   * Initializes the logger.
   */
  public abstract void initialize();




  /**
   * Terminates the logger.
   */
  public abstract void terminate();




  /**
   * Set the mask that defines the events which the logger should log.
   * 
   * @param  mask
   */
  public abstract void setMask( long mask );




  /**
   * Start logging events of the specified category.
   *
   * @param category The category.
   */
  public abstract void startLogging( String category );




  /**
   * Stop logging events of the specified category.
   *
   * @param category The category.
   */
  public abstract void stopLogging( String category );




  /**
   * Method enable
   */
  public abstract void enable();




  /**
   * Method disable
   */
  public abstract void disable();




  /**
   * Set the destination of our log messages.
   * 
   * @param uri  The target for our log messages.
   */
  public void setTarget( URI uri );




  /**
   * Returns the destination of our log messages.
   * 
   * @return  The target of our log messages.
   */
  public URI getTarget();




  /**
   * @return The currently set config.
   */
  public Config getConfig();




  /**
   * Configure the operation of this logger by setting its config.
   * 
   * <p>This only set the reference to the configuration. These values are not 
   * accessed until the logger is initialized. It is at that time any problems 
   * with the configuration will be realized.</p>
   * 
   * <p>Initialization occurs when the {@code Logger} is added to the main 
   * {@link Log} fixture.</p>
   * 
   * @param cfg The config from which to read the logger configuration.
   */
  public void setConfig( Config cfg );




  /**
   * If enabled, log an event of the specified category.
   * 
   * <p>A category is simply a string which tags the log entry. It is often 
   * used to filter log entries during analysis.</p>
   * 
   * <p>The event is the entry to place in the logs. The formatter will take 
   * the event and convert it into a string according the the formatters logic. 
   * Normally, the entry's <tt>toString()</tt> method is called, but some 
   * specific loggers will represent the entry differently if it knows how to 
   * handle the object.</p>
   *
   * @param category The category of the event
   * @param event The event, which is often just a simple string.
   * @param cause The exception that caused the log entry. Can be null.
   * 
   * @see coyote.loader.log.Formatter
   */
  public abstract void append( String category, Object event, Throwable cause );




  /**
   * Determine if the categories are locked and can not be changed. 
   * 
   * <p>Locking a logger prevents the unintentional modification of the mask
   * that determines which categories are handled by the logger. This means it 
   * is possible to have a logger that will only output trace messages, for 
   * example, regardless of what other code may attempt to set globally.</p>
   * 
   * <p>The static Log class checks this method prior to setting mask values. 
   * It is expected that the <tt>Logger</tt> instance may itself check its own 
   * &quot;locked&quot; state prior to letting its mask be changed.</p>
   * 
   * <p>While it is true that another class may &quot;unlock&quot; the logger, 
   * the unintentional alteration of the mask can still be prevented in most 
   * cases.</p>
   * 
   * @return  True if the instance is locked, false is its mask may be changed.
   */
  public abstract boolean isLocked();




  /**
   * Set the ability to modify the categories of this logger.
   * 
   * @param  flag True means the logger is locked and the categories can NOT be 
   *         changed, while false unlocks the logger allowing category changed.
   */
  public abstract void setLocked( boolean flag );




  /**
   * @return this loggers formatter
   */
  public Formatter getFormatter();




  /**
   * Set the formatter for this logger.
   * 
   * @param formatter The formatter this logger is to use.
   */
  public void setFormatter( final Formatter formatter );

}