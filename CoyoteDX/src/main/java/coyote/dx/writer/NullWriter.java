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
package coyote.dx.writer;

import coyote.dataframe.DataFrame;
import coyote.dx.TransformContext;


/**
 * A do-nothing implementation of a writer useful for testing
 */
public class NullWriter extends AbstractFrameWriter {

  /**
   * @see coyote.dx.FrameWriter#write(coyote.dataframe.DataFrame)
   */
  @Override
  public void write( DataFrame frame ) {}




  /**
   * @see coyote.dx.writer.AbstractFrameWriter#open(coyote.dx.TransformContext)
   */
  @Override
  public void open( TransformContext context ) {}

}
