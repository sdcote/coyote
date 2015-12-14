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
 * Delete a file or a directory from the file system.
 */
public class Delete extends AbstractFileTask {

  /**
   * @see coyote.batch.TransformTask#execute()
   */
  @Override
  public void execute() throws TaskException {
    final String filename = getString( ConfigTag.FILE );
    final String directory = getString( ConfigTag.DIRECTORY );
    if ( contains( ConfigTag.HALT_ON_ERROR ) ) {
      setHaltOnError( getBoolean( ConfigTag.HALT_ON_ERROR ) );
    }

    if ( StringUtil.isNotBlank( filename ) ) {
      final String file = resolveArgument( filename );
      coyote.loader.log.Log.info( LogMsg.createMsg( Batch.MSG, "Task.Deleting file named {}", file ) );

      try {
        FileUtil.deleteFile( file );
      } catch ( final Exception e ) {
        if ( haltOnError ) {
          getContext().setError( String.format( "Delete file operation '%s' failed: %s", file, e.getMessage() ) );
          return;
        }
      }

    } else if ( StringUtil.isNotBlank( directory ) ) {
      final String dir = resolveArgument( directory );
      coyote.loader.log.Log.info( LogMsg.createMsg( Batch.MSG, "Task.Deleting directory named {}", dir ) );

      try {
        FileUtil.clearDir( dir, true, true );
      } catch ( final Exception e ) {
        if ( haltOnError ) {
          getContext().setError( String.format( "Delete directory operation '%s' failed: %s", dir, e.getMessage() ) );
          return;
        }
      }

    } else {
      Log.warn( LogMsg.createMsg( Batch.MSG, "Task.Move has no {} or {} argument - nothing to do.", ConfigTag.FILE, ConfigTag.DIRECTORY ) );
    }

  }

}
