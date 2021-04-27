/*
 * Copyright (c) 2017 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.dx.db;

import java.sql.Connection;


/**
 * Models an abstraction of a component which provides connections to a 
 * database.
 */
public interface DatabaseConnector {

  /**
   * Get a connection to the database.
   * 
   * @return a JDBC connection the component can use to communicate with a 
   *         database.
   */
  public Connection getConnection();




  /**
   * Determine if the connector is managing connections or not.
   * 
   * <p>If the connections are pooled, it is safe to close the connection when 
   * the operation is complete. The connector will intercept the 
   * {@code close()} call and manage the connection for later reuse.
   * 
   * <p>If the connector is not pooling connections, the caller should either 
   * keep the connection for later reuse or close the connection and later 
   * retrieve another. Note: constantly creating new connections will 
   * drastically slow performance an place stress on the database server and 
   * network resources. If the connections are not pooled, it is better to 
   * keep the connection reference and only close the connection when the 
   * transformation ends or there is a problem with the existing connection.  
   * 
   * @return true if the connector is managing connections, false if the caller 
   *         is responsible for the connection.
   */
  public boolean isPooled();




  /**
   * Return the name of the user the connector is useing to connect to the 
   * database.
   * 
   * <p>This is to support the JDBC drivers which do not support retrieving 
   * the user name from the meta data. Since the connector has this data to 
   * make connections, this method should always return the correct value.
   * 
   * @return the name of the user used to create connections to the database. 
   *         Should never return null.
   */
  public String getUserName();

}
