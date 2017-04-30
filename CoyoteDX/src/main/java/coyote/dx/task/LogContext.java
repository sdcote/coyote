/*
 * Copyright (c) 2015 Stephan D. Cote' - All rights reserved.
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
 * 
 */
public class LogContext extends AbstractFileTask {

  /**
   * @see coyote.dx.TransformTask#execute()
   */
  @Override
  public void execute() throws TaskException {
    Log.info( getContext().dump() );
  }

}
