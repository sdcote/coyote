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
 * Save a context variable to a file.
 * 
 * <p>File will contain the string value of the named context variable. If the 
 * named value is not found, the file will be empty.
 * 
 * <p>The value will be treated as a template and resolved with values from 
 * the symbol table in the context.
 */
public class SaveContextValue extends AbstractFileTask {

  /**
   * @see coyote.dx.task.AbstractTransformTask#performTask()
   */
  @Override
  protected void performTask() throws TaskException {

    // the context variable to retrieve
    final String source = getString( ConfigTag.SOURCE );
    
    final String target = getString( ConfigTag.TARGET );
    final String filename = getString( ConfigTag.FILE );

    Log.fatal( "Not implemented" );
  }

  
}
