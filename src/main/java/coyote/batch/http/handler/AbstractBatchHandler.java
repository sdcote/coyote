/*
 * Copyright (c) 2016 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 *
 * Contributors:
 *   Stephan D. Cote 
 *      - Initial concept and initial implementation
 */
package coyote.batch.http.handler;

import java.util.Map;

import coyote.commons.network.MimeType;
import coyote.commons.network.http.IHTTPSession;
import coyote.commons.network.http.IStatus;
import coyote.commons.network.http.Response;
import coyote.commons.network.http.Status;
import coyote.commons.network.http.handler.DefaultHandler;
import coyote.commons.network.http.handler.UriResource;
import coyote.commons.network.http.handler.UriResponder;
import coyote.dataframe.DataFrame;
import coyote.dataframe.marshal.JSONMarshaler;


/**
 * Base class for all batch handlers.
 */
public abstract class AbstractBatchHandler extends DefaultHandler implements UriResponder {

  protected Status status = Status.OK;

  protected DataFrame results = new DataFrame();




  /**
   * @see coyote.commons.network.http.handler.UriResponder#delete(coyote.commons.network.http.handler.UriResource, java.util.Map, coyote.commons.network.http.IHTTPSession)
   */
  @Override
  public Response delete( UriResource uriResource, Map<String, String> urlParams, IHTTPSession session ) {
    return Response.createFixedLengthResponse( getStatus(), getMimeType(), getText() );
  }




  /**
   * @see coyote.commons.network.http.handler.UriResponder#get(coyote.commons.network.http.handler.UriResource, java.util.Map, coyote.commons.network.http.IHTTPSession)
   */
  @Override
  public Response get( UriResource uriResource, Map<String, String> urlParams, IHTTPSession session ) {
    return Response.createFixedLengthResponse( getStatus(), getMimeType(), getText() );
  }




  /**
   * @see coyote.commons.network.http.handler.UriResponder#other(java.lang.String, coyote.commons.network.http.handler.UriResource, java.util.Map, coyote.commons.network.http.IHTTPSession)
   */
  @Override
  public Response other( String method, UriResource uriResource, Map<String, String> urlParams, IHTTPSession session ) {
    return Response.createFixedLengthResponse( getStatus(), getMimeType(), getText() );
  }




  /**
   * @see coyote.commons.network.http.handler.UriResponder#post(coyote.commons.network.http.handler.UriResource, java.util.Map, coyote.commons.network.http.IHTTPSession)
   */
  @Override
  public Response post( UriResource uriResource, Map<String, String> urlParams, IHTTPSession session ) {
    return Response.createFixedLengthResponse( getStatus(), getMimeType(), getText() );
  }




  /**
   * @see coyote.commons.network.http.handler.UriResponder#put(coyote.commons.network.http.handler.UriResource, java.util.Map, coyote.commons.network.http.IHTTPSession)
   */
  @Override
  public Response put( UriResource uriResource, Map<String, String> urlParams, IHTTPSession session ) {
    return Response.createFixedLengthResponse( getStatus(), getMimeType(), getText() );
  }




  /**
   * @see coyote.commons.network.http.handler.DefaultHandler#getStatus()
   */
  @Override
  public IStatus getStatus() {
    return status;
  }




  /**
   * @see coyote.commons.network.http.handler.DefaultHandler#getText()
   */
  @Override
  public String getText() {
    return JSONMarshaler.marshal( results );
  }




  /**
   * @see coyote.commons.network.http.handler.DefaultStreamHandler#getMimeType()
   */
  @Override
  public String getMimeType() {
    return MimeType.JSON.getType();
  }

}
