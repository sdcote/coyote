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

import coyote.dataframe.DataFrame;
import coyote.dx.TransactionContext;
import coyote.dx.TransformContext;


/**
 * A do-nothing implementation of a reader, useful for testing.
 */
public class NullReader extends AbstractFrameReader {

  /**
   * @see coyote.dx.FrameReader#read(coyote.dx.TransactionContext)
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




  /**
   * @see coyote.dx.Component#open(coyote.dx.TransformContext)
   */
  @Override
  public void open( TransformContext context ) {}

}
