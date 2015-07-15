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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import coyote.batch.FrameValidator;
import coyote.batch.TransactionContext;
import coyote.batch.TransformContext;
import coyote.batch.ValidationException;


/**
 * There must be a value, even if it is an empty string or all whitespace.
 */
public class NotNull extends AbstractValidator implements FrameValidator {

  

  

  /**
   * @see coyote.batch.FrameValidator#process(coyote.batch.TransactionContext)
   */
  @Override
  public boolean process( TransactionContext context ) throws ValidationException {
    
    
    return true;
  }


}
