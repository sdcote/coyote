/*
 * Copyright (c) 2017 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.dx.transform;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.Test;

import coyote.dataframe.DataFrame;
import coyote.dx.AbstractTest;
import coyote.dx.TransformException;
import coyote.dx.context.TransactionContext;
import coyote.loader.cfg.Config;
import coyote.loader.cfg.ConfigurationException;


/**
 * 
 */
public class TextTest extends AbstractTest {

  @Test
  public void basic() throws IOException, ConfigurationException, TransformException {
    String cfgData = "{ \"field\" : \"number\",  \"format\" : \"#,###\" }";
    Config configuration = parseConfiguration(cfgData);
    TransactionContext context = createTransactionContext();
    DataFrame sourceFrame = new DataFrame();
    sourceFrame.put("number", 123456);
    context.setSourceFrame(sourceFrame);

    try (Text transformer = new Text()) {
      transformer.setConfiguration(configuration);
      transformer.open(getTransformContext());
      DataFrame result = transformer.process(sourceFrame);
      assertNotNull(result);
      Object obj = result.getObject("number");
      assertTrue(obj instanceof String);
      assertEquals("123,456", obj.toString());
    }
  }




  @Test
  public void doubleTest() throws IOException, ConfigurationException, TransformException {
    String cfgData = "{ \"field\" : \"number\",  \"format\" : \"#,###.000\" }";
    Config configuration = parseConfiguration(cfgData);
    TransactionContext context = createTransactionContext();
    DataFrame sourceFrame = new DataFrame();
    sourceFrame.put("number", 123456.05);
    context.setSourceFrame(sourceFrame);

    try (Text transformer = new Text()) {
      transformer.setConfiguration(configuration);
      transformer.open(getTransformContext());
      DataFrame result = transformer.process(sourceFrame);
      assertNotNull(result);
      Object obj = result.getObject("number");
      assertTrue(obj instanceof String);
      assertEquals("123,456.050", obj.toString());
    }
  }




  @Test
  public void formatDate() throws IOException, ConfigurationException, TransformException {
    String cfgData = "{ \"field\" : \"date\",  \"format\" : \"HH:mm:ss\" }";
    Config configuration = parseConfiguration(cfgData);
    TransactionContext context = createTransactionContext();

    DataFrame sourceFrame = new DataFrame();
    sourceFrame.put("date", new java.util.Date());
    context.setSourceFrame(sourceFrame);

    // NOTE: this tests the subclass, acting as an alias to the Text transform
    try (Format transformer = new Format()) {
      transformer.setConfiguration(configuration);
      transformer.open(getTransformContext());

      DataFrame result = transformer.process(sourceFrame);
      assertNotNull(result);
      Object obj = result.getObject("date");
      assertTrue(obj instanceof String);
      assertTrue(obj.toString().length() == 8);
    }
  }




  @Test
  public void guessDate() throws IOException, ConfigurationException, TransformException {
    String FIELDNAME = "date";
    DataFrame cfg = new DataFrame().set("field", FIELDNAME).set("format", "HH:mm:ss");
    Config configuration = new Config(cfg);
    TransactionContext context = createTransactionContext();

    DataFrame sourceFrame = new DataFrame();
    sourceFrame.put(FIELDNAME, new java.util.Date().toString());
    context.setSourceFrame(sourceFrame);

    try (Text transformer = new Text()) {
      transformer.setConfiguration(configuration);
      transformer.open(getTransformContext());

      DataFrame result = transformer.process(sourceFrame);
      assertNotNull(result);
      Object obj = result.getObject(FIELDNAME);
      assertTrue(obj instanceof String);
      assertTrue(obj.toString().length() == 8);
    }
  }




  @Test
  public void guessDouble() throws IOException, ConfigurationException, TransformException {
    String FIELDNAME = "number";
    DataFrame cfg = new DataFrame().set("field", FIELDNAME).set("format", "#,###.000");
    Config configuration = new Config(cfg);
    TransactionContext context = createTransactionContext();

    DataFrame sourceFrame = new DataFrame();
    sourceFrame.put(FIELDNAME, "12345.05");
    context.setSourceFrame(sourceFrame);

    try (Text transformer = new Text()) {
      transformer.setConfiguration(configuration);
      transformer.open(getTransformContext());

      DataFrame result = transformer.process(sourceFrame);
      assertNotNull(result);
      Object obj = result.getObject(FIELDNAME);
      assertTrue(obj instanceof String);
      assertEquals("12,345.050", obj.toString());
    }
  }




  @Test
  public void guessNumber() throws IOException, ConfigurationException, TransformException {
    String FIELDNAME = "number";
    DataFrame cfg = new DataFrame().set("field", FIELDNAME).set("format", "#,###");
    Config configuration = new Config(cfg);
    //System.out.println(configuration);
    TransactionContext context = createTransactionContext();

    DataFrame sourceFrame = new DataFrame();
    sourceFrame.put(FIELDNAME, "1234");
    context.setSourceFrame(sourceFrame);
    //System.out.println(sourceFrame);

    try (Text transformer = new Text()) {
      transformer.setConfiguration(configuration);
      transformer.open(getTransformContext());

      DataFrame result = transformer.process(sourceFrame);
      assertNotNull(result);
      Object obj = result.getObject(FIELDNAME);
      assertTrue(obj instanceof String);
      //System.out.println(obj);
      assertEquals("1,234", obj.toString());
    }
  }

}
