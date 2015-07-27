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


/**
 * 
 */
public class LogContext extends AbstractFileTask {
  final Logger log = LoggerFactory.getLogger( getClass() );




  /**
   * @see coyote.batch.TransformTask#execute(coyote.batch.TransformContext)
   */
  @Override
  public void execute( TransformContext transformContext ) throws TaskException {
    log.info( transformContext.dump() );

  }

}
