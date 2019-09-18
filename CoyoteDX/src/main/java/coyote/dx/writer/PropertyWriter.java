/*
 * Copyright (c) 2015 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.dx.writer;

import coyote.commons.StringUtil;
import coyote.dataframe.DataFrame;
import coyote.dataframe.marshal.JSONMarshaler;
import coyote.dx.CDX;
import coyote.dx.ConfigurableComponent;
import coyote.dx.FrameWriter;
import coyote.loader.log.Log;
import coyote.loader.log.LogMsg;


/**
 * Writes a data frame as a simple name-value pair string to either standard output (default) or standard error.
 */
public class PropertyWriter extends AbstractFrameFileWriter implements FrameWriter, ConfigurableComponent {

  /**
   * @see AbstractFrameFileWriter#write(DataFrame)
   */
  @Override
  public void write(final DataFrame frame) {

    // If there is a conditional expression
    if (expression != null) {

      try {
        // if the condition evaluates to true...
        if (evaluator.evaluateBoolean(expression)) {
          writeFrame(frame);
        }
      } catch (final IllegalArgumentException e) {
        Log.warn(LogMsg.createMsg(CDX.MSG, "Writer.boolean_evaluation_error", expression, e.getMessage()));
      }
    } else {
      // Unconditionally writing frame
      writeFrame(frame);
    }

  }




  /**
   * This is where we actually write the frame.
   * 
   * @param frame the frame to be written
   */
  private void writeFrame(final DataFrame frame) {

    printwriter.write(JSONMarshaler.toFormattedString(frame));
    printwriter.write(StringUtil.LINE_FEED);
    printwriter.flush();

    // Increment the row number
    rowNumber++;

  }

}
