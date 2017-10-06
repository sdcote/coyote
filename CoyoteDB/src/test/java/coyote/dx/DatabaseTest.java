/*
 * Copyright (c) 2017 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.dx;

import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import coyote.commons.jdbc.DatabaseUtil;
import coyote.dataframe.DataFrame;
import coyote.dataframe.marshal.JSONMarshaler;
import coyote.loader.cfg.Config;
import coyote.loader.cfg.ConfigurationException;
import coyote.loader.log.ConsoleAppender;
import coyote.loader.log.Log;


/**
 * 
 */
public class DatabaseTest {

  /**
   * @throws java.lang.Exception
   */
  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    Log.addLogger(Log.DEFAULT_LOGGER_NAME, new ConsoleAppender(Log.TRACE_EVENTS | Log.DEBUG_EVENTS | Log.INFO_EVENTS | Log.WARN_EVENTS | Log.ERROR_EVENTS | Log.FATAL_EVENTS));
  }




  /**
   * @throws java.lang.Exception
   */
  @AfterClass
  public static void tearDownAfterClass() throws Exception {
    File dbfile = new File("demodb.mv.db");
    System.out.println(dbfile.getAbsolutePath());
    dbfile.delete();

  }




  @Test
  public void basic() throws ConfigurationException, IOException {

    DataFrame config = new DataFrame() //
        .set(ConfigTag.DRIVER, "org.h2.Driver") //
        .set(ConfigTag.TARGET, "jdbc:h2:./demodb") //
        .set(ConfigTag.USERNAME, "sa") //
        .set(ConfigTag.PASSWORD, "");
    System.out.println(JSONMarshaler.toFormattedString(config));
    Config cfg = new Config(config);

    Database database = new Database();
    database.setConfiguration(cfg);

    Connection conn = database.getConnection();
    assertNotNull("Could not connect to the database", conn);
    System.out.println(DatabaseUtil.getProduct(conn));
    System.out.println(DatabaseUtil.getVersion(conn));
database.close();
  }




  @Test
  public void library() throws ConfigurationException, IOException {

    DataFrame config = new DataFrame() //
        .set(ConfigTag.LIBRARY, "jar:file:./src/resources/demojars/h2-1.4.196.jar!/") //
        .set(ConfigTag.DRIVER, "org.h2.Driver") //
        .set(ConfigTag.TARGET, "jdbc:h2:./demodb") //
        .set(ConfigTag.USERNAME, "sa") //
        .set(ConfigTag.PASSWORD, "");
    System.out.println(JSONMarshaler.toFormattedString(config));
    Config cfg = new Config(config);

    Database database = new Database();
    database.setConfiguration(cfg);

    Connection conn = database.getConnection();
    assertNotNull("Could not connect to the database", conn);
    database.close();
  }

}
