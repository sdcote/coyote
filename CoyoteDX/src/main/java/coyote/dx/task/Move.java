/*
 * Copyright (c) 2015 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 *
 * Contributors:
 *   Stephan D. Cote 
 *      - Initial concept and initial implementation
 */
package coyote.dx.task;

import coyote.dx.TaskException;


/**
 * Move the source file to a target location.
 * 
 * <p>When the operation is complete, the source file will not exist unless the 
 * target could not be created.</p>
 */
public class Move extends AbstractFileTask {

  /**
   * @see coyote.dx.TransformTask#execute()
   */
  @Override
  public void execute() throws TaskException {
    // TODO Auto-generated method stub

  }

}
