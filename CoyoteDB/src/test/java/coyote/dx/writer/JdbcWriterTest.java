/*
 * Copyright (c) 2017 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.dx.writer;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import coyote.TestingLoader;
import coyote.dataframe.DataFrame;
import coyote.dataframe.marshal.JSONMarshaler;
import coyote.dx.ConfigTag;
import coyote.dx.TransformEngine;
import coyote.dx.context.TransformContext;
import coyote.loader.cfg.Config;
import coyote.loader.cfg.ConfigurationException;
import coyote.loader.log.ConsoleAppender;
import coyote.loader.log.Log;


/**
 * 
 */
public class JdbcWriterTest {
  private static final String CATALOG = "writertest";
  //private static final String JDBC_DRIVER = "org.h2.Driver";
  private static final String JDBC_SOURCE = "org.h2.jdbcx.JdbcDataSource";
  private static final String DB_URL = "jdbc:h2:./" + CATALOG;
  //private static final String LIBRARY_LOC = "jar:file:.src/resources/demojars/h2-1.4.196.jar!/";
  private static final String USER = "username";
  private static final String PASS = "password";
  private static final String SCHEMA = "test";
  private static final String TABLE = "testdata";




  /**
   * @throws java.lang.Exception
   */
  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    Log.addLogger(Log.DEFAULT_LOGGER_NAME, new ConsoleAppender(Log.TRACE_EVENTS | Log.DEBUG_EVENTS | Log.INFO_EVENTS | Log.WARN_EVENTS | Log.ERROR_EVENTS | Log.FATAL_EVENTS));
  }




  /**
   * @throws Exception
   */
  @AfterClass
  public static void tearDownAfterClass() throws Exception {
    File dbfile = new File(CATALOG + ".mv.db");
    Log.debug(dbfile.getAbsolutePath());
    dbfile.delete();
    // delete the trace file if it exists - it's text file showing what commands were processed
    dbfile = new File(CATALOG + ".trace.db");
    if (dbfile.exists()) {
      Log.debug(dbfile.getAbsolutePath());
      dbfile.delete();
    }
  }




  @Test
  public void basic() throws ConfigurationException {
    // Create a Job
    DataFrame jobFrame = new DataFrame().set(ConfigTag.NAME, "test") //
        .set(ConfigTag.READER, // 
            new DataFrame().set(ConfigTag.CLASS, "StaticReader") // 
                .set(ConfigTag.FIELDS,
                    new DataFrame() //
                        .set("JobId", "EB00C166-9972-4147-9453-735E7EB15C60") //
                        .set("Delay", 1000) //
                        .set("Log", true) //
            ) //
        ).set(ConfigTag.WRITER, new DataFrame().set(ConfigTag.CLASS, "JdbcWriter") // 
            .set(ConfigTag.TARGET, DB_URL) //
            .set(ConfigTag.DRIVER, JDBC_SOURCE) //
            .set(ConfigTag.USERNAME, USER) //
            .set(ConfigTag.PASSWORD, PASS) //
            .set(ConfigTag.SCHEMA, SCHEMA) //
            .set(ConfigTag.TABLE, TABLE) //
            .set(ConfigTag.AUTO_CREATE, true) //
    );

    Config configuration = new Config();
    configuration.add(ConfigTag.JOB, jobFrame);

    Log.info(JSONMarshaler.toFormattedString(configuration));

    TestingLoader loader = new TestingLoader();
    loader.configure(configuration);
    loader.start(); // run the job

    // Check for results
    TransformEngine engine = loader.getEngine();
    TransformContext context = engine.getContext();
    assertNotNull(context);
    assertFalse(context.isInError());
  }




  @Test
  public void fixture() throws ConfigurationException {
    String fixtureName = "Default";
    // Create a Job
    DataFrame jobFrame = new DataFrame().set(ConfigTag.NAME, "test") //
        .set(ConfigTag.READER, // 
            new DataFrame().set(ConfigTag.CLASS, "StaticReader") // 
                .set(ConfigTag.FIELDS,
                    new DataFrame() //
                        .set("JobId", "EB00C166-9972-4147-9453-735E7EB15C60") //
                        .set("Delay", 1000) //
                        .set("Log", true) //
            ) //
        ).set(ConfigTag.WRITER, new DataFrame().set(ConfigTag.CLASS, "JdbcWriter") // 
            .set(ConfigTag.TARGET, fixtureName) //
            .set(ConfigTag.SCHEMA, SCHEMA) //
            .set(ConfigTag.TABLE, TABLE) //
            .set(ConfigTag.AUTO_CREATE, true) //
        ).set(ConfigTag.TASK, // create a task section
            new DataFrame().set("DatabaseFixture", // add a task, DatabaseFixture
                new DataFrame().set(ConfigTag.CLASS, "coyote.dx.db.DefaultDatabaseFixture") // static fixture
                    .set(ConfigTag.NAME, fixtureName) // name of the fixture for look-up
                    .set(ConfigTag.TARGET, DB_URL) //
                    .set(ConfigTag.DRIVER, JDBC_SOURCE) //
                    .set(ConfigTag.USERNAME, USER) //
                    .set(ConfigTag.PASSWORD, PASS) //
            ) //

    );

    Config configuration = new Config();
    configuration.add(ConfigTag.JOB, jobFrame);

    Log.info(JSONMarshaler.toFormattedString(configuration));

    TestingLoader loader = new TestingLoader();
    loader.configure(configuration);
    loader.start(); // run the job

    // Check for results
    TransformEngine engine = loader.getEngine();
    TransformContext context = engine.getContext();
    assertNotNull(context);
    assertFalse(context.isInError());
    Object obj = engine.getContext().get(fixtureName);
    assertNotNull(obj);
    assertTrue(obj instanceof coyote.dx.db.DatabaseFixture);
    coyote.dx.db.DatabaseFixture fixture = (coyote.dx.db.DatabaseFixture)obj;
    assertFalse(fixture.isPooled());
  }

}
