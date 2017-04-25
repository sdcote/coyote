/*
 * Copyright (c) 2016 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 *
 * Contributors:
 *   Stephan D. Cote 
 *      - Initial concept and initial implementation
 */
package demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import coyote.loader.log.ConsoleAppender;
import coyote.loader.log.DefaultFormatter;
import coyote.loader.log.Log;


/**
 * 
 */
public class SimpleLog {

  // This is the normal way to obtain a logger
  private static final Logger LOG = LoggerFactory.getLogger( SimpleLog.class );




  /**
   * @param args
   */
  public static void main( String[] args ) {

    coyote.loader.log.Logger con = new ConsoleAppender( -1L );
    con.setFormatter( new DefaultFormatter() );
    Log.addLogger( Log.DEFAULT_LOGGER_NAME, con );

    LOG.trace( "This is a trace message" );
    LOG.debug( "This is a debug message" );
    LOG.info( "This is an infor message" );
    LOG.warn( "This is a warning message" );
    LOG.error( "This is an error message" );
    System.out.println( "==================================================" );
    Log.trace( "This is a trace message" );
    Log.debug( "This is a debug message" );
    Log.info( "This is an infor message" );
    Log.warn( "This is a warning message" );
    Log.error( "This is an error message" );
    Log.fatal( "This is a fatal message" );
    Log.notice( "This is a notice message" );

  }

}
