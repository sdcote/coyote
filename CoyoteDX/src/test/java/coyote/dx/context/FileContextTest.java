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

import coyote.commons.FileUtil;
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
public class FileContextTest extends AbstractContextTest {

  /**
   * @throws java.lang.Exception
   */
  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    Log.addLogger(Log.DEFAULT_LOGGER_NAME, new ConsoleAppender(Log.TRACE_EVENTS | Log.DEBUG_EVENTS | Log.INFO_EVENTS | Log.WARN_EVENTS | Log.ERROR_EVENTS | Log.FATAL_EVENTS));
    //Log.addLogger( Log.DEFAULT_LOGGER_NAME, new ConsoleAppender( Log.INFO_EVENTS | Log.WARN_EVENTS | Log.ERROR_EVENTS | Log.FATAL_EVENTS ) );
  }




  /**
   * @throws java.lang.Exception
   */
  @AfterClass
  public static void tearDownAfterClass() throws Exception {
    File file = new File("wrk");
    FileUtil.deleteDirectory(file);

  }




  @Test
  public void fileContext() {
    String jobName = "ContextTest";

    DataFrame config = new DataFrame().set("fields", new DataFrame().set("SomeKey", "SomeValue").set("AnotherKey", "AnotherValue"));

    TransformEngine engine = new DefaultTransformEngine();
    engine.setName(jobName);
    TransformContext context = new FileContext();
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
    context = new FileContext();
    context.setConfiguration(new Config(config));
    engine.setContext(context);

    turnOver(engine);

    obj = context.get(Symbols.RUN_COUNT);
    assertTrue(obj instanceof Long);
    long lastRunCount = (Long)obj;
    assertEquals(nextRunCount + 1, lastRunCount);
  }

}
