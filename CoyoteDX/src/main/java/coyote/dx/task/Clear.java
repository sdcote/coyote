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

import coyote.commons.FileUtil;
import coyote.commons.StringUtil;
import coyote.dx.CDX;
import coyote.dx.ConfigTag;
import coyote.dx.TaskException;
import coyote.loader.log.Log;
import coyote.loader.log.LogMsg;


/**
 * Clear out the contents of a directory or delete it altogether
 * 
 * recurse = also clear out the sub directories; the directory structure remains intact
 */
public class Clear extends AbstractFileTask {

  /**
   * @see coyote.dx.task.AbstractTransformTask#performTask()
   */
  @Override
  protected void performTask() throws TaskException {
    final String directory = getString( ConfigTag.DIRECTORY );
    if ( contains( ConfigTag.HALT_ON_ERROR ) ) {
      setHaltOnError( getBoolean( ConfigTag.HALT_ON_ERROR ) );
    }

    // get if we should recurse into sub directories when clearing directories
    boolean recurse = true;
    if ( contains( ConfigTag.RECURSE ) ) {
      recurse = getBoolean( ConfigTag.RECURSE );
    }

    if ( StringUtil.isNotBlank( directory ) ) {
      final String dir = resolveArgument( directory );
      coyote.loader.log.Log.info( LogMsg.createMsg( CDX.MSG, "Task.Clearing directory named {%s}", dir ) );

      try {
        FileUtil.clearDir( dir, true, recurse );
      } catch ( final Exception e ) {
        if ( haltOnError ) {
          getContext().setError( String.format( "Task.Clearing directory operation '%s' failed: %s", dir, e.getMessage() ) );
          return;
        }
      }

    } else {
      Log.warn( LogMsg.createMsg( CDX.MSG, "Task.Clear has no {%s} or {%s} argument - nothing to do.", ConfigTag.FILE, ConfigTag.DIRECTORY ) );
    }

  }

}
