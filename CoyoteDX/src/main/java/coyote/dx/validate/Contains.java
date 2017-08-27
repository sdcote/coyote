/*
 * Copyright (c) 2017 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.dx.validate;

import coyote.dx.context.TransactionContext;
import coyote.dx.context.TransformContext;


/**
 * Configured thusly:<pre>
 * "Contains": { "field" : "Role", "values" : [ "USER", "ADMIN", "GUEST" ] }</pre>
 */
public class Contains extends AbstractValidator {

  /**
   * @see coyote.dx.validate.AbstractValidator#open(coyote.dx.context.TransformContext)
   */
  @Override
  public void open(TransformContext context) {}




  /**
   * @see coyote.dx.FrameValidator#process(coyote.dx.context.TransactionContext)
   */
  @Override
  public boolean process(TransactionContext context) throws ValidationException {
    // TODO Auto-generated method stub
    return false;
  }

}
