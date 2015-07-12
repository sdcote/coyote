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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import coyote.batch.ConfigTag;
import coyote.batch.TaskException;
import coyote.batch.TransformContext;
import coyote.commons.FileUtil;
import coyote.commons.StringUtil;


/**
 * Delete a file or a directory from the file system.
 */
public class Delete extends AbstractFileTask {
  final Logger log = LoggerFactory.getLogger( getClass() );




  /**
   * @see coyote.batch.TransformTask#execute(coyote.batch.TransformContext)
   */
  @Override
  public void execute( TransformContext transformContext ) throws TaskException {
    final String filename = getString( ConfigTag.FILE );
    final String directory = getString( ConfigTag.DIRECTORY );
    if ( contains( ConfigTag.HALT ) ) {
      setHaltOnError( getBoolean( ConfigTag.HALT ) );
    }

    if ( StringUtil.isNotBlank( filename ) ) {
      final String file = resolveArgument( filename );
      log.info( "Deleting file named {}", file );

      try {
        FileUtil.deleteFile( file );
      } catch ( final Exception e ) {
        if ( haltOnError ) {
          transformContext.setError( String.format( "Delete file operation '%s' failed: %s", file, e.getMessage() ) );
          return;
        }
      }

    } else if ( StringUtil.isNotBlank( directory ) ) {
      final String dir = resolveArgument( directory );
      log.info( "Deleting directory named {}", dir );

      try {
        FileUtil.clearDir( dir, true, true );
      } catch ( final Exception e ) {
        if ( haltOnError ) {
          transformContext.setError( String.format( "Delete directory operation '%s' failed: %s", dir, e.getMessage() ) );
          return;
        }
      }

    } else {
      log.warn( "Move has no {} or {} argument - nothing to do.", ConfigTag.FILE, ConfigTag.DIRECTORY );
    }

  }

}
