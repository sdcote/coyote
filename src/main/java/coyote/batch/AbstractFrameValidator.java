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
package coyote.batch;

import java.io.IOException;


/**
 * 
 */
public abstract class AbstractFrameValidator extends AbstractConfigurableComponent implements FrameValidator, ConfigurableComponent {

  /**
   * @see coyote.batch.Component#open(coyote.batch.TransformContext)
   */
  @Override
  public void open( TransformContext context ) {}




  /**
   * @see java.io.Closeable#close()
   */
  @Override
  public void close() throws IOException {}

}
