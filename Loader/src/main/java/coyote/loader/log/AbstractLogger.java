/*
 * Copyright (c) 2007 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 *
 * Contributors:
 *   Stephan D. Cote 
 *      - Initial concept and implementation
 */
package coyote.loader.log;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.StringTokenizer;

import coyote.loader.cfg.Config;


/**
 * LoggerBase is an abstract class that implements everything about an Logger 
 * except for a concrete implementation of append().
 */
public abstract class AbstractLogger implements Logger {
  protected boolean initialized = false;

  private volatile long mask;

  private volatile long disabledMask;

  private volatile boolean locked;

  protected Formatter formatter;

  protected Config config;

  /** The target of our logging operations */
  protected URI target;




  /**
   *
   */
  public AbstractLogger() {
    this( 0 );
  }




  /**
   * @param mask
   */
  public AbstractLogger( final long mask ) {
    this.mask = mask;
    formatter = new DefaultFormatter();
    config = null;
  }




  /**
   * @return the mask that defines which events this logger should log.
   */
  public long getMask() {
    return mask;
  }




  /**
   * Set the mask that defines the events which this logger should log.
   * 
   * @param mask  The mask
   */
  public void setMask( final long mask ) {
    if ( !locked ) {
      this.mask = mask;

      LogKernel.recalcMasks();
    }
  }




  /**
   * Add the specified mask to the current mask setting.
   *
   * @param mask The mask
   */
  public synchronized void addMask( final long mask ) {
    if ( !locked ) {
      this.mask |= mask;

      LogKernel.recalcMasks();
    }
  }




  /**
   * Remove the specified mask from the current mask setting.
   *
   * @param mask
   */
  public synchronized void removeMask( long mask ) {
    if ( !locked ) {
      this.mask &= ~mask;

      LogKernel.recalcMasks();
    }
  }




  /**
   * Start logging events of the specified category.
   *
   * @param category The category.
   */
  public void startLogging( final String category ) {
    addMask( Log.getCode( category ) );
  }




  /**
   * Stop logging events of the specified category.
   *
   * @param category The category.
   */
  public void stopLogging( final String category ) {
    removeMask( Log.getCode( category ) );
  }




  /**
   * Instructs this logger to zero-out its mask, effectively turning this logger
   * off for all categories.
   *
   * <p>The original mask is saved so it can be re-applied if enable() is later
   * called.</p>
   */
  public synchronized void disable() {
    if ( mask == 0 ) {
      return;
    }

    disabledMask |= mask;
    mask = 0;

    LogKernel.recalcMasks();
  }




  /**
   * Instructs the logger to re-enable itself back to logging those categories
   * it was previously logging.
   */
  public synchronized void enable() {
    // avoid enabling a logger that was not disabled. If a logger was operating
    // just fine and the enable() method is called before disable(), the enable
    // method would over-write the mask with all zeros, effectively disabling
    // the logger with no way to return the logger back to it original state!
    if ( ( mask != 0 ) && ( disabledMask == 0 ) ) {
      return;
    }

    mask |= disabledMask;
    disabledMask = 0;

    LogKernel.recalcMasks();
  }




  /**
   * Set the destination of our log messages.
   * 
   * @param uri  The target for our log messages.
   */
  public void setTarget( final URI uri ) {
    target = uri;
  }




  /**
   * Returns the destination of our log messages.
   * 
   * @return  The target of our log messages.
   */
  public URI getTarget() {
    if ( target == null ) {
      try {
        target = new URI( config.getAsString( AbstractLogger.TARGET_TAG ) );
      } catch ( final URISyntaxException e ) {}
    }
    return target;
  }




  /**
   * @return  The currently set configuration.
   */
  public Config getConfig() {
    return config;
  }




  /**
   * Configure the operation of this logger by setting its configuration.
   * 
   * @param cfg The configuration from which to read the logger configuration.
   */
  public void setConfig( final Config cfg ) {
    this.config = cfg;
  }




  /**
   * Start logging all categories
   */
  public void logAll() {
    mask = Long.MAX_VALUE;

    LogKernel.recalcMasks();
  }




  /**
   * Stop logging all categories
   */
  public void logNone() {
    mask = 0;

    LogKernel.recalcMasks();
  }




  /**
   * @return this loggers formatter
   */
  @Override
  public Formatter getFormatter() {
    return formatter;
  }




  /**
   * Set the formatter for this logger.
   * 
   * @param formatter The formatter this logger is to use.
   */
  @Override
  public void setFormatter( final Formatter formatter ) {
    this.formatter = formatter;
  }




  /**
   * Initialize the logger
   */
  public void initialize() {
    if ( config != null ) {
      // if the target is null, then check the properties object for the URI
      if ( target == null ) {
        // target = UriUtil.parse( properties.getProperty( TARGET_TAG ) );
        try {
          target = new URI( config.getString( Logger.TARGET_TAG ) );
        } catch ( final Exception e ) {
          System.err.println( "Invalid logger target URI (" + e.getMessage() + ") - '" + config.get( Logger.TARGET_TAG ) + "'" );
        }
      }

      // Case insensitive search for categories to log
      if ( config.getString( Logger.CATEGORY_TAG ) != null ) {
        for ( final StringTokenizer st = new StringTokenizer( config.getString( Logger.CATEGORY_TAG ), Logger.CATEGORY_DELIMS ); st.hasMoreTokens(); startLogging( st.nextToken().toUpperCase() ) );
      }
    }

    // determine if this logger is disabled, if so set mask to 0
    if ( config != null && config.getString( Logger.ENABLED_TAG ) != null ) {
      String str = config.getString( Logger.ENABLED_TAG ).toLowerCase();
      if ( "false".equals( str ) || "0".equals( str ) || "no".equals( str ) ) {
        disable(); // set the mask to 0
      }
    }

  }




  /**
   * @see coyote.loader.log.Logger#isLocked()
   */
  public boolean isLocked() {
    return locked;
  }




  /**
   * @see coyote.loader.log.Logger#setLocked(boolean)
   */
  public void setLocked( final boolean flag ) {
    locked = flag;
  }




  /**
   * If enabled, log an event of the specified category.
   *
   * @param category The category.
   * @param event The event, which is often just a simple string.
   * @param cause The exception that caused the log entry. Can be null.
   */
  public abstract void append( String category, Object event, Throwable cause );

}