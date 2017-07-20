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

import coyote.commons.FileUtil;
import coyote.commons.StringUtil;
import coyote.dx.CDX;
import coyote.dx.ConfigTag;
import coyote.dx.TaskException;
import coyote.loader.log.Log;
import coyote.loader.log.LogMsg;


/**
 * Save a context variable to a file.
 * 
 * <p>File will contain the string value of the named context variable. If the 
 * named value is not found, the file will be empty.
 * 
 * <p>The value will be treated as a template and resolved with values from 
 * the symbol table in the context.
 */
public class SaveContextValue extends AbstractFileTask {

  /**
   * @see coyote.dx.task.AbstractTransformTask#performTask()
   */
  @Override
  protected void performTask() throws TaskException {

    // don't resolve the value, but get the actual configuration attribute
    final String source = getConfiguration().getString( ConfigTag.SOURCE );

    if ( StringUtil.isNotEmpty( source ) ) {
      final String target = getTargetOrFile();
      if ( StringUtil.isNotBlank( target ) ) {
        Log.debug( "Using a filename of '" + target + "'" );;
        final File file = getAbsoluteFile( target );
        Log.debug( "Using absolute filename of '" + file.getAbsolutePath() + "'" );
        String contextVariable = getContext().getAsString( source );
        if ( StringUtil.isNotEmpty( contextVariable ) ) {
          if ( FileUtil.stringToFile( contextVariable, file.getAbsolutePath() ) ) {
            Log.debug( "Wrote context variable '" + source + "' ( " + contextVariable.length() + "chars) to " + file.getAbsolutePath() );
          } else {
            final String msg = LogMsg.createMsg( CDX.MSG, "%s failed: Write failed to %s (%s)", getClass().getSimpleName(), target, file.getAbsolutePath() ).toString();
            Log.error( msg );
            if ( haltOnError ) {
              getContext().setError( msg );
              return;
            }
          }
        } else {
          final String msg = LogMsg.createMsg( CDX.MSG, "%s failed: Context did not contain a value for '%s'", getClass().getSimpleName(), source ).toString();
          Log.error( msg );
          if ( haltOnError ) {
            getContext().setError( msg );
            return;
          }
        }

      }
    } else {
      final String msg = LogMsg.createMsg( CDX.MSG, "%s failed: No source (context key) configured", getClass().getSimpleName() ).toString();
      Log.error( msg );
      if ( haltOnError ) {
        getContext().setError( msg );
        return;
      }

    }
    Log.fatal( getClass().getSimpleName() + ": Not implemented" );
  }

}
