/*
 * Copyright (c) 2016 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.dx.listener;

import java.util.ArrayList;
import java.util.List;

import coyote.commons.StringUtil;
import coyote.dx.FrameValidator;
import coyote.dx.context.OperationalContext;
import coyote.dx.context.TransactionContext;
import coyote.loader.cfg.Config;
import coyote.loader.cfg.ConfigurationException;


/**
 * This writes the record with all the errors for later processing.
 */
public class Validation extends FileRecorder {

  List<String> validationErrors = new ArrayList<String>();




  /**
   * @see coyote.dx.listener.ContextRecorder#setConfiguration(coyote.loader.cfg.Config)
   */
  @Override
  public void setConfiguration(Config cfg) throws ConfigurationException {
    super.setConfiguration(cfg);

    // check for any other options to set here...like format, whether to include the error message...

  }




  /**
   * @see coyote.dx.listener.AbstractListener#onValidationFailed(coyote.dx.context.OperationalContext, coyote.dx.FrameValidator, java.lang.String)
   */
  @Override
  public void onValidationFailed(OperationalContext context, FrameValidator validator, String errorMessage) {

    StringBuffer b = new StringBuffer();
    b.append(validator.getFieldName());
    b.append(" did not pass '");
    b.append(validator.getClass().getSimpleName());
    b.append("' check: ");
    b.append(validator.getDescription());
    validationErrors.add(b.toString());
  }




  public void onFrameValidationFailed(TransactionContext context) {

    // write the record out with the errors

    StringBuffer b = new StringBuffer();

    b.append(context.getRow());
    b.append(": ");

    // show the frame which failed validation
    b.append(context.getWorkingFrame().toString());
    b.append(": ");

    // Show the validation errors
    for (int x = 0; x < validationErrors.size(); x++) {
      b.append(validationErrors.get(x));
      if (x + 1 < validationErrors.size()) {
        b.append(", ");
      }
    }
    b.append(StringUtil.LINE_FEED);

    // clear out the collected errors
    validationErrors.clear();

    // write out the validation failure
    write(b.toString());

  }

}
