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
package coyote.dx.listener;

import coyote.dx.context.OperationalContext;
import coyote.dx.context.TransformContext;


/**
 * Send an email when the context ends and is in error.
 */
public class FailureEmailListener extends AbstractEmailListener {

  private final String SUBJECT = "Data Transfer Job '[#$JobName#]' Failed";
  private final String MESSAGE = "An error occurred in the '[#$JobName#]' data transfer job with the ID of [#$JobId#] at [#$RunDateTime#].\n\nThe job status was\n[#$ContextStatus#]\n\nThe error message was:\n[#$ContextError#]\n\nCheck the job logs for more details.";




  /**
   * @see coyote.dx.listener.AbstractEmailListener#getDefaultSubject()
   */
  @Override
  String getDefaultSubject() {
    return SUBJECT;
  }




  /**
   * @see coyote.dx.listener.AbstractEmailListener#getDefaultBody()
   */
  @Override
  String getDefaultBody() {
    return MESSAGE;
  }




  /**
   * @see coyote.dx.listener.AbstractListener#onEnd(coyote.dx.context.OperationalContext)
   */
  @Override
  public void onEnd( OperationalContext context ) {
    if ( context instanceof TransformContext && context.isInError() ) {
      sendMessage(context);
    }
  }

}
