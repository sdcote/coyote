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

import coyote.loader.cfg.Config;


/**
 * The NullLogger class models a logger that does nothing.
 * 
 * <p>This allows calls to a logger to do nothing. It is helpful in creating an 
 * instance of this class as opposed to setting an object attribute to null so 
 * the caller does not have to always check for a null reference after calling 
 * an accessor method to get a logger.</p>
 * 
 * <p>See Martin Fowler's refactoring books (Refactoring to Patterns) or 
 * (Refactoring: Improving the Design of Existing Code) for details on using 
 * Null Objects in software.</p>
 */
public class NullLogger implements Logger {

  private static final String NULL_URI = "null:///";




  /**
   * @see coyote.loader.log.Logger#append(java.lang.String, java.lang.Object, java.lang.Throwable)
   */
  @Override
  public void append( final String category, final Object event, final Throwable cause ) {}




  /**
   * @see coyote.loader.log.Logger#disable()
   */
  @Override
  public void disable() {}




  /**
   * @see coyote.loader.log.Logger#enable()
   */
  @Override
  public void enable() {}




  /**
   * @see coyote.loader.log.Logger#getConfig()
   */
  @Override
  public Config getConfig() {
    return null;
  }




  /**
   * @see coyote.loader.log.Logger#getMask()
   */
  @Override
  public long getMask() {
    return 0;
  }




  /**
   * @see coyote.loader.log.Logger#getTarget()
   */
  @Override
  public URI getTarget() {
    try {
      return new URI( NULL_URI );
    } catch ( final URISyntaxException e ) {
      System.out.println( e.getMessage() );
    }
    return null;
  }




  /**
   * @see coyote.loader.log.Logger#initialize()
   */
  @Override
  public void initialize() {}




  /**
   * @see coyote.loader.log.Logger#isLocked()
   */
  @Override
  public boolean isLocked() {
    return false;
  }




  /**
   * @see coyote.loader.log.Logger#setConfig(coyote.loader.cfg.Config)
   */
  @Override
  public void setConfig( final Config cfg ) {}




  /**
   * @see coyote.loader.log.Logger#setLocked(boolean)
   */
  @Override
  public void setLocked( final boolean flag ) {}




  /**
   * @see coyote.loader.log.Logger#setMask(long)
   */
  @Override
  public void setMask( final long mask ) {}




  /**
   * @see coyote.loader.log.Logger#setTarget(java.net.URI)
   */
  @Override
  public void setTarget( final URI uri ) {}




  /**
   * @see coyote.loader.log.Logger#startLogging(java.lang.String)
   */
  @Override
  public void startLogging( final String category ) {}




  /**
   * @see coyote.loader.log.Logger#stopLogging(java.lang.String)
   */
  @Override
  public void stopLogging( final String category ) {}




  /**
   * @see coyote.loader.log.Logger#terminate()
   */
  @Override
  public void terminate() {}




  /**
   * @see coyote.loader.log.Logger#getFormatter()
   */
  @Override
  public Formatter getFormatter() {
    return null;
  }




  /**
   * @see coyote.loader.log.Logger#setFormatter(coyote.loader.log.Formatter)
   */
  @Override
  public void setFormatter( Formatter formatter ) {}

}
