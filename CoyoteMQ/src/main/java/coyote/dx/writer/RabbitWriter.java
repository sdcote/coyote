/*
 * Copyright (c) 2017 Stephan D. Cote' - All rights reserved.
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

import java.io.IOException;

import coyote.dataframe.DataFrame;
import coyote.dx.ConfigurableComponent;
import coyote.dx.FrameWriter;
import coyote.dx.context.TransformContext;


/**
 * 
 */
public class RabbitWriter extends AbstractFrameWriter implements FrameWriter, ConfigurableComponent {

  /**
   * @see coyote.dx.writer.AbstractFrameWriter#open(coyote.dx.context.TransformContext)
   */
  @Override
  public void open( TransformContext context ) {
    // TODO Auto-generated method stub
    super.open( context );
  }




  /**
   * @see coyote.dx.FrameWriter#write(coyote.dataframe.DataFrame)
   */
  @Override
  public void write( DataFrame frame ) {
    // TODO Auto-generated method stub

  }




  /**
   * @see coyote.dx.writer.AbstractFrameWriter#close()
   */
  @Override
  public void close() throws IOException {
    // TODO Auto-generated method stub
    super.close();
  }

}
