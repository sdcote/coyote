/*
 * Copyright (c) 2015 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.dataframe.marshal;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import coyote.dataframe.DataField;
import coyote.dataframe.DataFrame;
import coyote.dataframe.marshal.xml.XmlFrameParser;
import coyote.dataframe.marshal.xml.XmlWriter;
import coyote.dataframe.marshal.xml.XmlWriterConfig;


/**
 * 
 */
public class XMLMarshaler {

  /**
   * Marshal the given XML into a dataframe.
   * 
   * @param xml
   * 
   * @return Data frame containing the XML represented data
   */
  public static List<DataFrame> marshal(final String xml) throws MarshalException {
    List<DataFrame> retval = new ArrayList<DataFrame>();
    DataFrame frame = null;
    try {
      frame = new XmlFrameParser(xml).parse();
      if (frame != null) {
        retval.add(frame);
      }
    } catch (final Exception e) {
      throw new MarshalException("Could not marshal XML to DataFrame", e);
    }

    return retval;
  }




  /**
   * Generate a XML string from the given data frame.
   * 
   * @param frame The frame to marshal
   * 
   * @return A XML formatted string which can be marshaled back into a frame
   */
  public static String marshal(final DataFrame frame) {
    return write(frame, XmlWriterConfig.MINIMAL);
  }




  /**
   * Generate a nicely formatted (and indented) XML string from the given data frame.
   * 
   * @param frame The frame to marshal
   * 
   * @return A XML formatted string which can be marshaled back into a frame
   */
  public static String toFormattedString(final DataFrame frame) {
    return write(frame, XmlWriterConfig.FORMATTED);
  }




  /**
   * Generate minimal XML (single line, no whitespace) with datatype information 
   * for each field.
   * 
   * @param frame The frame to marshal
   * 
   * @return A XML string with minimal formating and data type information which can be marshaled back into a frame preserving data types
   */
  public static String toTypedString(final DataFrame frame) {
    return write(frame, XmlWriterConfig.TYPED);
  }




  /**
   * Generate a nicely formatted (and indented) XML string with datatype 
   * information for each field.
   * 
   * @param frame The frame to marshal
   * 
   * @return A formatted XML string with  data type information which can be marshaled back into a frame preserving data types
   */
  public static String toFormattedTypedString(final DataFrame frame) {
    return write(frame, XmlWriterConfig.TYPED_FORMATTED);
  }




  /**
   * Write the given frame using the given XML writer configuration
   *  
   * @param frame the frame to write
   * @param config the configuration with the settings to direct formatting
   * 
   * @return the string containing the marshaled data 
   */
  private static String write(final DataFrame frame, final XmlWriterConfig config) {

    // create string writer
    final StringWriter sw = new StringWriter();
    final BufferedWriter bw = new BufferedWriter(sw);
    final XmlWriter writer = config.createWriter(bw);

    try {
      writeFrame(frame, writer);
      bw.flush();
    } catch (IOException e) {
      return "<error>" + e.getMessage() + "</error>";
    }
    return sw.getBuffer().toString();
  }




  /**
   * Recursive function to handle the writing of frames with an XML writer.
   * 
   * @param frame the data frame to write
   * @param writer the writer of frames
   * 
   * @throws IOException if problems were encountered
   */
  private static void writeFrame(DataFrame frame, XmlWriter writer) throws IOException {

    if (frame != null && writer != null) {

      if (frame.size() > 0) {
        DataField field = null;
        writer.writeFrameOpen();

        for (int i = 0; i < frame.size(); i++) {
          field = frame.getField(i);

          writer.writeFieldOpen();
          writer.writeTagOpen();
          writer.writeFieldName(field);
          writer.writeFieldType(field);

          // if there is a value
          if (field.getValue().length > 0) {
            writer.writeTagClose();

            if (field.getType() == DataField.FRAMETYPE) {
              writer.writeFrameOpen();
              writeFrame((DataFrame)field.getObjectValue(), writer);
              writer.writeFrameClose();
            } else {
              writer.writeLiteral(field.getStringValue());
            }
            writer.writeTagOpen();
            writer.writeForwardSlash();
            writer.writeFieldName(field);
            writer.writeTagClose();
            writer.writeFieldClose();
          } else {
            writer.writeForwardSlash();
            writer.writeTagClose();
            writer.writeFieldClose();
          }
        }
        writer.writeFrameClose();
      } else {
        writer.writeEmptyFrame();
      }
    }
  }
}
