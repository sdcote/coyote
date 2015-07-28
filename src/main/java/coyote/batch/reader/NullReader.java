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
package coyote.batch.reader;

import coyote.batch.TransactionContext;
import coyote.batch.TransformContext;
import coyote.dataframe.DataFrame;


/**
 * A do-nothing implementation of a reader, useful for testing.
 */
public class NullReader extends AbstractFrameReader {

  /**
   * @see coyote.batch.FrameReader#read(coyote.batch.TransactionContext)
   */
  @Override
  public DataFrame read( TransactionContext context ) {
    return null;
  }




  /**
   * @see coyote.batch.FrameReader#eof()
   */
  @Override
  public boolean eof() {
    return true;
  }




  /**
   * @see coyote.batch.Component#open(coyote.batch.TransformContext)
   */
  @Override
  public void open( TransformContext context ) {}

}
