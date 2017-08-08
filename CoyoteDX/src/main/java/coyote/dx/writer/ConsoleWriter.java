/*
 * Copyright (c) 2017 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.dx.writer;

import coyote.commons.StringUtil;
import coyote.commons.template.Template;
import coyote.dataframe.DataFrame;
import coyote.dataframe.marshal.CSVMarshaler;
import coyote.dataframe.marshal.JSONMarshaler;
import coyote.dataframe.marshal.XMLMarshaler;
import coyote.dx.ConfigTag;


/**
 * Writes frames to STDOUT.
 * 
 * <p>Frames can be output in a variety of formats
 * 
 * <p>A message can be specified which will follow any frames which will be 
 * resolved as a template against the symbol table in the transform context. 
 */
public class ConsoleWriter extends AbstractFrameWriter {
  private final String NO_FORMAT = "none";
  private final String JSON_FORMAT = "json";
  private final String XML_FORMAT = "xml";
  private final String CSV_FORMAT = "csv";




  public String getFormat() {
    if (configuration.containsIgnoreCase(ConfigTag.FORMAT)) {
      return configuration.getString(ConfigTag.FORMAT);
    }
    return NO_FORMAT;
  }




  public String getMessage() {
    if (configuration.containsIgnoreCase(ConfigTag.MESSAGE)) {
      return configuration.getString(ConfigTag.MESSAGE);
    }
    return null;
  }




  private boolean isIndented() {
    if (configuration.containsIgnoreCase(ConfigTag.INDENT)) {
      return configuration.getBoolean(ConfigTag.INDENT);
    }
    return false;
  }




  /**
   * @see coyote.dx.writer.AbstractFrameWriter#write(coyote.dataframe.DataFrame)
   */
  @Override
  public void write(DataFrame frame) {
    if (!StringUtil.equalsIgnoreCase(NO_FORMAT, getFormat())) {
      if (isIndented()) {
        if (StringUtil.equalsIgnoreCase(JSON_FORMAT, getFormat())) {
          System.out.println(JSONMarshaler.toFormattedString(frame));
        } else if (StringUtil.equalsIgnoreCase(XML_FORMAT, getFormat())) {
          System.out.println(XMLMarshaler.toFormattedString(frame));
        } else if (StringUtil.equalsIgnoreCase(CSV_FORMAT, getFormat())) {
          System.out.println(CSVMarshaler.marshal(frame));
        } else {
          System.out.println("Don't know how to format data into '" + getFormat() + "'");
        }
      } else {
        if (StringUtil.equalsIgnoreCase(JSON_FORMAT, getFormat())) {
          System.out.println(JSONMarshaler.marshal(frame));
        } else if (StringUtil.equalsIgnoreCase(XML_FORMAT, getFormat())) {
          System.out.println(XMLMarshaler.marshal(frame));
        } else if (StringUtil.equalsIgnoreCase(CSV_FORMAT, getFormat())) {
          System.out.println(CSVMarshaler.marshal(frame));
        } else {
          System.out.println("Don't know how to format data into '" + getFormat() + "'");
        }
      }
    }

    if (StringUtil.isNotEmpty(getMessage())) {
      String message = Template.resolve(getMessage(), getContext().getSymbols());
      System.out.println(message);
    }

  }

}
