/*
 * Copyright (c) 2017 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.dx.writer;

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
public class CSVWriterTest extends AbstractTest {

  @Test
  public void separator() throws ConfigurationException, IOException, TransformException, DataFrameException {
    String cfgData = "{ \"separator\" : \"\\t\" }";
    Config configuration = parseConfiguration(cfgData);

    TransactionContext context = createTransactionContext();
    DataFrame sourceFrame = new DataFrame();
    context.setSourceFrame(sourceFrame);

    try (CsvWriter2 writer = new CsvWriter2()) {
      writer.setConfiguration(configuration);
      writer.open(getTransformContext());

      char separator = writer.getSeparator();
      int value = (int)separator;
      assertTrue(value == 9);
    }

  }

}
