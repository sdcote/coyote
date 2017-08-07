/*
 * Copyright (c) 2015 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.dx.listener;

import java.io.IOException;

import coyote.dx.AbstractConfigurableComponent;
import coyote.dx.ConfigurableComponent;
import coyote.dx.FrameReader;
import coyote.dx.FrameValidator;
import coyote.dx.FrameWriter;
import coyote.dx.context.ContextListener;
import coyote.dx.context.OperationalContext;
import coyote.dx.context.TransactionContext;
import coyote.dx.context.TransformContext;


/**
 * No-op implementation of a listener to assist in cleaner coding of listeners.
 */
public abstract class AbstractListener extends AbstractConfigurableComponent implements ContextListener, ConfigurableComponent {

  /**
   * @see coyote.dx.context.ContextListener#onError(coyote.dx.context.OperationalContext)
   */
  @Override
  public void onError(OperationalContext context) {
    // listeners should override this method to recieve error notifications
  }




  /**
   * @see coyote.dx.context.ContextListener#onEnd(coyote.dx.context.OperationalContext)
   */
  @Override
  public void onEnd(OperationalContext context) {
    // listeners should override this method to perform processing when the transform or context ends
  }




  /**
   * @see coyote.dx.context.ContextListener#onStart(coyote.dx.context.OperationalContext)
   */
  @Override
  public void onStart(OperationalContext context) {
    // listeners should override this method to perform processing before the transform or transaction starts
  }




  /**
   * @see coyote.dx.context.ContextListener#onRead(coyote.dx.context.TransactionContext, coyote.dx.FrameReader)
   */
  @Override
  public void onRead(TransactionContext context, FrameReader reader) {
    // listeners should override this method to perform processing related to reads
  }




  /**
   * @see coyote.dx.context.ContextListener#onWrite(coyote.dx.context.TransactionContext, coyote.dx.FrameWriter)
   */
  @Override
  public void onWrite(TransactionContext context, FrameWriter writer) {
    // listeners should override this method to perform processing related to write
  }




  /**
   * @see coyote.dx.context.ContextListener#onValidationFailed(coyote.dx.context.OperationalContext, coyote.dx.FrameValidator, java.lang.String)
   */
  @Override
  public void onValidationFailed(OperationalContext context, FrameValidator validator, String msg) {
    // listeners should override this method to perform processing when a field fails validations
  }




  /**
   * @see coyote.dx.Component#open(coyote.dx.context.TransformContext)
   */
  @Override
  public void open(TransformContext context) {
    super.context = context;
  }




  /**
   * @see java.io.Closeable#close()
   */
  @Override
  public void close() throws IOException {
    // listeners should perform their clean-up processing here
  }




  /**
   * @see coyote.dx.context.ContextListener#onFrameValidationFailed(coyote.dx.context.TransactionContext)
   */
  @Override
  public void onFrameValidationFailed(TransactionContext context) {
    // override this method to perform processing when the entire frame fails validation
  }

}
