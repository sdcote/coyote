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
package coyote.dx.task;

import coyote.dx.TaskException;
import coyote.loader.log.Log;

/**
 * Read a text file into the context.
 * 
 * <p>File is assumed to be a simple text file containing name-value pairs 
 * delimited with an equals '=' sign. Other characters can be specified as a 
 * delimiter.
 */
public class ReadIntoContext extends AbstractFileTask {

  /**
   * @see coyote.dx.task.AbstractTransformTask#performTask()
   */
  @Override
  protected void performTask() throws TaskException {
    Log.fatal( "Not implemented" );
  }

  
}
