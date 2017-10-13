/*
 * Copyright (c) 2017 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.dx.context;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import coyote.dataframe.DataFrame;
import coyote.dx.DefaultTransformEngine;
import coyote.dx.Symbols;
import coyote.dx.TransformEngine;
import coyote.loader.cfg.Config;
import coyote.loader.log.ConsoleAppender;
import coyote.loader.log.Log;


/**
 * 
 */
public class DatabaseContextTest extends AbstractContextTest {
  private static final String CATALOG = "context";
  private static final String JDBC_DRIVER = "org.h2.Driver";
  private static final String DB_URL = "jdbc:h2:./"+CATALOG;
  private static final String LIBRARY_LOC = "jar:file:.src/resources/demojars/h2-1.4.196.jar!/";
  private static final String USER = "username";
  private static final String PASS = "password";
  

  /**
   * @throws java.lang.Exception
   */
  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    Log.addLogger(Log.DEFAULT_LOGGER_NAME, new ConsoleAppender(Log.TRACE_EVENTS | Log.DEBUG_EVENTS | Log.INFO_EVENTS | Log.WARN_EVENTS | Log.ERROR_EVENTS | Log.FATAL_EVENTS));
    //Log.addLogger( Log.DEFAULT_LOGGER_NAME, new ConsoleAppender( Log.INFO_EVENTS | Log.WARN_EVENTS | Log.ERROR_EVENTS | Log.FATAL_EVENTS ) );
  }




  /**
   * @throws Exception
   */
  @AfterClass
  public static void tearDownAfterClass() throws Exception {
    File dbfile = new File(CATALOG+".mv.db");
    Log.debug(dbfile.getAbsolutePath());
    dbfile.delete();
    dbfile = new File(CATALOG+".trace.db");
    if (dbfile.exists()) {
      Log.debug(dbfile.getAbsolutePath());
      dbfile.delete();
    }
  }




  @Test
  public void contextWithLibraryAttribute() {
    String jobName = "ContextTest";

    DataFrame config = new DataFrame() //
        .set("class", "DatabaseContext") //
        .set("target", DB_URL) //
        .set("autocreate", true) //
        .set("library", LIBRARY_LOC) //
        .set("driver", JDBC_DRIVER) //
        .set("username", USER) //
        .set("password", PASS) //
        .set("fields",
            new DataFrame() //
                .set("SomeKey", "SomeValue") //
                .set("AnotherKey", "AnotherValue") //
    );

    TransformEngine engine = new DefaultTransformEngine();
    engine.setName(jobName);
    TransformContext context = new DatabaseContext();
    context.setConfiguration(new Config(config));
    engine.setContext(context);

    turnOver(engine);

    Object obj = context.get(Symbols.RUN_COUNT);
    assertTrue(obj instanceof Long);
    long runcount = (Long)obj;
    assertTrue(runcount > 0);

    turnOver(engine);

    obj = context.get(Symbols.RUN_COUNT);
    assertTrue(obj instanceof Long);
    long nextRunCount = (Long)obj;
    assertEquals(runcount + 1, nextRunCount);

    // Replace the context with a new one to test reading from database
    context = new DatabaseContext();
    context.setConfiguration(new Config(config));
    engine.setContext(context);

    turnOver(engine);

    obj = context.get(Symbols.RUN_COUNT);
    assertTrue(obj instanceof Long);
    long lastRunCount = (Long)obj;
    assertEquals(nextRunCount + 1, lastRunCount);
    
    context.close();
  }




  @Test
  public void msqltests() {
    String jobName = "ContextTest";

    DataFrame config = new DataFrame() //
        .set("class", "DatabaseContext") //
        .set("target", "jdbc:sqlserver://coyote.database.windows.net:1433;database=coyotedx") //
        .set("autocreate", true) //
        .set("library", "jar:file:src/resources/demojars/sqljdbc42.jar!/") //
        .set("driver", "com.microsoft.sqlserver.jdbc.SQLServerDriver") //
        .set("identity", "818E9553-4525-582D-AAD1-3DCAABDA98F918E955") //
        .set("ENC:username", "Z3d0v5lmgvPZRCsUdG/B4FsyrmPUM1WsVrQY8szJIetIJE3TBbjmBQ==") //
        .set("ENC:password", "k0Vl7ZgH3Fb0xaR3tlZcWkQKlyFNmIGISCRN0wW45gU=") //
        .set("fields",
            new DataFrame() //
                .set("SomeKey", "SomeValue") //
                .set("AnotherKey", "AnotherValue") //
    );

    TransformEngine engine = new DefaultTransformEngine();
    engine.setName(jobName);
    TransformContext context = new DatabaseContext();
    context.setConfiguration(new Config(config));
    engine.setContext(context);

    turnOver(engine);

    Object obj = context.get(Symbols.RUN_COUNT);
    assertTrue(obj instanceof Long);
    long runcount = (Long)obj;
    assertTrue(runcount > 0);

    turnOver(engine);

    obj = context.get(Symbols.RUN_COUNT);
    assertTrue(obj instanceof Long);
    long nextRunCount = (Long)obj;
    assertEquals(runcount + 1, nextRunCount);

    // Replace the context with a new one to test reading from database
    context = new DatabaseContext();
    context.setConfiguration(new Config(config));
    engine.setContext(context);

    turnOver(engine);

    obj = context.get(Symbols.RUN_COUNT);
    assertTrue(obj instanceof Long);
    long lastRunCount = (Long)obj;
    assertEquals(nextRunCount + 1, lastRunCount);
  }

}
