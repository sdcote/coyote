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
 * Unzip the given file.
 */
public class Unzip extends AbstractFileTask {

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
        if ( file.canRead() ) {
          if ( file.length() > 0 ) {

            // TODO: get the target directory
            
            Log.fatal( getClass().getSimpleName() + ": Not fully implemented" );

          } else {
            Log.warn( LogMsg.createMsg( CDX.MSG, "%s did not read any data from %s - empty file (%s)", getClass().getSimpleName(), source, file.getAbsolutePath() ) );
          }
        } else {
          final String msg = LogMsg.createMsg( CDX.MSG, "%s failed: File %s cannot be read (%s)", getClass().getSimpleName(), source, file.getAbsolutePath() ).toString();
          Log.error( msg );
          if ( haltOnError ) {
            getContext().setError( msg );
            return;
          }
        }
      } else {
        final String msg = LogMsg.createMsg( CDX.MSG, "%s failed: File %s does not exist (%s)", getClass().getSimpleName(), source, file.getAbsolutePath() ).toString();
        Log.error( msg );
        if ( haltOnError ) {
          getContext().setError( msg );
          return;
        }
      }
    } else {
      final String msg = LogMsg.createMsg( CDX.MSG, "%s failed: No data in %s configuration attribute", getClass().getSimpleName(), ConfigTag.SOURCE ).toString();
      Log.error( msg );
      if ( haltOnError ) {
        getContext().setError( msg );
        return;
      }
    }
  }

}
