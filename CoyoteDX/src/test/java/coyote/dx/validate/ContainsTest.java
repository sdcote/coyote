/*
 * Copyright (c) 2015 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.dx.validate;

//import static org.junit.Assert.*;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Ignore;
import org.junit.Test;

import coyote.dataframe.DataFrame;
import coyote.dx.AbstractTest;
import coyote.dx.ConfigTag;
import coyote.dx.FrameValidator;
import coyote.dx.context.TransactionContext;
import coyote.loader.cfg.Config;
import coyote.loader.cfg.ConfigurationException;


/**
 * 
 */
public class ContainsTest extends AbstractTest {

  @Test
  public void basics() {

    String[] models = {"PT3500", "PT4000", "PT4500"};
    DataFrame config = new DataFrame()
        .set(ConfigTag.FIELD, "model")
        .set(ConfigTag.VALUES, models)
        .set(ConfigTag.DESCRIPTION, "Only certain models are supported");

    Config configuration = parseConfiguration(config.toString());

    @SuppressWarnings("resource")
    FrameValidator validator = new Contains();

    // Configure it
    try {
      validator.setConfiguration(configuration);
    } catch (ConfigurationException e) {
      e.printStackTrace();
      fail(e.getMessage());
    }

    // open (initialize) the component 
    validator.open(getTransformContext());

    // Create a transaction context
    TransactionContext context = createTransactionContext();

    // Populate it with test data
    DataFrame sourceFrame = new DataFrame();
    sourceFrame.put("model", "PT4500");

    context.setSourceFrame(sourceFrame);

    try {
      boolean result = validator.process(context);
      assertTrue(result);
    } catch (ValidationException e) {
      e.printStackTrace();
      fail(e.getMessage());
    }

    // Case is different and ignore case is false by default
    context = createTransactionContext();
    sourceFrame = new DataFrame();
    sourceFrame.put("model", "pt4500");
    context.setSourceFrame(sourceFrame);

    try {
      boolean result = validator.process(context);
      assertFalse(result);
    } catch (ValidationException e) {
      e.printStackTrace();
      fail(e.getMessage());
    }

    // Blank value should return false
    context = createTransactionContext();
    sourceFrame = new DataFrame();
    sourceFrame.put("model", " ");
    context.setSourceFrame(sourceFrame);

    try {
      boolean result = validator.process(context);
      assertFalse(result);
    } catch (ValidationException e) {
      e.printStackTrace();
      fail(e.getMessage());
    }

    // Empty value should return false
    context = createTransactionContext();
    sourceFrame = new DataFrame();
    sourceFrame.put("model", "");
    context.setSourceFrame(sourceFrame);

    try {
      boolean result = validator.process(context);
      assertFalse(result);
    } catch (ValidationException e) {
      e.printStackTrace();
      fail(e.getMessage());
    }

    // Null value should return false
    context = createTransactionContext();
    sourceFrame = new DataFrame();
    sourceFrame.put("model", null);
    context.setSourceFrame(sourceFrame);

    try {
      boolean result = validator.process(context);
      assertFalse(result);
    } catch (ValidationException e) {
      e.printStackTrace();
      fail(e.getMessage());
    }

  }

  @Test
  public void ignoreCase() {

    String[] models = {"PT3500", "PT4000", "PT4500"};
    DataFrame config = new DataFrame()
        .set(ConfigTag.FIELD, "model")
        .set(ConfigTag.VALUES, models)
        .set(ConfigTag.DESCRIPTION, "Only certain models are supported")
        .set(ConfigTag.IGNORE_CASE,true);

    Config configuration = parseConfiguration(config.toString());

    @SuppressWarnings("resource")
    FrameValidator validator = new Contains();

    try {
      validator.setConfiguration(configuration);
    } catch (ConfigurationException e) {
      e.printStackTrace();
      fail(e.getMessage());
    }

    validator.open(getTransformContext());

    TransactionContext context = createTransactionContext();
    DataFrame sourceFrame = new DataFrame();
    sourceFrame.put("model", "PT4500");

    context.setSourceFrame(sourceFrame);

    try {
      boolean result = validator.process(context);
      assertTrue(result);
    } catch (ValidationException e) {
      e.printStackTrace();
      fail(e.getMessage());
    }

    context = createTransactionContext();
    sourceFrame = new DataFrame();
    sourceFrame.put("model", "pt4500");
    context.setSourceFrame(sourceFrame);

    try {
      boolean result = validator.process(context);
      assertTrue(result);
    } catch (ValidationException e) {
      e.printStackTrace();
      fail(e.getMessage());
    }

    context = createTransactionContext();
    sourceFrame = new DataFrame();
    sourceFrame.put("model", " ");
    context.setSourceFrame(sourceFrame);

    try {
      boolean result = validator.process(context);
      assertFalse(result);
    } catch (ValidationException e) {
      e.printStackTrace();
      fail(e.getMessage());
    }

    context = createTransactionContext();
    sourceFrame = new DataFrame();
    sourceFrame.put("model", "");
    context.setSourceFrame(sourceFrame);

    try {
      boolean result = validator.process(context);
      assertFalse(result);
    } catch (ValidationException e) {
      e.printStackTrace();
      fail(e.getMessage());
    }

    context = createTransactionContext();
    sourceFrame = new DataFrame();
    sourceFrame.put("model", null);
    context.setSourceFrame(sourceFrame);

    try {
      boolean result = validator.process(context);
      assertFalse(result);
    } catch (ValidationException e) {
      e.printStackTrace();
      fail(e.getMessage());
    }

  }




  @Ignore
  public void readFromFile() {
    DataFrame config = new DataFrame().set(ConfigTag.FIELD, "model").set(ConfigTag.VALUES, "modelfile.txt").set(ConfigTag.DESCRIPTION, "Only certain models are supported");
    Config configuration = parseConfiguration(config.toString());

    @SuppressWarnings("resource")
    FrameValidator validator = new Contains();
    try {
      validator.setConfiguration(configuration);
    } catch (ConfigurationException e) {
      e.printStackTrace();
      fail(e.getMessage());
    }

    validator.open(getTransformContext());
    TransactionContext context = createTransactionContext();
    DataFrame sourceFrame = new DataFrame();
    sourceFrame.put("model", "PT4500");
    context.setSourceFrame(sourceFrame);

    try {
      boolean result = validator.process(context);
      assertTrue(result);
    } catch (ValidationException e) {
      e.printStackTrace();
      fail(e.getMessage());
    }
  }






  @Ignore
  public void readFromUri() {
    DataFrame config = new DataFrame().set(ConfigTag.FIELD, "model").set(ConfigTag.VALUES, "file://modelfile.txt").set(ConfigTag.DESCRIPTION, "Only certain models are supported");
    Config configuration = parseConfiguration(config.toString());

    @SuppressWarnings("resource")
    FrameValidator validator = new Contains();
    try {
      validator.setConfiguration(configuration);
    } catch (ConfigurationException e) {
      e.printStackTrace();
      fail(e.getMessage());
    }

    validator.open(getTransformContext());
    TransactionContext context = createTransactionContext();
    DataFrame sourceFrame = new DataFrame();
    sourceFrame.put("model", "PT4500");
    context.setSourceFrame(sourceFrame);

    try {
      boolean result = validator.process(context);
      assertTrue(result);
    } catch (ValidationException e) {
      e.printStackTrace();
      fail(e.getMessage());
    }
  }






  @Ignore
  public void readFromNetUri() {
    DataFrame config = new DataFrame().set(ConfigTag.FIELD, "model").set(ConfigTag.VALUES, "http://modelfile.txt").set(ConfigTag.DESCRIPTION, "Only certain models are supported");
    Config configuration = parseConfiguration(config.toString());

    @SuppressWarnings("resource")
    FrameValidator validator = new Contains();
    try {
      validator.setConfiguration(configuration);
    } catch (ConfigurationException e) {
      e.printStackTrace();
      fail(e.getMessage());
    }

    validator.open(getTransformContext());
    TransactionContext context = createTransactionContext();
    DataFrame sourceFrame = new DataFrame();
    sourceFrame.put("model", "PT4500");
    context.setSourceFrame(sourceFrame);

    try {
      boolean result = validator.process(context);
      assertTrue(result);
    } catch (ValidationException e) {
      e.printStackTrace();
      fail(e.getMessage());
    }
  }

}
