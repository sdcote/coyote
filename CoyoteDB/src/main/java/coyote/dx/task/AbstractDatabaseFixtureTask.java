/*
 * Copyright (c) 2017 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.dx.task;

import coyote.dx.FrameReader;
import coyote.dx.FrameValidator;
import coyote.dx.FrameWriter;
import coyote.dx.context.ContextListener;
import coyote.dx.context.OperationalContext;
import coyote.dx.context.TransactionContext;
import coyote.dx.context.TransformContext;


/**
 * 
 */
public class AbstractDatabaseFixtureTask extends AbstractTransformTask implements ContextListener {
  
  

  /**
   * @see coyote.dx.task.AbstractTransformTask#open(coyote.dx.context.TransformContext)
   */
  @Override
  public void open(TransformContext context) {
    super.open(context);
    context.addListener(this);
  }




  /**
   * @see coyote.dx.context.ContextListener#onEnd(coyote.dx.context.OperationalContext)
   */
  @Override
  public void onEnd(OperationalContext context) {
    // no-op
  }




  /**
   * @see coyote.dx.context.ContextListener#onStart(coyote.dx.context.OperationalContext)
   */
  @Override
  public void onStart(OperationalContext context) {
    // no-op
  }




  /**
   * @see coyote.dx.context.ContextListener#onWrite(coyote.dx.context.TransactionContext, coyote.dx.FrameWriter)
   */
  @Override
  public void onWrite(TransactionContext context, FrameWriter writer) {
    // no-op
  }




  /**
   * @see coyote.dx.context.ContextListener#onRead(coyote.dx.context.TransactionContext, coyote.dx.FrameReader)
   */
  @Override
  public void onRead(TransactionContext context, FrameReader reader) {
    // no-op
  }




  /**
   * @see coyote.dx.context.ContextListener#onError(coyote.dx.context.OperationalContext)
   */
  @Override
  public void onError(OperationalContext context) {
    // no-op
  }




  /**
   * @see coyote.dx.context.ContextListener#onValidationFailed(coyote.dx.context.OperationalContext, coyote.dx.FrameValidator, java.lang.String)
   */
  @Override
  public void onValidationFailed(OperationalContext context, FrameValidator validator, String msg) {
    // no-op
  }




  /**
   * @see coyote.dx.context.ContextListener#onFrameValidationFailed(coyote.dx.context.TransactionContext)
   */
  @Override
  public void onFrameValidationFailed(TransactionContext context) {
    // no-op
  }

}
