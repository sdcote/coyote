/*
 * Copyright (c) 2017 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.dx.db;

import java.sql.Connection;

import coyote.loader.cfg.Config;
import coyote.loader.cfg.ConfigurationException;


/**
 * This is an abstraction for database connection components in the transform 
 * context.
 */
public interface DatabaseFixture {

  /**
   * Get a connection from the fixture.
   * 
   * @return a JDBC connection the component can use to communicate with a 
   *         database.
   */
  public Connection getConnection();




  /**
   * Determine if the fixture is managing connections or not.
   * 
   * <p>If the connections are pooled, it is safe to close the connection when 
   * the operation is complete. The fixture will intercept the {@code close()} 
   * call and manage the connection for later reuse.
   * 
   * <p>If the fixture is not pooling connections, the caller should either 
   * keep the connection for later reuse or close the connection and later 
   * retrieve another. Note: constantly creating new connections will 
   * drastically slow performance an place stress on the database server and 
   * network resources. If the connections are not pooled, it is better to 
   * keep the connection reference and only close the connection when the 
   * transformation ends or there is a problem with the existing connection.  
   * 
   * @return true if the fixture is managing connections, false if the caller 
   *         is responsible for the connection.
   */
  public boolean isPooled();




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
