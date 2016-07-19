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

import coyote.batch.Batch;
import coyote.batch.ConfigurableComponent;
import coyote.batch.FrameWriter;
import coyote.batch.eval.EvaluationException;
import coyote.commons.StringUtil;
import coyote.dataframe.DataFrame;
import coyote.loader.log.Log;
import coyote.loader.log.LogMsg;


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

    // If there is a conditional expression
    if ( expression != null ) {

      try {
        // if the condition evaluates to true...
        if ( evaluator.evaluateBoolean( expression ) ) {
          writeFrame( frame );
        }
      } catch ( final IllegalArgumentException e ) {
        Log.warn( LogMsg.createMsg( Batch.MSG, "Writer.boolean_evaluation_error", expression, e.getMessage() ) );
      }
    } else {
      // Unconditionally writing frame
      writeFrame( frame );
    }

  }




  /**
   * This is where we actually write the frame.
   * 
   * @param frame the frame to be written
   */
  private void writeFrame( final DataFrame frame ) {

    printwriter.write( frame.toString() );
    printwriter.write( StringUtil.LINE_FEED );
    printwriter.flush();

    // Increment the row number
    rowNumber++;

  }

}
