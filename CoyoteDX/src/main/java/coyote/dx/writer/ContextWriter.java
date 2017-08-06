/*
 * Copyright (c) 2017 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 *
 * Contributors:
 *   Stephan D. Cote 
 *      - Initial concept and implementation
 */
package coyote.dx.writer;

import coyote.commons.StringUtil;
import coyote.dataframe.DataFrame;
import coyote.dx.ConfigTag;
import coyote.dx.context.TransformContext;
import coyote.loader.log.Log;


/**
 * Context writers write their frames to a context variable.
 * 
 * <p>The writer will create an array of DataFrames in the context with a key 
 * of "ContextOutput". This field can be overridden via the "target" 
 * configuration variable.
 * 
 * <p>This can be an expensive operation as it will replace the existing array 
 * with a new array one larger than the last, copying the contents of the 
 * previous array and adding the current DataFrame to the last element. This 
 * is best used for those jobs which only create one or a few output frames. 
 * Anything larger and it may be more efficient to use the file system.
 * 
 * <p>The replace mode of operation is more efficient, but only saves the last 
 * dataframe. In this mode the writer overwrite the context variable with each 
 * write. It is intended for those applications where only one record is 
 * expected.
 */
public class ContextWriter extends AbstractFrameWriter {
  public static final String DEFAULT_CONTEXT_FIELD = "ContextOutput";
  private String contextFieldName = DEFAULT_CONTEXT_FIELD;
  private boolean replace = false;




  /**
   * @see coyote.dx.Component#open(coyote.dx.context.TransformContext)
   */
  @Override
  public void open(TransformContext context) {
    super.open(context);

    String fieldName = configuration.getString(ConfigTag.TARGET);
    if (StringUtil.isNotBlank(fieldName)) {
      contextFieldName = fieldName.trim();
    }

    try {
      replace = configuration.getBoolean(ConfigTag.REPLACE);
    } catch (NumberFormatException e) {
      // probably not found
    }

  }




  /**
   * @see coyote.dx.writer.AbstractFrameWriter#write(coyote.dataframe.DataFrame)
   */
  @Override
  public void write(DataFrame frame) {
    if (replace) {
      getContext().set(contextFieldName, frame);
    } else {
      Object dataobj = getContext().get(contextFieldName);
      if (dataobj == null) {
        DataFrame[] frames = new DataFrame[1];
        frames[0] = frame;
        getContext().set(contextFieldName, frames);
      } else if (dataobj instanceof DataFrame[]) {
        DataFrame[] ary = (DataFrame[])dataobj;
        DataFrame[] frames = new DataFrame[ary.length + 1];
        System.arraycopy(ary, 0, frames, 0, ary.length);
        frames[frames.length - 1] = frame;
        getContext().set(contextFieldName, frames);
      } else {
        Log.error("Write collision with existing object (" + dataobj.getClass().getName() + ") in " + contextFieldName);
      }
    }
  }

}
