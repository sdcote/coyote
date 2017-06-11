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
package coyote.loader;

import org.junit.Test;

import coyote.loader.log.ConsoleAppender;
import coyote.loader.log.Log;


/**
 * 
 */
public class DefaultLoaderTest {

  @Test
  public void test() {

    // Add a logger that will send log messages to the console 
    Log.addLogger( "Loader", new ConsoleAppender( Log.INFO_EVENTS | Log.WARN_EVENTS | Log.ERROR_EVENTS | Log.FATAL_EVENTS ) );
    Log.startLogging( Log.DEBUG );
    //Log.startLogging( Log.TRACE );

  }

}
