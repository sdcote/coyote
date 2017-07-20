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
 * Read a text file into the context as a set of name-value pairs.
 * 
 * <p>This allows a job to be populated with context variables at runtime.
 * 
 * <p>The file is assumed to be a simple text file containing name-value 
 * pairs. Each line is expected to contain one pair delimited with an equals 
 * '=' sign. Other characters can be specified as a delimiter.
 */
public class ReadIntoContext extends AbstractFileTask {

  private static final String DEFAULT_DELIMITER = "=";




  /**
   * @see coyote.dx.task.AbstractTransformTask#performTask()
   */
  @Override
  protected void performTask() throws TaskException {
    String delimiter = getDelimiter();
    String source = getSourceOrFile();
    if ( StringUtil.isNotBlank( source ) ) {
      Log.debug( "Using a filename of '" + source + "'" );;
      final File file = getFile( source );
      Log.debug( "Using absolute filename of '" + file.getAbsolutePath() + "'" );;

      if ( file.exists() ) {
        if ( file.canRead() ) {
          if ( file.length() > 0 ) {
            String[] lines = FileUtil.textToArray( file );
            Log.info( "Read in " + lines.length + " lines" );
            for ( String line : lines ) {
              String[] kvp = divide( line, delimiter );
              if ( StringUtil.isNotEmpty( kvp[1] ) ) {
                getContext().set( kvp[0], kvp[1] );
                Log.debug( "Recording '" + kvp[0] + "' in contex as '" + kvp[1] + "'" );
              } else {
                Log.warn( "No delimiter for line: '" + line + "'" );
              }
            }
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









  /**
   * @param line
   * @param delimiter
   * @return
   */
  private String[] divide( String line, String delimiter ) {
    String[] retval = new String[2];
    final int indx = line.indexOf( delimiter );
    if ( indx != -1 ) {
      retval[0] = line.substring( 0, indx );
      if ( indx < line.length() ) {
        retval[1] = line.substring( indx, line.length() );
      }
    } else {
      retval[0] = line;
    }

    return retval;
  }




  /**
   * @return the string which delimits the key from the value
   */
  private String getDelimiter() {
    String separator = getString( ConfigTag.DELIMITER );
    if ( StringUtil.isNotBlank( separator ) ) {
      return separator;
    }
    return DEFAULT_DELIMITER;
  }

}
