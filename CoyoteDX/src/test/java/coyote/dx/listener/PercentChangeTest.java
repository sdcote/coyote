/*
 * Copyright (c) 2018 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.dx.listener;

import static org.junit.Assert.fail;

import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import coyote.dataframe.DataFrame;
import coyote.dx.ConfigTag;
import coyote.loader.cfg.Config;
import coyote.loader.log.ConsoleAppender;
import coyote.loader.log.Log;


/**
 * 
 */
public class PercentChangeTest extends AbstractChangeListenerTest {

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    Log.addLogger(Log.DEFAULT_LOGGER_NAME, new ConsoleAppender(Log.TRACE_EVENTS | Log.DEBUG_EVENTS | Log.INFO_EVENTS | Log.WARN_EVENTS | Log.ERROR_EVENTS | Log.FATAL_EVENTS));
  }




  @Test
  public void basic() {

    List<DataFrame> frames = getSystemMetrics();

    Config listenerCfg = new Config();
    listenerCfg.put(ConfigTag.FIELD, "Fee");
    listenerCfg.put("Exceeds", ".1");

    try (PercentChange listener = new PercentChange()) {
      initListener(listener, listenerCfg);
      loadListener(listener, frames);

      System.out.println(listener.getSimpleAverage());
      System.out.println(listener.getExponentialAverage());

    } catch (Exception e) {
      System.out.println(e.getMessage());
      fail(e.getMessage());
    }

  }

}
