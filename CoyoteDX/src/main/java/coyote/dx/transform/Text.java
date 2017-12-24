/*
 * Copyright (c) 2015 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.dx.transform;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;

import coyote.commons.DateUtil;
import coyote.commons.StringUtil;
import coyote.dataframe.DataField;
import coyote.dataframe.DataFrame;
import coyote.dx.ConfigTag;
import coyote.dx.FrameTransform;
import coyote.dx.TransformException;
import coyote.loader.log.Log;


/**
 * Create a text (String) object from the data existing in the field.
 * 
 * <p>The primary use case is to prevent formatting objects in writers and 
 * formatting them in one location for all the writers. For example, a Date 
 * field can be converted to just the time portion with the format pattern.
 * 
 * <p>THe following configuration:<pre>
 * "Text": { "field": "date", "format": "YYYY-MM-dd" }</pre>
 * Converts the Date field named "date" to a text (String) field with the date
 * formatted in year-month-day format. BTW, even if the underlying field is 
 * not a date, this transform will try to parse it into a date tyape and apply 
 * the formatting.
 * 
 * <p>The same works with number formatting. The underlying numeric is 
 * formatted with the given pattern and if the underlying field is a String, 
 * this transform will try to convert it into the proper type and format it.
 */
public class Text extends AbstractFieldTransform implements FrameTransform {

  /**
   * @see coyote.dx.FrameTransform#process(coyote.dataframe.DataFrame)
   */
  @Override
  public DataFrame process(final DataFrame frame) throws TransformException {
    DataFrame retval = frame;
    DataField field = retval.getField(getFieldName());

    if (field != null) {
      String format = getConfiguration().getString(ConfigTag.FORMAT);

      if (StringUtil.isNotBlank(format)) {
        String text = "";
        short type = field.getType();

        switch (type) {
          case DataField.DOUBLE:
            text = new DecimalFormat(format).format((double)field.getObjectValue());
            break;
          case DataField.FLOAT:
            text = new DecimalFormat(format).format((float)field.getObjectValue());
            break;
          case DataField.S64:
          case DataField.U32:
            text = NumberFormat.getInstance().format((long)field.getObjectValue());
            break;
          case DataField.S32:
          case DataField.U16:
            text = NumberFormat.getInstance().format((int)field.getObjectValue());
            break;
          case DataField.S16:
          case DataField.U8:
            text = NumberFormat.getInstance().format((short)field.getObjectValue());
            break;
          case DataField.DATE:
            text = new SimpleDateFormat(format).format((java.util.Date)field.getObjectValue());
            break;
          case DataField.STRING:
            text = guess(field, format);
            if (text == null) {
              Log.error("Data field '" + field.getName() + "' of type 'String' could not be converted into a formattable type - Value: '" + field.getStringValue() + "'");
            }
            break;
          default:
            Log.error("Data field '" + field.getName() + "' of type : " + field.getTypeName() + " cannot be formatted");
            break;
        }

        if (StringUtil.isNotBlank(text)) {
          retval.put(field.getName(), text);
        }
      }

    }
    // return the dataframe with its new date field
    return retval;
  }




  /**
   * @param field
   * @param format
   * @return
   */
  private String guess(DataField field, String format) {
    String retval = null;
    String text = field.getStringValue();
    if (StringUtil.isNotBlank(text)) {
      java.util.Date date = DateUtil.parse(text);
      if (date != null) {
        retval = new SimpleDateFormat(format).format(date);
      } else {
        if (text.indexOf('.') > -1) {
          try {
            BigDecimal number = new BigDecimal(text);
            retval = new DecimalFormat(format).format(number);
          } catch (NumberFormatException e) {
            // ignore
          }
        } else {
          Long number = Long.parseLong(text);
          retval = new DecimalFormat(format).format(number);
        }
      }
    }

    return retval;
  }

}
