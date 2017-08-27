/*
 * Copyright (c) 2015 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.dx.validate;

import java.io.IOException;

import coyote.commons.StringUtil;
import coyote.dx.AbstractConfigurableComponent;
import coyote.dx.CDX;
import coyote.dx.ConfigTag;
import coyote.dx.ConfigurableComponent;
import coyote.dx.FrameValidator;
import coyote.dx.context.TransactionContext;
import coyote.dx.context.TransformContext;
import coyote.loader.cfg.Config;
import coyote.loader.cfg.ConfigurationException;
import coyote.loader.log.Log;
import coyote.loader.log.LogMsg;


/**
 * Validators work on the working frame and check to see if the working data 
 * meets the requirements of the DX job.
 * 
 * <p>Configuration example:
 * <pre>"Validate" : {
 *   "Distinct" : { "field" : "asset_tag",  "desc" : "Asset tag must be unique", "halt": false  }
 * },</pre>
 * 
 * <p>The "halt" configuration attribute controls if the transaction should be 
 * halted by placing the transaction context in error. 
 */
public abstract class AbstractValidator extends AbstractConfigurableComponent implements FrameValidator, ConfigurableComponent {

  protected String fieldName = null;
  protected String description = null;




  /**
   * Returns if this component is configured to halt on failure.
   * 
   * @return true if the "halt" configuration parameter is present and set to 
   *         true, false otherwise.
   */
  public boolean haltOnFail() {
    if (configuration.containsIgnoreCase(ConfigTag.HALT_ON_FAIL)) {
      return configuration.getBoolean(ConfigTag.HALT_ON_FAIL);
    }
    return false;
  }




  /**
   * @see coyote.dx.Component#open(coyote.dx.context.TransformContext)
   */
  @Override
  public void open(TransformContext context) {
    // all components need to implement this method but not all components 
    // need to be opened.    
  }




  /**
   * @see java.io.Closeable#close()
   */
  @Override
  public void close() throws IOException {
    // implementation for Closable, not all components require closing
  }




  /**
   * Fires the validation failed event in the listeners with the description 
   * of this validation rule and optionally sets the transaction context in 
   * error if {@link #haltOnFail()} is set to true.
   * 
   * @param context The current transaction context.
   * @param field the field name failing validation.
   * @param message the reason why the validation failed.
   */
  protected void fail(TransactionContext context, String field, String message) {
    context.fireValidationFailed(this, message);
    if (haltOnFail()) {
      context.setError(message);
    }
  }




  /**
   * Fires the validation failed event in the listeners with the description 
   * of this validation rule and optionally sets the transaction context in 
   * error if {@link #haltOnFail()} is set to true.
   * 
   * @param context The current transaction context.
   * @param field the field name failing validation.
   */
  protected void fail(TransactionContext context, String field) {
    if (StringUtil.isNotBlank(description)) {
      fail(context, field, description);
    } else {
      fail(context, field, getClass() + " validation of " + field + " failed");
    }
  }




  /**
  * @see coyote.dx.AbstractConfigurableComponent#setConfiguration(coyote.loader.cfg.Config)
  */
  @Override
  public void setConfiguration(Config cfg) throws ConfigurationException {
    configuration = cfg;

    // All validators need to know which fields to validate
    if (cfg.contains(ConfigTag.FIELD)) {
      fieldName = cfg.getAsString(ConfigTag.FIELD);
    } else {
      throw new ConfigurationException("Missing required '" + ConfigTag.FIELD + "' attribute");
    }

    if (cfg.contains(ConfigTag.DESCRIPTION)) {
      description = cfg.getAsString(ConfigTag.DESCRIPTION);
    }

    // Check if we are to thrown a context error if validation fails
    if (cfg.containsIgnoreCase(ConfigTag.HALT_ON_FAIL)) {
      try {
        cfg.getBoolean(ConfigTag.HALT_ON_FAIL);
      } catch (Exception e) {
        Log.info(LogMsg.createMsg(CDX.MSG, "Task.Header flag not valid " + e.getMessage()));
      }
    } else {
      Log.debug(LogMsg.createMsg(CDX.MSG, "Validator.no_halt_config"));
    }
  }




  /**
   * Field name accessor.
   * 
   * @return the name of the field to which this validator is targeted
   */
  public String getFieldName() {
    return fieldName;
  }




  /**
   * Validation descriptor message.
   * 
   * @return the description of this validator (Also used in error messages)
   */
  public String getDescription() {
    return description;
  }

}
