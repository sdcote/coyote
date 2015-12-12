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

import coyote.batch.TaskException;
import coyote.batch.TransformContext;
import coyote.commons.StringUtil;
import coyote.loader.log.Log;


/**
 * A task which generates a log entry
 */
public class LogEntry extends AbstractTransformTask {

  public static final String LEVEL = "level";
  public static final String MESSAGE = "msg";




  /**
   * @see coyote.batch.TransformTask#execute()
   */
  @Override
  public void execute() throws TaskException {

    String message = resolveArgument( MESSAGE );
    if ( StringUtil.isNotBlank( message ) ) {
      String level = resolveArgument( LEVEL );
      if ( StringUtil.isNotBlank( level ) ) {
        if ( "info".equalsIgnoreCase( level ) ) {
          Log.info( message );
        } else if ( "debug".equalsIgnoreCase( level ) ) {
          Log.debug( message );
        } else if ( "warn".equalsIgnoreCase( level ) ) {
          Log.warn( message );
        } else if ( "error".equalsIgnoreCase( level ) ) {
          Log.error( message );
        } else if ( "trace".equalsIgnoreCase( level ) ) {
          Log.trace( message );
        } else {
          Log.info( message );
        }
      } else {
        Log.info( message );
      }
    }

  }

}
