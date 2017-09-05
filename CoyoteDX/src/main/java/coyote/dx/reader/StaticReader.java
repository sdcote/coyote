/*
 * Copyright (c) 2017 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.dx.reader;

import coyote.commons.StringUtil;
import coyote.commons.template.Template;
import coyote.dataframe.DataField;
import coyote.dataframe.DataFrame;
import coyote.dx.CDX;
import coyote.dx.ConfigTag;
import coyote.dx.context.TransactionContext;
import coyote.dx.context.TransformContext;
import coyote.loader.cfg.Config;
import coyote.loader.log.Log;
import coyote.loader.log.LogMsg;


/**
 * A reader which creates a single static message, useful for testing, to 
 * insert data into databases, and to publish messages on topics and queues.
 * 
 * <p>Basic configuration is as follows:<pre>
 * "Reader" : {
 *     "class" : "StaticReader",
 *     "fields" : {
 *         "JobId": "EB00C166-9972-4147-9453-735E7EB15C60",
 *         "Delay": 1000,
 *         "Log": true
 *     }
 * }</pre> The "Fields" section specifies order and content of the fields. The
 * number of copies is controlled by the "limit" configuration attribute, and 
 * defaults to 1 if no limit is specified or if the value could not be parsed 
 * into an integer.
 *  
 * <p>Note numeric and boolean fields are treated as their respective types.
 */
public class StaticReader extends AbstractFrameReader {
  private int counter = 0;
  private int limit = 1;
  private DataFrame frame = null;




  /**
   * @see coyote.dx.FrameReader#read(coyote.dx.context.TransactionContext)
   */
  @Override
  public DataFrame read(TransactionContext context) {
    counter++;
    if (counter >= limit) {
      context.setLastFrame(true);
    }
    return resolve(frame);
  }




  /**
   * Resolve the variables in the configured frame.
   * 
   * <p>This scans through all the string fields and resolves them against the 
   * currently set symbol table.
   * 
   * @param frame the frame to resolve.
   * 
   * @return a frame will all the string templates resolved to the current 
   *         state of the symbol table or null if the passed frame is null.
   */
  private DataFrame resolve(DataFrame frame) {
    DataFrame retval = null;
    if (frame != null) {
      retval = new DataFrame();
      for (final DataField field : frame.getFields()) {
        if (field.getType() == DataField.STRING) {
          retval.add(field.getName(), Template.preProcess(field.getStringValue(), getContext().getSymbols()));
        } else {
          retval.add(field);
        }
      }
    }
    return retval;
  }




  /**
   * @see coyote.dx.FrameReader#eof()
   */
  @Override
  public boolean eof() {
    return counter >= limit;
  }




  /**
   * @see coyote.dx.Component#open(coyote.dx.context.TransformContext)
   */
  @Override
  public void open(TransformContext context) {
    super.open(context);
    counter = 0;
    try {
      limit = configuration.getInt(ConfigTag.LIMIT);
    } catch (NumberFormatException e) {
      limit = 1;
    }

    final Config section = configuration.getSection(ConfigTag.FIELDS);
    if (section != null) {
      frame = new DataFrame();
      for (final DataField field : section.getFields()) {
        if (!field.isFrame()) {
          if (StringUtil.isNotBlank(field.getName()) && !field.isNull()) {
            frame.set(field.getName(), field.getObjectValue());
          }
        }
      }
    } else {
      String msg = LogMsg.createMsg(CDX.MSG, "Reader.no_fields_specified", getClass().getName()).toString();
      Log.error(msg);
      context.setError(msg);
    }
  }

}
