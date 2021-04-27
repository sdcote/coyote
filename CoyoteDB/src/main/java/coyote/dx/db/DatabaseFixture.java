/*
 * Copyright (c) 2017 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.dx.db;

import coyote.loader.cfg.Config;
import coyote.loader.cfg.ConfigurationException;


/**
 * This is an abstraction for database connection components in the transform 
 * context.
 */
public interface DatabaseFixture extends DatabaseConnector {

  /**
   * This is how the fixture is configured.
   * 
   * <p>This should also initialize the fixture so it is ready for operation.
   * Calls to {@code getConnection()} should return connections.
   * 
   * @param cfg the configuration the fixture is to use
   * 
   * @throws ConfigurationException if there were problems with the configuration
   */
  public void setConfiguration(Config cfg) throws ConfigurationException;

}
