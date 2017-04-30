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
package coyote.commons.jdbc;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Properties;
import java.util.logging.Logger;


/**
 * The delegate class which allows the class loader to find and handle 
 * dynamically loaded JDBC drivers.
 */
public class DriverDelegate implements Driver {
  private Driver driver;




  public DriverDelegate( Driver d ) {
    this.driver = d;
  }




  public boolean acceptsURL( String u ) throws SQLException {
    return this.driver.acceptsURL( u );
  }




  public Connection connect( String u, Properties p ) throws SQLException {
    return this.driver.connect( u, p );
  }




  public int getMajorVersion() {
    return this.driver.getMajorVersion();
  }




  public int getMinorVersion() {
    return this.driver.getMinorVersion();
  }




  public DriverPropertyInfo[] getPropertyInfo( String u, Properties p ) throws SQLException {
    return this.driver.getPropertyInfo( u, p );
  }




  public boolean jdbcCompliant() {
    return this.driver.jdbcCompliant();
  }




  public Logger getParentLogger() throws SQLFeatureNotSupportedException {
    return this.driver.getParentLogger();
  }
}