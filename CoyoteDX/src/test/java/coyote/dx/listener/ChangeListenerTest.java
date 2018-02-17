/*
 * Copyright (c) 2018 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.dx.listener;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import coyote.commons.Decimal;
import coyote.dataframe.DataFrame;
import coyote.dx.ConfigTag;
import coyote.dx.context.ContextListener;
import coyote.loader.cfg.Config;
import coyote.loader.log.ConsoleAppender;
import coyote.loader.log.Log;


/**
 * 
 */
public class ChangeListenerTest extends AbstractChangeListenerTest {

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

    try (TestListener listener = new TestListener()) {
      initListener(listener, listenerCfg);
      loadListener(listener, frames);

      Decimal average = listener.getSimpleAverage();
      assertNotNull(average);
      assertTrue(average.equals(Decimal.valueOf("0.6125")));

      Decimal exponentialAverage = listener.getExponentialAverage();
      assertNotNull(exponentialAverage);
      assertTrue(exponentialAverage.equals(Decimal.valueOf("-0.4640625")));

      Decimal exponentialAverage2 = listener.getExponentialAverage(Decimal.valueOf("0.5"));
      assertNotNull(exponentialAverage2);
      assertTrue(exponentialAverage2.equals(Decimal.valueOf("0.41875")));
      
      Decimal lastSample = listener.getLastSample();
      assertNotNull(lastSample);
      assertTrue(lastSample.equals(Decimal.valueOf("-3")));

      Decimal firstSample = listener.getFirstSample();
      assertNotNull(firstSample);
      assertTrue(firstSample.equals(Decimal.valueOf("0.225")));

    } catch (Exception e) {
      System.out.println(e.getMessage());
      fail(e.getMessage());
    }

  }




  @Test
  public void fieldSearch() {
    List<DataFrame> frames = getSystemMetrics();

    Config listenerCfg = new Config();
    listenerCfg.put(ConfigTag.FIELD, "Memory");

    try (TestListener listener = new TestListener()) {
      initListener(listener, listenerCfg);
      loadListener(listener, frames);

      Decimal average = listener.getSimpleAverage();
      assertNotNull(average);
      assertTrue(average.equals(Decimal.valueOf("0.43875")));

      Decimal total = listener.getTotal();
      assertNotNull(total);
      assertTrue(total.equals(Decimal.valueOf("3.51")));

      Decimal min = listener.getMinimum();
      assertNotNull(min);
      assertTrue(min.equals(Decimal.valueOf("0.21")));

      Decimal max = listener.getMaximum();
      assertNotNull(max);
      assertTrue(max.equals(Decimal.valueOf("0.8")));

    } catch (Exception e) {
      System.out.println(e.getMessage());
      fail(e.getMessage());
    }

  }




  /**
   * Each unique value in the field "System" will specify a grouping.
   * Samples will be grouped by the different "System" names.
   */
  @Test
  public void grouping() {
    List<DataFrame> frames = getSystemMetrics();

    Config listenerCfg = new Config();
    listenerCfg.put(ConfigTag.FIELD, "Memory");
    listenerCfg.put(ConfigTag.GROUP, "System");

    try (TestListener listener = new TestListener()) {
      initListener(listener, listenerCfg);
      loadListener(listener, frames);

      // get the average memory usage for the Billing system
      Decimal average = listener.getSimpleAverage("Billing");
      assertNotNull(average);
      assertTrue(average.equals(Decimal.valueOf("0.55")));

      // get the average memory usage for the "OrderEntry" system
      average = listener.getSimpleAverage("OrderEntry");
      assertTrue(average.equals(Decimal.valueOf("0.3275")));

      Decimal total = listener.getTotal("Billing");
      assertNotNull(total);
      assertTrue(total.equals(Decimal.valueOf("2.2")));

      Decimal lastSample = listener.getLastSample("Billing");
      assertNotNull(lastSample);
      assertTrue(lastSample.equals(Decimal.valueOf("0.8")));

      // non-existant group
      Decimal number = listener.getTotal("Server");
      assertNotNull(number);
      assertTrue(number.equals(Decimal.valueOf("0")));

    } catch (Exception e) {
      System.out.println(e.getMessage());
      fail(e.getMessage());
    }

  }






  /**
   * Extend the abstract class so it can be tested.
   */
  private class TestListener extends AbstractChangeListener implements ContextListener {
    // we are testing the base class to nothing should go here
  }

}
