/*
 * Copyright (c) 2017 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.dx;

import java.sql.Connection;


/**
 * Defines a common interface components use to interact with datastores.
 * 
 * <p>It is expected that there will be multiple datastore implementations and
 * this interface enables interoperability between them. For example, the 
 * default datastore will probably only create JDBC connections and require the 
 * caller to maintain the lifecycle of the connection while a different
 * implementation may offer connection pooling. Either of these may be placed 
 * in the configuration and specified used at runtime.
 */
public interface DataStore {

  /**
   * Get a connection to the configured datastore.
   * 
   * @return a JDBC connection to the database represented by the datastore
   */
  public Connection getConnection();

}
