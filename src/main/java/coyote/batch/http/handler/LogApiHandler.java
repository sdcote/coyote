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
package coyote.batch.http.handler;

import java.util.Map;

import coyote.commons.network.MimeType;
import coyote.commons.network.http.IHTTPSession;
import coyote.commons.network.http.Response;
import coyote.commons.network.http.Status;
import coyote.commons.network.http.auth.Auth;
import coyote.commons.network.http.handler.UriResource;
import coyote.commons.network.http.handler.UriResponder;


/**
 * 
 */
public class LogApiHandler extends AbstractBatchHandler implements UriResponder {

  /**
   * Retrieve contents of a log if a log is identified.
   * Retrieve a list of logs if no log is identified.
   * 
   * @see coyote.batch.http.handler.AbstractBatchHandler#get(coyote.commons.network.http.handler.UriResource, java.util.Map, coyote.commons.network.http.IHTTPSession)
   */
  @Override
  @Auth(groups = "devop", requireSSL = true)
  public Response get( UriResource uriResource, Map<String, String> urlParams, IHTTPSession session ) {
    return Response.createFixedLengthResponse( Status.OK, MimeType.HTML.getType(), HandlerUtil.getDebugText( urlParams, session ) );
  }

}