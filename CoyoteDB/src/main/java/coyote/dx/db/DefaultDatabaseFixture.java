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
 * 
 */
public class DefaultDatabaseFixture implements DatabaseFixture {

  private Database database = new Database();




  /**
   * @see coyote.dx.db.DatabaseFixture#getConnection()
   */
  @Override
  public Connection getConnection() {
    return database.getConnection();
  }




  /**
   * @see coyote.dx.db.DatabaseFixture#isPooled()
   */
  @Override
  public boolean isPooled() {
    return false;
  }




  /**
   * @throws ConfigurationException 
   * @see coyote.dx.db.DatabaseFixture#setConfiguration(coyote.loader.cfg.Config)
   */
  @Override
  public void setConfiguration(Config cfg) throws ConfigurationException {
    database = new Database();
    database.setConfiguration(cfg);
  }




  /**
   * @see coyote.dx.db.DatabaseConnector#getUserName()
   */
  @Override
  public String getUserName() {
    return database.getUserName();
  }

}
