/*
 * Copyright (c) 2017 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.dx.task;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;

import org.junit.BeforeClass;
import org.junit.Test;

import coyote.commons.template.SymbolTable;
import coyote.dx.ConfigTag;
import coyote.dx.TaskException;
import coyote.dx.context.TransformContext;
import coyote.loader.cfg.Config;
import coyote.loader.cfg.ConfigurationException;
import coyote.loader.log.ConsoleAppender;
import coyote.loader.log.Log;


/**
 * 
 */
public class SetSymbolTest {
  private static final TransformContext context = new TransformContext();




  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    Log.addLogger(Log.DEFAULT_LOGGER_NAME, new ConsoleAppender(Log.TRACE_EVENTS | Log.DEBUG_EVENTS | Log.INFO_EVENTS | Log.WARN_EVENTS | Log.ERROR_EVENTS | Log.FATAL_EVENTS));
    context.setSymbols(new SymbolTable());
  }




  @Test
  public void setValue() throws ConfigurationException, TaskException, IOException {
    // Test numeric
    Config cfg = new Config();
    cfg.put(ConfigTag.SYMBOL, "lucky");
    cfg.put(ConfigTag.VALUE, 7);
    //System.out.println(cfg);

    context.getSymbols().clear();
    try (SetSymbol task = new SetSymbol()) {
      task.setConfiguration(cfg);
      task.open(context);
      task.execute();
      String symbol = context.getSymbols().getString("lucky");
      assertNotNull(symbol);
      assertEquals("7", symbol);
    }

    // test string
    cfg = new Config();
    cfg.put(ConfigTag.SYMBOL, "interval");
    cfg.put(ConfigTag.VALUE, "300");
    //System.out.println(cfg);

    context.getSymbols().clear();
    try (SetSymbol task = new SetSymbol()) {
      task.setConfiguration(cfg);
      task.open(context);
      task.execute();
      String symbol = context.getSymbols().getString("interval");
      assertNotNull(symbol);
      assertEquals("300", symbol);
    }

    // test boolean
    cfg = new Config();
    cfg.put(ConfigTag.SYMBOL, "flag");
    cfg.put(ConfigTag.VALUE, true);
    //System.out.println(cfg);

    context.getSymbols().clear();
    try (SetSymbol task = new SetSymbol()) {
      task.setConfiguration(cfg);
      task.open(context);
      task.execute();
      String symbol = context.getSymbols().getString("flag");
      assertNotNull(symbol);
      assertEquals("true", symbol);
    }

  }




  @Test
  public void evaluateNumeric() throws ConfigurationException, TaskException, IOException {
    Config cfg = new Config();
    cfg.put(ConfigTag.SYMBOL, "lucky");
    cfg.put(ConfigTag.EVALUATE, "3+4");
    //System.out.println(cfg);

    context.getSymbols().clear();
    try (SetSymbol task = new SetSymbol()) {
      task.setConfiguration(cfg);
      task.open(context);
      task.execute();
      String symbol = context.getSymbols().getString("lucky");
      assertNotNull(symbol);
      assertEquals("7", symbol);
    }

  }




  @Test
  public void evaluateBoolean() throws ConfigurationException, TaskException, IOException {
    Config cfg = new Config();
    cfg.put(ConfigTag.SYMBOL, "flag");
    cfg.put(ConfigTag.EVALUATE, "true || false");
    //System.out.println(cfg);

    context.getSymbols().clear();
    try (SetSymbol task = new SetSymbol()) {
      task.setConfiguration(cfg);
      task.open(context);
      task.execute();
      String symbol = context.getSymbols().getString("flag");
      assertNotNull(symbol);
      assertEquals("true", symbol);
    }
  }




  @Test
  public void evaluateTemplate() throws ConfigurationException, TaskException, IOException {
    Config cfg = new Config();
    cfg.put(ConfigTag.SYMBOL, "start");
    cfg.put(ConfigTag.EVALUATE, "[#$CurrentRunEpochSeconds#] - (60 * 60 * 2)");
    //System.out.println(cfg);

    context.getSymbols().clear();
    context.getSymbols().put("CurrentRunEpochSeconds", 1511916611);
    try (SetSymbol task = new SetSymbol()) {
      task.setConfiguration(cfg);
      task.open(context);
      task.execute();
      String symbol = context.getSymbols().getString("start");
      assertNotNull(symbol);
      assertEquals("1511909411", symbol);
    }
  }




  @Test
  public void contextVariable() throws ConfigurationException, TaskException, IOException {
    Config cfg = new Config();
    cfg.put(ConfigTag.SYMBOL, "sku");
    cfg.put(ConfigTag.VALUE, "ProductSKU");
    // System.out.println(cfg);

    context.getSymbols().clear();
    context.set("ProductSKU", "1029384756");

    try (SetSymbol task = new SetSymbol()) {
      task.setConfiguration(cfg);
      task.open(context);
      task.execute();
      String symbol = context.getSymbols().getString("sku");
      assertNotNull(symbol);
      assertEquals("1029384756", symbol);
    }
  }

}
