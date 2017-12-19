/*
 * Copyright (c) 2017 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.dx.transform;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;

import org.junit.BeforeClass;
import org.junit.Test;

import coyote.dataframe.DataField;
import coyote.dataframe.DataFrame;
import coyote.dataframe.DataFrameException;
import coyote.dx.AbstractTest;
import coyote.dx.TransformException;
import coyote.loader.cfg.Config;
import coyote.loader.cfg.ConfigurationException;
import coyote.loader.log.ConsoleAppender;
import coyote.loader.log.Log;


/**
 * 
 */
public class DateTest extends AbstractTest {

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    Log.addLogger(Log.DEFAULT_LOGGER_NAME, new ConsoleAppender(Log.TRACE_EVENTS | Log.DEBUG_EVENTS | Log.INFO_EVENTS | Log.WARN_EVENTS | Log.ERROR_EVENTS | Log.FATAL_EVENTS));
  }




  @Test
  public void unixTime() throws ConfigurationException, IOException, TransformException, DataFrameException {
    DataFrame cfg = new DataFrame().set("field", "date").set("format", "seconds");
    System.out.println(cfg.toString());
    Config configuration = new Config(cfg);

    DataFrame workingFrame = new DataFrame();
    workingFrame.put("date", "1513620300");

    try (Date transformer = new Date()) {
      transformer.setConfiguration(configuration);
      transformer.open(getTransformContext());

      DataFrame result = transformer.process(workingFrame);
      assertNotNull(result);
      DataField dateField = result.getField("date");
      assertTrue(dateField.getType() == DataField.DATE);
      Object value = dateField.getObjectValue();
      assertNotNull(value);
      assertTrue(value instanceof java.util.Date);
      java.util.Date date = (java.util.Date)value;
      System.out.println(date.toString());
    }

  }




  @Test
  public void javaTime() throws ConfigurationException, IOException, TransformException, DataFrameException {
    DataFrame cfg = new DataFrame().set("field", "date").set("format", "milliseconds");
    System.out.println(cfg.toString());
    Config configuration = new Config(cfg);

    DataFrame workingFrame = new DataFrame();
    workingFrame.put("date", "1513655245615");

    try (Date transformer = new Date()) {
      transformer.setConfiguration(configuration);
      transformer.open(getTransformContext());

      DataFrame result = transformer.process(workingFrame);
      assertNotNull(result);
      DataField dateField = result.getField("date");
      assertTrue(dateField.getType() == DataField.DATE);
      Object value = dateField.getObjectValue();
      assertNotNull(value);
      assertTrue(value instanceof java.util.Date);
      System.out.println(value.toString());
    }

  }




  @Test
  public void now() throws ConfigurationException, IOException, TransformException, DataFrameException {
    DataFrame cfg = new DataFrame().set("field", "date").set("format", "now");
    System.out.println(cfg.toString());
    Config configuration = new Config(cfg);

    DataFrame workingFrame = new DataFrame();
    workingFrame.put("date", "SomeValue");

    try (Date transformer = new Date()) {
      transformer.setConfiguration(configuration);
      transformer.open(getTransformContext());

      DataFrame result = transformer.process(workingFrame);
      assertNotNull(result);
      DataField dateField = result.getField("date");
      assertTrue(dateField.getType() == DataField.DATE);
      Object value = dateField.getObjectValue();
      assertNotNull(value);
      assertTrue(value instanceof java.util.Date);
      System.out.println(value.toString());
    }

  }




  @Test
  public void invalidFormat() throws ConfigurationException, IOException, DataFrameException {
    DataFrame cfg = new DataFrame().set("field", "date").set("format", "unixTime");
    Config configuration = new Config(cfg);

    DataFrame workingFrame = new DataFrame();
    workingFrame.put("date", "1513620300");

    try (Date transformer = new Date()) {
      transformer.setConfiguration(configuration);
      transformer.open(getTransformContext());
      transformer.process(workingFrame);
      fail("Should not support invalid format");
    } catch (TransformException e) {
      // expected
    }
  }




  @Test
  public void bestGuess() throws ConfigurationException, IOException, TransformException, DataFrameException {
    DataFrame cfg = new DataFrame().set("field", "date");
    System.out.println(cfg.toString());
    Config configuration = new Config(cfg);

    DataFrame workingFrame = new DataFrame();

    try (Date transformer = new Date()) {
      transformer.setConfiguration(configuration);
      transformer.open(getTransformContext());

      workingFrame.put("date", "2017-12-19 13:22:14");
      DataFrame result = transformer.process(workingFrame);
      assertNotNull(result);
      DataField dateField = result.getField("date");
      assertTrue(dateField.getType() == DataField.DATE);
      Object value = dateField.getObjectValue();
      assertNotNull(value);
      assertTrue(value instanceof java.util.Date);
      System.out.println(value.toString());

      workingFrame.put("date", "2017-12-19");
      result = transformer.process(workingFrame);
      assertNotNull(result);
      dateField = result.getField("date");
      assertTrue(dateField.getType() == DataField.DATE);
      value = dateField.getObjectValue();
      System.out.println(value.toString());

      workingFrame.put("date", "12-18-2017 09:03:27");
      result = transformer.process(workingFrame);
      assertNotNull(result);
      dateField = result.getField("date");
      assertTrue(dateField.getType() == DataField.DATE);
      value = dateField.getObjectValue();
      System.out.println(value.toString());

      workingFrame.put("date", "12-18-2017");
      result = transformer.process(workingFrame);
      assertNotNull(result);
      dateField = result.getField("date");
      assertTrue(dateField.getType() == DataField.DATE);
      value = dateField.getObjectValue();

    }

  }

}
