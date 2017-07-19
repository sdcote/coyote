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

import coyote.dx.ConfigTag;
import coyote.dx.TaskException;
import coyote.loader.log.Log;

/**
 * Read a text file into the context as a set of name-value pairs.
 * 
 * <p>This allows a job to be populated with context variables at runtime.
 * 
 * <p>The file is assumed to be a simple text file containing name-value 
 * pairs. Each line is expected to contain one pair delimited with an equals 
 * '=' sign. Other characters can be specified as a delimiter.
 */
public class ReadIntoContext extends AbstractFileTask {

  /**
   * @see coyote.dx.task.AbstractTransformTask#performTask()
   */
  @Override
  protected void performTask() throws TaskException {
    final String filename = getString( ConfigTag.FILE );
    final String source = getString( ConfigTag.SOURCE );
    // TODO: Expect "source", support "file", check for null
    
    Log.fatal( "Not implemented" );
  }

  
}
