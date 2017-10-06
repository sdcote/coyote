/*
 * Copyright (c) 2017 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.sql.Connection;
import java.sql.DriverManager;

import org.junit.Test;


/**
 * 
 */
public class H2Driver {

  @Test
  public void h2OnClassPath() {
    try {
      Class.forName("org.h2.jdbcx.JdbcDataSource");
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
      fail("Where is your H2 JDBC Driver? It should be on the class path.");
    }
    Connection connection = null;
    try {
      connection = DriverManager.getConnection("jdbc:h2:./test;MODE=Oracle", "sa", "");
    } catch (Exception e) {
      e.printStackTrace();
      fail("Connection Failed! " + e.getClass().getName() + " - " + e.getMessage());
    }

    assertNotNull("Failed to make connection!", connection);
  }

}
