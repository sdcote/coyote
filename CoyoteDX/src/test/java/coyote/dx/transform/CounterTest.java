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

import java.io.IOException;

import org.junit.Test;

import coyote.dataframe.DataFrame;
import coyote.dataframe.DataFrameException;
import coyote.dx.AbstractTest;
import coyote.dx.TransformException;
import coyote.dx.context.TransactionContext;
import coyote.loader.cfg.Config;
import coyote.loader.cfg.ConfigurationException;


/**
 * 
 */
public class CounterTest extends AbstractTest {

  @Test
  public void test() throws ConfigurationException, IOException, TransformException, DataFrameException {
    String cfgData = "{ \"field\" : \"Counter\" }";
    Config configuration = parseConfiguration(cfgData);
    TransactionContext context = createTransactionContext();
    DataFrame sourceFrame = new DataFrame();
    context.setSourceFrame(sourceFrame);

    try (Counter transformer = new Counter()) {
      transformer.setConfiguration(configuration);
      transformer.open(getTransformContext());

      DataFrame result = transformer.process(sourceFrame);
      assertNotNull(result);
      long value = result.getAsLong("Counter");
      assertTrue(value == 0);

      result = transformer.process(sourceFrame);
      assertNotNull(result);
      value = result.getAsLong("Counter");
      assertTrue(value == 1);

      result = transformer.process(sourceFrame);
      assertNotNull(result);
      value = result.getAsLong("Counter");
      assertTrue(value == 2);
    }

  }




  @Test
  public void step() throws ConfigurationException, IOException, TransformException, DataFrameException {
    String cfgData = "{ \"field\" : \"Counter\", \"step\" : 5 }";
    Config configuration = parseConfiguration(cfgData);
    TransactionContext context = createTransactionContext();
    DataFrame sourceFrame = new DataFrame();
    context.setSourceFrame(sourceFrame);

    try (Counter transformer = new Counter()) {
      transformer.setConfiguration(configuration);
      transformer.open(getTransformContext());

      DataFrame result = transformer.process(sourceFrame);
      assertNotNull(result);
      long value = result.getAsLong("Counter");
      assertTrue(value == 0);

      result = transformer.process(sourceFrame);
      assertNotNull(result);
      value = result.getAsLong("Counter");
      assertTrue(value == 5);

      result = transformer.process(sourceFrame);
      assertNotNull(result);
      value = result.getAsLong("Counter");
      assertTrue(value == 10);
    }

  }




  @Test
  public void start() throws ConfigurationException, IOException, TransformException, DataFrameException {
    String cfgData = "{ \"field\" : \"Counter\", \"start\" : 1 }";
    Config configuration = parseConfiguration(cfgData);
    TransactionContext context = createTransactionContext();
    DataFrame sourceFrame = new DataFrame();
    context.setSourceFrame(sourceFrame);

    try (Counter transformer = new Counter()) {
      transformer.setConfiguration(configuration);
      transformer.open(getTransformContext());

      DataFrame result = transformer.process(sourceFrame);
      assertNotNull(result);
      long value = result.getAsLong("Counter");
      assertTrue(value == 1);

      result = transformer.process(sourceFrame);
      assertNotNull(result);
      value = result.getAsLong("Counter");
      assertTrue(value == 2);

      result = transformer.process(sourceFrame);
      assertNotNull(result);
      value = result.getAsLong("Counter");
      assertTrue(value == 3);
    }

  }

}
