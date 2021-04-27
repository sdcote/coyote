/*
 * Copyright (c) 2017 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.dx.aggregate;

import static org.junit.Assert.assertEquals;
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
public class GroupingTest extends AbstractTest {

  /**
   * @throws java.lang.Exception
   */
  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    Log.addLogger(Log.DEFAULT_LOGGER_NAME, new ConsoleAppender(Log.TRACE_EVENTS | Log.DEBUG_EVENTS | Log.INFO_EVENTS | Log.WARN_EVENTS | Log.ERROR_EVENTS | Log.FATAL_EVENTS));
  }




  @Test
  public void basic() throws ConfigurationException, IOException {
    DataFrame cfg = new DataFrame().set("key", "city");
    Config configuration = new Config(cfg);

    List<DataFrame> frames = new ArrayList<>();
    frames.add(new DataFrame().set("city", "CMH").set("order", "A"));
    frames.add(new DataFrame().set("city", "CLE").set("order", "A"));
    frames.add(new DataFrame().set("city", "CMH").set("order", "B"));
    frames.add(new DataFrame().set("city", "IATA").set("order", "A"));
    frames.add(new DataFrame().set("city", "CLE").set("order", "B"));
    frames.add(new DataFrame().set("city", "CMH").set("order", "C"));
    frames.add(new DataFrame().set("city", "CLE").set("order", "C"));

    try (Grouping aggregator = new Grouping()) {
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
          assertTrue(result.size() > 0); // last frame generates results
        } else {
          assertTrue(result.size() == 0); // no results until last frame
        }
      }

      // size should be seven records - no limiting
      assertTrue(result.size() == 7);

      // order should be in the order the keys were received; CMH,CLE then IATA
      DataFrame frame = result.get(0);
      assertEquals("CMH", frame.getAsString("city"));
      assertEquals("A", frame.getAsString("order"));
      frame = result.get(1);
      assertEquals("CMH", frame.getAsString("city"));
      assertEquals("B", frame.getAsString("order"));
      frame = result.get(2);
      assertEquals("CMH", frame.getAsString("city"));
      assertEquals("C", frame.getAsString("order"));
      frame = result.get(3);
      assertEquals("CLE", frame.getAsString("city"));
      assertEquals("A", frame.getAsString("order"));
      frame = result.get(4);
      assertEquals("CLE", frame.getAsString("city"));
      assertEquals("B", frame.getAsString("order"));
      frame = result.get(5);
      assertEquals("CLE", frame.getAsString("city"));
      assertEquals("C", frame.getAsString("order"));
      frame = result.get(6);
      assertEquals("IATA", frame.getAsString("city"));
      assertEquals("A", frame.getAsString("order"));

    }
  }




  @Test
  public void limit() throws ConfigurationException, IOException {
    DataFrame cfg = new DataFrame().set("key", "city").set("limit", 2);
    Config configuration = new Config(cfg);

    List<DataFrame> frames = new ArrayList<>();
    frames.add(new DataFrame().set("city", "CMH").set("order", "A"));
    frames.add(new DataFrame().set("city", "CLE").set("order", "A"));
    frames.add(new DataFrame().set("city", "CMH").set("order", "B"));
    frames.add(new DataFrame().set("city", "IATA").set("order", "A"));
    frames.add(new DataFrame().set("city", "CLE").set("order", "B"));
    frames.add(new DataFrame().set("city", "CMH").set("order", "C"));
    frames.add(new DataFrame().set("city", "CLE").set("order", "C"));

    try (Grouping aggregator = new Grouping()) {
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

      // size should be five - 2 for CMH and CLE, only 1 for IATA
      assertTrue(result.size() == 5);

      // order should be in the order the keys were received; CMH,CLE then IATA
      DataFrame frame = result.get(0);
      assertEquals("CMH", frame.getAsString("city"));
      assertEquals("B", frame.getAsString("order"));
      frame = result.get(1);
      assertEquals("CMH", frame.getAsString("city"));
      assertEquals("C", frame.getAsString("order"));
      frame = result.get(2);
      assertEquals("CLE", frame.getAsString("city"));
      assertEquals("B", frame.getAsString("order"));
      frame = result.get(3);
      assertEquals("CLE", frame.getAsString("city"));
      assertEquals("C", frame.getAsString("order"));
      frame = result.get(4);
      assertEquals("IATA", frame.getAsString("city"));
      assertEquals("A", frame.getAsString("order"));

    }
  }




  @Test
  public void sort() throws ConfigurationException, IOException {
    DataFrame cfg = new DataFrame().set("key", "city").set("limit", 2).set("sort", "ascend");
    Config configuration = new Config(cfg);

    List<DataFrame> frames = new ArrayList<>();
    frames.add(new DataFrame().set("city", "CMH").set("order", "A"));
    frames.add(new DataFrame().set("city", "CLE").set("order", "A"));
    frames.add(new DataFrame().set("city", "CMH").set("order", "B"));
    frames.add(new DataFrame().set("city", "IATA").set("order", "A"));
    frames.add(new DataFrame().set("city", "CLE").set("order", "B"));
    frames.add(new DataFrame().set("city", "CMH").set("order", "C"));
    frames.add(new DataFrame().set("city", "CLE").set("order", "C"));

    try (Grouping aggregator = new Grouping()) {
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

      // size should be five - 2 for CMH and CLE, only 1 for IATA
      assertTrue(result.size() == 5);

      // order should be  CLE, CMH and IATA
      DataFrame frame = result.get(0);
      assertEquals("CLE", frame.getAsString("city"));
      assertEquals("B", frame.getAsString("order"));
      frame = result.get(1);
      assertEquals("CLE", frame.getAsString("city"));
      assertEquals("C", frame.getAsString("order"));
      frame = result.get(2);
      assertEquals("CMH", frame.getAsString("city"));
      assertEquals("B", frame.getAsString("order"));
      frame = result.get(3);
      assertEquals("CMH", frame.getAsString("city"));
      assertEquals("C", frame.getAsString("order"));
      frame = result.get(4);
      assertEquals("IATA", frame.getAsString("city"));
      assertEquals("A", frame.getAsString("order"));

    }
  }

}
