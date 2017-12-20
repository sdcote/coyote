/*
 * Copyright (c) 2015 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.dx.transform;

import java.text.SimpleDateFormat;

import coyote.commons.DateUtil;
import coyote.commons.StringUtil;
import coyote.dataframe.DataField;
import coyote.dataframe.DataFrame;
import coyote.dx.ConfigTag;
import coyote.dx.FrameTransform;
import coyote.dx.TransformException;


/**
 * Create a native date object from the data existing in the field.
 * 
 * <p>This converst the field from one date format (e.g. unix timestamp) to 
 * another or parse string data into a date object.
 * 
 * <p>The primary use case is to take epoch time in milliseconds (Java) or 
 * seconds (Unix) and convert it to a Date object. Another use case is to 
 * parse String representations of dates onto date references.
 * 
 * <p>The {@code format} configuration attribute describes the format currently existing in the field. Expected values include:<ul>
 * <li>{@code seconds} - Set the date with the current value representing number of seconds in the epoch, a.k.a. Unix time
 * <li>{@code unix} - same as {@code seconds}
 * <li>{@code milliseconds} - Set the date with the current value representing number of milliseconds in the epoch, a.k.a. Java time
 * <li>{@code java} - same as {@code milliseconds}
 * <li>{@code FormatString} - Set the date from the string in this field using a Java date format 
 * <li>{@code now} - Set the date to the current epoch time
 * </ul>
 */
public class Date extends AbstractFieldTransform implements FrameTransform {
  private static final String SECONDS = "seconds";
  private static final String MILLISECONDS = "milliseconds";
  private static final String JAVA = "java";
  private static final String UNIX = "unix";
  private static final String NOW = "now";




  /**
   * @see coyote.dx.FrameTransform#process(coyote.dataframe.DataFrame)
   */
  @Override
  public DataFrame process(final DataFrame frame) throws TransformException {
    DataFrame retval = frame;
    DataField field = retval.getField(getFieldName());
    if (field != null) {
      String token = getConfiguration().getString(ConfigTag.FORMAT);

      if (StringUtil.isNotBlank(token)) {
        String format = token.toLowerCase().trim();
        switch (format) {
          case NOW:
            field = setNow(field);
            break;
          case SECONDS:
          case UNIX:
            field = processSeconds(field);
            break;
          case JAVA:
          case MILLISECONDS:
            field = processMilliseconds(field);
            break;
          default:
            field = setFormatDate(field, token);
            break;
        }
      } else {
        field = processField(field);
      }

      // place the field in the dataframe overwriting the existing value
      retval.put(field.getName(), field.getObjectValue());
    }
    // return the dataframe with its new date field
    return retval;
  }




  private DataField setNow(DataField field) {
    return new DataField(field.getName(), new java.util.Date());
  }




  /**
   * @param field
   * @param format 
   * @throws TransformException if the time could not be retrieved from the field value as a long
   */
  private DataField setFormatDate(DataField field, String format) throws TransformException {
    DataField retval = null;

    if (field != null && field.isNotNull()) {
      String text = field.getStringValue();
      try {
        java.util.Date date = new SimpleDateFormat(format).parse(text);
        retval = new DataField(field.getName(), date);
      } catch (Exception e) {
        throw new TransformException("Value could not be converted into a date: '" + text + "' Reason: " + e.getMessage() + " -- " + field.toString());
      }
    } else {
      retval = new DataField(field.getName(), DataField.DATE, null);
    }

    return retval;
  }




  /**
   * @param field
   * @throws TransformException if the time could not be retrieved from the field value as a long
   */
  private DataField processMilliseconds(DataField field) throws TransformException {
    DataField retval = null;
    if (field != null && field.isNotNull()) {
      long time = getAsLong(field);
      java.util.Date date = new java.util.Date();
      date.setTime((long)time);
      retval = new DataField(field.getName(), date);
    }
    return retval;
  }




  /**
   * @param field
   * @throws TransformException if the time could not be retrieved from the field value as a long
   */
  private DataField processSeconds(DataField field) throws TransformException {
    DataField retval = null;
    if (field != null && field.isNotNull()) {
      long time = getAsLong(field);
      java.util.Date date = new java.util.Date();
      date.setTime((long)time * 1000);
      retval = new DataField(field.getName(), date);
    }
    return retval;
  }




  /**
   * @param field the filed to convert into a date
   */
  private DataField processField(DataField field) {
    DataField retval = null;
    if (field != null && field.isNotNull()) {
      String text = field.getStringValue();
      java.util.Date date = DateUtil.parse(text);
      retval = new DataField(field.getName(), date);
    }
    return retval;
  }




  private long getAsLong(DataField field) throws TransformException {
    long retval = 0;
    if (field != null) {
      Object val = field.getObjectValue();
      if (val != null) {
        if (val instanceof Long) {
          return ((Long)val).longValue();
        } else {
          try {
            return Long.parseLong(val.toString());
          } catch (Exception e) {
            throw new TransformException("Value could not be converted into a (date)long: '" + val.toString() + "' Reason: " + e.getMessage() + " -- " + field.toString());
          }
        }
      }
    }
    return retval;
  }

}
