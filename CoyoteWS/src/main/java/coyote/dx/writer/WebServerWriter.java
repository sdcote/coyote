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
import coyote.dx.AbstractConfigurableComponent;
import coyote.dx.ConfigurableComponent;
import coyote.dx.FrameWriter;
import coyote.dx.TransformContext;


/**
 * WebServerWriters are writers which generate responses to requests generated 
 * by WebServerReaders.
 * 
 * <p>Requests are queued up by a web server at the reader and passed through 
 * the pipeline where it reaches this writer. This writer then looks in the 
 * transaction context for a future object the reader (actually the request 
 * thread) is blocked on for a response.
 */
public class WebServerWriter extends AbstractConfigurableComponent implements FrameWriter, ConfigurableComponent {

  @Override
  public void open( TransformContext context ) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void close() throws IOException {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void write( DataFrame frame ) {
    // TODO Auto-generated method stub
    
  }

}
