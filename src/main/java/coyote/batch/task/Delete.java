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

import java.io.File;

import coyote.batch.Batch;
import coyote.batch.ConfigTag;
import coyote.batch.Symbols;
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

      File sourceFile = new File( filename );

      // if not absolute, use the current job directory
      if ( !sourceFile.isAbsolute() ) {
        sourceFile = new File( context.getSymbols().getString( Symbols.JOB_DIRECTORY ), sourceFile.getPath() );
      }
      coyote.loader.log.Log.debug( LogMsg.createMsg( Batch.MSG, "Task.deleting_file", file, sourceFile.getAbsolutePath() ) );

      if ( !FileUtil.deleteFile( sourceFile ) ) {
        String msg = LogMsg.createMsg( Batch.MSG, "Task.file_deletion_error", file, sourceFile.getAbsolutePath() ).toString();
        if ( haltOnError ) {
          getContext().setError( msg );
          return;
        } else {
          Log.error( msg );
        }
      }

    } else if ( StringUtil.isNotBlank( directory ) ) {
      final String dir = resolveArgument( directory );
      File dirFile = new File( dir );

      // if not absolute, use the current job directory
      if ( !dirFile.isAbsolute() ) {
        dirFile = new File( context.getSymbols().getString( Symbols.JOB_DIRECTORY ), dirFile.getPath() );
      }
      coyote.loader.log.Log.debug( LogMsg.createMsg( Batch.MSG, "Task.deleting_directory", dir, dirFile.getAbsolutePath() ) );

      if ( dirFile.exists() ) {
        if ( dirFile.isDirectory() ) {
          try {
            FileUtil.deleteDirectory( dirFile );
          } catch ( final Exception e ) {
            String msg = LogMsg.createMsg( Batch.MSG, "Task.directory_deletion_error", dir, dirFile.getAbsolutePath() ).toString();
            if ( haltOnError ) {
              getContext().setError( msg );
              return;
            } else {
              Log.error( msg );
            }
          }
        } else {
          String msg = LogMsg.createMsg( Batch.MSG, "Task.directory_specified_file_found", this.getClass().getName(), dir, dirFile.getAbsolutePath() ).toString();
          if ( haltOnError ) {
            getContext().setError( msg );
            return;
          } else {
            Log.error( msg );
          }
        }
      }

    } else {
      Log.warn( LogMsg.createMsg( Batch.MSG, "Task.delete_config_error", ConfigTag.FILE, ConfigTag.DIRECTORY ) );
    }

  }

}
