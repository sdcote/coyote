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
package coyote.commons.network.http.responder;

import java.util.Map;

import coyote.commons.StringUtil;
import coyote.commons.network.MimeType;
import coyote.commons.network.http.IHTTPSession;
import coyote.commons.network.http.IStatus;
import coyote.commons.network.http.Response;
import coyote.commons.network.http.Status;


/**
 * This is a test responder specifically for testing the URI parameters passed to
 */
public class ParamResponder implements Responder {
  public static final String UNSPECIFIED = "Unspecified";
  private String responseText = UNSPECIFIED;




  @Override
  public Response delete( Resource resource, Map<String, String> urlParams, IHTTPSession session ) {
    if ( StringUtil.isNotBlank( urlParams.get( "name" ) ) ) {
      responseText = urlParams.get( "name" );
    }
    return Response.createFixedLengthResponse( getStatus(), getMimeType(), getText() );
  }




  @Override
  public Response get( Resource resource, Map<String, String> urlParams, IHTTPSession session ) {
    if ( StringUtil.isNotBlank( urlParams.get( "name" ) ) ) {
      responseText = urlParams.get( "name" );
    }
    return Response.createFixedLengthResponse( getStatus(), getMimeType(), getText() );
  }




  @Override
  public Response other( String method, Resource resource, Map<String, String> urlParams, IHTTPSession session ) {
    if ( StringUtil.isNotBlank( urlParams.get( "name" ) ) ) {
      responseText = urlParams.get( "name" );
    }
    return Response.createFixedLengthResponse( getStatus(), getMimeType(), getText() );
  }




  @Override
  public Response post( Resource resource, Map<String, String> urlParams, IHTTPSession session ) {
    if ( StringUtil.isNotBlank( urlParams.get( "name" ) ) ) {
      responseText = urlParams.get( "name" );
    }
    return Response.createFixedLengthResponse( getStatus(), getMimeType(), getText() );
  }




  @Override
  public Response put( Resource resource, Map<String, String> urlParams, IHTTPSession session ) {
    if ( StringUtil.isNotBlank( urlParams.get( "name" ) ) ) {
      responseText = urlParams.get( "name" );
    }
    return Response.createFixedLengthResponse( getStatus(), getMimeType(), getText() );
  }




  /**
   * @return
   */
  private String getText() {
    return responseText;
  }




  /**
   * @return
   */
  private String getMimeType() {
    return MimeType.TEXT.getType();
  }




  /**
   * @return
   */
  private IStatus getStatus() {
    return Status.OK;
  }

}
