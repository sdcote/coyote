/*
 * Copyright (c) 2014 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 *
 * Contributors:
 *   Stephan D. Cote 
 *      - Initial concept and initial implementation
 */
package coyote.mbus;

import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import coyote.commons.ExceptionUtil;


/**
 * 
 */
public class DefaultLogAppender implements LogAppender {
  private final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat( "yyyyMMdd HHmmss.SSS" );
  private final StringBuffer logBuffer = new StringBuffer();

  // Logging print streams
  private PrintStream outStream = System.out;

  private volatile boolean enabled = false;




  /**
   * Constructor which allows multiple PrintStreams to be used by different 
   * appenders.
   * 
   * <p>One of these appenders can be created with System.out and another one 
   * for System.err allowing error logging to be separate from normal 
   * logging.</p>
   * 
   * @param stream The PrintStream to use for output. If null, System.out is used.
   */
  public DefaultLogAppender( PrintStream stream ) {
    outStream.flush();
    if ( stream != null )
      outStream = stream;
    else
      outStream = System.out;
  }




  /**
   * @return the enabled
   */
  public synchronized boolean isEnabled() {
    return enabled;
  }




  /**
   * @param enabled the enabled to set
   */
  public synchronized void setEnabled( boolean enabled ) {
    this.enabled = enabled;
  }




  /**
   * Create a message that is formatted and sent to the preset PrintStream.
   * 
   * @param msg The message to format and display
   */
  @Override
  public synchronized void append( final String msg ) {
    if ( enabled ) {
      outStream.println( formatLogMsg( msg ) );
      outStream.flush();
    }
  }




  /**
   * Format the message into a traditional format.
   * 
   * @param msg
   * @return
   */
  String formatLogMsg( final String msg ) {
    synchronized( logBuffer ) {
      logBuffer.delete( 0, logBuffer.length() );

      logBuffer.append( DATE_FORMATTER.format( new Date() ) );
      logBuffer.append( " [" );
      logBuffer.append( Thread.currentThread().getName() );
      logBuffer.append( "] at " );

      final StackTraceElement[] stack = new Exception().fillInStackTrace().getStackTrace();
      final StackTraceElement elem = stack[2];
      logBuffer.append( ExceptionUtil.getLocalJavaName( elem.getClassName() ) );
      logBuffer.append( "." );
      logBuffer.append( elem.getMethodName() );
      logBuffer.append( "():" );
      if ( elem.getLineNumber() < 0 ) {
        logBuffer.append( "Native Method" );
      } else {
        logBuffer.append( elem.getLineNumber() );
      }
      logBuffer.append( " - " );

      logBuffer.append( msg );

      return logBuffer.toString();
    }
  }

}
