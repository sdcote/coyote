/*
 * Copyright (c) 2017 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 *
 * Contributors:
 *   Stephan D. Cote 
 *      - Initial concept and initial implementation
 */
package coyote.batch.listener;

import coyote.batch.ContextListener;
import coyote.batch.OperationalContext;
import coyote.batch.TransformContext;

/**
 * This sends email when the transform completes.
 * 
 * <p>The normal use case is to send an email if a job fails, but there are 
 * also times when people want to be notified when a particular job runs and 
 * its output is ready to be viewed or processed. This listener supports both. 
 */
public class EmailTransformListener  extends AbstractListener implements ContextListener {

  /**
   * @see coyote.batch.listener.AbstractListener#onEnd(coyote.batch.OperationalContext)
   */
  @Override
  public void onEnd( OperationalContext context ) {

    if ( context instanceof TransformContext ) {
      //
      
    }
  }
  

}
