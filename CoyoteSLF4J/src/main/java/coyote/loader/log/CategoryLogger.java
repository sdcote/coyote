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
package coyote.loader.log;

import org.slf4j.Logger;
import org.slf4j.Marker;


/**
 * This is a logger which intercepts logging requests via the SLF4J API and 
 * sends them to a category logger.
 */
public class CategoryLogger implements Logger {

  private final String loggerName;




  public CategoryLogger( final String name ) {
    loggerName = name;
  }




  /**
   * @see org.slf4j.Logger#debug(org.slf4j.Marker, java.lang.String)
   */
  @Override
  public void debug( final Marker marker, final String msg ) {
    LogKernel.append( Log.DEBUG_EVENTS, msg, null );
  }




  /**
   * @see org.slf4j.Logger#debug(org.slf4j.Marker, java.lang.String, java.lang.Object)
   */
  @Override
  public void debug( final Marker marker, final String format, final Object arg ) {
    LogKernel.append( Log.DEBUG_EVENTS, String.format( format, arg ), null );
  }




  /**
   * @see org.slf4j.Logger#debug(org.slf4j.Marker, java.lang.String, java.lang.Object[])
   */
  @Override
  public void debug( final Marker marker, final String format, final Object... arguments ) {
    LogKernel.append( Log.DEBUG_EVENTS, String.format( format, arguments ), null );
  }




  /**
   * @see org.slf4j.Logger#debug(org.slf4j.Marker, java.lang.String, java.lang.Object, java.lang.Object)
   */
  @Override
  public void debug( final Marker marker, final String format, final Object arg1, final Object arg2 ) {
    LogKernel.append( Log.DEBUG_EVENTS, String.format( format, arg1, arg2 ), null );
  }




  /**
   * @see org.slf4j.Logger#debug(org.slf4j.Marker, java.lang.String, java.lang.Throwable)
   */
  @Override
  public void debug( final Marker marker, final String msg, final Throwable t ) {
    LogKernel.append( Log.DEBUG_EVENTS, msg, t );
  }




  /**
   * @see org.slf4j.Logger#debug(java.lang.String)
   */
  @Override
  public void debug( final String msg ) {
    LogKernel.append( Log.DEBUG_EVENTS, msg, null );
  }




  /**
   * @see org.slf4j.Logger#debug(java.lang.String, java.lang.Object)
   */
  @Override
  public void debug( final String format, final Object arg ) {
    LogKernel.append( Log.DEBUG_EVENTS, String.format( format, arg ), null );
  }




  /**
   * @see org.slf4j.Logger#debug(java.lang.String, java.lang.Object[])
   */
  @Override
  public void debug( final String format, final Object... arguments ) {
    LogKernel.append( Log.DEBUG_EVENTS, String.format( format, arguments ), null );
  }




  /**
   * @see org.slf4j.Logger#debug(java.lang.String, java.lang.Object, java.lang.Object)
   */
  @Override
  public void debug( final String format, final Object arg1, final Object arg2 ) {
    LogKernel.append( Log.DEBUG_EVENTS, String.format( format, arg1, arg2 ), null );
  }




  /**
   * @see org.slf4j.Logger#debug(java.lang.String, java.lang.Throwable)
   */
  @Override
  public void debug( final String msg, final Throwable t ) {
    LogKernel.append( Log.DEBUG_EVENTS, msg, t );
  }




  /**
   * @see org.slf4j.Logger#error(org.slf4j.Marker, java.lang.String)
   */
  @Override
  public void error( final Marker marker, final String msg ) {
    LogKernel.append( Log.ERROR_EVENTS, msg, null );
  }




  /**
   * @see org.slf4j.Logger#error(org.slf4j.Marker, java.lang.String, java.lang.Object)
   */
  @Override
  public void error( final Marker marker, final String format, final Object arg ) {
    LogKernel.append( Log.ERROR_EVENTS, String.format( format, arg ), null );
  }




  /**
   * @see org.slf4j.Logger#error(org.slf4j.Marker, java.lang.String, java.lang.Object[])
   */
  @Override
  public void error( final Marker marker, final String format, final Object... arguments ) {
    LogKernel.append( Log.ERROR_EVENTS, String.format( format, arguments ), null );
  }




  /**
   * @see org.slf4j.Logger#error(org.slf4j.Marker, java.lang.String, java.lang.Object, java.lang.Object)
   */
  @Override
  public void error( final Marker marker, final String format, final Object arg1, final Object arg2 ) {
    LogKernel.append( Log.ERROR_EVENTS, String.format( format, arg1, arg2 ), null );
  }




  /**
   * @see org.slf4j.Logger#error(org.slf4j.Marker, java.lang.String, java.lang.Throwable)
   */
  @Override
  public void error( final Marker marker, final String msg, final Throwable t ) {
    LogKernel.append( Log.ERROR_EVENTS, msg, t );
  }




  /**
   * @see org.slf4j.Logger#error(java.lang.String)
   */
  @Override
  public void error( final String msg ) {
    LogKernel.append( Log.ERROR_EVENTS, msg, null );
  }




  /**
   * @see org.slf4j.Logger#error(java.lang.String, java.lang.Object)
   */
  @Override
  public void error( final String format, final Object arg ) {
    LogKernel.append( Log.ERROR_EVENTS, String.format( format, arg ), null );
  }




  /**
   * @see org.slf4j.Logger#error(java.lang.String, java.lang.Object[])
   */
  @Override
  public void error( final String format, final Object... arguments ) {
    LogKernel.append( Log.ERROR_EVENTS, String.format( format, arguments ), null );
  }




  /**
   * @see org.slf4j.Logger#error(java.lang.String, java.lang.Object, java.lang.Object)
   */
  @Override
  public void error( final String format, final Object arg1, final Object arg2 ) {
    LogKernel.append( Log.ERROR_EVENTS, String.format( format, arg1, arg2 ), null );
  }




  /**
   * @see org.slf4j.Logger#error(java.lang.String, java.lang.Throwable)
   */
  @Override
  public void error( final String msg, final Throwable t ) {
    LogKernel.append( Log.ERROR_EVENTS, msg, t );
  }




  /**
   * @see org.slf4j.Logger#getName()
   */
  @Override
  public String getName() {
    return loggerName;
  }




  /**
   * @see org.slf4j.Logger#info(org.slf4j.Marker, java.lang.String)
   */
  @Override
  public void info( final Marker marker, final String msg ) {
    LogKernel.append( Log.INFO_EVENTS, msg, null );
  }




  /**
   * @see org.slf4j.Logger#info(org.slf4j.Marker, java.lang.String, java.lang.Object)
   */
  @Override
  public void info( final Marker marker, final String format, final Object arg ) {
    LogKernel.append( Log.INFO_EVENTS, String.format( format, arg ), null );
  }




  /**
   * @see org.slf4j.Logger#info(org.slf4j.Marker, java.lang.String, java.lang.Object[])
   */
  @Override
  public void info( final Marker marker, final String format, final Object... arguments ) {
    LogKernel.append( Log.INFO_EVENTS, String.format( format, arguments ), null );
  }




  /**
   * @see org.slf4j.Logger#info(org.slf4j.Marker, java.lang.String, java.lang.Object, java.lang.Object)
   */
  @Override
  public void info( final Marker marker, final String format, final Object arg1, final Object arg2 ) {
    LogKernel.append( Log.INFO_EVENTS, String.format( format, arg1, arg2 ), null );
  }




  /**
   * @see org.slf4j.Logger#info(org.slf4j.Marker, java.lang.String, java.lang.Throwable)
   */
  @Override
  public void info( final Marker marker, final String msg, final Throwable t ) {
    LogKernel.append( Log.INFO_EVENTS, msg, t );
  }




  /**
   * @see org.slf4j.Logger#info(java.lang.String)
   */
  @Override
  public void info( final String msg ) {
    LogKernel.append( Log.INFO_EVENTS, msg, null );
  }




  /**
   * @see org.slf4j.Logger#info(java.lang.String, java.lang.Object)
   */
  @Override
  public void info( final String format, final Object arg ) {
    LogKernel.append( Log.INFO_EVENTS, String.format( format, arg ), null );
  }




  /**
   * @see org.slf4j.Logger#info(java.lang.String, java.lang.Object[])
   */
  @Override
  public void info( final String format, final Object... arguments ) {
    LogKernel.append( Log.INFO_EVENTS, String.format( format, arguments ), null );
  }




  /**
   * @see org.slf4j.Logger#info(java.lang.String, java.lang.Object, java.lang.Object)
   */
  @Override
  public void info( final String format, final Object arg1, final Object arg2 ) {
    LogKernel.append( Log.INFO_EVENTS, String.format( format, arg1, arg2 ), null );
  }




  /**
   * @see org.slf4j.Logger#info(java.lang.String, java.lang.Throwable)
   */
  @Override
  public void info( final String msg, final Throwable t ) {
    LogKernel.append( Log.INFO_EVENTS, msg, t );
  }




  /**
   * @see org.slf4j.Logger#isDebugEnabled()
   */
  @Override
  public boolean isDebugEnabled() {
    return LogKernel.isLogging( Log.DEBUG );
  }




  /**
   * @see org.slf4j.Logger#isDebugEnabled(org.slf4j.Marker)
   */
  @Override
  public boolean isDebugEnabled( final Marker marker ) {
    return LogKernel.isLogging( Log.DEBUG );
  }




  /**
   * @see org.slf4j.Logger#isErrorEnabled()
   */
  @Override
  public boolean isErrorEnabled() {
    return LogKernel.isLogging( Log.ERROR );
  }




  /**
   * @see org.slf4j.Logger#isErrorEnabled(org.slf4j.Marker)
   */
  @Override
  public boolean isErrorEnabled( final Marker marker ) {
    return LogKernel.isLogging( Log.ERROR );
  }




  /**
   * @see org.slf4j.Logger#isInfoEnabled()
   */
  @Override
  public boolean isInfoEnabled() {
    return LogKernel.isLogging( Log.INFO );
  }




  /**
   * @see org.slf4j.Logger#isInfoEnabled(org.slf4j.Marker)
   */
  @Override
  public boolean isInfoEnabled( final Marker marker ) {
    return LogKernel.isLogging( Log.INFO );
  }




  /**
   * @see org.slf4j.Logger#isTraceEnabled()
   */
  @Override
  public boolean isTraceEnabled() {
    return LogKernel.isLogging( Log.TRACE );
  }




  /**
   * @see org.slf4j.Logger#isTraceEnabled(org.slf4j.Marker)
   */
  @Override
  public boolean isTraceEnabled( final Marker marker ) {
    return LogKernel.isLogging( Log.TRACE );
  }




  /**
   * @see org.slf4j.Logger#isWarnEnabled()
   */
  @Override
  public boolean isWarnEnabled() {
    return LogKernel.isLogging( Log.WARN );
  }




  /**
   * @see org.slf4j.Logger#isWarnEnabled(org.slf4j.Marker)
   */
  @Override
  public boolean isWarnEnabled( final Marker marker ) {
    return LogKernel.isLogging( Log.WARN );
  }




  /**
   * @see org.slf4j.Logger#trace(org.slf4j.Marker, java.lang.String)
   */
  @Override
  public void trace( final Marker marker, final String msg ) {
    LogKernel.append( Log.TRACE_EVENTS, msg, null );
  }




  /**
   * @see org.slf4j.Logger#trace(org.slf4j.Marker, java.lang.String, java.lang.Object)
   */
  @Override
  public void trace( final Marker marker, final String format, final Object arg ) {
    LogKernel.append( Log.TRACE_EVENTS, String.format( format, arg ), null );
  }




  /**
   * @see org.slf4j.Logger#trace(org.slf4j.Marker, java.lang.String, java.lang.Object[])
   */
  @Override
  public void trace( final Marker marker, final String format, final Object... arguments ) {
    LogKernel.append( Log.TRACE_EVENTS, String.format( format, arguments ), null );
  }




  /**
   * @see org.slf4j.Logger#trace(org.slf4j.Marker, java.lang.String, java.lang.Object, java.lang.Object)
   */
  @Override
  public void trace( final Marker marker, final String format, final Object arg1, final Object arg2 ) {
    LogKernel.append( Log.TRACE_EVENTS, String.format( format, arg1, arg2 ), null );
  }




  /**
   * @see org.slf4j.Logger#trace(org.slf4j.Marker, java.lang.String, java.lang.Throwable)
   */
  @Override
  public void trace( final Marker marker, final String msg, final Throwable t ) {
    LogKernel.append( Log.TRACE_EVENTS, msg, t );
  }




  /**
   * @see org.slf4j.Logger#trace(java.lang.String)
   */
  @Override
  public void trace( final String msg ) {
    LogKernel.append( Log.TRACE_EVENTS, msg );
  }




  /**
   * @see org.slf4j.Logger#trace(java.lang.String, java.lang.Object)
   */
  @Override
  public void trace( final String format, final Object arg ) {
    LogKernel.append( Log.TRACE_EVENTS, String.format( format, arg ), null );
  }




  /**
   * @see org.slf4j.Logger#trace(java.lang.String, java.lang.Object[])
   */
  @Override
  public void trace( final String format, final Object... arguments ) {
    LogKernel.append( Log.TRACE_EVENTS, String.format( format, arguments ), null );
  }




  /**
   * @see org.slf4j.Logger#trace(java.lang.String, java.lang.Object, java.lang.Object)
   */
  @Override
  public void trace( final String format, final Object arg1, final Object arg2 ) {
    LogKernel.append( Log.TRACE_EVENTS, String.format( format, arg1, arg2 ), null );
  }




  /**
   * @see org.slf4j.Logger#trace(java.lang.String, java.lang.Throwable)
   */
  @Override
  public void trace( final String msg, final Throwable t ) {
    LogKernel.append( Log.TRACE_EVENTS, msg, t );
  }




  /**
   * @see org.slf4j.Logger#warn(org.slf4j.Marker, java.lang.String)
   */
  @Override
  public void warn( final Marker marker, final String msg ) {
    LogKernel.append( Log.WARN_EVENTS, msg, null );
  }




  /**
   * @see org.slf4j.Logger#warn(org.slf4j.Marker, java.lang.String, java.lang.Object)
   */
  @Override
  public void warn( final Marker marker, final String format, final Object arg ) {
    LogKernel.append( Log.WARN_EVENTS, String.format( format, arg ), null );
  }




  /**
   * @see org.slf4j.Logger#warn(org.slf4j.Marker, java.lang.String, java.lang.Object[])
   */
  @Override
  public void warn( final Marker marker, final String format, final Object... arguments ) {
    LogKernel.append( Log.WARN_EVENTS, String.format( format, arguments ), null );
  }




  /**
   * @see org.slf4j.Logger#warn(org.slf4j.Marker, java.lang.String, java.lang.Object, java.lang.Object)
   */
  @Override
  public void warn( final Marker marker, final String format, final Object arg1, final Object arg2 ) {
    LogKernel.append( Log.WARN_EVENTS, String.format( format, arg1, arg2 ), null );
  }




  /**
   * @see org.slf4j.Logger#warn(org.slf4j.Marker, java.lang.String, java.lang.Throwable)
   */
  @Override
  public void warn( final Marker marker, final String msg, final Throwable t ) {
    LogKernel.append( Log.WARN_EVENTS, msg, t );
  }




  /**
   * @see org.slf4j.Logger#warn(java.lang.String)
   */
  @Override
  public void warn( final String msg ) {
    LogKernel.append( Log.WARN_EVENTS, msg, null );
  }




  /**
   * @see org.slf4j.Logger#warn(java.lang.String, java.lang.Object)
   */
  @Override
  public void warn( final String format, final Object arg ) {
    LogKernel.append( Log.WARN_EVENTS, String.format( format, arg ), null );
  }




  /**
   * @see org.slf4j.Logger#warn(java.lang.String, java.lang.Object[])
   */
  @Override
  public void warn( final String format, final Object... arguments ) {
    LogKernel.append( Log.WARN_EVENTS, String.format( format, arguments ), null );
  }




  /**
   * @see org.slf4j.Logger#warn(java.lang.String, java.lang.Object, java.lang.Object)
   */
  @Override
  public void warn( final String format, final Object arg1, final Object arg2 ) {
    LogKernel.append( Log.WARN_EVENTS, String.format( format, arg1, arg2 ), null );
  }




  /**
   * @see org.slf4j.Logger#warn(java.lang.String, java.lang.Throwable)
   */
  @Override
  public void warn( final String msg, final Throwable t ) {
    LogKernel.append( Log.WARN_EVENTS, msg, t );
  }

}
