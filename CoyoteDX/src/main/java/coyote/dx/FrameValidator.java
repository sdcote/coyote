/*
 * Copyright (c) 2015 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.dx;

import coyote.dx.context.TransactionContext;
import coyote.dx.validate.ValidationException;


/**
 * Place the transaction context in error if the contained working frame is 
 * invalid.
 */
public interface FrameValidator extends ConfigurableComponent {

  /**
   * Process the given transaction context
   * 
   * <p>FrameValidators are encouraged to call 
   * {@link coyote.dx.context.ContextListener#onValidationFailed(coyote.dx.context.OperationalContext, coyote.dx.FrameValidator, java.lang.String)} 
   * to inform other components in the data transfer job of the invalid data 
   * in the context with a detailed message indicating the exact failure. This
   * function allows the validator to be very specific as to the cause of the 
   * error, providing the exact values causing the failure.
   * 
   * <p>FrameValidators may chose to put the context in error, aborting the 
   * rest of processing for this transaction. Additionally, the validator may 
   * set the entire transform context in error to abort the job before the 
   * next record is read. The latter case is drastic and not expected to be 
   * the normal mode of operation.
   * 
   * @param context The transaction context containing the working frame to check.
   * 
   * @return true if the working frame in the transaction context passes 
   *         validation, false if the data is not valid.
   *
   * @throws ValidationException to place the transaction context in error and 
   *         halt processing of the record.
   */
  boolean process(TransactionContext context) throws ValidationException;




  /**
   * Allow components to know what field this component is validating.
   * 
   * @return the name of the field this validator checks
   */
  String getFieldName();




  /**
   * Return the description of the validation being performed.
   * 
   * @return the (short) description of this validator
   */
  String getDescription();

}
