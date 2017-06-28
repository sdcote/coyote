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
package coyote.dx.context;

import coyote.dx.ConfigurableComponent;
import coyote.dx.FrameReader;
import coyote.dx.FrameValidator;
import coyote.dx.FrameWriter;

/**
 * Listens to the operational context an performs actions on different events.
 */
public interface ContextListener extends ConfigurableComponent {

  void onEnd( OperationalContext context );




  void onStart( OperationalContext context );




  /**
   * Called just after a frame has been written by a writer.
   * 
   * @param context the transaction context
   * @param writer the writer which just called to write a frame
   */
  void onWrite( TransactionContext context, FrameWriter writer );




  /**
   * Called just after a frame has been read by a reader.
   * 
   * @param context the transaction context
   * @param reader the reader doing the reading
   */
  void onRead( TransactionContext context, FrameReader reader );




  void onError( OperationalContext context );




  /**
   * Event indicating validation failed in the given context.
   * 
   * <p>Additional details can be pulled from the validator and the context 
   * for very precise reporting on what happened where and why.
   * 
   * @param context The context in which the validation failed
   * @param validator The frame validator generating the event
   * @param msg error message indicating why the validation failed.
   * 
   * @see #onFrameValidationFailed(TransactionContext)
   */
  void onValidationFailed( OperationalContext context, FrameValidator validator, String msg );




  /**
   * Event indicating the working frame in the given context has failed one or 
   * more validations.
   * 
   * <p>To track which validations failed, implements the {@link 
   * #onValidationFailed(OperationalContext, FrameValidator, String)} method to
   * capture which validations failed.</p>
   * 
   * @param context the transaction context containing the failed working frame
   * 
   * @see #onValidationFailed(OperationalContext, FrameValidator, String)
   */
  void onFrameValidationFailed( TransactionContext context );

}
