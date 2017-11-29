/*
 * Copyright (c) 2017 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.dx.task;

import static org.junit.Assert.assertNotNull;

import java.io.IOException;

import org.junit.BeforeClass;
import org.junit.Test;

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
  private final TransformContext context = new TransformContext();



  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    Log.addLogger(Log.DEFAULT_LOGGER_NAME, new ConsoleAppender(Log.TRACE_EVENTS | Log.DEBUG_EVENTS | Log.INFO_EVENTS | Log.WARN_EVENTS | Log.ERROR_EVENTS | Log.FATAL_EVENTS));
  }




  @Test
  public void setValue() throws ConfigurationException, TaskException, IOException {

    context.open();
    
    Config cfg = new Config();
    cfg.put(ConfigTag.SYMBOL, "lucky");
    cfg.put(ConfigTag.VALUE, 7);
    System.out.println(cfg);

    try (SetSymbol task = new SetSymbol()) {
      task.setConfiguration(cfg);
      task.open(context);
      task.execute();
      String symbol = context.getSymbols().getString("lucky");
      assertNotNull(symbol);
    }

  }

}
