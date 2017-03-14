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

import coyote.commons.network.http.IHTTPSession;
import coyote.commons.network.http.Response;
import coyote.commons.network.http.handler.UriResource;
import coyote.commons.network.http.handler.UriResponder;


/**
 * This handler reports the health of the service.
 * 
 * <p>It is designed to be called repeatedly. If anything else but a 200 status 
 * code is received, the client can assume there are problems. 
 */
public class HealthCheckHandler extends AbstractBatchHandler implements UriResponder {

  /**
   * @see coyote.batch.http.handler.AbstractBatchHandler#get(coyote.commons.network.http.handler.UriResource, java.util.Map, coyote.commons.network.http.IHTTPSession)
   */
  @Override
  public Response get( UriResource uriResource, Map<String, String> urlParams, IHTTPSession session ) {
    return Response.createFixedLengthResponse( getStatus(), getMimeType(), "UP" );
  }

}
