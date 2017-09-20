/*
 * Copyright (c) 2007 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.loader.log;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.StringTokenizer;

import coyote.commons.ExceptionUtil;
import coyote.commons.StringUtil;


/**
 * ConsoleAppender is an implementation of Logger that extends AbstractLogger 
 * and defines event() to write the event to a Writer.
 * 
 * <p>This appender uses a minimalistic formatter to keep the console tidy.</p>
 */
public class ConsoleAppender extends AbstractLogger {

  Writer log_writer;

  private static final String STDERR = "STDERR";




  /**
   * Constructor ConsoleAppender
   */
  public ConsoleAppender() {
    this( new OutputStreamWriter( System.out ), 0 );
  }




  /**
   * Construct a WriterLogger that writes to System.out with an initial mask
   * value.
   *
   * @param mask The initial mask value.
   */
  public ConsoleAppender( final long mask ) {
    this( new OutputStreamWriter( System.out ), mask );
  }




  /**
   * Construct a WriterLogger that writes to the specified writer with
   * an initial mask value of zero (i.e. does not log any events).
   *
   * @param writer The writer.
   */
  public ConsoleAppender( final Writer writer ) {
    this( writer, 0 );
  }




  /**
   * Construct a WriterLogger that writes to the specified writer with an
   * initial mask value.
   *
   * @param writer The writer.
   * @param mask The initial mask value.
   */
  public ConsoleAppender( final Writer writer, final long mask ) {
    super( mask );
    formatter = new ConsoleFormatter();
    log_writer = writer;
  }




  /**
   * If enabled, log an event of the specified category to the underlying
   * Writer.
   *
   * <p>In order to remain thread-safe, a new formatter is created each call.</p>
   *
   * @param category The category.
   * @param event The event.
   * @param cause The exception that caused the log entry. Can be null.
   */
  public void append( final String category, final Object event, final Throwable cause ) {
    try {
      log_writer.write( formatter.format( event, category, cause ) );
      log_writer.flush();
    } catch ( final IOException ioe ) {
      // normal during shutdown sequences - but what about other times?
      // maybe we should consider refactoring this
    } catch ( final Exception e ) {
      System.err.println( this.getClass().getName() + " formatting error: " + e + ":" + e.getMessage() + StringUtil.LINE_FEED + ExceptionUtil.stackTrace( e ) );
    }
  }




  /**
   * @return  the writer.
   */
  public Writer getWriter() {
    return log_writer;
  }




  /**
   * Initialize the logger.
   */
  public void initialize() {
    // we don't call super.initialize() because we don't need the file based initialization

    // Switch to STDERR depending on configuration!
    if ( config != null && config.getString( TARGET_TAG ) != null && STDERR.equalsIgnoreCase( config.getString( TARGET_TAG ) ) ) {
      log_writer = new OutputStreamWriter( System.err );
    }

    if ( config != null && config.getString( Logger.CATEGORY_TAG ) != null ) {
      for ( final StringTokenizer st = new StringTokenizer( config.getString( Logger.CATEGORY_TAG ), Logger.CATEGORY_DELIMS ); st.hasMoreTokens(); startLogging( st.nextToken() ) );
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
   * Set the writer.
   *
   * @param writer The new writer.
   */
  public void setWriter( final Writer writer ) {
    log_writer = writer;
  }




  /**
   * Terminates the logger.
   */
  public void terminate() {
    // System.out and System.err should not be closed
  }
}