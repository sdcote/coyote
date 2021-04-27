/*
 * Copyright (c) 2015 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.dx.task;

import java.util.Locale;

import coyote.commons.StringUtil;
import coyote.dx.ConfigTag;
import coyote.dx.TaskException;
import coyote.loader.log.Log;


/**
 * Log the current state of the context including the symbol table.
 * 
 * <p>By default it logs it to the INFO category, but can be configured 
 * thusly:<pre>"LogContext":{"category":"debug"}</pre>
 */
public class LogContext extends AbstractFileTask {

  /**
   * @see coyote.dx.task.AbstractTransformTask#performTask()
   */
  @Override
  protected void performTask() throws TaskException {
    String category = getString(ConfigTag.CATEGORY);
    long mask;
    if (StringUtil.isNotEmpty(category)) {
      mask = Log.getCode(category.trim().toUpperCase(Locale.getDefault()));
    } else {
      mask = Log.INFO_EVENTS;
    }
    Log.append(mask, getContext().dump());
  }

}
