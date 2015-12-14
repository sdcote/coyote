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

import java.io.IOException;

import coyote.batch.Batch;
import coyote.batch.ConfigTag;
import coyote.batch.TaskException;
import coyote.commons.FileUtil;
import coyote.commons.StringUtil;
import coyote.loader.log.Log;
import coyote.loader.log.LogMsg;


/**
 * Make a copy of the source file in the target location.
 * source = source file
 * target = target file
 * fromdir = source directory
 * todir = target directory
 * pattern = regex pattern used to match files
 * recurse = also search subdirectories of fromdir
 * preserve = preserve the hierarchy of copied files in target directory
 * overwrite = copy over any existing files with the same name defaults to false
 * keepDate = whether to preserve the file date
 * rename = use a generational name for files with the same name preserve=false,overwrite=false defaults to true
 * 
 * <p>All the business logic for copy operations s in the FileUtil class in 
 * Coyote Commons contributed by the Coyote Loader. No file logic is needed in 
 * this library, just argument checks.</p>
 */
public class Copy extends AbstractFileTask {

  /**
   * @see coyote.batch.TransformTask#execute()
   */
  @Override
  public void execute() throws TaskException {

    // determine our configuration settings
    final String source = getString( ConfigTag.SOURCE );
    final String fromDir = getString( ConfigTag.FROMDIR );
    final String target = getString( ConfigTag.TARGET );
    final String toDir = getString( ConfigTag.TODIR );
    final String pattern = getString( ConfigTag.PATTERN );

    // recurse the source directory, defaults to false
    boolean recurse = getBoolean( ConfigTag.RECURSE );

    // preserve hierarchy, defaults to false (this is an edge case)
    boolean preserveHierarchy = getBoolean( ConfigTag.PRESERVE );

    // preserve the date on the copied file
    boolean keepDate = getBoolean( ConfigTag.KEEPDATE );

    // overwrite existing files defaults to true
    boolean overwrite = getBoolean( ConfigTag.OVERWRITE );

    // rename any files which may be overwritten defaults to true to prevent loss
    boolean rename = true;
    if ( contains( ConfigTag.RENAME ) ) {
      rename = getBoolean( ConfigTag.RENAME );
    }

    if ( StringUtil.isNotBlank( source ) ) {
      // file based copy
      final String src = resolveArgument( source );

      if ( StringUtil.isNotBlank( target ) ) {
        // this is a file to file copy
        final String tgt = resolveArgument( target );
        Log.debug( LogMsg.createMsg( Batch.MSG, "Task.Copying file named {} to file named {}", src, tgt ) );

        try {
          FileUtil.copyFile( src, tgt );
        } catch ( final IOException e ) {
          if ( haltOnError ) {
            getContext().setError( String.format( "Copy operation '%s' to '%s' failed: %s", src, tgt, e.getMessage() ) );
            return;
          }
        }

      } else if ( StringUtil.isNotBlank( toDir ) ) {
        // this is a file to directory copy
        final String tgt = resolveArgument( toDir );
        Log.debug( LogMsg.createMsg( Batch.MSG, "Task.Copying file named {} to directory named {}", src, tgt ) );

        try {
          FileUtil.copyFileToDir( src, tgt );
        } catch ( final IOException e ) {
          if ( haltOnError ) {
            getContext().setError( String.format( "Copy operation '%s' to '%s' failed: %s", src, tgt, e.getMessage() ) );
            return;
          }
        }
      } else {
        Log.debug( "Cannot copy without a target" );
        if ( haltOnError ) {
          getContext().setError( "Copy operation failed: no target argument" );
          return;
        }
      }

    } else if ( StringUtil.isNotBlank( fromDir ) ) {
      // This appears to be a directory-based copy

      if ( StringUtil.isNotBlank( toDir ) ) {
        // this is a directory to directory copy
        Log.debug( LogMsg.createMsg( Batch.MSG, "Task.copying_directory", fromDir, toDir, pattern, recurse, preserveHierarchy, keepDate, overwrite, rename ) );

        try {
          FileUtil.copyDirectory( fromDir, toDir, pattern, recurse, preserveHierarchy, keepDate, overwrite, rename );
        } catch ( final IOException e ) {
          if ( haltOnError ) {
            getContext().setError( String.format( "Copy operation '%s' to '%s' failed: %s", fromDir, toDir, e.getMessage() ) );
            return;
          }
        }
      } else {
        String msg = LogMsg.createMsg( Batch.MSG, "Task.copy_target_directory_missing" ).toString();
        Log.warn( msg );

        if ( haltOnError ) {
          getContext().setError( msg );
          return;
        }
      } // target check
    } else {
      Log.error( "Cannot copy without a source" );
      if ( haltOnError ) {
        getContext().setError( "Copy operation failed: no source argument" );
        return;
      }
    } // source check

  }
}
