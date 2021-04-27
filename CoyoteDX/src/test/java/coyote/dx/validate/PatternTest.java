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
public class PatternTest extends AbstractTest {

  @Test
  public void test() {

    String cfgData = "{ \"field\" : \"pci\",  \"avoid\" : \"^4[0-9]{12}(?:[0-9]{3})?$\", \"desc\" : \"VisaCard\", \"halt\" : false  }";
    Config configuration = parseConfiguration(cfgData);

    // Create a transaction context
    TransactionContext context = createTransactionContext();

    // Populate it with test data
    DataFrame sourceFrame = new DataFrame();
    sourceFrame.put("pci", "4539605594999243");
    context.setSourceFrame(sourceFrame);

    try (FrameValidator validator = new Pattern()) {
      // Configure the validator
      validator.setConfiguration(configuration);
      // open (initialize) the component 
      validator.open(getTransformContext());

      boolean result = validator.process(context);
      assertFalse(result);

      // Try changing the field name
      sourceFrame = new DataFrame();
      sourceFrame.put("pciii", "4539605594999243");
      context.setSourceFrame(sourceFrame);

      result = validator.process(context);
      assertTrue(result);

      // Try an Amex card
      sourceFrame = new DataFrame();
      sourceFrame.put("pci", "376774996208282");
      context.setSourceFrame(sourceFrame);

      result = validator.process(context);
      assertTrue(result);

      // 
      sourceFrame = new DataFrame();
      sourceFrame.put("pci", " ");
      context.setSourceFrame(sourceFrame);

      result = validator.process(context);
      assertTrue(result);

    } catch (ConfigurationException | ValidationException | IOException e) {
      e.printStackTrace();
      fail(e.getMessage());
    }

  }

}
