/*
 * Copyright (c) 2015 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.dx.task;

import coyote.commons.StringUtil;
import coyote.commons.template.Template;
import coyote.dx.ConfigTag;
import coyote.dx.TaskException;
import coyote.loader.log.Log;


/**
 * A task which generates a log entry.
 */
public class LogEntry extends AbstractTransformTask {

  public String getMessage() {
    if (configuration.containsIgnoreCase(ConfigTag.MESSAGE)) {
      return configuration.getString(ConfigTag.MESSAGE);
    }
    return null;
  }




  public String getCategory() {
    if (configuration.containsIgnoreCase(ConfigTag.CATEGORY)) {
      return configuration.getString(ConfigTag.CATEGORY);
    }
    return null;
  }




  /**
   * @see coyote.dx.task.AbstractTransformTask#performTask()
   */
  @Override
  protected void performTask() throws TaskException {

    String message = Template.resolve(getMessage(), getContext().getSymbols());

    if (StringUtil.isNotBlank(message)) {
      String level = getCategory();
      if (StringUtil.isNotBlank(level)) {
        if (StringUtil.equalsIgnoreCase("info", level)) {
          Log.info(message);
        } else if (StringUtil.equalsIgnoreCase("notice", level)) {
          Log.notice(message);
        } else if (StringUtil.equalsIgnoreCase("debug", level)) {
          Log.debug(message);
        } else if (StringUtil.equalsIgnoreCase("warn", level)) {
          Log.warn(message);
        } else if (StringUtil.equalsIgnoreCase("error", level)) {
          Log.error(message);
        } else if (StringUtil.equalsIgnoreCase("trace", level)) {
          Log.trace(message);
        } else {
          Log.append(level.toUpperCase(), message);
        }
      } else {
        Log.info(message);
      }
    }

  }

}
