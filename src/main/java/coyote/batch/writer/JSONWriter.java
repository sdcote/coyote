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

import coyote.batch.ConfigurableComponent;
import coyote.batch.FrameWriter;
import coyote.commons.StringUtil;
import coyote.dataframe.DataFrame;


/**
 * Writes a data frame as a simple JSON string to either standard output 
 * (default) or standard error.
 */
public class JSONWriter extends AbstractFrameWriter implements FrameWriter, ConfigurableComponent {

  /**
   * @see coyote.batch.writer.AbstractFrameWriter#write(coyote.dataframe.DataFrame)
   */
  @Override
  public void write( final DataFrame frame ) {

    printwriter.write( frame.toString() );
    printwriter.write( StringUtil.LINE_FEED );
    printwriter.flush();

    // Increment the row number
    rowNumber++;

  }

}
