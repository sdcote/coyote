/*
 * Copyright (c) 2018 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.dx.transform;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;

import org.junit.BeforeClass;
import org.junit.Test;

import coyote.dataframe.DataField;
import coyote.dataframe.DataFrame;
import coyote.dataframe.DataFrameException;
import coyote.dx.AbstractTest;
import coyote.dx.ConfigTag;
import coyote.dx.TransformException;
import coyote.dx.context.TransactionContext;
import coyote.loader.cfg.Config;
import coyote.loader.cfg.ConfigurationException;
import coyote.loader.log.ConsoleAppender;
import coyote.loader.log.Log;


/**
 * 
 */
public class AppendTest extends AbstractTest {

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    Log.addLogger(Log.DEFAULT_LOGGER_NAME, new ConsoleAppender(Log.TRACE_EVENTS | Log.DEBUG_EVENTS | Log.INFO_EVENTS | Log.WARN_EVENTS | Log.ERROR_EVENTS | Log.FATAL_EVENTS));
  }




  @Test
  public void basic() throws ConfigurationException, IOException, TransformException, DataFrameException {
    DataFrame cfg = new DataFrame().set(ConfigTag.FIELD, "NewField").set(ConfigTag.VALUE, "Bar");
    Config configuration = new Config(cfg);

    TransactionContext context = createTransactionContext();

    DataFrame workingFrame = new DataFrame().set("NewField", "Foo");
    context.setWorkingFrame(workingFrame);

    try (Append transformer = new Append()) {
      transformer.setConfiguration(configuration);
      transformer.open(getTransformContext());

      DataFrame result = transformer.process(workingFrame);
      assertNotNull(result);
      DataField field = result.getField("NewField");
      assertEquals("FooBar", field.getStringValue());
    }
  }




  @Test
  public void field() throws ConfigurationException, IOException, TransformException, DataFrameException {
    DataFrame cfg = new DataFrame().set(ConfigTag.FIELD, "FooField").set(ConfigTag.VALUE, "Working.BarField");
    Config configuration = new Config(cfg);

    TransactionContext context = createTransactionContext();
    getTransformContext().setTransaction(context);

    DataFrame workingFrame = new DataFrame().set("FooField", "Foo").set("BarField", "Bar");
    context.setWorkingFrame(workingFrame);

    try (Append transformer = new Append()) {
      transformer.setConfiguration(configuration);
      transformer.open(getTransformContext());

      DataFrame result = transformer.process(workingFrame);
      assertNotNull(result);
      DataField field = result.getField("FooField");
      assertNotNull(field);
      assertEquals("FooBar", field.getStringValue());
    }
  }

}
