/*
 * Copyright (c) 2017 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.dx.context;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import coyote.commons.Decimal;
import coyote.commons.FileUtil;
import coyote.commons.template.SymbolTable;
import coyote.dataframe.DataFrame;
import coyote.dataframe.DataFrameException;
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
    // Log.addLogger(Log.DEFAULT_LOGGER_NAME, new ConsoleAppender(Log.TRACE_EVENTS | Log.DEBUG_EVENTS | Log.INFO_EVENTS | Log.WARN_EVENTS | Log.ERROR_EVENTS | Log.FATAL_EVENTS));
    Log.addLogger(Log.DEFAULT_LOGGER_NAME, new ConsoleAppender(Log.INFO_EVENTS | Log.WARN_EVENTS | Log.ERROR_EVENTS | Log.FATAL_EVENTS));
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

    SymbolTable symbols = engine.getSymbolTable();
    obj = symbols.get(Symbols.CURRENT_RUN_MILLIS);
    assertTrue(obj instanceof Long);
    long millis = (Long)obj;

    obj = symbols.get(Symbols.CURRENT_RUN_SECONDS);
    assertTrue(obj instanceof Long);
    long seconds = (Long)obj;
    assertTrue(seconds == (millis / 1000));

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




  @Test
  public void storeState() throws DataFrameException {
    String jobName = "ContextStateTest";

    DataFrame config = new DataFrame().set("fields", new DataFrame().set("SomeKey", "SomeValue").set("AnotherKey", "AnotherValue"));

    TransformEngine engine = new DefaultTransformEngine();
    engine.setName(jobName);
    TransformContext context = new FileContext();
    context.setConfiguration(new Config(config));
    engine.setContext(context);

    // set some state data in the context to be persisted between runs    
    DataFrame state = new DataFrame().set("Orders",
        new DataFrame() //
            .set(new DataFrame().set("Id", "123").set("Type", "BUY").set("SKU", "ABC").set("Amount", 0.0123D).set("Price", 0.05678D)) //
            .set(new DataFrame().set("Id", "456").set("Type", "BUY").set("SKU", "DEF").set("Amount", 0.0456D).set("Price", 0.02468D)) // 
            .set(new DataFrame().set("Id", "789").set("Type", "BUY").set("SKU", "GHI").set("Amount", 0.0789D).set("Price", 0.01357D)) //
    );
    context.set("State", state);

    turnOver(engine);

    Object object = engine.getContext().get("State");
    assertTrue(object.equals(state));

    // model the recreation of a brand-new engine
    engine = new DefaultTransformEngine();
    engine.setName(jobName);
    TransformContext newcontext = new FileContext();
    newcontext.setConfiguration(new Config(config));
    engine.setContext(newcontext);

    turnOver(engine);

    // confirm the state object was retrieved properly
    TransformContext nextcontext = engine.getContext();
    Object obj = nextcontext.get("State");
    assertNotNull(obj);
    assertTrue(obj instanceof DataFrame);
    assertFalse(state == obj); // should be two different data frames

    DataFrame orders = ((DataFrame)obj).getAsFrame("Orders");
    assertTrue(orders.getFieldCount() == 3);

    DataFrame order = orders.getAsFrame(2);
    assertNotNull(order);
    double price = order.getAsDouble("Price");
    Decimal orderPrice = Decimal.valueOf(price);
    assertTrue(orderPrice.equals(Decimal.valueOf(0.01357D)));
  }

}
