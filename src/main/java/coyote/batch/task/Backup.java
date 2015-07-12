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
package coyote.batch.task;

import coyote.batch.TaskException;
import coyote.batch.TransformContext;


/**
 * Make a copy of a file in the given directory using a generation naming 
 * scheme effectively rotating the files.
 * 
 * <p>This task will create a copy of the file in the requested directory and 
 * append a number to the end of the file name. The latest backup is always 1; 
 * as all the files are renamed according to the age of their generation. For 
 * example FILE.1 is renamed to FILE.2 before the backup is created as FILE.1 
 * so no data is lost.</p>
 * 
 * <p>If a limit number is specified, the task will act like a file rotator, 
 * deleting the oldest file before creating a generational backup.</p>
 */
public class Backup extends AbstractFileTask {

  /**
   * @see coyote.batch.TransformTask#execute(coyote.batch.TransformContext)
   */
  @Override
  public void execute( TransformContext transformContext ) throws TaskException {
    // TODO Auto-generated method stub

  }

}
