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
package coyote.batch.http.nugget;

import java.util.Map;

import coyote.commons.network.MimeType;
import coyote.commons.network.http.IHTTPSession;
import coyote.commons.network.http.IStatus;
import coyote.commons.network.http.Response;
import coyote.commons.network.http.Status;
import coyote.commons.network.http.nugget.UriResource;
import coyote.commons.network.http.nugget.UriResponder;


/**
 * 
 */
public class LogApiHandler extends AbstractBatchNugget implements UriResponder {

  /**
   * Retrieve contents of a log if a log is identified.
   * Retrieve a list of logs if no log is identified.
   * 
   * @see coyote.batch.http.nugget.AbstractBatchNugget#get(coyote.commons.network.http.nugget.UriResource, java.util.Map, coyote.commons.network.http.IHTTPSession)
   */
  @Override
  public Response get( UriResource uriResource, Map<String, String> urlParams, IHTTPSession session ) {
    final StringBuilder text = new StringBuilder( "<html><body>" );
    text.append( "<h1>URL: " );
    text.append( session.getUri() );
    text.append( "</h1><br>" );

    if ( urlParams.size() > 0 ) {
      for ( final Map.Entry<String, String> entry : urlParams.entrySet() ) {
        final String key = entry.getKey();
        final String value = entry.getValue();
        text.append( "<p>URI Param '" );
        text.append( key );
        text.append( "' = " );
        text.append( value );
        text.append( "</p>" );
      }
    } else {
      text.append( "<p>No parameters parsed from URI</p><br>" );
    }

    final Map<String, String> queryParams = session.getParms();
    if ( queryParams.size() > 0 ) {
      for ( final Map.Entry<String, String> entry : queryParams.entrySet() ) {
        final String key = entry.getKey();
        final String value = entry.getValue();
        text.append( "<p>Query String Param '" );
        text.append( key );
        text.append( "' = " );
        text.append( value );
        text.append( "</p>" );
      }
    } else {
      text.append( "<p>No query params in URL</p><br>" );
    }
    return Response.createFixedLengthResponse( getStatus(), getMimeType(), text.toString() );
  }




  /**
   * Add entries to an identified log.
   * 
   * @see coyote.batch.http.nugget.AbstractBatchNugget#put(coyote.commons.network.http.nugget.UriResource, java.util.Map, coyote.commons.network.http.IHTTPSession)
   */
  @Override
  public Response put( UriResource uriResource, Map<String, String> urlParams, IHTTPSession session ) {
    return super.put( uriResource, urlParams, session );
  }




  /**
   * Delete an entire log.
   * 
   * @see coyote.batch.http.nugget.AbstractBatchNugget#delete(coyote.commons.network.http.nugget.UriResource, java.util.Map, coyote.commons.network.http.IHTTPSession)
   */
  @Override
  public Response delete( UriResource uriResource, Map<String, String> urlParams, IHTTPSession session ) {
    return super.delete( uriResource, urlParams, session );
  }




  /**
   * @see coyote.commons.network.http.nugget.DefaultHandler#getStatus()
   */
  @Override
  public IStatus getStatus() {
    return Status.OK;
  }




  /**
   * @see coyote.commons.network.http.nugget.DefaultHandler#getText()
   */
  @Override
  public String getText() {
    return "";
  }




  /**
   * @see coyote.commons.network.http.nugget.DefaultStreamHandler#getMimeType()
   */
  @Override
  public String getMimeType() {
    return MimeType.HTML.getType();
  }

}
