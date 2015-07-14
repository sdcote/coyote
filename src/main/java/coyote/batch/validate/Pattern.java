/*
 * Copyright (c) 2015 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 *
 * Contributors:
 *   Stephan D. Cote 
 *      - Initial concept and initial implementation
 */
package coyote.batch.validate;

import coyote.batch.FrameValidator;
import coyote.batch.TransactionContext;
import coyote.batch.ValidationException;


/**
 * 
 */
public class Pattern extends AbstractValidator implements FrameValidator {

  /**
   * @see coyote.batch.FrameValidator#process(coyote.batch.TransactionContext)
   */
  @Override
  public boolean process( TransactionContext context ) throws ValidationException {
    context.fireValidationFailed( context, "This is the error message why the validation failed" );
    return false;
  }

}
