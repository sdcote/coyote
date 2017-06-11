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

import java.text.SimpleDateFormat;
import java.util.Date;

import coyote.commons.ExceptionUtil;
import coyote.commons.StringUtil;


/**
 * Class DefaultFormatter
 */
public class DefaultFormatter implements Formatter {
  private static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss.SSS zzz" );
  private volatile long lastevent = 0;
  private static int stackDepth = 6;




  /**
   *
   */
  public DefaultFormatter() {
    super();
  }




  /**
   * @return the number of stack calls to skip to find where messages originate.
   */
  public static int getStackDepth() {
    return stackDepth;
  }




  /**
   * @param depth the number of stack calls to skip to find where messages originate.
   */
  public static void setStackDepth( int depth ) {
    DefaultFormatter.stackDepth = depth;
  }




  /**
   * Format the event into a string to be placed in the log.
   *
   * @param event the thing to be logged
   * @param category the type of log entry it is
   * @param cause The exception that caused the log entry. Can be null.
   *
   * @return a string representing the line to place in the log
   */
  public String format( final Object event, final String category, final Throwable cause ) {
    final long now = System.currentTimeMillis();

    if ( lastevent == 0 ) {
      lastevent = now;
    }

    final StringBuffer buffer = new StringBuffer();

    buffer.append( DefaultFormatter.DATE_FORMATTER.format( new Date( now ) ) );
    buffer.append( " | " );
    buffer.append( Thread.currentThread().getName() );
    buffer.append( " | " );
    buffer.append( category );
    buffer.append( " | " );
    buffer.append( Log.getInterval() );
    buffer.append( ":" );
    buffer.append( ( now - lastevent ) );
    buffer.append( " | " );

    // The trace category get additional location information
    if ( Log.TRACE.equals( category ) || Log.DEBUG.equals( category ) ) {
      final StackTraceElement[] stack = new Exception().fillInStackTrace().getStackTrace();

      final StackTraceElement elem = stack[( stack.length <= stackDepth ) ? stack.length - 1 : stackDepth];

      buffer.append( ExceptionUtil.getAbbreviatedClassname( elem.getClassName() ) );
      buffer.append( "." );
      buffer.append( elem.getMethodName() );
      buffer.append( "():" );

      if ( elem.getLineNumber() < 0 ) {
        buffer.append( "Native Method" );
      } else {
        buffer.append( elem.getLineNumber() );
      }

      buffer.append( " | " );
    }

    buffer.append( event.toString() );

    Throwable ball = cause;
    if ( ( event != null ) && ( cause == null ) && ( event instanceof Throwable ) ) {
      ball = (Throwable)event;
    }

    if ( ball != null ) {
      buffer.append( StringUtil.LINE_FEED );

      final StackTraceElement[] stack = ( ball ).getStackTrace();
      StackTraceElement elem = stack[( stack.length - 1 )];

      buffer.append( event.getClass().getName() );
      buffer.append( " at " );

      // --
      buffer.append( elem.getClassName() );
      buffer.append( "." );
      buffer.append( elem.getMethodName() );
      buffer.append( "(" );

      if ( elem.getLineNumber() < 0 ) {
        buffer.append( "Native Method" );
      } else {
        buffer.append( elem.getFileName() );
        buffer.append( ":" );
        buffer.append( elem.getLineNumber() );
      }

      buffer.append( ") message=[" );
      buffer.append( ( ball ).getMessage() );
      buffer.append( "]" );

      // --

      buffer.append( " - root cause: " );

      elem = stack[0];

      // -- yes, duplicate code, but inline is still faster --
      buffer.append( elem.getClassName() );
      buffer.append( "." );
      buffer.append( elem.getMethodName() );
      buffer.append( "(" );

      if ( elem.getLineNumber() < 0 ) {
        buffer.append( "Native Method" );
      } else {
        buffer.append( elem.getFileName() );
        buffer.append( ":" );
        buffer.append( elem.getLineNumber() );
      }

      buffer.append( ")" );
      // --

    }

    buffer.append( StringUtil.LINE_FEED );

    lastevent = now;

    return buffer.toString();
  }




  /**
   * @return any header data to be sent to the appender
   */
  public byte[] initialize() {
    return null;
  }




  /**
   * @return any footer data to be sent to the appender
   */
  public byte[] terminate() {
    return null;
  }

}