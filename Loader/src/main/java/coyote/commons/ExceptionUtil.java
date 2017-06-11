/*
 * Copyright (c) 2014 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 *
 * Contributors:
 *   Stephan D. Cote 
 *      - Initial concept and implementation
 */
package coyote.commons;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;


/**
 * Exception utilities shared between classes.
 */
public class ExceptionUtil {

  /**
   * Return the stack trace for the given throwable as a string.
   * 
   * <p>This will dump the entire stacktrace of the root exception
   *
   * @param t The Throwable object whose stack trace we want to render as a 
   *        string
   *
   * @return the stack trace as a string
   */
  public static String stackTrace( final Throwable t ) {
    final ByteArrayOutputStream out = new ByteArrayOutputStream();
    getRootException( t ).printStackTrace( new PrintWriter( out, true ) );
    return out.toString();
  }




  /**
   * Return the root cause message.
   *
   * <p>Return the message of the inner-most exception that is wrapped by this
   * and any nested exceptions therein. This will delegate the event call to
   * the first exception in the chain and return its value.</p>
   *
   * @return The message of the core exception.
   */
  public static String getRootMessage( final Throwable t ) {
    return getRootException( t ).getMessage();
  }




  /**
   * Return the root cause exception.
   *
   * <p>Return the inner-most exception that is wrapped by this and any nested
   * exceptions therein. This will delegate the event call to the first
   * exception in the chain and return its reference.</p>
   *
   * @return The message of the core exception.
   */
  public static Throwable getRootException( final Throwable t ) {
    if ( t.getCause() != null ) {
      return getRootException( t.getCause() );
    } else {
      return t;
    }
  }




  /**
   * Dump the exception and its message as a String with the root class, method
   * and line number.
   *
   * @return String suitable for logging.
   */
  public static String toString( final Throwable t ) {
    final StringBuffer buffer = new StringBuffer();
    final Throwable root = getRootException( t );
    final StackTraceElement[] stack = root.getStackTrace();
    final StackTraceElement elem = stack[0];

    buffer.append( getLocalJavaName( t.getClass().getName() ) );
    buffer.append( ": '" );

    if ( t.getMessage() == null ) {
      buffer.append( "" );
    } else {
      buffer.append( t.getMessage() );
    }

    if ( t.getCause() != null ) {
      buffer.append( "' caused by " );
      buffer.append( getLocalJavaName( root.getClass().getName() ) );
      buffer.append( " exception thrown from " );
    } else {
      buffer.append( "' at " );
    }

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

    return buffer.toString();
  }




  public static String getLocalJavaName( final String classname ) {
    return tail( classname, '.' );
  }




  private static String tail( final String text, final char ch ) {
    final int indx = text.lastIndexOf( ch );
    return ( indx != -1 ) ? text.substring( indx + 1 ) : text;
  }




  public static String getAbbreviatedClassname( final String classname ) {
    if ( classname != null ) {
      String[] tokens = classname.split( "\\." );

      if ( tokens.length > 1 ) {
        StringBuffer b = new StringBuffer();
        for ( int x = 0; x < tokens.length; x++ ) {
          if ( tokens[x].length() > 0 ) {
            if ( x + 1 < tokens.length ) {
              b.append( tokens[x].charAt( 0 ) );
              b.append( '.' );
            } else {
              b.append( tail( classname, '.' ) );
              break;
            }
          }
        }
        return b.toString();

      } else {
        return classname;
      }
    } else {
      return "";
    }

  }

}
