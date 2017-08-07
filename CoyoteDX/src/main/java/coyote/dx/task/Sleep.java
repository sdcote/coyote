/*
 * Copyright (c) 2017 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.dx.task;

import coyote.dx.ConfigTag;
import coyote.dx.TaskException;


/**
 * Sleep for the given number of milliseconds.
 * 
 * <p>Useful in pacing tasks to give other systems time to process data before 
 * continuing on.
 * 
 * "Sleep":{"millis":5000}
 * "Sleep":{"seconds":5}
 */
public class Sleep extends AbstractTransformTask {

  /**
   * @see coyote.dx.task.AbstractTransformTask#performTask()
   */
  @Override
  protected void performTask() throws TaskException {
    final int milliseconds = getInteger(ConfigTag.MILLIS);
    final int seconds = getInteger(ConfigTag.SECONDS);

    final long timeout;
    if (milliseconds > 0) {
      timeout = milliseconds;
    } else {
      timeout = seconds * 1000;
    }

    try {
      Thread.sleep(timeout);
    } catch (InterruptedException ignore) {}
  }

}
