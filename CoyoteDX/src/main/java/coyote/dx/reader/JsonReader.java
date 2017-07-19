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
package coyote.dx.reader;

import coyote.dataframe.DataFrame;
import coyote.dx.ConfigurableComponent;
import coyote.dx.FrameReader;
import coyote.dx.context.TransactionContext;

/**
 * 
 */
public class JsonReader extends AbstractFrameReader implements FrameReader, ConfigurableComponent {

  /**
   * @see coyote.dx.FrameReader#read(coyote.dx.context.TransactionContext)
   */
  @Override
  public DataFrame read( TransactionContext context ) {
    return null;
  }




  /**
   * @see coyote.dx.FrameReader#eof()
   */
  @Override
  public boolean eof() {
    return true;
  }

}
