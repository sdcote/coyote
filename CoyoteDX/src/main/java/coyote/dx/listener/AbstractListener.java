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
package coyote.dx.listener;

import java.io.IOException;

import coyote.dx.AbstractConfigurableComponent;
import coyote.dx.ConfigurableComponent;
import coyote.dx.ContextListener;
import coyote.dx.FrameReader;
import coyote.dx.FrameValidator;
import coyote.dx.FrameWriter;
import coyote.dx.OperationalContext;
import coyote.dx.TransactionContext;
import coyote.dx.TransformContext;


/**
 * No-op implementation of a listener to assist in cleaner coding of listeners.
 */
public abstract class AbstractListener extends AbstractConfigurableComponent implements ContextListener, ConfigurableComponent {

  /**
   * @see coyote.dx.ContextListener#onError(coyote.dx.OperationalContext)
   */
  @Override
  public void onError( OperationalContext context ) {}




  /**
   * @see coyote.dx.ContextListener#onEnd(coyote.dx.OperationalContext)
   */
  @Override
  public void onEnd( OperationalContext context ) {}




  /**
   * @see coyote.dx.ContextListener#onStart(coyote.dx.OperationalContext)
   */
  @Override
  public void onStart( OperationalContext context ) {}




  /**
   * @see coyote.dx.ContextListener#onRead(coyote.dx.TransactionContext, coyote.dx.FrameReader)
   */
  @Override
  public void onRead( TransactionContext context, FrameReader reader ) {}




  /**
   * @see coyote.dx.ContextListener#onWrite(coyote.dx.TransactionContext, coyote.dx.FrameWriter)
   */
  @Override
  public void onWrite( TransactionContext context, FrameWriter writer ) {}




  /**
   * @see coyote.dx.ContextListener#onValidationFailed(coyote.dx.OperationalContext, coyote.dx.FrameValidator, java.lang.String)
   */
  @Override
  public void onValidationFailed( OperationalContext context, FrameValidator validator, String msg ) {}




  /**
   * @see coyote.dx.Component#open(coyote.dx.TransformContext)
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




  /**
   * @see coyote.dx.ContextListener#onFrameValidationFailed(coyote.dx.TransactionContext)
   */
  @Override
  public void onFrameValidationFailed( TransactionContext context ) {}

}
