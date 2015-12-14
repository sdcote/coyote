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

import coyote.batch.Batch;
import coyote.batch.ConfigTag;
import coyote.batch.TaskException;
import coyote.commons.FileUtil;
import coyote.commons.StringUtil;
import coyote.loader.log.Log;
import coyote.loader.log.LogMsg;


/**
 * Clear out the contents of a directory or delete it altogether
 * 
 * recurse = also clear out the sub directories; the directory structure remains intact
 */
public class Clear extends AbstractFileTask {

  /**
   * @see coyote.batch.TransformTask#execute()
   */
  @Override
  public void execute() throws TaskException {
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
      coyote.loader.log.Log.info( LogMsg.createMsg( Batch.MSG, "Task.Clearing directory named {}", dir ) );

      try {
        FileUtil.clearDir( dir, true, recurse );
      } catch ( final Exception e ) {
        if ( haltOnError ) {
          getContext().setError( String.format( "Task.Clearing directory operation '%s' failed: %s", dir, e.getMessage() ) );
          return;
        }
      }

    } else {
      Log.warn( LogMsg.createMsg( Batch.MSG, "Task.Clear has no {} or {} argument - nothing to do.", ConfigTag.FILE, ConfigTag.DIRECTORY ) );
    }

  }

}
