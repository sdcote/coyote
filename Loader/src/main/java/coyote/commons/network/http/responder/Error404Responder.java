/*
 * Copyright (c) 2004 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 *
 * Contributors:
 *   Stephan D. Cote 
 */
package coyote.commons.network.http.responder;

import coyote.commons.network.http.IStatus;
import coyote.commons.network.http.Status;


/**
 * Handling error 404 - unrecognized URIs
 */
public class Error404Responder extends DefaultResponder {

  @Override
  public String getMimeType() {
    return "text/html";
  }




  @Override
  public IStatus getStatus() {
    return Status.NOT_FOUND;
  }




  @Override
  public String getText() {
    return "<html><body><h3>Error 404</h3><p>The requested resource does not exist.</p></body></html>";
  }
}