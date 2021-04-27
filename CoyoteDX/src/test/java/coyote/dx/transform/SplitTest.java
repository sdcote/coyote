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

import org.junit.BeforeClass;
import org.junit.Test;

import coyote.dataframe.DataField;
import coyote.dataframe.DataFrame;
import coyote.dataframe.DataFrameException;
import coyote.dx.AbstractTest;
import coyote.dx.TransformException;
import coyote.dx.context.TransactionContext;
import coyote.loader.cfg.Config;
import coyote.loader.cfg.ConfigurationException;
import coyote.loader.log.ConsoleAppender;
import coyote.loader.log.Log;


/**
 * 
 */
public class SplitTest extends AbstractTest {

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    Log.addLogger(Log.DEFAULT_LOGGER_NAME, new ConsoleAppender(Log.TRACE_EVENTS | Log.DEBUG_EVENTS | Log.INFO_EVENTS | Log.WARN_EVENTS | Log.ERROR_EVENTS | Log.FATAL_EVENTS));
  }




  @Test
  public void split() throws ConfigurationException, IOException, TransformException, DataFrameException {
    String cfgData = "{ \"field\" : \"DateTime\", \"Delimiter\" : \"T\" }";
    Config configuration = parseConfiguration(cfgData);
    TransactionContext context = createTransactionContext();
    DataFrame sourceFrame = new DataFrame();
    sourceFrame.put("DateTime", "2017-11-02T10:21:32.076-0400");

    context.setSourceFrame(sourceFrame);

    try (Split transformer = new Split()) {
      transformer.setConfiguration(configuration);
      transformer.open(getTransformContext());

      DataFrame result = transformer.process(sourceFrame);
      assertNotNull(result);
      DataField dateField = result.getField("DateTime.0");
      assertTrue(dateField.getType() == DataField.STRING);
      assertEquals("2017-11-02", dateField.getStringValue());
      DataField timeFeld = result.getField("DateTime.1");
      assertTrue(timeFeld.getType() == DataField.STRING);
      assertEquals("10:21:32.076-0400", timeFeld.getStringValue());
    }

  }

}
