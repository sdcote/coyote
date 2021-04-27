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

import java.io.IOException;

import org.junit.Test;

import coyote.dataframe.DataFrame;
import coyote.dx.AbstractTest;
import coyote.dx.FrameValidator;
import coyote.dx.context.TransactionContext;
import coyote.loader.cfg.Config;
import coyote.loader.cfg.ConfigurationException;


/**
 * 
 */
public class NotEmptyTest extends AbstractTest {

  @Test
  public void test() {

    String cfgData = "{ \"field\" : \"model\",  \"desc\" : \"Model cannot be empty\" }";
    Config configuration = parseConfiguration(cfgData);

    // Create a transaction context
    TransactionContext context = createTransactionContext();

    // Populate it with test data
    DataFrame sourceFrame = new DataFrame();
    sourceFrame.put("model", "PT4500");
    context.setSourceFrame(sourceFrame);

    try (FrameValidator validator = new NotEmpty()) {
      // Configure the validator
      validator.setConfiguration(configuration);
      // open (initialize) the component 
      validator.open(getTransformContext());

      boolean result = validator.process(context);
      assertTrue(result);

      // Create a new test frame and place it in the context for validation
      sourceFrame = new DataFrame();
      sourceFrame.put("model", " ");
      context.setSourceFrame(sourceFrame);

      result = validator.process(context);
      assertFalse(result);

    } catch (ConfigurationException | ValidationException | IOException e) {
      e.printStackTrace();
      fail(e.getMessage());
    }

  }

}
