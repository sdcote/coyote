/*
 * Copyright (c) 2017 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.dx.listener;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import coyote.dataframe.DataFrame;
import coyote.dx.AbstractTest;
import coyote.dx.ConfigTag;
import coyote.dx.context.TransactionContext;
import coyote.loader.cfg.Config;
import coyote.loader.log.ConsoleAppender;
import coyote.loader.log.Log;


/**
 * 
 */
public class TotalTest extends AbstractTest {

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    Log.addLogger(Log.DEFAULT_LOGGER_NAME, new ConsoleAppender(Log.TRACE_EVENTS | Log.DEBUG_EVENTS | Log.INFO_EVENTS | Log.WARN_EVENTS | Log.ERROR_EVENTS | Log.FATAL_EVENTS));
  }




  @Test
  public void basic() {

    List<DataFrame> frames = new ArrayList<>();
    frames.add(new DataFrame().set("Id", "123").set("Fee", 0.225));
    frames.add(new DataFrame().set("Id", "456").set("Fee", "0.225"));
    frames.add(new DataFrame().set("Id", "789").set("Fee", 5));
    frames.add(new DataFrame().set("Id", "012").set("Fee", "-3"));

    Config listenerCfg = new Config();
    listenerCfg.put(ConfigTag.FIELD, "Fee");

    try (FieldTotal listener = new FieldTotal()) {
      listener.setConfiguration(listenerCfg);
      getTransformContext().addListener(listener);
      listener.open(getTransformContext());

      for (int x = 0; x < frames.size(); x++) {
        TransactionContext txnCtx = new TransactionContext(getTransformContext());
        txnCtx.setRow(x);
        txnCtx.setTargetFrame(frames.get(x));
        getTransformContext().fireMap(txnCtx);
      }
      getTransformContext().end();

      Object symbol = getTransformContext().getSymbols().get("Fee.total");
      assertNotNull(symbol);
      assertTrue(symbol instanceof Double);
      String value = getTransformContext().getSymbols().getString("Fee.total", "#.#####");
      assertEquals("2.45", value);

    } catch (Exception e) {
      System.out.println(e.getMessage());
      fail(e.getMessage());
    }

  }

}
