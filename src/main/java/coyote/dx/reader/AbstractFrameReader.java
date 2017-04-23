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
package coyote.dx.reader;

import java.io.IOException;

import coyote.dx.AbstractConfigurableComponent;
import coyote.dx.ConfigurableComponent;
import coyote.dx.FrameReader;
import coyote.dx.TransformContext;


/**
 * 
 */
public abstract class AbstractFrameReader extends AbstractConfigurableComponent implements FrameReader, ConfigurableComponent {

  /**
   * @see coyote.dx.Component#open(coyote.dx.TransformContext)
   */
  @Override
  public void open( final TransformContext context ) {
    super.context = context;
  }




  /**
   * @see coyote.dx.Component#getContext()
   */
  @Override
  public TransformContext getContext() {
    return context;
  }




  /**
   * @see java.io.Closeable#close()
   */
  @Override
  public void close() throws IOException {}

}
