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
package coyote.batch.writer;

import coyote.dataframe.DataFrame;


/**
 * A do-nothing implementation of a writer useful for testing
 */
public class NullWriter extends AbstractFrameWriter {

  /**
   * @see coyote.batch.FrameWriter#write(coyote.dataframe.DataFrame)
   */
  @Override
  public void write( DataFrame frame ) {}

}
