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
package coyote.ui;

import coyote.loader.cfg.Config;
import coyote.loader.cfg.ConfigurationException;
import coyote.loader.log.ConsoleAppender;
import coyote.loader.log.Log;


/**
 * This creates a Mock Service object and configures it which starts this HTTP 
 * Manager.
 */
public class Server {

  /**
   * @param args
   */
  public static void main( String[] args ) {
    // Add a logger that will send log messages to the console 
    Log.addLogger( Log.DEFAULT_LOGGER_NAME, new ConsoleAppender( -1L ) );

    Config cfg = new Config();
    MockService service = new MockService();

    try {
      service.configure( cfg );
    } catch ( ConfigurationException e ) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

  }

}
