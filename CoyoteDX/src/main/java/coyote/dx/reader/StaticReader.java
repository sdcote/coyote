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
import coyote.dx.context.TransactionContext;
import coyote.dx.context.TransformContext;


/**
 * A reader which creates a single static message, useful for testing, to 
 * insert data into databases, and to publish messages on topics and queues.
 */
public class StaticReader extends AbstractFrameReader {
  private int counter = 0;
  private int limit = 1;




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
    return counter >= limit;
  }




  /**
   * @see coyote.dx.Component#open(coyote.dx.context.TransformContext)
   */
  @Override
  public void open( TransformContext context ) {}

}
