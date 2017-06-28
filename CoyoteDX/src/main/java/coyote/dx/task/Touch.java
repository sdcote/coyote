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

import java.io.File;

import coyote.commons.FileUtil;
import coyote.commons.StringUtil;
import coyote.dx.ConfigTag;
import coyote.dx.TaskException;


/**
 * Update the last access time of a file.
 * 
 * <p>This opens the file for writing, appends nothing to it and closes it. 
 * This works on both *nix and Windows and the file is 1) opened, 2) 
 * modified, and 3) has its last modified time set to the current time. 
 * Regardless of the OS, this should trigger the file system to acknowledge 
 * the file was accessed.
 *
 * <p>This should be configured thusly:<pre>
 * "Task": {
 *     "Touch": { "filename": "README.md" }
 *  }</pre>
 */
public class Touch extends AbstractFileTask {

  public String getFilename() {
    if ( configuration.containsIgnoreCase( ConfigTag.FILE ) ) {
      return configuration.getString( ConfigTag.FILE );
    }
    return null;
  }




  /**
   * @see coyote.dx.TransformTask#execute()
   */
  @Override
  public void execute() throws TaskException {
    String filename = getFilename();
    if ( StringUtil.isNotBlank( filename ) ) {
      File file = new File( filename );
      FileUtil.touch( file );
    }
  }

}
