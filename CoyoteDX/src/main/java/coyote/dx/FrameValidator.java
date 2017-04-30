/*
 * Copyright (c) 2015 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 *
 * Contributors:
 *   Stephan D. Cote 
 *      - Initial concept and implementation
 */
package coyote.dx;

import coyote.dx.validate.ValidationException;


/**
 * 
 */
public interface FrameValidator extends ConfigurableComponent {

  boolean process( TransactionContext context ) throws ValidationException;




  /**
   * @return the name of the file this validator checks
   */
  String getFieldName();




  /**
   * @return the (short) description of this validator
   */
  String getDescription();

}
