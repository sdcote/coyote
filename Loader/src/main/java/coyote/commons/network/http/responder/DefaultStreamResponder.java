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

import java.io.InputStream;
import java.util.Map;

import coyote.commons.network.http.HTTPD;
import coyote.commons.network.http.IHTTPSession;
import coyote.commons.network.http.IStatus;
import coyote.commons.network.http.Response;


/**
 * General responder to subclass when you provide stream data. Only chucked
 * responses will be generated.
 */
public abstract class DefaultStreamResponder implements Responder {

  @Override
  public Response delete( final Resource resource, final Map<String, String> urlParams, final IHTTPSession session ) {
    return get( resource, urlParams, session );
  }




  @Override
  public Response get( final Resource resource, final Map<String, String> urlParams, final IHTTPSession session ) {
    return Response.createChunkedResponse( getStatus(), getMimeType(), getData() );
  }




  public abstract InputStream getData();




  public abstract String getMimeType();




  public abstract IStatus getStatus();




  @Override
  public Response other( final String method, final Resource resource, final Map<String, String> urlParams, final IHTTPSession session ) {
    return get( resource, urlParams, session );
  }




  @Override
  public Response post( final Resource resource, final Map<String, String> urlParams, final IHTTPSession session ) {
    return get( resource, urlParams, session );
  }




  @Override
  public Response put( final Resource resource, final Map<String, String> urlParams, final IHTTPSession session ) {
    return get( resource, urlParams, session );
  }
}