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
 * Send an email when the context ends and is not in error.
 */
public class SuccessEmailListener extends AbstractEmailListener {

  private final String SUBJECT = "Data Transfer Job '[#$JobName#]' Completed";
  private final String MESSAGE = "The [#$JobName#], data transfer job with the ID of [#$JobId#] completed sucessfully at [#$RunDateTime#].";




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

    if ( context instanceof TransformContext && context.isNotInError() ) {
      sendMessage(context);
    }
  }





}
