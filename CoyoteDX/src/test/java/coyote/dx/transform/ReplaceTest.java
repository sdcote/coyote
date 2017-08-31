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
public class ReplaceTest extends AbstractTest {

  @Test
  public void basic() throws IOException, ConfigurationException, TransformException {
    String cfgData = "{ \"field\" : \"description\",  \"target\" : \"\\n\",  \"value\" : \" \" }";
    Config configuration = parseConfiguration(cfgData);
    System.out.println(configuration);

    TransactionContext context = createTransactionContext();

    DataFrame sourceFrame = new DataFrame();
    sourceFrame.put("description", "Now is the time\nfor all good men\nto come to the aid\nof the party.");
    context.setSourceFrame(sourceFrame);
    String data = sourceFrame.getAsString("description");
    System.out.println(data);

    try (Replace transformer = new Replace()) {
      transformer.setConfiguration(configuration);
      transformer.open(getTransformContext());

      DataFrame result = transformer.process(sourceFrame);
      assertNotNull(result);
      data = result.getAsString("description");
      System.out.println(data);
      assertEquals(data, "Now is the time for all good men to come to the aid of the party.");
    }

  }

}
