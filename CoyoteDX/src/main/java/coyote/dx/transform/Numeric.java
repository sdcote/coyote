/*
 * Copyright (c) 2015 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.dx.transform;

import coyote.commons.StringUtil;
import coyote.dataframe.DataField;
import coyote.dataframe.DataFrame;
import coyote.dx.ConfigTag;
import coyote.dx.FrameTransform;
import coyote.dx.TransformException;


/**
 * This is not implemented yet. 
 * <p>The {@code format} configuration attribute describes the format currently existing in the field. Expected values include:<ul>
 * <li>{@code nowSeconds} - set the field to the current epoch time UTC in seconds
 * <li>{@code nowMilliseconds} - set the field to the current epoch time UTC in milliseconds 
 * <li>{@code now} - same as {@code nowMilliseconds}
 * <li>{@code nowUnix} - same as {@code nowSeconds}
 * <li>{@code nowJava} - same as {@code nowMilliseconds}
 * </ul>
 */
public class Numeric extends AbstractFieldTransform implements FrameTransform {
  private static final String NOW_SECONDS = "nowseconds";
  private static final String NOW_MILLISECONDS = "nowmilliseconds";
  private static final String NOW_UNIX = "nowunix";
  private static final String NOW_JAVA = "nowjava";




  /**
   * @see coyote.dx.FrameTransform#process(coyote.dataframe.DataFrame)
   */
  @Override
  public DataFrame process(final DataFrame frame) throws TransformException {
    DataFrame retval = frame;
    DataField field = retval.getField(getFieldName());
    String token = getConfiguration().getString(ConfigTag.FORMAT);

    if (StringUtil.isNotBlank(token)) {
      String format = token.toLowerCase().trim();
      switch (format) {
        case NOW_JAVA:
        case NOW_MILLISECONDS:
          System.out.println("NOW_JAVA");
          field = setNowMilliseconds(field);
          break;
        case NOW_SECONDS:
        case NOW_UNIX:
          System.out.println("NOW_UNIX");
          field = setNowSeconds(field);
          break;
        default:
          field = processField(field);
          break;
      }
    } else

    {
      System.out.println("No format");
      field = processField(field);
    }

    // place the field in the dataframe overwriting the existing value
    retval.put(field.getName(), field.getObjectValue());

    // return the dataframe with its new date field
    return retval;
  }




  private DataField processField(DataField field) {
    DataField retval = null;
    retval = field; // for now
    return retval;
  }




  private DataField setNowMilliseconds(DataField field) {
    DataField retval = null;
    retval = new DataField(field.getName(), System.currentTimeMillis());
    return retval;
  }




  private DataField setNowSeconds(DataField field) {
    DataField retval = null;
    retval = new DataField(field.getName(), System.currentTimeMillis() / 1000);
    return retval;
  }

}
