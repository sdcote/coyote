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

/**
 * This task generates a compressed archive of a file or directory.
 * 
 * <p>The common use case for this task is to archive all the artifacts in a 
 * job directory so it can be sent as a single file to some destination either 
 * by file transfer more messaging tools.
 * 
 * <pre>"PostProcess": {
 *   "Archive" : { "directory": "wrkdir", "target": "wrkdir.zip", "enabled": false  },
 * }</pre>
 * 
 * Goals:
 * Archive a directory to a zip file.
 * Archive a file to a zip file
 * Archive a pattern of files to a zip file (like Copy task)
 * Add a directory to an existing zip
 * Add a file to an existing zip
 * 
 */
public class Archive extends AbstractFileTask {
  
  public String getDirectory() { return configuration.getAsString( ConfigTag.DIRECTORY ); }
  public void setDirectory( String value ) { configuration.set( ConfigTag.DIRECTORY, value ); }

  public String getTarget() { return configuration.getAsString( ConfigTag.TARGET ); }
  public void setTarget( String value ) { configuration.set( ConfigTag.TARGET, value ); }




  /**
   * @see coyote.dx.task.AbstractTransformTask#execute()
   */
  @Override
  public void execute() throws TaskException {

  }

}
