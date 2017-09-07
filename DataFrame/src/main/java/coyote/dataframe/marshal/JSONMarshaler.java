/*
 * Copyright (c) 2014 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.dataframe.marshal;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.List;

import coyote.dataframe.DataField;
import coyote.dataframe.DataFrame;
import coyote.dataframe.marshal.json.JsonFrameParser;
import coyote.dataframe.marshal.json.JsonWriter;
import coyote.dataframe.marshal.json.JsonWriterConfig;


/**
 * 
 */
public class JSONMarshaler {
  private static final String NULL = "null";
  private static final String TRUE = "true";
  private static final String FALSE = "false";




  /**
   * Marshal the given JSON into a dataframe.
   * 
   * @param json
   * 
   * @return Data frame containing the JSON represented data
   */
  public static List<DataFrame> marshal(final String json) throws MarshalException {
    List<DataFrame> retval = null;

    try {
      retval = new JsonFrameParser(json).parse();
    } catch (final Exception e) {
      throw new MarshalException("Could not marshal JSON to DataFrame: " + e.getMessage(), e);
    }

    return retval;
  }




  /**
   * Generate a JSON string from the given data frame.
   * 
   * @param frame The frame to marshal
   * 
   * @return A JSON formatted string which can be marshaled back into a frame
   */
  public static String marshal(final DataFrame frame) {
    return write(frame, JsonWriterConfig.MINIMAL);
  }




  /**
   * Generate a nicely formatted (and indented) JSON string from the given data frame.
   * 
   * @param frame The frame to marshal
   * 
   * @return A JSON formatted string which can be marshaled back into a frame
   */
  public static String toFormattedString(final DataFrame frame) {
    return write(frame, JsonWriterConfig.FORMATTED);
  }




  /**
   * @param frame
   * @param config
   * 
   * @return the string containing the marshaled data 
   */
  private static String write(final DataFrame frame, final JsonWriterConfig config) {

    // create string writer
    final StringWriter sw = new StringWriter();
    final BufferedWriter bw = new BufferedWriter(sw);
    final JsonWriter writer = config.createWriter(bw);

    try {
      writeFrame(frame, writer);
      bw.flush();
    } catch (IOException e) {
      return "[\"" + e.getMessage() + "\"]";
    }
    return sw.getBuffer().toString();
  }




  /**
   * 
   * @param frame
   * @param config
   * 
   * @throws IOException 
   */
  private static void writeFrame(final DataFrame frame, final JsonWriter writer) throws IOException {

    if (frame != null && writer != null) {
      if (frame.size() > 0) {
        boolean isArray = frame.isArray();
        if (isArray)
          writer.writeArrayOpen();
        else
          writer.writeObjectOpen();

        DataField field = null;
        for (int i = 0; i < frame.size(); i++) {
          field = frame.getField(i);

          if (!isArray) {
            if (field.getName() != null) {
              writer.writeMemberName(field.getName());
            } else {
              writer.writeString("");
            }
            writer.writeMemberSeparator();
          }

          if (field.getType() == DataField.UDEF) {
            writer.writeLiteral(NULL);
          } else if (field.getType() == DataField.BOOLEANTYPE) {
            if (TRUE.equalsIgnoreCase(field.getStringValue())) {
              writer.writeLiteral(TRUE);
            } else {
              writer.writeLiteral(FALSE);
            }
          } else if (field.isNumeric()) {
            writer.writeNumber(field.getStringValue());
          } else if (field.isArray()) {
            Object obj = field.getObjectValue();
            if (obj instanceof DataFrame) {
              writeFrame((DataFrame)obj, writer);
            } else {
              writer.writeArray(obj);
            }
          } else if (field.getType() == DataField.FRAMETYPE) {
            DataFrame dfm = (DataFrame)field.getObjectValue();
            if (dfm == null) {
              writer.writeEmptyArray();
            } else {
              writeFrame(dfm, writer);
            }
          } else {
            Object obj = field.getObjectValue();
            if (obj != null) {
              writer.writeString(obj.toString());
            } else {
              writer.writeLiteral(NULL);
            }
          }
          if (i + 1 < frame.size()) {
            writer.writeObjectSeparator();
          }
        }

        if (isArray)
          writer.writeArrayClose();
        else
          writer.writeObjectClose();

      } else {
        if (frame.isArrayBiased()) {
          writer.writeArrayOpen();
          writer.writeArrayClose();
        } else {
          writer.writeObjectOpen();
          writer.writeObjectClose();
        }
      }
    }
  }

}
