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

import coyote.commons.template.SymbolTable;
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
public class MultiplyTest extends AbstractTest {

  @Test
  public void basic() throws IOException, ConfigurationException, TransformException, DataFrameException {
    String cfgData = "{ \"field\" : \"TotalCost\",  \"factor\" : \"10\" }";
    Config configuration = parseConfiguration(cfgData);

    TransactionContext context = createTransactionContext();

    DataFrame sourceFrame = new DataFrame().set("TotalCost", "7");
    context.setSourceFrame(sourceFrame);

    try (Multiply transformer = new Multiply()) {
      transformer.setConfiguration(configuration);
      transformer.open(getTransformContext());

      DataFrame result = transformer.process(sourceFrame);
      assertNotNull(result);
      long value = result.getAsLong("TotalCost");
      assertTrue(value == 70);
    }

  }




  @Test
  public void source() throws IOException, ConfigurationException, TransformException, DataFrameException {
    String cfgData = "{ \"field\" : \"TotalCost\",  \"source\" : \"Count\",  \"factor\" : \"3.50\" }";
    Config configuration = parseConfiguration(cfgData);

    TransactionContext context = createTransactionContext();

    DataFrame sourceFrame = new DataFrame().set("Count", "6");
    context.setSourceFrame(sourceFrame);

    try (Multiply transformer = new Multiply()) {
      transformer.setConfiguration(configuration);
      transformer.open(getTransformContext());

      DataFrame result = transformer.process(sourceFrame);
      assertNotNull(result);
      double value = result.getAsDouble("TotalCost");
      assertTrue(value == 21);
    }
  }




  @Test
  public void setSymbol() throws IOException, ConfigurationException, TransformException, DataFrameException {
    String cfgData = "{ \"field\" : \"TotalCost\",  \"source\" : \"Count\",  \"factor\" : \"3.50\", \"setsymbol\": true }";
    Config configuration = parseConfiguration(cfgData);

    TransactionContext context = createTransactionContext();

    getTransformContext().setSymbols(new SymbolTable());

    DataFrame sourceFrame = new DataFrame().set("Count", "3");
    context.setSourceFrame(sourceFrame);

    try (Multiply transformer = new Multiply()) {
      transformer.setConfiguration(configuration);
      transformer.open(getTransformContext());

      DataFrame result = transformer.process(sourceFrame);
      assertNotNull(result);
      double value = result.getAsDouble("TotalCost");
      assertTrue(value == 10.5);

      Object symbol = getTransformContext().getSymbols().get("TotalCost");
      assertNotNull(symbol);
      assertTrue(symbol instanceof String);
      assertTrue("10.5".equals(symbol));

    }
  }

}
