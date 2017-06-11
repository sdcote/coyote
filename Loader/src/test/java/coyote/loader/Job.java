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

import coyote.loader.cfg.Config;
import coyote.loader.cfg.ConfigurationException;


/**
 * 
 */
public class Job extends AbstractLoader implements Loader {

  /**
   * @see coyote.loader.AbstractLoader#configure(coyote.loader.cfg.Config)
   */
  @Override
  public void configure( Config cfg ) throws ConfigurationException {
    // Always a good idea to call this first 
    super.configure( cfg );

    // Now do my own configuration 
  }




  /**
   * @see coyote.loader.AbstractLoader#start()
   */
  @Override
  public void start() {
    // Blocking call from the MAIN thread
  }




  /**
   * @see coyote.loader.thread.ThreadJob#shutdown()
   */
  @Override
  public void shutdown() {

    // Perform our shutdown activities first

    // Now perform the super-class termination
    super.shutdown();
  }

}
