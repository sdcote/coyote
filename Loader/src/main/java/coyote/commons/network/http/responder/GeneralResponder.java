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

import java.util.Map;

import coyote.commons.network.http.HTTPD;
import coyote.commons.network.http.IHTTPSession;
import coyote.commons.network.http.IStatus;
import coyote.commons.network.http.Response;
import coyote.commons.network.http.Status;


/**
 * Generic responder to print debug info as a html page.
 */
public class GeneralResponder extends DefaultResponder {

  @Override
  public Response get( final Resource resource, final Map<String, String> urlParams, final IHTTPSession session ) {
    final StringBuilder text = new StringBuilder( "<html><body>" );
    text.append( "<h1>Url: " );
    text.append( session.getUri() );
    text.append( "</h1><br>" );
    final Map<String, String> queryParams = session.getParms();
    if ( queryParams.size() > 0 ) {
      for ( final Map.Entry<String, String> entry : queryParams.entrySet() ) {
        final String key = entry.getKey();
        final String value = entry.getValue();
        text.append( "<p>Param '" );
        text.append( key );
        text.append( "' = " );
        text.append( value );
        text.append( "</p>" );
      }
    } else {
      text.append( "<p>no params in url</p><br>" );
    }
    return Response.createFixedLengthResponse( getStatus(), getMimeType(), text.toString() );
  }




  @Override
  public String getMimeType() {
    return "text/html";
  }




  @Override
  public IStatus getStatus() {
    return Status.OK;
  }




  @Override
  public String getText() {
    throw new IllegalStateException( "This method should not be called" );
  }

}