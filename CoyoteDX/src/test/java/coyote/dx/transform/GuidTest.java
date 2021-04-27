/*
 * Copyright (c) 2017 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.dx.transform;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.Test;

import coyote.commons.StringUtil;
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
public class GuidTest extends AbstractTest {

  @Test
  public void test() throws ConfigurationException, IOException, TransformException, DataFrameException {
    String cfgData = "{ \"field\" : \"Id\" }";
    Config configuration = parseConfiguration(cfgData);
    TransactionContext context = createTransactionContext();
    DataFrame sourceFrame = new DataFrame();
    context.setSourceFrame(sourceFrame);

    try (Guid transformer = new Guid()) {
      transformer.setConfiguration(configuration);
      transformer.open(getTransformContext());

      DataFrame result = transformer.process(sourceFrame);
      assertNotNull(result);
      String value = result.getAsString("Id");
      assertTrue(StringUtil.isNotBlank(value));

      result = transformer.process(sourceFrame);
      assertNotNull(result);
      String nextValue = result.getAsString("Id");
      assertTrue(StringUtil.isNotBlank(value));
      assertFalse(value.equals(nextValue));
    }

  }




  @Test
  public void secure() throws ConfigurationException, IOException, TransformException, DataFrameException {
    String cfgData = "{ \"field\" : \"Id\", \"secure\" : true }";
    Config configuration = parseConfiguration(cfgData);
    TransactionContext context = createTransactionContext();
    DataFrame sourceFrame = new DataFrame();
    context.setSourceFrame(sourceFrame);

    try (Guid transformer = new Guid()) {
      transformer.setConfiguration(configuration);
      transformer.open(getTransformContext());

      DataFrame result = transformer.process(sourceFrame);
      assertNotNull(result);
      String value = result.getAsString("Id");
      assertTrue(StringUtil.isNotBlank(value));

      result = transformer.process(sourceFrame);
      assertNotNull(result);
      String nextValue = result.getAsString("Id");
      assertTrue(StringUtil.isNotBlank(value));
      assertFalse(value.equals(nextValue));
    }

  }
  
}
