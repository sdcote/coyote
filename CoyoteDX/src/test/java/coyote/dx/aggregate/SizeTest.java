/*
 * Copyright (c) 2017 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.dx.aggregate;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import coyote.dataframe.DataFrame;
import coyote.dx.AbstractTest;
import coyote.dx.context.TransactionContext;
import coyote.loader.cfg.Config;
import coyote.loader.cfg.ConfigurationException;
import coyote.loader.log.ConsoleAppender;
import coyote.loader.log.Log;


/**
 * 
 */
public class SizeTest extends AbstractTest {

  /**
   * @throws java.lang.Exception
   */
  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    Log.addLogger(Log.DEFAULT_LOGGER_NAME, new ConsoleAppender(Log.TRACE_EVENTS | Log.DEBUG_EVENTS | Log.INFO_EVENTS | Log.WARN_EVENTS | Log.ERROR_EVENTS | Log.FATAL_EVENTS));
  }




  @Test
  public void limit() throws ConfigurationException, IOException {
    DataFrame cfg = new DataFrame().set("limit", 2);
    Config configuration = new Config(cfg);

    List<DataFrame> frames = new ArrayList<>();
    frames.add(new DataFrame().set("city", "CMH"));
    frames.add(new DataFrame().set("city", "CLE"));
    frames.add(new DataFrame().set("city", "IATA"));

    try (Size aggregator = new Size()) {
      aggregator.setConfiguration(configuration);
      aggregator.open(getTransformContext());

      List<DataFrame> result = null;
      List<DataFrame> framelist = new ArrayList<>();
      framelist.add(new DataFrame()); // prime the array
      TransactionContext context = createTransactionContext();
      for (int x = 0; x < frames.size(); x++) {
        framelist.set(0, frames.get(x));
        if (x + 1 == frames.size()) {
          context.setLastFrame(true);
        }
        result = aggregator.aggregate(framelist, context);
        assertNotNull(result);
        if (context.isLastFrame()) {
          assertTrue(result.size() > 0);
        } else {
          assertTrue(result.size() == 0);
        }
      }
      assertNotNull(result);

      // size should be 2
      assertTrue(result.size() == 2);
    }
  }

}
