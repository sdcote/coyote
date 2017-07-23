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

import java.io.File;

import coyote.commons.StringUtil;
import coyote.dx.CDX;
import coyote.dx.ConfigTag;
import coyote.dx.TaskException;
import coyote.loader.log.Log;
import coyote.loader.log.LogMsg;


/**
 * Check the size of the given file.
 */
public class CheckSize extends AbstractFileTask {

  /**
   * @see coyote.dx.task.AbstractTransformTask#performTask()
   */
  @Override
  protected void performTask() throws TaskException {
    final String source = getSourceOrFile();
    if ( StringUtil.isNotBlank( source ) ) {
      Log.debug( getClass().getSimpleName() + " using a filename of '" + source + "'" );;
      final File file = getExistingFile( source );
      Log.debug( getClass().getSimpleName() + " using absolute filename of '" + file.getAbsolutePath() + "'" );;

      if ( file.exists() ) {
        String attrName = getConfiguration().getString( ConfigTag.CONTEXT );

        String value = getContext().getAsString( attrName );
        Log.info( "Size should be " + value );
        long size;

        try {
          size = Long.parseLong( value );
        } catch ( NumberFormatException e ) {
          final String msg = LogMsg.createMsg( CDX.MSG, "%s failed: Context attribute %s does not contain a valid numeric (%s)", getClass().getSimpleName(), attrName, value ).toString();
          if ( haltOnError ) {
            throw new TaskException( msg );
          } else {
            Log.error( msg );
            return;
          }
        }

        if ( file.length() > 0 ) {
          if ( file.length() == size ) {
            Log.info( "File size verified for " + file.getAbsolutePath() );
          } else {
            final String msg = LogMsg.createMsg( CDX.MSG, "File size verification failed for '%s'  expecting %d was actually %d", source, file.getAbsolutePath(), size, file.length() ).toString();
            if ( haltOnError ) {
              throw new TaskException( msg );
            } else {
              Log.error( msg );
              return;
            }
          }
        } else {
          Log.warn( LogMsg.createMsg( CDX.MSG, "%s did not read any data from %s - empty file (%s)", getClass().getSimpleName(), source, file.getAbsolutePath() ) );
        }
      } else {
        final String msg = LogMsg.createMsg( CDX.MSG, "Task.failed_file_does_not_exist", getClass().getSimpleName(), source, file.getAbsolutePath() ).toString();
        if ( haltOnError ) {
          throw new TaskException( msg );
        } else {
          Log.error( msg );
          return;
        }
      }
    } else {
      final String msg = LogMsg.createMsg( CDX.MSG, "%s failed: No data in %s configuration attribute", getClass().getSimpleName(), ConfigTag.SOURCE ).toString();
      if ( haltOnError ) {
        throw new TaskException( msg );
      } else {
        Log.error( msg );
        return;
      }
    }
  }

}
