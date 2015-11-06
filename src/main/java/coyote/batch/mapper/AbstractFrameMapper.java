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
package coyote.batch.mapper;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import coyote.batch.AbstractConfigurableComponent;
import coyote.batch.Component;
import coyote.batch.TransformContext;


/**
 * 
 */
public abstract class AbstractFrameMapper extends AbstractConfigurableComponent implements Component {


  Map<String, String> fieldMap = new LinkedHashMap<String, String>();




  /**
   * @see coyote.batch.Component#getContext()
   */
  @Override
  public TransformContext getContext() {
    return context;
  }




  /**
   * @see coyote.batch.Component#open(coyote.batch.TransformContext)
   */
  @Override
  public void open( TransformContext context ) {
    this.context = context;

  }




  /**
   * @see java.io.Closeable#close()
   */
  @Override
  public void close() throws IOException {

  }
}
