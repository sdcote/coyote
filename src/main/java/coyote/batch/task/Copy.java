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
import coyote.batch.TransformContext;
import coyote.commons.FileUtil;
import coyote.commons.StringUtil;
import coyote.loader.log.Log;
import coyote.loader.log.LogMsg;


/**
 * Make a copy of the source file in the target location.
 */
public class Copy extends AbstractFileTask {

  @Override
  public void execute( final TransformContext transformContext ) throws TaskException {

    // determine our configuration settings
    final String source = getString( ConfigTag.SOURCE );
    final String sourcedir = getString( ConfigTag.FROMDIR );
    final String target = getString( ConfigTag.TARGET );
    final String targetdir = getString( ConfigTag.TODIR );
    final String pattern = getString( ConfigTag.PATTERN );

    // recurse the source directory, defaults to false
    boolean recurse = getBoolean( ConfigTag.RECURSE );

    // preserve hierarchy, defaults to false (this is an edge case)
    boolean preserveHierarchy = getBoolean( ConfigTag.PRESERVE );

    // overwrite existing files
    boolean overwrite = true;
    if ( contains( ConfigTag.OVERWRITE ) ) {
      overwrite = getBoolean( ConfigTag.OVERWRITE );
    }

   
    // do the work
    if ( StringUtil.isNotBlank( source ) ) {
      final String src = resolveArgument( source );

      if ( StringUtil.isNotBlank( target ) ) {
        // this is a file to file copy
        final String tgt = resolveArgument( target );
        Log.info( LogMsg.createMsg( Batch.MSG, "Task.Copying file named {} to file named {}", src, tgt ) );

        try {
          FileUtil.copyFile( src, tgt );
        } catch ( final IOException e ) {
          if ( haltOnError ) {
            transformContext.setError( String.format( "Copy operation '%s' to '%s' failed: %s", src, tgt, e.getMessage() ) );
            return;
          }
        }

      } else if ( StringUtil.isNotBlank( targetdir ) ) {
        // this is a file to directory copy
        final String tgt = resolveArgument( targetdir );
        Log.info( LogMsg.createMsg( Batch.MSG, "Task.Copying file named {} to directory named {}", src, tgt ) );

        try {
          FileUtil.copyFileToDir( src, tgt );
        } catch ( final IOException e ) {
          if ( haltOnError ) {
            transformContext.setError( String.format( "Copy operation '%s' to '%s' failed: %s", src, tgt, e.getMessage() ) );
            return;
          }
        }
      } else {
        Log.info( "Cannot copy without a target" );
        if ( haltOnError ) {
          transformContext.setError( "Copy operation failed: no target argument" );
          return;
        }
      }

    } else if ( StringUtil.isNotBlank( sourcedir ) ) {

      // This appears to be a directory-based copy

      final String src = resolveArgument( sourcedir );

      if ( StringUtil.isNotBlank( targetdir ) ) {
        // this is a directory to directory copy
        final String tgt = resolveArgument( targetdir );
        Log.info( LogMsg.createMsg( Batch.MSG, "Task.copying_directory", src, tgt, pattern, preserveHierarchy, recurse, overwrite ) );

        // the most common use case is to perform a flat copy, essentially moving all the discovered files from one directory to another
        if ( preserveHierarchy ) {

          try {
            // this preserves hierarchy...is this what is desired?
            FileUtil.copyDirectory( src, tgt );
          } catch ( final IOException e ) {
            if ( haltOnError ) {
              transformContext.setError( String.format( "Copy operation '%s' to '%s' failed: %s", src, tgt, e.getMessage() ) );
              return;
            }
          }
        } else {

        }

      } else {
        String msg = LogMsg.createMsg( Batch.MSG, "Task.copy_target_directory_missing" ).toString();
        Log.warn( msg );

        if ( haltOnError ) {
          transformContext.setError( msg );
          return;
        }
      }

    } else {
      Log.info( "Cannot copy without a source" );
      if ( haltOnError ) {
        transformContext.setError( "Copy operation failed: no source argument" );
        return;
      }
    }

  }
}
