/*
 * Copyright (c) 2019 Stephan D. Cote' - All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the MIT License which accompanies this distribution, and is
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.dx.writer;

import coyote.commons.StringUtil;
import coyote.dataframe.DataField;
import coyote.dataframe.DataFrame;
import coyote.dx.CDX;
import coyote.dx.ConfigurableComponent;
import coyote.dx.FrameWriter;
import coyote.loader.log.Log;
import coyote.loader.log.LogMsg;


/**
 * Writes a data frame as a simple name-value pair string to either standard output (default) or standard error.
 *
 * <p>The name of the pair will be the value of the "name" field in the given dataframe and the value of the pair is
 * the value of the "value" field. In simpler terms, the given frame must contain a "name" field and a "value" field.
 * This implies that each received dataframe will result in the creation of one key-value pair.</p>
 */
public class PropertyWriter extends AbstractFrameFileWriter implements FrameWriter, ConfigurableComponent {
  private static final String NAME = "name";
  private static final String VALUE = "value";

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
    DataField nameField = frame.getFieldIgnoreCase(NAME);
    DataField valueField = frame.getFieldIgnoreCase(VALUE);
    if (nameField != null && valueField != null) {
      printwriter.write(nameField.getStringValue());
      printwriter.write(" = ");
      printwriter.write(valueField.getStringValue());
      printwriter.write(StringUtil.LINE_FEED);
      printwriter.flush();
    } else {
      Log.warn("did not write property, both 'name' and 'value' field are required:\nName: " + nameField + "\nValue:" + valueField);
    }
    // Increment the row number
    rowNumber++;
  }

}
