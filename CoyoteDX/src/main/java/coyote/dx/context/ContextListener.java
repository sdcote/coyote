/*
 * Copyright (c) 2015 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.dx.context;

import coyote.dataframe.DataFrame;
import coyote.dx.ConfigurableComponent;
import coyote.dx.FrameReader;
import coyote.dx.FrameValidator;
import coyote.dx.FrameWriter;


/**
 * Listens to the operational context and performs actions on different events.
 */
public interface ContextListener extends ConfigurableComponent {

  /**
   * Called when either the Transaction or the Transform Context ends.
   * 
   * <p>The listener should use reflection (instanceof) to check if the 
   * context is a transaction or the transform. If the context is an instance 
   * of TransformContext, the engine is about to terminate.
   * 
   * @param context the context which is ending
   */
  void onEnd(OperationalContext context);




  /**
   * Called when either the Transaction or the Transform Context is starting.
   * 
   * <p>The listener should use reflection (instanceof) to check if the 
   * context is a transaction or the transform. If the context is an instance 
   * of TransformContext, the engine is about to start reading.
   * 
   * @param context the context which is starting
   */
  void onStart(OperationalContext context);




  /**
   * Called just after a frame has been written by a writer.
   * 
   * <p>If there a multiple writers, this method will be called multiple times 
   * for each transaction. This will be called even if the writer is disabled 
   * or does not run based on its set condition. This method will be called 
   * for every writer defined in the configuration. 
   * 
   * @param context the transaction context
   * @param writer the writer which just called to write a frame
   */
  void onWrite(TransactionContext context, FrameWriter writer);




  /**
   * Called just after a frame has been read by a reader.
   * 
   * @param context the transaction context
   * @param reader the reader doing the reading
   */
  void onRead(TransactionContext context, FrameReader reader);




  /**
   * Called when either the transaction or transform context is in error.
   * 
   * <p>The listener should use reflection (instanceof) to check if the 
   * context is a transaction or the transform. 
   * 
   * @param context the context which has been set to error.
   */
  void onError(OperationalContext context);




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
  void onValidationFailed(OperationalContext context, FrameValidator validator, String msg);




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
  void onFrameValidationFailed(TransactionContext context);




  /**
   * Called just after a successful mapping of the working frame to the
   * target frame.
   * 
   * <p>Note: this will not be called if the mapping failed for any reason.
   * 
   * @param txnContext the transaction context containing the data frames.
   */
  void onMap(TransactionContext txnContext);




  /**
   * Preload the listener with historic data.
   * 
   * <p>Some listeners may require prior frames to determine how to operate. 
   * An example of this is a listener which generates an event when frame 
   * values breach an averaged threshold. The generated average will be more 
   * accurate if the listener has the prior values before the first frame is 
   * read in by the reader. This method allows the listener to preload these 
   * historic frames if they are provided.
   * 
   * <p>Oldest frames are expected to be loaded first.
   * 
   * @param frame the historic frame to preload
   */
  public void preload(DataFrame frame);

}
