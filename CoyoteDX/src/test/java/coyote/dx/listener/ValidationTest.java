/*
 * Copyright (c) 2017 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.dx.listener;

import static org.junit.Assert.fail;

import org.junit.BeforeClass;
import org.junit.Test;

import coyote.dataframe.DataFrame;
import coyote.dx.AbstractTest;
import coyote.dx.ConfigTag;
import coyote.dx.FrameValidator;
import coyote.dx.context.TransactionContext;
import coyote.dx.validate.NotEmpty;
import coyote.loader.cfg.Config;
import coyote.loader.log.ConsoleAppender;
import coyote.loader.log.Log;


/**
 * 
 */
public class ValidationTest extends AbstractTest {

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    Log.addLogger(Log.DEFAULT_LOGGER_NAME, new ConsoleAppender(Log.TRACE_EVENTS | Log.DEBUG_EVENTS | Log.INFO_EVENTS | Log.WARN_EVENTS | Log.ERROR_EVENTS | Log.FATAL_EVENTS));
  }




  @Test
  public void standardOutput() {

    Config validatorCfg = new Config();
    validatorCfg.put(ConfigTag.FIELD, "model");
    validatorCfg.put(ConfigTag.DESCRIPTION, "Model cannot be empty");

    Config listenerCfg = new Config();
    listenerCfg.put(ConfigTag.TARGET, "STDOUT");

    String checksumFile = null;
    try (Validation listener = new Validation(); FrameValidator validator = new NotEmpty()) {

      validator.setConfiguration(validatorCfg);
      validator.open(getTransformContext());

      listener.setConfiguration(listenerCfg);
      listener.open(getTransformContext());

      TransactionContext txnCtx = new TransactionContext(getTransformContext());
      txnCtx.setRow(0);
      txnCtx.setWorkingFrame(new DataFrame().set("TestField", "NothingHere"));

      listener.onValidationFailed(txnCtx, validator, "Got blowed up real good");

      listener.onFrameValidationFailed(txnCtx);
    } catch (Exception e) {
      fail(e.getMessage());
    }

  }




  @Test
  public void standardError() {

    Config validatorCfg = new Config();
    validatorCfg.put(ConfigTag.FIELD, "model");
    validatorCfg.put(ConfigTag.DESCRIPTION, "Model cannot be empty");

    Config listenerCfg = new Config();
    listenerCfg.put(ConfigTag.TARGET, "STDERR");

    String checksumFile = null;
    try (Validation listener = new Validation(); FrameValidator validator = new NotEmpty()) {

      validator.setConfiguration(validatorCfg);
      validator.open(getTransformContext());

      listener.setConfiguration(listenerCfg);
      listener.open(getTransformContext());

      TransactionContext txnCtx = new TransactionContext(getTransformContext());
      txnCtx.setRow(0);
      txnCtx.setWorkingFrame(new DataFrame().set("TestField", "NothingHere"));

      listener.onValidationFailed(txnCtx, validator, "Got blowed up real good");

      listener.onFrameValidationFailed(txnCtx);
    } catch (Exception e) {
      fail(e.getMessage());
    }

  }

}
