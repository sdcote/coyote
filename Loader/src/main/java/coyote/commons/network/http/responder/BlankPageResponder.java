/*
 * Copyright (c) 2016 Stephan D. Cote' - All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the MIT License which accompanies this distribution, and is
 * available at http://creativecommons.org/licenses/MIT/
 */

package coyote.commons.network.http.responder;

import coyote.commons.network.http.IStatus;
import coyote.commons.network.http.Status;


/**
 * This responds with a blank page.
 *
 * <p>Useful to handle URLs which should respond, but not return any data.
 */
public class BlankPageResponder extends DefaultResponder {

  /**
   * @see coyote.commons.network.http.responder.DefaultStreamResponder#getMimeType()
   */
  @Override
  public String getMimeType() {
    return "text/html";
  }




  /**
   * @see coyote.commons.network.http.responder.DefaultResponder#getStatus()
   */
  @Override
  public IStatus getStatus() {
    return Status.OK;
  }




  /**
   * @see coyote.commons.network.http.responder.DefaultResponder#getText()
   */
  @Override
  public String getText() {
    return "<html><body></body></html>";
  }

}