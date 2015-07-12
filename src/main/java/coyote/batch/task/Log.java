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

import coyote.batch.TaskException;
import coyote.batch.TransformContext;
import coyote.commons.StringUtil;


/**
 * A task which generates a log entry
 */
public class Log extends AbstractTransformTask {
  final Logger log = LoggerFactory.getLogger( getClass() );

  public static final String LEVEL = "level";
  public static final String MESSAGE = "msg";




  /**
   * @see coyote.batch.TransformTask#execute(coyote.batch.TransformContext)
   */
  @Override
  public void execute( TransformContext transformContext ) throws TaskException {

    String message = resolveArgument( MESSAGE );
    if ( StringUtil.isNotBlank( message ) ) {
      String level = resolveArgument( LEVEL );
      if ( StringUtil.isNotBlank( level ) ) {
        if ( "info".equalsIgnoreCase( level ) ) {
          log.info( message );
        } else if ( "debug".equalsIgnoreCase( level ) ) {
          log.debug( message );
        } else if ( "warn".equalsIgnoreCase( level ) ) {
          log.warn( message );
        } else if ( "error".equalsIgnoreCase( level ) ) {
          log.error( message );
        } else if ( "trace".equalsIgnoreCase( level ) ) {
          log.trace( message );
        } else {
          log.info( message );
        }
      } else {
        log.info( message );
      }
    }

  }

}
