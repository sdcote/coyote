/*
 * Copyright (c) 2007 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.loader.log;

import java.util.Enumeration;


/**
 * Log Is a facade to the Log Kernel.
 * 
 * <p>This serves two purposes, first it is an abstraction for the underlying 
 * implementation and second it acts as a peer to other facades (e.g. SLF4J) 
 * to keep the stack count the same for the formatters using the stack to 
 * determine location of logging.</p> 
 */
public final class Log {

  /**
   * The name of the category of events where an expected event has occurred 
   * and additional (verbose) information will be displayed including the 
   * location in the code where the event occurred.
   */
  public static final String TRACE = "TRACE";

  /**
   * The name of the category of events where an expected event has occurred 
   * and additional (verbose) information will be displayed.
   */
  public static final String DEBUG = "DEBUG";

  /** 
   * The name of the category of events where an expected event has occurred. 
   */
  public static final String INFO = "INFO";

  /** 
   * The name of the category of events where an expected event has occurred 
   * but should be noticed by operations. Monitoring systems scan for events of 
   * this category.
   */
  public static final String NOTICE = "NOTICE";

  /**
   * The name of the category of events where an unexpected event has occurred 
   * but execution can continue. The code can compensate for the event and the 
   * occurrence may even be acceptable.
   */
  public static final String WARN = "WARN";

  /** 
   * The name of the category of events where an unexpected event has occurred 
   * but execution can continue while operations may not produce the expected 
   * results.
   */
  public static final String ERROR = "ERROR";

  /** 
   * The name of the category of events where an unexpected event has occurred 
   * and all or part of the thread of execution can not continue.
   */
  public static final String FATAL = "FATAL";

  /** The category mask for the TRACE category. */
  public static final long TRACE_EVENTS = LogKernel.getCode( Log.TRACE );
  /** The category mask for the DEBUG category. */
  public static final long DEBUG_EVENTS = LogKernel.getCode( Log.DEBUG );
  /** The category mask for the INFO category. */
  public static final long INFO_EVENTS = LogKernel.getCode( Log.INFO );
  /** The category mask for the NOTICE category. */
  public static final long NOTICE_EVENTS = LogKernel.getCode( Log.NOTICE );
  /** The category mask for the WARN category. */
  public static final long WARN_EVENTS = LogKernel.getCode( Log.WARN );
  /** The category mask for the ERROR category. */
  public static final long ERROR_EVENTS = LogKernel.getCode( Log.ERROR );
  /** The category mask for the FATAL category. */
  public static final long FATAL_EVENTS = LogKernel.getCode( Log.FATAL );
  /** The category mask for all the events. */
  public static final long ALL_EVENTS = -1L;

  /** 
   * The name of the default logger, or the name of the logger created and 
   * enabled by the logging subsystem when first accessed and initialized. 
   */
  public static final String DEFAULT_LOGGER_NAME = "default";

  static {
    // are the only logging framework
    LogKernel.addLogger( LogKernel.DEFAULT_LOGGER_NAME, new NullAppender( Log.INFO_EVENTS | Log.NOTICE_EVENTS | Log.WARN_EVENTS | Log.ERROR_EVENTS | Log.FATAL_EVENTS ) );
  } // static initializer




  /**
   * Checks if a string is not null, empty ("") and not only whitespace.
   * 
   * @param str the String to check, may be null
   * 
   * @return <code>true</code> if the String is not empty and not null and not
   *         whitespace
   */
  public static boolean isBlank( String str ) {
    int strLen;
    if ( str == null || ( strLen = str.length() ) == 0 ) {
      return true;
    }
    for ( int i = 0; i < strLen; i++ ) {
      if ( ( Character.isWhitespace( str.charAt( i ) ) == false ) ) {
        return false;
      }
    }
    return true;
  }




  /**
   * Add the specified logger to the static collection of loggers.
   *
   * @param name The name of the logger.
   * @param logger The logger to add.
   */
  public synchronized static void addLogger( final String name, final Logger logger ) {
    LogKernel.addLogger( name, logger );
  }




  /**
   * Log the object using the info category
   *
   * @param category The category of the desired log operation 
   * @param event The event to log.
   */
  public static void append( final long category, final Object event ) {
    LogKernel.append( category, event, null );
  }




  /**
   * Send append( category, message ) to each logger that is logging the
   * specified category.
   *
   * @param code The category code.
   * @param event The event to log.
   * @param cause The cause of the event.
   */
  public synchronized static void append( final long code, final Object event, final Throwable cause ) {
    LogKernel.append( code, event, cause );
  }




  /**
   * Log the object using the info category.
   *
   * @param event The event to log.
   */
  public static void append( final Object event ) {
    LogKernel.append( Log.INFO_EVENTS, event, null );
  }




  /**
   * Send append( category, message ) to each logger that is logging the
   * specified category.
   *
   * @param category The category of the desired log operation 
   * @param event The event to log.
   */
  public synchronized static void append( final String category, final Object event ) {
    LogKernel.append( category, event, null );
  }




  /**
   * Send append( category, message ) to each logger that is logging the
   * specified category.
   *
   * @param category The category.
   * @param event The event to log.
   * @param cause The exception that caused the log entry. Can be null.
   */
  public synchronized static void append( final String category, final Object event, final Throwable cause ) {
    LogKernel.append( Log.getCode( category ), event, null );
  }




  /**
   * Log the event with category "DEBUG".
   *
   * <p>This is equivalent to <tt>log( DEBUG_EVENTS, event );</tt></p>
   *
   * @param event The event to log
   */
  public static void debug( final Object event ) {
    Log.append( Log.DEBUG_EVENTS, event, null );
  }




  /**
   * Log the event with category "DEBUG".
   *
   * <p>This is equivalent to <tt>log( DEBUG_EVENTS, event, cause );</tt></p>
   *
   * @param event The event to log
   * @param cause The cause of the event.
   */
  public static void debug( final Object event, final Throwable cause ) {
    Log.append( Log.DEBUG_EVENTS, event, cause );
  }




  /**
   * Disable the specified logger.
   * 
   * <p>The named logger will save a mask of all the categories it is currently 
   * logging and set the current category mask to zero effectively turning the 
   * logger off.</p>
   *
   * @param name The name of the logger to temporally disable.
   * 
   * @see #enableLogger(String)
   */
  public static synchronized void disableLogger( final String name ) {
    LogKernel.disableLogger( name );
  }




  /**
   * Re-enable the specified logger.
   * 
   * <p>The named logger will begin logging the categories it was previously
   * logging when it was disabled.</p>
   *
   * @param name The name of the logger to re-enable.
   * 
   * @see #disableLogger(String)
   */
  public static synchronized void enableLogger( final String name ) {
    LogKernel.enableLogger( name );
  }




  /**
   * Log the event with category "ERROR".
   *
   * <p>This is equivalent to <tt>log( ERROR_EVENTS, event );</tt></p>
   *
   * @param event
   */
  public static void error( final Object event ) {
    Log.append( Log.ERROR_EVENTS, event, null );
  }




  /**
   * Log the event with category "ERROR".
   *
   * <p>This is equivalent to <tt>log( ERROR_EVENTS, event, cause );</tt></p>
   *
   * @param event The event to log
   * @param cause The cause of the event.
   */
  public static void error( final Object event, final Throwable cause ) {
    Log.append( Log.ERROR_EVENTS, event, cause );
  }




  /**
   * Log the event with category "FATAL".
   *
   * <p>This is equivalent to <tt>log( FATAL_EVENTS, event );</tt></p>
   *
   * @param event
   */
  public static void fatal( final Object event ) {
    Log.append( Log.FATAL_EVENTS, event, null );
  }




  /**
   * Log the event with category "FATAL".
   *
   * <p>This is equivalent to <tt>log( FATAL_EVENTS, event );</tt></p>
   *
   * @param event The event to log
   * @param cause The cause of the event.
   */
  public static void fatal( final Object event, final Throwable cause ) {
    Log.append( Log.FATAL_EVENTS, event, cause );
  }




  /**
   * Access the name of the given category code.
   * 
   * @return The category associated with the specified code.
   */
  public synchronized static String getCategory( final long code ) {
    return LogKernel.codeToString.get( new Long( code ) );
  }




  /**
   * Access all the named categories currently registered with the logging
   * subsystem.
   * 
   * <p>This is a good way to discover what categories have been registered by
   * components &quot;behind the scenes&quot;. When used in development trouble 
   * shooting activities, a developer may discover logging categories to enable 
   * and give new insight into the operation of the application.</p>
   *
   * @return An array of category names.
   * 
   * @see #getCode(String)
   */
  public synchronized static String[] getCategoryNames() {
    return LogKernel.getCategoryNames();
  }




  /**
   * Return the code for the specified category.
   *
   * <p>This allows us to specify 58 user-defined categories to the existing 6
   * before an over-run occurs.</p>
   *
   * @param category The category name.
   *
   * @return The code for the given category.
   */
  public static synchronized long getCode( final String category ) {
    return LogKernel.getCode( category );
  }




  /**
   * Access the default logger, that is, the logger with the name of "default".
   * 
   * @return The default logger, or null if there is no logger named "default".
   */
  public synchronized static Logger getDefaultLogger() {
    return LogKernel.getDefaultLogger();
  }




  /**
   * Return the number of milliseconds since logging began.
   *
   * <p>This is useful for a single point of reference between log entries.</p>
   *
   * @return The number of milliseconds since logging began.
   */
  public static long getInterval() {
    return LogKernel.getInterval();
  }




  /**
   * Return the Logger object with the given name.
   *
   * @param name The name of the logger to retrieve.
   *
   * @return The reference to the Logger object with the given name.
   */
  public synchronized static Logger getLogger( final String name ) {
    return LogKernel.getLogger( name );
  }




  /**
   * Access the number of loggers currently in the static collection.
   *
   * @return the number of logger in the static collection.
   */
  public synchronized static int getLoggerCount() {
    return LogKernel.getLoggerCount();
  }




  /**
   * @return an enumeration over all the current logger names.
   */
  public synchronized static Enumeration<String> getLoggerNames() {
    return LogKernel.getLoggerNames();
  }




  /**
   * Return an enumeration over all the current loggers.
   *
   * @return an enumeration over all the current loggers.
   */
  public synchronized static Enumeration<Logger> getLoggers() {
    return LogKernel.getLoggers();
  }




  /**
   * Log the event with category "INFO".
   *
   * <p>This is equivalent to <tt>log( INFO_EVENTS, event );</tt></p>
   *
   * @param event
   */
  public static void info( final Object event ) {
    Log.append( Log.INFO_EVENTS, event, null );
  }




  /**
   * Log the event with category "INFO".
   *
   * <p>This is equivalent to <tt>log( INFO_EVENTS, event, cause );</tt></p>
   *
   * @param event The event to log
   * @param cause The cause of the event.
   */
  public static void info( final Object event, final Throwable cause ) {
    Log.append( Log.INFO_EVENTS, event, cause );
  }




  /**
   * Log the event with category "NOTICE".
   *
   * <p>This is equivalent to <tt>log( NOTICE_EVENTS, event );</tt></p>
   *
   * @param event
   */
  public static void notice( final Object event ) {
    Log.append( Log.NOTICE_EVENTS, event, null );
  }




  /**
   * Log the event with category "NOTICE".
   *
   * <p>This is equivalent to <tt>log( NOTICE_EVENTS, event, cause );</tt></p>
   *
   * @param event The event to log
   * @param cause The cause of the event.
   */
  public static void notice( final Object event, final Throwable cause ) {
    Log.append( Log.NOTICE_EVENTS, event, cause );
  }




  /**
   * Return true if at least one of the loggers is logging a category defined
   * by the mask.
   * 
   * <p>This is the fastest way to determine if it is worth the time and effort 
   * to construct a message before the append method is called.</p> 
   *
   * @param mask The mask.
   *
   * @return true if at least one of the loggers is logging a category defined
   *         by the mask false otherwise
   */
  public static boolean isLogging( final long mask ) {
    return LogKernel.isLogging( mask );
  }




  /**
   * Return true if at least one of the loggers is logging the specified
   * category.
   *
   * @param category The category.
   *
   * @return true if at least one of the loggers is logging a category defined
   *         by the mask false otherwise
   */
  public static boolean isLogging( final String category ) {
    return LogKernel.isLogging( category );
  }




  /**
   * Check to see if a named logger is permanent.
   * 
   * @param name The name of the logger to check.
   * 
   * @return True if the logger is tagged as permanent, false otherwise.
   */
  public static boolean isPermanent( final String name ) {
    return LogKernel.isPermanent( name );
  }




  /**
   * Make the logger with the given name permanent.
   * 
   * <p>The logger with the given name will not be removed from the logging 
   * system once made permanent. There is no way to undo this operation for the 
   * life of the runtime.</p>
   * 
   * <p><strong>NOTE:</strong> It is possible to make a named logger permanent 
   * even before it is added to the system. Once a string is passed to this 
   * method, the logging system will forever ignore requests to remove it.</p>
   * 
   * @param name The name to ignore on any remove request.
   */
  public static void makeLoggerPermanent( final String name ) {
    LogKernel.makeLoggerPermanent( name );
  }




  /**
   * Remove the default logger from the logging fixture.
   * 
   * <p>Removing the logger from the static collection will result in the 
   * default logger not being included in collective <tt>append()</tt> 
   * operations.</p>
   */
  public synchronized static void removeDefaultLogger() {
    LogKernel.removeDefaultLogger();
  }




  /**
   * Remove the specified logger from the static collection of loggers.
   * 
   * <p>If the name of the logger appears on the permanent logger list, it will
   * NOT be removed. The operation will be silently ignored. This includes the
   * default logger name.</p>
   *
   * @param name The name of the logger.
   */
  public static synchronized void removeLogger( final String name ) {
    LogKernel.removeLogger( name );
  }




  /**
   * Removes all logers from the system - including permanent loggers.
   */
  public static synchronized void removeAllLoggers() {
    LogKernel.removeAllLoggers();
  }




  /**
   * Set the mask of all the loggers.
   *
   * @param mask The mask to set to all loggers in the collection.
   */
  public static synchronized void setMask( final long mask ) {
    LogKernel.setMask( mask );
  }




  /**
   * Instruct all loggers to start logging events of the specified category.
   *
   * @param category The category.
   */
  public synchronized static void startLogging( final String category ) {
    LogKernel.startLogging( category );
  }




  /**
   * Instruct all loggers to stop logging events of the specified category.
   *
   * @param category The category.
   */
  public synchronized static void stopLogging( final String category ) {
    LogKernel.stopLogging( category );
  }




  /**
   * Log the event with category "TRACE".
   *
   * <p>This is equivalent to <tt>log( TRACE_EVENTS, event );</tt></p>
   *
   * @param event
   */
  public static void trace( final Object event ) {
    Log.append( Log.TRACE_EVENTS, event, null );
  }




  /**
   * Log the event with category "TRACE".
   *
   * <p>This is equivalent to <tt>log( TRACE_EVENTS, event, cause );</tt></p>
   *
   * @param event The event to log
   * @param cause The cause of the event.
   */
  public static void trace( final Object event, final Throwable cause ) {
    Log.append( Log.TRACE_EVENTS, event, cause );
  }




  /**
   * Log the event with category "WARN".
   *
   * <p>This is equivalent to <tt>log( WARN_EVENTS, event );</tt></p>
   *
   * @param event
   */
  public static void warn( final Object event ) {
    Log.append( Log.WARN_EVENTS, event, null );
  }




  /**
   * Log the event with category "WARN".
   *
   * <p>This is equivalent to <tt>log( WARN_EVENTS, event, cause );</tt></p>
   *
   * @param event The event to log
   * @param cause The cause of the event.
   */
  public static void warn( final Object event, final Throwable cause ) {
    Log.append( Log.WARN_EVENTS, event, cause );
  }




  /**
   * Private constructor to keep instances of this class from being created.
   */
  private Log() {}

}
