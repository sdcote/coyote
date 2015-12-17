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
package coyote.batch.listener;

import java.io.IOException;

import coyote.batch.AbstractConfigurableComponent;
import coyote.batch.ConfigurableComponent;
import coyote.batch.ContextListener;
import coyote.batch.FrameWriter;
import coyote.batch.OperationalContext;
import coyote.batch.TransactionContext;
import coyote.batch.TransformContext;


/**
 * No-op implementation of a listener to assist in cleaner coding of listeners.
 */
public abstract class AbstractListener extends AbstractConfigurableComponent implements ContextListener, ConfigurableComponent {

  /**
   * @see coyote.batch.ContextListener#onError(coyote.batch.OperationalContext)
   */
  @Override
  public void onError( OperationalContext context ) {}




  /**
   * @see coyote.batch.ContextListener#onEnd(coyote.batch.OperationalContext)
   */
  @Override
  public void onEnd( OperationalContext context ) {}




  /**
   * @see coyote.batch.ContextListener#onStart(coyote.batch.OperationalContext)
   */
  @Override
  public void onStart( OperationalContext context ) {}




  /**
   * @see coyote.batch.ContextListener#onRead(coyote.batch.TransactionContext)
   */
  @Override
  public void onRead( TransactionContext context ) {}




  /**
   * @see coyote.batch.ContextListener#onWrite(coyote.batch.TransactionContext, coyote.batch.FrameWriter)
   */
  @Override
  public void onWrite( TransactionContext context, FrameWriter writer ) {}




  /**
   * @see coyote.batch.ContextListener#onValidationFailed(coyote.batch.OperationalContext, java.lang.String)
   */
  @Override
  public void onValidationFailed( OperationalContext context, String msg ) {}




  /**
   * @see coyote.batch.Component#open(coyote.batch.TransformContext)
   */
  @Override
  public void open( TransformContext context ) {
    super.context = context;
  }




  /**
   * @see java.io.Closeable#close()
   */
  @Override
  public void close() throws IOException {}

}
