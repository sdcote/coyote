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


/**
 * A task which generates a console message.
 */
public class Message extends AbstractTransformTask {

  public String getMessage() {
    if (configuration.containsIgnoreCase(ConfigTag.MESSAGE)) {
      return configuration.getString(ConfigTag.MESSAGE);
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
      System.out.println(message);
    }

  }

}
