/*
 * Copyright (c) 2015 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.dx.writer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import coyote.commons.StringUtil;
import coyote.dataframe.DataField;
import coyote.dataframe.DataFrame;
import coyote.dx.CDX;
import coyote.dx.ConfigTag;
import coyote.dx.ConfigurableComponent;
import coyote.dx.FieldDefinition;
import coyote.dx.FrameWriter;
import coyote.dx.context.TransformContext;
import coyote.loader.log.Log;
import coyote.loader.log.LogMsg;


/**
 * 
 */
public class FlatFileWriter extends AbstractFrameFileWriter implements FrameWriter, ConfigurableComponent {

  /** The list of fields we are to write in the order they are to be written */
  List<FieldDefinition> fields = new ArrayList<FieldDefinition>();

  private final char padChar = ' ';
  private int recordLength = 0;




  /**
   * @see coyote.dx.writer.AbstractFrameFileWriter#open(coyote.dx.context.TransformContext)
   */
  @Override
  public void open(final TransformContext context) {
    super.open(context);

    // Now setup our field definitions
    final DataFrame fieldcfg = getFrame(ConfigTag.FIELDS);
    if (fieldcfg != null) {
      boolean trim = false;// flag to trim values

      // position of the last record
      int last = 0;

      for (final DataField field : fieldcfg.getFields()) {
        try {
          final DataFrame fielddef = (DataFrame)field.getObjectValue();

          // determine if values should be trimmed for this field
          try {
            trim = fielddef.getAsBoolean(ConfigTag.TRIM);
          } catch (final Exception e) {
            trim = false;
          }

          int alignment = 0; // left alignment
          final String align = fielddef.getAsString(ConfigTag.ALIGN);
          if (StringUtil.isNotBlank(align)) {
            if (align.startsWith("L") || align.startsWith("l")) {
              alignment = 0;
            } else if (align.startsWith("R") || align.startsWith("r")) {
              alignment = 2;
            } else if (align.startsWith("C") || align.startsWith("c")) {
              alignment = 1;
            } else {
              Log.warn(LogMsg.createMsg(CDX.MSG, "Writer.Unrecognized {%s} configuration value of '{%s}' - defaulting to 'left' alignment", ConfigTag.ALIGN, align));
            }
          }

          fields.add(new FieldDefinition(field.getName(), fielddef.getAsInt(ConfigTag.START), fielddef.getAsInt(ConfigTag.LENGTH), fielddef.getAsString(ConfigTag.TYPE), fielddef.getAsString(ConfigTag.FORMAT), trim, alignment));

          // see how long the record is to be by keeping track of the longest 
          // start position and then adding the field length
          if (fielddef.getAsInt(ConfigTag.START) > last) {
            last = fielddef.getAsInt(ConfigTag.START);
            recordLength = last + fielddef.getAsInt(ConfigTag.LENGTH);
          }
        } catch (final Exception e) {
          context.setError("Problems loading field definition '" + field.getName() + "' - " + e.getClass().getSimpleName() + " : " + e.getMessage());
          return;
        }
      }

      Log.debug(LogMsg.createMsg(CDX.MSG, "Writer.flat_file_opened", fields.size(), recordLength));
    } else {
      context.setError("There are no fields configured in the writer");
      return;
    }

  }




  /**
   * @see coyote.dx.writer.AbstractFrameFileWriter#write(coyote.dataframe.DataFrame)
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
    final StringBuilder line = new StringBuilder(recordLength);
    for (int i = 0; i < recordLength; i++) {
      line.append(padChar);
    }

    for (final FieldDefinition def : fields) {

      // get the field from the frame with the name in the definition
      final DataField field = frame.getField(def.getName());

      if (field != null) {

        // format it according to the format in the definition
        String fieldText = null;

        if (def.hasFormatter()) {
          fieldText = def.getFormattedValue(field);
        } else {
          fieldText = field.getStringValue();
        }

        final String text = StringUtil.fixedLength(fieldText, def.getLength(), def.getAlignment(), padChar);

        // now insert
        line.insert(def.getStart(), text);
      } else {
        Log.trace(LogMsg.createMsg(CDX.MSG, "Writer.No field named '{%s}' in frame.", def.getName()));
      }

    }

    // Inserts cause other characters to move to the right, therefore we will 
    // need to truncate the string to the requested length
    line.delete(recordLength, line.length());

    // write to line to the file
    printwriter.write(line.toString());
    printwriter.write(StringUtil.LINE_FEED);
    printwriter.flush();

    // Increment the row number
    rowNumber++;

  }




  /**
   * @see coyote.dx.writer.AbstractFrameFileWriter#close()
   */
  @Override
  public void close() throws IOException {
    fields.clear();
    super.close();
  }

}
