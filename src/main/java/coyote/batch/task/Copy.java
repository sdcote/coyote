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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import coyote.batch.ConfigTag;
import coyote.batch.TaskException;
import coyote.batch.TransformContext;
import coyote.commons.FileUtil;
import coyote.commons.StringUtil;


/**
 * Make a copy of the source file in the target location.
 */
public class Copy extends AbstractFileTask {
  final Logger log = LoggerFactory.getLogger( getClass() );




  @Override
  public void execute( final TransformContext transformContext ) throws TaskException {

    // determine our configuration settings
    final String source = getString( ConfigTag.SOURCE );
    final String sourcedir = getString( ConfigTag.FROMDIR );
    final String target = getString( ConfigTag.TARGET );
    final String targetdir = getString( ConfigTag.TODIR );
    if ( contains( ConfigTag.HALT ) ) {
      setHaltOnError( getBoolean( ConfigTag.HALT ) );
    }

    // do the work
    if ( StringUtil.isNotBlank( source ) ) {
      final String src = resolveArgument( source );

      if ( StringUtil.isNotBlank( target ) ) {
        // this is a file to file copy
        final String tgt = resolveArgument( target );
        log.info( "Copying file named {} to file named {}", src, tgt );

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
        log.info( "Copying file named {} to directory named {}", src, tgt );

        try {
          FileUtil.copyFileToDir( src, tgt );
        } catch ( final IOException e ) {
          if ( haltOnError ) {
            transformContext.setError( String.format( "Copy operation '%s' to '%s' failed: %s", src, tgt, e.getMessage() ) );
            return;
          }
        }
      } else {
        log.info( "Cannot copy without a target" );
        if ( haltOnError ) {
          transformContext.setError( "Copy operation failed: no target argument" );
          return;
        }
      }

    } else if ( StringUtil.isNotBlank( sourcedir ) ) {
      final String src = resolveArgument( sourcedir );

      if ( StringUtil.isNotBlank( targetdir ) ) {
        // this is a directory to directory copy
        final String tgt = resolveArgument( targetdir );
        log.info( "Copying directory named {} to directory named {}", src, tgt );
        try {
          FileUtil.copyDirectory( src, tgt );
        } catch ( final IOException e ) {
          if ( haltOnError ) {
            transformContext.setError( String.format( "Copy operation '%s' to '%s' failed: %s", src, tgt, e.getMessage() ) );
            return;
          }
        }

      } else {
        log.info( "Cannot copy a directory without a target directory" );
        if ( haltOnError ) {
          transformContext.setError( "Copy operation failed: no target directory argument" );
          return;
        }
      }

    } else {
      log.info( "Cannot copy without a source" );
      if ( haltOnError ) {
        transformContext.setError( "Copy operation failed: no source argument" );
        return;
      }
    }

  }
}
